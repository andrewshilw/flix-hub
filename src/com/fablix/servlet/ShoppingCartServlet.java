package com.fablix.servlet;

import com.fablix.model.CartItem;
import com.fablix.util.CartUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

@WebServlet("/shopping-cart")
public class ShoppingCartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        Map<String, CartItem> cart = CartUtil.ensureCart(session.getAttribute("cart"));
        session.setAttribute("cart", cart);

        request.setAttribute("cart", cart);
        request.setAttribute("total", CartUtil.computeTotal(cart));
        request.getRequestDispatcher("shopping-cart.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        Map<String, CartItem> cart = CartUtil.ensureCart(session.getAttribute("cart"));

        String action = request.getParameter("action");
        String movieId = request.getParameter("movieId");
        if (movieId != null && cart.containsKey(movieId)) {
            CartItem item = cart.get(movieId);
            if ("increase".equals(action)) {
                item.setQuantity(item.getQuantity() + 1);
            } else if ("decrease".equals(action)) {
                int next = item.getQuantity() - 1;
                if (next <= 0) {
                    cart.remove(movieId);
                } else {
                    item.setQuantity(next);
                }
            } else if ("delete".equals(action)) {
                cart.remove(movieId);
            } else if ("update".equals(action)) {
                int quantity = parseIntParam(request.getParameter("quantity"), item.getQuantity());
                if (quantity <= 0) {
                    cart.remove(movieId);
                } else {
                    item.setQuantity(quantity);
                }
            }
        }

        session.setAttribute("cart", cart);
        response.sendRedirect(request.getContextPath() + "/shopping-cart");
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
