package com.fablix.servlet;

import com.fablix.util.DbConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/movie-suggestion")
public class MovieSuggestionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int MAX_SUGGESTIONS = 10;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JsonArray suggestions = new JsonArray();
        String rawQuery = trimToNull(request.getParameter("query"));

        if (rawQuery == null || rawQuery.length() < 3) {
            response.getWriter().write(suggestions.toString());
            return;
        }

        String booleanQuery = buildBooleanPrefixQuery(rawQuery);
        if (booleanQuery == null) {
            response.getWriter().write(suggestions.toString());
            return;
        }

        String sql = "SELECT m.id, m.title, MATCH(m.title) AGAINST (? IN BOOLEAN MODE) AS score " +
                "FROM movies m " +
                "WHERE MATCH(m.title) AGAINST (? IN BOOLEAN MODE) " +
                "ORDER BY score DESC, m.title ASC " +
                "LIMIT ?";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(DbConfig.URL, DbConfig.USER, DbConfig.PASSWORD);
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, booleanQuery);
                statement.setString(2, booleanQuery);
                statement.setInt(3, MAX_SUGGESTIONS);

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        suggestions.add(buildSuggestion(resultSet.getString("id"), resultSet.getString("title")));
                    }
                }
            }
        } catch (Exception e) {
            throw new ServletException("Unable to fetch movie suggestions", e);
        }

        response.getWriter().write(suggestions.toString());
    }

    private JsonObject buildSuggestion(String movieId, String title) {
        JsonObject suggestion = new JsonObject();
        suggestion.addProperty("value", title);

        JsonObject data = new JsonObject();
        data.addProperty("movieId", movieId);
        suggestion.add("data", data);
        return suggestion;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String buildBooleanPrefixQuery(String query) {
        String[] rawTokens = query.trim().split("\\s+");
        StringBuilder builder = new StringBuilder();

        for (String token : rawTokens) {
            String cleaned = token.replaceAll("[^A-Za-z0-9]", "");
            if (cleaned.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append('+').append(cleaned).append('*');
        }

        return builder.length() == 0 ? null : builder.toString();
    }
}
