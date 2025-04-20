package com.freelance.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Feedback extends BaseModel {
    private int id;
    private int projectId;
    private int fromUserId;
    private int toUserId;
    private int rating;
    private String comment;
    private String createdAt;

    public Feedback() {
        super();
    }

    public Feedback(int projectId, int fromUserId, int toUserId, int rating, String comment) {
        super();
        this.projectId = projectId;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.rating = rating;
        this.comment = comment;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }
    public int getFromUserId() { return fromUserId; }
    public void setFromUserId(int fromUserId) { this.fromUserId = fromUserId; }
    public int getToUserId() { return toUserId; }
    public void setToUserId(int toUserId) { this.toUserId = toUserId; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // Database operations
    public boolean save() {
        try {
            String query = "INSERT INTO feedback (project_id, from_user_id, to_user_id, rating, comment) VALUES (?, ?, ?, ?, ?)";
            int result = executeUpdate(query, projectId, fromUserId, toUserId, rating, comment);
            if (result > 0) {
                System.out.println("Feedback saved successfully for project: " + projectId);
                return true;
            } else {
                System.out.println("Failed to save feedback for project: " + projectId);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error saving feedback: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Feedback> findByUserId(int userId) {
        List<Feedback> feedbacks = new ArrayList<>();
        try {
            String query = "SELECT * FROM feedback WHERE to_user_id = ?";
            ResultSet rs = executeQuery(query, userId);
            while (rs.next()) {
                Feedback feedback = new Feedback();
                feedback.setId(rs.getInt("id"));
                feedback.setProjectId(rs.getInt("project_id"));
                feedback.setFromUserId(rs.getInt("from_user_id"));
                feedback.setToUserId(rs.getInt("to_user_id"));
                feedback.setRating(rs.getInt("rating"));
                feedback.setComment(rs.getString("comment"));
                feedback.setCreatedAt(rs.getString("created_at"));
                feedbacks.add(feedback);
            }
        } catch (SQLException e) {
            System.out.println("Error finding feedback: " + e.getMessage());
            e.printStackTrace();
        }
        return feedbacks;
    }

    public double getAverageRating(int userId) {
        try {
            String query = "SELECT AVG(rating) as avg_rating FROM feedback WHERE to_user_id = ?";
            ResultSet rs = executeQuery(query, userId);
            if (rs.next()) {
                return rs.getDouble("avg_rating");
            }
        } catch (SQLException e) {
            System.out.println("Error calculating average rating: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }
} 