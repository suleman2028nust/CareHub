package com.example.db_project1;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class DoctorAddFormController implements Initializable {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField middleNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField phoneField;

    @FXML
    private ComboBox<String> genderComboBox;

    @FXML
    private DatePicker dobPicker;

    @FXML
    private ComboBox<String> specializationComboBox;

    @FXML
    private TextField experienceField;

    @FXML
    private TextField languagesField;

    @FXML
    private TextField scheduleField;

    @FXML
    private Label photoNameLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Button browseButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private Image selectedImage = null;
    private AdminDoctorController adminDoctorController;
    private DatabaseConnection dbConnection;

    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Initialize database connection
            dbConnection = new DatabaseConnection();

            // Populate gender ComboBox
            genderComboBox.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));

            // Populate specialization ComboBox with common specialties
            List<String> specializations = Arrays.asList(
                    "Cardiology", "Dermatology", "Endocrinology", "Gastroenterology",
                    "Neurology", "Obstetrics & Gynecology", "Oncology", "Ophthalmology",
                    "Orthopedics", "Pediatrics", "Psychiatry", "Pulmonology",
                    "Radiology", "Urology", "General Medicine", "Surgery"
            );
            specializationComboBox.setItems(FXCollections.observableArrayList(specializations));

        } catch (Exception e) {
            System.out.println("Error during DoctorAddFormController initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setAdminDoctorController(AdminDoctorController controller) {
        this.adminDoctorController = controller;
    }

    @FXML
    private void handleBrowsePhoto(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Doctor Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) browseButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                selectedImage = new Image(new FileInputStream(selectedFile));
                photoNameLabel.setText(selectedFile.getName());
            } catch (IOException e) {
                System.out.println("Error loading image: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Error", "Image Selection Error",
                        "Failed to load the selected image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateInputs()) {
            return;
        }

        try {
            // Create new Doctor object
            Doctor newDoctor = createDoctorFromInputs();

            // Save doctor to database using the save method from Doctor class
            if (newDoctor.save()) {
                // Add doctor to UI table if there's a parent controller
                if (adminDoctorController != null) {
                    adminDoctorController.addDoctorToTable(newDoctor);
                }

                showAlert(Alert.AlertType.INFORMATION, "Success", "Doctor Added",
                        "Doctor has been successfully added to the system.");

                // Close the window
                closeWindow();
            } else {
                System.out.println("Failed to insert doctor into database");
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to Add Doctor",
                        "There was an error adding the doctor to the database. Please try again.");
            }
        } catch (Exception e) {
            System.out.println("Error while saving doctor: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Add Doctor Error",
                    "An error occurred while saving the doctor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Doctor createDoctorFromInputs() {
        Doctor doctor = new Doctor();
        doctor.setFirstName(firstNameField.getText().trim());
        doctor.setMiddleName(middleNameField.getText().trim().isEmpty() ? null : middleNameField.getText().trim());
        doctor.setLastName(lastNameField.getText().trim());
        doctor.setEmail(emailField.getText().trim());
        doctor.setPhoneNo(phoneField.getText().trim());
        doctor.setSpecialization(specializationComboBox.getValue());
        String genderValue = genderComboBox.getValue();
        if (genderValue != null) {
            genderValue = genderValue.trim();
        }
        doctor.setGender(genderValue);

        // Parse experience as integer
        try {
            doctor.setExperience(Integer.parseInt(experienceField.getText().trim()));
        } catch (NumberFormatException e) {
            doctor.setExperience(0);
        }

        doctor.setLanguage(languagesField.getText().trim());
        doctor.setSchedule(scheduleField.getText().trim());
        doctor.setPassword(passwordField.getText());

        // Set date of birth if provided
        if (dobPicker.getValue() != null) {
            java.sql.Date sqlDate = java.sql.Date.valueOf(dobPicker.getValue());
            doctor.setDob(sqlDate);
        }

        // Set gender if you add a ComboBox for it
        // doctor.setGender(genderComboBox.getValue());

        if (selectedImage != null) {
            doctor.setPicture(selectedImage);
        }

        return doctor;
    }

    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();

        if (firstNameField.getText().trim().isEmpty()) {
            errorMessage.append("- First Name is required\n");
        }

        if (lastNameField.getText().trim().isEmpty()) {
            errorMessage.append("- Last Name is required\n");
        }

        if (emailField.getText().trim().isEmpty()) {
            errorMessage.append("- Email is required\n");
        } else if (!isValidEmail(emailField.getText().trim())) {
            errorMessage.append("- Email format is invalid\n");
        }
        if (genderComboBox.getValue() == null || genderComboBox.getValue().trim().isEmpty()) {
            errorMessage.append("- Gender is required\n");
        }

        if (passwordField.getText().isEmpty()) {
            errorMessage.append("- Password is required\n");
        } else if (passwordField.getText().length() < 6) {
            errorMessage.append("- Password must be at least 6 characters\n");
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            errorMessage.append("- Passwords do not match\n");
        }

        if (phoneField.getText().trim().isEmpty()) {
            errorMessage.append("- Phone Number is required\n");
        }

        String specialization = specializationComboBox.getValue();
        if (specialization == null || specialization.trim().isEmpty()) {
            errorMessage.append("- Specialization is required\n");
        }

        if (experienceField.getText().trim().isEmpty()) {
            errorMessage.append("- Experience is required\n");
        } else {
            try {
                Integer.parseInt(experienceField.getText().trim());
            } catch (NumberFormatException e) {
                errorMessage.append("- Experience must be a number\n");
            }
        }

        if (languagesField.getText().trim().isEmpty()) {
            errorMessage.append("- Languages are required\n");
        }

        if (scheduleField.getText().trim().isEmpty()) {
            errorMessage.append("- Schedule is required\n");
        }

        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please correct the following errors:",
                    errorMessage.toString());
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}