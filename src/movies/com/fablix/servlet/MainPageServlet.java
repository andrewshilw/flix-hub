package com.fablix.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {"/", "/main"})
public class MainPageServlet extends DatabaseServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<String> genres = new ArrayList<>();

        try {
            try (Connection conn = getReadConnection()) {
                String query = "SELECT name FROM genres ORDER BY name ASC";
                try (PreparedStatement statement = conn.prepareStatement(query);
                     ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        genres.add(rs.getString("name"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        request.setAttribute("genres", genres);
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }
}
