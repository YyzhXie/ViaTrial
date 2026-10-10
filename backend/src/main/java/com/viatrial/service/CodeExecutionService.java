package com.viatrial.service;

import com.viatrial.common.BizException;
import com.viatrial.dto.request.CodeExecutionRequest;
import com.viatrial.dto.response.CodeExecutionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Service
public class CodeExecutionService {

    private static final Duration COMPILE_TIMEOUT = Duration.ofSeconds(20);
    private static final Duration RUN_TIMEOUT = Duration.ofSeconds(5);
    private static final int MAX_OUTPUT_BYTES = 64 * 1024;
    private static final String OUTPUT_TRUNCATED = "\n[输出已截断]";

    private final String cCompiler;
    private final String cppCompiler;
    private final String javaRuntime;
    private final String pythonRuntime;
    private final CodeToolchainInstaller toolchainInstaller;
    private final Semaphore executionSlots;

    public CodeExecutionService(
            @Value("${viatrial.code-execution.compiler.c:gcc}") String cCompiler,
            @Value("${viatrial.code-execution.compiler.cpp:g++}") String cppCompiler,
            @Value("${viatrial.code-execution.runtime.java:}") String javaRuntime,
            @Value("${viatrial.code-execution.runtime.python:}") String pythonRuntime,
            @Value("${viatrial.code-execution.max-concurrent:2}") int maxConcurrentExecutions,
            CodeToolchainInstaller toolchainInstaller) {
        if (maxConcurrentExecutions < 1) {
            throw new IllegalArgumentException("max-concurrent must be at least 1");
        }
        this.cCompiler = cCompiler == null ? "" : cCompiler.trim();
        this.cppCompiler = cppCompiler == null ? "" : cppCompiler.trim();
        this.javaRuntime = javaRuntime == null ? "" : javaRuntime.trim();
        this.pythonRuntime = pythonRuntime == null ? "" : pythonRuntime.trim();
        this.toolchainInstaller = toolchainInstaller;
        this.executionSlots = new Semaphore(maxConcurrentExecutions);
    }

    public CodeExecutionResponse execute(CodeExecutionRequest request) {
        if (!executionSlots.tryAcquire()) {
            throw new BizException(503, "当前代码执行任务较多，请稍后重试");
        }

        Path workDirectory = null;
        try {
            workDirectory = Files.createTempDirectory("viatrial-code-");
            prepare(request.language(), request.sourceCode(), workDirectory);

            CodeExecutionResponse compileFailure = compile(request.language(), workDirectory);
            if (compileFailure != null) return compileFailure;
            return executePrepared(request, workDirectory);
        } catch (IOException exception) {
            return response("CE", "本地工具链准备失败，请检查网络连接或工具链配置", null,
                    exception.getMessage(), null, null);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BizException(503, "代码执行已中断");
        } finally {
            if (workDirectory != null) {
                deleteDirectory(workDirectory);
            }
            executionSlots.release();
        }
    }

    public List<CodeExecutionResponse> executeAll(String sourceCode, String language,
                                                   List<ProgrammingQuestionContentValidator.TestCase> cases,
                                                   Integer timeLimitMs, Integer memoryLimitMb) {
        if (cases.isEmpty()) return List.of();
        if (!executionSlots.tryAcquire()) {
            throw new BizException(503, "当前代码执行任务较多，请稍后重试");
        }

        Path workDirectory = null;
        try {
            workDirectory = Files.createTempDirectory("viatrial-code-");
            prepare(language, sourceCode, workDirectory);
            CodeExecutionResponse compileFailure = compile(language, workDirectory);
            if (compileFailure != null) return Collections.nCopies(cases.size(), compileFailure);

            List<CodeExecutionResponse> results = new ArrayList<>(cases.size());
            for (ProgrammingQuestionContentValidator.TestCase testCase : cases) {
                CodeExecutionRequest request = new CodeExecutionRequest(sourceCode, language,
                        testCase.input(), testCase.expectedOutput(), timeLimitMs, memoryLimitMb);
                Path caseDirectory = createCaseDirectory(workDirectory);
                results.add(executePrepared(request, caseDirectory));
            }
            return results;
        } catch (IOException exception) {
            CodeExecutionResponse error = response("CE", "本地工具链准备失败，请检查网络连接或工具链配置", null,
                    exception.getMessage(), null, null);
            return Collections.nCopies(cases.size(), error);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BizException(503, "代码执行已中断");
        } finally {
            if (workDirectory != null) deleteDirectory(workDirectory);
            executionSlots.release();
        }
    }

