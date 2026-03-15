package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton that provides a shared JDBC connection to MySQL.
 */
public class DBConnection {

    private static final String DB_URL  = "jdbc:mysql://localhost:3306/shms_db"
                                        + "?useSSL=false"
                                        + "&serverTimezone=UTC"
                                        + "&allowPublicKeyRetrieval=true";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "@Samarmaharjan49#"; // Update to your local password.

    private static DBConnection instance;
    private Connection connection;

    private DBConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("[DB] Connected to MySQL successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] MySQL driver JAR not found in lib/.");
        } catch (SQLException e) {
            System.err.println("[DB] Connection failed: " + e.getMessage());
            System.err.println("[DB] Check MySQL is running and credentials are correct.");
        }
    }

    public static DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    /**
     * Returns the active JDBC connection, reopening it if needed.
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("[DB] Reconnecting...");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("[DB] Reconnect failed: " + e.getMessage());
        }
        return connection;
    }

    /**
     * Closes the connection; safe to call multiple times.
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error closing connection: " + e.getMessage());
        }
    }
}
