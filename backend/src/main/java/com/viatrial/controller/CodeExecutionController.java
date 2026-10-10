package com.viatrial.controller;

import com.viatrial.common.Result;
import com.viatrial.dto.request.CodeExecutionRequest;
import com.viatrial.dto.request.CodeGradingRequest;
import com.viatrial.dto.response.CodeExecutionResponse;
import com.viatrial.dto.response.CodeGradingResponse;
import com.viatrial.service.CodeGradingService;
import com.viatrial.service.CodeExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "在线代码执行")
@RestController
@RequestMapping("/api/v1/code")
public class CodeExecutionController {

    private final CodeExecutionService codeExecutionService;
    private final CodeGradingService codeGradingService;

    public CodeExecutionController(CodeExecutionService codeExecutionService, CodeGradingService codeGradingService) {
        this.codeExecutionService = codeExecutionService;
        this.codeGradingService = codeGradingService;
    }

    @Operation(summary = "编译并运行单个自定义测试点")
    @PostMapping("/execute")
    public Result<CodeExecutionResponse> execute(@Valid @RequestBody CodeExecutionRequest request) {
        return Result.success(codeExecutionService.execute(request));
    }

    @Operation(summary = "提交编程题并运行完整测试集")
    @PostMapping("/grade")
    public Result<CodeGradingResponse> grade(@Valid @RequestBody CodeGradingRequest request) {
        return Result.success(codeGradingService.grade(request));
    }
}
