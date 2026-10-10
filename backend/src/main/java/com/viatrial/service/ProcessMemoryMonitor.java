package com.viatrial.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

final class ProcessMemoryMonitor {

    private static final long SAMPLE_TIMEOUT_MS = 300;

    private ProcessMemoryMonitor() {
    }

    static long sampleTreeBytes(Process process) {
        Set<Long> processIds = new HashSet<>();
        processIds.add(process.pid());
        try {
            process.toHandle().descendants().forEach(handle -> processIds.add(handle.pid()));
        } catch (UnsupportedOperationException ignored) {
            // The root process can still be measured on platforms without descendant support.
        }
        if (isWindows()) return sampleWindows(processIds);
        if (Files.isDirectory(Path.of("/proc"))) return sampleProcfs(processIds);
        return samplePs(processIds);
    }

    private static long sampleProcfs(Set<Long> processIds) {
        long total = 0;
        for (long pid : processIds) {
            Path status = Path.of("/proc", Long.toString(pid), "status");
            try {
                for (String line : Files.readAllLines(status, StandardCharsets.UTF_8)) {
                    if (line.startsWith("VmRSS:")) {
                        String[] parts = line.trim().split("\\s+");
                        if (parts.length >= 2) total += Long.parseLong(parts[1]) * 1024;
                        break;
                    }
                }
            } catch (IOException | NumberFormatException ignored) {
                // A process may exit between obtaining its handle and reading its status.
            }
        }
        return total;
    }

    private static long sampleWindows(Set<Long> processIds) {
        String systemRoot = System.getenv().getOrDefault("SystemRoot", "C:\\Windows");
        Path tasklist = Path.of(systemRoot, "System32", "tasklist.exe");
        try {
            Process sampler = new ProcessBuilder(tasklist.toString(), "/FO", "CSV", "/NH")
                    .redirectErrorStream(true).start();
            CompletableFuture<List<String>> lines = CompletableFuture.supplyAsync(() -> {
                try (BufferedReader reader = sampler.inputReader(StandardCharsets.UTF_8)) {
                    List<String> output = new ArrayList<>();
                    String line;
                    while ((line = reader.readLine()) != null) output.add(line);
                    return output;
                } catch (IOException exception) {
                    return List.of();
                }
            });
            if (!sampler.waitFor(SAMPLE_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                sampler.destroyForcibly();
                return -1;
            }
            long total = 0;
            for (String line : lines.get(SAMPLE_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                List<String> fields = parseCsv(line);
                if (fields.size() < 5) continue;
                try {
                    long pid = Long.parseLong(fields.get(1));
                    if (!processIds.contains(pid)) continue;
                    String digits = fields.get(4).replaceAll("[^0-9]", "");
                    if (!digits.isEmpty()) total += Long.parseLong(digits) * 1024;
                } catch (NumberFormatException ignored) {
                    // Ignore localized or unavailable tasklist memory values.
                }
            }
            return total;
        } catch (IOException | InterruptedException | java.util.concurrent.ExecutionException
                 | java.util.concurrent.TimeoutException exception) {
            if (exception instanceof InterruptedException) Thread.currentThread().interrupt();
            return -1;
        }
    }

    private static long samplePs(Set<Long> processIds) {
        if (processIds.isEmpty()) return 0;
        List<String> command = new ArrayList<>(List.of("ps", "-o", "rss=", "-p"));
        command.add(String.join(",", processIds.stream().map(String::valueOf).toList()));
        try {
            Process sampler = new ProcessBuilder(command).redirectErrorStream(true).start();
            CompletableFuture<List<String>> lines = CompletableFuture.supplyAsync(() -> {
                try (BufferedReader reader = sampler.inputReader(StandardCharsets.UTF_8)) {
                    return reader.lines().toList();
                } catch (IOException exception) {
                    return List.of();
                }
            });
            if (!sampler.waitFor(SAMPLE_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                sampler.destroyForcibly();
                return -1;
            }
            long total = 0;
            for (String line : lines.get(SAMPLE_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                try {
                    total += Long.parseLong(line.trim()) * 1024;
                } catch (NumberFormatException ignored) {
                    // Ignore ps header or a process which disappeared during sampling.
                }
            }
            return total;
        } catch (IOException | InterruptedException | java.util.concurrent.ExecutionException
                 | java.util.concurrent.TimeoutException exception) {
            if (exception instanceof InterruptedException) Thread.currentThread().interrupt();
            return -1;
        }
    }

    private static List<String> parseCsv(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (character == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    value.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == ',' && !quoted) {
                values.add(value.toString());
                value.setLength(0);
            } else {
                value.append(character);
            }
        }
        values.add(value.toString());
        return values;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
