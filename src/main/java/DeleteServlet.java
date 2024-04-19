import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebServlet("/DeleteServlet")
public class DeleteServlet extends HttpServlet {
    private static final Logger Log = LogManager.getLogger(DeleteServlet.class);
    private Connection connection;
    public void init() throws ServletException {
        super.init();
        DatabaseInitializer initializer = DatabaseInitializer.getInstance();
        connection = initializer.getConnection();
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get the ID parameter from the request
        String idParam = request.getParameter("id");

        try {
            // Parse the ID parameter to an integer
            int id = Integer.parseInt(idParam);

            if (connection != null) {
                try (PreparedStatement statement = connection.prepareStatement("DELETE FROM user_data WHERE id = ?")) {
                    statement.setInt(1, id);
                    int rowsDeleted = statement.executeUpdate();
                    if (rowsDeleted > 0) {
                        Log.info("Data with ID " + id + " deleted successfully");
                    } else {
                        Log.info("No data found with ID " + id);
                    }
                } catch (SQLException e) {
                    Log.error("Exception occurred while deleting user data", e);
                }
            } else {
                Log.error("Database connection is null");
            }

            // Redirect back to the main page after deletion
            response.sendRedirect(request.getContextPath() + "/");
        } catch (NumberFormatException e) {
            // Handle the case where the ID parameter is not a valid integer
            Log.error("Invalid ID parameter: " + idParam, e);
            // Optionally, you can display an error message to the user or redirect to an error page
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID parameter");
        }
        finally {
            if (connection==null){
                try {
                    connection.close();
                } catch (SQLException e) {
                    Log.error(e);
                }
            }
        }
    }
}
