from mysql.connector import (connection)
import psycopg2
import csv
import math
import bcrypt
import secrets
import os
from pathlib import Path
import mimetypes


cnxMysql = connection.MySQLConnection(user='root', password='my-secret-pw', host='127.0.0.1', database='mysql')
cnxPgsql = psycopg2.connect(database="postgres", user="postgres", password="1234", host="127.0.0.1", port="5433")

Path("data").mkdir(parents=True, exist_ok=True)
Path("error").mkdir(parents=True, exist_ok=True)

PAGE_SIZE = 100


def generate_random_pw():
    return "{bcrypt}" + bcrypt.hashpw(secrets.token_urlsafe(64).encode('utf-8'), bcrypt.gensalt()).decode("utf-8")


def extract_table(name, query, csv_header, insert_query, process_data=lambda x: x):
    print("Processing {}".format(name))

    cursor_mysql = cnxMysql.cursor()
    cursor_pgsql = cnxPgsql.cursor()

    # Count entities
    cursor_mysql.execute("SELECT count(*) FROM ({}) x".format(query))
    count = cursor_mysql.fetchone()[0]
    print("Found {} rows".format(count))
    error_encountered = False
    with open('data/' + name + '.csv', 'w', newline='', encoding='utf-8') as csvFile:
        with open('error/' + name + '.csv', 'w', newline='', encoding='utf-8') as csvErrorFile:
            csv_writer = csv.writer(csvFile, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
            csv_error_writer = csv.writer(csvErrorFile, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
            csv_writer.writerow(csv_header)
            csv_error_writer.writerow(csv_header + ["error"])

            for i in range(math.ceil(count / PAGE_SIZE)):
                print("Processing {} - {}".format(i * PAGE_SIZE, min((i + 1) * PAGE_SIZE, count)))
                # Load Data
                cursor_mysql.execute("{} LIMIT {} OFFSET {} ".format(query, PAGE_SIZE, i * PAGE_SIZE))
                # Store
                for x in cursor_mysql:
                    x = process_data(x)
                    try:
                        cursor_pgsql.execute(insert_query, list(x))
                    except psycopg2.errors.UniqueViolation as err:
                        print("Unique Violation by {}".format(x))
                        error_encountered = True
                        csv_error_writer.writerow(list(x) + [str(err)])
                    except psycopg2.errors.ForeignKeyViolation as err:
                        print("Foreign Key Violation by {}".format(x))
                        error_encountered = True
                        csv_error_writer.writerow(list(x) + [str(err)])
                    except psycopg2.errors.NotNullViolation as err:
                        print("Not Null Violation by {}".format(x))
                        error_encountered = True
                        csv_error_writer.writerow(list(x) + [str(err)])
                    except psycopg2.errors.CheckViolation as err:
                        print("Check Violation by {}".format(x))
                        error_encountered = True
                        csv_error_writer.writerow(list(x) + [str(err)])
                    else:
                        csv_writer.writerow(list(x))
                    cnxPgsql.commit()
    if not error_encountered:
        os.remove('error/' + name + '.csv')

    # Fix index
    if 'id' in csv_header:
        cursor_pgsql.execute("""
        SELECT pg_catalog.setval(pg_get_serial_sequence('%s', 'id'), MAX(id)) FROM %s;
        """ % (name, name))

    cursor_pgsql.close()
    cursor_mysql.close()

    print("Finished processing {}".format(name))


def create_courses():
    print("Processing course")
    cursor_pgsql = cnxPgsql.cursor()
    cursor_pgsql.execute("""
    INSERT INTO course (module_id, semester_id) SELECT module.id AS module_id, semester.id AS semester_id FROM module, semester
    """)
    cnxPgsql.commit()
    cursor_pgsql.close()
    print("Finished processing course")


cursor = cnxPgsql.cursor()
cursor.execute("DROP TABLE IF EXISTS session_question_selection_temp")

cursor.execute("""
CREATE TABLE IF NOT EXISTS session_question_selection_temp (
    session_id int REFERENCES session(id),
    question_id int REFERENCES question_base(id),
    local_id int,
    is_checked bool,
    created_at timestamp,
    PRIMARY KEY (session_id, question_id, local_id)
)
""")
cursor.execute("""
CREATE OR REPLACE FUNCTION create_legacy_sc_question (question_id int, correct_answer_local_id int, a1 text, a2 text, a3 text, a4 text, a5 text)
RETURNS integer AS $$
BEGIN
    INSERT INTO question_single_choice(question_id, correct_answer_local_id, type) VALUES ($1, $2, 'single-choice');
    IF ($3 IS NOT NULL AND $3 != '') THEN
        INSERT INTO question_single_choice_answer(question_id, local_id, text) VALUES ($1, 1, $3);
    end if;
    IF ($4 IS NOT NULL AND $4 != '') THEN
        INSERT INTO question_single_choice_answer(question_id, local_id, text) VALUES ($1, 2, $4);
    end if;
    IF ($5 IS NOT NULL AND $5 != '') THEN
        INSERT INTO question_single_choice_answer(question_id, local_id, text) VALUES ($1, 3, $5);
    end if;
    IF ($6 IS NOT NULL AND $6 != '') THEN
        INSERT INTO question_single_choice_answer(question_id, local_id, text) VALUES ($1, 4, $6);
    end if;
    IF ($7 IS NOT NULL AND $7 != '') THEN
        INSERT INTO question_single_choice_answer(question_id, local_id, text) VALUES ($1, 5, $7);
    end if;
    RETURN 1;
END
$$ LANGUAGE plpgsql;
""")
cursor.execute("DROP TABLE IF EXISTS session_sc_question_temp")
cursor.execute("""
CREATE TABLE IF NOT EXISTS session_sc_question_temp (
    question_id int,
    correct_answer_local_id int,
    a0 text,
    a1 text,
    a2 text,
    a3 text,
    a4 text,
    PRIMARY KEY (question_id)
)
""")
cursor.execute("TRUNCATE app_user CASCADE")
cursor.execute("TRUNCATE login_attempt CASCADE")
cursor.execute("TRUNCATE app_user_has_major CASCADE")
cursor.execute("TRUNCATE app_user_has_major_section CASCADE")
cursor.execute("TRUNCATE semester CASCADE")
cursor.execute("TRUNCATE module CASCADE")
cursor.execute("TRUNCATE major_has_module CASCADE")
cursor.execute("TRUNCATE major_section_has_module CASCADE")
cursor.execute("TRUNCATE course CASCADE")
cursor.execute("TRUNCATE tag CASCADE")
cursor.execute("TRUNCATE exam CASCADE")
cursor.execute("TRUNCATE file CASCADE")
cursor.execute("TRUNCATE question_base CASCADE")
cursor.execute("TRUNCATE question_has_file CASCADE")
cursor.execute("TRUNCATE question_has_tag CASCADE")
cursor.execute("TRUNCATE question_has_comment CASCADE")
cursor.execute("TRUNCATE question_has_error CASCADE")
cursor.execute("TRUNCATE question_single_choice CASCADE")
cursor.execute("TRUNCATE question_multiple_choice CASCADE")
cursor.execute("TRUNCATE question_single_choice_answer CASCADE")
cursor.execute("TRUNCATE question_multiple_choice_answer CASCADE")
cursor.execute("TRUNCATE session CASCADE")
cursor.execute("TRUNCATE session_has_question CASCADE")
cursor.execute("TRUNCATE session_question_selection_temp CASCADE")
cursor.execute("TRUNCATE session_used_course CASCADE")
cursor.execute("TRUNCATE session_used_tag CASCADE")
cursor.execute("ALTER TABLE tag ADD IF NOT EXISTS legacy_id INTEGER")
cnxPgsql.commit()


# User
def process_user_data(user):
    user_id, username, email, first_name, last_name, email_confirmed, locked, role, university_id, created_at = user
    return user_id, username, email, first_name, last_name, generate_random_pw(), bool(email_confirmed), bool(locked), role, university_id, created_at


extract_table(
    'app_user',
    ("""
    SELECT
       ID as id,
       Username as username,
       CONCAT(Email, '@stud.uni-frankfurt.de') AS email,
       Vorname AS first_name,
       Nachname AS last_name,
       IF(aktiviert, TRUE, FALSE) AS email_confirmed,
       IF(gesperrt, TRUE, FALSE) AS locked,
       IF(Admin, 'ADMIN', IF(Moderator, 'MOD', 'USER')) AS role,
       1 AS university_id,
       Registrationsdatum as created_at
    FROM `DEFIKO_Userdaten`
    """),
    ['id', 'username', 'email', 'first_name', 'last_name', 'password_hash', 'email_confirmed', 'locked', 'role', 'university_id', 'created_at'],
    "INSERT INTO app_user (id, username, email, first_name, last_name, password_hash, email_confirmed, locked, role, university_id, created_at) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
    process_user_data
)


def process_login_attempt_data(login_attempt):
    user_id, timestamp, success = login_attempt
    return user_id, timestamp, bool(success)


extract_table(
    'login_attempt',
    ("""
    SELECT
        User_ID AS user_id,
        Timestamp AS timestamp,
        TRUE AS success
    FROM `DEFIKO_Statistics_Logins`
    """),
    ['user_id', 'timestamp', 'success'],
    "INSERT INTO login_attempt (user_id, timestamp, success) VALUES (%s, %s, %s)",
    process_login_attempt_data
)

extract_table(
    'app_user_has_major',
    ("""
    SELECT * FROM (
        (
            SELECT
                ID AS user_id,
                1 AS major_id
            FROM `DEFIKO_Userdaten` WHERE VKHuman = 1 OR KHuman = 1
        )
        UNION ALL
        (
            SELECT
                ID AS user_id,
                2 AS major_id
            FROM `DEFIKO_Userdaten` WHERE VKZahn = 1 OR KZahn = 1
        )
    ) x
    """),
    ['user_id', 'major_id'],
    "INSERT INTO app_user_has_major (user_id, major_id) VALUES (%s, %s)"
)

extract_table(
    'app_user_has_major_section',
    ("""
    SELECT * FROM (
        (
            SELECT
                ID AS user_id,
                1 AS section_id,
                1 AS major_id
            FROM `DEFIKO_Userdaten` WHERE VKHuman = 1
        )
        UNION ALL
        (
            SELECT
                ID AS user_id,
                2 AS section_id,
                1 AS major_id
            FROM `DEFIKO_Userdaten` WHERE KHuman = 1
        )
        UNION ALL
        (
            SELECT
                ID AS user_id,
                3 AS section_id,
                2 AS major_id
            FROM `DEFIKO_Userdaten` WHERE VKZahn = 1
        )
        UNION ALL
        (
            SELECT
                ID AS user_id,
                4 AS section_id,
                2 AS major_id
            FROM `DEFIKO_Userdaten` WHERE KZahn = 1
        )
    ) x
    """),
    ['user_id', 'section_id', 'major_id'],
    "INSERT INTO app_user_has_major_section (user_id, section_id, major_id) VALUES (%s, %s, %s)"
)


extract_table(
    'semester',
    ("""
    SELECT
    Semester_ID AS id,
    Semester AS name,
    IF(Semesterabschnitt = 'SS', CONVERT(REPLACE(Semester, Semesterabschnitt, ''), UNSIGNED INTEGER), CONVERT(SUBSTRING_INDEX(REPLACE(Semester, Semesterabschnitt, ''), '/', 1), UNSIGNED INTEGER) + 2000) AS start_year,
    IF(Semesterabschnitt = 'SS', CONVERT(REPLACE(Semester, Semesterabschnitt, ''), UNSIGNED INTEGER), CONVERT(SUBSTRING_INDEX(REPLACE(Semester, Semesterabschnitt, ''), '/', -1), UNSIGNED INTEGER) + 2000) AS end_year
    FROM `DEFIKO_Semester`
     """),
    ['id', 'name', 'start_year', 'end_year'],
    "INSERT INTO semester (id, name, start_year, end_year) VALUES (%s, %s, %s, %s)"
)


def process_module_data(module):
    module_id, university_id, name, is_university_wide = module
    return module_id, university_id, name, bool(is_university_wide)


extract_table(
    'module',
    ("""
    SELECT
       Fach_ID AS id,
       1 AS university_id,
       Fachname AS name,
       FALSE AS is_university_wide
    FROM `DEFIKO_Faecher`
     """),
    ['id', 'university_id', 'name', 'is_university_wide'],
    "INSERT INTO module (id, university_id, name, is_university_wide) VALUES (%s, %s, %s, %s)",
    process_module_data
)


extract_table(
    'major_section_has_module',
    ("""
    SELECT * FROM (
        (
            SELECT
                Fach_ID AS module_id,
                1 AS section_id
            FROM `DEFIKO_Faecher` WHERE Fach_Studienabschnitt = 1
        )
        UNION ALL
        (
            SELECT
                Fach_ID AS module_id,
                2 AS section_id
            FROM `DEFIKO_Faecher` WHERE Fach_Studienabschnitt = 2
        )
        UNION ALL
        (
            SELECT
                Fach_ID AS module_id,
                3 AS section_id
            FROM `DEFIKO_Faecher` WHERE Fach_Studienabschnitt = 3
        )
        UNION ALL
        (
            SELECT
                Fach_ID AS module_id,
                4 AS section_id
            FROM `DEFIKO_Faecher` WHERE Fach_Studienabschnitt = 5
        )
    ) x
     """),
    ['module_id', 'section_id'],
    "INSERT INTO major_section_has_module (module_id, section_id) VALUES (%s, %s)"
)


create_courses()


extract_table(
    'tag',
    ("""
    SELECT * FROM (
        (
            SELECT
                Tag_ID AS legacy_id,
                REPLACE(Tag, char(0), '') AS name,
                Fach_ID AS module_id
            FROM DEFIKO_Tags WHERE Fach_ID != 0
        )
        UNION
        (
            SELECT
                Tag_ID AS legacy_id,
                REPLACE(Tag, char(0), '') AS name,
                DEFIKO_Faecher.Fach_ID AS module_id
            FROM DEFIKO_Tags
                JOIN DEFIKO_Faecher ON DEFIKO_Faecher.Hauptfach_ID = DEFIKO_Tags.Hauptfach_ID
        )
    ) x
     """),
    ['legacy_id', 'name', 'module_id'],
    "INSERT INTO tag (legacy_id, name, module_id) VALUES (%s, %s, %s)"
)


def process_exam_data(exam):
    exam_id, date, name, is_retry, is_complete, module_id, semester_id = exam
    return exam_id, date, name, bool(is_retry), bool(is_complete), module_id, semester_id


extract_table(
    'exam',
    ("""
    SELECT
       Klausur_ID AS id,
       Klausurdatum AS date,
       REPLACE(Klausurname, char(0), '') AS name,
       FALSE AS is_retry,
       FALSE AS is_complete,
       Fach_ID AS module_id,
       Semester_ID AS semester_id
    FROM DEFIKO_Klausurkalender
     """),
    ['id', 'module_id', 'semester_id', 'date', 'name', 'is_retry', 'is_complete'],
    "INSERT INTO exam (id, course_id, date, name, is_retry, is_complete) SELECT %s AS id, course.id AS course_id, %s AS date, %s AS name, %s AS is_retry, %s AS is_complete FROM course WHERE module_id = %s AND semester_id = %s",
    process_exam_data
)


def process_file_data(file):
    file_id, media_type, filename, data = file
    return file_id, mimetypes.types_map['.{}'.format(media_type)], filename, data


extract_table(
    'file',
    ("""
    SELECT
       Medien_ID AS id,
       Mediumtype AS type,
       Medienname AS filename,
       Medium AS data
    FROM `DEFIKO_Medien`
     """),
    ['id', 'media_type', 'filename', 'data'],
    "INSERT INTO file (id, media_type, filename, data) VALUES (%s, %s, %s, %s)",
    process_file_data
)


def process_question_base_data(question_base):
    question_id, text, question_type, points, exam_id, creator_id, origin, is_approved, created_at, module_id, semester_id = question_base
    return question_id, text, question_type, points, exam_id, creator_id, origin, bool(is_approved), created_at, module_id, semester_id


extract_table(
    'question_base',
    ("""
    SELECT
           Frage_ID AS id,
           Fragentext AS text,
           IF((Loesung1 + Loesung2 + Loesung3 + Loesung4 + Loesung5) = 1, 'single-choice', 'multiple-choice') AS type,
           Fragepunkte AS points,
           IF(Temp_Klausur_ID = 0, NULL, Temp_Klausur_ID) AS exam_id,
           Eingabe_User_ID AS creator_id,
           IF(Gedaechtnissprotokoll = 1, 'GEPR', 'ORIG') AS origin,
           IF(Freigabe = 1, TRUE, FALSE) AS is_approved,
           Erstelldatum AS created_at,
           Fach_ID AS module_id,
           Semester_ID AS semester_id
    FROM `DEFIKO_Fragen`
     """),
    ['id', 'text', 'type', 'points', 'exam_id', 'creator_id', 'origin', 'is_approved', 'created_at', 'module_id', 'semester_id'],
    "INSERT INTO question_base(id, course_id, text, type, points, exam_id, creator_id, origin, is_approved, created_at) SELECT %s AS id, course.id AS course_id, %s AS text, %s AS type, %s AS points, %s AS exam_id, %s AS creator_id, %s AS origin, %s AS is_approved, %s AS created_at FROM course WHERE module_id = %s AND semester_id = %s",
    process_question_base_data
)


extract_table(
    'question_has_file',
    ("""
    SELECT
       Frage_ID AS question_id,
       Medien_ID AS file_id
    FROM `DEFIKO_Medien`
     """),
    ['question_id', 'file_id'],
    "INSERT INTO question_has_file(question_id, file_id) VALUES (%s, %s)",
)


extract_table(
    'question_has_tag',
    ("""
    SELECT
       `DEFIKO_Frage_Tags`.Frage_ID AS question_id,
       Tag_ID AS tag_id,
       Fach_ID AS module_id
    FROM `DEFIKO_Frage_Tags` JOIN `DEFIKO_Fragen` ON `DEFIKO_Fragen`.Frage_ID = `DEFIKO_Frage_Tags`.Frage_ID
     """),
    ['question_id', 'tag_id', 'module_id'],
    "INSERT INTO question_has_tag(question_id, tag_id) SELECT %s AS question_id, id FROM tag WHERE legacy_id = %s AND module_id = %s",
)


extract_table(
    'question_has_comment',
    ("""
    SELECT
       Kommentar_ID AS id,
       Kommentar AS comment,
       Frage_ID AS question_id,
       User_ID AS creator_id,
       Hinzufuegungs_Datum AS created_at
    FROM `DEFIKO_Kommentare`
     """),
    ['id', 'comment', 'question_id', 'creator_id', 'created_at'],
    "INSERT INTO question_has_comment(id, comment, question_id, creator_id, created_at) VALUES (%s, %s, %s, %s, %s)",
)


def process_question_error_data(data):
    error_id, question_id, creator_id, comment, created_at,  is_resolved, resolved_at, last_assigned_moderator_id = data
    return error_id, question_id, creator_id, comment, created_at,  bool(is_resolved), resolved_at, last_assigned_moderator_id


extract_table(
    'question_has_error',
    ("""
    SELECT
       Frage_Fehler_ID AS id,
       Frage_ID AS question_id,
       User_ID AS creator_id,
       Fehlertext AS comment,
       Datum_Erstellung AS created_at,
       TRUE AS is_resolved,
       Datum_Fertigstellung AS resolved_at,
       IF(letzter_Bearbeiter_id = 0, NULL, letzter_Bearbeiter_id) AS last_moderator
    FROM `DEFIKO_Frage_Fehler`
     """),
    ['id', 'question_id', 'creator_id', 'comment', 'created_at', 'is_resolved', 'resolved_at', 'last_assigned_moderator_id'],
    "INSERT INTO question_has_error(id, question_id, creator_id, comment, created_at,  is_resolved, resolved_at, last_assigned_moderator_id) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)",
    process_question_error_data
)


extract_table(
    'question_single_choice',
    ("""
    SELECT
       Frage_ID AS question_id,
       IF(Loesung1 = 1, 1, IF(Loesung2 = 1, 2, IF(Loesung3 = 1, 3, IF(Loesung4 = 1, 4, 5)))) AS correct_answer_local_id,
       Antwort1 AS a0,
       Antwort2 AS a1,
       Antwort3 AS a2,
       Antwort4 AS a3,
       Antwort5 AS a4
    FROM `DEFIKO_Fragen` WHERE (Loesung1 + Loesung2 + Loesung3 + Loesung4 + Loesung5) = 1
     """),
    ['question_id', 'correct_answer_local_id', 'a0', 'a1', 'a2', 'a3', 'a4'],
    """
        SELECT * FROM create_legacy_sc_question(%s, %s, %s, %s, %s, %s, %s)
    """,
)


def process_question_mc(data):
    question_id, l1, l2, l3, l4, l5 = data

    answers = []
    if l1:
        answers.append(1)
    if l2:
        answers.append(2)
    if l3:
        answers.append(3)
    if l4:
        answers.append(4)
    if l5:
        answers.append(5)

    return question_id, answers


extract_table(
    'question_multiple_choice',
    ("""
    SELECT
       Frage_ID AS question_id,
       Loesung1 AS l1,
       Loesung2 AS l2,
       Loesung3 AS l3,
       Loesung4 AS l4,
       Loesung5 AS l5
    FROM `DEFIKO_Fragen` WHERE (Loesung1 + Loesung2 + Loesung3 + Loesung4 + Loesung5) != 1
     """),
    ['question_id', 'correct_answer_local_ids'],
    """
        INSERT INTO question_multiple_choice(question_id, type, correct_answer_local_ids)
        VALUES (%s, 'multiple-choice', %s)
    """,
    process_question_mc
)


extract_table(
    'question_multiple_choice_answer',
    ("""
    SELECT * FROM (
        (
            SELECT
               Frage_ID AS question_id,
               1 AS local_id,
               Antwort1 AS text
            FROM `DEFIKO_Fragen` WHERE Antwort1 IS NOT NULL AND Antwort1 != '' AND (Loesung1 + Loesung2 + Loesung3 + Loesung4 + Loesung5) != 1
        ) UNION ALL (
            SELECT
               Frage_ID AS question_id,
               2 AS local_id,
               Antwort2 AS text
            FROM `DEFIKO_Fragen` WHERE Antwort2 IS NOT NULL AND Antwort2 != '' AND (Loesung1 + Loesung2 + Loesung3 + Loesung4 + Loesung5) != 1
        ) UNION ALL (
            SELECT
               Frage_ID AS question_id,
               3 AS local_id,
               Antwort3 AS text
            FROM `DEFIKO_Fragen` WHERE Antwort3 IS NOT NULL AND Antwort3 != '' AND (Loesung1 + Loesung2 + Loesung3 + Loesung4 + Loesung5) != 1
        ) UNION ALL (
            SELECT
               Frage_ID AS question_id,
               4 AS local_id,
               Antwort4 AS text
            FROM `DEFIKO_Fragen` WHERE Antwort4 IS NOT NULL AND Antwort4 != '' AND (Loesung1 + Loesung2 + Loesung3 + Loesung4 + Loesung5) != 1
        ) UNION ALL (
            SELECT
               Frage_ID AS question_id,
               5 AS local_id,
               Antwort5 AS text
            FROM `DEFIKO_Fragen` WHERE Antwort5 IS NOT NULL AND Antwort5 != '' AND (Loesung1 + Loesung2 + Loesung3 + Loesung4 + Loesung5) != 1
        )
    ) x
     """),
    ['question_id', 'local_id', 'text'],
    """
        INSERT INTO question_multiple_choice_answer(question_id, local_id, text)
        VALUES (%s, %s, %s)
    """
)


def process_session_data(data):
    session_id, creator_id, notes, type, name, is_random, is_finished, created_at = data
    return session_id, creator_id, notes, type, name, bool(is_random), bool(is_finished), created_at


extract_table(
    'session',
    ("""
    SELECT
       Session_ID AS id,
       User_ID AS creator_id,
       REPLACE(Session_Beschreibung, char(0), '') AS notes,
       IF(Pruefungssession = 1, 'exam', 'practice') AS type,
       REPLACE(Sessionname, char(0), '') AS name,
       IF(Zufaellige_Reihenfolge = 'on', true, false) AS is_random,
       IF(Nicht_Beantwortet = 'on', false, true) AS is_finished,
       Session_Datum AS created_at
    FROM `DEFIKO_Sessions`
     """),
    ['id', 'creator_id', 'notes', 'type', 'name', 'is_random', 'is_finished', 'created_at'],
    """
        INSERT INTO session(id, creator_id, notes, type, name, is_random, is_finished, created_at) 
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
    """,
    process_session_data
)


def process_session_question_data(data):
    session_id, question_id, is_submitted, time = data
    return session_id, question_id, bool(is_submitted), time


extract_table(
    'session_has_question',
    ("""
    SELECT
       Session_ID AS session_id,
       Frage_ID AS question_id,
       IF(Userantwort1 + Userantwort2 + Userantwort3 + Userantwort4 + Userantwort5 > 0, TRUE, FALSE) AS is_submitted,
       1 AS time
    FROM `DEFIKO_Session_Fragen`
     """),
    ['session_id', 'question_id', 'is_submitted', 'time'],
    """
        INSERT INTO session_has_question(session_id, question_id, is_submitted, time) 
        VALUES (%s, %s, %s, %s)
    """,
    process_session_question_data
)


extract_table(
    'session_used_course',
    ("""
    SELECT
       DEFIKO_Session_Faecher.Session_ID AS session_id,
       Semester_ID AS semester_id,
       Fach_ID AS module_id
    FROM `DEFIKO_Session_Faecher` JOIN `DEFIKO_Session_Semester` ON DEFIKO_Session_Faecher.Session_ID = DEFIKO_Session_Semester.Session_ID
     """),
    ['session_id', 'semester_id', 'module_id'],
    """
        INSERT INTO session_used_course(session_id, course_id) 
        SELECT %s as session_id, id AS course_id FROM course WHERE semester_id = %s AND module_id = %s
    """,
)


extract_table(
    'session_used_tag',
    ("""
    SELECT
       DEFIKO_Session_Tags.Session_ID AS session_id,
       DEFIKO_Session_Tags.Tag_ID AS tag_id,
       Fach_ID AS module_id
    FROM `DEFIKO_Session_Tags`
    JOIN `DEFIKO_Session_Faecher` ON DEFIKO_Session_Faecher.Session_ID = `DEFIKO_Session_Tags`.Session_ID
     """),
    ['session_id', 'tag_id', 'module_id'],
    """
        INSERT INTO session_used_tag(session_id, tag_id) SELECT %s AS session_id, tag.id AS tag_id FROM tag WHERE legacy_id = %s AND module_id = %s
    """,
)


cursor = cnxPgsql.cursor()
cursor.execute("ALTER TABLE tag DROP legacy_id")
cursor.execute("DROP FUNCTION create_legacy_sc_question")
cnxPgsql.commit()

cnxPgsql.close()
cnxMysql.close()
