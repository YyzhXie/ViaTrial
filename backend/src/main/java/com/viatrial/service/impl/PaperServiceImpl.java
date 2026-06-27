package com.viatrial.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.viatrial.common.BizException;
import com.viatrial.common.ErrorCode;
import com.viatrial.dto.request.PaperGenerateRequest;
import com.viatrial.dto.response.PaperGenerateResponse;
import com.viatrial.dto.response.PaperQuestionResponse;
import com.viatrial.dto.response.QuestionResponse;
import com.viatrial.entity.Question;
import com.viatrial.entity.Subject;
import com.viatrial.mapper.QuestionMapper;
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

    private final QuestionMapper questionMapper;

    private final QuestionServiceImpl questionService;

    public PaperServiceImpl(SubjectMapper subjectMapper,
                            QuestionMapper questionMapper,
                            QuestionServiceImpl questionService) {
        this.subjectMapper = subjectMapper;
        this.questionMapper = questionMapper;
        this.questionService = questionService;
    }

    @Override
    public PaperGenerateResponse generatePaper(PaperGenerateRequest request) {
        List<Question> selectedQuestions = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        int totalRequested = 0;

        for (Map.Entry<Long, Integer> entry : request.getSubjectCountMap().entrySet()) {
            Long subjectId = entry.getKey();
            Integer requestedCount = entry.getValue();
            if (subjectId == null) {
                throw new BizException(ErrorCode.PARAM_ERROR, "Subject ID cannot be null");
            }
            if (requestedCount == null || requestedCount <= 0) {
                throw new BizException(ErrorCode.PARAM_ERROR, "Question count must be greater than 0");
            }

            Subject subject = subjectMapper.selectById(subjectId);
            if (subject == null) {
                throw new BizException(ErrorCode.NOT_FOUND, "Subject does not exist");
            }

            totalRequested += requestedCount;
            Long total = questionMapper.selectCount(new QueryWrapper<Question>().eq("subject_id", subjectId));
            if (total == 0) {
                warnings.add("Subject \"" + subject.getName() + "\" has no questions.");
            } else if (total < requestedCount) {
                selectedQuestions.addAll(questionMapper.selectList(
                        new QueryWrapper<Question>().eq("subject_id", subjectId).orderByAsc("id")));
                warnings.add("Subject \"" + subject.getName() + "\" has insufficient questions. Requested "
                        + requestedCount + ", actual " + total + ".");
            } else {
                selectedQuestions.addAll(questionMapper.selectRandomBySubjectId(subjectId, requestedCount));
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
