package com.fablix.util;

import jakarta.servlet.ServletContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public final class SearchTimingLogger {
    private static final String LOG_FILE_NAME = "search-timing.log";
    private static final Object LOCK = new Object();

    private SearchTimingLogger() {
    }

    public static void logSample(ServletContext servletContext, long servletTimeNs, long jdbcTimeNs) {
        Path logPath = resolveLogPath(servletContext);
        String line = "TS=" + servletTimeNs + ",TJ=" + jdbcTimeNs + System.lineSeparator();

        synchronized (LOCK) {
            try {
                Path parent = logPath.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                Files.writeString(
                        logPath,
                        line,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                );
            } catch (IOException e) {
                servletContext.log("Failed to write search timing sample to " + logPath, e);
            }
        }
    }

    private static Path resolveLogPath(ServletContext servletContext) {
        String configuredPath = System.getProperty("fabflix.searchTimingLog");
        if (configuredPath != null && !configuredPath.isBlank()) {
            return Paths.get(configuredPath);
        }

        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.isBlank()) {
            return Paths.get(catalinaBase, "logs", LOG_FILE_NAME);
        }

        String realPath = servletContext.getRealPath("/WEB-INF/" + LOG_FILE_NAME);
        if (realPath != null && !realPath.isBlank()) {
            return Paths.get(realPath);
        }

        Object tempDir = servletContext.getAttribute(ServletContext.TEMPDIR);
        if (tempDir instanceof java.io.File) {
            return ((java.io.File) tempDir).toPath().resolve(LOG_FILE_NAME);
        }

        return Paths.get(LOG_FILE_NAME);
    }
}
