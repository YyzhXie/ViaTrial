package com.viatrial.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "题型响应")
public class QuestionTypeResponse {

    @Schema(description = "题型ID", example = "1")
    private Long id;

    @Schema(description = "科目ID", example = "1")
    private Long subjectId;

    @Schema(description = "题型名称", example = "选择题")
    private String name;

    public QuestionTypeResponse() {
    }

    public QuestionTypeResponse(Long id, Long subjectId, String name) {
        this.id = id;
        this.subjectId = subjectId;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
