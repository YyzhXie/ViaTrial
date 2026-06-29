package com.viatrial.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

@Schema(description = "生成试卷请求")
public class PaperGenerateRequest {

    @NotNull(message = "科目ID不能为空")
    @Schema(description = "科目ID", example = "1")
    private Long subjectId;

    @NotEmpty(message = "题型抽题数量不能为空")
    @Schema(description = "题型抽题数量")
    private Map<Long, Integer> typeCountMap;

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Map<Long, Integer> getTypeCountMap() {
        return typeCountMap;
    }

    public void setTypeCountMap(Map<Long, Integer> typeCountMap) {
        this.typeCountMap = typeCountMap;
    }
}
