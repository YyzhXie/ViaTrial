package com.viatrial.config;

public class DatabaseConfig {

    private DatabaseConfig() {

    }

    public static final String DATA_DIR = "data";
    public static final String DATABASE_FILE = "viatrial.db";
    public static final String DATABASE_URL = "jdbc:sqlite:./" + DATA_DIR + "/" + DATABASE_FILE;
}
