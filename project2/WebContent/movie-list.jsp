<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.net.URLEncoder" %>

<%@ page import="com.fablix.model.Movie" %> <!DOCTYPE html>
<html>
<head>
    <title>Movie List</title>
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
<a href="<%= request.getContextPath() %>/">Home</a>
<%
    String pageTitle = (String) request.getAttribute("pageTitle");
    String criteriaDescription = (String) request.getAttribute("criteriaDescription");
    Integer pageNum = (Integer) request.getAttribute("page");
    Integer pageSize = (Integer) request.getAttribute("pageSize");
    Integer totalCount = (Integer) request.getAttribute("totalCount");
    String sortField = (String) request.getAttribute("sortField");
    String sortOrder = (String) request.getAttribute("sortOrder");
    String titleParam = (String) request.getAttribute("title");
    String yearParam = (String) request.getAttribute("year");
    String directorParam = (String) request.getAttribute("director");
    String starParam = (String) request.getAttribute("star");
    String genreParam = (String) request.getAttribute("genre");
    String titlePrefixParam = (String) request.getAttribute("titlePrefix");
    if (pageNum == null) pageNum = 1;
    if (pageSize == null) pageSize = 10;
    if (totalCount == null) totalCount = 0;
    int totalPages = (int) Math.ceil(totalCount / (double) pageSize);
%>
<h1><%= pageTitle != null ? pageTitle : "Movie List" %></h1>
<% if (criteriaDescription != null) { %>
<p><strong>Filters:</strong> <%= criteriaDescription %></p>
<% } %>
<div>
    <form action="movie-list" method="get">
        <% if (titleParam != null) { %><input type="hidden" name="title" value="<%= titleParam %>"><% } %>
        <% if (yearParam != null) { %><input type="hidden" name="year" value="<%= yearParam %>"><% } %>
        <% if (directorParam != null) { %><input type="hidden" name="director" value="<%= directorParam %>"><% } %>
        <% if (starParam != null) { %><input type="hidden" name="star" value="<%= starParam %>"><% } %>
        <% if (genreParam != null) { %><input type="hidden" name="genre" value="<%= genreParam %>"><% } %>
        <% if (titlePrefixParam != null) { %><input type="hidden" name="titlePrefix" value="<%= titlePrefixParam %>"><% } %>
        <label>Sort:
            <select name="sort">
                <option value="title" <%= "title".equals(sortField) ? "selected" : "" %>>Title</option>
                <option value="rating" <%= "rating".equals(sortField) ? "selected" : "" %>>Rating</option>
            </select>
        </label>
        <label>Order:
            <select name="order">
                <option value="asc" <%= "asc".equals(sortOrder) ? "selected" : "" %>>Ascending</option>
                <option value="desc" <%= "desc".equals(sortOrder) ? "selected" : "" %>>Descending</option>
            </select>
        </label>
        <label>Per Page:
            <select name="pageSize">
                <option value="10" <%= pageSize == 10 ? "selected" : "" %>>10</option>
                <option value="25" <%= pageSize == 25 ? "selected" : "" %>>25</option>
                <option value="50" <%= pageSize == 50 ? "selected" : "" %>>50</option>
                <option value="100" <%= pageSize == 100 ? "selected" : "" %>>100</option>
            </select>
        </label>
        <input type="hidden" name="page" value="1">
        <button type="submit">Apply</button>
    </form>
</div>
<div>
    <%
        String baseQuery = "";
        if (titleParam != null) baseQuery += "&title=" + URLEncoder.encode(titleParam, "UTF-8");
        if (yearParam != null) baseQuery += "&year=" + URLEncoder.encode(yearParam, "UTF-8");
        if (directorParam != null) baseQuery += "&director=" + URLEncoder.encode(directorParam, "UTF-8");
        if (starParam != null) baseQuery += "&star=" + URLEncoder.encode(starParam, "UTF-8");
        if (genreParam != null) baseQuery += "&genre=" + URLEncoder.encode(genreParam, "UTF-8");
        if (titlePrefixParam != null) baseQuery += "&titlePrefix=" + URLEncoder.encode(titlePrefixParam, "UTF-8");
        baseQuery += "&sort=" + URLEncoder.encode(sortField, "UTF-8");
        baseQuery += "&order=" + URLEncoder.encode(sortOrder, "UTF-8");
        baseQuery += "&pageSize=" + pageSize;
    %>
    <% if (pageNum > 1) { %>
    <a href="movie-list?page=<%= pageNum - 1 %><%= baseQuery %>">Prev</a>
    <% } %>
    <span>Page <%= pageNum %> of <%= Math.max(totalPages, 1) %> (Total <%= totalCount %> movies)</span>
    <% if (pageNum < totalPages) { %>
    <a href="movie-list?page=<%= pageNum + 1 %><%= baseQuery %>">Next</a>
    <% } %>
</div>
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
        <td>
            <%
                if (m.getGenres() != null && !m.getGenres().isEmpty()) {
                    String[] genreNames = m.getGenres().split(", ");
                    for (int i = 0; i < genreNames.length; i++) {
                        String g = genreNames[i];
                        String encoded = URLEncoder.encode(g, "UTF-8");
            %>
            <a href="movie-list?genre=<%= encoded %>"><%= g %></a><%= (i < genreNames.length - 1) ? ", " : "" %>
            <%
                    }
                }
            %>
        </td>
        <td>
            <%
                if (m.getStars() != null && m.getStarIds() != null) {
                    String[] starNames = m.getStars().split(",");
                    String[] starIds = m.getStarIds().split(",");
                    for (int i = 0; i < starNames.length && i < starIds.length; i++) {
            %>
            <a href="single-star?id=<%= starIds[i] %>"><%= starNames[i] %></a>
            <%= (i < starNames.length - 1 && i < starIds.length - 1) ? ", " : "" %>
            <%
                    }
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
