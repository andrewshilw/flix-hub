<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Fabflix Login</title>
    <script src="https://www.google.com/recaptcha/api.js" type="text/javascript"></script>
    <style>
        body { font-family: Arial, sans-serif; }
        .login-box { max-width: 360px; margin: 60px auto; padding: 20px; border: 1px solid #ddd; }
        label { display: block; margin-top: 12px; }
        input[type="email"], input[type="password"] { width: 100%; padding: 8px; box-sizing: border-box; }
        .error { color: #b00020; margin-top: 12px; }
        button { margin-top: 16px; padding: 8px 12px; }
    </style>
</head>
<body>
<div class="login-box">
    <h1>Login</h1>
    <form action="login" method="post">
        <label for="email">Email</label>
        <input id="email" name="email" type="email" required />

        <label for="password">Password</label>
        <input id="password" name="password" type="password" required />

        <div style="margin-top: 12px;">
            <div class="g-recaptcha" data-sitekey="6LeT7WwsAAAAACVihZXLDbp16v8lRVKlVzlM9LIe"></div>
        </div>

        <button type="submit">Sign In</button>
    </form>

    <%
        String error = (String) request.getAttribute("error");
        if (error != null) {
    %>
    <div class="error"><%= error %></div>
    <%
        }
    %>
</div>
</body>
</html>
