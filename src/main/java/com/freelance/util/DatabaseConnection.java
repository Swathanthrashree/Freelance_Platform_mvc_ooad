package com.freelance.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/freelance_platform?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&autoReconnect=true";
    private static final String USER = "root";
    private static final String PASSWORD = "champs#123";

    public static Connection getConnection() throws SQLException {
        try {
            System.out.println("Attempting to connect to database with URL: " + URL);
            System.out.println("Username: " + USER);
            System.out.println("Password length: " + PASSWORD.length());
            
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver loaded successfully");
            
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connected successfully");
            return conn;
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found: " + e.getMessage());
            throw new SQLException("Database driver not found", e);
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            throw e;
        }
    }
} 