package com.fablix.servlet;

import com.fablix.model.Star;
import com.fablix.util.DbConfig;
import com.fablix.model.Movie;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/single-star")
public class SingleStarServlet extends HttpServlet {
    private static final long serialVersionUID = 3L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String starId = request.getParameter("id");
        Star star = null;
        List<Movie> moviesActedIn = new ArrayList<>();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(DbConfig.URL, DbConfig.USER, DbConfig.PASSWORD)) {

                // 1. Get Star Info
                String starQuery = "SELECT * FROM stars WHERE id = ?";
                PreparedStatement starStmt = conn.prepareStatement(starQuery);
                starStmt.setString(1, starId);
                ResultSet rs = starStmt.executeQuery();
                if (rs.next()) {
                    String birthYearStr = rs.getString("birthYear");
                    int birthYear = (birthYearStr != null) ? Integer.parseInt(birthYearStr) : 0;
                    star = new Star(rs.getString("id"), rs.getString("name"), birthYear);
                }
                rs.close();
                starStmt.close();

                // 2. Get Movies this star is in
                // We reuse the Movie object but only fill the fields we need (id, title, year, director)
                String movieQuery = "SELECT m.id, m.title, m.year, m.director " +
                        "FROM movies m " +
                        "JOIN stars_in_movies sm ON m.id = sm.movieId " +
                        "WHERE sm.starId = ? " +
                        "ORDER BY m.year DESC, m.title ASC";

                PreparedStatement movieStmt = conn.prepareStatement(movieQuery);
                movieStmt.setString(1, starId);
                ResultSet mrs = movieStmt.executeQuery();

                while (mrs.next()) {
                    // Pass null for fields we don't query here (rating, genres, stars)
                    moviesActedIn.add(new Movie(
                            mrs.getString("id"),
                            mrs.getString("title"),
                            mrs.getString("year"),
                            mrs.getString("director"),
                            null, null, null, null
                    ));
                }
                mrs.close();
                movieStmt.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        request.setAttribute("star", star);
        request.setAttribute("movies", moviesActedIn);
        String backToListUrl = (String) request.getSession().getAttribute("lastMovieListUrl");
        request.setAttribute("backToListUrl", backToListUrl);
        request.getRequestDispatcher("single-star.jsp").forward(request, response);
    }
}
