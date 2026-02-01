package com.fablix.servlet;

import com.fablix.model.Movie;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/movie-list")
public class MovieListServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        String loginUser = "root";
//        String loginPasswd = "Tghdfj123!"; // Your password
//        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";
        String loginUser = "mytestuser";
        String loginPasswd = "My6$Password";
        String loginUrl =
                "jdbc:mysql://localhost:3306/moviedb" +
                        "?useSSL=false" +
                        "&allowPublicKeyRetrieval=true" +
                        "&serverTimezone=UTC";

        List<Movie> movieList = new ArrayList<>();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Modern driver class
            try (Connection conn = DriverManager.getConnection(loginUrl, loginUser, loginPasswd)) {

                // This query gets the Top 20 rated movies and collapses stars/genres into single strings
                String query = "SELECT m.id, m.title, m.year, m.director, r.rating, " +
                        "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name ASC SEPARATOR ', '), ', ', 3) as genres, " +
                        "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.name ASC SEPARATOR ','), ',', 3) as stars, " +
                        "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY s.name ASC SEPARATOR ','), ',', 3) as starIds " +
                        "FROM movies m " +
                        "JOIN ratings r ON m.id = r.movieId " +
                        "JOIN genres_in_movies gm ON m.id = gm.movieId " +
                        "JOIN genres g ON gm.genreId = g.id " +
                        "JOIN stars_in_movies sm ON m.id = sm.movieId " +
                        "JOIN stars s ON sm.starId = s.id " +
                        "GROUP BY m.id, m.title, m.year, m.director, r.rating " +
                        "ORDER BY r.rating DESC " +
                        "LIMIT 20";

                PreparedStatement statement = conn.prepareStatement(query);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    movieList.add(new Movie(
                            rs.getString("id"),
                            rs.getString("title"),
                            rs.getString("year"),
                            rs.getString("director"),
                            rs.getString("rating"),
                            rs.getString("genres"),
                            rs.getString("stars"),
                            rs.getString("starIds")
                    ));
                }
                rs.close();
                statement.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Pass the list to the JSP
        request.setAttribute("movieList", movieList);
        request.getRequestDispatcher("movie-list.jsp").forward(request, response);
    }
}