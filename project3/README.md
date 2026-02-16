# Project 3

## Task 6: CSV Data Import

### Data Loading Program
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

### Run
```bash
mvn -q -DskipTests compile
java -cp target/classes;target/cs122b-project3/WEB-INF/lib/* com.fablix.util.CsvDataImporter "C:\Users\shian\Downloads\csv_package-1.tar.gz"
```

PowerShell equivalent:
```powershell
mvn -q -DskipTests compile
java -cp "target/classes;target/cs122b-project3/WEB-INF/lib/*" com.fablix.util.CsvDataImporter "C:\Users\shian\Downloads\csv_package-1.tar.gz"
```

### Cleaning / Validation Behavior
- Missing required values are reported and skipped.
- Invalid numeric values are treated as `NULL` where nullable; otherwise row is reported and skipped.
- Duplicate rows are tracked and inserted once.
- Inconsistent relationship rows (missing foreign keys) are reported and skipped.
- Import continues after bad rows (does not crash the run).

### Optimization Report
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
