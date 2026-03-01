DROP PROCEDURE IF EXISTS add_movie;

DELIMITER $$

CREATE PROCEDURE add_movie(
    IN p_title VARCHAR(100),
    IN p_year INT,
    IN p_director VARCHAR(100),
    IN p_star_name VARCHAR(100),
    IN p_genre_name VARCHAR(32)
)
BEGIN
    DECLARE v_movie_id VARCHAR(10);
    DECLARE v_star_id VARCHAR(10);
    DECLARE v_genre_id INT;

    SELECT id INTO v_movie_id
    FROM movies
    WHERE title = p_title AND year = p_year AND director = p_director
    LIMIT 1;

    IF v_movie_id IS NOT NULL THEN
        SELECT 'Duplicate movie exists. No insert performed.' AS status, v_movie_id AS movie_id;
    ELSE
        SELECT CONCAT('tt', LPAD(COALESCE(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)), 0) + 1, 7, '0'))
        INTO v_movie_id
        FROM movies;

        INSERT INTO movies(id, title, year, director)
        VALUES (v_movie_id, p_title, p_year, p_director);

        SELECT id INTO v_star_id
        FROM stars
        WHERE name = p_star_name
        LIMIT 1;

        IF v_star_id IS NULL THEN
            SELECT CONCAT('nm', LPAD(COALESCE(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)), 0) + 1, 7, '0'))
            INTO v_star_id
            FROM stars;

            INSERT INTO stars(id, name, birthYear)
            VALUES (v_star_id, p_star_name, NULL);
        END IF;

        SELECT id INTO v_genre_id
        FROM genres
        WHERE name = p_genre_name
        LIMIT 1;

        IF v_genre_id IS NULL THEN
            SELECT COALESCE(MAX(id), 0) + 1 INTO v_genre_id FROM genres;
            INSERT INTO genres(id, name) VALUES (v_genre_id, p_genre_name);
        END IF;

        INSERT INTO stars_in_movies(starId, movieId)
        VALUES (v_star_id, v_movie_id);

        INSERT INTO genres_in_movies(genreId, movieId)
        VALUES (v_genre_id, v_movie_id);

        INSERT INTO ratings(movieId, rating, numVotes)
        VALUES (v_movie_id, 0.0, 0);

        SELECT 'Movie added successfully.' AS status, v_movie_id AS movie_id;
    END IF;
END$$

DELIMITER ;
