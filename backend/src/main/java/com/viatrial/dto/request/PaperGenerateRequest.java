package com.viatrial.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.Map;

@Schema(description = "生成试卷请求")
public class PaperGenerateRequest {

    @NotEmpty(message = "抽题科目数量不能为空")
    @Schema(description = "科目抽题数量")
    private Map<Long, Integer> subjectCountMap;

    public Map<Long, Integer> getSubjectCountMap() {
        return subjectCountMap;
    }

    public void setSubjectCountMap(Map<Long, Integer> subjectCountMap) {
        this.subjectCountMap = subjectCountMap;
    }
}
