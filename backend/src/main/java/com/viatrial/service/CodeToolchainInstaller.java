package com.viatrial.service;

import com.viatrial.config.DataDirectoryResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** Installs pinned, official Windows toolchain bundles into ViaTrial's data directory on first use. */
@Component
public class CodeToolchainInstaller {

    private static final String ZIG_VERSION = "0.17.0";
    private static final String ZIG_URL = "https://ziglang.org/download/0.17.0/zig-x86_64-windows-0.17.0.zip";
    private static final String ZIG_SHA256 = "b5663f69581dcf391293fbf16c06cb80d81d806545ce618b4d0bab7f0eb8c428";
    private static final String PYTHON_VERSION = "3.14.8";
    private static final String PYTHON_URL = "https://www.python.org/ftp/python/3.14.8/python-3.14.8-embed-amd64.zip";
    private static final String PYTHON_SHA256 = "a93abe456ab01bd96d7a085b3cdb6566b3063f4241360d114142fbdb07f0a310";
    private static final long MAX_ARCHIVE_BYTES = 150L * 1024 * 1024;
    private static final long MAX_EXPANDED_BYTES = 500L * 1024 * 1024;

    private final Path toolchainDirectory;

    public CodeToolchainInstaller(DataDirectoryResolver dataDirectoryResolver) {
        this.toolchainDirectory = dataDirectoryResolver.getDataDirectory().resolve("toolchains");
    }

    public Path zigExecutable() throws IOException, InterruptedException {
        requireWindowsX64();
        Path root = installArchive("zig-" + ZIG_VERSION, ZIG_URL, ZIG_SHA256);
        return root.resolve("zig-x86_64-windows-" + ZIG_VERSION).resolve("zig.exe");
    }

    public Path pythonExecutable() throws IOException, InterruptedException {
        requireWindowsX64();
        Path root = installArchive("python-" + PYTHON_VERSION, PYTHON_URL, PYTHON_SHA256);
        return root.resolve("python.exe");
    }

    private synchronized Path installArchive(String name, String url, String expectedSha256)
            throws IOException, InterruptedException {
        Path installed = toolchainDirectory.resolve(name);
        if (Files.isDirectory(installed)) return installed;

        Files.createDirectories(toolchainDirectory);
        Path archive = Files.createTempFile(toolchainDirectory, name, ".zip.part");
        Path extraction = Files.createTempDirectory(toolchainDirectory, name + ".extract-");
        try {
            var request = java.net.http.HttpRequest.newBuilder(java.net.URI.create(url))
                    .timeout(java.time.Duration.ofMinutes(5))
                    .GET()
                    .build();
            var response = java.net.http.HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(20))
                    .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
                    .build()
                    .send(request, java.net.http.HttpResponse.BodyHandlers.ofFile(archive));
            if (response.statusCode() != 200 || Files.size(archive) > MAX_ARCHIVE_BYTES) {
                throw new IOException("工具链下载失败（HTTP " + response.statusCode() + "）: " + url);
            }
            String actualSha256 = sha256(archive);
            if (!expectedSha256.equalsIgnoreCase(actualSha256)) {
                throw new IOException("工具链文件校验失败，请删除 " + archive + " 后重试");
            }
            extractZip(archive, extraction);
            Files.move(extraction, installed, StandardCopyOption.ATOMIC_MOVE);
            return installed;
        } catch (java.nio.file.AtomicMoveNotSupportedException exception) {
            Files.move(extraction, installed);
            return installed;
        } finally {
            Files.deleteIfExists(archive);
            deleteDirectory(extraction);
        }
    }

    private void extractZip(Path archive, Path destination) throws IOException {
        long expanded = 0;
        try (InputStream input = Files.newInputStream(archive); ZipInputStream zip = new ZipInputStream(input)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                Path output = destination.resolve(entry.getName()).normalize();
                if (!output.startsWith(destination)) throw new IOException("工具链压缩包包含无效路径");
                if (entry.isDirectory()) {
                    Files.createDirectories(output);
                } else {
                    Files.createDirectories(output.getParent());
                    try (var file = Files.newOutputStream(output)) {
                        byte[] buffer = new byte[8192];
                        int count;
                        while ((count = zip.read(buffer)) != -1) {
                            expanded += count;
                            if (expanded > MAX_EXPANDED_BYTES) throw new IOException("工具链压缩包解压体积超出限制");
                            file.write(buffer, 0, count);
                        }
                    }
                }
                zip.closeEntry();
            }
        }
    }

    private String sha256(Path file) throws IOException {
        try (InputStream input = Files.newInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[64 * 1024];
            int count;
            while ((count = input.read(buffer)) != -1) digest.update(buffer, 0, count);
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private void requireWindowsX64() throws IOException {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String architecture = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        if (!os.contains("win") || !(architecture.equals("amd64") || architecture.equals("x86_64"))) {
            throw new IOException("自动安装的本地工具链目前支持 Windows x64；请配置本机编译器");
        }
    }

    private void deleteDirectory(Path directory) {
        if (!Files.exists(directory)) return;
        try (var paths = Files.walk(directory)) {
            paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // Best-effort cleanup of a failed or completed extraction staging directory.
                }
            });
        } catch (IOException ignored) {
            // Best-effort cleanup of a failed or completed extraction staging directory.
        }
    }
}
