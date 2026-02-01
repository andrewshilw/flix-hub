package com.fablix.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.fablix.util.DbConfig;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("login.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            request.setAttribute("error", "Email and password are required.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(DbConfig.URL, DbConfig.USER, DbConfig.PASSWORD)) {
                String query = "SELECT password FROM customers WHERE email = ?";
                try (PreparedStatement statement = conn.prepareStatement(query)) {
                    statement.setString(1, email);
                    try (ResultSet rs = statement.executeQuery()) {
                        if (!rs.next()) {
                            request.setAttribute("error", "No account found for that email.");
                            request.getRequestDispatcher("login.jsp").forward(request, response);
                            return;
                        }

                        String dbPassword = rs.getString("password");
                        if (!password.equals(dbPassword)) {
                            request.setAttribute("error", "Incorrect password.");
                            request.getRequestDispatcher("login.jsp").forward(request, response);
                            return;
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("customerEmail", email);
        response.sendRedirect(request.getContextPath() + "/");
    }
}
