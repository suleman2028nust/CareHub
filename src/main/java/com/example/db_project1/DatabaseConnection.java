package com.example.db_project1;

import java.sql.*;

public class DatabaseConnection {
    private static Connection connection;

    private static final String URL = "jdbc:mysql://127.0.0.1:3306/hms";
    private static final String USER = "root";
    private static final String PASSWORD = "mysql2024";

    // Static block to load the driver once when the class loads
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        }
    }

    // Private constructor to prevent instantiation
    DatabaseConnection() {
    }

    // Get connection, create it if null or closed
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database connection established.");
            }
        } catch (SQLException e) {
            System.err.println("Failed to establish database connection: " + e.getMessage());
            return null;
        }
        return connection;
    }

    // Close the connection if open
    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("Database connection closed.");
                }
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }

    // Execute query and return ResultSet
    public static ResultSet executeQuery(String query) {
        try {
            Connection conn = getConnection();
            if (conn == null) {
                System.err.println("No database connection available.");
                return null;
            }
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
            return null;
        }
    }

    // Execute update (INSERT, UPDATE, DELETE)
    public static int executeUpdate(String query) {
        try {
            Connection conn = getConnection();
            if (conn == null) {
                System.err.println("No database connection available.");
                return -1;
            }
            Statement stmt = conn.createStatement();
            return stmt.executeUpdate(query);
        } catch (SQLException e) {
            System.err.println("Error executing update: " + e.getMessage());
            return -1;
        }
    }

    // Execute prepared query with parameters
    public static ResultSet executePreparedQuery(String query, Object... params) {
        try {
            Connection conn = getConnection();
            if (conn == null) {
                System.err.println("No database connection available.");
                return null;
            }
            PreparedStatement pstmt = conn.prepareStatement(query);

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            return pstmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("Error executing prepared query: " + e.getMessage());
            return null;
        }
    }

    // Execute prepared update with parameters
    public static int executePreparedUpdate(String query, Object... params) {
        try {
            Connection conn = getConnection();
            if (conn == null) {
                System.err.println("No database connection available.");
                return -1;
            }
            PreparedStatement pstmt = conn.prepareStatement(query);

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error executing prepared update: " + e.getMessage());
            return -1;
        }
    }
}
