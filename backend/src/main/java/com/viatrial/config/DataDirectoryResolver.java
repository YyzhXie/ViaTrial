package com.viatrial.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Set;

/**
 * 解析并创建数据目录，同时托管访问令牌。
 *
 * <p>审计项 D-2：数据库路径原先依赖进程工作目录（CWD），从仓库根启动落在
 * {@code <repo>/data/viatrial.db}，从 {@code backend/} 启动落在
 * {@code <repo>/backend/data/viatrial.db}，磁盘上因此出现过两个库。此处统一解析为
 * <b>绝对路径</b>并作为数据库 URL 的唯一来源，所有启动方式（jar、IDE、
 * {@code mvn spring-boot:run}）都命中同一个文件。</p>
 *
 * <p>目录取值遵循 Spring 配置优先级（{@code viatrial.data-dir}），因此测试可以通过
 * 属性覆盖把数据写入 {@code target/} 下的隔离目录（见 {@code TestDataDirectoryInitializer}）。</p>
 */
@Component
public class DataDirectoryResolver {

    public static final String DATA_DIRECTORY_PROPERTY = "viatrial.data-dir";
    public static final String DATA_DIRECTORY_KEY = "app.data-dir";

    private static final String DEFAULT_DATA_DIRECTORY = "data";
    private static final String TOKEN_FILE_NAME = ".write-token";

    private static final Logger log = LoggerFactory.getLogger(DataDirectoryResolver.class);

    private final SecurityProperties securityProperties;

    private final SecureRandom secureRandom = new SecureRandom();

    private Path dataDirectory;

    private Path writeTokenFile;

    private String writeToken = "";

    public DataDirectoryResolver(Environment environment, SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;

        String configured = environment.getProperty(DATA_DIRECTORY_PROPERTY, DEFAULT_DATA_DIRECTORY);
        this.dataDirectory = Path.of(configured).toAbsolutePath().normalize();
    }

    @PostConstruct
    void initialize() {
        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create data directory: " + dataDirectory, e);
        }

        this.writeTokenFile = dataDirectory.resolve(TOKEN_FILE_NAME);
        this.writeToken = resolveWriteToken();

        log.info("ViaTrial data directory resolved to {}", dataDirectory);

        if (isTestDataDirectory()) {
            log.info("Running against an isolated test data directory; the production database is untouched.");
        }
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public String getDatabaseUrl() {
        return "jdbc:sqlite:" + dataDirectory.resolve("viatrial.db");
    }

    public Path getWriteTokenFile() {
        return writeTokenFile;
    }

    public String getWriteToken() {
        return writeToken;
    }

    private boolean isTestDataDirectory() {
        return dataDirectory.toString().contains("test-data");
    }

    private String resolveWriteToken() {
        String configured = securityProperties.getWriteToken();
        if (!configured.isEmpty()) {
            log.info("API write token loaded from configuration ({}).", "viatrial.security.write-token");
            return configured;
        }

        try {
            if (Files.isRegularFile(writeTokenFile)) {
                String stored = Files.readString(writeTokenFile, StandardCharsets.UTF_8).trim();
                if (!stored.isEmpty()) {
                    return stored;
                }
            }

            String generated = generateToken();
            Files.writeString(writeTokenFile, generated + System.lineSeparator(), StandardCharsets.UTF_8);
            restrictToOwner(writeTokenFile);
            log.info("Generated a new API write token at {}", writeTokenFile);
            return generated;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read or create API write token file: " + writeTokenFile, e);
        }
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private void restrictToOwner(Path file) {
        try {
            Files.setPosixFilePermissions(file, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
        } catch (IOException | UnsupportedOperationException e) {
            // Windows 等非 POSIX 文件系统不支持 POSIX 权限，目录本身已由操作系统用户隔离。
            log.debug("POSIX file permissions are not available for {}; skipped.", file);
        }
    }
}
