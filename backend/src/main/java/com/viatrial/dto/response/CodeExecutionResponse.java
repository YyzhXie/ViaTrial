package com.viatrial.dto.response;

public record CodeExecutionResponse(
        String verdict,
        String status,
        String stdout,
        String stderr,
        String compileOutput,
        String time,
        String memory) {
}
