package com.freelance.controller;

import com.freelance.model.Project;
import com.freelance.model.Proposal;
import com.freelance.model.Feedback;
import com.freelance.util.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;

public class ClientDashboardController {
    @FXML private ComboBox<String> skillComboBox;
    @FXML private ListView<String> selectedSkillsList;
    @FXML private VBox freelancerResults;
    @FXML private TextField projectTitle;
    @FXML private TextArea projectDescription;
    @FXML private TextField projectBudget;
    @FXML private DatePicker projectDeadline;
    @FXML private VBox activeProjectsContainer;
    @FXML private VBox completedProjectsContainer;
    @FXML private VBox proposalsContainer;
    @FXML private VBox projectProgressContainer;
    @FXML private VBox invoicesContainer;
    @FXML private ComboBox<String> freelancerComboBox;
    @FXML private Slider ratingSlider;
    @FXML private Label ratingValue;
    @FXML private TextArea ratingComment;
    @FXML private Button logoutButton;
    @FXML private VBox freelancerProfilesContainer;

    private ObservableList<String> selectedSkills = FXCollections.observableArrayList();
    private int clientId; // Set this when user logs in

    @FXML
    public void initialize() {
        // Initialize skills list
        List<String> skills = List.of("Python", "Java", "JavaScript", "SQL", "Machine Learning");
        skillComboBox.setItems(FXCollections.observableArrayList(skills));
        selectedSkillsList.setItems(selectedSkills);

        // Initialize rating slider
        ratingSlider.valueProperty().addListener((obs, oldVal, newVal) -> 
            ratingValue.setText(String.format("%.0f", newVal.doubleValue())));

        // Load initial data
        loadProjects();
        loadProposals();
        loadInvoices();
        loadFreelancersForRating();
        loadFreelancerProfiles();
    }

    @FXML
    private void addSkill() {
        String skill = skillComboBox.getValue();
        if (skill != null && !selectedSkills.contains(skill)) {
            selectedSkills.add(skill);
        }
    }

