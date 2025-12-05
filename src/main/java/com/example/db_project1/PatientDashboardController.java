

package com.example.db_project1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PatientDashboardController implements Initializable {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private StackPane contentArea;

    @FXML
    private Button homeBtn;

    @FXML
    private Button allDoctorsBtn;

    @FXML
    private Button appointmentsBtn;

    @FXML
    private Button prescriptionsBtn;

    @FXML
    private Button billsBtn;


    @FXML
    private Button profileBtn;

    @FXML
    private Button logoutBtn;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label userEmailLabel;

    @FXML
    private ImageView userAvatar;

    private Patient currentPatient;
    private DatabaseConnection dbConnection;
    private boolean initialLoadComplete = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dbConnection = new DatabaseConnection();

        // Set up logout button action
        logoutBtn.setOnAction(event -> logout());

        // Set default user avatar
        try {
            userAvatar.setImage(new Image(getClass().getResourceAsStream("/images/user-avatar.png")));
        } catch (Exception e) {
            System.out.println("Failed to load user avatar: " + e.getMessage());
        }

        // Don't load Home view yet - we'll do it in setPatient() when we have patient data
    }

    public void setPatient(Patient patient) {
        this.currentPatient = patient;
        System.out.println("Patient set in dashboard: " + (patient != null ? patient.getName() : "null"));

        // Update UI with patient info
        if (patient != null) {
            userNameLabel.setText(patient.getName());
            userEmailLabel.setText(patient.getEmail());

            // Load the Home view with patient data
            try {
                loadView("Home.fxml");
                initialLoadComplete = true;
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Failed to load Home view: " + e.getMessage());
            }
        }
    }

    private void logout() {
        try {
            // Close database connection
            if (dbConnection != null) {
                dbConnection.closeConnection();
            }

            // Navigate to login screen
            Main.setRoot("login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxml) throws IOException {
        // Reset all button styles
        resetButtonStyles();

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        contentArea.getChildren().clear();
        contentArea.getChildren().add(loader.load());

        // Handle different controllers based on loaded view
        Object controller = loader.getController();

        if (controller instanceof HomeController) {
            ((HomeController) controller).setPatient(currentPatient);
            ((HomeController) controller).loadData();
            homeBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #2d7ae5; -fx-border-width: 0 0 0 5; -fx-text-fill: #2d7ae5;");
        } else if (controller instanceof AllDoctorsController) {
            ((AllDoctorsController) controller).setPatient(currentPatient);
            ((AllDoctorsController) controller).loadData();
            allDoctorsBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #2d7ae5; -fx-border-width: 0 0 0 5; -fx-text-fill: #2d7ae5;");
        }
        else if (controller instanceof MyAppointmentsController) {
            ((MyAppointmentsController) controller).setPatient(currentPatient);
            ((MyAppointmentsController) controller).loadData();
            appointmentsBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #2d7ae5; -fx-border-width: 0 0 0 5; -fx-text-fill: #2d7ae5;");
        }
        else if (controller instanceof MyPrescriptionsController) {
            ((MyPrescriptionsController) controller).setPatient(currentPatient);
            ((MyPrescriptionsController) controller).loadData();
            prescriptionsBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #2d7ae5; -fx-border-width: 0 0 0 5; -fx-text-fill: #2d7ae5;");
        }
        else if (controller instanceof MyBillsController) {
            ((MyBillsController) controller).setPatient(currentPatient);
            ((MyBillsController) controller).loadData();
            billsBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #2d7ae5; -fx-border-width: 0 0 0 5; -fx-text-fill: #2d7ae5;");
        }
        else if (controller instanceof MyProfileController) {
            ((MyProfileController) controller).setPatient(currentPatient);
            ((MyProfileController) controller).loadData();
            profileBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #2d7ae5; -fx-border-width: 0 0 0 5; -fx-text-fill: #2d7ae5;");
        }
    }

    private void resetButtonStyles() {
        String defaultStyle = "-fx-background-color: transparent; -fx-border-color: transparent;";
        homeBtn.setStyle(defaultStyle);
        allDoctorsBtn.setStyle(defaultStyle);
        appointmentsBtn.setStyle(defaultStyle);
        prescriptionsBtn.setStyle(defaultStyle);
        billsBtn.setStyle(defaultStyle);
        profileBtn.setStyle(defaultStyle);
    }

    // Event handlers for navigation buttons
    @FXML
    void homeBtnOnAction(ActionEvent event) {
        try {
            loadView("Home.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void allDoctorsBtnOnAction(ActionEvent event) {
        try {
            loadView("AllDoctors.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void myBookingsBtnOnAction(ActionEvent event) {
        try {
            loadView("MyAppointments.fxml");
        } catch (IOException e) {
            System.out.println("failed");
            e.printStackTrace();
        }
    }

    @FXML
    void prescriptionsBtnOnAction(ActionEvent event) {
        try {
            loadView("MyPrescription.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void billsBtnOnAction(ActionEvent event) {
        try {
            System.out.println("bills button clicked");
            loadView("MyBills.fxml");
        } catch (IOException e) {
            System.out.println("failed");
            e.printStackTrace();
        }
    }

    @FXML
    void profileBtnOnAction(ActionEvent event) {
        try {
            loadView("MyProfile.fxml");
        } catch (IOException e) {
            System.out.println("failed");
            e.printStackTrace();
        }
    }
}