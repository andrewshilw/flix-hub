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
<a href="movie-list">Back to Movie List</a>

<%
    Star star = (Star) request.getAttribute("star");
    if (star != null) {
%>
<h1><%= star.getName() %></h1>
<p><strong>Date of Birth:</strong> <%= (star.getBirthYear() != 0) ? star.getBirthYear() : "N/A" %></p>

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