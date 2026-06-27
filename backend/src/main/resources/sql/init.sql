CREATE TABLE IF NOT EXISTS subject (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(50) NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_subject_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS question_type (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    subject_id INTEGER NOT NULL,
    name VARCHAR(50) NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_subject_type_name UNIQUE (subject_id, name),
    CONSTRAINT fk_question_type_subject
        FOREIGN KEY (subject_id)
        REFERENCES subject(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_question_type_subject_id
    ON question_type (subject_id);

CREATE TABLE IF NOT EXISTS tag (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(50) NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_tag_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS question (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    subject_id INTEGER NOT NULL,
    type_id INTEGER NOT NULL,
    content TEXT NOT NULL,
    answer TEXT NULL,
    analysis TEXT NULL,
    image_url VARCHAR(500) NULL,
    answer_image_url VARCHAR(500) NULL,
    difficulty INTEGER NOT NULL DEFAULT 1,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_question_subject
        FOREIGN KEY (subject_id)
        REFERENCES subject(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_question_type
        FOREIGN KEY (type_id)
        REFERENCES question_type(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT chk_question_difficulty
        CHECK (difficulty IN (1, 2, 3))
);

CREATE INDEX IF NOT EXISTS idx_question_subject_id
    ON question (subject_id);

CREATE INDEX IF NOT EXISTS idx_question_type_id
    ON question (type_id);

CREATE INDEX IF NOT EXISTS idx_question_created_time
    ON question (created_time);

CREATE INDEX IF NOT EXISTS idx_question_difficulty
    ON question (difficulty);

CREATE TABLE IF NOT EXISTS question_tag (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    question_id INTEGER NOT NULL,
    tag_id INTEGER NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_question_tag UNIQUE (question_id, tag_id),
    CONSTRAINT fk_question_tag_question
        FOREIGN KEY (question_id)
        REFERENCES question(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_question_tag_tag
        FOREIGN KEY (tag_id)
        REFERENCES tag(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_question_tag_question_id
    ON question_tag (question_id);

CREATE INDEX IF NOT EXISTS idx_question_tag_tag_id
    ON question_tag (tag_id);
