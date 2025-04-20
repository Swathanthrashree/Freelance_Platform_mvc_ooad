package com.freelance.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import java.io.IOException;
import java.sql.*;
import com.freelance.util.DatabaseConnection;

public class RegisterController {
    @FXML private TextField username;
    @FXML private TextField email;
    @FXML private PasswordField password;
    @FXML private PasswordField confirmPassword;
    @FXML private ComboBox<String> roleComboBox;

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList("Client", "Freelancer"));
    }

    @FXML
    private void handleRegister() {
        if (!validateInput()) {
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if username already exists
            String checkQuery = "SELECT id FROM users WHERE username = ? OR email = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, username.getText());
            checkStmt.setString(2, email.getText());
            
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                showAlert("Error", "Username or email already exists");
                return;
            }

            // Insert new user
            String insertQuery = "INSERT INTO users (username, email, password, role) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setString(1, username.getText());
            insertStmt.setString(2, email.getText());
            insertStmt.setString(3, password.getText());
            insertStmt.setString(4, roleComboBox.getValue());

            int result = insertStmt.executeUpdate();
            if (result > 0) {
                showAlert("Success", "Registration successful! Please login.");
                loadLoginPage();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Error registering user: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackToLogin() {
        loadLoginPage();
    }

    private boolean validateInput() {
        if (username.getText().isEmpty() || email.getText().isEmpty() || 
            password.getText().isEmpty() || confirmPassword.getText().isEmpty() || 
            roleComboBox.getValue() == null) {
            showAlert("Error", "Please fill in all fields");
            return false;
        }

        if (!email.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert("Error", "Please enter a valid email address");
            return false;
        }

        if (!password.getText().equals(confirmPassword.getText())) {
            showAlert("Error", "Passwords do not match");
            return false;
        }

        if (password.getText().length() < 6) {
            showAlert("Error", "Password must be at least 6 characters long");
            return false;
        }

        return true;
    }

    private void loadLoginPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/freelance/view/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/freelance/styles/styles.css").toExternalForm());
            
            Stage currentStage = (Stage) username.getScene().getWindow();
            currentStage.setTitle("Freelance Platform - Login");
            currentStage.setScene(scene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load login page: " + e.getMessage());
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