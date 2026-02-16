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
- Demo video: https://drive.google.com/file/d/1moS-igocPlDNaQBWmFUbn4yY8GWbHBJq/view?usp=sharing
- Member: Andrew Shi (full contribution)

### Filenames with Prepared Statements
- src/com/fablix/servlet/AddToCartServlet.java
- src/com/fablix/servlet/EmployeeDashboardServlet.java
- src/com/fablix/servlet/LoginServlet.java
- src/com/fablix/servlet/MainPageServlet.java
- src/com/fablix/servlet/MovieListServlet.java
- src/com/fablix/servlet/PlaceOrderServlet.java
- src/com/fablix/servlet/SingleMovieServlet.java
- src/com/fablix/servlet/SingleStarServlet.java
- src/com/fablix/servlet/StarServlet.java
- src/com/fablix/util/CsvDataImporter.java
- src/com/fablix/util/UpdateSecurePassword.java
- src/com/fablix/util/VerifyPassword.java

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
cd /home/ubuntu/cs122b-winter-2026-ok/project3
mvn exec:java -Dexec.mainClass="com.fablix.util.CsvDataImporter" -Dexec.args="/home/ubuntu/csv_package-1.tar.gz"
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

#### Inconsistency Report
```bash
Preloaded existing rows: movies=9052, stars=60150, genres=23, stars_in_movies=79921, genres_in_movies=15615, ratings=7998
[movies] bad row 289: required field missing/invalid (id/title/year/director)
[movies] bad row 290: required field missing/invalid (id/title/year/director)
[movies] bad row 291: required field missing/invalid (id/title/year/director)
[movies] bad row 457: required field missing/invalid (id/title/year/director)
[movies] bad row 2258: required field missing/invalid (id/title/year/director)
[movies] bad row 2525: required field missing/invalid (id/title/year/director)
[movies] bad row 2531: required field missing/invalid (id/title/year/director)
[movies] bad row 2532: required field missing/invalid (id/title/year/director)
[movies] bad row 2533: required field missing/invalid (id/title/year/director)
[movies] bad row 2534: required field missing/invalid (id/title/year/director)
[movies] bad row 2535: required field missing/invalid (id/title/year/director)
[movies] bad row 2536: required field missing/invalid (id/title/year/director)
[movies] bad row 2537: required field missing/invalid (id/title/year/director)
[movies] bad row 2538: required field missing/invalid (id/title/year/director)
[movies] bad row 2539: required field missing/invalid (id/title/year/director)
[movies] bad row 2540: required field missing/invalid (id/title/year/director)
[movies] bad row 2541: required field missing/invalid (id/title/year/director)
[movies] bad row 2542: required field missing/invalid (id/title/year/director)
[movies] bad row 2543: required field missing/invalid (id/title/year/director)
[movies] bad row 2544: required field missing/invalid (id/title/year/director)
[movies] bad row 2545: required field missing/invalid (id/title/year/director)
[movies] bad row 2931: required field missing/invalid (id/title/year/director)
[movies] bad row 4236: required field missing/invalid (id/title/year/director)
[movies] bad row 4780: required field missing/invalid (id/title/year/director)
[movies] bad row 5059: required field missing/invalid (id/title/year/director)
[movies] bad row 5676: required field missing/invalid (id/title/year/director)
[movies] bad row 5683: required field missing/invalid (id/title/year/director)
[movies] bad row 6016: required field missing/invalid (id/title/year/director)
[movies] bad row 6951: required field missing/invalid (id/title/year/director)
[movies] bad row 6952: required field missing/invalid (id/title/year/director)
[movies] bad row 7195: required field missing/invalid (id/title/year/director)
[movies] bad row 7359: required field missing/invalid (id/title/year/director)
[movies] bad row 7600: required field missing/invalid (id/title/year/director)
[movies] bad row 7761: required field missing/invalid (id/title/year/director)
[movies] bad row 7836: required field missing/invalid (id/title/year/director)
[movies] bad row 8269: required field missing/invalid (id/title/year/director)
[movies] bad row 8630: required field missing/invalid (id/title/year/director)
[movies] bad row 9038: required field missing/invalid (id/title/year/director)
[movies] bad row 9144: required field missing/invalid (id/title/year/director)
[movies] bad row 9306: required field missing/invalid (id/title/year/director)
[movies] bad row 9780: required field missing/invalid (id/title/year/director)
[movies] bad row 10048: required field missing/invalid (id/title/year/director)
[movies] bad row 10140: required field missing/invalid (id/title/year/director)
[movies] bad row 10431: required field missing/invalid (id/title/year/director)
[movies] bad row 11118: required field missing/invalid (id/title/year/director)
[movies] bad row 11252: required field missing/invalid (id/title/year/director)
[movies] bad row 11287: required field missing/invalid (id/title/year/director)
[movies] bad row 11555: required field missing/invalid (id/title/year/director)
[movies] bad row 11587: required field missing/invalid (id/title/year/director)
[movies] bad row 11591: required field missing/invalid (id/title/year/director)
[movies] bad row 11611: required field missing/invalid (id/title/year/director)
[movies] bad row 11634: required field missing/invalid (id/title/year/director)
[movies] bad row 11637: required field missing/invalid (id/title/year/director)
[movies] bad row 11925: required field missing/invalid (id/title/year/director)
[movies] bad row 12098: required field missing/invalid (id/title/year/director)
[movies] bad row 12099: required field missing/invalid (id/title/year/director)
movies: inserted=12007, skipped-duplicate=52, bad=56, timeMs=911
[stars] bad row 8996: required field missing/invalid (id/name)
stars: inserted=18678, skipped-duplicate=24, bad=1, timeMs=559
[genres] bad row 71: required field missing/invalid (id/name)
genres: inserted=100, skipped-duplicate=23, bad=1, timeMs=15
[stars_in_movies] bad row 73: foreign key missing (starId=s000006860, movieId=AAd10)
[stars_in_movies] bad row 74: foreign key missing (starId=s000004466, movieId=AAd10)
[stars_in_movies] bad row 75: foreign key missing (starId=s000001606, movieId=AAd10)
[stars_in_movies] bad row 76: foreign key missing (starId=s000006860, movieId=AAd10)
[stars_in_movies] bad row 77: foreign key missing (starId=s000006860, movieId=AAd10)
[stars_in_movies] bad row 78: foreign key missing (starId=s000006861, movieId=AAd10)
[stars_in_movies] bad row 79: foreign key missing (starId=s000002135, movieId=AAd10)
[stars_in_movies] bad row 114: foreign key missing (starId=s000005418, movieId=AAn14)
[stars_in_movies] bad row 115: foreign key missing (starId=s000006875, movieId=AAn14)
[stars_in_movies] bad row 116: foreign key missing (starId=s000006876, movieId=AAn14)
[stars_in_movies] bad row 117: foreign key missing (starId=s000003865, movieId=AAn14)
[stars_in_movies] bad row 118: foreign key missing (starId=s000006877, movieId=AAn14)
[stars_in_movies] bad row 119: foreign key missing (starId=s000006878, movieId=AAn14)
[stars_in_movies] bad row 120: foreign key missing (starId=s000000417, movieId=AAn14)
[stars_in_movies] bad row 121: foreign key missing (starId=s000000323, movieId=AAn14)
[stars_in_movies] bad row 122: foreign key missing (starId=s000006879, movieId=AAn14)
[stars_in_movies] bad row 123: foreign key missing (starId=s000006880, movieId=AAn14)
[stars_in_movies] bad row 124: foreign key missing (starId=s000006881, movieId=AAn14)
[stars_in_movies] bad row 125: foreign key missing (starId=s000006882, movieId=AAn14)
[stars_in_movies] bad row 126: foreign key missing (starId=s000006100, movieId=AAn14)
[stars_in_movies] bad row 127: foreign key missing (starId=s000006699, movieId=AAn14)
[stars_in_movies] bad row 128: foreign key missing (starId=s000006883, movieId=AAn14)
[stars_in_movies] bad row 188: foreign key missing (starId=s000006914, movieId=AbK15)
[stars_in_movies] bad row 189: foreign key missing (starId=s000006859, movieId=AbK15)
[stars_in_movies] bad row 190: foreign key missing (starId=s000006859, movieId=AbK15)
[stars_in_movies] bad row 191: foreign key missing (starId=s000006859, movieId=AbK15)
[stars_in_movies] bad row 324: foreign key missing (starId=s000006936, movieId=ACf10)
[stars_in_movies] bad row 325: foreign key missing (starId=s000006937, movieId=ACf10)
[stars_in_movies] bad row 326: foreign key missing (starId=s000006299, movieId=ACf10)
[stars_in_movies] bad row 327: foreign key missing (starId=s000006938, movieId=ACf10)
[stars_in_movies] bad row 433: foreign key missing (starId=s000007001, movieId=ACu27)
[stars_in_movies] bad row 434: foreign key missing (starId=s000007002, movieId=ACu27)
[stars_in_movies] bad row 435: foreign key missing (starId=s000007003, movieId=ACu27)
[stars_in_movies] bad row 436: foreign key missing (starId=s000006860, movieId=ACu27)
[stars_in_movies] bad row 437: foreign key missing (starId=s000006860, movieId=ACu27)
[stars_in_movies] bad row 667: foreign key missing (starId=s000007092, movieId=ADx1)
[stars_in_movies] bad row 668: foreign key missing (starId=s000007093, movieId=ADx1)
[stars_in_movies] bad row 669: foreign key missing (starId=s000007094, movieId=ADx1)
[stars_in_movies] bad row 670: foreign key missing (starId=s000006859, movieId=ADx1)
[stars_in_movies] bad row 671: foreign key missing (starId=s000006859, movieId=ADx1)
[stars_in_movies] bad row 672: foreign key missing (starId=s000006859, movieId=ADx1)
[stars_in_movies] bad row 836: foreign key missing (starId=s000002757, movieId=AHa10)
[stars_in_movies] bad row 837: foreign key missing (starId=s000004752, movieId=AHa10)
[stars_in_movies] bad row 838: foreign key missing (starId=s000002868, movieId=AHa10)
[stars_in_movies] bad row 839: foreign key missing (starId=s000004199, movieId=AHa10)
[stars_in_movies] bad row 840: foreign key missing (starId=s000007152, movieId=AHa10)
[stars_in_movies] bad row 841: foreign key missing (starId=s000006140, movieId=AHa10)
[stars_in_movies] bad row 842: foreign key missing (starId=s000001410, movieId=AHa10)
[stars_in_movies] bad row 1615: foreign key missing (starId=s000000490, movieId=AMa69)
[stars_in_movies] bad row 1712: foreign key missing (starId=s000004257, movieId=Amo13)
[stars_in_movies] bad row 1713: foreign key missing (starId=s000007474, movieId=Amo13)
[stars_in_movies] bad row 1714: foreign key missing (starId=s000007475, movieId=Amo13)
[stars_in_movies] bad row 1715: foreign key missing (starId=s000007476, movieId=Amo13)
[stars_in_movies] bad row 1716: foreign key missing (starId=s000004347, movieId=Amo13)
[stars_in_movies] bad row 1717: foreign key missing (starId=s000004814, movieId=AMp1)
[stars_in_movies] bad row 1718: foreign key missing (starId=s000007477, movieId=AMp1)
[stars_in_movies] bad row 1719: foreign key missing (starId=s000002955, movieId=AMp1)
[stars_in_movies] bad row 1720: foreign key missing (starId=s000007478, movieId=AMp1)
[stars_in_movies] bad row 1721: foreign key missing (starId=s000006776, movieId=AMp1)
[stars_in_movies] bad row 1722: foreign key missing (starId=s000003538, movieId=AMp1)
[stars_in_movies] bad row 1798: foreign key missing (starId=s000002036, movieId=AnB24)
[stars_in_movies] bad row 1799: foreign key missing (starId=s000000872, movieId=AnB24)
[stars_in_movies] bad row 1800: foreign key missing (starId=s000007024, movieId=AnB24)
[stars_in_movies] bad row 1801: foreign key missing (starId=s000006860, movieId=AnB24)
[stars_in_movies] bad row 1802: foreign key missing (starId=s000007510, movieId=AnB24)
[stars_in_movies] bad row 2353: foreign key missing (starId=s000004677, movieId=ArL51)
[stars_in_movies] bad row 2354: foreign key missing (starId=s000004490, movieId=ArL51)
[stars_in_movies] bad row 2355: foreign key missing (starId=s000004127, movieId=ArL51)
[stars_in_movies] bad row 2356: foreign key missing (starId=s000005168, movieId=ArL51)
[stars_in_movies] bad row 2357: foreign key missing (starId=s000006148, movieId=ArL51)
[stars_in_movies] bad row 2358: foreign key missing (starId=s000002117, movieId=ArL51)
[stars_in_movies] bad row 2359: foreign key missing (starId=s000001778, movieId=ArL51)
[stars_in_movies] bad row 2597: foreign key missing (starId=s000006859, movieId=ATk12)
[stars_in_movies] bad row 2598: foreign key missing (starId=s000006541, movieId=ATk12)
[stars_in_movies] bad row 3062: foreign key missing (starId=s000000596, movieId=BBl1)
[stars_in_movies] bad row 3077: foreign key missing (starId=s000002234, movieId=BCh10)
[stars_in_movies] bad row 3078: foreign key missing (starId=s000002755, movieId=BCh10)
[stars_in_movies] bad row 3143: foreign key missing (starId=s000001557, movieId=BdP1)
[stars_in_movies] bad row 3469: foreign key missing (starId=s000008059, movieId=BeM10)
[stars_in_movies] bad row 3470: foreign key missing (starId=s000002560, movieId=BeM10)
[stars_in_movies] bad row 3471: foreign key missing (starId=s000008060, movieId=BeM10)
[stars_in_movies] bad row 3472: foreign key missing (starId=s000000608, movieId=BeM10)
[stars_in_movies] bad row 3473: foreign key missing (starId=s000008061, movieId=BeM10)
[stars_in_movies] bad row 3686: foreign key missing (starId=s000008127, movieId=BHt10)
[stars_in_movies] bad row 3687: foreign key missing (starId=s000005268, movieId=BHt10)
[stars_in_movies] bad row 3688: foreign key missing (starId=s000001712, movieId=BHt10)
[stars_in_movies] bad row 3930: foreign key missing (starId=s000005197, movieId=Bmy14)
[stars_in_movies] bad row 3931: foreign key missing (starId=s000008217, movieId=Bmy14)
[stars_in_movies] bad row 3932: foreign key missing (starId=s000008218, movieId=Bmy14)
[stars_in_movies] bad row 3933: foreign key missing (starId=s000008219, movieId=Bmy14)
[stars_in_movies] bad row 3934: foreign key missing (starId=s000008220, movieId=Bmy14)
[stars_in_movies] bad row 3935: foreign key missing (starId=s000008221, movieId=Bmy14)
[stars_in_movies] bad row 3936: foreign key missing (starId=s000008222, movieId=Bmy14)
[stars_in_movies] bad row 3959: foreign key missing (starId=s000004656, movieId=BnjR10)
[stars_in_movies] bad row 4154: foreign key missing (starId=s000000866, movieId=BRf39)
[stars_in_movies] bad row 4155: foreign key missing (starId=s000008315, movieId=BRf39)
[stars_in_movies] bad row 4206: foreign key missing (starId=s000004059, movieId=zSh10)
[stars_in_movies] bad row 4207: foreign key missing (starId=s000008259, movieId=zSh10)
[stars_in_movies] bad row 4422: foreign key missing (starId=s000003221, movieId=BsK30)
[stars_in_movies] bad row 4531: foreign key missing (starId=s000008428, movieId=BuE10)
stars_in_movies: inserted=46686, skipped-duplicate=1223, bad=1029, timeMs=1380
[genres_in_movies] bad row 218: foreign key missing (genreId=7, movieId=MSe10)
[genres_in_movies] bad row 219: foreign key missing (genreId=7, movieId=MSe15)
[genres_in_movies] bad row 220: foreign key missing (genreId=7, movieId=MSe18)
[genres_in_movies] bad row 346: foreign key missing (genreId=18, movieId=CGl44)
[genres_in_movies] bad row 1827: required field missing/invalid (genreId/movieId)
[genres_in_movies] bad row 1828: required field missing/invalid (genreId/movieId)
[genres_in_movies] bad row 1829: required field missing/invalid (genreId/movieId)
[genres_in_movies] bad row 1830: required field missing/invalid (genreId/movieId)
[genres_in_movies] bad row 1831: required field missing/invalid (genreId/movieId)
[genres_in_movies] bad row 1832: required field missing/invalid (genreId/movieId)
[genres_in_movies] bad row 1833: required field missing/invalid (genreId/movieId)
[genres_in_movies] bad row 1834: required field missing/invalid (genreId/movieId)
[genres_in_movies] bad row 1835: required field missing/invalid (genreId/movieId)
[genres_in_movies] bad row 1836: required field missing/invalid (genreId/movieId)
[genres_in_movies] bad row 1837: required field missing/invalid (genreId/movieId)
[genres_in_movies] bad row 1838: required field missing/invalid (genreId/movieId)
[genres_in_movies] bad row 2117: foreign key missing (genreId=5, movieId=HBH10)
[genres_in_movies] bad row 2118: foreign key missing (genreId=7, movieId=HBH10)
[genres_in_movies] bad row 3108: foreign key missing (genreId=10, movieId=JD7)
[genres_in_movies] bad row 3532: foreign key missing (genreId=48, movieId=WS45)
[genres_in_movies] bad row 4776: foreign key missing (genreId=71, movieId=SS4)
[genres_in_movies] bad row 5066: foreign key missing (genreId=71, movieId=DPo2)
[genres_in_movies] bad row 5067: foreign key missing (genreId=71, movieId=DPo4)
[genres_in_movies] bad row 5427: foreign key missing (genreId=10, movieId=RD10)
[genres_in_movies] bad row 5566: foreign key missing (genreId=2, movieId=RHd2)
[genres_in_movies] bad row 5788: foreign key missing (genreId=13, movieId=Z6510)
[genres_in_movies] bad row 6352: foreign key missing (genreId=1, movieId=MS0)
[genres_in_movies] bad row 7041: foreign key missing (genreId=4, movieId=WiW30)
[genres_in_movies] bad row 7102: foreign key missing (genreId=71, movieId=SRi19)
[genres_in_movies] bad row 7140: foreign key missing (genreId=48, movieId=DLy25)
[genres_in_movies] bad row 7295: foreign key missing (genreId=15, movieId=MLt15)
[genres_in_movies] bad row 7702: foreign key missing (genreId=2, movieId=GyM24)
[genres_in_movies] bad row 8034: foreign key missing (genreId=7, movieId=NIs10)
[genres_in_movies] bad row 8308: foreign key missing (genreId=2, movieId=BSi3)
[genres_in_movies] bad row 8925: foreign key missing (genreId=12, movieId=CsS15)
[genres_in_movies] bad row 9051: required field missing/invalid (genreId/movieId)
[genres_in_movies] bad row 9079: foreign key missing (genreId=14, movieId=BeM10)
[genres_in_movies] bad row 9406: foreign key missing (genreId=20, movieId=Z9590)
[genres_in_movies] bad row 9407: foreign key missing (genreId=1, movieId=AbK15)
genres_in_movies: inserted=9786, skipped-duplicate=25, bad=39, timeMs=241
[ratings] bad row 1052: foreign key missing (movieId=MLt15)
ratings: inserted=1591, skipped-duplicate=2, bad=1, timeMs=109
Import finished in 4s
```