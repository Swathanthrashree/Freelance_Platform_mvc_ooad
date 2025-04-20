package com.freelance.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Proposal extends BaseModel {
    private int id;
    private int projectId;
    private int freelancerId;
    private double amount;
    private String description;
    private String status;
    private String createdAt;

    public Proposal() {
        super();
    }

    public Proposal(int projectId, int freelancerId, double amount, String description) {
        super();
        this.projectId = projectId;
        this.freelancerId = freelancerId;
        this.amount = amount;
        this.description = description;
        this.status = "Pending";
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }
    public int getFreelancerId() { return freelancerId; }
    public void setFreelancerId(int freelancerId) { this.freelancerId = freelancerId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // Database operations
    public boolean save() {
        try {
            String query = "INSERT INTO proposals (project_id, freelancer_id, amount, description, status) VALUES (?, ?, ?, ?, ?)";
            int result = executeUpdate(query, projectId, freelancerId, amount, description, status);
            if (result > 0) {
                System.out.println("Proposal saved successfully for project: " + projectId);
                return true;
            } else {
                System.out.println("Failed to save proposal for project: " + projectId);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error saving proposal: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStatus(String newStatus) {
        try {
            String query = "UPDATE proposals SET status = ? WHERE id = ?";
            int result = executeUpdate(query, newStatus, id);
            if (result > 0) {
                this.status = newStatus;
                System.out.println("Proposal status updated successfully: " + newStatus);
                return true;
            } else {
                System.out.println("Failed to update proposal status");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error updating proposal status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Proposal> findByProjectId(int projectId) {
        List<Proposal> proposals = new ArrayList<>();
        try {
            String query = "SELECT * FROM proposals WHERE project_id = ?";
            ResultSet rs = executeQuery(query, projectId);
            while (rs.next()) {
                Proposal proposal = new Proposal();
                proposal.setId(rs.getInt("id"));
                proposal.setProjectId(rs.getInt("project_id"));
                proposal.setFreelancerId(rs.getInt("freelancer_id"));
                proposal.setAmount(rs.getDouble("amount"));
                proposal.setDescription(rs.getString("description"));
                proposal.setStatus(rs.getString("status"));
                proposal.setCreatedAt(rs.getString("created_at"));
                proposals.add(proposal);
            }
        } catch (SQLException e) {
            System.out.println("Error finding proposals: " + e.getMessage());
            e.printStackTrace();
        }
        return proposals;
    }

    public List<Proposal> findByFreelancerId(int freelancerId) {
        List<Proposal> proposals = new ArrayList<>();
        try {
            String query = "SELECT * FROM proposals WHERE freelancer_id = ?";
            ResultSet rs = executeQuery(query, freelancerId);
            while (rs.next()) {
                Proposal proposal = new Proposal();
                proposal.setId(rs.getInt("id"));
                proposal.setProjectId(rs.getInt("project_id"));
                proposal.setFreelancerId(rs.getInt("freelancer_id"));
                proposal.setAmount(rs.getDouble("amount"));
                proposal.setDescription(rs.getString("description"));
                proposal.setStatus(rs.getString("status"));
                proposal.setCreatedAt(rs.getString("created_at"));
                proposals.add(proposal);
            }
        } catch (SQLException e) {
            System.out.println("Error finding proposals: " + e.getMessage());
            e.printStackTrace();
        }
        return proposals;
    }
} 