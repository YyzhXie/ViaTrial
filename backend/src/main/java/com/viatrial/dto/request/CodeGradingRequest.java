package com.viatrial.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CodeGradingRequest(
        @NotNull Long questionId,
        @NotBlank @Size(max = 20000) String sourceCode,
        @NotBlank @Pattern(regexp = "c|cpp|java|python") String language) {
}
