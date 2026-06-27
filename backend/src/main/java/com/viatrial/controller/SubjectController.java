package com.viatrial.controller;

import com.viatrial.common.Result;
import com.viatrial.dto.request.SubjectAddRequest;
import com.viatrial.dto.response.SubjectResponse;
import com.viatrial.service.SubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "科目管理")
@RestController
@RequestMapping("/api/v1/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @Operation(summary = "新增科目")
    @PostMapping
    public Result<Long> addSubject(@Valid @RequestBody SubjectAddRequest request) {
        return Result.success(subjectService.addSubject(request));
    }

    @Operation(summary = "查询科目列表")
    @GetMapping
    public Result<List<SubjectResponse>> listSubjects() {
        return Result.success(subjectService.listSubjects());
    }

    @Operation(summary = "删除科目")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteSubject(@PathVariable Long id) {
        return Result.success(subjectService.deleteSubject(id));
    }
}
