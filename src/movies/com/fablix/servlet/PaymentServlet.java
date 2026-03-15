package com.fablix.servlet;

import com.fablix.model.CartItem;
import com.fablix.util.CartUtil;
import com.fablix.util.RedisSession;
import com.fablix.util.RedisSessionManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/payment")
public class PaymentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RedisSession session = RedisSessionManager.getOrCreateSession(request, response);
        Map<String, CartItem> cart = CartUtil.ensureCart(session.getCart());
        session.setCart(cart);
        RedisSessionManager.saveSession(request, response, session);

        request.setAttribute("total", CartUtil.computeTotal(cart));
        request.getRequestDispatcher("payment.jsp").forward(request, response);
    }
}