    @FXML
    private void searchFreelancers() {
        freelancerResults.getChildren().clear();
        if (selectedSkills.isEmpty()) {
            showAlert("Error", "Please select at least one skill");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            StringBuilder query = new StringBuilder(
                "SELECT DISTINCT u.id, u.username, fs.Skill_Name, fs.Proficiency_Level " +
                "FROM users u " +
                "JOIN freelancer_skills fs ON u.id = fs.Freelancer_ID " +
                "WHERE u.role = 'Freelancer' AND fs.Skill_Name IN ("
            );
            
            for (int i = 0; i < selectedSkills.size(); i++) {
                query.append(i > 0 ? ", ?" : "?");
            }
            query.append(") ORDER BY u.username");

            PreparedStatement stmt = conn.prepareStatement(query.toString());
            for (int i = 0; i < selectedSkills.size(); i++) {
                stmt.setString(i + 1, selectedSkills.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                VBox freelancerBox = new VBox(5);
                freelancerBox.getStyleClass().add("freelancer-card");
                
                Label nameLabel = new Label(rs.getString("username"));
                Label skillLabel = new Label("Skill: " + rs.getString("Skill_Name") + 
                                          " (" + rs.getString("Proficiency_Level") + ")");
                
                freelancerBox.getChildren().addAll(nameLabel, skillLabel);
                freelancerResults.getChildren().add(freelancerBox);
            }
        } catch (SQLException e) {
            showAlert("Error", "Error searching freelancers: " + e.getMessage());
        }
    }

    @FXML
    private void postProject() {
        if (!validateProjectInput()) {
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO projects (Client_ID, Title, Description, Budget, Deadline, P_Status) " +
                          "VALUES (?, ?, ?, ?, ?, 0)";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, clientId);
            stmt.setString(2, projectTitle.getText());
            stmt.setString(3, projectDescription.getText());
            stmt.setDouble(4, Double.parseDouble(projectBudget.getText()));
            stmt.setDate(5, java.sql.Date.valueOf(projectDeadline.getValue()));
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                showAlert("Success", "Project posted successfully!");
                clearProjectForm();
                loadProjects();
            }
        } catch (SQLException e) {
            showAlert("Error", "Error posting project: " + e.getMessage());
        }
    }

    @FXML
    private void submitRating() {
        String selectedFreelancer = freelancerComboBox.getValue();
        if (selectedFreelancer == null) {
            showAlert("Error", "Please select a freelancer");
            return;
        }

        int freelancerId = Integer.parseInt(selectedFreelancer.split(" - ")[0]);
        int rating = (int) ratingSlider.getValue();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE users SET rating = " +
                          "(SELECT AVG(r.rating) FROM (SELECT ? as rating UNION ALL " +
                          "SELECT rating FROM users WHERE id = ? AND role = 'Freelancer') r) " +
                          "WHERE id = ? AND role = 'Freelancer'";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, rating);
            stmt.setInt(2, freelancerId);
            stmt.setInt(3, freelancerId);
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                showAlert("Success", "Rating submitted successfully!");
                ratingSlider.setValue(3);
                ratingComment.clear();
            }
        } catch (SQLException e) {
            showAlert("Error", "Error submitting rating: " + e.getMessage());
        }
    }

    private void loadProjects() {
        activeProjectsContainer.getChildren().clear();
        completedProjectsContainer.getChildren().clear();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM projects WHERE Client_ID = ? ORDER BY P_Status DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, clientId);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                VBox projectBox = createProjectBox(rs);
                if (rs.getInt("P_Status") < 100) {
                    activeProjectsContainer.getChildren().add(projectBox);
                } else {
                    completedProjectsContainer.getChildren().add(projectBox);
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Error loading projects: " + e.getMessage());
        }
    }

    private void loadProposals() {
        proposalsContainer.getChildren().clear();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT p.*, b.* FROM projects p " +
                          "JOIN bids b ON p.P_ID = b.P_ID " +
                          "WHERE p.Client_ID = ? ORDER BY b.Bid_Status";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, clientId);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                VBox proposalBox = createProposalBox(rs);
                proposalsContainer.getChildren().add(proposalBox);
            }
        } catch (SQLException e) {
            showAlert("Error", "Error loading proposals: " + e.getMessage());
        }
    }

    private void loadInvoices() {
        invoicesContainer.getChildren().clear();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT i.*, p.Title FROM invoices i " +
                          "JOIN projects p ON i.P_ID = p.P_ID " +
                          "WHERE p.Client_ID = ? ORDER BY i.Issue_Date DESC";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, clientId);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                VBox invoiceBox = createInvoiceBox(rs);
                invoicesContainer.getChildren().add(invoiceBox);
            }
        } catch (SQLException e) {
            showAlert("Error", "Error loading invoices: " + e.getMessage());
        }
    }

    private void loadFreelancersForRating() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT DISTINCT u.id, u.username " +
                          "FROM users u " +
                          "JOIN bids b ON u.id = b.Freelancer_ID " +
                          "JOIN projects p ON b.P_ID = p.P_ID " +
                          "WHERE u.role = 'Freelancer' " +
                          "AND p.Client_ID = ? " +
                          "AND b.Bid_Status = 'Approved' " +
                          "AND p.P_Status = 100 " +  // Only completed projects
                          "ORDER BY u.username";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, clientId);
            
            ResultSet rs = stmt.executeQuery();
            List<String> freelancers = new ArrayList<>();
            while (rs.next()) {
                freelancers.add(rs.getInt("id") + " - " + rs.getString("username"));
            }
            freelancerComboBox.setItems(FXCollections.observableArrayList(freelancers));
        } catch (SQLException e) {
            showAlert("Error", "Error loading freelancers: " + e.getMessage());
        }
    }

    private void loadFreelancerProfiles() {
        freelancerProfilesContainer.getChildren().clear();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT DISTINCT u.id, u.username, u.email, u.rating, " +
                          "GROUP_CONCAT(CONCAT(fs.Skill_Name, ' (', fs.Proficiency_Level, ')') SEPARATOR ', ') as skills " +
                          "FROM users u " +
                          "LEFT JOIN freelancer_skills fs ON u.id = fs.Freelancer_ID " +
                          "WHERE u.role = 'Freelancer' " +
                          "GROUP BY u.id, u.username, u.email, u.rating " +
                          "ORDER BY u.username";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                VBox profileBox = createFreelancerProfileBox(rs);
                freelancerProfilesContainer.getChildren().add(profileBox);
            }
        } catch (SQLException e) {
            showAlert("Error", "Error loading freelancer profiles: " + e.getMessage());
        }
    }

    private VBox createProjectBox(ResultSet rs) throws SQLException {
        VBox projectBox = new VBox(5);
        projectBox.getStyleClass().add("project-card");
        
        Label titleLabel = new Label("Title: " + rs.getString("Title"));
        Label descLabel = new Label("Description: " + rs.getString("Description"));
        Label budgetLabel = new Label("Budget: $" + rs.getDouble("Budget"));
        Label progressLabel = new Label("Progress: " + rs.getInt("P_Status") + "%");
        ProgressBar progressBar = new ProgressBar(rs.getInt("P_Status") / 100.0);
        
        projectBox.getChildren().addAll(titleLabel, descLabel, budgetLabel, progressLabel, progressBar);
        return projectBox;
    }

    private VBox createProposalBox(ResultSet rs) throws SQLException {
        VBox proposalBox = new VBox(10);
        proposalBox.getStyleClass().add("proposal-card");

        Label projectLabel = new Label("Project: " + rs.getString("Title"));
        Label bidAmountLabel = new Label("Bid Amount: $" + rs.getDouble("Amount"));
        Label dateLabel = new Label("Date: " + rs.getTimestamp("Created_At").toString());
        Label statusLabel = new Label("Status: " + rs.getString("Bid_Status"));
        Label timeframeLabel = new Label("Timeframe: " + rs.getDate("Timeframe").toString());

        // Store the bid ID before creating the lambda
        final int bidId = rs.getInt("Bid_ID");
        Button approveButton = new Button("Approve");
        approveButton.setOnAction(e -> approveBid(bidId));
        approveButton.setVisible("Pending".equals(rs.getString("Bid_Status")));

        proposalBox.getChildren().addAll(projectLabel, bidAmountLabel, dateLabel, timeframeLabel, statusLabel);
        if ("Pending".equals(rs.getString("Bid_Status"))) {
            proposalBox.getChildren().add(approveButton);
        }

        return proposalBox;
    }

    private VBox createInvoiceBox(ResultSet rs) throws SQLException {
        VBox invoiceBox = new VBox(10);
        invoiceBox.getStyleClass().add("invoice-card");

        Label projectLabel = new Label("Project: " + rs.getString("Title"));
        Label amountLabel = new Label("Amount: $" + rs.getDouble("Amount"));
        Label statusLabel = new Label("Status: " + rs.getString("Status"));
        Label issueDateLabel = new Label("Issue Date: " + rs.getDate("Issue_Date").toString());

        // Store the invoice ID before creating the lambda
        final int invoiceId = rs.getInt("Invoice_ID");
        Button paidButton = new Button("Mark as Paid");
        paidButton.setVisible("Unpaid".equals(rs.getString("Status")));
        paidButton.setOnAction(e -> markInvoiceAsPaid(invoiceId));

        invoiceBox.getChildren().addAll(projectLabel, amountLabel, statusLabel, issueDateLabel);
        if ("Unpaid".equals(rs.getString("Status"))) {
            invoiceBox.getChildren().add(paidButton);
        }

        return invoiceBox;
    }

    private void approveBid(int bidId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE bids SET Bid_Status = 'Approved' WHERE Bid_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, bidId);
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                showAlert("Success", "Bid approved successfully!");
                loadProposals();
            }
        } catch (SQLException e) {
            showAlert("Error", "Error approving bid: " + e.getMessage());
        }
    }

    private void markInvoiceAsPaid(int invoiceId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE invoices SET Status = 'Paid' WHERE Invoice_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, invoiceId);
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                showAlert("Success", "Invoice marked as paid successfully!");
                loadInvoices(); // Refresh the invoices list
            }
        } catch (SQLException e) {
            showAlert("Error", "Error updating invoice status: " + e.getMessage());
        }
    }

    private boolean validateProjectInput() {
        if (projectTitle.getText().isEmpty()) {
            showAlert("Error", "Please enter a project title");
            return false;
        }
        if (projectDescription.getText().isEmpty()) {
            showAlert("Error", "Please enter a project description");
            return false;
        }
        try {
            Double.parseDouble(projectBudget.getText());
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid budget amount");
            return false;
        }
        if (projectDeadline.getValue() == null) {
            showAlert("Error", "Please select a deadline");
            return false;
        }
        if (projectDeadline.getValue().isBefore(LocalDate.now())) {
            showAlert("Error", "Deadline cannot be in the past");
            return false;
        }
        return true;
    }

    private void clearProjectForm() {
        projectTitle.clear();
        projectDescription.clear();
        projectBudget.clear();
        projectDeadline.setValue(null);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
        loadProjects();
        loadProposals();
        loadInvoices();
        loadFreelancersForRating();
        loadFreelancerProfiles();
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

    private VBox createFreelancerProfileBox(ResultSet rs) throws SQLException {
        VBox profileBox = new VBox(10);
        profileBox.getStyleClass().add("profile-card");
        profileBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-border-color: #dee2e6; -fx-border-radius: 5;");

        Label nameLabel = new Label("Name: " + rs.getString("username"));
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        Label emailLabel = new Label("Email: " + rs.getString("email"));
        
        // Add rating display
        double rating = rs.getDouble("rating");
        String ratingText = rating > 0 ? String.format("%.1f/5", rating) : "No ratings yet";
        Label ratingLabel = new Label("Rating: " + ratingText);
        ratingLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #007bff;");
        
        Label skillsLabel = new Label("Skills: " + (rs.getString("skills") != null ? rs.getString("skills") : "No skills listed"));
        skillsLabel.setWrapText(true);

        profileBox.getChildren().addAll(nameLabel, emailLabel, ratingLabel, skillsLabel);
        return profileBox;
    }
} 