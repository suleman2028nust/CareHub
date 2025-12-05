package com.example.db_project1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    @FXML
    private Label dateLabel;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchBtn;

    @FXML
    private Label doctorsCountLabel;

    @FXML
    private Label appointmentsCountLabel;

    @FXML
    private Label todayAppointmentsLabel;

    @FXML
    private Label prescriptionsCountLabel;

    @FXML
    private TableView<Appointment> upcomingAppointmentsTable;

    @FXML
    private TableColumn<Appointment, Integer> appointIdColumn;

    @FXML
    private TableColumn<Appointment, String> doctorNameColumn;

    @FXML
    private TableColumn<Appointment, String> specialtyColumn;

    @FXML
    private TableColumn<Appointment, String> dateColumn;

    @FXML
    private TableColumn<Appointment, String> timeColumn;

    @FXML
    private TableColumn<Appointment, String> statusColumn;

    private Patient currentPatient;
    private DatabaseConnection dbConnection;
    private ObservableList<Appointment> appointmentsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("HomeController initialized");

        // Initialize database connection
        dbConnection = new DatabaseConnection();

        // Set today's date
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        dateLabel.setText(today.format(formatter));

        // Configure table columns
        appointIdColumn.setCellValueFactory(new PropertyValueFactory<>("appointId"));
        doctorNameColumn.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        specialtyColumn.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Add action buttons to the table rows
        addButtonToTable();
    }

    public void setPatient(Patient patient) {
        System.out.println("Setting patient: " + (patient != null ? patient.getName() : "null"));
        this.currentPatient = patient;
        // Load data immediately after setting the patient
        loadData();
    }

    public void loadData() {
        if (currentPatient == null) {
            System.out.println("Cannot load data: patient is null");
            return;
        }

        System.out.println("Loading data for patient ID: " + currentPatient.getPatientId());

        // Load counts
        loadDoctorsCount();
        loadAppointmentsCount();
        loadTodayAppointmentsCount();
        loadPrescriptionsCount();

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

    private void loadAppointmentsCount() {
        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT COUNT(*) FROM Appointment WHERE patient_id = ?";
            System.out.println("Executing query: " + query + " with patient ID: " + currentPatient.getPatientId());

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, currentPatient.getPatientId());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                appointmentsCountLabel.setText(String.valueOf(count));
                System.out.println("Appointments count: " + count);
            } else {
                System.out.println("No result returned from appointments count query");
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error loading appointments count: " + e.getMessage());
            showAlert("Error", "Database Error", "Failed to load appointments count: " + e.getMessage());
        }
    }

    private void loadTodayAppointmentsCount() {
        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT COUNT(*) FROM Appointment WHERE patient_id = ? AND date = CURDATE()";
            System.out.println("Executing query: " + query + " with patient ID: " + currentPatient.getPatientId());

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, currentPatient.getPatientId());
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

    private void loadPrescriptionsCount() {
        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT COUNT(*) FROM Prescription WHERE patient_id = ?";
            System.out.println("Executing query: " + query + " with patient ID: " + currentPatient.getPatientId());

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, currentPatient.getPatientId());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                prescriptionsCountLabel.setText(String.valueOf(count));
                System.out.println("Prescriptions count: " + count);
            } else {
                System.out.println("No result returned from prescriptions count query");
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error loading prescriptions count: " + e.getMessage());
            showAlert("Error", "Database Error", "Failed to load prescriptions count: " + e.getMessage());
        }
    }

    private void loadUpcomingAppointments() {
        try {
            appointmentsList.clear();
            Connection conn = dbConnection.getConnection();

            String query = "SELECT a.appoint_id, a.date, a.time, a.specialization, a.status, " +
                    "CONCAT(d.first_name, ' ', IFNULL(d.mid_name, ''), ' ', d.last_name) AS doctor_name " +
                    "FROM Appointment a " +
                    "JOIN Doctor d ON a.doctor_id = d.doctor_id " +
                    "WHERE a.patient_id = ? AND a.date >= CURDATE() " +
                    "AND a.status = 'Scheduled' " +
                    "ORDER BY a.date, a.time LIMIT 5";

            System.out.println("Executing query: " + query + " with patient ID: " + currentPatient.getPatientId());

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, currentPatient.getPatientId());
            ResultSet resultSet = statement.executeQuery();

            int rowCount = 0;
            while (resultSet.next()) {
                rowCount++;
                int appointId = resultSet.getInt("appoint_id");
                String doctorName = resultSet.getString("doctor_name");
                String specialization = resultSet.getString("specialization");
                Date date = resultSet.getDate("date");
                Time time = resultSet.getTime("time");
                String status = resultSet.getString("status");

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

                Appointment appointment = new Appointment(
                        appointId,
                        currentPatient.getPatientId(),
                        doctorName.trim(),
                        date.toLocalDate().format(dateFormatter),
                        time.toLocalTime().format(timeFormatter),
                        specialization,
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
        // Add a column with a view details button for each row
        TableColumn<Appointment, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(120);

        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button detailsButton = new Button("Details");

            {
                detailsButton.setStyle("-fx-background-color: #2d7ae5; -fx-text-fill: white;");
                detailsButton.setOnAction(event -> {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    showAppointmentDetails(appointment);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(detailsButton);
                }
            }
        });

        upcomingAppointmentsTable.getColumns().add(actionCol);
    }

    private void showAppointmentDetails(Appointment appointment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("appointment-details-view.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Appointment Details");
            dialog.setDialogPane((DialogPane) loader.load());

            // The commented out code would be implemented when the AppointmentDetailsController is available
            /*
            AppointmentDetailsController controller = loader.getController();
            controller.setAppointment(appointment);
            */

            dialog.showAndWait();

            // Reload data if appointment was modified
            loadData();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Application Error", "Failed to load appointment details dialog: " + e.getMessage());
        }
    }

    @FXML
    private void searchBtnOnAction(ActionEvent event) {
        if (searchField.getText().trim().isEmpty()) {
            showAlert("Warning", "Search Empty", "Please enter doctor name or specialization to search.");
            return;
        }

        try {
            PatientDashboardController dashboardController =
                    (PatientDashboardController) searchField.getScene().getWindow().getUserData();

            if (dashboardController == null) {
                System.out.println("Dashboard controller is null!");
                showAlert("Error", "Navigation Error", "Failed to access parent controller.");
                return;
            }

            // Navigate to doctors view
            dashboardController.allDoctorsBtnOnAction(event);

            // Give a short delay for the view to load
            Thread.sleep(100);

            // Find the AllDoctorsController - this needs to be done using the dashboard controller
            // since we can't directly access the loader here
            System.out.println("Attempting to set search term in AllDoctorsController");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Navigation Error", "Failed to navigate to doctors page: " + e.getMessage());
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