package com.viatrial.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "新增题型请求")
public class QuestionTypeAddRequest {

    @NotNull(message = "科目ID不能为空")
    @Schema(description = "科目ID", example = "1")
    private Long subjectId;

    @NotBlank(message = "题型名称不能为空")
    @Size(max = 50, message = "题型名称不能超过50个字符")
    @Schema(description = "题型名称", example = "选择题")
    private String name;

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
