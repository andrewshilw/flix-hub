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
<form action="logout" method="post" style="float:right; margin-left:8px;">
    <button type="submit">Logout</button>
</form>
<form action="shopping-cart" method="get" style="float:right;">
    <button type="submit">Checkout</button>
</form>
<a href="<%= request.getContextPath() %>/">Home</a>
<%
    String pageTitle = (String) request.getAttribute("pageTitle");
    String criteriaDescription = (String) request.getAttribute("criteriaDescription");
    Integer pageNum = (Integer) request.getAttribute("page");
    Integer pageSize = (Integer) request.getAttribute("pageSize");
    Integer totalCount = (Integer) request.getAttribute("totalCount");
    String sortField1 = (String) request.getAttribute("sortField1");
    String sortOrder1 = (String) request.getAttribute("sortOrder1");
    String sortField2 = (String) request.getAttribute("sortField2");
    String sortOrder2 = (String) request.getAttribute("sortOrder2");
    String titleParam = (String) request.getAttribute("title");
    String queryParam = (String) request.getAttribute("query");
    String yearParam = (String) request.getAttribute("year");
    String directorParam = (String) request.getAttribute("director");
    String starParam = (String) request.getAttribute("star");
    String genreParam = (String) request.getAttribute("genre");
    String titlePrefixParam = (String) request.getAttribute("titlePrefix");
    if (pageNum == null) pageNum = 1;
    if (pageSize == null) pageSize = 10;
    if (totalCount == null) totalCount = 0;
    int totalPages = (int) Math.ceil(totalCount / (double) pageSize);
    String cartMessage = (String) session.getAttribute("cartMessage");
    String cartMessageType = (String) session.getAttribute("cartMessageType");
    if (cartMessage != null) {
        session.removeAttribute("cartMessage");
        session.removeAttribute("cartMessageType");
    }
%>
<% if (cartMessage != null) { %>
<p style="color:<%= "error".equals(cartMessageType) ? "red" : "green" %>;"><%= cartMessage %></p>
<% } %>
<h1><%= pageTitle != null ? pageTitle : "Movie List" %></h1>
<% if (criteriaDescription != null) { %>
<p><strong>Filters:</strong> <%= criteriaDescription %></p>
<% } %>
<div>
    <form action="movie-list" method="get">
        <% if (titleParam != null) { %><input type="hidden" name="title" value="<%= titleParam %>"><% } %>
        <% if (queryParam != null) { %><input type="hidden" name="query" value="<%= queryParam %>"><% } %>
        <% if (yearParam != null) { %><input type="hidden" name="year" value="<%= yearParam %>"><% } %>
        <% if (directorParam != null) { %><input type="hidden" name="director" value="<%= directorParam %>"><% } %>
        <% if (starParam != null) { %><input type="hidden" name="star" value="<%= starParam %>"><% } %>
        <% if (genreParam != null) { %><input type="hidden" name="genre" value="<%= genreParam %>"><% } %>
        <% if (titlePrefixParam != null) { %><input type="hidden" name="titlePrefix" value="<%= titlePrefixParam %>"><% } %>
        <label>Primary Sort:
            <select name="sort1">
                <option value="rating" <%= "rating".equals(sortField1) ? "selected" : "" %>>Rating</option>
                <option value="title" <%= "title".equals(sortField1) ? "selected" : "" %>>Title</option>
            </select>
        </label>
        <label>Order:
            <select name="order1">
                <option value="asc" <%= "asc".equals(sortOrder1) ? "selected" : "" %>>Ascending</option>
                <option value="desc" <%= "desc".equals(sortOrder1) ? "selected" : "" %>>Descending</option>
            </select>
        </label>
        <label>Secondary Sort:
            <select name="sort2">
                <option value="rating" <%= "rating".equals(sortField2) ? "selected" : "" %>>Rating</option>
                <option value="title" <%= "title".equals(sortField2) ? "selected" : "" %>>Title</option>
            </select>
        </label>
        <label>Order:
            <select name="order2">
                <option value="asc" <%= "asc".equals(sortOrder2) ? "selected" : "" %>>Ascending</option>
                <option value="desc" <%= "desc".equals(sortOrder2) ? "selected" : "" %>>Descending</option>
            </select>
        </label>
        <label>Per Page:
            <select name="pageSize">
                <option value="10" <%= pageSize == 10 ? "selected" : "" %>>10</option>
                <option value="20" <%= pageSize == 20 ? "selected" : "" %>>20</option>
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
        if (queryParam != null) baseQuery += "&query=" + URLEncoder.encode(queryParam, "UTF-8");
        if (yearParam != null) baseQuery += "&year=" + URLEncoder.encode(yearParam, "UTF-8");
        if (directorParam != null) baseQuery += "&director=" + URLEncoder.encode(directorParam, "UTF-8");
        if (starParam != null) baseQuery += "&star=" + URLEncoder.encode(starParam, "UTF-8");
        if (genreParam != null) baseQuery += "&genre=" + URLEncoder.encode(genreParam, "UTF-8");
        if (titlePrefixParam != null) baseQuery += "&titlePrefix=" + URLEncoder.encode(titlePrefixParam, "UTF-8");
        baseQuery += "&sort1=" + URLEncoder.encode(sortField1, "UTF-8");
        baseQuery += "&order1=" + URLEncoder.encode(sortOrder1, "UTF-8");
        baseQuery += "&sort2=" + URLEncoder.encode(sortField2, "UTF-8");
        baseQuery += "&order2=" + URLEncoder.encode(sortOrder2, "UTF-8");
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
        <th>Cart</th>
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
        <td>
            <form action="add-to-cart" method="post">
                <input type="hidden" name="movieId" value="<%= m.getId() %>">
                <button type="submit">Add to Shopping Cart</button>
            </form>
        </td>
    </tr>
    <%
            }
        }
    %>
</table>
</body>
</html>
