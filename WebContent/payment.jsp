<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Payment</title>
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
    Double total = (Double) request.getAttribute("total");
    String error = (String) request.getAttribute("error");
%>

<h1>Payment</h1>
<p><strong>Total:</strong> $<%= total != null ? String.format("%.2f", total) : "0.00" %></p>

<% if (error != null) { %>
<p style="color:red;"><%= error %></p>
<% } %>

<form action="place-order" method="post">
    <label>First Name: <input type="text" name="firstName" required></label><br>
    <label>Last Name: <input type="text" name="lastName" required></label><br>
    <label>Credit Card Number: <input type="text" name="cardNumber" required></label><br>
    <label>Expiration Date: <input type="date" name="expiration" required></label><br>
    <button type="submit">Place Order</button>
</form>
</body>
</html>
