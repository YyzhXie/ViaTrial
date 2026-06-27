package com.viatrial.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "标签响应")
public class TagResponse {

    @Schema(description = "标签ID", example = "1")
    private Long id;

    @Schema(description = "标签名称", example = "期末重点")
    private String name;

    public TagResponse() {
    }

    public TagResponse(Long id, String name) {
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
