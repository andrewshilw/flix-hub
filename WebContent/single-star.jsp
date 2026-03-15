<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.fablix.model.Star" %>
<%@ page import="com.fablix.model.Movie" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html>
<head>
    <title>Star Details</title>
</head>
<body>
<form action="logout" method="post" style="float:right; margin-left:8px;">
    <button type="submit">Logout</button>
</form>
<form action="shopping-cart" method="get" style="float:right;">
    <button type="submit">Checkout</button>
</form>
<a href="<%= request.getContextPath() %>/">Home</a>
<br>
<%
    String backToListUrl = (String) request.getAttribute("backToListUrl");
    if (backToListUrl == null || backToListUrl.isBlank()) {
        backToListUrl = request.getContextPath() + "/movie-list";
    }
%>
<a href="<%= backToListUrl %>">Back to Movie List</a>

<%
    Star star = (Star) request.getAttribute("star");
    Long accessCount = (Long) request.getAttribute("accessCount");
    if (star != null) {
%>
<h1><%= star.getName() %></h1>
<p><strong>Date of Birth:</strong> <%= (star.getBirthYear() != 0) ? star.getBirthYear() : "N/A" %></p>
<% if (accessCount != null) { %>
<p><strong>Shared star-service access count:</strong> <%= accessCount %></p>
<% } %>

<h3>Movies Acted In</h3>
<ul>
    <%
        List<Movie> movies = (List<Movie>) request.getAttribute("movies");
        if (movies != null) {
            for (Movie m : movies) {
    %>
    <li>
        <a href="single-movie?id=<%= m.getId() %>">
            <%= m.getTitle() %>
        </a>
        (<%= m.getYear() %>)
    </li>
    <%
            }
        }
    %>
</ul>
<%
} else {
%>
<h2>Star not found.</h2>
<% } %>
</body>
</html>
