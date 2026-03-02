package com.fablix.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// This annotation maps this Java Servlet Class to a URL
@WebServlet("/stars")
public class StarServlet extends DatabaseServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Set response mime type
        response.setContentType("text/html");

        // Get the PrintWriter for writing response
        PrintWriter out = response.getWriter();

        out.println("<html>");
        out.println("<head><title>Fabflix</title></head>");

        try {
            String query = "SELECT * from stars limit 10";
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {

                out.println("<body>");
                out.println("<h1>MovieDB Stars</h1>");

                out.println("<table border>");

                // Add table header row
                out.println("<tr>");
                out.println("<td>id</td>");
                out.println("<td>name</td>");
                out.println("<td>birth year</td>");
                out.println("</tr>");

                // Add a row for every star result
                while (resultSet.next()) {
                    // get a star from result set
                    String starID = resultSet.getString("id");
                    String starName = resultSet.getString("name");
                    String birthYear = resultSet.getString("birthyear");

                    out.println("<tr>");
                    out.println("<td>" + starID + "</td>");
                    out.println("<td>" + starName + "</td>");
                    out.println("<td>" + birthYear + "</td>");
                    out.println("</tr>");
                }

                out.println("</table>");
                out.println("</body>");
            }

        } catch (Exception e) {
            /*
             * After you deploy the WAR file through tomcat manager webpage,
             *   there's no console to see the print messages.
             * Tomcat append all the print messages to the file: tomcat_directory/logs/catalina.out
             *
             * To view the last n lines (for example, 100 lines) of messages you can use:
             *   tail -100 catalina.out
             * This can help you debug your program after deploying it on AWS.
             */
            request.getServletContext().log("Error: ", e);

            out.println("<body>");
            out.println("<p>");
            out.println("Exception in doGet: " + e.getMessage());
            out.println("</p>");
            out.print("</body>");
        }

        out.println("</html>");
        out.close();

    }


}
