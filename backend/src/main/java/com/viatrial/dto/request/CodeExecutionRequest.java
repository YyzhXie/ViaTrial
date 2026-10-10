package com.viatrial.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CodeExecutionRequest(
        @NotBlank @Size(max = 20000) String sourceCode,
        @NotBlank @Pattern(regexp = "c|cpp|java|python") String language,
        @NotNull @Size(max = 10000) String input,
        @NotNull @Size(max = 10000) String expectedOutput,
        @Min(50) @Max(30000) Integer timeLimitMs,
        @Min(16) @Max(2048) Integer memoryLimitMb) {
}
