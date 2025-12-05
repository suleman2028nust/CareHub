package com.example.db_project1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

public class SignupController {
    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField addressField;

    @FXML
    private TextField emailField;

    @FXML
    private DatePicker dobPicker;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button resetButton;

    @FXML
    private Button SignupButton;

    @FXML
    private Hyperlink loginLink;

    private DatabaseConnection dbConnection;

    @FXML
    public void initialize() {
        dbConnection = new DatabaseConnection();
    }

    @FXML
    public void handleSignup(ActionEvent event) {
        if (validateInputs()) {
            // Use a single connection for both email validation and insertion
            try (Connection conn = dbConnection.getConnection()) {
                if (emailAlreadyExists(conn)) { // Reuse the same connection for email validation
                    showAlert(Alert.AlertType.ERROR, "Signup Failed", "This Email is already registered.");
                    return;
                }

                // Insert patient data into the database
                String insertSQL = "INSERT INTO Patient (first_name, mid_name, last_name, address, email, password, dob) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                    pstmt.setString(1, firstNameField.getText().trim());
                    pstmt.setString(2, ""); // No middle name field? Pass empty string
                    pstmt.setString(3, lastNameField.getText().trim());
                    pstmt.setString(4, addressField.getText().trim());
                    pstmt.setString(5, emailField.getText().trim());
                    pstmt.setString(6, passwordField.getText()); // Consider hashing password for security
                    pstmt.setDate(7, Date.valueOf(dobPicker.getValue()));

                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Signup Successful",
                                "Your account has been created successfully!");
                        // Navigate to login page
                        Main.setRoot("Login");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Signup Failed", "Failed to save your data. Please try again.");
                    }
                }
            } catch (SQLException | IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
            }
        }
    }

    private boolean emailAlreadyExists(Connection conn) {
        String query = "SELECT COUNT(*) FROM Patient WHERE email = ?";
        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, emailField.getText().trim());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error checking Email: " + e.getMessage());
        }
        return false;
    }

    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();

        if (firstNameField.getText().trim().isEmpty()) {
            errorMessage.append("First name is required.\n");
        }

        if (lastNameField.getText().trim().isEmpty()) {
            errorMessage.append("Last name is required.\n");
        }

        if (addressField.getText().trim().isEmpty()) {
            errorMessage.append("Address is required.\n");
        }

        if (emailField.getText().trim().isEmpty()) {
            errorMessage.append("Email is required.\n");
        }

        if (passwordField.getText().isEmpty()) {
            errorMessage.append("Password is required.\n");
        }

        if (dobPicker.getValue() == null) {
            errorMessage.append("Date of birth is required.\n");
        } else if (dobPicker.getValue().isAfter(LocalDate.now())) {
            errorMessage.append("Date of birth cannot be in the future.\n");
        }

        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", errorMessage.toString());
            return false;
        }

        return true;
    }
    private static Patient patientInProgress;

    // Add this public getter method
    public static Patient getPatientInProgress() {
        return patientInProgress;
    }

    @FXML
    public void handleReset(ActionEvent event) {
        firstNameField.clear();
        lastNameField.clear();
        addressField.clear();
        emailField.clear();
        passwordField.clear();
        dobPicker.setValue(null);
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        try {
            Main.setRoot("Login");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Failed to navigate to login page: " + e.getMessage());
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