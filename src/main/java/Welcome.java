import jakarta.servlet.*;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;


public class Welcome extends HttpServlet {

    private Connection connection;
    public void init() throws ServletException {

        super.init();
        DatabaseInitializer initializer = DatabaseInitializer.getInstance();
        connection = initializer.getConnection();
    }
    public void destroy() {
        super.destroy();
        if (connection != null) {
            try {
                connection.close();
                Log.info("Database connection closed successfully.");
            } catch (SQLException e) {
                Log.error("Error closing database connection: " + e.getMessage(), e);
            }
        }
    }
    private static final Logger Log = LogManager.getLogger(Welcome.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Display the form and user data
        displayForm(response);
        displayUserData(request, response, connection);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String phoneNumber = request.getParameter("phoneNumber");

        if (connection != null) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO user_data (first_name, last_name, phone_number) VALUES (?, ?, ?)")) {
                statement.setString(1, firstName);
                statement.setString(2, lastName);
                statement.setString(3, phoneNumber);
                statement.executeUpdate();
                Log.info("Data Added");
            } catch (SQLException e) {
                response.setContentType("text/html");
                PrintWriter out = response.getWriter();
                out.println("<h2>");
                out.println("An error occurred while processing your request. Please try again later.<br>");
                out.println("Error Details: " + e.getMessage());
                out.println("</h2>");
                Log.error("Exception occurred while Inserting user data", e);
                return; // Return to avoid redirecting to /Welcome
            }
        } else {
            Log.error("Database connection is null");
        }

        response.sendRedirect(request.getContextPath() + "/");
    }

    private void displayForm(HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><body><title>Welcome</title>");
        out.println("<h1>Welcome To My page.</h1>");
        out.println("<form method='post'>");
        out.println("First Name: <input type='text' name='firstName'><br>");
        out.println("Last Name: <input type='text' name='lastName'><br>");
        out.println("Phone Number: <input type='text' name='phoneNumber'><br>");
        out.println("<input type='submit' value='Add Data'>");
        out.println("</form>");
    }

    private void displayUserData(HttpServletRequest request, HttpServletResponse response, Connection connection) throws IOException {
        PrintWriter out = response.getWriter();

        // Display the form to get the number of rows
        out.println("<form method='get'>");
        out.println("Enter number of Rows: <input type='text' name='NumberofRows'><br>");
        out.println("<input type='submit' value='Set Rows per Page'>");
        out.println("</form>");

        // Retrieve the number of rows per page from the request
        int rowsPerPage = 6; // Default value
        String rowsPerPageParam = request.getParameter("NumberofRows");
        if (rowsPerPageParam != null && !rowsPerPageParam.isEmpty()) {
            rowsPerPage = Integer.parseInt(rowsPerPageParam);
        }

        // Get current page number from request parameter, if provided
        int currentPage = 1;
        String pageParam = request.getParameter("page");
        if (pageParam != null && !pageParam.isEmpty()) {
            currentPage = Integer.parseInt(pageParam);
        }

        out.println("<form method='get'>");
        out.println("SearchBar: <input type='text' name='searchQuery'><br>");
        out.println("<input type='submit' value='Search'>");
        out.println("</form>");

        out.println("<form method='get'>");
        out.println("<select name='sortOrder'>");
        out.println("<option value='ASC'>Ascending</option>");
        out.println("<option value='DESC'>Descending</option>");
        out.println("</select>");
        out.println("<input type='submit' value='Sort'>");
        out.println("</form>");

        try {
        String searchQuery = request.getParameter("searchQuery");
        String sortOrder = request.getParameter("sortOrder"); // Get selected sorting order
            // Construct the SQL query based on the search query
            String sqlQuery = "SELECT * FROM user_data";
            if (searchQuery != null && !searchQuery.isEmpty()) {
                sqlQuery += " WHERE first_name LIKE '%" + searchQuery + "%' OR last_name LIKE '%" + searchQuery + "%' OR phone_number LIKE '%" + searchQuery + "%'" +
                        " ";
            }
            if (sortOrder != null && !sortOrder.isEmpty()) {
                sqlQuery += " ORDER BY first_name " + sortOrder; // Sort by first name with selected order
            }
            // Get total count of rows in user_data table
            Statement countStmt = connection.createStatement();
            ResultSet countRs = countStmt.executeQuery("SELECT COUNT(*) AS total FROM (" + sqlQuery + ") AS search_results");
            int totalCount = 0;
            if (countRs.next()) {
                totalCount = countRs.getInt("total");
            }

            // Calculate total pages
            int totalPages = (int) Math.ceil((double) totalCount / rowsPerPage);

            // Calculate offset
            int offset = (currentPage - 1) * rowsPerPage;
            PreparedStatement statement =null;
            // Execute query with pagination
            try  {
                statement = connection.prepareStatement(sqlQuery + " LIMIT ?, ?");

                statement.setInt(1, offset);
                statement.setInt(2, rowsPerPage);
                ResultSet resultSet = statement.executeQuery();

                // Display fetched data
                out.println("<table border='1'>");
                out.println("<tr><th>ID</th><th>First Name</th><th>Last Name</th><th>Phone Number</th><th>Delete</th></tr>");
                while (resultSet.next()) {
                    out.println("<tr>");
                    out.println("<td>" + resultSet.getInt("id") + "</td>");
                    out.println("<td>" + resultSet.getString("first_name") + "</td>");
                    out.println("<td>" + resultSet.getString("last_name") + "</td>");
                    out.println("<td>" + resultSet.getString("phone_number") + "</td>");
                    out.println("<td><a href=DeleteServlet?id=" + resultSet.getString("id") + ">Delete</a></td>");
                    out.println("</tr>");
                }
                out.println("</table>");

                // Display pagination controls
                out.println("<div>");
                out.println("Page: ");
                for (int i = 1; i <= totalPages; i++) {
                    if (i == currentPage) {
                        out.println("<b>" + i + "</b> ");
                    } else {
                        // Include the search query parameter in the pagination links
                        String queryString = "?page=" + i + "&NumberofRows=" + rowsPerPage;
                        if (searchQuery != null && !searchQuery.isEmpty()) {
                            queryString += "&searchQuery=" + searchQuery;
                        }
                        out.println("<a href='" + request.getRequestURI() + queryString + "'>" + i + "</a> ");
                    }
                }
                out.println("</div>");
            } catch (SQLException e) {
                Log.error("Exception occurred while fetching user data", e);
            }
        } catch (SQLException e) {
            Log.error("Exception occurred ", e);
        }
    }
}


//        try {
//            connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
//            if (connection != null) {
//                Log.info("Connected");
//            } else {
//                Log.info("Sorry");
//            }
//        } catch (SQLException e) {
//            Log.error("Exception occurred while connecting to the database", e);
//        } finally {
//
//
//
//        }