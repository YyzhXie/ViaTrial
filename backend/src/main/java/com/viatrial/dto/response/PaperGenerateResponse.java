package com.viatrial.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "生成试卷响应")
public class PaperGenerateResponse {

    private String paperId;

    private Integer totalRequested;

    private Integer totalActual;

    private List<String> warnings;

    private List<PaperQuestionResponse> questions;

    public String getPaperId() {
        return paperId;
    }

    public void setPaperId(String paperId) {
        this.paperId = paperId;
    }

    public Integer getTotalRequested() {
        return totalRequested;
    }

    public void setTotalRequested(Integer totalRequested) {
        this.totalRequested = totalRequested;
    }

    public Integer getTotalActual() {
        return totalActual;
    }

    public void setTotalActual(Integer totalActual) {
        this.totalActual = totalActual;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public List<PaperQuestionResponse> getQuestions() {
        return questions;
    }

    public void setQuestions(List<PaperQuestionResponse> questions) {
        this.questions = questions;
    }
}
