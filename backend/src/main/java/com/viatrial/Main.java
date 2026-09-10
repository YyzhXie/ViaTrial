package com.viatrial;

import com.viatrial.config.DataDirectoryResolver;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@MapperScan("com.viatrial.mapper")
@ConfigurationPropertiesScan
@SpringBootApplication
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Main.class, args);
        logStartupSummary(context);
    }

    private static void logStartupSummary(ConfigurableApplicationContext context) {
        Environment environment = context.getEnvironment();
        String address = environment.getProperty("server.address", "0.0.0.0");
        String port = environment.getProperty("server.port", "8080");
        String dataDirectory = context.getBean(DataDirectoryResolver.class).getDataDirectory().toString();

        log.info("ViaTrial started. Listening on {}:{}, data directory: {}", address, port, dataDirectory);

        if (!isLoopbackAddress(address)) {
            log.warn("""
                    Server is bound to {} and is therefore reachable from the local network. \
                    ViaTrial has no login system: state-changing requests require the write token stored in \
                    {}/.write-token (or viatrial.security.write-token). Keep that file private, and prefer \
                    server.address=127.0.0.1 to restrict access to this machine.""", address, dataDirectory);
        }
    }

    private static boolean isLoopbackAddress(String address) {
        return "127.0.0.1".equals(address)
                || "localhost".equalsIgnoreCase(address)
                || "::1".equals(address)
                || "[::1]".equals(address);
    }
}
