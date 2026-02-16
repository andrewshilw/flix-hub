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

@WebServlet("/payment")
public class PaymentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        Map<String, CartItem> cart = CartUtil.ensureCart(session.getAttribute("cart"));
        session.setAttribute("cart", cart);

        request.setAttribute("total", CartUtil.computeTotal(cart));
        request.getRequestDispatcher("payment.jsp").forward(request, response);
    }
}
