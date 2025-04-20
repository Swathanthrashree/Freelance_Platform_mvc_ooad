package com.freelance.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import com.freelance.util.DatabaseConnection;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;

public class FreelancerDashboardController {
    @FXML private VBox availableProjectsContainer;
    @FXML private VBox myBidsContainer;
    @FXML private VBox activeProjectsContainer;
    @FXML private VBox completedProjectsContainer;
    @FXML private VBox invoicesContainer;
    @FXML private VBox clientProfilesContainer;
    @FXML private ComboBox<String> skillComboBox;
    @FXML private ComboBox<String> proficiencyComboBox;
    @FXML private ListView<String> skillsList;
    @FXML private VBox ratingsContainer;
    @FXML private Button logoutButton;

    private int freelancerId;
    private final ObservableList<String> skills = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize skills list
        List<String> availableSkills = List.of("Python", "Java", "JavaScript", "SQL", "Machine Learning");
        skillComboBox.setItems(FXCollections.observableArrayList(availableSkills));
        
        // Initialize proficiency levels
        List<String> proficiencyLevels = List.of("Beginner", "Intermediate", "Expert");
        proficiencyComboBox.setItems(FXCollections.observableArrayList(proficiencyLevels));
        
        skillsList.setItems(skills);
        
        // Load initial data
        loadAvailableProjects();
        loadMyBids();
        loadActiveProjects();
        loadInvoices();
        loadSkills();
        loadRatings();
        loadClientProfiles();
    }

    @FXML
    private void addSkill() {
        String skill = skillComboBox.getValue();
        String proficiency = proficiencyComboBox.getValue();
        
        if (skill == null || proficiency == null) {
            showAlert("Error", "Please select both skill and proficiency level");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO freelancer_skills (Freelancer_ID, Skill_Name, Proficiency_Level) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, freelancerId);
            stmt.setString(2, skill);
            stmt.setString(3, proficiency);
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                showAlert("Success", "Skill added successfully!");
                loadSkills();
            }
        } catch (SQLException e) {
            showAlert("Error", "Error adding skill: " + e.getMessage());
        }
    }

    @FXML
    private void showInvoiceDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Generate Invoice");
        dialog.setHeaderText("Select a project to generate invoice");

        ComboBox<String> projectComboBox = new ComboBox<>();
        TextField amountField = new TextField();
        amountField.setPromptText("Enter amount");

        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("Project:"),
            projectComboBox,
            new Label("Amount:"),
            amountField
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Load completed projects
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT p.P_ID, p.Title FROM projects p " +
                          "JOIN bids b ON p.P_ID = b.P_ID " +
                          "WHERE b.Freelancer_ID = ? AND p.P_Status = 100 " +
                          "AND NOT EXISTS (SELECT 1 FROM invoices i WHERE i.P_ID = p.P_ID)";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, freelancerId);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                projectComboBox.getItems().add(rs.getInt("P_ID") + " - " + rs.getString("Title"));
            }
        } catch (SQLException e) {
            showAlert("Error", "Error loading projects: " + e.getMessage());
            return;
        }

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String selectedProject = projectComboBox.getValue();
            if (selectedProject == null || amountField.getText().isEmpty()) {
                showAlert("Error", "Please fill all fields");
                return;
            }

            try {
                int projectId = Integer.parseInt(selectedProject.split(" - ")[0]);
                double amount = Double.parseDouble(amountField.getText());

                generateInvoice(projectId, amount);
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid amount");
            }
        }
    }

    private void generateInvoice(int projectId, double amount) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO invoices (P_ID, Amount, Status, Issue_Date) VALUES (?, ?, 'Unpaid', ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, projectId);
            stmt.setDouble(2, amount);
            stmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                showAlert("Success", "Invoice generated successfully!");
                loadInvoices();
            }
        } catch (SQLException e) {
            showAlert("Error", "Error generating invoice: " + e.getMessage());
        }
    }

    private void loadAvailableProjects() {
        availableProjectsContainer.getChildren().clear();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT p.* FROM projects p " +
                          "WHERE p.P_Status = 0 " +
                          "AND NOT EXISTS (SELECT 1 FROM bids b WHERE b.P_ID = p.P_ID AND b.Freelancer_ID = ?)";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, freelancerId);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                VBox projectBox = createProjectBox(rs, true);
                availableProjectsContainer.getChildren().add(projectBox);
            }
        } catch (SQLException e) {
            showAlert("Error", "Error loading available projects: " + e.getMessage());
        }
    }

    private void loadMyBids() {
        myBidsContainer.getChildren().clear();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT p.*, b.* FROM projects p " +
                          "JOIN bids b ON p.P_ID = b.P_ID " +
                          "WHERE b.Freelancer_ID = ?";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, freelancerId);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                VBox bidBox = createBidBox(rs);
                myBidsContainer.getChildren().add(bidBox);
            }
        } catch (SQLException e) {
            showAlert("Error", "Error loading bids: " + e.getMessage());
        }
    }

    private void loadActiveProjects() {
        activeProjectsContainer.getChildren().clear();
        completedProjectsContainer.getChildren().clear();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT p.* FROM projects p " +
                          "JOIN bids b ON p.P_ID = b.P_ID " +
                          "WHERE b.Freelancer_ID = ? AND b.Bid_Status = 'Approved'";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, freelancerId);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                VBox projectBox = createProjectBox(rs, false);
                if (rs.getInt("P_Status") < 100) {
                    activeProjectsContainer.getChildren().add(projectBox);
                } else {
                    completedProjectsContainer.getChildren().add(projectBox);
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Error loading active projects: " + e.getMessage());
        }
    }

    private void loadInvoices() {
        invoicesContainer.getChildren().clear();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT i.*, p.Title FROM invoices i " +
                          "JOIN projects p ON i.P_ID = p.P_ID " +
                          "JOIN bids b ON p.P_ID = b.P_ID " +
                          "WHERE b.Freelancer_ID = ?";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, freelancerId);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                VBox invoiceBox = createInvoiceBox(rs);
                invoicesContainer.getChildren().add(invoiceBox);
            }
        } catch (SQLException e) {
            showAlert("Error", "Error loading invoices: " + e.getMessage());
        }
    }

    private void loadSkills() {
        skills.clear();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT Skill_Name, Proficiency_Level FROM freelancer_skills WHERE Freelancer_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, freelancerId);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                skills.add(rs.getString("Skill_Name") + " (" + rs.getString("Proficiency_Level") + ")");
            }
        } catch (SQLException e) {
            showAlert("Error", "Error loading skills: " + e.getMessage());
        }
    }

    private void loadRatings() {
        ratingsContainer.getChildren().clear();

        try (Connection conn = DatabaseConnection.getConnection()) {
            // First get average rating
            String avgQuery = "SELECT rating FROM users WHERE id = ? AND role = 'Freelancer'";
            PreparedStatement avgStmt = conn.prepareStatement(avgQuery);
            avgStmt.setInt(1, freelancerId);
            
            ResultSet avgRs = avgStmt.executeQuery();
            if (avgRs.next()) {
                double avgRating = avgRs.getDouble("rating");
                Label avgLabel = new Label(String.format("Average Rating: %.1f/5", avgRating));
                avgLabel.getStyleClass().add("rating-average");
                ratingsContainer.getChildren().add(avgLabel);
            }
        } catch (SQLException e) {
            showAlert("Error", "Error loading ratings: " + e.getMessage());
        }
    }

    private void loadClientProfiles() {
        clientProfilesContainer.getChildren().clear();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT DISTINCT u.id, u.username, u.email, " +
                          "COUNT(DISTINCT p.P_ID) as total_projects " +
                          "FROM users u " +
                          "LEFT JOIN projects p ON u.id = p.Client_ID " +
                          "WHERE u.role = 'Client' " +
                          "GROUP BY u.id, u.username, u.email " +
                          "ORDER BY u.username";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                VBox profileBox = createClientProfileBox(rs);
                clientProfilesContainer.getChildren().add(profileBox);
            }
        } catch (SQLException e) {
            showAlert("Error", "Error loading client profiles: " + e.getMessage());
        }
    }

    private VBox createProjectBox(ResultSet rs, boolean isAvailable) throws SQLException {
        VBox projectBox = new VBox(5);
        projectBox.getStyleClass().add("project-card");
        
        Label titleLabel = new Label("Title: " + rs.getString("Title"));
        Label descLabel = new Label("Description: " + rs.getString("Description"));
        Label budgetLabel = new Label("Budget: $" + rs.getDouble("Budget"));
        
        projectBox.getChildren().addAll(titleLabel, descLabel, budgetLabel);
        
        if (isAvailable) {
            TextField bidAmount = new TextField();
            bidAmount.setPromptText("Enter bid amount");
            DatePicker timeframe = new DatePicker();
            timeframe.setPromptText("Select completion date");
            Button bidButton = new Button("Submit Bid");
            
            final int projectId = rs.getInt("P_ID");
            bidButton.setOnAction(e -> submitBid(projectId, bidAmount, timeframe));
            projectBox.getChildren().addAll(bidAmount, timeframe, bidButton);
        } else {
            Label progressLabel = new Label("Progress: " + rs.getInt("P_Status") + "%");
            Slider progressSlider = new Slider(0, 100, rs.getInt("P_Status"));
            progressSlider.setShowTickLabels(true);
            progressSlider.setShowTickMarks(true);
            
            Button updateButton = new Button("Update Progress");
            final int projectId = rs.getInt("P_ID");
            updateButton.setOnAction(e -> updateProgress(projectId, (int) progressSlider.getValue()));
            
            projectBox.getChildren().addAll(progressLabel, progressSlider, updateButton);
        }
        
        return projectBox;
    }

    private VBox createBidBox(ResultSet rs) throws SQLException {
        VBox bidBox = new VBox(5);
        bidBox.getStyleClass().add("bid-card");
        
        Label projectLabel = new Label("Project: " + rs.getString("Title"));
        Label amountLabel = new Label("Bid Amount: $" + rs.getDouble("Amount"));
        Label statusLabel = new Label("Status: " + rs.getString("Bid_Status"));
        Label timeframeLabel = new Label("Completion Date: " + rs.getDate("Timeframe"));
        
        bidBox.getChildren().addAll(projectLabel, amountLabel, statusLabel, timeframeLabel);
        return bidBox;
    }

    private VBox createInvoiceBox(ResultSet rs) throws SQLException {
        VBox invoiceBox = new VBox(5);
        invoiceBox.getStyleClass().add("invoice-card");
        
        Label projectLabel = new Label("Project: " + rs.getString("Title"));
        Label amountLabel = new Label("Amount: $" + rs.getDouble("Amount"));
        Label statusLabel = new Label("Status: " + rs.getString("Status"));
        Label dateLabel = new Label("Issue Date: " + rs.getDate("Issue_Date"));
        
        invoiceBox.getChildren().addAll(projectLabel, amountLabel, statusLabel, dateLabel);
        return invoiceBox;
    }

    private VBox createClientProfileBox(ResultSet rs) throws SQLException {
        VBox profileBox = new VBox(10);
        profileBox.getStyleClass().add("profile-card");
        profileBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-border-color: #dee2e6; -fx-border-radius: 5;");

        Label nameLabel = new Label("Name: " + rs.getString("username"));
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        Label emailLabel = new Label("Email: " + rs.getString("email"));
        
        Label projectsLabel = new Label("Total Projects Posted: " + rs.getInt("total_projects"));

        profileBox.getChildren().addAll(nameLabel, emailLabel, projectsLabel);
        return profileBox;
    }

    private void submitBid(int projectId, TextField amountField, DatePicker timeframe) {
        try {
            double amount = Double.parseDouble(amountField.getText());
            LocalDate date = timeframe.getValue();
            
            if (date == null) {
                showAlert("Error", "Please select a completion date");
                return;
            }
            
            if (date.isBefore(LocalDate.now())) {
                showAlert("Error", "Completion date cannot be in the past");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "INSERT INTO bids (P_ID, Freelancer_ID, Amount, Timeframe, Bid_Status) VALUES (?, ?, ?, ?, 'Pending')";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, projectId);
                stmt.setInt(2, freelancerId);
                stmt.setDouble(3, amount);
                stmt.setDate(4, java.sql.Date.valueOf(date));
                
                int result = stmt.executeUpdate();
                if (result > 0) {
                    showAlert("Success", "Bid submitted successfully!");
                    loadAvailableProjects();
                    loadMyBids();
                }
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid amount");
        } catch (SQLException e) {
            showAlert("Error", "Error submitting bid: " + e.getMessage());
        }
    }

    private void updateProgress(int projectId, int progress) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE projects SET P_Status = ? WHERE P_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, progress);
            stmt.setInt(2, projectId);
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                showAlert("Success", "Progress updated successfully!");
                loadActiveProjects();
            }
        } catch (SQLException e) {
            showAlert("Error", "Error updating progress: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setFreelancerId(int freelancerId) {
        this.freelancerId = freelancerId;
        loadAvailableProjects();
        loadMyBids();
        loadActiveProjects();
        loadInvoices();
        loadSkills();
        loadRatings();
        loadClientProfiles();
    }

    @FXML
    private void handleLogout() {
        try {
            // Get the current stage
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            
            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/freelance/view/login.fxml"));
            Parent root = loader.load();
            
            // Create new scene with login view
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/freelance/styles/styles.css").toExternalForm());
            
            // Set the scene on current stage
            currentStage.setScene(scene);
            currentStage.show();
        } catch (IOException e) {
            showAlert("Error", "Could not load login page: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 