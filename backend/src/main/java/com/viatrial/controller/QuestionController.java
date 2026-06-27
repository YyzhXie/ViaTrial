package com.viatrial.controller;

import com.viatrial.common.PageResult;
import com.viatrial.common.Result;
import com.viatrial.dto.request.QuestionAddRequest;
import com.viatrial.dto.request.QuestionPageRequest;
import com.viatrial.dto.response.QuestionResponse;
import com.viatrial.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "题目管理")
@Validated
@RestController
@RequestMapping("/api/v1/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @Operation(summary = "新增题目")
    @PostMapping
    public Result<Long> addQuestion(@Valid @RequestBody QuestionAddRequest request) {
        return Result.success(questionService.addQuestion(request));
    }

    @Operation(summary = "分页查询题目")
    @GetMapping("/page")
    public Result<PageResult<QuestionResponse>> pageQuestions(@Valid @ModelAttribute QuestionPageRequest request) {
        return Result.success(questionService.pageQuestions(request));
    }

    @Operation(summary = "删除题目")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteQuestion(@PathVariable Long id) {
        return Result.success(questionService.deleteQuestion(id));
    }
}
