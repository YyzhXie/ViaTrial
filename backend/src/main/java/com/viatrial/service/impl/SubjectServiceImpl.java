package com.viatrial.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.viatrial.common.BizException;
import com.viatrial.common.ErrorCode;
import com.viatrial.dto.request.SubjectAddRequest;
import com.viatrial.dto.response.SubjectResponse;
import com.viatrial.entity.Question;
import com.viatrial.entity.QuestionTag;
import com.viatrial.entity.QuestionType;
import com.viatrial.entity.Subject;
import com.viatrial.mapper.QuestionMapper;
import com.viatrial.mapper.QuestionTagMapper;
import com.viatrial.mapper.QuestionTypeMapper;
import com.viatrial.mapper.SubjectMapper;
import com.viatrial.service.SubjectService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubjectServiceImpl implements SubjectService {

    private static final List<String> DEFAULT_QUESTION_TYPES = List.of("选择题", "判断题", "填空题");

    private final SubjectMapper subjectMapper;

    private final QuestionTypeMapper questionTypeMapper;

    private final QuestionMapper questionMapper;

    private final QuestionTagMapper questionTagMapper;

    public SubjectServiceImpl(SubjectMapper subjectMapper,
                              QuestionTypeMapper questionTypeMapper,
                              QuestionMapper questionMapper,
                              QuestionTagMapper questionTagMapper) {
        this.subjectMapper = subjectMapper;
        this.questionTypeMapper = questionTypeMapper;
        this.questionMapper = questionMapper;
        this.questionTagMapper = questionTagMapper;
    }

    @Override
    @Transactional
    public Long addSubject(SubjectAddRequest request) {
        String name = request.getName().trim();
        Long count = subjectMapper.selectCount(new QueryWrapper<Subject>().eq("name", name));
        if (count > 0) {
            throw new BizException(ErrorCode.CONFLICT, "科目名称已存在");
        }

        Subject subject = new Subject();
        subject.setName(name);

        try {
            subjectMapper.insert(subject);
        } catch (DataIntegrityViolationException ex) {
            throw new BizException(ErrorCode.CONFLICT, "科目名称已存在");
        }
        createDefaultQuestionTypes(subject.getId());
        return subject.getId();
    }

    @Override
    public List<SubjectResponse> listSubjects() {
        return subjectMapper.selectList(new QueryWrapper<Subject>().orderByAsc("id"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public Boolean deleteSubject(Long id) {
        Subject subject = subjectMapper.selectById(id);
        if (subject == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }

        List<Long> questionIds = questionMapper.selectList(
                        new QueryWrapper<Question>().select("id").eq("subject_id", id))
                .stream()
                .map(Question::getId)
                .toList();

        if (!questionIds.isEmpty()) {
            questionTagMapper.delete(new QueryWrapper<QuestionTag>().in("question_id", questionIds));
            questionMapper.deleteBatchIds(questionIds);
        }

        questionTypeMapper.delete(new QueryWrapper<QuestionType>().eq("subject_id", id));
        return subjectMapper.deleteById(id) > 0;
    }

    private void createDefaultQuestionTypes(Long subjectId) {
        for (String name : DEFAULT_QUESTION_TYPES) {
            QuestionType questionType = new QuestionType();
            questionType.setSubjectId(subjectId);
            questionType.setName(name);
            questionTypeMapper.insert(questionType);
        }
    }

    private SubjectResponse toResponse(Subject subject) {
        return new SubjectResponse(subject.getId(), subject.getName());
    }
}
