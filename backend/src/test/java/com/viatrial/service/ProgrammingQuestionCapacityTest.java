package com.viatrial.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viatrial.common.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProgrammingQuestionCapacityTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ProgrammingQuestionContentValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ProgrammingQuestionContentValidator(objectMapper);
    }

    @Test
    void shouldAcceptTwentyMaximumSizeTestCases() {
        assertDoesNotThrow(() -> validator.validate(programmingContent(20, 10000, 10000), "编程题"));
    }

    @Test
    void shouldRejectTooManyOrOversizedTestCasesAndOrdinaryQuestion() {
        assertThrows(BizException.class,
                () -> validator.validate(programmingContent(21, 1, 1), "编程题"));
        assertThrows(BizException.class,
                () -> validator.validate(programmingContent(1, 10001, 1), "编程题"));
        assertThrows(BizException.class,
                () -> validator.validate("x".repeat(20001), "填空题"));
    }

    @Test
    void shouldRejectMalformedProgrammingPayload() {
        assertThrows(BizException.class, () -> validator.validate("not encoded", "编程题"));
    }

    private String programmingContent(int count, int inputLength, int outputLength) {
        List<Map<String, String>> cases = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cases.add(Map.of("input", "i".repeat(inputLength),
                    "expectedOutput", "o".repeat(outputLength)));
        }
        try {
            String metadata = objectMapper.writeValueAsString(Map.of(
                    "statement", "题面".repeat(10000),
                    "language", "cpp",
                    "starterCode", "x".repeat(20000),
                    "testCases", cases));
            String encoded = Base64.getEncoder().encodeToString(metadata.getBytes(StandardCharsets.UTF_8));
            return "<!--viatrial-programming:v1:" + encoded + "-->\n" + "题面".repeat(10000);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
