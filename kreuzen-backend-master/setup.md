# Kreuzen Setup

Dieses Dokument beschreibt den Prozess, um Kreuzen auf einem Root-Server zu deployen. Aufgrund der Architektur ist auch
ein Deployment in Cloud-Umgebungen wie Kubernetes denkbar, wird jedoch hier nicht weiter beschrieben.

## Benötigte Software

Folgene Software muss installiert werden, um Kreuzen bereitzustellen:
- [PostgreSQL (>= 10)](https://www.postgresql.org/)
- [NGINX](https://nginx.org/)
- [Java 11](https://openjdk.java.net/)

Lokal muss außerdem noch [NodeJS](https://nodejs.org/) und [npm](https://www.npmjs.com/) installiert werden, um das Frontend zu kompilieren.

Auf Ubuntu kann die benötigte Software beispielsweise mit folgenden Befehlen installiert werden:
```bash
# Postgres
## Create the file repository configuration:
sudo sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list'
## Import the repository signing key:
wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -
## Update the package lists:
sudo apt-get update
## Install the latest version of PostgreSQL.
sudo apt-get -y install postgresql

# NGINX
sudo apt-get -y install nginx

# Java 11
sudo apt-get -y install openjdk-11-jdk
```

NGINX könnte auch durch Apache oder andere Proxies ersetzt werden. Für Java 11 können auch andere Distributions,
beispielsweiße von Oracle, verwendet werden.

## NGINX Setup

NGINX wird als reverse Proxy verwendet, um Traffic zu dem Front- und Backend zu lenken. Außerdem kann NGINX genutzt
werden, um Verbindungen zu Clients mit SSL zu verschlüsseln.

Um in Nginx einen Serverblock als Reverse Proxy einzurichten, kann z.B. der default(`/etc/nginx/sites-available/default`) 
ersetzt werden. Wie ein neuer hinzugefügt wird, kann der NGINX Doku entnommen werden. Folgende Konfiguration kann verwendet werden:
```
server {
        listen 80 default_server;
        listen [::]:80 default_server;

        root /home/kreuzen/frontend;

        index index.html;

        server_name _;

        # Frontend Server
        location / {
                try_files $uri $uri/ /index.html =404;
        }

        # Reverse Proxy for Backend
        location /api/ {
                proxy_set_header Host $host;
                proxy_set_header X-Real-IP $remote_addr;
                proxy_pass http://localhost:8080/;
        }
}
```
Im Servername kann statt des defaults auch der Domainname verwendet werden. Auch das Root-Directory kann nach belieben
anders gewählt werden. Außerdem kann mit Certbot und Let's Encrypt automatisch SSL eingerichtet werden.

Damit NGINX keinen "Forbidden" Fehler zurückgibt, benötigt NGINX lese Berechtigungen auf alle bereitgestellten Files, 
sowie Ausführungsberechtigungen in allen Parent Verzeichnissen.
```bash
chmod 755 /home/kreuzen
chmod 755 /home/kreuzen/frontend
chmod 664 /home/kreuzen/frontend/*
```

## Postgres Setup

Zuerst muss eine neue Rolle angelegt werden. In den meisten Fällen sollte der Name des Nutzers verwendet werden, unter
dem Kreuzen laufen soll. Danach kann eine Datenbank für diesen Nutzer angelegt werden. Damit der Login über SSH ohne 
Passwort möglich ist, muss der Name der Rolle für die Datenbank verwendet werden.
```bash
createuser kreuzen
createdb kreuzen
```
Soll auf die 
Datenbank von extern (z.B. zur Entwicklung) müssen folgende Zeilen in die `pg_hba.conf` Datei hinzugefügt werden 
(Ubuntu mit PG 13 in: `/etc/postgresql/13/main/pg_hba.conf`):
```
host    all             all             0.0.0.0/0               md5
host    all             all             ::/0                    md5
```
Danach sollte die DB restartet werden. Unter Ubuntu geht dies mit `sudo service postgres restart`.

Damit das Backend sich mit der Datenbank verbinden kann, muss nun Passwort Login eingerichtet werden. Hierfür muss erstmal
eine Shell Verbindung zur Datenbank mit `psql` aufgebaut werden.

Das Passwort kann dann mit `ALTER USER kreuzen WITH ENCRYPTED PASSWORD 'passwort hier einfügen';` gesetzt werden.

Das Schema kann erstellt werden, indem alle Statements aus der `schema.sql` ausgeführt werden.

Da das Frontend momentan keine Möglichkeiten bietet, um Univsersitäten, Studiengänge und Studienabschnitte zu verwalten
müssen diese auch manuell angelegt werden.
Dies kann beispielsweiße so erreicht werden:
```sql
INSERT INTO university (name, allowed_mail_domains) VALUES ('TU Darmstadt', '{"stud.tu-darmstadt.de"}');
INSERT INTO major (university_id, name) VALUES (1, 'Informatik');
INSERT INTO major_section (major_id, name) VALUES (1, 'Bachelor');
INSERT INTO major_section (major_id, name) VALUES (1, 'Master');
```

## Frontend Setup

Bevor das Frontend kompiliert werden kann, muss es konfiguriert werden. Hierzu muss in der `.env` Datei
die Variable `REACT_APP_BACKEND_URL` auf die Url des Backends gesetzt werden. So muss dort z.B. 
`REACT_APP_BACKEND_URL=https://kreuzenonline.de/api` stehen.

Das Frontend kann lokal mit npm folgendermaßen kompiliert werden:
```
npm install
npm run build
```
Hierdurch wird ein Build im `build` Order erstellt. Der Inhalt des `build` Ordners muss nun auf den Webserver, in 
unserem Fall `/home/kreuzen/frontend`, kopiert werden.

## Backend Setup

Das Backend kann lokal oder auf dem Server mit Maven gebaut werden. Davor müssen jedoch noch die Konfigurationsdateien
ausgefüllt werden. Hierfür muss im Backend Ordner die `src/main/resources/application.properties` folgendermaßen 
ausgefüllt werden.
```
spring.datasource.url=jdbc:postgresql://IP_DES_SERVERS:5432/kreuzen
spring.datasource.username=kreuzen
spring.datasource.password=POSTGRES_PASSWORT
jwt.key=EIN_SECRET_KEY
app.base-url=https://DOMAIN
app.smtp.password=EMAIL_SERVER_PASSWORT
app.smtp.username=EMAIL_SERVER_USERNAME@DOMAIN
app.locale=de_DE
spring.jackson.serialization.write-dates-as-timestamps=false
```

Danach kann mit `./mvnw clean package` unter Linux oder `mvnw.cmd clean package` unter Windows ein Build erstellt werden.
Das Build wird unter `target/kreuzen.jar` gespeichert und kann mit `java -jar target/kreuzen.jar` ausgeführt werden.

Nun kann das Build auf den Server kopiert werden und dort ausgeführt werden.

Für ein Produktivsystem bietet es sich an die Ausführung mit einem Daemon einzurichten, damit das Backend im Hintergrund
läuft und automatisch mit dem Server startet. Folgendermaßen kann ein solcher Daemon unter Ubuntu eingerichtet werden:

Der Service wird definiert durch Erstellen von `/etc/systemd/system/kreuzen-backend.service` mit folgendem Inhalt:
```
[Unit]
Description=Kreuzen Backend

[Service]
WorkingDirectory=/home/kreuzen/backend
ExecStart=/usr/bin/java -jar /home/kreuzen/backend/kreuzen.jar
User=kreuzen
Type=simple
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```
In dieser Konfiguration wird die `kreuzen.jar` in dem Verzeichniss `/home/kreuzen/backend` erwartet.

Um den Service zu aktivieren, muss noch folgendes ausgeführt werden:

```bash
# Start Service
systemctl start kreuzen-backend
# Enable Start on Boot
systemctl enable kreuzen-backend
```

Der Log kann mit `sudo journalctl -u kreuzen-backend.service` ausgelesen werden.
