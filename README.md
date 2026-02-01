# CS 122B Winter 2026 Projects

## Project 1
- Demo video: https://drive.google.com/file/d/1Xu-MGIgYv1Lb4EyGZ_3DIY7em0WsBUcZ/view?usp=sharing
- Member: Andrew Shi (full contribution)

## Project 2
- Demo video: [to be added]
- Member: Andrew Shi (full contribution)

### Project 2 Notes

#### LIKE / ILIKE usage
- Search (Main Page -> Movie List): `MovieListServlet` uses `LIKE` with `%keyword%` for substring matching on `m.title`, `m.director`, and `s.name`.
- Browse by title (A-Z / 0-9): `MovieListServlet` uses `LIKE` with `prefix%` (case-insensitive via `UPPER(m.title)`) to match titles that start with the selected character.
- Year uses exact match (`m.year = ?`) and does not use substring matching.