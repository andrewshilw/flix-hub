package com.fablix.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.jasypt.util.password.StrongPasswordEncryptor;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/_dashboard")
public class EmployeeDashboardServlet extends DatabaseServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        boolean loggedIn = session != null && session.getAttribute("employeeEmail") != null;

        request.setAttribute("employeeLoggedIn", loggedIn);
        if (loggedIn) {
            request.setAttribute("employeeEmail", session.getAttribute("employeeEmail"));
            request.setAttribute("employeeName", session.getAttribute("employeeName"));
            request.setAttribute("metadataRows", loadMetadata());
        }
        request.getRequestDispatcher("dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        boolean loggedIn = session.getAttribute("employeeEmail") != null;

        if (!loggedIn) {
            handleLogin(request, response, session);
            return;
        }

        String action = request.getParameter("action");
        if ("logout".equals(action)) {
            session.removeAttribute("employeeEmail");
            session.removeAttribute("employeeName");
            response.sendRedirect(request.getContextPath() + "/_dashboard");
            return;
        }

        if ("add-star".equals(action)) {
            handleAddStar(request);
        } else if ("add-movie".equals(action)) {
            handleAddMovie(request);
        } else {
            request.setAttribute("dashboardError", "Unknown action.");
        }

        request.setAttribute("employeeLoggedIn", true);
        request.setAttribute("employeeEmail", session.getAttribute("employeeEmail"));
        request.setAttribute("employeeName", session.getAttribute("employeeName"));
        request.setAttribute("metadataRows", loadMetadata());
        request.getRequestDispatcher("dashboard.jsp").forward(request, response);
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ServletException, IOException {
        String email = trimToNull(request.getParameter("email"));
        String password = request.getParameter("password");

        if (email == null || password == null || password.isBlank()) {
            request.setAttribute("dashboardError", "Email and password are required.");
            request.setAttribute("employeeLoggedIn", false);
            request.getRequestDispatcher("dashboard.jsp").forward(request, response);
            return;
        }

        try {
            EmployeeAuth employeeAuth = verifyEmployee(email, password);
            if (!employeeAuth.success) {
                request.setAttribute("dashboardError", "Invalid employee credentials.");
                request.setAttribute("employeeLoggedIn", false);
                request.getRequestDispatcher("dashboard.jsp").forward(request, response);
                return;
            }

            session.setAttribute("employeeEmail", email);
            session.setAttribute("employeeName", employeeAuth.fullName);
            response.sendRedirect(request.getContextPath() + "/_dashboard");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private EmployeeAuth verifyEmployee(String email, String password) throws Exception {
        String query = "SELECT password, fullname FROM employees WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return new EmployeeAuth(false, null);
                }
                String encryptedPassword = rs.getString("password");
                boolean success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
                return new EmployeeAuth(success, rs.getString("fullname"));
            }
        }
    }

    private void handleAddStar(HttpServletRequest request) {
        String name = trimToNull(request.getParameter("starName"));
        String birthYearText = trimToNull(request.getParameter("birthYear"));

        if (name == null) {
            request.setAttribute("dashboardError", "Star name is required.");
            return;
        }

        Integer birthYear = null;
        if (birthYearText != null) {
            try {
                birthYear = Integer.parseInt(birthYearText);
            } catch (NumberFormatException e) {
                request.setAttribute("dashboardError", "Birth year must be a number.");
                return;
            }
        }

        try {
            String insertSql =
                    "INSERT INTO stars(id, name, birthYear) " +
                            "SELECT CONCAT('nm', LPAD(COALESCE(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)), 0) + 1, 7, '0')), ?, ? " +
                            "FROM stars";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, name);
                if (birthYear == null) {
                    stmt.setNull(2, Types.INTEGER);
                } else {
                    stmt.setInt(2, birthYear);
                }
                stmt.executeUpdate();
            }
            request.setAttribute("dashboardMessage", "Star added successfully.");
        } catch (Exception e) {
            request.setAttribute("dashboardError", "Failed to add star: " + e.getMessage());
        }
    }

    private void handleAddMovie(HttpServletRequest request) {
        String title = trimToNull(request.getParameter("movieTitle"));
        String yearText = trimToNull(request.getParameter("movieYear"));
        String director = trimToNull(request.getParameter("movieDirector"));
        String starName = trimToNull(request.getParameter("movieStar"));
        String genreName = trimToNull(request.getParameter("movieGenre"));

        if (title == null || yearText == null || director == null || starName == null || genreName == null) {
            request.setAttribute("dashboardError", "Movie title, year, director, star, and genre are required.");
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearText);
        } catch (NumberFormatException e) {
            request.setAttribute("dashboardError", "Movie year must be a number.");
            return;
        }

        try {
            try (Connection conn = getConnection();
                 CallableStatement call = conn.prepareCall("{CALL add_movie(?, ?, ?, ?, ?)}")) {
                call.setString(1, title);
                call.setInt(2, year);
                call.setString(3, director);
                call.setString(4, starName);
                call.setString(5, genreName);

                String resultMessage = "Movie operation completed.";
                boolean hasResult = call.execute();
                if (hasResult) {
                    try (ResultSet rs = call.getResultSet()) {
                        if (rs.next()) {
                            String status = rs.getString("status");
                            String movieId = rs.getString("movie_id");
                            resultMessage = status + (movieId == null ? "" : (" (movieId=" + movieId + ")"));
                        }
                    }
                }
                request.setAttribute("dashboardMessage", resultMessage);
            }
        } catch (Exception e) {
            request.setAttribute("dashboardError", "Failed to add movie: " + e.getMessage());
        }
    }

    private List<String[]> loadMetadata() {
        List<String[]> rows = new ArrayList<>();
        try {
            try (Connection conn = getConnection();
                 PreparedStatement schemaStmt = conn.prepareStatement("SELECT DATABASE()");
                 ResultSet schemaRs = schemaStmt.executeQuery()) {

                if (!schemaRs.next()) {
                    return rows;
                }

                String schema = schemaRs.getString(1);
                String sql =
                        "SELECT table_name, column_name, column_type " +
                                "FROM information_schema.columns " +
                                "WHERE table_schema = ? " +
                                "ORDER BY table_name ASC, ordinal_position ASC";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, schema);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            rows.add(new String[]{
                                    rs.getString("table_name"),
                                    rs.getString("column_name"),
                                    rs.getString("column_type")
                            });
                        }
                    }
                }
            }
        } catch (Exception e) {
            rows.clear();
            rows.add(new String[]{"<error>", "metadata", e.getMessage()});
        }
        return rows;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static class EmployeeAuth {
        private final boolean success;
        private final String fullName;

        private EmployeeAuth(boolean success, String fullName) {
            this.success = success;
            this.fullName = fullName;
        }
    }
}
