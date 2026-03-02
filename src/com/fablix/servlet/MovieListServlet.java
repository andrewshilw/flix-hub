package com.fablix.servlet;

import com.fablix.model.Movie;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/movie-list")
public class MovieListServlet extends DatabaseServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long jdbcElapsedTime = 0L;
        String fulltextRawQuery = trimToNull(request.getParameter("query"));
        List<String> fulltextTokens = tokenizeSearchQuery(fulltextRawQuery);
        String title = trimToNull(request.getParameter("title"));
        String year = trimToNull(request.getParameter("year"));
        String director = trimToNull(request.getParameter("director"));
        String star = trimToNull(request.getParameter("star"));
        String genre = trimToNull(request.getParameter("genre"));
        String titlePrefix = trimToNull(request.getParameter("titlePrefix"));
        String sortField = trimToNull(request.getParameter("sort"));
        String sortOrder = trimToNull(request.getParameter("order"));
        String sortField1 = trimToNull(request.getParameter("sort1"));
        String sortOrder1 = trimToNull(request.getParameter("order1"));
        String sortField2 = trimToNull(request.getParameter("sort2"));
        String sortOrder2 = trimToNull(request.getParameter("order2"));
        String pageSizeParam = request.getParameter("pageSize");

        int page = parseIntParam(request.getParameter("page"), 1);
        int pageSize = parseIntParam(pageSizeParam, 10);
        if (page < 1) {
            page = 1;
        }
        if (pageSize != 10 && pageSize != 20 && pageSize != 25 && pageSize != 50 && pageSize != 100) {
            pageSize = 10;
        }

        boolean hasSearchParams = !fulltextTokens.isEmpty() || title != null || year != null || director != null || star != null;
        boolean hasBrowseParams = genre != null || titlePrefix != null;
        boolean hasFilters = hasSearchParams || hasBrowseParams;
        boolean sortProvided = sortField != null || sortField1 != null;
        boolean orderProvided = sortOrder != null || sortOrder1 != null;
        boolean pageSizeProvided = pageSizeParam != null && !pageSizeParam.isBlank();

        if (!hasFilters) {
            if (!sortProvided) {
                sortField1 = "rating";
            }
            if (!orderProvided) {
                sortOrder1 = "desc";
            }
            if (!pageSizeProvided) {
                pageSize = 20;
            }
        }

        if (sortField1 == null) {
            sortField1 = sortField;
        }
        if (sortOrder1 == null) {
            sortOrder1 = sortOrder;
        }

        if (!"rating".equalsIgnoreCase(sortField1)) {
            sortField1 = "title";
        } else {
            sortField1 = "rating";
        }
        if (!"desc".equalsIgnoreCase(sortOrder1)) {
            sortOrder1 = "asc";
        } else {
            sortOrder1 = "desc";
        }

        if (sortField2 == null) {
            sortField2 = "rating".equals(sortField1) ? "title" : "rating";
        }
        if (!"rating".equalsIgnoreCase(sortField2)) {
            sortField2 = "title";
        } else {
            sortField2 = "rating";
        }
        if (sortField2.equals(sortField1)) {
            sortField2 = "rating".equals(sortField1) ? "title" : "rating";
        }

        if (!"desc".equalsIgnoreCase(sortOrder2)) {
            sortOrder2 = "asc";
        } else {
            sortOrder2 = "desc";
        }

        List<Movie> movieList = new ArrayList<>();
        int totalCount = 0;

        long jdbcStartTime = 0L;
        try {
            jdbcStartTime = System.nanoTime();
            try (Connection conn = getReadConnection()) {

                StringBuilder query = new StringBuilder();
                query.append("SELECT m.id, m.title, m.year, m.director, r.rating, ")
                        .append("SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name ASC SEPARATOR ', '), ', ', 3) as genres, ")
                        .append("SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY sc.movieCount DESC, s.name ASC SEPARATOR ','), ',', 3) as stars, ")
                        .append("SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY sc.movieCount DESC, s.name ASC SEPARATOR ','), ',', 3) as starIds ")
                        .append("FROM movies m ")
                        .append("JOIN ratings r ON m.id = r.movieId ")
                        .append("JOIN genres_in_movies gm ON m.id = gm.movieId ")
                        .append("JOIN genres g ON gm.genreId = g.id ")
                        .append("JOIN stars_in_movies sm ON m.id = sm.movieId ")
                        .append("JOIN stars s ON sm.starId = s.id ")
                        .append("JOIN (SELECT starId, COUNT(*) AS movieCount FROM stars_in_movies GROUP BY starId) sc ")
                        .append("ON s.id = sc.starId ");

                List<String> conditions = new ArrayList<>();
                List<Object> params = new ArrayList<>();

                for (String token : fulltextTokens) {
                    conditions.add("LOWER(m.title) REGEXP ?");
                    params.add(buildTitlePrefixRegex(token));
                }
                if (title != null) {
                    conditions.add("m.title LIKE ?");
                    params.add("%" + title + "%");
                }
                if (year != null) {
                    conditions.add("m.year = ?");
                    params.add(year);
                }
                if (director != null) {
                    conditions.add("m.director LIKE ?");
                    params.add("%" + director + "%");
                }
                if (star != null) {
                    conditions.add("s.name LIKE ?");
                    params.add("%" + star + "%");
                }
                if (genre != null) {
                    conditions.add("g.name = ?");
                    params.add(genre);
                }
                if (titlePrefix != null) {
                    if ("*".equals(titlePrefix)) {
                        conditions.add("m.title REGEXP '^[^0-9A-Za-z]'");
                    } else {
                        conditions.add("UPPER(m.title) LIKE ?");
                        params.add(titlePrefix.toUpperCase() + "%");
                    }
                }

                if (!conditions.isEmpty()) {
                    query.append("WHERE ").append(String.join(" AND ", conditions)).append(" ");
                }

                query.append("GROUP BY m.id, m.title, m.year, m.director, r.rating ");

                String firstField = "rating".equals(sortField1) ? "r.rating" : "m.title";
                String secondField = "rating".equals(sortField2) ? "r.rating" : "m.title";
                query.append("ORDER BY ")
                        .append(firstField).append(" ").append(sortOrder1).append(", ")
                        .append(secondField).append(" ").append(sortOrder2).append(" ");
                query.append("LIMIT ? OFFSET ? ");

                PreparedStatement statement = conn.prepareStatement(query.toString());
                for (int i = 0; i < params.size(); i++) {
                    statement.setObject(i + 1, params.get(i));
                }
                statement.setInt(params.size() + 1, pageSize);
                statement.setInt(params.size() + 2, (page - 1) * pageSize);
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

                StringBuilder countQuery = new StringBuilder();
                countQuery.append("SELECT COUNT(DISTINCT m.id) AS total ")
                        .append("FROM movies m ")
                        .append("JOIN ratings r ON m.id = r.movieId ")
                        .append("JOIN genres_in_movies gm ON m.id = gm.movieId ")
                        .append("JOIN genres g ON gm.genreId = g.id ")
                        .append("JOIN stars_in_movies sm ON m.id = sm.movieId ")
                        .append("JOIN stars s ON sm.starId = s.id ");
                if (!conditions.isEmpty()) {
                    countQuery.append("WHERE ").append(String.join(" AND ", conditions)).append(" ");
                }
                try (PreparedStatement countStmt = conn.prepareStatement(countQuery.toString())) {
                    for (int i = 0; i < params.size(); i++) {
                        countStmt.setObject(i + 1, params.get(i));
                    }
                    try (ResultSet crs = countStmt.executeQuery()) {
                        if (crs.next()) {
                            totalCount = crs.getInt("total");
                        }
                    }
                }
            }
            jdbcElapsedTime = System.nanoTime() - jdbcStartTime;
        } catch (Exception e) {
            if (jdbcStartTime != 0L) {
                jdbcElapsedTime = System.nanoTime() - jdbcStartTime;
            }
            e.printStackTrace();
        }

        request.setAttribute(SearchTimingFilter.JDBC_TIME_ATTRIBUTE, jdbcElapsedTime);

        HttpSession session = request.getSession();
        String queryString = request.getQueryString();
        String listUrl = request.getContextPath() + "/movie-list";
        if (queryString != null && !queryString.isBlank()) {
            listUrl += "?" + queryString;
        }
        session.setAttribute("lastMovieListUrl", listUrl);

        // Pass the list to the JSP
        request.setAttribute("movieList", movieList);
        request.setAttribute("pageTitle", buildPageTitle(hasFilters, hasSearchParams, hasBrowseParams));
        request.setAttribute("criteriaDescription",
                buildCriteriaDescription(fulltextRawQuery, title, year, director, star, genre, titlePrefix));
        request.setAttribute("page", page);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("sortField1", sortField1);
        request.setAttribute("sortOrder1", sortOrder1);
        request.setAttribute("sortField2", sortField2);
        request.setAttribute("sortOrder2", sortOrder2);
        request.setAttribute("totalCount", totalCount);
        request.setAttribute("title", title);
        request.setAttribute("query", fulltextRawQuery);
        request.setAttribute("year", year);
        request.setAttribute("director", director);
        request.setAttribute("star", star);
        request.setAttribute("genre", genre);
        request.setAttribute("titlePrefix", titlePrefix);
        request.getRequestDispatcher("movie-list.jsp").forward(request, response);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String buildPageTitle(boolean hasFilters, boolean hasSearchParams, boolean hasBrowseParams) {
        if (!hasFilters) {
            return "Top 20 Rated Movies";
        }
        if (hasSearchParams && hasBrowseParams) {
            return "Search and Browse Results";
        }
        if (hasSearchParams) {
            return "Search Results";
        }
        return "Browse Results";
    }

    private String buildCriteriaDescription(String fulltextQuery, String title, String year, String director, String star,
                                            String genre, String titlePrefix) {
        List<String> parts = new ArrayList<>();
        if (fulltextQuery != null) {
            parts.add("Full-text title query = \"" + fulltextQuery + "\"");
        }
        if (title != null) {
            parts.add("Title contains \"" + title + "\"");
        }
        if (year != null) {
            parts.add("Year = " + year);
        }
        if (director != null) {
            parts.add("Director contains \"" + director + "\"");
        }
        if (star != null) {
            parts.add("Star contains \"" + star + "\"");
        }
        if (genre != null) {
            parts.add("Genre = " + genre);
        }
        if (titlePrefix != null) {
            if ("*".equals(titlePrefix)) {
                parts.add("Title starts with non-alphanumeric");
            } else {
                parts.add("Title starts with \"" + titlePrefix.toUpperCase() + "\"");
            }
        }
        if (parts.isEmpty()) {
            return null;
        }
        return String.join("; ", parts);
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

    private int parseIntParam(String value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
