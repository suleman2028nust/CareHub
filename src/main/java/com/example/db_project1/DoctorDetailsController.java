package com.example.db_project1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class DoctorDetailsController implements Initializable {
    @FXML
    private AnchorPane detailsPane;

    @FXML
    private Label doctorNameLabel;

    @FXML
    private Label doctorEmailLabel;

    @FXML
    private Label doctorNICLabel;

    @FXML
    private Label doctorTelephoneLabel;

    @FXML
    private Label doctorSpecialtiesLabel;

    @FXML
    private Button okButton;

    // Model object to store doctor details
    private Doctor doctor;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set action for OK button
        okButton.setOnAction(this::handleOkButton);
    }

    /**
     * Set the doctor data to be displayed in the details view
     * @param doctor The doctor object containing all details
     */
    public void setDoctorData(Doctor doctor) {
        this.doctor = doctor;

        // Populate UI elements with doctor data
        if (doctor != null) {
            doctorNameLabel.setText(doctor.getName());
            doctorEmailLabel.setText(doctor.getEmail());
            doctorNICLabel.setText(doctor.getNic());
            doctorTelephoneLabel.setText(doctor.getTelephone());
            doctorSpecialtiesLabel.setText(doctor.getSpecialties());
        }
    }

    /**
     * Handle the OK button click event
     * @param event The action event
     */
    @FXML
    private void handleOkButton(ActionEvent event) {
        // Close the dialog
        Stage stage = (Stage) okButton.getScene().getWindow();
        stage.close();
    }

    public void setDoctor(com.example.db_project1.Doctor doctor) {
        doctorNameLabel.setText(doctor.getName());
        doctorEmailLabel.setText(doctor.getEmail());
    }

    /**
     * Doctor model class (simplified for demo)
     */
    public static class Doctor {
        private String name;
        private String email;
        private String nic;
        private String telephone;
        private String specialties;

        public Doctor(String name, String email, String nic, String telephone, String specialties) {
            this.name = name;
            this.email = email;
            this.nic = nic;
            this.telephone = telephone;
            this.specialties = specialties;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getNic() {
            return nic;
        }

        public String getTelephone() {
            return telephone;
        }

        public String getSpecialties() {
            return specialties;
        }
    }
}