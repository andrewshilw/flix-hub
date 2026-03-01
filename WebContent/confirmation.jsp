<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.fablix.model.CartItem" %>
<!DOCTYPE html>
<html>
<head>
    <title>Order Confirmation</title>
</head>
<body>
<form action="logout" method="post" style="float:right; margin-left:8px;">
    <button type="submit">Logout</button>
</form>
<form action="shopping-cart" method="get" style="float:right;">
    <button type="submit">Checkout</button>
</form>
<a href="<%= request.getContextPath() %>/">Home</a>

<%
    List<CartItem> items = (List<CartItem>) request.getAttribute("purchasedItems");
    Double total = (Double) request.getAttribute("total");
    java.sql.Date saleDate = (java.sql.Date) request.getAttribute("saleDate");
%>

<h1>Order Confirmation</h1>
<p>Your order has been placed successfully.</p>
<% if (saleDate != null) { %>
<p><strong>Sale Date:</strong> <%= saleDate %></p>
<% } %>

<% if (items != null && !items.isEmpty()) { %>
<h3>Items</h3>
<ul>
    <%
        for (CartItem item : items) {
    %>
    <li><%= item.getTitle() %> x <%= item.getQuantity() %></li>
    <%
        }
    %>
</ul>
<% } %>

<p><strong>Total Paid:</strong> $<%= total != null ? String.format("%.2f", total) : "0.00" %></p>
</body>
</html>
