package com.fablix.util;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class CsvDataImporter {
    private static final int BATCH_SIZE = 5000;
    private static final int LOG_LIMIT_PER_FILE = 100;

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: CsvDataImporter <csv-dir-or-tar.gz>");
            System.exit(1);
        }

        long startNanos = System.nanoTime();
        Path input = Paths.get(args[0]);
        Path csvDir = null;
        Path tempDir = null;

        if (!Files.exists(input)) {
            System.err.println("Input path does not exist: " + input);
            System.exit(1);
        }

        try {
            if (isTarGz(input)) {
                tempDir = Files.createTempDirectory("csv-import-");
                extractTarGz(input, tempDir);
                csvDir = detectCsvRoot(tempDir);
            } else {
                csvDir = input;
            }

            runImport(csvDir);
        } finally {
            if (tempDir != null) {
                deleteRecursively(tempDir);
            }
        }

        long seconds = (System.nanoTime() - startNanos) / 1_000_000_000L;
        System.out.println("Import finished in " + seconds + "s");
    }

    private static void runImport(Path csvDir) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");

        String optimizedUrl = withUrlParams(DbConfig.URL,
                "rewriteBatchedStatements=true",
                "cachePrepStmts=true",
                "useServerPrepStmts=true");

        try (Connection conn = DriverManager.getConnection(optimizedUrl, DbConfig.USER, DbConfig.PASSWORD)) {
            conn.setAutoCommit(false);

            Set<String> movieIds = loadStringSet(conn, "SELECT id FROM movies");
            Set<String> starIds = loadStringSet(conn, "SELECT id FROM stars");
            Set<Integer> genreIds = loadIntSet(conn, "SELECT id FROM genres");
            Set<String> simPairs = loadPairSet(conn, "SELECT starId, movieId FROM stars_in_movies");
            Set<String> gimPairs = loadPairSet(conn, "SELECT genreId, movieId FROM genres_in_movies");
            Set<String> ratingsMovieIds = loadStringSet(conn, "SELECT movieId FROM ratings");

            System.out.println("Preloaded existing rows: movies=" + movieIds.size()
                    + ", stars=" + starIds.size()
                    + ", genres=" + genreIds.size()
                    + ", stars_in_movies=" + simPairs.size()
                    + ", genres_in_movies=" + gimPairs.size()
                    + ", ratings=" + ratingsMovieIds.size());

            importMovies(conn, resolveCsv(csvDir, "movies.csv"), movieIds);
            importStars(conn, resolveCsv(csvDir, "stars.csv"), starIds);
            importGenres(conn, resolveCsv(csvDir, "genres.csv"), genreIds);
            importStarsInMovies(conn, resolveCsv(csvDir, "stars_in_movies.csv"), starIds, movieIds, simPairs);
            importGenresInMovies(conn, resolveCsv(csvDir, "genres_in_movies.csv"), genreIds, movieIds, gimPairs);
            Path ratingsPath = resolveOptionalCsv(csvDir, "ratings.csv");
            if (ratingsPath != null) {
                importRatings(conn, ratingsPath, movieIds, ratingsMovieIds);
            } else {
                System.out.println("ratings.csv not found. Skipping ratings import.");
            }
        }
    }

    private static void importMovies(Connection conn, Path csv, Set<String> movieIds) throws Exception {
        String sql = "INSERT INTO movies(id, title, year, director) VALUES (?, ?, ?, ?)";
        int inserted = 0;
        int skipped = 0;
        int bad = 0;
        int logged = 0;

        long t0 = System.nanoTime();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             CSVParser parser = openParser(csv)) {
            for (CSVRecord r : parser) {
                String id = trimToNull(r.get("id"));
                String title = trimToNull(r.get("title"));
                String director = trimToNull(r.get("director"));
                Integer year = parseIntOrNull(trimToNull(r.get("year")));

                if (id == null || title == null || director == null || year == null) {
                    bad++;
                    if (logged++ < LOG_LIMIT_PER_FILE) {
                        System.out.println("[movies] bad row " + r.getRecordNumber()
                                + ": required field missing/invalid (id/title/year/director)");
                    }
                    continue;
                }

                if (!movieIds.add(id)) {
                    skipped++;
                    continue;
                }

                stmt.setString(1, id);
                stmt.setString(2, title);
                stmt.setInt(3, year);
                stmt.setString(4, director);
                stmt.addBatch();
                inserted++;

                if (inserted % BATCH_SIZE == 0) {
                    stmt.executeBatch();
                    conn.commit();
                }
            }
            stmt.executeBatch();
            conn.commit();
        }
        long ms = (System.nanoTime() - t0) / 1_000_000L;
        System.out.println("movies: inserted=" + inserted + ", skipped-duplicate=" + skipped + ", bad=" + bad + ", timeMs=" + ms);
    }

    private static void importStars(Connection conn, Path csv, Set<String> starIds) throws Exception {
        String sql = "INSERT INTO stars(id, name, birthYear) VALUES (?, ?, ?)";
        int inserted = 0;
        int skipped = 0;
        int bad = 0;
        int logged = 0;

        long t0 = System.nanoTime();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             CSVParser parser = openParser(csv)) {
            for (CSVRecord r : parser) {
                String id = trimToNull(r.get("id"));
                String name = trimToNull(r.get("name"));
                Integer birthYear = parseIntOrNull(trimToNull(r.get("birthYear")));

                if (id == null || name == null) {
                    bad++;
                    if (logged++ < LOG_LIMIT_PER_FILE) {
                        System.out.println("[stars] bad row " + r.getRecordNumber()
                                + ": required field missing/invalid (id/name)");
                    }
                    continue;
                }

                if (!starIds.add(id)) {
                    skipped++;
                    continue;
                }

                stmt.setString(1, id);
                stmt.setString(2, name);
                if (birthYear == null) {
                    stmt.setNull(3, java.sql.Types.INTEGER);
                } else {
                    stmt.setInt(3, birthYear);
                }
                stmt.addBatch();
                inserted++;

                if (inserted % BATCH_SIZE == 0) {
                    stmt.executeBatch();
                    conn.commit();
                }
            }
            stmt.executeBatch();
            conn.commit();
        }
        long ms = (System.nanoTime() - t0) / 1_000_000L;
        System.out.println("stars: inserted=" + inserted + ", skipped-duplicate=" + skipped + ", bad=" + bad + ", timeMs=" + ms);
    }

    private static void importGenres(Connection conn, Path csv, Set<Integer> genreIds) throws Exception {
        String sql = "INSERT INTO genres(id, name) VALUES (?, ?)";
        int inserted = 0;
        int skipped = 0;
        int bad = 0;
        int logged = 0;

        long t0 = System.nanoTime();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             CSVParser parser = openParser(csv)) {
            for (CSVRecord r : parser) {
                Integer id = parseIntOrNull(trimToNull(r.get("id")));
                String name = trimToNull(r.get("name"));

                if (id == null || name == null) {
                    bad++;
                    if (logged++ < LOG_LIMIT_PER_FILE) {
                        System.out.println("[genres] bad row " + r.getRecordNumber()
                                + ": required field missing/invalid (id/name)");
                    }
                    continue;
                }

                if (!genreIds.add(id)) {
                    skipped++;
                    continue;
                }

                stmt.setInt(1, id);
                stmt.setString(2, name);
                stmt.addBatch();
                inserted++;

                if (inserted % BATCH_SIZE == 0) {
                    stmt.executeBatch();
                    conn.commit();
                }
            }
            stmt.executeBatch();
            conn.commit();
        }
        long ms = (System.nanoTime() - t0) / 1_000_000L;
        System.out.println("genres: inserted=" + inserted + ", skipped-duplicate=" + skipped + ", bad=" + bad + ", timeMs=" + ms);
    }

    private static void importStarsInMovies(
            Connection conn, Path csv, Set<String> starIds, Set<String> movieIds, Set<String> pairSet) throws Exception {
        String sql = "INSERT INTO stars_in_movies(starId, movieId) VALUES (?, ?)";
        int inserted = 0;
        int skipped = 0;
        int bad = 0;
        int logged = 0;

        long t0 = System.nanoTime();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             CSVParser parser = openParser(csv)) {
            for (CSVRecord r : parser) {
                String starId = trimToNull(r.get("starId"));
                String movieId = trimToNull(r.get("movieId"));

                if (starId == null || movieId == null) {
                    bad++;
                    if (logged++ < LOG_LIMIT_PER_FILE) {
                        System.out.println("[stars_in_movies] bad row " + r.getRecordNumber()
                                + ": required field missing (starId/movieId)");
                    }
                    continue;
                }
                if (!starIds.contains(starId) || !movieIds.contains(movieId)) {
                    bad++;
                    if (logged++ < LOG_LIMIT_PER_FILE) {
                        System.out.println("[stars_in_movies] bad row " + r.getRecordNumber()
                                + ": foreign key missing (starId=" + starId + ", movieId=" + movieId + ")");
                    }
                    continue;
                }

                String key = starId + "|" + movieId;
                if (!pairSet.add(key)) {
                    skipped++;
                    continue;
                }

                stmt.setString(1, starId);
                stmt.setString(2, movieId);
                stmt.addBatch();
                inserted++;

                if (inserted % BATCH_SIZE == 0) {
                    stmt.executeBatch();
                    conn.commit();
                }
            }
            stmt.executeBatch();
            conn.commit();
        }
        long ms = (System.nanoTime() - t0) / 1_000_000L;
        System.out.println("stars_in_movies: inserted=" + inserted + ", skipped-duplicate=" + skipped + ", bad=" + bad + ", timeMs=" + ms);
    }

    private static void importGenresInMovies(
            Connection conn, Path csv, Set<Integer> genreIds, Set<String> movieIds, Set<String> pairSet) throws Exception {
        String sql = "INSERT INTO genres_in_movies(genreId, movieId) VALUES (?, ?)";
        int inserted = 0;
        int skipped = 0;
        int bad = 0;
        int logged = 0;

        long t0 = System.nanoTime();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             CSVParser parser = openParser(csv)) {
            for (CSVRecord r : parser) {
                Integer genreId = parseIntOrNull(trimToNull(r.get("genreId")));
                String movieId = trimToNull(r.get("movieId"));

                if (genreId == null || movieId == null) {
                    bad++;
                    if (logged++ < LOG_LIMIT_PER_FILE) {
                        System.out.println("[genres_in_movies] bad row " + r.getRecordNumber()
                                + ": required field missing/invalid (genreId/movieId)");
                    }
                    continue;
                }
                if (!genreIds.contains(genreId) || !movieIds.contains(movieId)) {
                    bad++;
                    if (logged++ < LOG_LIMIT_PER_FILE) {
                        System.out.println("[genres_in_movies] bad row " + r.getRecordNumber()
                                + ": foreign key missing (genreId=" + genreId + ", movieId=" + movieId + ")");
                    }
                    continue;
                }

                String key = genreId + "|" + movieId;
                if (!pairSet.add(key)) {
                    skipped++;
                    continue;
                }

                stmt.setInt(1, genreId);
                stmt.setString(2, movieId);
                stmt.addBatch();
                inserted++;

                if (inserted % BATCH_SIZE == 0) {
                    stmt.executeBatch();
                    conn.commit();
                }
            }
            stmt.executeBatch();
            conn.commit();
        }
        long ms = (System.nanoTime() - t0) / 1_000_000L;
        System.out.println("genres_in_movies: inserted=" + inserted + ", skipped-duplicate=" + skipped + ", bad=" + bad + ", timeMs=" + ms);
    }

    private static void importRatings(Connection conn, Path csv, Set<String> movieIds, Set<String> existingRatingsMovieIds)
            throws Exception {
        String sql = "INSERT INTO ratings(movieId, rating, numVotes) VALUES (?, ?, ?)";
        int inserted = 0;
        int skipped = 0;
        int bad = 0;
        int logged = 0;

        long t0 = System.nanoTime();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             CSVParser parser = openParser(csv)) {
            for (CSVRecord r : parser) {
                String movieId = trimToNull(r.get("movieId"));
                Double rating = parseDoubleOrNull(trimToNull(r.get("rating")));
                Integer numVotes = parseIntOrNull(trimToNull(r.get("numVotes")));

                if (movieId == null || rating == null || numVotes == null) {
                    bad++;
                    if (logged++ < LOG_LIMIT_PER_FILE) {
                        System.out.println("[ratings] bad row " + r.getRecordNumber()
                                + ": required field missing/invalid (movieId/rating/numVotes)");
                    }
                    continue;
                }
                if (!movieIds.contains(movieId)) {
                    bad++;
                    if (logged++ < LOG_LIMIT_PER_FILE) {
                        System.out.println("[ratings] bad row " + r.getRecordNumber()
                                + ": foreign key missing (movieId=" + movieId + ")");
                    }
                    continue;
                }
                if (!existingRatingsMovieIds.add(movieId)) {
                    skipped++;
                    continue;
                }

                stmt.setString(1, movieId);
                stmt.setDouble(2, rating);
                stmt.setInt(3, numVotes);
                stmt.addBatch();
                inserted++;

                if (inserted % BATCH_SIZE == 0) {
                    stmt.executeBatch();
                    conn.commit();
                }
            }
            stmt.executeBatch();
            conn.commit();
        }
        long ms = (System.nanoTime() - t0) / 1_000_000L;
        System.out.println("ratings: inserted=" + inserted + ", skipped-duplicate=" + skipped + ", bad=" + bad + ", timeMs=" + ms);
    }

    private static CSVParser openParser(Path csvPath) throws IOException {
        Reader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8);
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreSurroundingSpaces(true)
                .setTrim(true)
                .build();
        return new CSVParser(reader, format);
    }

    private static Set<String> loadStringSet(Connection conn, String sql) throws SQLException {
        Set<String> set = new HashSet<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String v = rs.getString(1);
                if (v != null) {
                    set.add(v);
                }
            }
        }
        return set;
    }

    private static Set<Integer> loadIntSet(Connection conn, String sql) throws SQLException {
        Set<Integer> set = new HashSet<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int v = rs.getInt(1);
                if (!rs.wasNull()) {
                    set.add(v);
                }
            }
        }
        return set;
    }

    private static Set<String> loadPairSet(Connection conn, String sql) throws SQLException {
        Set<String> set = new HashSet<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String left = rs.getString(1);
                String right = rs.getString(2);
                if (left != null && right != null) {
                    set.add(left + "|" + right);
                }
            }
        }
        return set;
    }

    private static Integer parseIntOrNull(String s) {
        if (s == null) {
            return null;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double parseDoubleOrNull(String s) {
        if (s == null) {
            return null;
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static Path resolveCsv(Path dir, String fileName) throws IOException {
        Path p = resolveOptionalCsv(dir, fileName);
        if (p == null) {
            throw new IOException("Missing required CSV: " + fileName + " in " + dir);
        }
        return p;
    }

    private static Path resolveOptionalCsv(Path dir, String fileName) throws IOException {
        try (var stream = Files.walk(dir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().equalsIgnoreCase(fileName))
                    .findFirst()
                    .orElse(null);
        }
    }

    private static boolean isTarGz(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".tar.gz") || name.endsWith(".tgz");
    }

    private static void extractTarGz(Path tarGz, Path destination) throws IOException {
        try (InputStream fileIn = Files.newInputStream(tarGz);
             InputStream gzipIn = new GzipCompressorInputStream(fileIn);
             TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {

            TarArchiveEntry entry;
            while ((entry = tarIn.getNextTarEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String name = entry.getName();
                if (name.contains("..")) {
                    continue;
                }
                if (name.contains("/._") || name.startsWith("._")) {
                    continue;
                }
                Path output = destination.resolve(name).normalize();
                if (!output.startsWith(destination)) {
                    continue;
                }
                Files.createDirectories(output.getParent());
                Files.copy(tarIn, output, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private static Path detectCsvRoot(Path extractedRoot) throws IOException {
        try (var stream = Files.walk(extractedRoot, 2)) {
            Path movies = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().equalsIgnoreCase("movies.csv"))
                    .findFirst()
                    .orElse(null);
            if (movies != null) {
                return movies.getParent();
            }
        }
        return extractedRoot;
    }

    private static String withUrlParams(String baseUrl, String... params) {
        StringBuilder sb = new StringBuilder(baseUrl);
        boolean hasQuery = baseUrl.contains("?");
        for (String p : params) {
            if (p == null || p.isBlank()) {
                continue;
            }
            sb.append(hasQuery ? "&" : "?").append(p);
            hasQuery = true;
        }
        return sb.toString();
    }

    private static void deleteRecursively(Path root) {
        try (var walk = Files.walk(root)) {
            walk.sorted((a, b) -> b.getNameCount() - a.getNameCount()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // best effort temp cleanup
                }
            });
        } catch (IOException ignored) {
            // best effort temp cleanup
        }
    }
}
