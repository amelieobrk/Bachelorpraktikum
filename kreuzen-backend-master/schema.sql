-- -----------------------------------------------------
-- Kreuzen SQL Schema (PostgreSQL)
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Trigger trigger_set_timestamp
-- -----------------------------------------------------

CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
  IF NEW is distinct from OLD THEN
    NEW.updated_at := NOW();
  END IF;
  RETURN NEW;
END;
$$;


-- -----------------------------------------------------
-- Table hint
-- -----------------------------------------------------

CREATE TABLE hint(
    id SERIAL PRIMARY KEY,
    text text NOT NULL,
    is_active bool NOT NULL DEFAULT FALSE,
    created_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    updated_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc')
);

CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON hint
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- Table key_value
-- -----------------------------------------------------

CREATE TABLE key_value(
    key text PRIMARY KEY,
    value text,
    created_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    updated_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc')
);

CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON key_value
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- Table page_views_per_day
-- -----------------------------------------------------

CREATE TABLE page_views_per_day(
    date date,
    url text,
    views int DEFAULT 0,
    PRIMARY KEY (date, url)
);


-- -----------------------------------------------------
-- Table university
-- -----------------------------------------------------

CREATE TABLE university(
    id SERIAL PRIMARY KEY,
    name text NOT NULL,
    allowed_mail_domains text[],
    created_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    updated_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc')
);
CREATE UNIQUE INDEX university_name_unique_idx ON university (LOWER(name));

CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON university
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- Table role
-- -----------------------------------------------------

CREATE TABLE role(
    name text PRIMARY KEY,
    display_name text NOT NULL
);

CREATE UNIQUE INDEX role_name_unique_idx ON role (LOWER(name));


-- -----------------------------------------------------
-- Table app_user
-- -----------------------------------------------------

CREATE TABLE app_user(
    id SERIAL PRIMARY KEY,
    username text NOT NULL,
    email text NOT NULL,
    first_name text NOT NULL,
    last_name text NOT NULL,
    password_hash text NOT NULL,
    email_confirmed bool NOT NULL DEFAULT FALSE,
    locked bool NOT NULL DEFAULT FALSE,
    role TEXT NOT NULL DEFAULT 'USER' REFERENCES role(name) ON UPDATE CASCADE,
    university_id int NOT NULL REFERENCES university(id) ON DELETE CASCADE ON UPDATE CASCADE,
    created_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    updated_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc')
);
CREATE UNIQUE INDEX app_user_email_unique_idx on app_user (LOWER(email));
CREATE UNIQUE INDEX app_user_username_unique_idx on app_user (LOWER(username));

CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON app_user
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- Table login_attempt
-- -----------------------------------------------------

CREATE TABLE login_attempt(
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE ON UPDATE CASCADE,
    timestamp timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    success bool NOT NULL DEFAULT FALSE
);


-- -----------------------------------------------------
-- Table usage_time
-- -----------------------------------------------------

CREATE TABLE usage_time(
    date date,
    user_id INT REFERENCES app_user(id) ON DELETE CASCADE ON UPDATE CASCADE,
    minute_count INT NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, date)
);


-- -----------------------------------------------------
-- Table password_reset_token
-- -----------------------------------------------------

CREATE TABLE password_reset_token(
    user_id int PRIMARY KEY REFERENCES app_user(id) ON DELETE CASCADE ON UPDATE CASCADE,
    token_hash text NOT NULL,
    expires_at timestamp NOT NULL,
    created_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    updated_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc')
);

CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON password_reset_token
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- Table email_confirmation_token
-- -----------------------------------------------------

CREATE TABLE email_confirmation_token(
    user_id int PRIMARY KEY REFERENCES app_user(id) ON DELETE CASCADE ON UPDATE CASCADE,
    token_hash text NOT NULL,
    created_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    updated_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc')
);

CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON email_confirmation_token
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- Table major
-- -----------------------------------------------------

CREATE TABLE major(
    id SERIAL PRIMARY KEY,
    university_id int NOT NULL REFERENCES university(id) ON DELETE CASCADE ON UPDATE CASCADE,
    name text NOT NULL,
    created_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    updated_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc')
);

