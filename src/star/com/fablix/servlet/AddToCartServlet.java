package com.fablix.servlet;

import com.fablix.model.CartItem;
import com.fablix.util.CartUtil;
import com.fablix.util.PriceUtil;
import com.fablix.util.RedisSession;
import com.fablix.util.RedisSessionManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

@WebServlet("/add-to-cart")
public class AddToCartServlet extends DatabaseServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String movieId = request.getParameter("movieId");
        RedisSession session = RedisSessionManager.getOrCreateSession(request, response);

        String message;
        String messageType = "success";

        if (movieId == null || movieId.isBlank()) {
            message = "Missing movie id.";
            messageType = "error";
        } else {
            String title = fetchMovieTitle(movieId);
            if (title == null) {
                message = "Movie not found.";
                messageType = "error";
            } else {
                Map<String, CartItem> cart = CartUtil.ensureCart(session.getCart());
                CartItem item = cart.get(movieId);
                if (item == null) {
                    double price = PriceUtil.priceForMovie(movieId);
                    item = new CartItem(movieId, title, price, 1);
                } else {
                    item.setQuantity(item.getQuantity() + 1);
                }
                cart.put(movieId, item);
                session.setCart(cart);
                message = "Added \"" + title + "\" to your cart.";
            }
        }

        session.setCartMessage(message);
        session.setCartMessageType(messageType);
        RedisSessionManager.saveSession(request, response, session);

        String referer = request.getHeader("Referer");
        if (referer == null || referer.isBlank()) {
            referer = request.getContextPath() + "/movie-list";
        }
        response.sendRedirect(referer);
    }

    private String fetchMovieTitle(String movieId) {
        try {
            try (Connection conn = getReadConnection()) {
                String query = "SELECT title FROM movies WHERE id = ?";
                try (PreparedStatement statement = conn.prepareStatement(query)) {
                    statement.setString(1, movieId);
                    try (ResultSet rs = statement.executeQuery()) {
                        if (rs.next()) {
                            return rs.getString("title");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
