package com.example.db_project1;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class AppointmentDetailsController {

    @FXML
    private Label patientNameLabel;

    @FXML
    private Label appointmentDateLabel;

    @FXML
    private Label appointmentTimeLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label reasonLabel;

    @FXML
    private Button closeButton;

    @FXML
    private Button completeButton;

    private DoctorAppointment currentAppointment;

    private void showAppointmentDetails(Appointment appointment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("appointment-details-view.fxml"));

            // Load the root layout (e.g., AnchorPane) from the FXML file
            Parent root = loader.load();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Appointment Details");

            // Set the layout as the dialog content instead of casting to DialogPane
            dialog.getDialogPane().setContent(root);

            // Optional: Add action buttons like OK and Cancel
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Uncomment when AppointmentDetailsController is available
        /*
        AppointmentDetailsController controller = loader.getController();
        controller.setAppointment(appointment);
        */

            dialog.showAndWait();

            // Reload data if appointment was modified
            loadData();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Application Error", "Failed to load appointment details dialog: " + e.getMessage());
        }
    }
    public void setAppointment(DoctorAppointment appointment) {
        this.currentAppointment = appointment;
        loadData();
    }


    private void loadData() {
        if (currentAppointment != null) {
            // Refresh the appointment's displayed information
            appointmentDateLabel.setText(currentAppointment.getDate());
            appointmentTimeLabel.setText(currentAppointment.getTime());
            patientNameLabel.setText(currentAppointment.getPatientName());
            statusLabel.setText(currentAppointment.getStatus());
            reasonLabel.setText(currentAppointment.getReason());

            // Log that data was reloaded for debugging purposes
            System.out.println("Appointment details reloaded for ID: " + currentAppointment.getAppointId());
        } else {
            System.out.println("Cannot load data: no current appointment is set.");
        }
    }
    private void showAlert(String error, String applicationError, String s) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(error);
        alert.setHeaderText(applicationError);
        alert.setContentText(s);
        alert.showAndWait();
    }

    @FXML
    private void initialize() {
        // Configure "Close" button
        closeButton.setOnAction(event -> closeDialog());

        // Configure "Mark as Completed" button
        completeButton.setOnAction(event -> markAppointmentAsCompleted());
    }

    private void closeDialog() {
        // Close the dialog window
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private void markAppointmentAsCompleted() {
        if (currentAppointment != null) {
            // Update the status
            currentAppointment.setStatus("Completed");
            statusLabel.setText("Completed");

            // Perform database update logic (if required)
            // Example:
            // DatabaseConnection dbConnection = new DatabaseConnection();
            // try (Connection conn = dbConnection.getConnection()) {
            //     PreparedStatement statement = conn.prepareStatement(
            //         "UPDATE Appointment SET status = 'Completed' WHERE appoint_id = ?"
            //     );
            //     statement.setInt(1, currentAppointment.getAppointId());
            //     statement.executeUpdate();
            // } catch (SQLException e) {
            //     e.printStackTrace();
            // }

            // Notify user (optional)
            System.out.println("Appointment marked as completed");

            // Optionally, close the dialog after completion
            closeDialog();
        }
    }
}