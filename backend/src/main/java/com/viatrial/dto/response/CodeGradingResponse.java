package com.viatrial.dto.response;

import java.util.List;

public record CodeGradingResponse(String verdict, String status, List<TestPointResult> testPoints) {
    public record TestPointResult(
            int index,
            String label,
            String verdict,
            String status,
            String time,
            String memory) {
    }
}
