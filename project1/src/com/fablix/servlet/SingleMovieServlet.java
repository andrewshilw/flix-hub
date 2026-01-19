package com.fablix.servlet;

import com.fablix.model.Movie;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

@WebServlet("/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        String loginUser = "root";
        String loginPasswd = "Tghdfj123!"; // CHANGE THIS
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(loginUrl, loginUser, loginPasswd)) {

                // Query to get details for ONE movie by ID
                String query = "SELECT m.id, m.title, m.year, m.director, r.rating, " +
                        "GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ', ') as genres, " +
                        "GROUP_CONCAT(DISTINCT s.name ORDER BY s.name SEPARATOR ',') as stars, " +
                        "GROUP_CONCAT(DISTINCT s.id ORDER BY s.name SEPARATOR ',') as starIds " +
                        "FROM movies m " +
                        "LEFT JOIN ratings r ON m.id = r.movieId " +
                        "LEFT JOIN genres_in_movies gm ON m.id = gm.movieId " +
                        "LEFT JOIN genres g ON gm.genreId = g.id " +
                        "LEFT JOIN stars_in_movies sm ON m.id = sm.movieId " +
                        "LEFT JOIN stars s ON sm.starId = s.id " +
                        "WHERE m.id = ? " +
                        "GROUP BY m.id, m.title, m.year, m.director, r.rating";

                PreparedStatement statement = conn.prepareStatement(query);
                statement.setString(1, id);
                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    Movie movie = new Movie(
                            rs.getString("id"),
                            rs.getString("title"),
                            rs.getString("year"),
                            rs.getString("director"),
                            rs.getString("rating"),
                            rs.getString("genres"),
                            rs.getString("stars"),
                            rs.getString("starIds")
                    );
                    request.setAttribute("movie", movie);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        request.getRequestDispatcher("single-movie.jsp").forward(request, response);
    }
}