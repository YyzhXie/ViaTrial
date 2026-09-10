package com.viatrial.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

@Schema(description = "预览试卷请求")
public class PaperGenerateRequest {

    /**
     * 单题型单次抽题上限（审计项 C-2）。原先 Integer 无上界，可以要求抽取
     * Integer.MAX_VALUE 题，触发 LIMIT 巨值全表扫描 + 随机排序。
     */
    public static final int MAX_QUESTIONS_PER_TYPE = 200;

    @NotNull(message = "科目ID不能为空")
    @Schema(description = "科目ID", example = "1")
    private Long subjectId;

    @NotEmpty(message = "题型抽题数量不能为空")
    @Size(max = 50, message = "单次组卷的题型数量不能超过50个")
    @Valid
    @Schema(description = "题型抽题数量，每项取值 1-" + MAX_QUESTIONS_PER_TYPE)
    private Map<@NotNull(message = "题型ID不能为空") Long,
            @NotNull(message = "抽题数量不能为空")
            @Min(value = 1, message = "抽题数量必须大于0")
            @Max(value = MAX_QUESTIONS_PER_TYPE, message = "单题型抽题数量不能超过" + MAX_QUESTIONS_PER_TYPE)
            Integer> typeCountMap;

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
