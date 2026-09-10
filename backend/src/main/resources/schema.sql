-- ViaTrial schema 唯一真相源。
-- 初始化入口：com.viatrial.database.DatabaseInitializer（由 Spring ScriptUtils 解析执行）。
-- 增量结构变更请新增 db/migration/V<n>__*.sql，不要修改本文件中已被历史库应用过的语句语义。

CREATE TABLE IF NOT EXISTS subject (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS question_type (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    subject_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (subject_id, name),
    FOREIGN KEY (subject_id) REFERENCES subject(id) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS tag (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS question (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    subject_id INTEGER NOT NULL,
    type_id INTEGER NOT NULL,
    content TEXT NOT NULL,
    answer TEXT,
    analysis TEXT,
    image_url TEXT,
    answer_image_url TEXT,
    difficulty INTEGER NOT NULL DEFAULT 1,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (difficulty IN (1, 2, 3)),
    FOREIGN KEY (subject_id) REFERENCES subject(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (type_id) REFERENCES question_type(id) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_question_subject_id
ON question(subject_id);

CREATE INDEX IF NOT EXISTS idx_question_type_id
ON question(type_id);

-- 组卷/统计按 (subject_id, type_id) 同时过滤，复合索引避免单列索引 + 值过滤退化（审计项 D-7）。
CREATE INDEX IF NOT EXISTS idx_question_subject_type
ON question(subject_id, type_id);

CREATE INDEX IF NOT EXISTS idx_question_created_time
ON question(created_time);

CREATE TABLE IF NOT EXISTS question_tag (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    question_id INTEGER NOT NULL,
    tag_id INTEGER NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (question_id, tag_id),
    FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_question_tag_tag_id
ON question_tag(tag_id);
