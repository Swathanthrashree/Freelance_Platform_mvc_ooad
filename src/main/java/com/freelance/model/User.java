package com.freelance.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class User extends BaseModel {
    private int id;
    private String username;
    private String email;
    private String password;
    private String role;

    public User() {
        super();
    }

    public User(String username, String email, String password, String role) {
        super();
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // Database operations
    public boolean save() {
        if (username == null || email == null || password == null || role == null) {
            System.out.println("Error: Missing required fields");
            System.out.println("Username: " + username);
            System.out.println("Email: " + email);
            System.out.println("Password: " + (password != null ? "set" : "null"));
            System.out.println("Role: " + role);
            return false;
        }

        try {
            // First check if email already exists
            String checkQuery = "SELECT COUNT(*) FROM users WHERE email = ?";
            ResultSet rs = executeQuery(checkQuery, email);
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Error: Email already exists: " + email);
                return false;
            }

            // If email doesn't exist, proceed with insert
            String query = "INSERT INTO users (username, email, password, role) VALUES (?, ?, ?, ?)";
            System.out.println("Executing query: " + query);
            System.out.println("With values: username=" + username + ", email=" + email + 
                             ", password=<hidden>, role=" + role);
            
            int result = executeUpdate(query, username, email, password, role);
            if (result > 0) {
                System.out.println("User saved successfully: " + username);
                // Get the generated ID
                String idQuery = "SELECT id FROM users WHERE email = ?";
                ResultSet idRs = executeQuery(idQuery, email);
                if (idRs.next()) {
                    this.id = idRs.getInt("id");
                    System.out.println("Generated ID: " + this.id);
                }
                return true;
            } else {
                System.out.println("Failed to save user: No rows affected");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error saving user: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        }
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try {
            String query = "SELECT * FROM users";
            ResultSet rs = executeQuery(query);
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                users.add(user);
            }
        } catch (SQLException e) {
            System.out.println("Error finding users: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    public User findByEmail(String email) {
        try {
            String query = "SELECT * FROM users WHERE email = ?";
            System.out.println("Finding user by email: " + email);
            ResultSet rs = executeQuery(query, email);
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                System.out.println("Found user: " + user.getUsername());
                return user;
            } else {
                System.out.println("No user found with email: " + email);
            }
        } catch (SQLException e) {
            System.out.println("Error finding user: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
} 