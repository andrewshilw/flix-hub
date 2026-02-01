package com.fablix.util;

public final class DbConfig {
    public static final String USER = "root";
    public static final String PASSWORD = "Tghdfj123!";
    public static final String URL =
            "jdbc:mysql://localhost:3306/moviedb" +
                    "?useSSL=false" +
                    "&allowPublicKeyRetrieval=true" +
                    "&serverTimezone=UTC";

    private DbConfig() {
        // Utility class
    }
}
