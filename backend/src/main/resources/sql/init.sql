CREATE TABLE subject (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '科目ID',
    name VARCHAR(50) NOT NULL COMMENT '科目名称',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_subject_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科目表';

CREATE TABLE question_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '题型ID',
    subject_id BIGINT NOT NULL COMMENT '科目ID',
    name VARCHAR(50) NOT NULL COMMENT '题型名称',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_subject_type_name (subject_id, name),
    KEY idx_question_type_subject_id (subject_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题型表';

CREATE TABLE tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '标签ID',
    name VARCHAR(50) NOT NULL COMMENT '标签名称',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_tag_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

CREATE TABLE question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '题目ID',
    subject_id BIGINT NOT NULL COMMENT '科目ID',
    type_id BIGINT NOT NULL COMMENT '题型ID',
    content TEXT NOT NULL COMMENT '题目正文，支持LaTeX字符串',
    answer TEXT NULL COMMENT '参考答案，支持LaTeX字符串',
    analysis TEXT NULL COMMENT '解析，可为空，支持LaTeX字符串',
    image_url VARCHAR(500) NULL COMMENT '题目图片URL，一期只保存字符串',
    answer_image_url VARCHAR(500) NULL COMMENT '答案图片URL，一期只保存字符串',
    difficulty TINYINT NOT NULL DEFAULT 1 COMMENT '难度：1简单，2中等，3困难',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_question_subject_id (subject_id),
    KEY idx_question_type_id (type_id),
    KEY idx_question_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';

CREATE TABLE question_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_question_tag (question_id, tag_id),
    KEY idx_question_tag_question_id (question_id),
    KEY idx_question_tag_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目标签关联表';
