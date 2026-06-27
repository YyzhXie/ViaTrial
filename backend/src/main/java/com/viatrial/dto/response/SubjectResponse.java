package com.viatrial.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "科目响应")
public class SubjectResponse {

    @Schema(description = "科目ID", example = "1")
    private Long id;

    @Schema(description = "科目名称", example = "高等数学")
    private String name;

    public SubjectResponse() {
    }

    public SubjectResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
