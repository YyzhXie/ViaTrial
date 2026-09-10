package com.viatrial.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "新增题目请求")
public class QuestionAddRequest {

    /**
     * 题目正文/答案/解析长度上限（审计项 C-2）。此前这些字段完全没有长度约束，
     * 可以写入任意大的 TEXT，批量读取时撑爆内存；这里与库中 TEXT 的实际用途对齐。
     */
    public static final int MAX_CONTENT_LENGTH = 20000;

    public static final int MAX_ANSWER_LENGTH = 20000;

    public static final int MAX_ANALYSIS_LENGTH = 20000;

    /** 单题标签数量上限（审计项 C-3），避免超长 IN 子句。 */
    public static final int MAX_TAG_COUNT = 50;

    @NotNull(message = "科目ID不能为空")
    @Schema(description = "科目ID", example = "1")
    private Long subjectId;

    @NotNull(message = "题型ID不能为空")
    @Schema(description = "题型ID", example = "1")
    private Long typeId;

    @NotBlank(message = "题目正文不能为空")
    @Size(max = MAX_CONTENT_LENGTH, message = "题目正文不能超过" + MAX_CONTENT_LENGTH + "个字符")
    @Schema(description = "题目正文")
    private String content;

    @Size(max = MAX_ANSWER_LENGTH, message = "参考答案不能超过" + MAX_ANSWER_LENGTH + "个字符")
    @Schema(description = "参考答案")
    private String answer;

    @Size(max = MAX_ANALYSIS_LENGTH, message = "解析不能超过" + MAX_ANALYSIS_LENGTH + "个字符")
    @Schema(description = "解析")
    private String analysis;

    @Size(max = 500, message = "题目图片URL不能超过500个字符")
    @Schema(description = "题目图片URL")
    private String imageUrl;

    @Size(max = 500, message = "答案图片URL不能超过500个字符")
    @Schema(description = "答案图片URL")
    private String answerImageUrl;

    @Min(value = 1, message = "难度只能为1、2、3")
    @Max(value = 3, message = "难度只能为1、2、3")
    @Schema(description = "难度：1简单，2中等，3困难", example = "1")
    private Integer difficulty;

    @Size(max = MAX_TAG_COUNT, message = "单题标签不能超过" + MAX_TAG_COUNT + "个")
    @Schema(description = "标签ID列表")
    private List<Long> tagIds;

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAnswerImageUrl() {
        return answerImageUrl;
    }

    public void setAnswerImageUrl(String answerImageUrl) {
        this.answerImageUrl = answerImageUrl;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public List<Long> getTagIds() {
        return tagIds;
    }

    public void setTagIds(List<Long> tagIds) {
        this.tagIds = tagIds;
    }
}
