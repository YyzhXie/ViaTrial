package com.viatrial.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.viatrial.common.BizException;
import com.viatrial.common.ErrorCode;
import com.viatrial.dto.request.PaperGenerateRequest;
import com.viatrial.dto.response.PaperGenerateResponse;
import com.viatrial.dto.response.PaperQuestionResponse;
import com.viatrial.dto.response.QuestionResponse;
import com.viatrial.entity.Question;
import com.viatrial.entity.QuestionType;
import com.viatrial.entity.Subject;
import com.viatrial.mapper.QuestionMapper;
import com.viatrial.mapper.QuestionTypeMapper;
import com.viatrial.mapper.SubjectMapper;
import com.viatrial.service.PaperService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PaperServiceImpl implements PaperService {

    private static final DateTimeFormatter PAPER_ID_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final SubjectMapper subjectMapper;

    private final QuestionTypeMapper questionTypeMapper;

    private final QuestionMapper questionMapper;

    private final QuestionServiceImpl questionService;

    public PaperServiceImpl(SubjectMapper subjectMapper,
                            QuestionTypeMapper questionTypeMapper,
                            QuestionMapper questionMapper,
                            QuestionServiceImpl questionService) {
        this.subjectMapper = subjectMapper;
        this.questionTypeMapper = questionTypeMapper;
        this.questionMapper = questionMapper;
        this.questionService = questionService;
    }

    @Override
    public PaperGenerateResponse generatePaper(PaperGenerateRequest request) {
        List<Question> selectedQuestions = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        int totalRequested = 0;

        Subject subject = subjectMapper.selectById(request.getSubjectId());
        if (subject == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "Subject does not exist");
        }

        for (Map.Entry<Long, Integer> entry : request.getTypeCountMap().entrySet()) {
            Long typeId = entry.getKey();
            Integer requestedCount = entry.getValue();
            if (typeId == null) {
                throw new BizException(ErrorCode.PARAM_ERROR, "Question type ID cannot be null");
            }
            if (requestedCount == null || requestedCount <= 0) {
                throw new BizException(ErrorCode.PARAM_ERROR, "Question count must be greater than 0");
            }

            QuestionType questionType = questionTypeMapper.selectById(typeId);
            if (questionType == null) {
                throw new BizException(ErrorCode.NOT_FOUND, "Question type does not exist");
            }
            if (!questionType.getSubjectId().equals(request.getSubjectId())) {
                throw new BizException(ErrorCode.PARAM_ERROR, "Question type does not belong to subject");
            }

            totalRequested += requestedCount;
            Long total = questionMapper.selectCount(new QueryWrapper<Question>()
                    .eq("subject_id", request.getSubjectId())
                    .eq("type_id", typeId));
            if (total == 0) {
                warnings.add("题型“" + questionType.getName() + "”暂无题目。");
            } else if (total < requestedCount) {
                selectedQuestions.addAll(questionMapper.selectList(
                        new QueryWrapper<Question>()
                                .eq("subject_id", request.getSubjectId())
                                .eq("type_id", typeId)
                                .orderByAsc("id")));
                warnings.add("题型“" + questionType.getName() + "”题量不足，要求 "
                        + requestedCount + " 题，实际 " + total + " 题。");
            } else {
                selectedQuestions.addAll(questionMapper.selectRandomBySubjectIdAndTypeId(
                        request.getSubjectId(), typeId, requestedCount));
            }
        }

        List<PaperQuestionResponse> questions = questionService.toQuestionResponses(selectedQuestions)
                .stream()
                .map(this::toPaperQuestionResponse)
                .toList();

        PaperGenerateResponse response = new PaperGenerateResponse();
        response.setPaperId(generatePaperId());
        response.setTotalRequested(totalRequested);
        response.setTotalActual(questions.size());
        response.setWarnings(warnings);
        response.setQuestions(questions);
        return response;
    }

    private PaperQuestionResponse toPaperQuestionResponse(QuestionResponse questionResponse) {
        PaperQuestionResponse response = new PaperQuestionResponse();
        response.setId(questionResponse.getId());
        response.setSubjectId(questionResponse.getSubjectId());
        response.setSubjectName(questionResponse.getSubjectName());
        response.setTypeId(questionResponse.getTypeId());
        response.setTypeName(questionResponse.getTypeName());
        response.setContent(questionResponse.getContent());
        response.setAnswer(questionResponse.getAnswer());
        response.setAnalysis(questionResponse.getAnalysis());
        response.setImageUrl(questionResponse.getImageUrl());
        response.setAnswerImageUrl(questionResponse.getAnswerImageUrl());
        response.setDifficulty(questionResponse.getDifficulty());
        response.setTags(questionResponse.getTags());
        return response;
    }

    private String generatePaperId() {
        return "PAPER-" + PAPER_ID_TIME_FORMATTER.format(LocalDateTime.now()) + "-"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }
}
