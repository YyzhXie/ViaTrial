package com.viatrial.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "新增科目请求")
public class SubjectAddRequest {

    @NotBlank(message = "科目名称不能为空")
    @Size(max = 50, message = "科目名称不能超过50个字符")
    @Schema(description = "科目名称", example = "高等数学")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
