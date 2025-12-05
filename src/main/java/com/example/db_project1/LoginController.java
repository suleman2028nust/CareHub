
package com.example.db_project1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import java.io.IOException;
import java.sql.*;

public class LoginController {
    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink signUpLink;

    private DatabaseConnection dbConnection;

    @FXML
    public void initialize() {
        // Initialize database connection
        dbConnection = new DatabaseConnection();
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        // Validate inputs
        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Error",
                    "Please enter both email and password.");
            return;
        }

        try {
            // First, try to authenticate as a patient
            Patient patient = authenticatePatient(email, password);
            if (patient != null) {
                // Load patient dashboard
                loadPatientDashboard(patient);
                System.out.println("loading patient dashboard");
                return;
            }

            // Next, try to authenticate as a doctor
            Doctor doctor = authenticateDoctor(email, password);
            if (doctor != null) {
                // Load doctor dashboard
                loadDoctorDashboard(doctor);
                return;
            }

            // Finally, try to authenticate as an admin
            Admin admin = authenticateAdmin(email, password);
            if (admin != null) {
                // Load admin dashboard
                loadAdminDashboard(admin);
                return;
            }

            // If we get here, authentication failed
            showAlert(Alert.AlertType.ERROR, "Login Failed",
                    "Invalid email or password. Please try again.");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "A database error occurred: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Failed to navigate to dashboard: " + e.getMessage());
        }
    }

    private Patient authenticatePatient(String email, String password) throws SQLException {
        String query = "SELECT * FROM Patient WHERE email = ?";
        PreparedStatement preparedStatement = dbConnection.getConnection().prepareStatement(query);
        preparedStatement.setString(1, email);

        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            String storedPassword = resultSet.getString("password");

            // Verify password (using plain comparison for now, should use BCrypt in production)
            if (password.equals(storedPassword)) {
                // Create and return patient object
                Patient patient = new Patient();
                patient.setPatientId(resultSet.getInt("patient_id"));
                patient.setName(resultSet.getString("name"));
                patient.setEmail(email);
                patient.setAddress(resultSet.getString("address"));
                patient.setPassword(resultSet.getString("password"));
                // Set other patient attributes as needed

                System.out.println("Patient authenticated: " + patient.getName());
                System.out.println("PATIENT AUTHENTICATED");
                return patient;
            }
        }

        return null; // Authentication failed
    }

    private Doctor authenticateDoctor(String email, String password) throws SQLException {
        String query = "SELECT * FROM Doctor WHERE email = ?";
        PreparedStatement preparedStatement = dbConnection.getConnection().prepareStatement(query);
        preparedStatement.setString(1, email);

        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            String storedPassword = resultSet.getString("password");

            // Verify password (using plain comparison for now, should use BCrypt in production)
            if (password.equals(storedPassword)) {
                // Create and return doctor object
                Doctor doctor = new Doctor();
                doctor.setDoctorId(resultSet.getInt("doctor_id"));

                // Concatenate first_name, mid_name, and last_name to construct full name
                String firstName = resultSet.getString("first_name");
                String middleName = resultSet.getString("mid_name");
                String lastName = resultSet.getString("last_name");

                // Build the full name, ensuring mid_name is not null
                String fullName = firstName;
                if (middleName != null && !middleName.isEmpty()) {
                    fullName += " " + middleName;
                }
                fullName += " " + lastName;

                doctor.setName(fullName.trim()); // Trim extra spaces just in case

                doctor.setEmail(email);
                doctor.setSpecialization(resultSet.getString("specialization"));
                // Set other doctor attributes as needed

                System.out.println("Doctor authenticated: " + doctor.getName());
                return doctor;
            }
        }
        return null; // Authentication failed
    }

    private Admin authenticateAdmin(String email, String password) throws SQLException {
        String query = "SELECT * FROM Admin WHERE email = ?";
        PreparedStatement preparedStatement = dbConnection.getConnection().prepareStatement(query);
        preparedStatement.setString(1, email);

        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            String storedPassword = resultSet.getString("password");

            // Verify password (using plain comparison for now, should use BCrypt in production)
            if (password.equals(storedPassword)) {
                // Create and return admin object
                Admin admin = new Admin();
                admin.setAdminId(resultSet.getInt("admin_id"));

                // Get name from database
                String firstName = resultSet.getString("first_name");
                String middleName = resultSet.getString("mid_name");
                String lastName = resultSet.getString("last_name");

                // Set individual name components
                admin.setFirstName(firstName);
                admin.setMiddleName(middleName != null ? middleName : "");
                admin.setLastName(lastName);

                admin.setEmail(email);
                admin.setPassword(password);

                // Set other fields if they exist in your admin table
                try {
                    admin.setGender(resultSet.getString("gender"));
                    admin.setDob(resultSet.getString("dob"));
                    admin.setAddress(resultSet.getString("address"));
                    admin.setPhoneNo(resultSet.getString("phone_no"));
                    admin.setPic(resultSet.getString("pic"));
                } catch (SQLException e) {
                    // Fields might not exist, just continue
                    System.out.println("Some admin fields might be missing: " + e.getMessage());
                }

                System.out.println("Admin authenticated: " + admin.getName());
                return admin;
            }
        }

        return null; // Authentication failed
    }

    private void loadPatientDashboard(Patient patient) throws IOException {
        // Simply delegate to Main's setRoot method to handle the scene transition
        // This will load PatientDashboard.fxml and set the patient in the controller
        Main.setRoot("PatientDashboard", patient);
    }

    private void loadDoctorDashboard(Doctor doctor) throws IOException {
        // Similar to loadPatientDashboard, but for doctor
        // For now, let's just show an alert since we don't have the doctor dashboard yet
//        showAlert(Alert.AlertType.INFORMATION, "Doctor Login Successful",
//                "Welcome, Dr. " + doctor.getName() + "! Doctor dashboard will be implemented.");
        System.out.println("Loading doctor dashboard for: " + doctor.getName());
        Main.setRoot("DoctorDashboard", doctor);
    }

    private void loadAdminDashboard(Admin admin) throws IOException {
        // Use the new setRoot method in Main class to load the admin dashboard
        Main.setRoot("AdminDashboard", admin);
        System.out.println("Loading admin dashboard for: " + admin.getName());
    }

    @FXML
    public void handleSignUp(ActionEvent event) {
        try {
            // Navigate to the signup page
            Main.setRoot("Signup");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Failed to navigate to signup page: " + e.getMessage());
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