CREATE UNIQUE INDEX major_name_unique_idx on major (university_id, LOWER(name));

CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON major
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- Table major_section
-- -----------------------------------------------------

CREATE TABLE major_section(
    id SERIAL PRIMARY KEY,
    major_id int NOT NULL REFERENCES major(id) ON DELETE CASCADE ON UPDATE CASCADE,
    name text NOT NULL,
    created_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    updated_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    UNIQUE (major_id, id)
);

CREATE UNIQUE INDEX major_section_name_unique_idx on major_section (major_id, LOWER(name));

CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON major_section
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- Table app_user_has_major
-- -----------------------------------------------------

CREATE TABLE app_user_has_major(
    user_id integer REFERENCES app_user(id) ON DELETE CASCADE ON UPDATE CASCADE,
    major_id integer REFERENCES major(id) ON DELETE CASCADE ON UPDATE CASCADE,
    created_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    updated_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    PRIMARY KEY (user_id, major_id)
);

CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON app_user_has_major
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- Table app_user_has_major_section
-- -----------------------------------------------------

CREATE TABLE app_user_has_major_section(
    user_id integer REFERENCES app_user(id) ON DELETE CASCADE ON UPDATE CASCADE,
    major_id integer REFERENCES major(id) ON DELETE CASCADE ON UPDATE CASCADE,
    section_id int REFERENCES major_section(id) ON DELETE CASCADE ON UPDATE CASCADE,
    created_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    updated_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    PRIMARY KEY (user_id, major_id, section_id),
    FOREIGN KEY (user_id, major_id) REFERENCES app_user_has_major(user_id, major_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (major_id, section_id) REFERENCES major_section(major_id, id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON app_user_has_major_section
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- Table semester
-- -----------------------------------------------------

CREATE TABLE semester(
    id SERIAL PRIMARY KEY,
    start_year integer NOT NULL,
    end_year integer NOT NULL,
    name text NOT NULL,
    created_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    updated_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc')
);

CREATE UNIQUE INDEX semester_name_unique_idx ON semester (LOWER(name));

CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON semester
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- Table module
-- -----------------------------------------------------

CREATE TABLE module(
    id SERIAL PRIMARY KEY,
    university_id integer NOT NULL REFERENCES university(id) ON DELETE CASCADE ON UPDATE CASCADE,
    name text NOT NULL,
    is_university_wide boolean NOT NULL DEFAULT FALSE,
    created_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    updated_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc')
);

CREATE UNIQUE INDEX module_name_unique_idx ON module (university_id, LOWER(name));

CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON module
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- Table major_has_module
-- -----------------------------------------------------

CREATE TABLE major_has_module(
    module_id integer REFERENCES module(id) ON DELETE CASCADE ON UPDATE CASCADE,
    major_id integer REFERENCES major(id) ON DELETE CASCADE ON UPDATE CASCADE,
    created_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    updated_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    PRIMARY KEY (module_id, major_id)
);

CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON major_has_module
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- Table major_section_has_module
-- -----------------------------------------------------

CREATE TABLE major_section_has_module(
    module_id integer REFERENCES module(id) ON DELETE CASCADE ON UPDATE CASCADE,
    section_id integer REFERENCES major_section(id) ON DELETE CASCADE ON UPDATE CASCADE,
    created_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    updated_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    PRIMARY KEY (module_id, section_id)
);

CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON major_section_has_module
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- Table course
-- -----------------------------------------------------

CREATE TABLE course(
    id SERIAL PRIMARY KEY,
    module_id integer NOT NULL REFERENCES module(id) ON DELETE CASCADE ON UPDATE CASCADE,
    semester_id integer NOT NULL REFERENCES semester(id) ON DELETE CASCADE ON UPDATE CASCADE,
    created_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    updated_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc')
);

CREATE UNIQUE INDEX course_unique_idx ON course (module_id, semester_id);

CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON course
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();

CREATE VIEW course_view AS
    SELECT c.*, CONCAT(dm.name, ' (', ds.name, ')') AS name FROM course c JOIN module dm on c.module_id = dm.id JOIN semester ds on c.semester_id = ds.id;

CREATE RULE course_view_insert AS ON INSERT TO course_view
    DO INSTEAD INSERT INTO course (module_id, semester_id) VALUES (NEW.module_id, NEW.semester_id) RETURNING *, (
        SELECT CONCAT(m.name, ' (', s.name, ')') FROM module m, semester s WHERE m.id = module_id AND s.id = semester_id
    ) AS name;

CREATE RULE course_view_delete AS ON DELETE TO course_view
    DO INSTEAD DELETE FROM course WHERE module_id = OLD.module_id AND semester_id = OLD.semester_id;

CREATE RULE course_view_update AS ON UPDATE TO course_view
    DO INSTEAD UPDATE course SET module_id = NEW.module_id, semester_id = NEW.semester_id WHERE id = OLD.id RETURNING *, (
        SELECT CONCAT(m.name, ' (', s.name, ')') FROM module m, semester s WHERE m.id = module_id AND s.id = semester_id
    ) AS name;

DROP RULE course_view_insert ON course_view;


-- -----------------------------------------------------
-- Table tag
-- -----------------------------------------------------

CREATE TABLE tag(
    id SERIAL PRIMARY KEY,
    name text NOT NULL,
    module_id integer NOT NULL REFERENCES module(id) ON DELETE CASCADE ON UPDATE CASCADE,
    created_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    updated_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc')
);

CREATE UNIQUE INDEX tag_name_unique_idx ON tag (module_id, LOWER(name));

CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON tag
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- Table exam
-- -----------------------------------------------------

CREATE TABLE exam(
    id SERIAL PRIMARY KEY,
    course_id integer NOT NULL REFERENCES course(id) ON DELETE CASCADE ON UPDATE CASCADE,
    date date NOT NULL,
    name text,
    is_retry boolean NOT NULL DEFAULT FALSE,
    is_complete boolean NOT NULL DEFAULT FALSE,
    created_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    updated_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc')
);

CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON exam
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- Table file
-- -----------------------------------------------------

CREATE TABLE file(
    id SERIAL PRIMARY KEY,
    media_type text NOT NULL,
    filename text NOT NULL,
    data bytea NOT NULL,
    description text,
    created_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT(NOW() AT TIME ZONE 'utc'),
    updated_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT(NOW() AT TIME ZONE 'utc')
);
CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON file
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();

-- -----------------------------------------------------
-- Table question_origins
-- -----------------------------------------------------

CREATE TABLE question_origins(
    name text PRIMARY KEY,
    display_name text NOT NULL
);


-- -----------------------------------------------------
-- Table question_base
-- -----------------------------------------------------

CREATE TYPE question_type AS ENUM ('single-choice', 'multiple-choice', 'assignment');
CREATE CAST (varchar AS question_type) WITH INOUT AS IMPLICIT;
CREATE CAST (varchar[] AS question_type[]) WITH INOUT AS IMPLICIT;

CREATE TABLE question_base(
    id SERIAL PRIMARY KEY,
    text text NOT NULL,
    type question_type NOT NULL,
    additional_information text,
    points int NOT NULL DEFAULT 1,
    exam_id integer REFERENCES exam(id) ON DELETE CASCADE ON UPDATE CASCADE,
    course_id integer NOT NULL REFERENCES course(id) ON DELETE CASCADE ON UPDATE CASCADE,
    creator_id integer NOT NULL REFERENCES app_user(id) ON DELETE CASCADE ON UPDATE CASCADE,
    updater_id integer REFERENCES app_user(id) ON DELETE CASCADE ON UPDATE CASCADE,
    origin text NOT NULL REFERENCES question_origins(name) ON UPDATE CASCADE,
    is_approved boolean NOT NULL DEFAULT FALSE,
    created_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT(NOW() AT TIME ZONE 'utc'),
    updated_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT(NOW() AT TIME ZONE 'utc'),
    UNIQUE (id, type)
);
CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON question_base
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();

-- -----------------------------------------------------
-- Table question_has_file
-- -----------------------------------------------------

CREATE TABLE question_has_file(
    question_id int REFERENCES question_base(id) ON DELETE CASCADE ON UPDATE CASCADE,
    file_id int REFERENCES file(id) ON DELETE CASCADE ON UPDATE CASCADE,
    PRIMARY KEY (question_id, file_id)
);


-- -----------------------------------------------------
-- Table question_single_choice
-- -----------------------------------------------------

CREATE TABLE question_single_choice(
    id SERIAL,
    question_id int PRIMARY KEY REFERENCES question_base(id) ON DELETE CASCADE ON UPDATE CASCADE,
    type question_type NOT NULL CHECK ( type = 'single-choice' ) DEFAULT 'single-choice',
    correct_answer_local_id integer NOT NULL,
    FOREIGN KEY (question_id, type) REFERENCES question_base(id, type) ON DELETE CASCADE ON UPDATE CASCADE
);


-- -----------------------------------------------------
-- Table question_multiple_choice
-- -----------------------------------------------------

CREATE TABLE question_multiple_choice(
    id SERIAL,
    question_id int PRIMARY KEY REFERENCES question_base(id) ON DELETE CASCADE ON UPDATE CASCADE,
    type question_type NOT NULL CHECK ( type = 'multiple-choice' ) DEFAULT 'multiple-choice',
    correct_answer_local_ids int[],
    FOREIGN KEY (question_id, type) REFERENCES question_base(id, type) ON DELETE CASCADE ON UPDATE CASCADE
);


-- -----------------------------------------------------
-- Table question_single_choice_answer
-- -----------------------------------------------------

CREATE TABLE question_single_choice_answer(
    id SERIAL PRIMARY KEY,
    question_id int NOT NULL REFERENCES question_single_choice(question_id) ON DELETE CASCADE ON UPDATE CASCADE,
    local_id int NOT NULL,
    text text NOT NULL,
    UNIQUE (question_id, local_id)
);

-- -----------------------------------------------------
-- Table question_multiple_choice_answer
-- -----------------------------------------------------

CREATE TABLE question_multiple_choice_answer(
    id SERIAL PRIMARY KEY,
    question_id int NOT NULL REFERENCES question_multiple_choice(question_id) ON DELETE CASCADE ON UPDATE CASCADE,
    local_id int NOT NULL,
    text text NOT NULL,
    UNIQUE (question_id, local_id)
);


-- -----------------------------------------------------
-- Table question_assignment
-- -----------------------------------------------------

CREATE TABLE question_assignment(
    id SERIAL,
    question_id int PRIMARY KEY REFERENCES question_base(id) ON DELETE CASCADE ON UPDATE CASCADE,
    type question_type NOT NULL CHECK ( type = 'assignment' ) DEFAULT 'assignment',
    FOREIGN KEY (question_id, type) REFERENCES question_base(id, type) ON DELETE CASCADE ON UPDATE CASCADE
);


-- -----------------------------------------------------
-- Table question_assignment_answer
-- -----------------------------------------------------

CREATE TABLE question_assignment_answer(
    id SERIAL PRIMARY KEY,
    question_id integer REFERENCES question_assignment(question_id) ON DELETE CASCADE ON UPDATE CASCADE,
    local_id integer NOT NULL,
    answer text NOT NULL,
    UNIQUE (question_id, local_id)
);


-- -----------------------------------------------------
-- Table question_assignment_identifier
-- -----------------------------------------------------

CREATE TABLE question_assignment_identifier(
    id SERIAL PRIMARY KEY,
    question_id integer REFERENCES question_assignment(question_id) ON DELETE CASCADE ON UPDATE CASCADE,
    local_id integer NOT NULL,
    identifier text NOT NULL,
    correct_answer_local_id int REFERENCES question_assignment_answer(id) NOT NULL,
    UNIQUE (question_id, local_id)
);


-- -----------------------------------------------------
-- Table question_has_tag
-- -----------------------------------------------------

CREATE TABLE question_has_tag(
    question_id integer REFERENCES question_base(id) ON DELETE CASCADE ON UPDATE CASCADE,
    tag_id integer REFERENCES tag(id) ON DELETE CASCADE ON UPDATE CASCADE,
    PRIMARY KEY (question_id, tag_id)
);


-- -----------------------------------------------------
-- Table question_has_comment
-- -----------------------------------------------------

CREATE TABLE question_has_comment(
    id SERIAL PRIMARY KEY,
    question_id integer NOT NULL REFERENCES question_base(id) ON DELETE CASCADE ON UPDATE CASCADE,
    creator_id integer NOT NULL REFERENCES app_user(id) ON DELETE CASCADE ON UPDATE CASCADE,
    comment text NOT NULL,
    created_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT(NOW() AT TIME ZONE 'utc'),
    updated_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT(NOW() AT TIME ZONE 'utc')
);

CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON question_has_comment
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- Table question_has_error
-- -----------------------------------------------------

CREATE TABLE question_has_error(
    id SERIAL PRIMARY KEY,
    question_id integer NOT NULL REFERENCES question_base(id) ON DELETE CASCADE ON UPDATE CASCADE,
    creator_id integer NOT NULL REFERENCES app_user(id) ON DELETE CASCADE ON UPDATE CASCADE,
    comment text NOT NULL,
    source text,
    is_resolved boolean NOT NULL DEFAULT FALSE,
    last_assigned_moderator_id int REFERENCES app_user(id) ON DELETE CASCADE ON UPDATE CASCADE,
    resolved_at timestamp WITHOUT TIME ZONE,
    created_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT(NOW() AT TIME ZONE 'utc'),
    updated_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT(NOW() AT TIME ZONE 'utc')
);

CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON question_has_error
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- Table session
-- -----------------------------------------------------

CREATE TYPE session_type AS ENUM ('practice', 'exam');
CREATE CAST (varchar AS session_type) WITH INOUT AS IMPLICIT;
CREATE CAST (varchar[] AS session_type[]) WITH INOUT AS IMPLICIT;

CREATE TABLE session(
    id SERIAL PRIMARY KEY,
    creator_id integer NOT NULL REFERENCES app_user(id) ON DELETE CASCADE ON UPDATE CASCADE,
    notes text,
    type session_type NOT NULL DEFAULT 'practice',
    name text NOT NULL,
    is_random bool NOT NULL DEFAULT FALSE,
    is_finished bool NOT NULL DEFAULT FALSE,
    created_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT(NOW() AT TIME ZONE 'utc'),
    updated_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT(NOW() AT TIME ZONE 'utc')
);

CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON session
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();

-- -----------------------------------------------------
-- Table session_has_question
-- -----------------------------------------------------

CREATE TABLE session_has_question(
    id SERIAL UNIQUE,
    session_id int REFERENCES session(id) ON DELETE CASCADE ON UPDATE CASCADE,
    question_id int REFERENCES question_base(id) ON DELETE CASCADE ON UPDATE CASCADE,
    local_id int NOT NULL,
    time int NOT NULL DEFAULT 0,
    is_submitted bool NOT NULL DEFAULT FALSE,
    PRIMARY KEY (session_id, question_id),
    UNIQUE (local_id, session_id)
);


-- -----------------------------------------------------
-- Table session_used_course
-- -----------------------------------------------------

CREATE TABLE session_used_course(
    session_id int REFERENCES session(id) ON DELETE CASCADE ON UPDATE CASCADE,
    course_id int REFERENCES course(id) ON DELETE CASCADE ON UPDATE CASCADE,
    PRIMARY KEY (session_id, course_id)
);


-- -----------------------------------------------------
-- Table session_used_tag
-- -----------------------------------------------------

CREATE TABLE session_used_tag(
    session_id int REFERENCES session(id) ON DELETE CASCADE ON UPDATE CASCADE,
    tag_id int REFERENCES tag(id) ON DELETE CASCADE ON UPDATE CASCADE,
    PRIMARY KEY (session_id, tag_id)
);


-- -----------------------------------------------------
-- Table session_single_choice_selection
-- -----------------------------------------------------

CREATE TABLE session_single_choice_selection(
    id SERIAL UNIQUE,
    session_id int REFERENCES session(id) ON DELETE CASCADE ON UPDATE CASCADE,
    answer_id int REFERENCES question_single_choice_answer(id) ON DELETE CASCADE ON UPDATE CASCADE,
    is_checked bool NOT NULL DEFAULT FALSE,
    is_crossed bool NOT NULL DEFAULT FALSE,
    created_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT(NOW() AT TIME ZONE 'utc'),
    updated_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT(NOW() AT TIME ZONE 'utc'),
    PRIMARY KEY (session_id, answer_id)
);

CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON session_single_choice_selection
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();

-- -----------------------------------------------------
-- Table session_multiple_choice_selection
-- -----------------------------------------------------

CREATE TABLE session_multiple_choice_selection(
    id SERIAL UNIQUE,
    session_id int REFERENCES session(id) ON DELETE CASCADE ON UPDATE CASCADE,
    answer_id int REFERENCES question_multiple_choice_answer(id) ON DELETE CASCADE ON UPDATE CASCADE,
    is_checked bool NOT NULL DEFAULT FALSE,
    is_crossed bool NOT NULL DEFAULT FALSE,
    created_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT(NOW() AT TIME ZONE 'utc'),
    updated_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT(NOW() AT TIME ZONE 'utc'),
    PRIMARY KEY (session_id, answer_id)
);

CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON session_multiple_choice_selection
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- Table session_assignment_selection
-- -----------------------------------------------------

CREATE TABLE session_assignment_selection(
    id SERIAL UNIQUE,
    session_id int REFERENCES session(id) ON DELETE CASCADE ON UPDATE CASCADE,
    answer_id int REFERENCES question_assignment_answer(id) ON DELETE CASCADE ON UPDATE CASCADE,
    identifier_id int REFERENCES question_assignment_identifier(id) ON DELETE CASCADE ON UPDATE CASCADE,
    created_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT(NOW() AT TIME ZONE 'utc'),
    updated_at timestamp WITHOUT TIME ZONE NOT NULL DEFAULT(NOW() AT TIME ZONE 'utc'),
    PRIMARY KEY (session_id, answer_id, identifier_id)
);

CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON session_assignment_selection
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();


-- -----------------------------------------------------
-- View Search
-- -----------------------------------------------------

CREATE VIEW single_choice_question_search AS
    SELECT a.question_id AS id, string_agg(a.text, ' ') AS answers FROM question_single_choice_answer a
        GROUP BY a.question_id;

CREATE VIEW multiple_choice_question_search AS
    SELECT a.question_id AS id, string_agg(a.text, ' ') AS answers FROM question_multiple_choice_answer a
        GROUP BY a.question_id;

CREATE VIEW question_search AS
    SELECT
       q.id AS id,
       setweight(to_tsvector('german', q.text), 'A') ||
       setweight(to_tsvector('german', coalesce(q.additional_information, '')), 'B') ||
       setweight(to_tsvector('german', coalesce(sct.answers, mct.answers, '')), 'B') ||
       setweight(to_tsvector('german', c.name), 'B') ||
       setweight(to_tsvector('german', coalesce(string_agg(t.name, ' '), '')), 'C') as document
    FROM question_base q
        JOIN course_view c ON q.course_id = c.id
        LEFT JOIN question_has_tag qht ON q.id = qht.question_id
        LEFT JOIN tag t on qht.tag_id = t.id
        LEFT JOIN single_choice_question_search sct ON sct.id = q.id
        LEFT JOIN multiple_choice_question_search mct ON mct.id = q.id
        GROUP BY q.id, c.name, sct.answers, mct.answers;


-- -----------------------------------------------------
-- Init Data
-- -----------------------------------------------------

INSERT INTO question_origins (name, display_name) VALUES ('ORIG', 'Originalfrage');
INSERT INTO question_origins (name, display_name) VALUES ('GEPR', 'Gedächtnisprotokoll-Frage');
INSERT INTO question_origins (name, display_name) VALUES ('NIKL', 'Nicht-Klausur-Frage');
INSERT INTO question_origins (name, display_name) VALUES ('IMPP', 'IMPP-Frage');

INSERT INTO role (name, display_name) VALUES ('USER', 'User');
INSERT INTO role (name, display_name) VALUES ('MOD', 'Moderator');
INSERT INTO role (name, display_name) VALUES ('ADMIN', 'Administrator');
INSERT INTO role (name, display_name) VALUES ('SUDO', 'Super-Admin');
