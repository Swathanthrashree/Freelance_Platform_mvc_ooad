package com.freelance.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.application.Platform;
import java.io.IOException;
import java.sql.*;
import com.freelance.util.DatabaseConnection;

public class LoginController {
    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private ComboBox<String> roleComboBox;

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList("Client", "Freelancer"));
    }

    @FXML
    private void handleLogin() {
        String user = username.getText();
        String pass = password.getText();
        String role = roleComboBox.getValue();

        if (user.isEmpty() || pass.isEmpty() || role == null) {
            showAlert("Error", "Please fill in all fields");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("Attempting login for user: " + user); // Debug print
            
            // Print the actual query and values for debugging
            String query = "SELECT id, role FROM users WHERE username = ? AND password = ? AND LOWER(role) = LOWER(?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, user);
            stmt.setString(2, pass);
            stmt.setString(3, role);
            
            System.out.println("Query: " + query); // Debug print
            System.out.println("Role selected: " + role); // Debug print

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("id");
                String dbRole = rs.getString("role");
                System.out.println("Login successful. User ID: " + userId + ", Role: " + dbRole); // Debug print
                loadDashboard(dbRole, userId);
            } else {
                System.out.println("Login failed - no matching credentials"); // Debug print
                showAlert("Error", "Invalid credentials");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database error: " + e.getMessage());
        }
    }

    private void loadDashboard(String role, int userId) {
        try {
            String fxmlFile = role.equalsIgnoreCase("Client") ? "client_dashboard.fxml" : "freelancer_dashboard.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/freelance/view/" + fxmlFile));
            Parent root = loader.load();

            if (role.equalsIgnoreCase("Client")) {
                ClientDashboardController controller = loader.getController();
                controller.setClientId(userId);
            } else {
                FreelancerDashboardController controller = loader.getController();
                controller.setFreelancerId(userId);
            }

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/freelance/styles/styles.css").toExternalForm());
            
            Stage currentStage = (Stage) username.getScene().getWindow();
            currentStage.setTitle("Freelance Platform - " + role);
            currentStage.setScene(scene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/freelance/view/register.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/freelance/styles/styles.css").toExternalForm());
            
            Stage currentStage = (Stage) username.getScene().getWindow();
            currentStage.setTitle("Freelance Platform - Register");
            currentStage.setScene(scene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load registration page: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}