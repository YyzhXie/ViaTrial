package com.viatrial.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(description = "分页查询题目请求")
public class QuestionPageRequest {

    @Min(value = 1, message = "页码必须大于0")
    @Schema(description = "当前页", example = "1")
    private Long page = 1L;

    @Min(value = 1, message = "每页数量必须大于0")
    @Schema(description = "每页数量", example = "10")
    private Long size = 10L;

    @Schema(description = "科目ID")
    private Long subjectId;

    @Schema(description = "题型ID")
    private Long typeId;

    @Schema(description = "标签ID")
    private Long tagId;

    public Long getPage() {
        return page == null ? 1L : page;
    }

    public void setPage(Long page) {
        this.page = page;
    }

    public Long getSize() {
        return size == null ? 10L : size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }
}
