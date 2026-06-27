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

    @NotNull(message = "科目ID不能为空")
    @Schema(description = "科目ID", example = "1")
    private Long subjectId;

    @NotNull(message = "题型ID不能为空")
    @Schema(description = "题型ID", example = "1")
    private Long typeId;

    @NotBlank(message = "题目正文不能为空")
    @Schema(description = "题目正文")
    private String content;

    @Schema(description = "参考答案")
    private String answer;

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
