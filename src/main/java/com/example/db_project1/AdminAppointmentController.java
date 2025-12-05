package com.example.db_project1;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminAppointmentController implements Initializable {

    @FXML
    private TableView<AppointmentView> appointmentTable;

    @FXML
    private TableColumn<AppointmentView, String> patientNameColumn;

    @FXML
    private TableColumn<AppointmentView, String> appointmentNumberColumn;

    @FXML
    private TableColumn<AppointmentView, String> doctorColumn;

    @FXML
    private TableColumn<AppointmentView, String> sessionTitleColumn;

    @FXML
    private TableColumn<AppointmentView, String> sessionDateTimeColumn;

    @FXML
    private TableColumn<AppointmentView, String> appointmentDateColumn;

    @FXML
    private TableColumn<AppointmentView, Void> eventsColumn;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<Doctor> doctorComboBox;

    @FXML
    private Button filterButton;

    @FXML
    private Button backButton;

    @FXML
    private Label dateLabel;

    @FXML
    private Label appointmentCountLabel;

    private Admin currentAdmin;
    private DatabaseConnection dbConnection;
    private final ObservableList<AppointmentView> appointmentList = FXCollections.observableArrayList();
    private final Map<Integer, String> patientNames = new HashMap<>();
    private final Map<Integer, Doctor> doctors = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize database connection
        dbConnection = new DatabaseConnection();

        // Set up the datepicker format
        setupDatePicker();

        // Set up the doctor combo box
        loadDoctors();

        // Configure table columns
        setupTableColumns();

        // Set current date in the label
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        // Set up event handlers
        setupEventHandlers();

        // Load appointments initially without filters
        loadAppointments(null, null);
    }


    private void setupDatePicker() {
        // Set date picker format
        datePicker.setConverter(new StringConverter<LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return formatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, formatter);
                } else {
                    return null;
                }
            }
        });
    }

    private void setupTableColumns() {
        // Set up table columns
        patientNameColumn.setCellValueFactory(cellData -> cellData.getValue().patientNameProperty());
        appointmentNumberColumn.setCellValueFactory(cellData -> cellData.getValue().appointmentNumberProperty());
        doctorColumn.setCellValueFactory(cellData -> cellData.getValue().doctorNameProperty());
        sessionTitleColumn.setCellValueFactory(cellData -> cellData.getValue().sessionTitleProperty());
        sessionDateTimeColumn.setCellValueFactory(cellData -> cellData.getValue().sessionDateTimeProperty());
        appointmentDateColumn.setCellValueFactory(cellData -> cellData.getValue().appointmentDateProperty());

        // Add action buttons to the events column
        setupActionsColumn();
    }

    private void setupActionsColumn() {
        Callback<TableColumn<AppointmentView, Void>, TableCell<AppointmentView, Void>> cellFactory = param -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button cancelBtn = new Button("Cancel");
            private final Button completeBtn = new Button("Complete");

            {
                // Set button styles
                viewBtn.setStyle("-fx-background-color: #e3f0fc; -fx-text-fill: #1976d2; -fx-font-size: 12px;");
                cancelBtn.setStyle("-fx-background-color: #ffebee; -fx-text-fill: #d32f2f; -fx-font-size: 12px;");
                completeBtn.setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #388e3c; -fx-font-size: 12px;");

                // Configure view button action
                viewBtn.setOnAction(event -> {
                    AppointmentView appointment = getTableView().getItems().get(getIndex());
                    showAppointmentDetails(appointment);
                });

                // Configure cancel button action
                cancelBtn.setOnAction(event -> {
                    AppointmentView appointment = getTableView().getItems().get(getIndex());
                    cancelAppointment(appointment);
                });

                // Configure complete button action
                completeBtn.setOnAction(event -> {
                    AppointmentView appointment = getTableView().getItems().get(getIndex());
                    completeAppointment(appointment);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    AppointmentView appointment = getTableView().getItems().get(getIndex());

                    // Create HBox to hold buttons with spacing
                    javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(5);

                    // Add view button for all appointments
                    hbox.getChildren().add(viewBtn);

                    // Only show cancel button for scheduled appointments
                    if ("Scheduled".equals(appointment.getStatus())) {
                        hbox.getChildren().add(cancelBtn);
                       //hbox.getChildren().add(completeBtn);
                    }

                    setGraphic(hbox);
                }
            }
        };

        eventsColumn.setCellFactory(cellFactory);
    }

    private void setupEventHandlers() {
        // Filter button action
        filterButton.setOnAction(this::handleFilter);

        // Back button action
        backButton.setOnAction(event -> {
            try {
                // Go back to admin dashboard
                // This should be handled by your existing navigation system
                System.out.println("Back button pressed");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Error returning to dashboard: " + e.getMessage());
            }
        });
    }

    public void setAdmin(Admin admin) {
        this.currentAdmin = admin;
        System.out.println("Admin set in appointment controller: " + (admin != null ? admin.getName() : "null"));
    }

    public void loadData() {
        // Refresh appointment data
        loadAppointments(null, null);
    }

    private void loadDoctors() {
        ObservableList<Doctor> doctorsList = FXCollections.observableArrayList();

        try {
            String query = "SELECT doctor_id, CONCAT(first_name, ' ', last_name) AS name, specialization " +
                    "FROM Doctor ORDER BY name";

            ResultSet rs = dbConnection.executeQuery(query);

            while (rs != null && rs.next()) {
                int id = rs.getInt("doctor_id");
                String name = rs.getString("name");
                String specialization = rs.getString("specialization");

                Doctor doctor = new Doctor();
                doctor.setDoctorId(id);
                doctor.setFirstName(name); // Using first_name field to store full name for simplicity
                doctor.setSpecialization(specialization);

                doctorsList.add(doctor);
                doctors.put(id, doctor);
            }

            if (rs != null) rs.close();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading doctors: " + e.getMessage());
        }

        doctorComboBox.setItems(doctorsList);

        // Set cell factory to display doctor name
        doctorComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Doctor item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getFirstName() + " - " + item.getSpecialization());
                }
            }
        });

        // Set converter for selection display
        doctorComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Doctor doctor) {
                if (doctor == null) {
                    return null;
                }
                return doctor.getFirstName() + " - " + doctor.getSpecialization();
            }

            @Override
            public Doctor fromString(String string) {
                // Not needed for ComboBox
                return null;
            }
        });
    }

    private void loadAppointments(LocalDate filterDate, Integer filterDoctorId) {
        appointmentList.clear();
        patientNames.clear();

        try {
            // First, fetch all patient names for later use
            loadPatientNames();

            // Build the query with potential filters
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT a.appoint_id, a.patient_id, a.doctor_id, a.date, a.time, ")
                    .append("a.specialization, a.status, a.consultation_fee, a.type, ")
                    .append("CONCAT(d.first_name, ' ', d.last_name) AS doctor_name ")
                    .append("FROM Appointment a ")
                    .append("JOIN Doctor d ON a.doctor_id = d.doctor_id ");

            // Add filters if applicable
            if (filterDate != null || filterDoctorId != null) {
                queryBuilder.append("WHERE ");

                if (filterDate != null) {
                    queryBuilder.append("a.date = ? ");
                }

                if (filterDate != null && filterDoctorId != null) {
                    queryBuilder.append("AND ");
                }

                if (filterDoctorId != null) {
                    queryBuilder.append("a.doctor_id = ? ");
                }
            }

            queryBuilder.append("ORDER BY a.date DESC, a.time ASC");

            // Prepare and execute statement
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString());

            // Set parameters if applicable
            int paramIndex = 1;
            if (filterDate != null) {
                stmt.setString(paramIndex++, filterDate.toString());
            }

            if (filterDoctorId != null) {
                stmt.setInt(paramIndex, filterDoctorId);
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int appointId = rs.getInt("appoint_id");
                int patientId = rs.getInt("patient_id");
                int doctorId = rs.getInt("doctor_id");
                String date = rs.getString("date");
                String time = rs.getString("time");
                String specialization = rs.getString("specialization");
                String status = rs.getString("status");
                double fee = rs.getDouble("consultation_fee");
                String type = rs.getString("type");
                String doctorName = rs.getString("doctor_name");

                // Get patient name from our cache
                String patientName = patientNames.getOrDefault(patientId, "Unknown Patient");

                // Create AppointmentView object
                AppointmentView appointmentView = new AppointmentView(
                        appointId, patientId, doctorId, patientName, doctorName,
                        specialization, date, time, status, fee, type
                );

                appointmentList.add(appointmentView);
            }

            rs.close();
            stmt.close();

            // Update table with new data
            appointmentTable.setItems(appointmentList);

            // Update count label
            appointmentCountLabel.setText("All Appointments (" + appointmentList.size() + ")");

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading appointments: " + e.getMessage());
        }
    }
    private boolean patientExists(int patientId, Connection conn) throws SQLException {
        String sql = "SELECT 1 FROM patient WHERE patient_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean adminExists(int adminId, Connection conn) throws SQLException {
        String sql = "SELECT 1 FROM admin WHERE admin_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, adminId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean doctorExists(int doctorId, Connection conn) throws SQLException {
        String sql = "SELECT 1 FROM doctor WHERE doctor_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }


    private void loadPatientNames() {
        try {
            String query = "SELECT patient_id, CONCAT(first_name, ' ', last_name) AS full_name FROM Patient";
            ResultSet rs = dbConnection.executeQuery(query);

            while (rs != null && rs.next()) {
                int id = rs.getInt("patient_id");
                String name = rs.getString("full_name");
                patientNames.put(id, name);
            }

            if (rs != null) rs.close();

        } catch (SQLException e) {
            System.err.println("Error loading patient names: " + e.getMessage());
        }
    }

    @FXML
    private void handleFilter(ActionEvent event) {
        LocalDate selectedDate = datePicker.getValue();
        Doctor selectedDoctor = doctorComboBox.getValue();
        Integer doctorId = (selectedDoctor != null) ? selectedDoctor.getDoctorId() : null;

        loadAppointments(selectedDate, doctorId);
    }

    private void showAppointmentDetails(AppointmentView appointment) {
        // Create content for the dialog
        StringBuilder content = new StringBuilder();
        content.append("Appointment #").append(appointment.getAppointmentId()).append("\n\n");
        content.append("Patient: ").append(appointment.getPatientName()).append("\n");
        content.append("Doctor: ").append(appointment.getDoctorName()).append("\n");
        content.append("Date: ").append(appointment.getDate()).append("\n");
        content.append("Time: ").append(appointment.getTime()).append("\n");
        content.append("Type: ").append(appointment.getType()).append("\n");
        content.append("Specialization: ").append(appointment.getSpecialization()).append("\n");
        content.append("Fee: $").append(String.format("%.2f", appointment.getFee())).append("\n");
        content.append("Status: ").append(appointment.getStatus());

        // Show the dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Appointment Details");
        alert.setHeaderText("Appointment Information");
        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    private void cancelAppointment(AppointmentView appointment) {
        // Confirm before cancelling
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Cancel Appointment");
        confirmAlert.setHeaderText("Cancel Appointment #" + appointment.getAppointmentId());
        confirmAlert.setContentText("Are you sure you want to cancel this appointment?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Update appointment status in database
                String query = "UPDATE Appointment SET status = 'Cancelled' WHERE appoint_id = ?";
                int affected = dbConnection.executePreparedUpdate(query, appointment.getAppointmentId());

                if (affected > 0) {
                    // Update the local view
                    appointment.setStatus("Cancelled");
                    appointmentTable.refresh();

                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Appointment #" + appointment.getAppointmentId() + " has been cancelled successfully.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to cancel appointment. Please try again.");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Database Error",
                        "Error updating appointment: " + e.getMessage());
            }
        }
    }
    private Admin loggedInAdmin;

    public void setLoggedInAdmin(Admin admin) {
        this.loggedInAdmin = admin;
    }

    public int getLoggedInAdminId() {
        if (loggedInAdmin != null) {
            return loggedInAdmin.getAdminId();
        } else {
            return -1; // or handle no admin logged in case
        }
    }
    private com.example.db_project1.Doctor loggedInDoctor;

    public void setLoggedInDoctor(com.example.db_project1.Doctor doctor) {
        this.loggedInDoctor = doctor;
    }
    public int getLoggedInDoctorId() {
        if (loggedInDoctor != null) {
            return loggedInDoctor.getDoctorId();
        } else {
            // doctor not set, return a default value or handle error
            return -1;
        }
    }
    private void completeAppointment(AppointmentView appointment) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Complete Appointment");
        confirmAlert.setHeaderText("Mark Appointment #" + appointment.getAppointmentId() + " as Completed");
        confirmAlert.setContentText("Are you sure you want to mark this appointment as completed?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);

                // Validate patient exists
                if (!recordExists("patient", "patient_id", appointment.getPatientId(), conn)) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Patient ID does not exist.");
                    conn.rollback();
                    return;
                }

                // Get logged-in admin ID safely
                Integer loggedInAdminId = getLoggedInAdminId();

                // Validate admin exists if admin ID is present
                if (loggedInAdminId != null && !recordExists("admin", "admin_id", loggedInAdminId, conn)) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Admin ID does not exist.");
                    conn.rollback();
                    return;
                }

                // Get logged-in doctor ID if you have it, else null
                Integer loggedInDoctorId = getLoggedInDoctorId(); // Implement this method similarly
                if (loggedInDoctorId != null && !recordExists("doctor", "doctor_id", loggedInDoctorId, conn)) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Doctor ID does not exist.");
                    conn.rollback();
                    return;
                }

                // Debug prints
                System.out.println("Inserting bill with:");
                System.out.println("Patient ID: " + appointment.getPatientId());
                System.out.println("Admin ID: " + loggedInAdminId);
                System.out.println("Doctor ID: " + loggedInDoctorId);

                // Step 1: Update appointment status
                String updateQuery = "UPDATE Appointment SET status = 'Completed' WHERE appoint_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                    updateStmt.setInt(1, appointment.getAppointmentId());
                    int affected = updateStmt.executeUpdate();
                    if (affected <= 0) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to update appointment status.");
                        conn.rollback();
                        return;
                    }
                }

                // Step 2: Insert bill record
                String billQuery = "INSERT INTO Bill (patient_id, date, time, amount, payment_status, admin_id, doctor_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement billStmt = conn.prepareStatement(billQuery)) {
                    billStmt.setInt(1, appointment.getPatientId());
                    billStmt.setDate(2, java.sql.Date.valueOf(java.time.LocalDate.now()));
                    billStmt.setTime(3, java.sql.Time.valueOf(java.time.LocalTime.now()));
                    billStmt.setDouble(4, appointment.getFee());
                    billStmt.setString(5, "Pending"); // ENUM value must match exactly

                    if (loggedInAdminId != null) {
                        billStmt.setInt(6, loggedInAdminId);
                    } else {
                        billStmt.setNull(6, java.sql.Types.INTEGER);
                    }

                    if (loggedInDoctorId != null) {
                        billStmt.setInt(7, loggedInDoctorId);
                    } else {
                        billStmt.setNull(7, java.sql.Types.INTEGER);
                    }

                    int billRows = billStmt.executeUpdate();
                    if (billRows <= 0) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to generate bill.");
                        conn.rollback();
                        return;
                    }
                }

                // Commit transaction
                conn.commit();

                // Update local view and refresh UI
                appointment.setStatus("Completed");
                appointmentTable.refresh();

                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Appointment #" + appointment.getAppointmentId() + " has been marked as completed and bill generated.");

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Error updating appointment: " + e.getMessage());
            }
        }
    }

    private boolean recordExists(String tableName, String columnName, int id, Connection conn) throws SQLException {
        String sql = "SELECT 1 FROM " + tableName + " WHERE " + columnName + " = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }


    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class AppointmentView {
        private final int appointmentId;
        private final int patientId;
        private final int doctorId;
        private final SimpleStringProperty patientName;
        private final SimpleStringProperty appointmentNumber;
        private final SimpleStringProperty doctorName;
        private final SimpleStringProperty sessionTitle;
        private final SimpleStringProperty sessionDateTime;
        private final SimpleStringProperty appointmentDate;
        private final String date;
        private final String time;
        private final String specialization;
        private String status;
        private final double fee;
        private final String type;

        public AppointmentView(int appointmentId, int patientId, int doctorId, String patientName,
                               String doctorName, String specialization, String date, String time,
                               String status, double fee, String type) {
            this.appointmentId = appointmentId;
            this.patientId = patientId;
            this.doctorId = doctorId;
            this.patientName = new SimpleStringProperty(patientName);
            this.appointmentNumber = new SimpleStringProperty("#" + appointmentId);
            this.doctorName = new SimpleStringProperty(doctorName);
            this.sessionTitle = new SimpleStringProperty(specialization + " - " + type);
            this.sessionDateTime = new SimpleStringProperty(date + " " + time);
            this.appointmentDate = new SimpleStringProperty(date);
            this.date = date;
            this.time = time;
            this.specialization = specialization;
            this.status = status;
            this.fee = fee;
            this.type = type;
        }


        public SimpleStringProperty patientNameProperty() {
            return patientName;
        }

        public SimpleStringProperty appointmentNumberProperty() {
            return appointmentNumber;
        }

        public SimpleStringProperty doctorNameProperty() {
            return doctorName;
        }

        public SimpleStringProperty sessionTitleProperty() {
            return sessionTitle;
        }

        public SimpleStringProperty sessionDateTimeProperty() {
            return sessionDateTime;
        }

        public SimpleStringProperty appointmentDateProperty() {
            return appointmentDate;
        }

        public int getAppointmentId() {
            return appointmentId;
        }

        public int getPatientId() {
            return patientId;
        }

        public int getDoctorId() {
            return doctorId;
        }

        public String getPatientName() {
            return patientName.get();
        }

        public String getDoctorName() {
            return doctorName.get();
        }

        public String getDate() {
            return date;
        }

        public String getTime() {
            return time;
        }

        public String getSpecialization() {
            return specialization;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public double getFee() {
            return fee;
        }

        public String getType() {
            return type;
        }
    }

    public static class Doctor {
        private int doctorId;
        private String firstName;
        private String lastName;
        private String specialization;

        public int getDoctorId() {
            return doctorId;
        }

        public void setDoctorId(int doctorId) {
            this.doctorId = doctorId;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getSpecialization() {
            return specialization;
        }

        public void setSpecialization(String specialization) {
            this.specialization = specialization;
        }

        @Override
        public String toString() {
            return firstName + " " + lastName + " - " + specialization;
        }
    }
}