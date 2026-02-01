<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.List" %>
<%
    List<String> genres = (List<String>) request.getAttribute("genres");
    if (genres == null) {
        response.sendRedirect(request.getContextPath() + "/main");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Fablix Main Page</title>
    <style>
        .section { margin-bottom: 24px; }
        .browse-list a { margin-right: 8px; }
    </style>
</head>
<body>
<form action="logout" method="post" style="float:right;">
    <button type="submit">Logout</button>
</form>
<h1>Fablix Main Page</h1>
<p><a href="movie-list">Top 20 Rated Movies</a></p>

<div class="section">
    <h2>Search Movies</h2>
    <form action="movie-list" method="get">
        <label>Title: <input type="text" name="title"></label><br>
        <label>Year: <input type="number" name="year"></label><br>
        <label>Director: <input type="text" name="director"></label><br>
        <label>Star's Name: <input type="text" name="star"></label><br>
        <button type="submit">Search</button>
    </form>
</div>

<div class="section">
    <h2>Browse by Genre</h2>
    <div class="browse-list">
        <%
            for (String g : genres) {
                String encoded = URLEncoder.encode(g, "UTF-8");
        %>
        <a href="movie-list?genre=<%= encoded %>"><%= g %></a>
        <%
            }
        %>
    </div>
</div>

<div class="section">
    <h2>Browse by Title</h2>
    <div class="browse-list">
        <%
            for (int i = 0; i <= 9; i++) {
        %>
        <a href="movie-list?titlePrefix=<%= i %>"><%= i %></a>
        <%
            }
            for (char c = 'A'; c <= 'Z'; c++) {
        %>
        <a href="movie-list?titlePrefix=<%= c %>"><%= c %></a>
        <%
            }
        %>
        <a href="movie-list?titlePrefix=*">*</a>
    </div>
</div>
</body>
</html>
