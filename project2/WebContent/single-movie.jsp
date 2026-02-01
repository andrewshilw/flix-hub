<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.fablix.model.Movie" %>
<!DOCTYPE html>
<html>
<head>
    <title>Movie Details</title>
</head>
<body>
<form action="logout" method="post" style="float:right;">
    <button type="submit">Logout</button>
</form>
<a href="index.jsp">Home</a>
<br>
<a href="movie-list">Back to Movie List</a>

<%
    Movie m = (Movie) request.getAttribute("movie");
    if (m != null) {
%>
<h1><%= m.getTitle() %> (<%= m.getYear() %>)</h1>
<p><strong>Director:</strong> <%= m.getDirector() %></p>
<p><strong>Rating:</strong> <%= m.getRating() != null ? m.getRating() : "N/A" %></p>
<p><strong>Genres:</strong> <%= m.getGenres() != null ? m.getGenres() : "None" %></p>

<h3>Stars</h3>
<ul>
    <%
        if (m.getStars() != null && !m.getStars().isEmpty()) {
            String[] starNames = m.getStars().split(",");
            String[] starIds = m.getStarIds().split(",");
            for (int i = 0; i < starNames.length; i++) {
    %>
    <li><a href="single-star?id=<%= starIds[i] %>"><%= starNames[i] %></a></li>
    <%
        }
    } else {
    %>
    <li>No stars recorded.</li>
    <% } %>
</ul>
<%
} else {
%>
<h2>Movie not found.</h2>
<% } %>
</body>
</html>
