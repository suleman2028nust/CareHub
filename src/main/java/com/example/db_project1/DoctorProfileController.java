package com.example.db_project1;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DoctorProfileController implements Initializable {

    @FXML
    private Label dateLabel;

    @FXML
    private ImageView doctorImageView;

    @FXML
    private Label doctorNameLabel;

    @FXML
    private Label doctorSpecializationLabel;

    @FXML
    private Label experienceLabel;

    @FXML
    private Label fullNameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label phoneLabel;

    @FXML
    private Label genderLabel;

    @FXML
    private Label dobLabel;

    @FXML
    private Label doctorIdLabel;

    @FXML
    private Label specializationDetailLabel;

    @FXML
    private Label experienceDetailLabel;

    @FXML
    private Label languagesLabel;

    @FXML
    private Label scheduleLabel;

    private Doctor currentDoctor;
    private DatabaseConnection dbConnection;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize database connection
        dbConnection = new DatabaseConnection();

        // Set current date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        dateLabel.setText(LocalDate.now().format(formatter));

        // Set default doctor image if needed
        setDefaultDoctorImage();
    }

    /**
     * Sets the doctor object and loads doctor data into the profile view
     * @param doctor The current logged-in doctor
     */
    public void setDoctor(Doctor doctor) {
        this.currentDoctor = doctor;
        if (doctor != null) {
            System.out.println("Loading profile for doctor ID: " + doctor.getDoctorId());
            loadData();
        } else {
            System.err.println("Error: Doctor object is null in ProfileController");
        }
    }

    /**
     * Loads doctor data from database using the current doctor ID
     */
    public void loadData() {
        if (currentDoctor == null || currentDoctor.getDoctorId() <= 0) {
            System.err.println("Cannot load doctor data: Invalid doctor ID");
            return;
        }

        try {
            // Get doctor data from database using prepared statement
            String query = "SELECT doctor_id, first_name, mid_name, last_name, specialization, " +
                    "experience, language, email, phone_no, schedule, " +
                    "gender, dob, picture FROM doctor WHERE doctor_id = ?";

            Connection conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, currentDoctor.getDoctorId());

            System.out.println("Executing query for doctor ID: " + currentDoctor.getDoctorId());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Update current doctor object with all fields from database
                currentDoctor.setDoctorId(rs.getInt("doctor_id"));
                currentDoctor.setFirstName(rs.getString("first_name"));
                currentDoctor.setMiddleName(rs.getString("mid_name"));
                currentDoctor.setLastName(rs.getString("last_name"));
                currentDoctor.setSpecialization(rs.getString("specialization"));
                currentDoctor.setExperience(rs.getInt("experience"));
                currentDoctor.setLanguage(rs.getString("language"));
                currentDoctor.setEmail(rs.getString("email"));

                // Use the correct column name here
                currentDoctor.setPhoneNo(rs.getString("phone_no"));

                currentDoctor.setSchedule(rs.getString("schedule"));
                currentDoctor.setGender(rs.getString("gender"));

                // Get date of birth if available
                java.sql.Date dbDob = rs.getDate("dob");
                if (dbDob != null) {
                    currentDoctor.setDob(dbDob);
                }

                // Get doctor image if available
                java.sql.Blob blob = rs.getBlob("picture");
                if (blob != null && blob.length() > 0) {
                    try (java.io.InputStream is = blob.getBinaryStream()) {
                        Image img = new Image(is);
                        currentDoctor.setPicture(img);
                        doctorImageView.setImage(img);
                        System.out.println("Doctor image loaded successfully");
                    } catch (Exception e) {
                        System.err.println("Error loading doctor image: " + e.getMessage());
                        setDefaultDoctorImage();
                    }
                } else {
                    System.out.println("No profile image found, using default");
                    setDefaultDoctorImage();
                }

                // Update UI with doctor information
                updateProfileUI();
                System.out.println("Doctor profile loaded successfully: " + currentDoctor.getName());
            } else {
                System.err.println("No doctor found with ID: " + currentDoctor.getDoctorId());
            }

            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            System.err.println("Database error while loading doctor profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Updates the UI with current doctor information
     */
    private void updateProfileUI() {
        try {
            // Basic info in the sidebar
            doctorNameLabel.setText("Dr. " + currentDoctor.getName());
            doctorSpecializationLabel.setText(currentDoctor.getSpecialization() != null ?
                    currentDoctor.getSpecialization() : "Not specified");
            experienceLabel.setText(currentDoctor.getExperience() + " years");

            // Personal information section
            fullNameLabel.setText("Dr. " + currentDoctor.getName());
            emailLabel.setText(currentDoctor.getEmail() != null ?
                    currentDoctor.getEmail() : "Not specified");
            phoneLabel.setText(currentDoctor.getPhoneNo() != null ?
                    currentDoctor.getPhoneNo() : "Not specified");
            genderLabel.setText(currentDoctor.getGender() != null ?
                    currentDoctor.getGender() : "Not specified");

            // Format and display date of birth if available
            if (currentDoctor.getDob() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                dobLabel.setText(dateFormat.format(currentDoctor.getDob()));
            } else {
                dobLabel.setText("Not specified");
            }

            // Doctor ID display
            doctorIdLabel.setText("D" + String.format("%05d", currentDoctor.getDoctorId()));

            // Professional information section
            specializationDetailLabel.setText(currentDoctor.getSpecialization() != null ?
                    currentDoctor.getSpecialization() : "Not specified");
            experienceDetailLabel.setText(currentDoctor.getExperience() + " years");
            languagesLabel.setText(currentDoctor.getLanguage() != null ?
                    currentDoctor.getLanguage() : "Not specified");
            scheduleLabel.setText(currentDoctor.getSchedule() != null ?
                    currentDoctor.getSchedule() : "Not specified");

            System.out.println("Profile UI updated successfully");
        } catch (Exception e) {
            System.err.println("Error updating profile UI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sets a default placeholder image for the doctor
     */
    private void setDefaultDoctorImage() {
        try {
            // Load default doctor avatar
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/doctor-avatar.png"));
            doctorImageView.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("Error loading default doctor image: " + e.getMessage());
        }
    }
}