package com.fablix.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;

import com.fablix.util.RedisSession;
import com.fablix.util.RedisSessionManager;
import com.fablix.util.RecaptchaVerifyUtils;
import org.jasypt.util.password.StrongPasswordEncryptor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/login")
public class LoginServlet extends DatabaseServlet {
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

        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        try {
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        } catch (Exception e) {
            request.setAttribute("error", "reCAPTCHA verification failed. Please try again.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        try {
            try (Connection conn = getReadConnection()) {
                String query = "SELECT id, password FROM customers WHERE email = ?";
                try (PreparedStatement statement = conn.prepareStatement(query)) {
                    statement.setString(1, email);
                    try (ResultSet rs = statement.executeQuery()) {
                        if (!rs.next()) {
                            request.setAttribute("error", "No account found for that email.");
                            request.getRequestDispatcher("login.jsp").forward(request, response);
                            return;
                        }

                        String encryptedPassword = rs.getString("password");
                        boolean success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
                        if (!success) {
                            request.setAttribute("error", "Incorrect password.");
                            request.getRequestDispatcher("login.jsp").forward(request, response);
                            return;
                        }

                        int customerId = rs.getInt("id");
                        RedisSession session = RedisSessionManager.getOrCreateSession(request, response);
                        session.setCustomerEmail(email);
                        session.setCustomerId(customerId);
                        session.setCustomerLoginTime(Instant.now().toString());
                        RedisSessionManager.saveSession(request, response, session);
                        response.sendRedirect(request.getContextPath() + "/");
                        return;
                    }
                }
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
