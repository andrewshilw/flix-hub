package com.fablix.servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/movie-suggestion")
public class MovieSuggestionServlet extends DatabaseServlet {
    private static final long serialVersionUID = 1L;
    private static final int MAX_SUGGESTIONS = 10;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String rawQuery = trimToNull(request.getParameter("query"));
        List<String> tokens = tokenizeSearchQuery(rawQuery);
        if (rawQuery == null || rawQuery.length() < 3 || tokens.isEmpty()) {
            response.getWriter().write("[]");
            return;
        }

        List<String> suggestions = new ArrayList<>();

        try {
            try (Connection conn = getReadConnection()) {
                StringBuilder query = new StringBuilder();
                query.append("SELECT m.id, m.title, COALESCE(r.rating, 0) AS rating ")
                        .append("FROM movies m ")
                        .append("LEFT JOIN ratings r ON m.id = r.movieId ");

                List<String> conditions = new ArrayList<>();
                List<String> params = new ArrayList<>();
                for (String token : tokens) {
                    conditions.add("LOWER(m.title) REGEXP ?");
                    params.add(buildTitlePrefixRegex(token));
                }

                query.append("WHERE ").append(String.join(" AND ", conditions)).append(" ")
                        .append("ORDER BY rating DESC, m.title ASC ")
                        .append("LIMIT ?");

                try (PreparedStatement statement = conn.prepareStatement(query.toString())) {
                    int index = 1;
                    for (String param : params) {
                        statement.setString(index++, param);
                    }
                    statement.setInt(index, MAX_SUGGESTIONS);

                    try (ResultSet rs = statement.executeQuery()) {
                        while (rs.next()) {
                            suggestions.add(buildSuggestionJson(rs.getString("id"), rs.getString("title")));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            return;
        }

        response.getWriter().write("[" + String.join(",", suggestions) + "]");
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private List<String> tokenizeSearchQuery(String query) {
        List<String> tokens = new ArrayList<>();
        if (query == null) {
            return tokens;
        }
        String[] rawTokens = query.trim().split("\\s+");
        for (String token : rawTokens) {
            String cleaned = token.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
            if (!cleaned.isEmpty()) {
                tokens.add(cleaned);
            }
        }
        return tokens;
    }

    private String buildTitlePrefixRegex(String token) {
        return "(^|[^[:alnum:]])" + token + "[[:alnum:]]*";
    }

    private String buildSuggestionJson(String movieId, String title) {
        return "{\"value\":\"" + escapeJson(title) + "\",\"data\":{\"movieId\":\"" + escapeJson(movieId) + "\"}}";
    }

    private String escapeJson(String value) {
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (ch < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) ch));
                    } else {
                        escaped.append(ch);
                    }
            }
        }
        return escaped.toString();
    }
}
