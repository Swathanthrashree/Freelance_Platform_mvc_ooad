package com.freelance;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.freelance.view.MainView;
import com.freelance.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Test database connection first
        try {
            System.out.println("Testing database connection...");
            Connection conn = DatabaseConfig.getConnection();
            
            if (conn != null) {
                System.out.println("Connection successful! Testing a simple query...");
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SHOW DATABASES");
                System.out.println("Available databases:");
                while (rs.next()) {
                    System.out.println("- " + rs.getString(1));
                }
                rs.close();
                stmt.close();
            } else {
                System.out.println("Failed to establish database connection.");
            }
        } catch (Exception e) {
            System.out.println("Database test failed: " + e.getMessage());
            e.printStackTrace();
        }

        // Continue with normal application startup
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/freelance/view/login.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/freelance/styles/styles.css").toExternalForm());
        
        primaryStage.setTitle("Freelance Platform - Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
} 