package com.example.db_project1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.io.FileNotFoundException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class DoctorEditFormController implements Initializable {

    // --- FXML fields matching the FXML file ---
    @FXML private TextField idField;
    @FXML private TextField firstNameField;
    @FXML private TextField middleNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private DatePicker dobPicker;
    @FXML private ComboBox<String> specializationComboBox;
    @FXML private Spinner<Integer> experienceSpinner;
    @FXML private TextField languageField;
    @FXML private TextArea scheduleArea;
    @FXML private ImageView doctorImageView;
    @FXML private Button uploadImageButton;
    @FXML private Button updateButton;
    @FXML private Button cancelButton;


    // --- Legacy fields for backward compatibility (do not remove) ---
    @FXML private TextField passwordField; // not in FXML, but kept for legacy
    @FXML private TextField contactField; // not in FXML, but kept for legacy
    @FXML private TextField experienceField; // not in FXML, but kept for legacy
    @FXML private TextField scheduleField; // not in FXML, but kept for legacy
    @FXML private Button saveButton; // not in FXML, but kept for legacy

    private Doctor currentDoctor;
    private File selectedImageFile = null;
    private AdminDoctorController adminDoctorController;
    private DatabaseConnection dbConnection;
    private boolean imageChanged = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize database connection
        dbConnection = new DatabaseConnection();

        // Set up specialization options
        ObservableList<String> specializations = FXCollections.observableArrayList(
                "Cardiology", "Dermatology", "Endocrinology", "Gastroenterology", "Neurology",
                "Obstetrics & Gynecology", "Oncology", "Ophthalmology", "Orthopedics",
                "Pediatrics", "Psychiatry", "Pulmonology", "Radiology", "Urology"
        );
        specializationComboBox.setItems(specializations);

        // Set up gender options
        ObservableList<String> genders = FXCollections.observableArrayList("Male", "Female", "Other");
        genderComboBox.setItems(genders);

        // Set up experience spinner (0-50 years)
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, 0);
        experienceSpinner.setValueFactory(valueFactory);
        experienceSpinner.setEditable(true);

        // Set up image upload button
        uploadImageButton.setOnAction(event -> handleImageUpload());

        // Set up update button (calls handleUpdate)
        updateButton.setOnAction(event -> handleUpdate(event));

        // Set up cancel button
        cancelButton.setOnAction(event -> handleCancel());
    }

    public void setDoctor(Doctor doctor) {
        this.currentDoctor = doctor;
        populateFields();
    }

    public void setAdminDoctorController(AdminDoctorController controller) {
        this.adminDoctorController = controller;
    }

    private void populateFields() {
        if (currentDoctor == null) return;

        // For new FXML fields
        idField.setText(String.valueOf(currentDoctor.getDoctorId()));
        firstNameField.setText(currentDoctor.getFirstName());
        middleNameField.setText(currentDoctor.getMiddleName() != null ? currentDoctor.getMiddleName() : "");
        lastNameField.setText(currentDoctor.getLastName());
        emailField.setText(currentDoctor.getEmail());
        phoneField.setText(currentDoctor.getPhoneNo());
        specializationComboBox.setValue(currentDoctor.getSpecialization());
        experienceSpinner.getValueFactory().setValue(currentDoctor.getExperience());
        languageField.setText(currentDoctor.getLanguage());
        scheduleArea.setText(currentDoctor.getSchedule());

        // Legacy fields for backward compatibility
        if (contactField != null) contactField.setText(currentDoctor.getPhoneNo());
        if (experienceField != null) experienceField.setText(String.valueOf(currentDoctor.getExperience()));
        if (scheduleField != null) scheduleField.setText(currentDoctor.getSchedule());

        // Load current doctor's image if available
        if (currentDoctor.getPicture() != null) {
            doctorImageView.setImage(currentDoctor.getPicture());
        } else {
            try {
                doctorImageView.setImage(new Image(getClass().getResourceAsStream("/images/default-avatar.png")));
            } catch (Exception e) {
                System.out.println("Failed to load default doctor avatar: " + e.getMessage());
            }
        }

        // Load gender and DOB from database
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            String query = "SELECT gender, dob FROM doctor WHERE doctor_id = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, currentDoctor.getDoctorId());
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String gender = rs.getString("gender");
                if (gender != null) {
                    genderComboBox.setValue(gender);
                }

                Date dob = rs.getDate("dob");
                if (dob != null) {
                    dobPicker.setValue(dob.toLocalDate());
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading doctor details: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // --- FXML event handlers required by the new FXML ---


    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    // --- Existing methods (unchanged logic, but now integrated with new FXML) ---
    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Doctor Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        // Show open file dialog
        Stage stage = (Stage) uploadImageButton.getScene().getWindow();
        selectedImageFile = fileChooser.showOpenDialog(stage);

        if (selectedImageFile != null) {
            try {
                Image image = new Image(new FileInputStream(selectedImageFile));
                doctorImageView.setImage(image);
                imageChanged = true;
            } catch (FileNotFoundException e) {
                System.out.println("Error loading image: " + e.getMessage());
                showErrorAlert("Image Error", "Failed to load selected image", e.getMessage());
            }
        }
    }

    private void handleSave() {
        // Validate required fields first
        if (!validateFields()) {
            return;
        }

        // Safely extract and trim all input fields, handling nulls
        String firstName = firstNameField.getText() != null ? firstNameField.getText().trim() : "";
        String middleName = middleNameField.getText() != null ? middleNameField.getText().trim() : "";
        String lastName = lastNameField.getText() != null ? lastNameField.getText().trim() : "";
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String phone = phoneField.getText() != null ? phoneField.getText().trim() : "";
        String specialization = specializationComboBox.getValue() != null ? specializationComboBox.getValue() : "";
        String gender = genderComboBox.getValue() != null ? genderComboBox.getValue() : "";
        String language = languageField.getText() != null ? languageField.getText().trim() : "";
        String schedule = scheduleArea.getText() != null ? scheduleArea.getText().trim() : "";

        // Update currentDoctor object with safe values
        currentDoctor.setFirstName(firstName);
        currentDoctor.setMiddleName(middleName);
        currentDoctor.setLastName(lastName);
        currentDoctor.setEmail(email);
        currentDoctor.setPhoneNo(phone);
        currentDoctor.setSpecialization(specialization);
        currentDoctor.setLanguage(language);
        currentDoctor.setSchedule(schedule);

        // Handle experience spinner safely
        int experienceVal = 0;
        try {
            experienceVal = experienceSpinner.getValue();
        } catch (Exception e) {
            experienceVal = 0;
        }
        currentDoctor.setExperience(experienceVal);

        // Handle image if changed
        if (imageChanged && selectedImageFile != null) {
            try {
                Image image = new Image(new FileInputStream(selectedImageFile));
                currentDoctor.setPicture(image);
            } catch (FileNotFoundException e) {
                System.out.println("Error loading image for save: " + e.getMessage());
            }
        }

        // Database update logic
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = dbConnection.getConnection();

            String sql = "UPDATE doctor SET first_name=?, mid_name=?, last_name=?, email=?, " +
                    "phone_no=?, gender=?, dob=?, specialization=?, experience=?, language=?, schedule=?";

            // Only update password if provided
            if (passwordField != null && passwordField.getText() != null && !passwordField.getText().isEmpty()) {
                sql += ", password=?";
            }

            // Only update image if a new one was selected
            if (imageChanged && selectedImageFile != null) {
                sql += ", picture=?";
            }

            sql += " WHERE doctor_id=?";

            pstmt = conn.prepareStatement(sql);
            int paramIndex = 1;

            pstmt.setString(paramIndex++, currentDoctor.getFirstName());
            pstmt.setString(paramIndex++, currentDoctor.getMiddleName());
            pstmt.setString(paramIndex++, currentDoctor.getLastName());
            pstmt.setString(paramIndex++, currentDoctor.getEmail());
            pstmt.setString(paramIndex++, currentDoctor.getPhoneNo());
            pstmt.setString(paramIndex++, gender);

            // Handle DOB safely
            if (dobPicker.getValue() != null) {
                pstmt.setDate(paramIndex++, Date.valueOf(dobPicker.getValue()));
            } else {
                pstmt.setNull(paramIndex++, Types.DATE);
            }

            pstmt.setString(paramIndex++, currentDoctor.getSpecialization());
            pstmt.setInt(paramIndex++, currentDoctor.getExperience());
            pstmt.setString(paramIndex++, currentDoctor.getLanguage());
            pstmt.setString(paramIndex++, currentDoctor.getSchedule());

            // Password parameter if provided
            if (passwordField != null && passwordField.getText() != null && !passwordField.getText().isEmpty()) {
                pstmt.setString(paramIndex++, passwordField.getText()); // Consider hashing password
            }

            // Image parameter if changed
            if (imageChanged && selectedImageFile != null) {
                FileInputStream fis = new FileInputStream(selectedImageFile);
                pstmt.setBinaryStream(paramIndex++, fis, (int) selectedImageFile.length());
            }

            // Doctor ID as last parameter
            pstmt.setInt(paramIndex, currentDoctor.getDoctorId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                if (adminDoctorController != null) {
                    adminDoctorController.updateDoctorInTable(currentDoctor);
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Doctor updated successfully!");
                alert.showAndWait();

                // Close the form
                Stage stage = (Stage) updateButton.getScene().getWindow();
                stage.close();
            } else {
                showErrorAlert("Database Error", "Failed to update doctor", "No rows were affected in the database.");
            }

        } catch (Exception e) {
            System.out.println("Error updating doctor: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Error", "Failed to update doctor", e.getMessage());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    private boolean validateFields() {
        StringBuilder errorMessages = new StringBuilder();

        if (firstNameField.getText().trim().isEmpty()) {
            errorMessages.append("First name is required\n");
        }
        if (lastNameField.getText().trim().isEmpty()) {
            errorMessages.append("Last name is required\n");
        }

        if (emailField.getText().trim().isEmpty()) {
            errorMessages.append("Email is required\n");
        } else if (!emailField.getText().trim().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            errorMessages.append("Invalid email format\n");
        }

        if (specializationComboBox.getValue() == null) {
            errorMessages.append("Specialization is required\n");
        }

        if (genderComboBox.getValue() == null) {
            errorMessages.append("Gender is required\n");
        }

        // Experience from spinner
        try {
            experienceSpinner.getValue();
        } catch (Exception e) {
            errorMessages.append("Experience must be a number\n");
        }

        if (errorMessages.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(errorMessages.toString());
            alert.showAndWait();
            return false;
        }

        return true;
    }

    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // The following two methods are kept for compatibility with FXML, but their logic is now handled above
    public void handleUpdate(ActionEvent actionEvent) { handleSave(); }
    public void handleUploadImage(ActionEvent actionEvent) { handleImageUpload(); }
}
