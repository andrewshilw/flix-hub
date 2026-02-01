<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>

<%@ page import="com.fablix.model.Movie" %> <!DOCTYPE html>
<html>
<head>
    <title>Top 20 Movies</title>
    <style>
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ddd; padding: 8px; }
        th { background-color: #f2f2f2; }
    </style>
</head>
<body>
<form action="logout" method="post" style="float:right;">
    <button type="submit">Logout</button>
</form>
<a href="index.jsp">Home</a>
<h1>Top 20 Rated Movies</h1>
<table>
    <tr>
        <th>Title</th>
        <th>Year</th>
        <th>Director</th>
        <th>Genres</th>
        <th>Stars</th>
        <th>Rating</th>
    </tr>
    <%
        List<Movie> movies = (List<Movie>) request.getAttribute("movieList");
        if (movies != null) {
            for (Movie m : movies) {
    %>
    <tr>
        <td><a href="single-movie?id=<%= m.getId() %>"><%= m.getTitle() %></a></td>
        <td><%= m.getYear() %></td>
        <td><%= m.getDirector() %></td>
        <td><%= m.getGenres() %></td>
        <td>
            <%
                // Logic to split the comma-separated stars and create individual links
                String[] starNames = m.getStars().split(",");
                String[] starIds = m.getStarIds().split(",");
                for (int i = 0; i < starNames.length; i++) {
            %>
            <a href="single-star?id=<%= starIds[i] %>"><%= starNames[i] %></a>
            <%= (i < starNames.length - 1) ? ", " : "" %>
            <%
                }
            %>
        </td>
        <td><%= m.getRating() %></td>
    </tr>
    <%
            }
        }
    %>
</table>
</body>
</html>
