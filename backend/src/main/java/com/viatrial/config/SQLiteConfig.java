package com.viatrial.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class SQLiteConfig {

    @PostConstruct
    public void initDataDirectory() throws IOException {
        Files.createDirectories(Path.of(DatabaseConfig.DATA_DIR));
    }
}
