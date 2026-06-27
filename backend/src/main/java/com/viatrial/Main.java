package com.viatrial;

import com.viatrial.database.DatabaseInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        DatabaseInitializer.initialize();
        System.out.println("ViaTrial database Initialized successfully.");

        SpringApplication.run(Main.class, args);
    }
}
