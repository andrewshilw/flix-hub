<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%
    Boolean employeeLoggedIn = (Boolean) request.getAttribute("employeeLoggedIn");
    if (employeeLoggedIn == null) {
        employeeLoggedIn = false;
    }
    String dashboardError = (String) request.getAttribute("dashboardError");
    String dashboardMessage = (String) request.getAttribute("dashboardMessage");
    String employeeEmail = (String) request.getAttribute("employeeEmail");
    String employeeName = (String) request.getAttribute("employeeName");
    List<String[]> metadataRows = (List<String[]>) request.getAttribute("metadataRows");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Fabflix Dashboard</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 24px; }
        .box { border: 1px solid #ccc; padding: 16px; margin-bottom: 16px; }
        .error { color: #b00020; }
        .ok { color: #0a7d00; }
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ddd; padding: 6px 8px; text-align: left; }
        th { background: #f7f7f7; }
        label { display: block; margin-top: 10px; }
        input[type="text"], input[type="number"], input[type="email"], input[type="password"] { width: 100%; max-width: 420px; padding: 8px; box-sizing: border-box; }
        button { margin-top: 12px; padding: 8px 12px; }
    </style>
</head>
<body>
<h1>Fabflix Employee Dashboard</h1>

<% if (dashboardError != null) { %>
<p class="error"><%= dashboardError %></p>
<% } %>
<% if (dashboardMessage != null) { %>
<p class="ok"><%= dashboardMessage %></p>
<% } %>

<% if (!employeeLoggedIn) { %>
<div class="box">
    <h2>Employee Login</h2>
    <form method="post" action="_dashboard">
        <label for="email">Email</label>
        <input id="email" name="email" type="email" required>
        <label for="password">Password</label>
        <input id="password" name="password" type="password" required>
        <button type="submit">Sign In</button>
    </form>
</div>
<% } else { %>
<div class="box">
    <p>Signed in as <strong><%= employeeName == null ? employeeEmail : employeeName %></strong>
        (<%= employeeEmail %>)</p>
    <form method="post" action="_dashboard">
        <input type="hidden" name="action" value="logout">
        <button type="submit">Logout</button>
    </form>
</div>

<div class="box">
    <h2>Add Star</h2>
    <form method="post" action="_dashboard">
        <input type="hidden" name="action" value="add-star">
        <label for="starName">Star Name (required)</label>
        <input id="starName" name="starName" type="text" required>
        <label for="birthYear">Birth Year (optional)</label>
        <input id="birthYear" name="birthYear" type="number">
        <button type="submit">Add Star</button>
    </form>
</div>

<div class="box">
    <h2>Add Movie (Stored Procedure)</h2>
    <form method="post" action="_dashboard">
        <input type="hidden" name="action" value="add-movie">
        <label for="movieTitle">Title</label>
        <input id="movieTitle" name="movieTitle" type="text" required>
        <label for="movieYear">Year</label>
        <input id="movieYear" name="movieYear" type="number" required>
        <label for="movieDirector">Director</label>
        <input id="movieDirector" name="movieDirector" type="text" required>
        <label for="movieStar">Star Name (new or existing)</label>
        <input id="movieStar" name="movieStar" type="text" required>
        <label for="movieGenre">Genre Name (new or existing)</label>
        <input id="movieGenre" name="movieGenre" type="text" required>
        <button type="submit">Add Movie</button>
    </form>
</div>

<div class="box">
    <h2>Database Metadata</h2>
    <table>
        <thead>
        <tr><th>Table</th><th>Column</th><th>Type</th></tr>
        </thead>
        <tbody>
        <% if (metadataRows != null) {
            for (String[] row : metadataRows) { %>
        <tr>
            <td><%= row[0] %></td>
            <td><%= row[1] %></td>
            <td><%= row[2] %></td>
        </tr>
        <%  }
        } %>
        </tbody>
    </table>
</div>
<% } %>

</body>
</html>
