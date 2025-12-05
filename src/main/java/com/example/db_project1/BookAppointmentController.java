
package com.example.db_project1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class BookAppointmentController implements Initializable {

    @FXML
    private Label dateLabel;

    @FXML
    private ComboBox<String> appointmentTypeCombo;

    @FXML
    private ComboBox<String> specializationCombo;

    @FXML
    private Button searchDoctorsBtn;

    @FXML
    private ListView<Doctor> doctorsListView;

    @FXML
    private VBox doctorDetailsPane;

    @FXML
    private Label doctorNameLabel;

    @FXML
    private Label doctorSpecializationLabel;

    @FXML
    private Label doctorExperienceLabel;

    @FXML
    private Label doctorLanguagesLabel;

    @FXML
    private Label doctorScheduleLabel;

    @FXML
    private DatePicker appointmentDatePicker;

    @FXML
    private ComboBox<String> appointmentTimeCombo;

    @FXML
    private Label summaryDoctorLabel;

    @FXML
    private Label summarySpecializationLabel;

    @FXML
    private Label summaryDateLabel;

    @FXML
    private Label summaryTimeLabel;

    @FXML
    private Label summaryTypeLabel;

    @FXML
    private Label summaryFeeLabel;

    @FXML
    private Button cancelBtn;

    @FXML
    private Button bookBtn;
    private Set<DayOfWeek> allowedDays = new HashSet<>();
    private LocalTime scheduleStart;
    private LocalTime scheduleEnd;


    private Patient currentPatient;
    private DatabaseConnection dbConnection;
    private ObservableList<Doctor> doctors = FXCollections.observableArrayList();
    private Doctor selectedDoctor;
    private Map<String, Double> consultationFees = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dbConnection = new DatabaseConnection();

        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        appointmentTypeCombo.setItems(FXCollections.observableArrayList(
                "New Visit", "Follow-up", "Emergency", "Consultation"
        ));

        consultationFees.put("New Visit", 1200.00);
        consultationFees.put("Follow-up", 750.00);
        consultationFees.put("Emergency", 1500.00);
        consultationFees.put("Consultation", 900.00);

        loadSpecializations();

        appointmentDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                // Disable past dates
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        // This will be updated dynamically based on selected doctor and date
        appointmentTimeCombo.setItems(FXCollections.observableArrayList());

        setupListeners();
    }


    private void loadSpecializations() {
        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT DISTINCT specialization FROM Doctor ORDER BY specialization";
            ResultSet rs = dbConnection.executeQuery(query);

            List<String> specializations = new ArrayList<>();
            while (rs.next()) {
                specializations.add(rs.getString("specialization"));
            }

            specializationCombo.setItems(FXCollections.observableArrayList(specializations));

        } catch (SQLException e) {
            showAlert("Error", "Failed to load specializations: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private List<String> generateTimeSlots() {
        List<String> slots = new ArrayList<>();
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(17, 0);

        while (start.isBefore(end)) {
            slots.add(start.format(DateTimeFormatter.ofPattern("HH:mm")));
            start = start.plusMinutes(30);
        }

        return slots;
    }


    private void setupListeners() {
        doctorsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedDoctor = newVal;
                displayDoctorDetails(selectedDoctor);
                doctorDetailsPane.setVisible(true);
                appointmentTimeCombo.getItems().clear(); // Reset time slots
                appointmentDatePicker.setValue(null); // Reset date picker
                updateSummary();
            }
        });

        appointmentTypeCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateSummary();
            }
        });

        appointmentDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateTimeSlotsForDoctorAndDate();
                updateSummary();
            }
        });

        appointmentTimeCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateSummary();
            }
        });
    }
    private void updateTimeSlotsForDoctorAndDate() {
        appointmentTimeCombo.getItems().clear();

        if (selectedDoctor == null || appointmentDatePicker.getValue() == null) return;

        DayOfWeek selectedDay = appointmentDatePicker.getValue().getDayOfWeek();
        if (!allowedDays.contains(selectedDay)) {
            showAlert("Not Available", "This doctor is not available on " + selectedDay, Alert.AlertType.INFORMATION);
            return;
        }

        List<String> availableSlots = getAvailableTimeSlotsForDate(appointmentDatePicker.getValue());

        if (availableSlots.isEmpty()) {
            showAlert("No Slots", "No available time slots on this date.", Alert.AlertType.INFORMATION);
        }

        appointmentTimeCombo.setItems(FXCollections.observableArrayList(availableSlots));
    }



    private void displayDoctorDetails(Doctor doctor) {
        doctorNameLabel.setText(doctor.getName());
        doctorSpecializationLabel.setText(doctor.getSpecialization());
        doctorExperienceLabel.setText(doctor.getExperience() + " years");
        doctorLanguagesLabel.setText(doctor.getLanguage());
        doctorScheduleLabel.setText(doctor.getSchedule());

        // Parse schedule string
        parseScheduleDays(doctor.getSchedule());
        parseScheduleTimeRange(doctor.getSchedule());

        // Update the appointment date picker to allow only valid days
        appointmentDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                boolean isValid = !empty && !date.isBefore(LocalDate.now()) && allowedDays.contains(date.getDayOfWeek());
                setDisable(!isValid);
            }
        });

        // Refresh time slots if doctor or date changed
        if (appointmentDatePicker.getValue() != null && allowedDays.contains(appointmentDatePicker.getValue().getDayOfWeek())) {
            updateAvailableTimeSlots();
        } else {
            appointmentTimeCombo.getItems().clear();
        }
    }


    private void updateSummary() {
        if (selectedDoctor != null) {
            summaryDoctorLabel.setText(selectedDoctor.getName());
            summarySpecializationLabel.setText(selectedDoctor.getSpecialization());
        }

        if (appointmentDatePicker.getValue() != null) {
            summaryDateLabel.setText(appointmentDatePicker.getValue().toString());
        }

        if (appointmentTimeCombo.getValue() != null) {
            summaryTimeLabel.setText(appointmentTimeCombo.getValue());
        }

        if (appointmentTypeCombo.getValue() != null) {
            String type = appointmentTypeCombo.getValue();
            summaryTypeLabel.setText(type);

            // Get consultation fee based on type
            double fee = consultationFees.getOrDefault(type, 0.0);
            summaryFeeLabel.setText(String.format("$%.2f", fee));
        }
    }
    private void parseScheduleTimeRange(String schedule) {
        scheduleStart = LocalTime.of(9, 0);
        scheduleEnd = LocalTime.of(17, 0); // default values

        if (schedule == null || !schedule.contains(":")) return;

        String[] parts = schedule.split(":");
        if (parts.length < 2) return;

        String[] timeParts = parts[1].trim().split("-");
        if (timeParts.length == 2) {
            try {
                scheduleStart = LocalTime.parse(timeParts[0].trim(), DateTimeFormatter.ofPattern("HH:mm"));
                scheduleEnd = LocalTime.parse(timeParts[1].trim(), DateTimeFormatter.ofPattern("HH:mm"));
            } catch (Exception e) {
                System.err.println("Invalid time range: " + schedule);
            }
        }
    }
    private void updateAvailableTimeSlots() {
        List<String> timeSlots = new ArrayList<>();
        LocalTime time = scheduleStart;

        while (!time.isAfter(scheduleEnd.minusMinutes(30))) {
            timeSlots.add(time.format(DateTimeFormatter.ofPattern("HH:mm")));
            time = time.plusMinutes(30);
        }

        appointmentTimeCombo.setItems(FXCollections.observableArrayList(timeSlots));
    }

    private List<String> getAvailableTimeSlotsForDate(LocalDate date) {
        List<String> timeSlots = new ArrayList<>();
        LocalTime time = scheduleStart;

        while (!time.isAfter(scheduleEnd.minusMinutes(30))) {
            String slot = time.format(DateTimeFormatter.ofPattern("HH:mm"));
            if (isTimeSlotAvailable(selectedDoctor.getDoctorId(), date.toString(), slot)) {
                timeSlots.add(slot);
            }
            time = time.plusMinutes(30);
        }

        return timeSlots;
    }

    private void parseScheduleDays(String schedule) {
        allowedDays.clear();
        if (schedule == null || !schedule.contains(":")) return;

        String dayPart = schedule.split(":")[0].trim(); // e.g., "Mon-Wed-Fri"
        String[] days = dayPart.split("-");

        for (String day : days) {
            switch (day.trim().toLowerCase()) {
                case "mon" -> allowedDays.add(DayOfWeek.MONDAY);
                case "tue" -> allowedDays.add(DayOfWeek.TUESDAY);
                case "wed" -> allowedDays.add(DayOfWeek.WEDNESDAY);
                case "thu" -> allowedDays.add(DayOfWeek.THURSDAY);
                case "fri" -> allowedDays.add(DayOfWeek.FRIDAY);
                case "sat" -> allowedDays.add(DayOfWeek.SATURDAY);
                case "sun" -> allowedDays.add(DayOfWeek.SUNDAY);
            }
        }
    }

    @FXML
    void searchDoctors(ActionEvent event) {
        String specialization = specializationCombo.getValue();

        if (specialization == null || specialization.isEmpty()) {
            showAlert("Warning", "Please select a specialization", Alert.AlertType.WARNING);
            return;
        }

        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT * FROM Doctor WHERE specialization = ? ORDER BY first_name, mid_name, last_name";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, specialization);

            ResultSet rs = stmt.executeQuery();

            doctors.clear();
            while (rs.next()) {
                Image picture = null;
                Blob pictureBlob = rs.getBlob("picture");  // Replace "picture" with your actual image column name
                if (pictureBlob != null) {
                    try (InputStream is = pictureBlob.getBinaryStream()) {
                        picture = new Image(is);
                    } catch (Exception e) {
                        e.printStackTrace();
                        picture = null;
                    }
                }

                Doctor doctor = new Doctor(
                        rs.getInt("doctor_id"),
                        rs.getString("first_name"),
                        rs.getString("mid_name"),
                        rs.getString("last_name"),
                        rs.getString("specialization"),
                        rs.getInt("experience"),
                        rs.getString("language"),
                        rs.getString("email"),
                        rs.getString("phone_no"),
                        rs.getString("schedule"),
                        picture  // pass the image here
                );

                doctors.add(doctor);
            }

            doctorsListView.setItems(doctors);

            if (doctors.isEmpty()) {
                showAlert("Information", "No doctors found for the selected specialization", Alert.AlertType.INFORMATION);
            }

        } catch (SQLException e) {
            showAlert("Error", "Failed to search doctors: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void bookAppointment(ActionEvent event) {
        // Validate all fields are filled
        if (selectedDoctor == null) {
            showAlert("Warning", "Please select a doctor", Alert.AlertType.WARNING);
            return;
        }

        if (appointmentTypeCombo.getValue() == null) {
            showAlert("Warning", "Please select appointment type", Alert.AlertType.WARNING);
            return;
        }

        if (appointmentDatePicker.getValue() == null) {
            showAlert("Warning", "Please select appointment date", Alert.AlertType.WARNING);
            return;
        }

        if (appointmentTimeCombo.getValue() == null) {
            showAlert("Warning", "Please select appointment time", Alert.AlertType.WARNING);
            return;
        }

        // Check if time slot is available
        if (!isTimeSlotAvailable(selectedDoctor.getDoctorId(),
                appointmentDatePicker.getValue().toString(),
                appointmentTimeCombo.getValue())) {
            showAlert("Warning", "This time slot is already booked. Please select another time.", Alert.AlertType.WARNING);
            return;
        }

        // Proceed with booking
        try {
            Connection conn = dbConnection.getConnection();
            String query = "INSERT INTO Appointment (patient_id, doctor_id, date, time, specialization, status, consultation_fee, type) " +
                    "VALUES (?, ?, ?, ?, ?, 'Scheduled', ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, currentPatient.getPatientId());
            stmt.setInt(2, selectedDoctor.getDoctorId());
            stmt.setString(3, appointmentDatePicker.getValue().toString());
            stmt.setString(4, appointmentTimeCombo.getValue());
            stmt.setString(5, selectedDoctor.getSpecialization());

            // Get fee based on appointment type
            double fee = consultationFees.getOrDefault(appointmentTypeCombo.getValue(), 0.0);
            stmt.setDouble(6, fee);

            stmt.setString(7, appointmentTypeCombo.getValue());

            int result = stmt.executeUpdate();

            if (result > 0) {
                showAlert("Success", "Appointment booked successfully!", Alert.AlertType.INFORMATION);
                closeWindow();
            } else {
                showAlert("Error", "Failed to book appointment", Alert.AlertType.ERROR);
            }

        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean isTimeSlotAvailable(int doctorId, String date, String time) {
        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT COUNT(*) FROM Appointment WHERE doctor_id = ? AND date = ? AND time = ? AND status = 'Scheduled'";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, doctorId);
            stmt.setString(2, date);
            stmt.setString(3, time);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                return count == 0; // If count is 0, time slot is available
            }

            return true;

        } catch (SQLException e) {
            showAlert("Error", "Failed to check time slot availability: " + e.getMessage(), Alert.AlertType.ERROR);
            return false;
        }
    }

    @FXML
    void cancelAppointment(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setPatient(Patient patient) {
        this.currentPatient = patient;
    }
}