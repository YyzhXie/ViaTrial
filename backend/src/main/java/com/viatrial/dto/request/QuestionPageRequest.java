package com.viatrial.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "分页查询题目请求")
public class QuestionPageRequest {

    /**
     * 分页上限（审计项 C-1）。原先只校验 {@code @Min(1)}，{@code size=1000000}
     * 会生成超大 LIMIT/OFFSET，形成 CPU 与内存放大型 DoS。
     */
    public static final long MAX_PAGE = 10000L;

    public static final long MAX_SIZE = 100L;

    @Min(value = 1, message = "页码必须大于0")
    @Max(value = MAX_PAGE, message = "页码不能超过" + MAX_PAGE)
    @Schema(description = "当前页", example = "1")
    private Long page = 1L;

    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = MAX_SIZE, message = "每页数量不能超过" + MAX_SIZE)
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
