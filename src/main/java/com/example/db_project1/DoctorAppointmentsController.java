package com.example.db_project1;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DoctorAppointmentsController implements Initializable {

    @FXML
    private Label appointmentCountLabel;

    @FXML
    private Button backBtn;

    @FXML
    private TextField searchField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private Button searchBtn;

    @FXML
    private ComboBox<String> statusFilterComboBox;

    @FXML
    private Button refreshBtn;

    @FXML
    private TableView<AppointmentWithPatient> appointmentsTable;

    @FXML
    private TableColumn<AppointmentWithPatient, Integer> idColumn;

    @FXML
    private TableColumn<AppointmentWithPatient, String> patientNameColumn;

    @FXML
    private TableColumn<AppointmentWithPatient, String> dateColumn;

    @FXML
    private TableColumn<AppointmentWithPatient, String> timeColumn;

    @FXML
    private TableColumn<AppointmentWithPatient, String> typeColumn;

    @FXML
    private TableColumn<AppointmentWithPatient, String> statusColumn;

    @FXML
    private TableColumn<AppointmentWithPatient, String> specializationColumn;

    @FXML
    private TableColumn<AppointmentWithPatient, Double> feeColumn;

    @FXML
    private TableColumn<AppointmentWithPatient, Void> actionsColumn;

    private Doctor currentDoctor;
    private DatabaseConnection dbConnection;
    private ObservableList<AppointmentWithPatient> appointments = FXCollections.observableArrayList();

    // Model class for appointment with patient name
    public static class AppointmentWithPatient {
        private int appointId;
        private int patientId;
        private String patientName;
        private String date;
        private String time;
        private String specialization;
        private String status;
        private double consultationFee;
        private String type;

        public AppointmentWithPatient(int appointId, int patientId, String patientName, String date, String time,
                                      String specialization, String status, double consultationFee, String type) {
            this.appointId = appointId;
            this.patientId = patientId;
            this.patientName = patientName;
            this.date = date;
            this.time = time;
            this.specialization = specialization;
            this.status = status;
            this.consultationFee = consultationFee;
            this.type = type;
        }

        public int getAppointId() {
            return appointId;
        }

        public int getPatientId() {
            return patientId;
        }

        public String getPatientName() {
            return patientName;
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

        public double getConsultationFee() {
            return consultationFee;
        }

        public String getType() {
            return type;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dbConnection = new DatabaseConnection();

        // Initialize statusFilterComboBox
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
                "All", "Scheduled", "Completed", "Cancelled", "No-Show"
        ));
        statusFilterComboBox.setValue("All");

        // Initialize columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("appointId"));
        patientNameColumn.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        specializationColumn.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        feeColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getConsultationFee()).asObject());

        // Setup actions column
        setupActionsColumn();

        // Initialize datePicker with null value to show all dates initially
        datePicker.setValue(null);
    }

    public void setDoctor(Doctor doctor) {
        this.currentDoctor = doctor;
        // Don't load appointments here, wait for loadData() to be called by the dashboard
    }

    /**
     * Public method to load appointment data - called from DoctorDashboardController
     */
    public void loadData() {
        if (currentDoctor != null) {
            loadAppointments();
        } else {
            System.out.println("Error: Cannot load appointments, doctor is null");
        }
    }

    @FXML
    void backBtnOnAction(ActionEvent event) {
        try {
            // Navigate back to doctor dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("DoctorDashboard.fxml"));
            Parent root = loader.load();

            // Pass doctor to the controller
            DoctorDashboardController controller = loader.getController();
            controller.setDoctor(currentDoctor);

            // Get current stage and set new scene
            Stage stage = (Stage) backBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to navigate back: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void refreshBtnOnAction(ActionEvent event) {
        // Clear all filters
        searchField.clear();
        datePicker.setValue(null);
        statusFilterComboBox.setValue("All");
        loadAppointments();
    }

    @FXML
    void searchBtnOnAction(ActionEvent event) {
        loadAppointments();
    }

    private void loadAppointments() {
        if (currentDoctor == null) {
            return;
        }

        appointments.clear();

        try {
            Connection conn = dbConnection.getConnection();
            StringBuilder queryBuilder = new StringBuilder(
                    "SELECT a.appoint_id, a.patient_id, p.name AS patient_name, " +
                            "a.date, a.time, a.specialization, a.status, a.consultation_fee, a.type " +
                            "FROM Appointment a " +
                            "JOIN Patient p ON a.patient_id = p.patient_id " +
                            "WHERE a.doctor_id = ? "
            );

            // Add filters if specified
            if (searchField.getText() != null && !searchField.getText().trim().isEmpty()) {
                queryBuilder.append("AND p.name LIKE ? ");
            }

            // Only add date filter if a date is selected
            if (datePicker.getValue() != null) {
                queryBuilder.append("AND a.date = ? ");
            }

            if (statusFilterComboBox.getValue() != null && !statusFilterComboBox.getValue().equals("All")) {
                queryBuilder.append("AND a.status = ? ");
            }

            queryBuilder.append("ORDER BY a.date, a.time");

            PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString());
            int paramIndex = 1;

            stmt.setInt(paramIndex++, currentDoctor.getDoctorId());

            if (searchField.getText() != null && !searchField.getText().trim().isEmpty()) {
                stmt.setString(paramIndex++, "%" + searchField.getText().trim() + "%");
            }

            // Only add date parameter if a date is selected
            if (datePicker.getValue() != null) {
                stmt.setString(paramIndex++, datePicker.getValue().toString());
            }

            if (statusFilterComboBox.getValue() != null && !statusFilterComboBox.getValue().equals("All")) {
                stmt.setString(paramIndex, statusFilterComboBox.getValue());
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                AppointmentWithPatient appointment = new AppointmentWithPatient(
                        rs.getInt("appoint_id"),
                        rs.getInt("patient_id"),
                        rs.getString("patient_name"),
                        rs.getString("date"),
                        rs.getString("time"),
                        rs.getString("specialization"),
                        rs.getString("status"),
                        rs.getDouble("consultation_fee"),
                        rs.getString("type")
                );

                appointments.add(appointment);
            }

            rs.close();
            stmt.close();

            // Update table
            appointmentsTable.setItems(appointments);

            // Update count label
            appointmentCountLabel.setText("(" + appointments.size() + ")");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load appointments: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupActionsColumn() {
        Callback<TableColumn<AppointmentWithPatient, Void>, TableCell<AppointmentWithPatient, Void>> cellFactory =
                new Callback<>() {
                    @Override
                    public TableCell<AppointmentWithPatient, Void> call(final TableColumn<AppointmentWithPatient, Void> param) {
                        return new TableCell<>() {
                            private final Button viewBtn = new Button("View");
                            private final Button completeBtn = new Button("Complete");
                            private final Button noShowBtn = new Button("No-Show");
                            private final HBox pane = new HBox(5);

                            {
                                // Style buttons
                                viewBtn.setStyle("-fx-background-color: #2d7ae5; -fx-text-fill: white;");
                                completeBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white;");
                                noShowBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");

                                // Add actions
                                viewBtn.setOnAction(event -> {
                                    AppointmentWithPatient appointment = getTableView().getItems().get(getIndex());
                                    viewPatientDetails(appointment);
                                });

                                completeBtn.setOnAction(event -> {
                                    AppointmentWithPatient appointment = getTableView().getItems().get(getIndex());
                                    completeAppointment(appointment);
                                });

                                noShowBtn.setOnAction(event -> {
                                    AppointmentWithPatient appointment = getTableView().getItems().get(getIndex());
                                    markAsNoShow(appointment);
                                });

                                pane.getChildren().add(viewBtn);
                            }

                            @Override
                            public void updateItem(Void item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                } else {
                                    // Get the appointment for this row
                                    AppointmentWithPatient appointment = getTableView().getItems().get(getIndex());

                                    // Reset pane
                                    pane.getChildren().clear();
                                    pane.getChildren().add(viewBtn);

                                    // Add other buttons based on status
                                    if ("Scheduled".equals(appointment.getStatus())) {
                                        pane.getChildren().addAll(completeBtn, noShowBtn);
                                    }

                                    setGraphic(pane);
                                }
                            }
                        };
                    }
                };

        actionsColumn.setCellFactory(cellFactory);
    }

    private void viewPatientDetails(AppointmentWithPatient appointment) {
        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT * FROM Patient WHERE patient_id = ?";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, appointment.getPatientId());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                StringBuilder details = new StringBuilder();
                details.append("Patient ID: ").append(rs.getInt("patient_id")).append("\n");
                details.append("Name: ").append(rs.getString("name")).append("\n");
                details.append("Date of Birth: ").append(rs.getString("dob")).append("\n");
                details.append("Gender: ").append(rs.getString("gender")).append("\n");
                details.append("Blood Group: ").append(rs.getString("blood_group")).append("\n");
                details.append("Phone: ").append(rs.getString("phone_no")).append("\n");
                details.append("Email: ").append(rs.getString("email")).append("\n");
                details.append("Address: ").append(rs.getString("address")).append("\n\n");

                details.append("Appointment Details:\n");
                details.append("Date: ").append(appointment.getDate()).append("\n");
                details.append("Time: ").append(appointment.getTime()).append("\n");
                details.append("Type: ").append(appointment.getType()).append("\n");
                details.append("Status: ").append(appointment.getStatus()).append("\n");
                details.append("Fee: $").append(appointment.getConsultationFee());

                showAlert("Patient Details", details.toString(), Alert.AlertType.INFORMATION);
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load patient details: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void completeAppointment(AppointmentWithPatient appointment) {
        if (currentDoctor == null) {
            showAlert("Error", "Doctor information not available.", Alert.AlertType.ERROR);
            return;
        }

        if (!"Scheduled".equals(appointment.getStatus())) {
            showAlert("Error", "Only scheduled appointments can be marked as completed.", Alert.AlertType.ERROR);
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Complete Appointment");
        confirmAlert.setHeaderText("Mark as Completed");
        confirmAlert.setContentText("Are you sure you want to mark this appointment as completed?");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            Connection conn = null;
            try {
                conn = dbConnection.getConnection();
                conn.setAutoCommit(false); // Start transaction

                // Step 1: Mark appointment as completed
                String updateQuery = "UPDATE Appointment SET status = 'Completed' WHERE appoint_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setInt(1, appointment.getAppointId());
                int rowsAffected = updateStmt.executeUpdate();
                updateStmt.close();

                if (rowsAffected > 0) {
                    // Step 2: Insert bill
                    String billQuery = "INSERT INTO Bill (patient_id, date, time, amount, payment_status, admin_id, doctor_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement billStmt = conn.prepareStatement(billQuery);

                    billStmt.setInt(1, appointment.getPatientId());
                    billStmt.setDate(2, java.sql.Date.valueOf(java.time.LocalDate.now()));
                    billStmt.setTime(3, java.sql.Time.valueOf(java.time.LocalTime.now()));
                    billStmt.setDouble(4, appointment.getConsultationFee());
                    billStmt.setString(5, "Pending"); // Payment status
                    billStmt.setNull(6, java.sql.Types.INTEGER); // admin_id is null
                    billStmt.setInt(7, currentDoctor.getDoctorId()); // Use current doctor's ID

                    System.out.println("Inserting bill for appointment ID: " + appointment.getAppointId());
                    System.out.println("Patient ID: " + appointment.getPatientId());
                    System.out.println("Doctor ID: " + currentDoctor.getDoctorId());
                    System.out.println("Amount: $" + appointment.getConsultationFee());

                    int billRows = billStmt.executeUpdate();
                    billStmt.close();

                    if (billRows > 0) {
                        conn.commit(); // Commit transaction

                        // Update UI
                        appointment.setStatus("Completed");
                        appointmentsTable.refresh();

                        showAlert("Success",
                                "Appointment completed successfully!\n" +
                                        "Bill generated for $" + String.format("%.2f", appointment.getConsultationFee()) +
                                        "\nPayment Status: Pending",
                                Alert.AlertType.INFORMATION);

                        // Open prescription form
                        openAddPrescriptionForm(appointment);
                    } else {
                        conn.rollback();
                        showAlert("Error", "Failed to generate bill.", Alert.AlertType.ERROR);
                    }
                } else {
                    conn.rollback();
                    showAlert("Error", "Failed to update appointment status.", Alert.AlertType.ERROR);
                }

            } catch (SQLException e) {
                // Rollback transaction on error
                if (conn != null) {
                    try {
                        conn.rollback();
                        System.out.println("Transaction rolled back due to error");
                    } catch (SQLException rollbackEx) {
                        rollbackEx.printStackTrace();
                    }
                }
                e.printStackTrace();
                showAlert("Error", "Database error while completing appointment: " + e.getMessage(), Alert.AlertType.ERROR);
            } finally {
                // Reset auto-commit
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void markAsNoShow(AppointmentWithPatient appointment) {
        if (!"Scheduled".equals(appointment.getStatus())) {
            showAlert("Error", "Only scheduled appointments can be marked as no-show.", Alert.AlertType.ERROR);
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("No-Show");
        confirmAlert.setHeaderText("Mark as No-Show");
        confirmAlert.setContentText("Are you sure the patient did not show up for this appointment?");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                Connection conn = dbConnection.getConnection();
                String query = "UPDATE Appointment SET status = 'No-Show' WHERE appoint_id = ?";

                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, appointment.getAppointId());

                int rowsAffected = stmt.executeUpdate();
                stmt.close();

                if (rowsAffected > 0) {
                    appointment.setStatus("No-Show");
                    appointmentsTable.refresh();
                    showAlert("Success", "Appointment marked as No-Show.", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error", "Failed to update appointment status.", Alert.AlertType.ERROR);
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Database error: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void openAddPrescriptionForm(AppointmentWithPatient appointment) {
        try {
            // Update the path to the FXML file if necessary
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/db_project1/AddPrescription.fxml"));
            Parent root = loader.load();

            // Pass data to controller if required
            // Uncomment and adjust if you have AddPrescriptionController implemented
            /*
            AddPrescriptionController controller = loader.getController();
            controller.setAppointmentData(appointment.getAppointId(),
                                          appointment.getPatientId(),
                                          currentDoctor.getDoctorId());
            */

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add Prescription");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open prescription form. Please check if AddPrescription.fxml exists: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Unexpected error opening prescription form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}