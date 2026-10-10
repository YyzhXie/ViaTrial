package com.viatrial.service;

import com.viatrial.dto.request.CodeExecutionRequest;
import com.viatrial.dto.response.CodeExecutionResponse;
import com.viatrial.config.DataDirectoryResolver;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CodeExecutionServiceTest {

    private final CodeExecutionService service = new CodeExecutionService(
            "", "", "", "", 2,
            new CodeToolchainInstaller(mockDataDirectoryResolver()));

    private static DataDirectoryResolver mockDataDirectoryResolver() {
        DataDirectoryResolver resolver = mock(DataDirectoryResolver.class);
        when(resolver.getDataDirectory()).thenReturn(Path.of(System.getProperty("java.io.tmpdir")));
        return resolver;
    }

    @Test
    void shouldCompileAndRunJavaLocally() {
        CodeExecutionResponse result = service.execute(new CodeExecutionRequest(
                "public class Main { public static void main(String[] args) { System.out.println(42); } }",
                "java", "", "42\n", null, null));

        assertEquals("AC", result.verdict());
        assertEquals("42\n", result.stdout());
    }

    @Test
    void shouldReturnWrongAnswerForDifferentOutput() {
        CodeExecutionResponse result = service.execute(new CodeExecutionRequest(
                "public class Main { public static void main(String[] args) { System.out.println(41); } }",
                "java", "", "42\n", null, null));

        assertEquals("WA", result.verdict());
    }
}
