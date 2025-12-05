package com.example.db_project1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class AdminHomeController implements Initializable {

    @FXML
    private Label dateLabel;

    @FXML
    private Label doctorsCount;

    @FXML
    private Label patientsCount;

    @FXML
    private Label newBookingsCount;

    @FXML
    private Label todayApptsCount;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private TableView<Appointment> appointmentsTable;

    @FXML
    private TableColumn<Appointment, Integer> appointmentIdColumn;

    @FXML
    private TableColumn<Appointment, String> patientNameColumn;

    @FXML
    private TableColumn<Appointment, String> doctorNameColumn;

    @FXML
    private TableColumn<Appointment, String> dateColumn;

    @FXML
    private TableColumn<Appointment, String> timeColumn;

    @FXML
    private TableColumn<Appointment, String> statusColumn;

    @FXML
    private Button showAllAppointmentsButton;

    private Admin currentAdmin;
    private DatabaseConnection dbConnection;
    private ObservableList<Appointment> appointmentsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dbConnection = new DatabaseConnection();

        todayApptsCount.setWrapText(true);

        // Set today's date
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        dateLabel.setText(today.format(formatter));

        // Setup table columns with properties from Appointment model
        appointmentIdColumn.setCellValueFactory(new PropertyValueFactory<>("appointId"));
        patientNameColumn.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        doctorNameColumn.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        dateColumn.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createObjectBinding(() -> {
                    // Format date from String yyyy-MM-dd to dd-MMM-yyyy
                    try {
                        LocalDate ld = LocalDate.parse(cellData.getValue().getDate());
                        return ld.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
                    } catch (Exception e) {
                        return cellData.getValue().getDate();
                    }
                })
        );
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Reload data when search is cleared
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                loadUpcomingAppointments();
            }
        });
    }

    public void setAdmin(Admin admin) {
        this.currentAdmin = admin;
    }

    public void loadData() {
        loadCounts();
        loadUpcomingAppointments();
    }

    private void loadCounts() {
        try {
            ResultSet rs = dbConnection.executeQuery("SELECT COUNT(*) FROM Doctor");
            if (rs.next()) doctorsCount.setText(String.valueOf(rs.getInt(1)));

            rs = dbConnection.executeQuery("SELECT COUNT(*) FROM Patient");
            if (rs.next()) patientsCount.setText(String.valueOf(rs.getInt(1)));

            String todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            rs = dbConnection.executeQuery("SELECT COUNT(*) FROM Appointment WHERE status = 'Scheduled' AND date = '" + todayStr + "'");
            if (rs.next()) newBookingsCount.setText(String.valueOf(rs.getInt(1)));

            rs = dbConnection.executeQuery("SELECT COUNT(*) FROM Appointment WHERE date = '" + todayStr + "'");
            if (rs.next()) todayApptsCount.setText(String.valueOf(rs.getInt(1)));

        } catch (SQLException e) {
            System.out.println("Error loading counts: " + e.getMessage());
            showAlert("Database Error", "Failed to load dashboard data: " + e.getMessage());
        }
    }

    // Load upcoming appointments using the view vw_upcoming_appointments
    private void loadUpcomingAppointments() {
        appointmentsList.clear();

        try {
            LocalDate today = LocalDate.now();
            LocalDate nextWeek = today.plusDays(7);

            String query = "SELECT appoint_id, patient_name, doctor_name, date, time, specialization, status " +
                    "FROM vw_upcoming_appointments " +
                    "WHERE date BETWEEN '" + today + "' AND '" + nextWeek + "' " +
                    "ORDER BY date ASC, time ASC";

            ResultSet rs = dbConnection.executeQuery(query);

            while (rs.next()) {
                Appointment appointment = new Appointment(
                        rs.getInt("appoint_id"),
                        rs.getString("patient_name"),
                        rs.getString("doctor_name"),
                        rs.getString("date"),
                        rs.getString("time"),
                        rs.getString("specialization"),
                        rs.getString("status")
                );
                appointmentsList.add(appointment);
            }

            appointmentsTable.setItems(appointmentsList);

        } catch (SQLException e) {
            System.out.println("Error loading appointments: " + e.getMessage());
            showAlert("Database Error", "Failed to load appointments data: " + e.getMessage());
        }
    }

    // Search button handler
    @FXML
    private void handleSearch(ActionEvent event) {
        String searchText = searchField.getText().trim();

        if (searchText.isEmpty()) {
            loadUpcomingAppointments();
            return;
        }

        appointmentsList.clear();

        try {
            String query = "SELECT appoint_id, patient_name, doctor_name, date, time, specialization, status " +
                    "FROM vw_upcoming_appointments " +
                    "WHERE doctor_name LIKE '%" + searchText + "%' OR doctor_email LIKE '%" + searchText + "%' " +
                    "ORDER BY date ASC, time ASC";

            ResultSet rs = dbConnection.executeQuery(query);

            while (rs.next()) {
                Appointment appointment = new Appointment(
                        rs.getInt("appoint_id"),
                        rs.getString("patient_name"),
                        rs.getString("doctor_name"),
                        rs.getString("date"),
                        rs.getString("time"),
                        rs.getString("specialization"),
                        rs.getString("status")
                );
                appointmentsList.add(appointment);
            }

            appointmentsTable.setItems(appointmentsList);

        } catch (SQLException e) {
            System.out.println("Error searching appointments: " + e.getMessage());
            showAlert("Search Error", "Failed to search appointments: " + e.getMessage());
        }
    }

    // Show all appointments button handler
    @FXML
    private void handleShowAllAppointments(ActionEvent event) {
        try {
            AdminDashboardController parentController = (AdminDashboardController)
                    appointmentsTable.getScene().getUserData();

            if (parentController != null) {
                parentController.handleAppointments(new ActionEvent());
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminAppointment.fxml"));
                StackPane contentArea = (StackPane) appointmentsTable.getScene().lookup("#contentArea");
                if (contentArea != null) {
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(loader.load());

                    AdminAppointmentController controller = loader.getController();
                    controller.setAdmin(currentAdmin);
                    controller.loadData();
                }
            }
        } catch (Exception e) {
            System.out.println("Error navigating to appointments: " + e.getMessage());
            showAlert("Navigation Error", "Failed to navigate to appointments: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void refreshData() {
        loadData();
    }

    // Appointment class with updated constructor and getters for TableView binding
    public static class Appointment {
        private final int appointId;
        private final String patientName;
        private final String doctorName;
        private final String date;
        private final String time;
        private final String specialization;
        private final String status;

        public Appointment(int appointId, String patientName, String doctorName, String date, String time, String specialization, String status) {
            this.appointId = appointId;
            this.patientName = patientName;
            this.doctorName = doctorName;
            this.date = date;
            this.time = time;
            this.specialization = specialization;
            this.status = status;
        }

        public int getAppointId() {
            return appointId;
        }

        public String getPatientName() {
            return patientName;
        }

        public String getDoctorName() {
            return doctorName;
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
    }
}
