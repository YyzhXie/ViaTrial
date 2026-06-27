package com.viatrial.controller;

import com.viatrial.common.Result;
import com.viatrial.dto.request.TagAddRequest;
import com.viatrial.dto.response.TagResponse;
import com.viatrial.service.TagService;
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

@Tag(name = "标签管理")
@RestController
@RequestMapping("/api/v1/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @Operation(summary = "新增标签")
    @PostMapping
    public Result<Long> addTag(@Valid @RequestBody TagAddRequest request) {
        return Result.success(tagService.addTag(request));
    }

    @Operation(summary = "查询标签列表")
    @GetMapping
    public Result<List<TagResponse>> listTags() {
        return Result.success(tagService.listTags());
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteTag(@PathVariable Long id) {
        return Result.success(tagService.deleteTag(id));
    }
}
