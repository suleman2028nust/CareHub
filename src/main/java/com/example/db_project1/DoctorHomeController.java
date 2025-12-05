package com.example.db_project1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DoctorHomeController implements Initializable {

    @FXML
    private Label dateLabel;

    @FXML
    private Label welcomeMessage;

    @FXML
    private Button viewAppointmentsBtn;

    @FXML
    private Label doctorsCountLabel;

    @FXML
    private Label patientsCountLabel;

    @FXML
    private Label newBookingsLabel;

    @FXML
    private Label todayAppointmentsLabel;

    @FXML
    private TableView<DoctorAppointment> upcomingAppointmentsTable;

    @FXML
    private TableColumn<DoctorAppointment, Integer> appointIdColumn;

    @FXML
    private TableColumn<DoctorAppointment, String> patientNameColumn;

    @FXML
    private TableColumn<DoctorAppointment, String> dateColumn;

    @FXML
    private TableColumn<DoctorAppointment, String> timeColumn;

    @FXML
    private TableColumn<DoctorAppointment, String> reasonColumn;

    @FXML
    private TableColumn<DoctorAppointment, String> statusColumn;

    private Doctor currentDoctor;
    private DatabaseConnection dbConnection;
    private ObservableList<DoctorAppointment> appointmentsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("DoctorHomeController initialized");

        // Initialize database connection
        dbConnection = new DatabaseConnection();

        // Set today's date
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        dateLabel.setText(today.format(formatter));

        // Configure table columns
        appointIdColumn.setCellValueFactory(new PropertyValueFactory<>("appointId"));
        patientNameColumn.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        reasonColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Add action buttons to the table rows
        addButtonToTable();
    }

    public void setDoctor(Doctor doctor) {
        System.out.println("Setting doctor: " + (doctor != null ? doctor.getName() : "null"));
        this.currentDoctor = doctor;

        if (doctor != null) {
            // Set welcome message with doctor's name
            welcomeMessage.setText("Welcome Dr. " + doctor.getName() + "! Thanks for joining with us. We are always trying to provide you a complete service. You can view your daily schedule and manage patient appointments.");

            // Load data immediately after setting the doctor
            loadData();
        }
    }

    public void loadData() {
        if (currentDoctor == null) {
            System.out.println("Cannot load data: doctor is null");
            return;
        }

        System.out.println("Loading data for doctor ID: " + currentDoctor.getDoctorId());

        // Load counts
        loadDoctorsCount();
        loadPatientsCount();
        loadNewBookingsCount();
        loadTodayAppointmentsCount();

        // Load upcoming appointments
        loadUpcomingAppointments();
    }

    private void loadDoctorsCount() {
        try {
            Connection conn = dbConnection.getConnection();
            if (conn == null) {
                System.out.println("Database connection is null!");
                showAlert("Error", "Database Error", "Failed to connect to database.");
                return;
            }

            String query = "SELECT COUNT(*) FROM Doctor";
            System.out.println("Executing query: " + query);

            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                doctorsCountLabel.setText(String.valueOf(count));
                System.out.println("Total doctors count: " + count);
            } else {
                System.out.println("No result returned from doctors count query");
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error loading doctors count: " + e.getMessage());
            showAlert("Error", "Database Error", "Failed to load doctors count: " + e.getMessage());
        }
    }

    private void loadPatientsCount() {
        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT COUNT(*) FROM Patient";
            System.out.println("Executing query: " + query);

            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                patientsCountLabel.setText(String.valueOf(count));
                System.out.println("Total patients count: " + count);
            } else {
                System.out.println("No result returned from patients count query");
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error loading patients count: " + e.getMessage());
            showAlert("Error", "Database Error", "Failed to load patients count: " + e.getMessage());
        }
    }

    private void loadNewBookingsCount() {
        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT COUNT(*) FROM Appointment WHERE doctor_id = ? AND status = 'Scheduled' AND date >= CURDATE() AND date <= DATE_ADD(CURDATE(), INTERVAL 7 DAY)";
            System.out.println("Executing query: " + query + " with doctor ID: " + currentDoctor.getDoctorId());

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, currentDoctor.getDoctorId());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                newBookingsLabel.setText(String.valueOf(count));
                System.out.println("New bookings count: " + count);
            } else {
                System.out.println("No result returned from new bookings count query");
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error loading new bookings count: " + e.getMessage());
            showAlert("Error", "Database Error", "Failed to load new bookings count: " + e.getMessage());
        }
    }

    private void loadTodayAppointmentsCount() {
        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT COUNT(*) FROM Appointment WHERE doctor_id = ? AND date = CURDATE()";
            System.out.println("Executing query: " + query + " with doctor ID: " + currentDoctor.getDoctorId());

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, currentDoctor.getDoctorId());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                todayAppointmentsLabel.setText(String.valueOf(count));
                System.out.println("Today's appointments count: " + count);
            } else {
                System.out.println("No result returned from today's appointments count query");
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error loading today's appointments count: " + e.getMessage());
            showAlert("Error", "Database Error", "Failed to load today's appointments count: " + e.getMessage());
        }
    }

    private void loadUpcomingAppointments() {
        try {
            appointmentsList.clear();
            Connection conn = dbConnection.getConnection();

            String query = "SELECT a.appoint_id, a.patient_id, a.date, a.time, a.status, a.type, " +
                    "p.name AS patient_name " +
                    "FROM Appointment a " +
                    "JOIN Patient p ON a.patient_id = p.patient_id " +
                    "WHERE a.doctor_id = ? AND ((a.date = CURDATE() AND a.time >= CURTIME()) OR a.date > CURDATE()) " +
                    "AND a.status = 'Scheduled' " +
                    "ORDER BY a.date, a.time LIMIT 15";

            System.out.println("Executing query: " + query + " with doctor ID: " + currentDoctor.getDoctorId());

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, currentDoctor.getDoctorId());
            ResultSet resultSet = statement.executeQuery();

            int rowCount = 0;
            while (resultSet.next()) {
                rowCount++;
                int appointId = resultSet.getInt("appoint_id");
                int patientId = resultSet.getInt("patient_id");
                String patientName = resultSet.getString("patient_name");
                Date date = resultSet.getDate("date");
                Time time = resultSet.getTime("time");
                String status = resultSet.getString("status");
                String type = resultSet.getString("type");

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

                DoctorAppointment appointment = new DoctorAppointment(
                        appointId,
                        patientId,
                        patientName,
                        currentDoctor.getDoctorId(),
                        currentDoctor.getName(),
                        date.toLocalDate().format(dateFormatter),
                        time.toLocalTime().format(timeFormatter),
                        type,
                        status
                );


                appointmentsList.add(appointment);
            }

            System.out.println("Loaded " + rowCount + " upcoming appointments");
            upcomingAppointmentsTable.setItems(appointmentsList);

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error loading upcoming appointments: " + e.getMessage());
            showAlert("Error", "Database Error", "Failed to load upcoming appointments: " + e.getMessage());
        }
    }

    private void addButtonToTable() {
        // Add a column with action buttons for each row
        TableColumn<DoctorAppointment, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(180);

        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button detailsButton = new Button("Details");
            private final Button completeButton = new Button("Complete");
            private final HBox buttonBox = new HBox(5);

            {
                detailsButton.setStyle("-fx-background-color: #2d7ae5; -fx-text-fill: white;");
                completeButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");

                buttonBox.getChildren().addAll(detailsButton, completeButton);

                detailsButton.setOnAction(event -> {
                    DoctorAppointment appointment = getTableView().getItems().get(getIndex());
                    showAppointmentDetails(appointment);
                });

                completeButton.setOnAction(event -> {
                    DoctorAppointment appointment = getTableView().getItems().get(getIndex());
                    completeAppointment(appointment);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonBox);
                }
            }
        });

        upcomingAppointmentsTable.getColumns().add(actionCol);
    }


    private void completeAppointment(DoctorAppointment appointment) {
        try {
            Connection conn = dbConnection.getConnection();
            String query = "UPDATE Appointment SET status = 'Completed' WHERE appoint_id = ?";
            System.out.println("Executing query: " + query + " with appointment ID: " + appointment.getAppointId());

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, appointment.getAppointId());
            int result = statement.executeUpdate();

            if (result > 0) {
                System.out.println("Appointment marked as completed successfully");
                showAlert("Success", "Appointment Completed", "The appointment has been marked as completed.");

                // Reload data to refresh the view
                loadData();
            } else {
                System.out.println("Failed to mark appointment as completed");
                showAlert("Error", "Update Failed", "Failed to mark the appointment as completed.");
            }

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error completing appointment: " + e.getMessage());
            showAlert("Error", "Database Error", "Failed to complete appointment: " + e.getMessage());
        }
    }

    private void showAppointmentDetails(DoctorAppointment appointment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/db_project1/appointment-details-view.fxml"));

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Appointment Details");
            dialog.setDialogPane(loader.load());

            // Access the controller and pass appointment details
            AppointmentDetailsController controller = loader.getController();
            controller.setAppointment(appointment);

            // Show the dialog
            dialog.showAndWait();

            // Optionally reload data in case of status changes
            loadData();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Application Error", "Failed to load appointment details dialog: " + e.getMessage());
        }
    }

    @FXML
    private void viewAppointmentsBtnOnAction(ActionEvent event) {
        try {
            // Navigate to appointments view
            DoctorDashboardController dashboardController =
                    (DoctorDashboardController) dateLabel.getScene().getWindow().getUserData();

            if (dashboardController == null) {
                System.out.println("Dashboard controller is null!");
                showAlert("Error", "Navigation Error", "Failed to access parent controller.");
                return;
            }

            // Navigate to appointments view - this would be implemented in the DoctorDashboardController
             //dashboardController.appointmentsBtnOnAction(event);

            // For now, just show a message that we would navigate
            showAlert("Information", "Navigation", "Would navigate to All Appointments view here.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Navigation Error", "Failed to navigate to appointments page: " + e.getMessage());
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}