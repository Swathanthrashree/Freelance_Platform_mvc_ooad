package com.freelance.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    private static final String URL = "jdbc:mysql://localhost:3306/freelance_platform?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&autoReconnect=true";
    private static final String USER = "root";
    private static final String PASSWORD = "champs#123";

    private static Connection connection = null;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                System.out.println("Attempting to connect to database with URL: " + URL);
                System.out.println("Username: " + USER);
                System.out.println("Password length: " + PASSWORD.length());
                
                Class.forName("com.mysql.cj.jdbc.Driver");
                System.out.println("MySQL JDBC Driver loaded successfully");
                
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database connected successfully");
            } catch (ClassNotFoundException e) {
                System.out.println("MySQL JDBC Driver not found: " + e.getMessage());
                e.printStackTrace();
            } catch (SQLException e) {
                System.out.println("Database connection failed: " + e.getMessage());
                System.out.println("SQL State: " + e.getSQLState());
                System.out.println("Error Code: " + e.getErrorCode());
                e.printStackTrace();
                
                // Close connection if it was partially established
                if (connection != null) {
                    try {
                        connection.close();
                        connection = null;
                    } catch (SQLException ex) {
                        System.out.println("Error closing failed connection: " + ex.getMessage());
                    }
                }
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Database connection closed");
            } catch (SQLException e) {
                System.out.println("Error closing database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
} 