    private CodeExecutionResponse compile(String language, Path workDirectory) throws IOException, InterruptedException {
        if (isInterpreted(language)) return null;
        List<String> compileCommand = compileCommand(language, workDirectory);
        ProcessResult compilation = runProcess(compileCommand, workDirectory, "", COMPILE_TIMEOUT, null);
        if (compilation.timedOut()) {
            return response("TLE", "编译超时", null, compilation.stderr(), compilation.stdout(), compilation.elapsedSeconds());
        }
        if (compilation.exitCode() != 0) {
            return response("CE", "编译失败", null, compilation.stderr(), compilation.stdout(), compilation.elapsedSeconds());
        }
        return null;
    }

    private CodeExecutionResponse executePrepared(CodeExecutionRequest request, Path workDirectory)
            throws IOException, InterruptedException {
        List<String> runCommand = prepareRunCommand(request.language(), workDirectory);
        Duration runTimeout = request.timeLimitMs() == null ? RUN_TIMEOUT : Duration.ofMillis(request.timeLimitMs());
        ProcessResult execution = runProcess(runCommand, workDirectory, request.input(), runTimeout, request.memoryLimitMb());
        if (execution.timedOut()) {
            return response("TLE", "时间超限 (TLE)", execution.stdout(), execution.stderr(), null,
                    execution.elapsedSeconds(), execution.peakMemoryBytes());
        }
        if (execution.memoryExceeded()) {
            return response("MLE", "内存超限 (MLE)", execution.stdout(), execution.stderr(), null,
                    execution.elapsedSeconds(), execution.peakMemoryBytes());
        }
        if (execution.resourceMeasurementFailed()) {
            return response("ERROR", "无法读取程序内存，竞赛判题未完成", execution.stdout(), execution.stderr(), null,
                    execution.elapsedSeconds(), execution.peakMemoryBytes());
        }
        if (execution.exitCode() != 0) {
            return response("RE", "运行错误", execution.stdout(), execution.stderr(), null,
                    execution.elapsedSeconds(), execution.peakMemoryBytes());
        }
        String verdict = normalize(execution.stdout()).equals(normalize(request.expectedOutput())) ? "AC" : "WA";
        return response(verdict, "AC".equals(verdict) ? "Accepted" : "Wrong Answer",
                execution.stdout(), execution.stderr(), null, execution.elapsedSeconds(), execution.peakMemoryBytes());
    }

