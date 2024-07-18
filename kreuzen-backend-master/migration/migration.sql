--
-- Setup für Uni Frankfurt
--

INSERT INTO university (name, allowed_mail_domains) VALUES ('Goethe-Universität Frankfurt', '{"stud.uni-frankfurt.de"}');

INSERT INTO major (university_id, name) VALUES (1, 'Humanmedizin');
INSERT INTO major (university_id, name) VALUES (1, 'Zahnmedizin');

INSERT INTO major_section (major_id, name) VALUES (1, 'Vorklinik');
INSERT INTO major_section (major_id, name) VALUES (1, 'Klinik');
INSERT INTO major_section (major_id, name) VALUES (2, 'Vorklinik');
INSERT INTO major_section (major_id, name) VALUES (2, 'Klinik');
