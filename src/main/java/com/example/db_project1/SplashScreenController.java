package com.example.db_project1;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SplashScreenController implements Initializable {
    @FXML
    private Rectangle progressBar;

    @FXML
    private Label loadingLabel;

    private Timeline timeline;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Start animation and loading sequence after a short delay
        Platform.runLater(this::startSplashAnimation);
    }

    private void startSplashAnimation() {
        // Animate the progress bar
        timeline = new Timeline();

        // Set up the animation keyframes for the progress bar
        KeyValue kv1 = new KeyValue(progressBar.widthProperty(), 300);
        KeyFrame kf1 = new KeyFrame(Duration.seconds(3), kv1);
        timeline.getKeyFrames().add(kf1);
        timeline.setOnFinished(event -> loadLoginScreen());

        // Create a separate timeline for the loading text
        Timeline loadingTextTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0.5), e -> loadingLabel.setText("Loading.")),
                new KeyFrame(Duration.seconds(1.0), e -> loadingLabel.setText("Loading..")),
                new KeyFrame(Duration.seconds(1.5), e -> loadingLabel.setText("Loading...")),
                new KeyFrame(Duration.seconds(2.0), e -> loadingLabel.setText("Loading."))
        );
        loadingTextTimeline.setCycleCount(2);

        // Start the animations
        timeline.play();
        loadingTextTimeline.play();
    }

    private void loadLoginScreen() {
        try {
            // Get current stage and its dimensions
            Stage currentStage = (Stage) progressBar.getScene().getWindow();

            // Get screen dimensions to ensure the new scene is created at full size
            double screenWidth = javafx.stage.Screen.getPrimary().getBounds().getWidth();
            double screenHeight = javafx.stage.Screen.getPrimary().getBounds().getHeight();

            // Load the login page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();

            // Create new scene with the login page and the screen dimensions
            Scene scene = new Scene(root, screenWidth, screenHeight);

            // Register the scene with Main class
            Main.setMainScene(scene);

            // Apply fade-in transition to the login page
            root.setOpacity(0);
            Timeline fadeIn = new Timeline(
                    new KeyFrame(Duration.seconds(1), new KeyValue(root.opacityProperty(), 1))
            );

            // Set the scene to stage while preserving maximized state
            currentStage.setScene(scene);

            // Make sure maximized state is maintained
            currentStage.setMaximized(true);

            // Play the fade-in animation
            fadeIn.play();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not load Login.fxml");
        }
    }

    // Method to be called when the application is closed
    public void closeAnimation() {
        if (timeline != null) {
            timeline.stop();
        }
    }
}