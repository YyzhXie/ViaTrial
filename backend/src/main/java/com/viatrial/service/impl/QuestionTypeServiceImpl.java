package com.viatrial.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.viatrial.common.BizException;
import com.viatrial.common.ErrorCode;
import com.viatrial.dto.request.QuestionTypeAddRequest;
import com.viatrial.dto.response.QuestionTypeResponse;
import com.viatrial.entity.Question;
import com.viatrial.entity.QuestionType;
import com.viatrial.mapper.QuestionMapper;
import com.viatrial.mapper.QuestionTypeMapper;
import com.viatrial.mapper.SubjectMapper;
import com.viatrial.service.QuestionTypeService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionTypeServiceImpl implements QuestionTypeService {

    private final QuestionTypeMapper questionTypeMapper;

    private final SubjectMapper subjectMapper;

    private final QuestionMapper questionMapper;

    public QuestionTypeServiceImpl(QuestionTypeMapper questionTypeMapper,
                                   SubjectMapper subjectMapper,
                                   QuestionMapper questionMapper) {
        this.questionTypeMapper = questionTypeMapper;
        this.subjectMapper = subjectMapper;
        this.questionMapper = questionMapper;
    }

    @Override
    @Transactional
    public Long addQuestionType(QuestionTypeAddRequest request) {
        if (subjectMapper.selectById(request.getSubjectId()) == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "科目不存在");
        }

        String name = request.getName().trim();
        Long count = questionTypeMapper.selectCount(new QueryWrapper<QuestionType>()
                .eq("subject_id", request.getSubjectId())
                .eq("name", name));
        if (count > 0) {
            throw new BizException(ErrorCode.CONFLICT, "同一科目下题型名称不能重复");
        }

        QuestionType questionType = new QuestionType();
        questionType.setSubjectId(request.getSubjectId());
        questionType.setName(name);

        try {
            questionTypeMapper.insert(questionType);
        } catch (DataIntegrityViolationException ex) {
            throw new BizException(ErrorCode.CONFLICT, "同一科目下题型名称不能重复");
        }
        return questionType.getId();
    }

    @Override
    public List<QuestionTypeResponse> listQuestionTypes(Long subjectId) {
        if (subjectMapper.selectById(subjectId) == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "科目不存在");
        }

        return questionTypeMapper.selectList(new QueryWrapper<QuestionType>()
                        .eq("subject_id", subjectId)
                        .orderByAsc("id"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public Boolean deleteQuestionType(Long id) {
        QuestionType questionType = questionTypeMapper.selectById(id);
        if (questionType == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "题型不存在");
        }

        Long questionCount = questionMapper.selectCount(new QueryWrapper<Question>().eq("type_id", id));
        if (questionCount > 0) {
            throw new BizException(ErrorCode.CONFLICT, "该题型下存在题目，不能删除");
        }

        return questionTypeMapper.deleteById(id) > 0;
    }

    private QuestionTypeResponse toResponse(QuestionType questionType) {
        return new QuestionTypeResponse(questionType.getId(), questionType.getSubjectId(), questionType.getName());
    }
}
