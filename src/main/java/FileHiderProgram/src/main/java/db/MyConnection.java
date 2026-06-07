package FileHiderProgram.src.main.java.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MyConnection {
    public static Connection connection = null;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String DB_URL_WITH_DB = "jdbc:mysql://localhost:3306/ytproject?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "Your_Password";
    private static boolean initialized = false;

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            if (!initialized) {
                initializeDatabase();
                initialized = true;
            }

            connection = DriverManager.getConnection(DB_URL_WITH_DB, USER, PASSWORD);
            System.out.println("Connection ho gya saab");

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    private static void initializeDatabase() throws SQLException {
        try (Connection tempConn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             Statement stmt = tempConn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS OOP_Project");
            stmt.executeUpdate("USE OOP_Project");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS users (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "name VARCHAR(100) NOT NULL, " +
                            "email VARCHAR(100) UNIQUE NOT NULL, " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS data (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "name VARCHAR(255), " +
                            "path VARCHAR(255), " +
                            "email VARCHAR(100), " +
                            "bin_data LONGTEXT, " +
                            "uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}