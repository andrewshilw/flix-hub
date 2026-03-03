package com.fablix.servlet;

import com.fablix.model.CartItem;
import com.fablix.util.CartUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/place-order")
public class PlaceOrderServlet extends DatabaseServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        Map<String, CartItem> cart = CartUtil.ensureCart(session.getAttribute("cart"));
        session.setAttribute("cart", cart);

        if (cart.isEmpty()) {
            forwardWithError(request, response, "Your cart is empty.", cart);
            return;
        }

        String firstName = trimToNull(request.getParameter("firstName"));
        String lastName = trimToNull(request.getParameter("lastName"));
        String cardNumber = trimToNull(request.getParameter("cardNumber"));
        String expiration = trimToNull(request.getParameter("expiration"));

        if (firstName == null || lastName == null || cardNumber == null || expiration == null) {
            forwardWithError(request, response, "All payment fields are required.", cart);
            return;
        }

        Integer customerId = (Integer) session.getAttribute("customerId");
        if (customerId == null) {
            customerId = fetchCustomerId(session);
            if (customerId == null) {
                forwardWithError(request, response, "Unable to find your customer record.", cart);
                return;
            }
            session.setAttribute("customerId", customerId);
        }

        Date saleDate = new Date(System.currentTimeMillis());

        try {
            try (Connection conn = getWriteConnection()) {
                conn.setAutoCommit(false);

                if (!isValidCard(conn, cardNumber, firstName, lastName, expiration)) {
                    conn.rollback();
                    forwardWithError(request, response, "Payment information is invalid.", cart);
                    return;
                }

                try (PreparedStatement saleStmt = conn.prepareStatement(
                        "INSERT INTO sales(customerId, movieId, saleDate) VALUES(?,?,?)")) {
                    for (CartItem item : cart.values()) {
                        for (int i = 0; i < item.getQuantity(); i++) {
                            saleStmt.setInt(1, customerId);
                            saleStmt.setString(2, item.getMovieId());
                            saleStmt.setDate(3, saleDate);
                            saleStmt.addBatch();
                        }
                    }
                    saleStmt.executeBatch();
                }

                conn.commit();
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }

        List<CartItem> purchasedItems = new ArrayList<>(cart.values());
        double total = CartUtil.computeTotal(cart);
        session.setAttribute("cart", new LinkedHashMap<>());

        request.setAttribute("purchasedItems", purchasedItems);
        request.setAttribute("total", total);
        request.setAttribute("saleDate", saleDate);
        request.getRequestDispatcher("confirmation.jsp").forward(request, response);
    }

    private boolean isValidCard(Connection conn, String cardNumber, String firstName, String lastName, String expiration)
            throws Exception {
        String query = "SELECT id FROM creditcards WHERE id = ? AND firstName = ? AND lastName = ? AND expiration = ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, cardNumber);
            statement.setString(2, firstName);
            statement.setString(3, lastName);
            statement.setString(4, expiration);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String error,
                                  Map<String, CartItem> cart) throws ServletException, IOException {
        request.setAttribute("error", error);
        request.setAttribute("total", CartUtil.computeTotal(cart));
        request.getRequestDispatcher("payment.jsp").forward(request, response);
    }

    private Integer fetchCustomerId(HttpSession session) {
        Object emailObj = session.getAttribute("customerEmail");
        if (!(emailObj instanceof String)) {
            return null;
        }
        String email = (String) emailObj;
        try {
            try (Connection conn = getReadConnection()) {
                String query = "SELECT id FROM customers WHERE email = ?";
                try (PreparedStatement statement = conn.prepareStatement(query)) {
                    statement.setString(1, email);
                    try (ResultSet rs = statement.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt("id");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
