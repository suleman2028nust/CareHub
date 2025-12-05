package com.example.db_project1;

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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MyAppointmentsController implements Initializable {

    @FXML
    private Label dateLabel;

    @FXML
    private ComboBox<String> statusFilterCombo;

    @FXML
    private DatePicker dateFilterPicker;

    @FXML
    private Button applyFilterBtn;

    @FXML
    private Button clearFilterBtn;

    @FXML
    private Button newAppointmentBtn;

    @FXML
    private TabPane appointmentsTabPane;

    // All Appointments Table
    @FXML
    private TableView<Appointment> allAppointmentsTable;

    @FXML
    private TableColumn<Appointment, Integer> allApptIdCol;

    @FXML
    private TableColumn<Appointment, String> allDoctorCol;

    @FXML
    private TableColumn<Appointment, String> allSpecializationCol;

    @FXML
    private TableColumn<Appointment, String> allDateCol;

    @FXML
    private TableColumn<Appointment, String> allTimeCol;

    @FXML
    private TableColumn<Appointment, String> allStatusCol;

    @FXML
    private TableColumn<Appointment, String> allTypeCol;

    @FXML
    private TableColumn<Appointment, Void> allActionsCol;

    // Upcoming Appointments Table
    @FXML
    private TableView<Appointment> upcomingAppointmentsTable;

    @FXML
    private TableColumn<Appointment, Integer> upApptIdCol;

    @FXML
    private TableColumn<Appointment, String> upDoctorCol;

    @FXML
    private TableColumn<Appointment, String> upSpecializationCol;

    @FXML
    private TableColumn<Appointment, String> upDateCol;

    @FXML
    private TableColumn<Appointment, String> upTimeCol;

    @FXML
    private TableColumn<Appointment, String> upTypeCol;

    @FXML
    private TableColumn<Appointment, Void> upActionsCol;

    // Completed Appointments Table
    @FXML
    private TableView<Appointment> completedAppointmentsTable;

    @FXML
    private TableColumn<Appointment, Integer> compApptIdCol;

    @FXML
    private TableColumn<Appointment, String> compDoctorCol;

    @FXML
    private TableColumn<Appointment, String> compSpecializationCol;

    @FXML
    private TableColumn<Appointment, String> compDateCol;

    @FXML
    private TableColumn<Appointment, String> compTimeCol;

    @FXML
    private TableColumn<Appointment, String> compTypeCol;

    @FXML
    private TableColumn<Appointment, Void> compActionsCol;

    // Cancelled Appointments Table
    @FXML
    private TableView<Appointment> cancelledAppointmentsTable;

    @FXML
    private TableColumn<Appointment, Integer> cancelApptIdCol;

    @FXML
    private TableColumn<Appointment, String> cancelDoctorCol;

    @FXML
    private TableColumn<Appointment, String> cancelSpecializationCol;

    @FXML
    private TableColumn<Appointment, String> cancelDateCol;

    @FXML
    private TableColumn<Appointment, String> cancelTimeCol;

    @FXML
    private TableColumn<Appointment, Void> cancelActionsCol;

    private Patient currentPatient;
    private DatabaseConnection dbConnection;
    private ObservableList<Appointment> allAppointments = FXCollections.observableArrayList();
    private ObservableList<Appointment> upcomingAppointments = FXCollections.observableArrayList();
    private ObservableList<Appointment> completedAppointments = FXCollections.observableArrayList();
    private ObservableList<Appointment> cancelledAppointments = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dbConnection = new DatabaseConnection();

        // Set today's date
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        // Initialize tables
        initializeAllAppointmentsTable();
        initializeUpcomingAppointmentsTable();
        initializeCompletedAppointmentsTable();
        initializeCancelledAppointmentsTable();

        // Set default filter value
        statusFilterCombo.setValue("All");
    }

    public void setPatient(Patient patient) {
        this.currentPatient = patient;
        loadData();
    }

    public void loadData() {
        if (currentPatient == null) {
            return;
        }

        loadAppointments();
    }

    private void loadAppointments() {
        String status = statusFilterCombo.getValue();
        LocalDate filterDate = dateFilterPicker.getValue();

        // Clear existing lists
        allAppointments.clear();
        upcomingAppointments.clear();
        completedAppointments.clear();
        cancelledAppointments.clear();

        try {
            Connection conn = dbConnection.getConnection();
            StringBuilder queryBuilder = new StringBuilder(
                    "SELECT a.appoint_id, a.patient_id, a.doctor_id, " +
                            "CONCAT(d.first_name, ' ', d.mid_name, ' ', d.last_name) AS doctor_name, " +
                            "a.date, a.time, a.specialization, a.status, a.consultation_fee, a.type " +
                            "FROM Appointment a " +
                            "JOIN Doctor d ON a.doctor_id = d.doctor_id " +
                            "WHERE a.patient_id = ? "
            );

            // Add filters if specified
            if (!status.equals("All")) {
                queryBuilder.append("AND a.status = ? ");
            }

            if (filterDate != null) {
                queryBuilder.append("AND a.date = ? ");
            }

            queryBuilder.append("ORDER BY a.date DESC, a.time DESC");

            PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString());
            stmt.setInt(1, currentPatient.getPatientId());

            int paramIndex = 2;
            if (!status.equals("All")) {
                stmt.setString(paramIndex++, status);
            }

            if (filterDate != null) {
                stmt.setString(paramIndex, filterDate.toString());
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Appointment appointment = new Appointment(
                        rs.getInt("appoint_id"),
                        rs.getInt("patient_id"),
                        rs.getInt("doctor_id"),
                        rs.getString("doctor_name"),
                        rs.getString("date"),
                        rs.getString("time"),
                        rs.getString("specialization"),
                        rs.getString("status"),
                        rs.getDouble("consultation_fee"),
                        rs.getString("type")
                );

                // Add to all appointments list
                allAppointments.add(appointment);

                // Add to specific list based on status
                String appointmentStatus = rs.getString("status");
                if (appointmentStatus.equals("Scheduled")) {
                    upcomingAppointments.add(appointment);
                } else if (appointmentStatus.equals("Completed")) {
                    completedAppointments.add(appointment);
                } else if (appointmentStatus.equals("Cancelled") || appointmentStatus.equals("No-Show")) {
                    cancelledAppointments.add(appointment);
                }
            }

            rs.close();
            stmt.close();

            // Update table views
            allAppointmentsTable.setItems(allAppointments);
            upcomingAppointmentsTable.setItems(upcomingAppointments);
            completedAppointmentsTable.setItems(completedAppointments);
            cancelledAppointmentsTable.setItems(cancelledAppointments);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load appointments: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void initializeAllAppointmentsTable() {
        allApptIdCol.setCellValueFactory(new PropertyValueFactory<>("appointId"));
        allDoctorCol.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        allSpecializationCol.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        allDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        allTimeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        allStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        allTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        setupActionsColumn(allActionsCol, false);
    }

    private void initializeUpcomingAppointmentsTable() {
        upApptIdCol.setCellValueFactory(new PropertyValueFactory<>("appointId"));
        upDoctorCol.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        upSpecializationCol.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        upDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        upTimeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        upTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        setupActionsColumn(upActionsCol, true);
    }

    private void initializeCompletedAppointmentsTable() {
        compApptIdCol.setCellValueFactory(new PropertyValueFactory<>("appointId"));
        compDoctorCol.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        compSpecializationCol.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        compDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        compTimeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        compTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        setupActionsColumn(compActionsCol, false);
    }

    private void initializeCancelledAppointmentsTable() {
        cancelApptIdCol.setCellValueFactory(new PropertyValueFactory<>("appointId"));
        cancelDoctorCol.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        cancelSpecializationCol.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        cancelDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        cancelTimeCol.setCellValueFactory(new PropertyValueFactory<>("time"));

        setupActionsColumn(cancelActionsCol, false);
    }

    private void setupActionsColumn(TableColumn<Appointment, Void> actionsColumn, boolean isUpcoming) {
        Callback<TableColumn<Appointment, Void>, TableCell<Appointment, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Appointment, Void> call(final TableColumn<Appointment, Void> param) {
                return new TableCell<>() {
                    private final Button viewBtn = new Button("View");
                    private final Button rescheduleBtn = new Button("Reschedule");
                    private final Button cancelBtn = new Button("Cancel");
                    private final HBox pane = new HBox(5);

                    {
                        viewBtn.setStyle("-fx-background-color: #2d7ae5; -fx-text-fill: white;");
                        rescheduleBtn.setStyle("-fx-background-color: #ffa726; -fx-text-fill: white;");
                        cancelBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white;");

                        viewBtn.setOnAction(event -> {
                            Appointment appointment = getTableView().getItems().get(getIndex());
                            viewAppointmentDetails(appointment);
                        });

                        rescheduleBtn.setOnAction(event -> {
                            Appointment appointment = getTableView().getItems().get(getIndex());
                            rescheduleAppointment(appointment);
                        });

                        cancelBtn.setOnAction(event -> {
                            Appointment appointment = getTableView().getItems().get(getIndex());
                            cancelAppointment(appointment);
                        });

                        pane.getChildren().add(viewBtn);
                        if (isUpcoming) {
                            pane.getChildren().addAll(rescheduleBtn, cancelBtn);
                        }
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(pane);
                        }
                    }
                };
            }
        };

        actionsColumn.setCellFactory(cellFactory);
    }

    @FXML
    void handleApplyFilter(ActionEvent event) {
        loadAppointments();
    }

    @FXML
    void handleClearFilter(ActionEvent event) {
        statusFilterCombo.setValue("All");
        dateFilterPicker.setValue(null);
        loadAppointments();
    }

    @FXML
    void handleNewAppointment(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("BookAppointment.fxml"));
            Parent root = loader.load();

            BookAppointmentController controller = loader.getController();
            controller.setPatient(currentPatient);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Book New Appointment");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Reload data after booking
            loadAppointments();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading booking form: " + e.getMessage());
            showAlert("Error", "Could not open booking form", Alert.AlertType.ERROR);
        }
    }

    private void viewAppointmentDetails(Appointment appointment) {
        showAlert("Appointment Details",
                "ID: " + appointment.getAppointId() + "\n" +
                        "Doctor: " + appointment.getDoctorName() + "\n" +
                        "Specialization: " + appointment.getSpecialization() + "\n" +
                        "Date: " + appointment.getDate() + "\n" +
                        "Time: " + appointment.getTime() + "\n" +
                        "Status: " + appointment.getStatus() + "\n" +
                        "Type: " + appointment.getType() + "\n" +
                        "Consultation Fee: $" + appointment.getConsultationFee(),
                Alert.AlertType.INFORMATION
        );
    }

    private void rescheduleAppointment(Appointment appointment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RescheduleAppointment.fxml"));
            Parent root = loader.load();

           //RescheduleAppointmentController controller = loader.getController();
            //controller.setAppointment(appointment);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Reschedule Appointment");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Reload data after rescheduling
            loadAppointments();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open reschedule form", Alert.AlertType.ERROR);
        }
    }

    private void cancelAppointment(Appointment appointment) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Cancel Appointment");
        confirmAlert.setHeaderText("Are you sure you want to cancel this appointment?");
        confirmAlert.setContentText("Appointment with " + appointment.getDoctorName() + " on " +
                appointment.getDate() + " at " + appointment.getTime());

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            try {
                Connection conn = dbConnection.getConnection();
                String query = "UPDATE Appointment SET status = 'Cancelled' WHERE appoint_id = ?";

                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, appointment.getAppointId());

                int rowsAffected = stmt.executeUpdate();
                stmt.close();

                if (rowsAffected > 0) {
                    showAlert("Success", "Appointment cancelled successfully.", Alert.AlertType.INFORMATION);
                    loadAppointments();
                } else {
                    showAlert("Error", "Failed to cancel appointment.", Alert.AlertType.ERROR);
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Database error: " + e.getMessage(), Alert.AlertType.ERROR);
            }
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