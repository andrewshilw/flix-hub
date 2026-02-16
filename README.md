# CS 122B Winter 2026 Projects

## Project 1
- Demo video: https://drive.google.com/file/d/1Xu-MGIgYv1Lb4EyGZ_3DIY7em0WsBUcZ/view?usp=sharing
- Member: Andrew Shi (full contribution)

## Project 2
- Demo video: https://drive.google.com/file/d/1oZt3azQpJqjajToQRAIQBIDSFnS2SACq/view?usp=sharing
- Member: Andrew Shi (full contribution)

### Project 2 Notes

#### LIKE / ILIKE usage
- Search (Main Page -> Movie List): `MovieListServlet` uses `LIKE` with `%keyword%` for substring matching on `m.title`, `m.director`, and `s.name`.
- Browse by title (A-Z / 0-9): `MovieListServlet` uses `LIKE` with `prefix%` (case-insensitive via `UPPER(m.title)`) to match titles that start with the selected character.
- Year uses exact match (`m.year = ?`) and does not use substring matching.

## Project 3
- Demo video: TBA
- Member: Andrew Shi (full contribution)

### Task 6: CSV Data Import

#### Data Loading Program
- Class: `src/com/fablix/util/CsvDataImporter.java`
- Input supported:
  - Extracted directory containing CSV files
  - `.tar.gz` package directly (for example `csv_package-1.tar.gz`)
- CSV files expected:
  - `movies.csv`
  - `stars.csv`
  - `genres.csv`
  - `stars_in_movies.csv`
  - `genres_in_movies.csv`
  - `ratings.csv` (optional; importer skips if missing)

#### Run
```bash
mvn -q -DskipTests compile
java -cp target/classes;target/cs122b-project3/WEB-INF/lib/* com.fablix.util.CsvDataImporter "C:\Users\shian\Downloads\csv_package-1.tar.gz"
```

PowerShell equivalent:
```powershell
mvn -q -DskipTests compile
java -cp "target/classes;target/cs122b-project3/WEB-INF/lib/*" com.fablix.util.CsvDataImporter "C:\Users\shian\Downloads\csv_package-1.tar.gz"
```

#### Cleaning / Validation Behavior
- Missing required values are reported and skipped.
- Invalid numeric values are treated as `NULL` where nullable; otherwise row is reported and skipped.
- Duplicate rows are tracked and inserted once.
- Inconsistent relationship rows (missing foreign keys) are reported and skipped.
- Import continues after bad rows (does not crash the run).

#### Optimization Report
The importer implements the following optimizations beyond just using `PreparedStatement` and autocommit control:

1. Preloaded keysets for existence checks
- Technique:
  - Loads existing IDs/pairs into `HashSet`s before import:
    - `movies.id`, `stars.id`, `genres.id`
    - `(starId,movieId)` and `(genreId,movieId)` link pairs
    - existing `ratings.movieId`
  - Rejects duplicates and FK-invalid rows in memory before DB insert.
- Impact:
  - Avoids per-row `SELECT` existence checks.
  - Reduces failed insert attempts and DB round-trips.

2. Driver-level batch rewrite + large batch flushes
- Technique:
  - Uses JDBC URL flags: `rewriteBatchedStatements=true`, `cachePrepStmts=true`, `useServerPrepStmts=true`.
  - Inserts in large batches (`BATCH_SIZE=5000`) and commits per batch.
- Impact:
  - Collapses many insert statements into fewer wire calls.
  - Significantly reduces network and JDBC overhead on large files.