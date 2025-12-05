package com.example.db_project1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DoctorDashboardController implements Initializable {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private StackPane contentArea;

    @FXML
    private Button homeBtn;

    @FXML
    private Button appointmentsBtn;

    @FXML
    private Button patientsBtn;

    @FXML
    private Button prescriptionsBtn;

    @FXML
    private Button profileBtn;

    @FXML
    private Button settingsBtn;

    @FXML
    private Button logoutBtn;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label userEmailLabel;

    @FXML
    private ImageView userAvatar;

    private Doctor currentDoctor;
    private DatabaseConnection dbConnection;
    private boolean initialLoadComplete = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dbConnection = new DatabaseConnection();

        // Set up logout button action
        logoutBtn.setOnAction(event -> logout());

        // Set default user avatar
        try {
            userAvatar.setImage(new Image(getClass().getResourceAsStream("/images/doctor-avatar.png")));
        } catch (Exception e) {
            System.out.println("Failed to load doctor avatar: " + e.getMessage());
        }

        // Don't load Dashboard view yet - we'll do it in setDoctor() when we have doctor data
    }

    public void setDoctor(Doctor doctor) {
        this.currentDoctor = doctor;
        System.out.println("Doctor set in dashboard: " + (doctor != null ? doctor.getName() : "null"));

        // Update UI with doctor info
        if (doctor != null) {
            userNameLabel.setText(doctor.getName());
            userEmailLabel.setText(doctor.getEmail());

            // Load the Dashboard view with doctor data
            try {
                loadView("DoctorHome.fxml");
                initialLoadComplete = true;
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Failed to load Dashboard view: " + e.getMessage());
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

        if (controller instanceof DoctorHomeController) {
            ((DoctorHomeController) controller).setDoctor(currentDoctor);
            ((DoctorHomeController) controller).loadData();
            homeBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #2d7ae5; -fx-border-width: 0 0 0 5; -fx-text-fill: #2d7ae5;");
        } else if (controller instanceof DoctorAppointmentsController) {
            ((DoctorAppointmentsController) controller).setDoctor(currentDoctor);
            ((DoctorAppointmentsController) controller).loadData();
            appointmentsBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #2d7ae5; -fx-border-width: 0 0 0 5; -fx-text-fill: #2d7ae5;");
        } else if (controller instanceof DoctorPatientsController) {
            ((DoctorPatientsController) controller).setDoctor(currentDoctor);
            ((DoctorPatientsController) controller).loadData();
            patientsBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #2d7ae5; -fx-border-width: 0 0 0 5; -fx-text-fill: #2d7ae5;");
        } else if (controller instanceof DoctorPrescriptionsController) {
            ((DoctorPrescriptionsController) controller).setDoctor(currentDoctor);
            ((DoctorPrescriptionsController) controller).loadData();
            prescriptionsBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #2d7ae5; -fx-border-width: 0 0 0 5; -fx-text-fill: #2d7ae5;");
        } else if (controller instanceof DoctorProfileController) {
            ((DoctorProfileController) controller).setDoctor(currentDoctor);
            ((DoctorProfileController) controller).loadData();
            profileBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #2d7ae5; -fx-border-width: 0 0 0 5; -fx-text-fill: #2d7ae5;");
        }
    }

    private void resetButtonStyles() {
        String defaultStyle = "-fx-background-color: transparent; -fx-border-color: transparent;";
        homeBtn.setStyle(defaultStyle);
        appointmentsBtn.setStyle(defaultStyle);
        patientsBtn.setStyle(defaultStyle);
        prescriptionsBtn.setStyle(defaultStyle);
        profileBtn.setStyle(defaultStyle);
        //settingsBtn.setStyle(defaultStyle);
    }

    // Event handlers for navigation buttons
    @FXML
    void homeBtnOnAction(ActionEvent event) {
        try {
            loadView("DoctorHome.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void myAppointmentsBtnOnAction(ActionEvent event) {
        try {
            loadView("DoctorAppointments.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void myPatientsBtnOnAction(ActionEvent event) {
        try {
            loadView("DoctorPatients.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void prescriptionsBtnOnAction(ActionEvent event) {
        try {
            loadView("DoctorPrescriptions.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void profileBtnOnAction(ActionEvent event) {
        try {
            loadView("DoctorProfile.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    @FXML
//    void settingsBtnOnAction(ActionEvent event) {
//        try {
//            loadView("DoctorSettings.fxml");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}