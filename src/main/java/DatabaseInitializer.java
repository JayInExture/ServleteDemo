

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DatabaseInitializer {
    private static DatabaseInitializer instance;
    private Connection connection;
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/demoservlet";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "Mysql#1234";
    private static final Logger Log = LogManager.getLogger(DatabaseInitializer.class);

    private DatabaseInitializer() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
            Log.info("Database connection initialized successfully.");
        } catch (ClassNotFoundException | SQLException e) {
            Log.error("Error initializing database connection: " + e.getMessage(), e);
        }
        }

    public static synchronized DatabaseInitializer getInstance() {
        if (instance == null) {
            instance = new DatabaseInitializer();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                Log.info("Database connection closed successfully.");
            } catch (SQLException e) {
                Log.error("Error closing database connection: " + e.getMessage(), e);
            }
        }
    }
}


//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//
//import jakarta.servlet.ServletContextEvent;
//import jakarta.servlet.ServletContextListener;
//import jakarta.servlet.annotation.WebListener;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//@WebListener
//public class DatabaseInitializer implements ServletContextListener {
//    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/demoservlet";
//    private static final String JDBC_USER = "root";
//    private static final String JDBC_PASSWORD = "Mysql#1234";
//    private static final Logger Log = LogManager.getLogger(DatabaseInitializer.class);
//
//    @Override
//    public void contextInitialized(ServletContextEvent sce) {
//        try {
//            Class.forName("com.mysql.cj.jdbc.Driver");
//            Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
//            sce.getServletContext().setAttribute("dbConnection", connection);
//            Log.info("Database connection initialized successfully.");
//        } catch (ClassNotFoundException | SQLException e) {
//            Log.error("Error initializing database connection: " + e.getMessage(), e);
//        }
//    }
//
//    @Override
//    public void contextDestroyed(ServletContextEvent sce) {
//        Connection connection = (Connection) sce.getServletContext().getAttribute("dbConnection");
//        if (connection != null) {
//            try {
//                connection.close();
//                Log.info("Database connection closed successfully.");
//            } catch (SQLException e) {
//                Log.error("Error closing database connection: " + e.getMessage(), e);
//            }
//        }
//    }
//}
