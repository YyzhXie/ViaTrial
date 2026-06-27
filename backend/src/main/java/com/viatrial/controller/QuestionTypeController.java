package com.viatrial.controller;

import com.viatrial.common.Result;
import com.viatrial.dto.request.QuestionTypeAddRequest;
import com.viatrial.dto.response.QuestionTypeResponse;
import com.viatrial.service.QuestionTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "题型管理")
@Validated
@RestController
@RequestMapping("/api/v1/question-types")
public class QuestionTypeController {

    private final QuestionTypeService questionTypeService;

    public QuestionTypeController(QuestionTypeService questionTypeService) {
        this.questionTypeService = questionTypeService;
    }

    @Operation(summary = "新增题型")
    @PostMapping
    public Result<Long> addQuestionType(@Valid @RequestBody QuestionTypeAddRequest request) {
        return Result.success(questionTypeService.addQuestionType(request));
    }

    @Operation(summary = "按科目查询题型")
    @GetMapping
    public Result<List<QuestionTypeResponse>> listQuestionTypes(
            @NotNull(message = "科目ID不能为空") @RequestParam Long subjectId) {
        return Result.success(questionTypeService.listQuestionTypes(subjectId));
    }

    @Operation(summary = "删除题型")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteQuestionType(@PathVariable Long id) {
        return Result.success(questionTypeService.deleteQuestionType(id));
    }
}
