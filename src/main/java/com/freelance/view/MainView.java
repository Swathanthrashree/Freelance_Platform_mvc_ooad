package com.freelance.view;

import com.freelance.controller.UserController;
import com.freelance.controller.ProjectController;
import com.freelance.controller.ClientDashboardController;
import com.freelance.controller.FreelancerDashboardController;
import com.freelance.model.User;
import com.freelance.model.Project;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.List;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class MainView {
    private Stage stage;
    private BorderPane mainLayout;
    private VBox contentArea;
    private UserController userController;
    private ProjectController projectController;
    private User currentUser;

    public MainView(Stage stage) {
        this.stage = stage;
        this.mainLayout = new BorderPane();
        this.contentArea = new VBox(10);
        this.contentArea.setPadding(new Insets(20));
        this.userController = new UserController();
        this.projectController = new ProjectController();
    }

    public void show() {
        showLoginScreen();
    }

    private void showLoginScreen() {
        VBox loginBox = new VBox(10);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new Insets(20));

        Label titleLabel = new Label("Freelance Platform Login");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setMaxWidth(300);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(300);

        Button loginButton = new Button("Login");
        loginButton.setMaxWidth(300);
        loginButton.setOnAction(e -> handleLogin(emailField.getText(), passwordField.getText()));

        Button registerButton = new Button("Register");
        registerButton.setMaxWidth(300);
        registerButton.setOnAction(e -> showRegisterScreen());

        loginBox.getChildren().addAll(titleLabel, emailField, passwordField, loginButton, registerButton);

        Scene scene = new Scene(loginBox, 400, 300);
        stage.setTitle("Login - Freelance Platform");
        stage.setScene(scene);
        stage.show();
    }

    private void showRegisterScreen() {
        VBox registerBox = new VBox(10);
        registerBox.setAlignment(Pos.CENTER);
        registerBox.setPadding(new Insets(20));

        Label titleLabel = new Label("Register New Account");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(300);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setMaxWidth(300);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(300);

        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Freelancer", "Client");
        roleComboBox.setPromptText("Select Role");
        roleComboBox.setMaxWidth(300);

        Button registerButton = new Button("Register");
        registerButton.setMaxWidth(300);
        registerButton.setOnAction(e -> handleRegister(
            usernameField.getText(),
            emailField.getText(),
            passwordField.getText(),
            roleComboBox.getValue()
        ));

        Button backButton = new Button("Back to Login");
        backButton.setMaxWidth(300);
        backButton.setOnAction(e -> showLoginScreen());

        registerBox.getChildren().addAll(
            titleLabel, usernameField, emailField, passwordField,
            roleComboBox, registerButton, backButton
        );

        Scene scene = new Scene(registerBox, 400, 400);
        stage.setTitle("Register - Freelance Platform");
        stage.setScene(scene);
    }

    private void handleLogin(String email, String password) {
        currentUser = userController.loginUser(email, password);
        if (currentUser != null) {
            showMainApplication();
        } else {
            showAlert("Login Failed", "Invalid email or password");
        }
    }

    private void handleRegister(String username, String email, String password, String role) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
            showAlert("Registration Failed", "Please fill in all fields");
            return;
        }

        if (userController.registerUser(username, email, password, role)) {
            showAlert("Success", "Registration successful! Please login.");
            showLoginScreen();
        } else {
            showAlert("Registration Failed", "Could not register user. Please try again.");
        }
    }

    private void showMainApplication() {
        try {
            String fxmlFile;
            if (currentUser.getRole().equalsIgnoreCase("Client")) {
                fxmlFile = "/com/freelance/view/client_dashboard.fxml";
            } else {
                fxmlFile = "/com/freelance/view/freelancer_dashboard.fxml";
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            
            // Set the user ID in the controller
            if (currentUser.getRole().equalsIgnoreCase("Client")) {
                ClientDashboardController controller = loader.getController();
                controller.setClientId(currentUser.getId());
            } else {
                FreelancerDashboardController controller = loader.getController();
                controller.setFreelancerId(currentUser.getId());
            }
            
            Scene scene = new Scene(root, 1024, 768);
            stage.setTitle("Freelance Platform - " + currentUser.getRole());
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load dashboard: " + e.getMessage());
        }
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        
        // File Menu
        Menu fileMenu = new Menu("File");
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> System.exit(0));
        fileMenu.getItems().add(exitItem);
        
        // Help Menu
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showAboutDialog());
        helpMenu.getItems().add(aboutItem);
        
        menuBar.getMenus().addAll(fileMenu, helpMenu);
        return menuBar;
    }

    private VBox createNavigationButtons() {
        VBox buttons = new VBox(10);
        buttons.setPadding(new Insets(10));
        buttons.setPrefWidth(200);
        
        Button[] navButtons = {
            createNavButton("Dashboard", this::showDashboard),
            createNavButton("Projects", this::showProjects),
            createNavButton("Freelancers", this::showFreelancers),
            createNavButton("Clients", this::showClients),
            createNavButton("Messages", this::showMessages)
        };
        
        buttons.getChildren().addAll(navButtons);
        return buttons;
    }

    private Button createNavButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(40);
        button.setOnAction(e -> action.run());
        return button;
    }

    private void showDashboard() {
        contentArea.getChildren().clear();
        
        VBox dashboardContent = new VBox(20);
        dashboardContent.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Dashboard");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        // Add dashboard content based on user role
        if (currentUser.getRole().equals("Client")) {
            List<Project> clientProjects = projectController.getClientProjects(currentUser.getId());
            Label projectsLabel = new Label("Your Projects: " + clientProjects.size());
            dashboardContent.getChildren().addAll(titleLabel, projectsLabel);
        } else {
            List<Project> allProjects = projectController.getAllProjects();
            Label projectsLabel = new Label("Available Projects: " + allProjects.size());
            dashboardContent.getChildren().addAll(titleLabel, projectsLabel);
        }
        
        contentArea.getChildren().add(dashboardContent);
    }

    private void showProjects() {
        contentArea.getChildren().clear();
        
        VBox projectsContent = new VBox(20);
        projectsContent.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Projects");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        // Add project creation form for clients
        if (currentUser.getRole().equals("Client")) {
            VBox createProjectForm = new VBox(10);
            createProjectForm.setPadding(new Insets(20));
            createProjectForm.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 5;");
            
            Label formTitle = new Label("Create New Project");
            formTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            
            TextField titleField = new TextField();
            titleField.setPromptText("Project Title");
            
            TextArea descriptionArea = new TextArea();
            descriptionArea.setPromptText("Project Description");
            descriptionArea.setPrefRowCount(3);
            
            TextField budgetField = new TextField();
            budgetField.setPromptText("Budget");
            
            Button createButton = new Button("Create Project");
            createButton.setOnAction(e -> {
                try {
                    double budget = Double.parseDouble(budgetField.getText());
                    if (projectController.createProject(
                        titleField.getText(),
                        descriptionArea.getText(),
                        budget,
                        currentUser.getId()
                    )) {
                        showAlert("Success", "Project created successfully!");
                        showProjects(); // Refresh the projects view
                    } else {
                        showAlert("Error", "Failed to create project");
                    }
                } catch (NumberFormatException ex) {
                    showAlert("Error", "Please enter a valid budget amount");
                }
            });
            
            createProjectForm.getChildren().addAll(
                formTitle, titleField, descriptionArea, budgetField, createButton
            );
            projectsContent.getChildren().add(createProjectForm);
        }
        
        // Show projects list
        List<Project> projects = currentUser.getRole().equals("Client") 
            ? projectController.getClientProjects(currentUser.getId())
            : projectController.getAllProjects();
            
        for (Project project : projects) {
            VBox projectCard = new VBox(10);
            projectCard.setPadding(new Insets(10));
            projectCard.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
            
            Label projectTitle = new Label(project.getTitle());
            projectTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            
            Label projectDescription = new Label(project.getDescription());
            projectDescription.setWrapText(true);
            
            Label projectBudget = new Label("Budget: $" + project.getBudget());
            Label projectStatus = new Label("Status: " + project.getStatus());
            
            projectCard.getChildren().addAll(
                projectTitle, projectDescription, projectBudget, projectStatus
            );
            
            projectsContent.getChildren().add(projectCard);
        }
        
        projectsContent.getChildren().add(0, titleLabel);
        contentArea.getChildren().add(projectsContent);
    }

    private void showFreelancers() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(new Label("Freelancers Content"));
    }

    private void showClients() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(new Label("Clients Content"));
    }

    private void showMessages() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(new Label("Messages Content"));
    }

    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Freelance Platform");
        alert.setContentText("Version 1.0\nA platform connecting freelancers with clients.");
        alert.showAndWait();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 