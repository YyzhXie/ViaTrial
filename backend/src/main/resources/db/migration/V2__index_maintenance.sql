-- 索引维护（审计项 D-6 / D-7）
--
-- 1) 删除与 UNIQUE 约束隐式索引最左前缀重复的显式单列索引：
--      question_type(subject_id)  由 UNIQUE(subject_id, name) 覆盖
--      question_tag(question_id)  由 UNIQUE(question_id, tag_id) 覆盖
--    保留 idx_question_tag_tag_id（tag_id 检索仍需要）。
-- 2) 补齐组卷/统计热路径所需的 (subject_id, type_id) 复合索引。

DROP INDEX IF EXISTS idx_question_type_subject_id;
DROP INDEX IF EXISTS idx_question_tag_question_id;

CREATE INDEX IF NOT EXISTS idx_question_subject_type
ON question(subject_id, type_id);

CREATE INDEX IF NOT EXISTS idx_question_tag_tag_id
ON question_tag(tag_id);
