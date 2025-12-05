/*
 * SignupAccountDetailsController handles the second part of patient signup
 * with email, password, and phone number
 */
package com.example.db_project1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.IOException;
import java.sql.*;

public class SignupAccountDetailsController {
    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button backButton;

    @FXML
    private Button signupButton;

    private DatabaseConnection dbConnection;

    @FXML
    public void initialize() {
        // Initialize database connection
        dbConnection = new DatabaseConnection();
    }

    @FXML
    public void handleSignup(ActionEvent event) {
        if (validateInputs()) {
            // Get the patient object from the first signup page
            Patient patient = SignupController.getPatientInProgress();

            if (patient == null) {
                showAlert(Alert.AlertType.ERROR, "Signup Error",
                        "Patient information is missing. Please start again.");
                try {
                    Main.setRoot("Signup");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            // Add account details to the patient object
            patient.setEmail(emailField.getText().trim());
            //patient.setPhone(phoneField.getText().trim());
            // In a real application, encrypt the password before storing
            //patient.setPassword(passwordField.getText());

            // Insert patient into database
            if (registerPatient(patient)) {
                showAlert(Alert.AlertType.INFORMATION, "Signup Successful",
                        "Your account has been created successfully! Please login.");

                try {
                    // Navigate back to login page
                    Main.setRoot("Login");
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Navigation Error",
                            "Failed to navigate to login page: " + e.getMessage());
                }
            }
        }
    }

    private boolean registerPatient(Patient patient) {
        try {
            // SQL query to insert a new patient
            String query = "INSERT INTO Patient (name, address, nic, dob, email, phone, password) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement preparedStatement = dbConnection.getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, patient.getName());
            preparedStatement.setString(2, patient.getAddress());
            //preparedStatement.setString(3, patient.getNic());
            preparedStatement.setDate(4, java.sql.Date.valueOf(patient.getDob()));
            preparedStatement.setString(5, patient.getEmail());
            //preparedStatement.setString(6, patient.getPhone());
            //preparedStatement.setString(7, patient.getPassword());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                // Get the auto-generated patient_id
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    patient.setPatientId(generatedKeys.getInt(1));
                }
                System.out.println("Patient registered successfully with ID: " + patient.getPatientId());
                return true;
            } else {
                showAlert(Alert.AlertType.ERROR, "Registration Failed",
                        "Failed to insert patient record. Please try again.");
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "A database error occurred: " + e.getMessage());
            return false;
        }
    }

    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();

        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (email.isEmpty()) {
            errorMessage.append("Email is required.\n");
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errorMessage.append("Please enter a valid email address.\n");
        } else {
            // Check if email already exists in database
            try {
                String query = "SELECT COUNT(*) FROM Patient WHERE email = ?";
                PreparedStatement preparedStatement = dbConnection.getConnection().prepareStatement(query);
                preparedStatement.setString(1, email);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next() && resultSet.getInt(1) > 0) {
                    errorMessage.append("This email is already registered.\n");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                errorMessage.append("Database error while checking email: ").append(e.getMessage()).append("\n");
            }
        }

        if (phone.isEmpty()) {
            errorMessage.append("Phone number is required.\n");
        } else if (!phone.matches("\\d{10}")) {
            errorMessage.append("Please enter a valid 10-digit phone number.\n");
        }

        if (password.isEmpty()) {
            errorMessage.append("Password is required.\n");
        } else if (password.length() < 6) {
            errorMessage.append("Password must be at least 6 characters long.\n");
        }

        if (confirmPassword.isEmpty()) {
            errorMessage.append("Please confirm your password.\n");
        } else if (!password.equals(confirmPassword)) {
            errorMessage.append("Passwords do not match.\n");
        }

        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", errorMessage.toString());
            return false;
        }

        return true;
    }

    @FXML
    public void handleBack(ActionEvent event) {
        try {
            // Navigate back to the first signup page
            Main.setRoot("Signup");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Failed to navigate back to signup page: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}