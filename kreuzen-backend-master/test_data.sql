-- -----------------------------------------------------
-- Test Data
-- -----------------------------------------------------

INSERT INTO university (name, allowed_mail_domains) VALUES ('Uni1', '{"gmail.com"}');
INSERT INTO university (name, allowed_mail_domains) VALUES ('Uni2', '{"stud.tu-darmstadt.de"}');
INSERT INTO university (name, allowed_mail_domains) VALUES ('Uni3', '{"mittenbuehler.com"}');
INSERT INTO university (name, allowed_mail_domains) VALUES ('Uni4', '{"mittenbuehler.com"}');

INSERT INTO major (university_id, name) VALUES (1, 'Informatik');
INSERT INTO major (university_id, name) VALUES (1, 'Physik');
INSERT INTO major (university_id, name) VALUES (1, 'Medizin');
INSERT INTO major (university_id, name) VALUES (2, 'Physik');
INSERT INTO major (university_id, name) VALUES (2, 'Medizin');
INSERT INTO major (university_id, name) VALUES (3, 'Informatik');
INSERT INTO major (university_id, name) VALUES (3, 'Physik');
INSERT INTO major (university_id, name) VALUES (4, 'Physik');

INSERT INTO major_section (major_id, name) VALUES (1, 'A');
INSERT INTO major_section (major_id, name) VALUES (1, 'B');
INSERT INTO major_section (major_id, name) VALUES (1, 'C');
INSERT INTO major_section (major_id, name) VALUES (2, 'A');
INSERT INTO major_section (major_id, name) VALUES (2, 'B');
INSERT INTO major_section (major_id, name) VALUES (3, 'C');
INSERT INTO major_section (major_id, name) VALUES (3, 'A');
INSERT INTO major_section (major_id, name) VALUES (4, 'B');
INSERT INTO major_section (major_id, name) VALUES (4, 'C');
INSERT INTO major_section (major_id, name) VALUES (4, 'A');
INSERT INTO major_section (major_id, name) VALUES (5, 'B');
INSERT INTO major_section (major_id, name) VALUES (6, 'C');
INSERT INTO major_section (major_id, name) VALUES (7, 'A');
INSERT INTO major_section (major_id, name) VALUES (8, 'B');
INSERT INTO major_section (major_id, name) VALUES (8, 'C');

INSERT INTO semester (start_year, end_year, name) VALUES (2018, 2019, 'WiSe 18/19');
INSERT INTO semester (start_year, end_year, name) VALUES (2019, 2019, 'SoSe 19');
