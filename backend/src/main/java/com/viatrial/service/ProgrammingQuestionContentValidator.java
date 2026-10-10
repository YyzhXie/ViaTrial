package com.viatrial.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.viatrial.common.BizException;
import com.viatrial.dto.request.QuestionAddRequest;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class ProgrammingQuestionContentValidator {

    public record TestCase(String input, String expectedOutput, boolean sample) {}
    public record ExecutionSettings(boolean competitionMode, int timeLimitMs, int memoryLimitMb) {}

    private static final String MARKER_V1 = "<!--viatrial-programming:v1:";
    private static final String MARKER_V2 = "<!--viatrial-programming:v2:";
    private static final int MAX_TEST_CASES = 20;
    private static final int MAX_CODE_LENGTH = 20000;
    private static final int MAX_TEST_VALUE_LENGTH = 10000;
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("c", "cpp", "java", "python");

    private final ObjectMapper objectMapper;

    public ProgrammingQuestionContentValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** Remove private test points before a programming question is sent to the practice client. */
    public String practiceContent(String content) {
        String marker = markerFor(content);
        if (marker == null) return content;
        int end = content.indexOf("-->", marker.length());
        if (end < 0) return content;
        try {
            byte[] bytes = Base64.getDecoder().decode(content.substring(marker.length(), end));
            JsonNode data = objectMapper.readTree(bytes);
            ArrayNode samples = objectMapper.createArrayNode();
            JsonNode cases = data.path("testCases");
            if (cases.isArray()) {
                for (JsonNode testCase : cases) {
                    // Existing v1 questions predate the visibility flag, so their cases stay public.
                    if (!testCase.has("isSample") || testCase.path("isSample").asBoolean(false)) {
                        ObjectNode sample = objectMapper.createObjectNode();
                        sample.put("input", testCase.path("input").asText(""));
                        sample.put("expectedOutput", testCase.path("expectedOutput").asText(""));
                        sample.put("isSample", true);
                        samples.add(sample);
                    }
                }
            }
            ObjectNode publicData = data.deepCopy();
            publicData.set("testCases", samples);
            String encoded = Base64.getEncoder().encodeToString(
                    objectMapper.writeValueAsBytes(publicData));
            return marker + encoded + content.substring(end);
        } catch (Exception exception) {
            throw new BizException(400, "编程题数据格式无效");
        }
    }

    public List<TestCase> testCases(String content) {
        String marker = markerFor(content);
        if (marker == null) throw new BizException(400, "编程题数据格式无效");
        int end = content.indexOf("-->", marker.length());
        if (end < 0) throw new BizException(400, "编程题数据格式无效");
        try {
            JsonNode data = objectMapper.readTree(Base64.getDecoder().decode(content.substring(marker.length(), end)));
            JsonNode cases = data.path("testCases");
            if (!cases.isArray()) throw new BizException(400, "编程题测试点数据无效");
            List<TestCase> result = new ArrayList<>(cases.size());
            for (JsonNode testCase : cases) {
                result.add(new TestCase(testCase.path("input").asText(),
                        testCase.path("expectedOutput").asText(),
                        !testCase.has("isSample") || testCase.path("isSample").asBoolean(false)));
            }
            return result;
        } catch (BizException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BizException(400, "编程题数据格式无效");
        }
    }

    public ExecutionSettings executionSettings(String content) {
        String marker = markerFor(content);
        if (marker == null) throw new BizException(400, "编程题数据格式无效");
        int end = content.indexOf("-->", marker.length());
        if (end < 0) throw new BizException(400, "编程题数据格式无效");
        try {
            JsonNode data = objectMapper.readTree(Base64.getDecoder().decode(content.substring(marker.length(), end)));
            return new ExecutionSettings(data.path("competitionMode").asBoolean(false),
                    data.path("timeLimitMs").asInt(1000), data.path("memoryLimitMb").asInt(256));
        } catch (Exception exception) {
            throw new BizException(400, "编程题数据格式无效");
        }
    }

    public void validate(String content, String questionTypeName) {
        boolean programming = questionTypeName.matches("(?i).*(编程|程序|programming|coding).*");
        if (!programming) {
            if (content != null && content.length() > QuestionAddRequest.MAX_CONTENT_LENGTH) {
                throw new BizException(400,
                        "普通题目正文不能超过" + QuestionAddRequest.MAX_CONTENT_LENGTH + "个字符");
            }
            return;
        }

        if (content == null || content.length() > QuestionAddRequest.MAX_PROGRAMMING_CONTENT_LENGTH) {
            throw new BizException(400, "编程题内容超过允许上限");
        }
        String marker = markerFor(content);
        int end = marker == null ? -1 : content.indexOf("-->", marker.length());
        if (end < 0) {
            throw new BizException(400, "编程题数据格式无效");
        }

        try {
            String encodedData = content.substring(marker.length(), end);
            JsonNode data = objectMapper.readTree(Base64.getDecoder().decode(encodedData));
            String language = data.path("language").asText();
            String statement = data.path("statement").asText();
            String starterCode = data.path("starterCode").asText();
            JsonNode competitionMode = data.path("competitionMode");
            JsonNode timeLimitMs = data.path("timeLimitMs");
            JsonNode memoryLimitMb = data.path("memoryLimitMb");
            JsonNode testCases = data.path("testCases");
            if (!SUPPORTED_LANGUAGES.contains(language)
                    || statement.length() > QuestionAddRequest.MAX_CONTENT_LENGTH
                    || starterCode.length() > MAX_CODE_LENGTH
                    || !testCases.isArray()
                    || testCases.isEmpty()
                    || testCases.size() > MAX_TEST_CASES) {
                throw new BizException(400, "编程题语言、题面、代码或测试点数量无效");
            }
            if (!competitionMode.isMissingNode() && !competitionMode.isBoolean()) {
                throw new BizException(400, "竞赛模式配置无效");
            }
            if ((!timeLimitMs.isMissingNode() && !timeLimitMs.canConvertToInt())
                    || (!memoryLimitMb.isMissingNode() && !memoryLimitMb.canConvertToInt())) {
                throw new BizException(400, "竞赛模式配置无效");
            }
            if (competitionMode.asBoolean(false)
                    && (timeLimitMs.asInt(1000) < 50 || timeLimitMs.asInt(1000) > 30000
                    || memoryLimitMb.asInt(256) < 16 || memoryLimitMb.asInt(256) > 2048)) {
                throw new BizException(400, "竞赛模式的时间限制或内存限制无效");
            }
            for (JsonNode testCase : testCases) {
                if (!testCase.path("input").isTextual()
                        || !testCase.path("expectedOutput").isTextual()
                        || (testCase.has("isSample") && !testCase.path("isSample").isBoolean())
                        || testCase.path("input").asText().length() > MAX_TEST_VALUE_LENGTH
                        || testCase.path("expectedOutput").asText().length() > MAX_TEST_VALUE_LENGTH) {
                    throw new BizException(400, "每个测试点的输入和预期输出不能超过10000个字符");
                }
            }
        } catch (BizException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BizException(400, "编程题数据格式无效");
        }
    }

    private String markerFor(String content) {
        if (content == null) return null;
        if (content.startsWith(MARKER_V1)) return MARKER_V1;
        if (content.startsWith(MARKER_V2)) return MARKER_V2;
        return null;
    }
}
