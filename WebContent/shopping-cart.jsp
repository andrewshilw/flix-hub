<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.Map" %>
<%@ page import="com.fablix.model.CartItem" %>
<!DOCTYPE html>
<html>
<head>
    <title>Shopping Cart</title>
    <style>
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ddd; padding: 8px; }
        th { background-color: #f2f2f2; }
        .actions form { display: inline; margin-right: 6px; }
    </style>
</head>
<body>
<form action="logout" method="post" style="float:right; margin-left:8px;">
    <button type="submit">Logout</button>
</form>
<a href="<%= request.getContextPath() %>/">Home</a>

<%
    Map<String, CartItem> cart = (Map<String, CartItem>) request.getAttribute("cart");
    Double total = (Double) request.getAttribute("total");
%>

<h1>Your Shopping Cart</h1>

<% if (cart == null || cart.isEmpty()) { %>
<p>Your cart is empty.</p>
<% } else { %>
<table>
    <tr>
        <th>Movie</th>
        <th>Price</th>
        <th>Quantity</th>
        <th>Subtotal</th>
        <th>Actions</th>
    </tr>
    <%
        for (CartItem item : cart.values()) {
            double subtotal = item.getPrice() * item.getQuantity();
    %>
    <tr>
        <td><%= item.getTitle() %></td>
        <td>$<%= String.format("%.2f", item.getPrice()) %></td>
        <td>
            <form action="shopping-cart" method="post">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="movieId" value="<%= item.getMovieId() %>">
                <input type="number" name="quantity" min="1" value="<%= item.getQuantity() %>">
                <button type="submit">Update</button>
            </form>
        </td>
        <td>$<%= String.format("%.2f", subtotal) %></td>
        <td class="actions">
            <form action="shopping-cart" method="post">
                <input type="hidden" name="action" value="increase">
                <input type="hidden" name="movieId" value="<%= item.getMovieId() %>">
                <button type="submit">+</button>
            </form>
            <form action="shopping-cart" method="post">
                <input type="hidden" name="action" value="decrease">
                <input type="hidden" name="movieId" value="<%= item.getMovieId() %>">
                <button type="submit">-</button>
            </form>
            <form action="shopping-cart" method="post">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="movieId" value="<%= item.getMovieId() %>">
                <button type="submit">Delete</button>
            </form>
        </td>
    </tr>
    <%
        }
    %>
</table>
<p><strong>Total:</strong> $<%= total != null ? String.format("%.2f", total) : "0.00" %></p>
<form action="payment" method="get">
    <button type="submit">Proceed to Payment</button>
</form>
<% } %>
</body>
</html>
