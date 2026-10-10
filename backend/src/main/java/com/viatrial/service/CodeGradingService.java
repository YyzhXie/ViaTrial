package com.viatrial.service;

import com.viatrial.common.BizException;
import com.viatrial.dto.request.CodeGradingRequest;
import com.viatrial.dto.response.CodeExecutionResponse;
import com.viatrial.dto.response.CodeGradingResponse;
import com.viatrial.entity.Question;
import com.viatrial.entity.QuestionType;
import com.viatrial.mapper.QuestionMapper;
import com.viatrial.mapper.QuestionTypeMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class CodeGradingService {

    private static final Pattern PROGRAMMING_TYPE = Pattern.compile("(?i).*(编程|程序|programming|coding).*");

    private final QuestionMapper questionMapper;
    private final QuestionTypeMapper questionTypeMapper;
    private final ProgrammingQuestionContentValidator contentValidator;
    private final CodeExecutionService executionService;

    public CodeGradingService(QuestionMapper questionMapper,
                              QuestionTypeMapper questionTypeMapper,
                              ProgrammingQuestionContentValidator contentValidator,
                              CodeExecutionService executionService) {
        this.questionMapper = questionMapper;
        this.questionTypeMapper = questionTypeMapper;
        this.contentValidator = contentValidator;
        this.executionService = executionService;
    }

    public CodeGradingResponse grade(CodeGradingRequest request) {
        Question question = questionMapper.selectById(request.questionId());
        if (question == null) throw new BizException(404, "题目不存在");
        QuestionType type = questionTypeMapper.selectById(question.getTypeId());
        if (type == null || !PROGRAMMING_TYPE.matcher(type.getName()).matches()) {
            throw new BizException(400, "题目不是编程题");
        }
        ProgrammingQuestionContentValidator.ExecutionSettings settings =
                contentValidator.executionSettings(question.getContent());
        List<ProgrammingQuestionContentValidator.TestCase> cases = contentValidator.testCases(question.getContent());
        if (cases.isEmpty()) throw new BizException(400, "该编程题没有配置测试点");

        List<CodeExecutionResponse> executions = executionService.executeAll(request.sourceCode(), request.language(), cases,
                settings.competitionMode() ? settings.timeLimitMs() : null,
                settings.competitionMode() ? settings.memoryLimitMb() : null);
        List<CodeGradingResponse.TestPointResult> pointResults = new ArrayList<>(cases.size());
        String overallVerdict = "AC";
        int sampleNumber = 0;
        for (int i = 0; i < cases.size(); i++) {
            ProgrammingQuestionContentValidator.TestCase testCase = cases.get(i);
            CodeExecutionResponse result = executions.get(i);
            String label = testCase.sample() ? "样例 " + (++sampleNumber) : "测试点 " + (i + 1);
            pointResults.add(new CodeGradingResponse.TestPointResult(i + 1, label, result.verdict(),
                    result.status(), result.time(), result.memory()));
            if ("AC".equals(overallVerdict) && !"AC".equals(result.verdict())) {
                overallVerdict = result.verdict();
            }
        }
        String status = "AC".equals(overallVerdict) ? "所有测试点通过" : "存在未通过测试点";
        return new CodeGradingResponse(overallVerdict, status, pointResults);
    }
}