    private Path createCaseDirectory(Path compiledDirectory) throws IOException {
        Path caseDirectory = Files.createTempDirectory(compiledDirectory, "case-");
        try (var files = Files.list(compiledDirectory)) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                Files.copy(file, caseDirectory.resolve(file.getFileName()));
            }
        }
        return caseDirectory;
    }

    private void prepare(String language, String source, Path directory) throws IOException {
        String fileName = switch (language) {
            case "c" -> "Main.c";
            case "cpp" -> "Main.cpp";
            case "java" -> "Main.java";
            case "python" -> "Main.py";
            default -> throw new BizException(400, "暂不支持该编程语言");
        };
        Files.writeString(directory.resolve(fileName), source, StandardCharsets.UTF_8);
    }

    private List<String> compileCommand(String language, Path directory) throws IOException, InterruptedException {
        return switch (language) {
            case "c" -> compilerCommand(cCompiler, "gcc", "cc", "-std=c17", "-O2", "-o", executablePath(directory), directory.resolve("Main.c").toString());
            case "cpp" -> compilerCommand(cppCompiler, "g++", "c++", "-std=c++17", "-O2", "-o", executablePath(directory), directory.resolve("Main.cpp").toString());
            case "java" -> List.of(resolveJavaRuntime(), "-m", "jdk.compiler/com.sun.tools.javac.Main", "-encoding", "UTF-8", "-proc:none", "-d", directory.toString(), directory.resolve("Main.java").toString());
            default -> throw new BizException(400, "暂不支持该编程语言");
        };
    }

    private List<String> prepareRunCommand(String language, Path directory) throws IOException, InterruptedException {
        if ("c".equals(language) || "cpp".equals(language)) {
            return List.of(executablePath(directory));
        }
        if ("java".equals(language)) {
            return List.of(resolveJavaRuntime(), "-Dfile.encoding=UTF-8", "-cp", directory.toString(), "Main");
        }
        if ("python".equals(language)) {
            return List.of(resolvePythonRuntime(), "-I", directory.resolve("Main.py").toString());
        }
        throw new BizException(400, "暂不支持该编程语言");
    }

    private boolean isInterpreted(String language) {
        return "python".equals(language);
    }

    private String executablePath(Path directory) {
        String name = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win") ? "Main.exe" : "Main";
        return directory.resolve(name).toString();
    }

    private ProcessResult runProcess(List<String> command, Path directory, String input, Duration timeout, Integer memoryLimitMb)
            throws IOException, InterruptedException {
        long startedAt = System.nanoTime();
        Process process = new ProcessBuilder(command).directory(directory.toFile()).start();
        CompletableFuture<String> stdout = readLimited(process.getInputStream());
        CompletableFuture<String> stderr = readLimited(process.getErrorStream());
        try (var stdin = process.getOutputStream()) {
            stdin.write(input.getBytes(StandardCharsets.UTF_8));
        }

        long deadline = startedAt + timeout.toNanos();
        long nextMemorySample = startedAt;
        long peakMemoryBytes = 0;
        boolean timedOut = false;
        boolean memoryExceeded = false;
        boolean resourceMeasurementFailed = false;
        while (process.isAlive()) {
            long now = System.nanoTime();
            if (now >= deadline) {
                timedOut = true;
                break;
            }
            if (memoryLimitMb != null && now >= nextMemorySample) {
                long memoryBytes = ProcessMemoryMonitor.sampleTreeBytes(process);
                if (memoryBytes >= 0) {
                    peakMemoryBytes = Math.max(peakMemoryBytes, memoryBytes);
                    if (memoryBytes > memoryLimitMb * 1024L * 1024L) {
                        memoryExceeded = true;
                        break;
                    }
                } else {
                    resourceMeasurementFailed = true;
                    break;
                }
                nextMemorySample = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(75);
            }
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0) {
                timedOut = true;
                break;
            }
            process.waitFor(Math.min(TimeUnit.NANOSECONDS.toMillis(remaining), 50), TimeUnit.MILLISECONDS);
        }
        if (timedOut || memoryExceeded || resourceMeasurementFailed) {
            terminateTree(process);
            process.waitFor(1, TimeUnit.SECONDS);
        }
        String output = getReader(stdout);
        String error = getReader(stderr);
        double elapsed = (System.nanoTime() - startedAt) / 1_000_000_000.0;
        return new ProcessResult(process.isAlive() || timedOut || memoryExceeded ? -1 : process.exitValue(),
                timedOut, memoryExceeded, resourceMeasurementFailed, output, error,
                String.format(Locale.ROOT, "%.3f", elapsed), peakMemoryBytes);
    }

    private CompletableFuture<String> readLimited(InputStream stream) {
        return CompletableFuture.supplyAsync(() -> {
            try (stream; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int total = 0;
                boolean truncated = false;
                int count;
                while ((count = stream.read(buffer)) != -1) {
                    int accepted = Math.min(count, MAX_OUTPUT_BYTES - total);
                    if (accepted > 0) {
                        output.write(buffer, 0, accepted);
                        total += accepted;
                    }
                    if (accepted < count) truncated = true;
                }
                String value = output.toString(StandardCharsets.UTF_8);
                return truncated ? value + OUTPUT_TRUNCATED : value;
            } catch (IOException exception) {
                return "读取程序输出失败: " + exception.getMessage();
            }
        });
    }

    private String getReader(CompletableFuture<String> reader) throws InterruptedException {
        try {
            return reader.get(2, TimeUnit.SECONDS);
        } catch (java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException exception) {
            return "读取程序输出失败";
        }
    }

    private void terminateTree(Process process) {
        process.toHandle().descendants().forEach(ProcessHandle::destroyForcibly);
        process.destroyForcibly();
    }

    private String normalize(String value) {
        return value == null ? "" : value.replace("\r\n", "\n").stripTrailing();
    }

    private CodeExecutionResponse response(String verdict, String status, String stdout, String stderr,
                                           String compileOutput, String time) {
        return response(verdict, status, stdout, stderr, compileOutput, time, 0);
    }

    private CodeExecutionResponse response(String verdict, String status, String stdout, String stderr,
                                           String compileOutput, String time, long memoryBytes) {
        String memory = memoryBytes <= 0 ? null : String.format(Locale.ROOT, "%.1f MB", memoryBytes / (1024.0 * 1024.0));
        return new CodeExecutionResponse(verdict, status, stdout, stderr, compileOutput, time, memory);
    }

    private List<String> compilerCommand(String configured, String systemCommand, String zigSubcommand, String... args)
            throws IOException, InterruptedException {
        String executable;
        boolean zig = false;
        if (!configured.isBlank()) {
            executable = resolveConfiguredCommand(configured);
        } else {
            executable = findOnPath(systemCommand);
            if (executable == null) {
                executable = toolchainInstaller.zigExecutable().toString();
                zig = true;
            }
        }
        List<String> command = new java.util.ArrayList<>();
        command.add(executable);
        if (zig) command.add(zigSubcommand);
        command.addAll(List.of(args));
        return command;
    }

    private String resolvePythonRuntime() throws IOException, InterruptedException {
        if (!pythonRuntime.isBlank()) return resolveConfiguredCommand(pythonRuntime);
        String systemPython3 = findOnPath("python3");
        if (systemPython3 != null) return systemPython3;
        String systemPython = findOnPath("python");
        return systemPython != null ? systemPython : toolchainInstaller.pythonExecutable().toString();
    }

    private String resolveJavaRuntime() throws IOException {
        if (!javaRuntime.isBlank()) return resolveConfiguredCommand(javaRuntime);
        String executableName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win") ? "java.exe" : "java";
        Path bundled = Path.of(System.getProperty("java.home"), "bin", executableName);
        if (Files.isExecutable(bundled)) return bundled.toString();
        String systemJava = findOnPath("java");
        if (systemJava != null) return systemJava;
        throw new IOException("找不到 Java 运行时");
    }

    private String resolveConfiguredCommand(String command) throws IOException {
        Path path = Path.of(command);
        if (path.isAbsolute() || command.contains("\\") || command.contains("/")) {
            if (Files.isExecutable(path)) return path.toAbsolutePath().toString();
            throw new IOException("配置的可执行文件不存在: " + command);
        }
        String found = findOnPath(command);
        if (found == null) throw new IOException("PATH 中找不到可执行文件: " + command);
        return found;
    }

    private String findOnPath(String command) {
        String pathValue = System.getenv("PATH");
        if (pathValue == null) return null;
        String[] extensions = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")
                ? System.getenv().getOrDefault("PATHEXT", ".COM;.EXE;.BAT;.CMD").toLowerCase(Locale.ROOT).split(";")
                : new String[]{""};
        for (String directory : pathValue.split(java.util.regex.Pattern.quote(System.getProperty("path.separator")))) {
            for (String extension : extensions) {
                Path candidate = Path.of(directory).resolve(command + (command.contains(".") ? "" : extension));
                if (candidate.toString().toLowerCase(Locale.ROOT).contains("\\windowsapps\\")) continue;
                if (Files.isRegularFile(candidate) && Files.isExecutable(candidate)) return candidate.toAbsolutePath().toString();
            }
        }
        return null;
    }

    private void deleteDirectory(Path directory) {
        try (var paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // Best-effort cleanup of the private temporary source directory.
                }
            });
        } catch (IOException ignored) {
            // Best-effort cleanup of the private temporary source directory.
        }
    }

    private record ProcessResult(int exitCode, boolean timedOut, boolean memoryExceeded, boolean resourceMeasurementFailed,
                                String stdout, String stderr,
                                String elapsedSeconds, long peakMemoryBytes) {
    }
}
