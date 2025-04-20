package com.freelance.model;

import java.sql.*;
import java.time.LocalDate;
import com.freelance.util.DatabaseConnection;
import java.util.ArrayList;
import java.util.List;

public class Project {
    private int projectId;
    private int clientId;
    private String title;
    private String description;
    private double budget;
    private LocalDate deadline;
    private int status;

    public Project() {}

    public Project(int clientId, String title, String description, double budget, LocalDate deadline) {
        this.clientId = clientId;
        this.title = title;
        this.description = description;
        this.budget = budget;
        this.deadline = deadline;
        this.status = 0;
    }

    // Constructor for ProjectController
    public Project(String title, String description, double budget, int clientId) {
        this.title = title;
        this.description = description;
        this.budget = budget;
        this.clientId = clientId;
        this.deadline = LocalDate.now().plusMonths(1); // Default deadline of 1 month
        this.status = 0;
    }

    // Getters and Setters
    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }
    public int getClientId() { return clientId; }
    public void setClientId(int clientId) { this.clientId = clientId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    // Database operations
    public boolean save() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO Project (Client_ID, Title, Description, Budget, Deadline, P_Status) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, clientId);
            stmt.setString(2, title);
            stmt.setString(3, description);
            stmt.setDouble(4, budget);
            stmt.setDate(5, Date.valueOf(deadline));
            stmt.setInt(6, status);

            int result = stmt.executeUpdate();
            if (result > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    this.projectId = rs.getInt(1);
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Project findById(int projectId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM projects WHERE P_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, projectId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Project project = new Project();
                project.setProjectId(rs.getInt("P_ID"));
                project.setClientId(rs.getInt("Client_ID"));
                project.setTitle(rs.getString("Title"));
                project.setDescription(rs.getString("Description"));
                project.setBudget(rs.getDouble("Budget"));
                project.setDeadline(rs.getDate("Deadline").toLocalDate());
                project.setStatus(rs.getInt("P_Status"));
                return project;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateStatus(int newStatus) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE Project SET P_Status = ? WHERE P_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, newStatus);
            stmt.setInt(2, projectId);

            int result = stmt.executeUpdate();
            if (result > 0) {
                this.status = newStatus;
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Project> findAll() {
        List<Project> projects = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM projects";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Project project = new Project();
                project.setProjectId(rs.getInt("P_ID"));
                project.setClientId(rs.getInt("Client_ID"));
                project.setTitle(rs.getString("Title"));
                project.setDescription(rs.getString("Description"));
                project.setBudget(rs.getDouble("Budget"));
                project.setDeadline(rs.getDate("Deadline").toLocalDate());
                project.setStatus(rs.getInt("P_Status"));
                projects.add(project);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return projects;
    }

    public List<Project> findByClientId(int clientId) {
        List<Project> projects = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM projects WHERE Client_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Project project = new Project();
                project.setProjectId(rs.getInt("P_ID"));
                project.setClientId(rs.getInt("Client_ID"));
                project.setTitle(rs.getString("Title"));
                project.setDescription(rs.getString("Description"));
                project.setBudget(rs.getDouble("Budget"));
                project.setDeadline(rs.getDate("Deadline").toLocalDate());
                project.setStatus(rs.getInt("P_Status"));
                projects.add(project);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return projects;
    }
} 