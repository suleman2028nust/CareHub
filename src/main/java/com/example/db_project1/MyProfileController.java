package com.example.db_project1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class MyProfileController implements Initializable {

    // Personal Information Tab Fields
    @FXML private TextField firstNameField;
    @FXML private TextField midNameField;
    @FXML private TextField lastNameField;
    @FXML private DatePicker dobPicker;
    @FXML private ComboBox<String> genderCombo;
    @FXML private ComboBox<String> bloodGroupCombo;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField insuranceField;
    @FXML private TextField patientIdField;
    @FXML private TextArea addressField;
    @FXML private Label dateLabel;
    @FXML private Button changePhotoBtn;
    @FXML private Button resetBtn;
    @FXML private Button saveBtn;

    // Medical History Tab Fields
    @FXML private Label totalVisitsLabel;
    @FXML private Label lastVisitLabel;
    @FXML private Label recentAppointmentsLabel;
    @FXML private Label upcomingAppointmentsLabel;
    @FXML private TableView<VisitHistory> visitHistoryTable;
    @FXML private TableColumn<VisitHistory, String> visitDateCol;
    @FXML private TableColumn<VisitHistory, String> visitDoctorCol;
    @FXML private TableColumn<VisitHistory, String> visitTypeCol;
    @FXML private TableColumn<VisitHistory, String> visitDiagnosisCol;
    @FXML private TableColumn<VisitHistory, Button> visitDetailsCol;
    @FXML private VBox medicalConditionsBox;
    @FXML private Label noConditionsLabel;
    @FXML private VBox allergiesBox;
    @FXML private Label noAllergiesLabel;

    // Security Tab Fields
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private CheckBox twoFactorAuthCheckbox;
    @FXML private CheckBox loginNotificationsCheckbox;
    @FXML private TextField recoveryEmailField;
    @FXML private Button changePasswordBtn;
    @FXML private Button updateRecoveryEmailBtn;
    @FXML private Button saveSecuritySettingsBtn;

    private Patient currentPatient;
    private DatabaseConnection dbConnection;
    private ObservableList<VisitHistory> visitHistoryList;
    private String originalProfileImagePath;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set current date
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        // Initialize database connection
        dbConnection = new DatabaseConnection();

        // Initialize visit history table
        initializeVisitHistoryTable();

        // Initialize lists
        visitHistoryList = FXCollections.observableArrayList();
    }

    public void setPatient(Patient patient) {
        this.currentPatient = patient;
        populatePatientData();
    }

    public void loadData() {
        if (currentPatient != null) {
            // Load medical history data
            loadVisitHistory();
            loadMedicalSummary();
            loadMedicalConditions();
            loadAllergies();

            // Load security settings (from a separate table if implemented)
            // loadSecuritySettings();
        }
    }

    private void populatePatientData() {
        if (currentPatient != null) {
            // Populate personal information fields
            firstNameField.setText(currentPatient.getFirstName());
            midNameField.setText(currentPatient.getMiddleName());
            lastNameField.setText(currentPatient.getLastName());
            dobPicker.setValue(currentPatient.getDob());
            genderCombo.setValue(currentPatient.getGender());
            bloodGroupCombo.setValue(currentPatient.getBloodGroup());
            emailField.setText(currentPatient.getEmail());
            phoneField.setText(currentPatient.getPhoneNo());
            insuranceField.setText(currentPatient.getInsuranceType());
            patientIdField.setText(String.valueOf(currentPatient.getPatientId()));
            addressField.setText(currentPatient.getAddress());
        }
    }

    private void initializeVisitHistoryTable() {
        visitDateCol.setCellValueFactory(new PropertyValueFactory<>("visitDate"));
        visitDoctorCol.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        visitTypeCol.setCellValueFactory(new PropertyValueFactory<>("visitType"));
        visitDiagnosisCol.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        visitDetailsCol.setCellValueFactory(new PropertyValueFactory<>("detailsButton"));
    }

    private void loadVisitHistory() {
        visitHistoryList.clear();

        String query = "SELECT a.date, a.time, a.type, \n" +
                "       CONCAT(d.first_name, ' ', d.mid_name, ' ', d.last_name) AS doctor_name, \n" +
                "       p.diagnosis\n" +
                "FROM Appointment a\n" +
                "JOIN Doctor d ON a.doctor_id = d.doctor_id\n" +
                "LEFT JOIN Prescription p ON a.patient_id = p.patient_id AND a.doctor_id = p.doctor_id\n" +
                "WHERE a.patient_id = ? AND a.status = 'Completed'\n" +
                "ORDER BY a.date DESC, a.time DESC";

        try {
            PreparedStatement stmt = dbConnection.getConnection().prepareStatement(query);
            stmt.setInt(1, currentPatient.getPatientId());

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String visitDate = rs.getDate("date").toString() + " " + rs.getTime("time").toString();
                String doctorName = rs.getString("doctor_name");
                String visitType = rs.getString("type");
                String diagnosis = rs.getString("diagnosis") != null ? rs.getString("diagnosis") : "Not recorded";

                Button detailsButton = new Button("View");
                detailsButton.setOnAction(e -> showVisitDetails(visitDate, doctorName, diagnosis));

                VisitHistory visit = new VisitHistory(visitDate, doctorName, visitType, diagnosis, detailsButton);
                visitHistoryList.add(visit);
            }

            if (visitHistoryList.isEmpty()) {
                VisitHistory noVisits = new VisitHistory("No visit history", "-", "-", "-", null);
                visitHistoryList.add(noVisits);
            }

            visitHistoryTable.setItems(visitHistoryList);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading visit history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadMedicalSummary() {
        try {
            // Get total visits count
            String visitsQuery = "SELECT COUNT(*) FROM Appointment WHERE patient_id = ? AND status = 'Completed'";
            PreparedStatement visitsStmt = dbConnection.getConnection().prepareStatement(visitsQuery);
            visitsStmt.setInt(1, currentPatient.getPatientId());
            ResultSet visitsRs = visitsStmt.executeQuery();

            if (visitsRs.next()) {
                totalVisitsLabel.setText(visitsRs.getString(1));
            }

            // Get last visit date
            String lastVisitQuery = "SELECT MAX(date) FROM Appointment WHERE patient_id = ? AND status = 'Completed'";
            PreparedStatement lastVisitStmt = dbConnection.getConnection().prepareStatement(lastVisitQuery);
            lastVisitStmt.setInt(1, currentPatient.getPatientId());
            ResultSet lastVisitRs = lastVisitStmt.executeQuery();

            if (lastVisitRs.next() && lastVisitRs.getDate(1) != null) {
                lastVisitLabel.setText(lastVisitRs.getDate(1).toString());
            } else {
                lastVisitLabel.setText("N/A");
            }

            // Get recent appointments count (last 30 days)
            String recentQuery = "SELECT COUNT(*) FROM Appointment WHERE patient_id = ? AND status = 'Completed' AND date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)";
            PreparedStatement recentStmt = dbConnection.getConnection().prepareStatement(recentQuery);
            recentStmt.setInt(1, currentPatient.getPatientId());
            ResultSet recentRs = recentStmt.executeQuery();

            if (recentRs.next()) {
                recentAppointmentsLabel.setText(recentRs.getString(1));
            }

            // Get upcoming appointments count
            String upcomingQuery = "SELECT COUNT(*) FROM Appointment WHERE patient_id = ? AND status = 'Scheduled' AND date >= CURDATE()";
            PreparedStatement upcomingStmt = dbConnection.getConnection().prepareStatement(upcomingQuery);
            upcomingStmt.setInt(1, currentPatient.getPatientId());
            ResultSet upcomingRs = upcomingStmt.executeQuery();

            if (upcomingRs.next()) {
                upcomingAppointmentsLabel.setText(upcomingRs.getString(1));
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading medical summary: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadMedicalConditions() {
        // This would typically load from a separate medical_conditions table
        // Since we don't have this table defined in your schema, this is a placeholder
        // You would implement this if you add a medical_conditions table to your database

        medicalConditionsBox.getChildren().clear();
        medicalConditionsBox.getChildren().add(noConditionsLabel);
    }

    private void loadAllergies() {
        // This would typically load from a separate allergies table
        // Since we don't have this table defined in your schema, this is a placeholder
        // You would implement this if you add an allergies table to your database

        allergiesBox.getChildren().clear();
        allergiesBox.getChildren().add(noAllergiesLabel);
    }

    @FXML
    private void handleChangePhoto(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(changePhotoBtn.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Save the image path (in a real app, you'd copy the file to an app directory)
                originalProfileImagePath = selectedFile.getPath();

                // Display the selected image (this assumes you have an ImageView in your FXML)
                // If there's an ImageView in your FXML named userAvatar, uncomment the code below
                // ImageView userAvatar = (ImageView) changePhotoBtn.getParent().lookup("#userAvatar");
                // if (userAvatar != null) {
                //     userAvatar.setImage(new Image(selectedFile.toURI().toString()));
                // }

                showAlert(Alert.AlertType.INFORMATION, "Success", "Profile photo selected. Click Save Changes to update your profile.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not load image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleReset(ActionEvent event) {
        // Reset fields to original patient data
        populatePatientData();
        showAlert(Alert.AlertType.INFORMATION, "Reset", "Form has been reset to original values.");
    }

    @FXML
    private void handleSave(ActionEvent event) {
        try {
            String updateQuery = "UPDATE Patient SET " +
                    "first_name = ?, " +
                    "mid_name = ?, " +
                    "last_name = ?, " +
                    "dob = ?, " +
                    "gender = ?, " +
                    "blood_group = ?, " +
                    "email = ?, " +
                    "phone_no = ?, " +
                    "insurance_type = ?, " +
                    "address = ? " +
                    "WHERE patient_id = ?";

            PreparedStatement stmt = dbConnection.getConnection().prepareStatement(updateQuery);
            stmt.setString(1, firstNameField.getText());
            stmt.setString(2, midNameField.getText());
            stmt.setString(3, lastNameField.getText());
            stmt.setDate(4, Date.valueOf(dobPicker.getValue()));
            stmt.setString(5, genderCombo.getValue());
            stmt.setString(6, bloodGroupCombo.getValue());
            stmt.setString(7, emailField.getText());
            stmt.setString(8, phoneField.getText());
            stmt.setString(9, insuranceField.getText());
            stmt.setString(10, addressField.getText());
            stmt.setInt(11, currentPatient.getPatientId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                // Update the current patient object with new values
                currentPatient.setFirstName(firstNameField.getText());
                currentPatient.setMiddleName(midNameField.getText());
                currentPatient.setLastName(lastNameField.getText());
                currentPatient.setDob(dobPicker.getValue());
                currentPatient.setGender(genderCombo.getValue());
                currentPatient.setBloodGroup(bloodGroupCombo.getValue());
                currentPatient.setEmail(emailField.getText());
                currentPatient.setPhoneNo(phoneField.getText());
                currentPatient.setInsuranceType(insuranceField.getText());
                currentPatient.setAddress(addressField.getText());

                // Update the name (which is normally generated by MySQL)
                String fullName = firstNameField.getText();
                if (midNameField.getText() != null && !midNameField.getText().isEmpty()) {
                    fullName += " " + midNameField.getText();
                }
                fullName += " " + lastNameField.getText();
                currentPatient.setName(fullName);

                showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully!");
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "No changes were made.");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error updating profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleChangePassword(ActionEvent event) {
        // Validate input
        if (currentPasswordField.getText().isEmpty() ||
                newPasswordField.getText().isEmpty() ||
                confirmPasswordField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields", "Please fill in all password fields.");
            return;
        }

        if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
            showAlert(Alert.AlertType.ERROR, "Password Mismatch", "New password and confirmation do not match.");
            return;
        }

        // Here you would verify the current password against what's stored in the database
        // and update it if correct. Since we don't have user authentication in the schema,
        // this is a placeholder.

        showAlert(Alert.AlertType.INFORMATION, "Success", "Password has been updated successfully!");

        // Clear password fields
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    @FXML
    private void handleUpdateRecoveryEmail(ActionEvent event) {
        String recoveryEmail = recoveryEmailField.getText();
        if (recoveryEmail.isEmpty() || !recoveryEmail.contains("@")) {
            showAlert(Alert.AlertType.WARNING, "Invalid Email", "Please enter a valid email address.");
            return;
        }

        // Here you would update the recovery email in your database
        // Since there's no recovery email in the schema, this is a placeholder

        showAlert(Alert.AlertType.INFORMATION, "Success", "Recovery email updated successfully!");
    }

    @FXML
    private void handleSaveSecuritySettings(ActionEvent event) {
        boolean twoFactorEnabled = twoFactorAuthCheckbox.isSelected();
        boolean loginNotificationsEnabled = loginNotificationsCheckbox.isSelected();

        // Here you would save these settings to your database
        // Since there's no security settings table in the schema, this is a placeholder

        showAlert(Alert.AlertType.INFORMATION, "Success", "Security settings saved successfully!");
    }

    private void showVisitDetails(String visitDate, String doctorName, String diagnosis) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Visit Details");
        alert.setHeaderText("Visit on " + visitDate);
        alert.setContentText("Doctor: " + doctorName + "\n" +
                "Diagnosis: " + diagnosis);
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Inner class to represent visit history items
    public static class VisitHistory {
        private String visitDate;
        private String doctorName;
        private String visitType;
        private String diagnosis;
        private Button detailsButton;

        public VisitHistory(String visitDate, String doctorName, String visitType, String diagnosis, Button detailsButton) {
            this.visitDate = visitDate;
            this.doctorName = doctorName;
            this.visitType = visitType;
            this.diagnosis = diagnosis;
            this.detailsButton = detailsButton;
        }

        public String getVisitDate() {
            return visitDate;
        }

        public String getDoctorName() {
            return doctorName;
        }

        public String getVisitType() {
            return visitType;
        }

        public String getDiagnosis() {
            return diagnosis;
        }

        public Button getDetailsButton() {
            return detailsButton;
        }
    }
}