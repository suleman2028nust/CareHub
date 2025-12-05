
package com.example.db_project1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {
    private static Scene scene;
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        // Load splash screen first
        FXMLLoader splashLoader = new FXMLLoader(Main.class.getResource("SplashScreen.fxml"));
        Parent splashRoot = splashLoader.load();

        // Get screen dimensions to make sure scene size matches maximized window
        // This will help preserve maximized state when switching scenes
        double screenWidth = javafx.stage.Screen.getPrimary().getBounds().getWidth();
        double screenHeight = javafx.stage.Screen.getPrimary().getBounds().getHeight();

        // Create splash screen scene with screen dimensions
        Scene splashScene = new Scene(splashRoot, screenWidth, screenHeight);

        // Store primary stage for later use
        primaryStage = stage;

        // Set stage properties
        stage.setTitle("eDoc - Healthcare Management System");
        stage.setScene(splashScene);
        stage.setMaximized(true);
        stage.setResizable(true);
        stage.show();

        // The SplashScreenController will handle the transition to the login screen
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    // Method to set root with Patient object
    static void setRoot(String fxml, Patient patient) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(fxml + ".fxml"));
        Parent root = fxmlLoader.load();

        // Set patient in controller
        if (fxml.equals("PatientDashboard")) {
            PatientDashboardController controller = fxmlLoader.getController();
            controller.setPatient(patient);
        }
        scene.setRoot(root);
    }

    // Method to set root with Admin object
    static void setRoot(String fxml, Admin admin) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(fxml + ".fxml"));
        Parent root = fxmlLoader.load();

        // Set admin in controller
        if (fxml.equals("AdminDashboard")) {
            AdminDashboardController controller = fxmlLoader.getController();
            controller.setAdmin(admin);
        }
        scene.setRoot(root);
    }

    // Method to set root with Doctor object
    static void setRoot(String fxml, Doctor doctor) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(fxml + ".fxml"));
        Parent root = fxmlLoader.load();

        // Set doctor in controller (you would need to implement this)
        if (fxml.equals("DoctorDashboard")) {
            DoctorDashboardController controller = fxmlLoader.getController();
            controller.setDoctor(doctor);
        }
        scene.setRoot(root);
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    // Method to set the main scene after splash screen
    public static void setMainScene(Scene mainScene) {
        scene = mainScene;
        // Ensure that stage stays maximized when setting the new scene
        if (primaryStage != null) {
            primaryStage.setMaximized(true);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}