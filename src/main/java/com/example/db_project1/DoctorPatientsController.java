package com.example.db_project1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DoctorPatientsController implements Initializable {

    @FXML
    private Button backBtn;

    @FXML
    private Label patientCountLabel;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchBtn;

    @FXML
    private ComboBox<String> filterComboBox;

    @FXML
    private Button refreshBtn;

    @FXML
    private TableView<Patient> patientsTable;

    @FXML
    private TableColumn<Patient, Integer> idColumn;

    @FXML
    private TableColumn<Patient, String> nameColumn;

    @FXML
    private TableColumn<Patient, LocalDate> dobColumn;

    @FXML
    private TableColumn<Patient, String> genderColumn;

    @FXML
    private TableColumn<Patient, String> bloodGroupColumn;

    @FXML
    private TableColumn<Patient, String> phoneColumn;

    @FXML
    private TableColumn<Patient, String> emailColumn;

    @FXML
    private TableColumn<Patient, Void> actionsColumn;

    private Doctor currentDoctor;
    private DatabaseConnection dbConnection;
    private ObservableList<Patient> allPatients = FXCollections.observableArrayList();
    private FilteredList<Patient> filteredPatients;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dbConnection = new DatabaseConnection();

        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        dobColumn.setCellValueFactory(new PropertyValueFactory<>("dob"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        bloodGroupColumn.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNo"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Format date column
        dobColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
                }
            }
        });

        // Set up action buttons column
        setupActionsColumn();

        // Initialize filter combo box
        filterComboBox.setItems(FXCollections.observableArrayList(
                "All Patients",
                "Recent Patients",
                "Male Patients",
                "Female Patients"
        ));
        filterComboBox.setValue("All Patients");
        filterComboBox.setOnAction(event -> applyFilter());

        // Set up search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilter();
        });

        // Initialize filtered list
        filteredPatients = new FilteredList<>(allPatients, p -> true);
        patientsTable.setItems(filteredPatients);
    }

    public void setDoctor(Doctor doctor) {
        this.currentDoctor = doctor;
        System.out.println("Doctor set in patients view: " + (doctor != null ? doctor.getName() : "null"));

        if (doctor != null) {
            loadData();
        }
    }

    public void loadData() {
        if (currentDoctor == null) return;

        allPatients.clear();

        try {
            // SQL query to get patients for the current doctor based on appointment history
            String query = "SELECT DISTINCT p.* FROM Patient p " +
                    "JOIN Appointment a ON p.patient_id = a.patient_id " +
                    "WHERE a.doctor_id = ?";

            ResultSet resultSet = dbConnection.executePreparedQuery(query, currentDoctor.getDoctorId());

            if (resultSet != null) {
                while (resultSet.next()) {
                    Patient patient = new Patient();
                    patient.setPatientId(resultSet.getInt("patient_id"));
                    patient.setFirstName(resultSet.getString("first_name"));
                    patient.setMiddleName(resultSet.getString("mid_name"));
                    patient.setLastName(resultSet.getString("last_name"));
                    patient.setName(resultSet.getString("name"));

                    // Handle date conversion
                    java.sql.Date sqlDate = resultSet.getDate("dob");
                    if (sqlDate != null) {
                        patient.setDob(sqlDate.toLocalDate());
                    }

                    patient.setGender(resultSet.getString("gender"));
                    patient.setBloodGroup(resultSet.getString("blood_group"));
                    patient.setInsuranceType(resultSet.getString("insurance_type"));
                    patient.setEmail(resultSet.getString("email"));
                    patient.setAddress(resultSet.getString("address"));
                    patient.setPhoneNo(resultSet.getString("phone_no"));

                    allPatients.add(patient);
                }

                resultSet.close();
            }

            // Update patient count label
            patientCountLabel.setText("(" + allPatients.size() + ")");

            // Apply initial filter
            applyFilter();

        } catch (SQLException e) {
            System.err.println("Error loading patients data: " + e.getMessage());
            e.printStackTrace();

            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Failed to Load Patients");
            alert.setContentText("An error occurred while loading patient data: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void setupActionsColumn() {
        Callback<TableColumn<Patient, Void>, TableCell<Patient, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Patient, Void> call(final TableColumn<Patient, Void> param) {
                return new TableCell<>() {
                    private final Button viewButton = new Button("View");
                    private final Button historyButton = new Button("History");
                    private final HBox pane = new HBox(5, viewButton, historyButton);

                    {
                        // Configure buttons
                        viewButton.setStyle("-fx-background-color: #2d7ae5; -fx-text-fill: white;");
                        historyButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");

                        // Add action handlers
                        viewButton.setOnAction(event -> {
                            Patient patient = getTableView().getItems().get(getIndex());
                            viewPatientDetails(patient);
                        });

                        historyButton.setOnAction(event -> {
                            Patient patient = getTableView().getItems().get(getIndex());
                            viewPatientHistory(patient);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
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

    private void viewPatientDetails(Patient patient) {
        // For now, just show an alert with basic patient info
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Patient Details");
        alert.setHeaderText("Patient: " + patient.getName());

        StringBuilder content = new StringBuilder();
        content.append("ID: ").append(patient.getPatientId()).append("\n");
        content.append("DOB: ").append(patient.getDob()).append("\n");
        content.append("Gender: ").append(patient.getGender()).append("\n");
        content.append("Blood Group: ").append(patient.getBloodGroup()).append("\n");
        content.append("Insurance: ").append(patient.getInsuranceType()).append("\n");
        content.append("Phone: ").append(patient.getPhoneNo()).append("\n");
        content.append("Email: ").append(patient.getEmail()).append("\n");
        content.append("Address: ").append(patient.getAddress()).append("\n");

        alert.setContentText(content.toString());
        alert.showAndWait();

        // In a complete implementation, you'd navigate to a detailed patient view
        // For example:
        // try {
        //     FXMLLoader loader = new FXMLLoader(getClass().getResource("PatientDetails.fxml"));
        //     Parent root = loader.load();
        //     PatientDetailsController controller = loader.getController();
        //     controller.setPatient(patient);
        //     controller.setDoctor(currentDoctor);
        //
        //     Stage stage = new Stage();
        //     stage.setTitle("Patient Details");
        //     stage.setScene(new Scene(root));
        //     stage.show();
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }
    }

    private void viewPatientHistory(Patient patient) {
        // Fetch patient medical history from database
        StringBuilder history = new StringBuilder();

        try {
            // Get appointment history
            String appointmentQuery = "SELECT a.*, p.remarks, p.diagnosis " +
                    "FROM Appointment a " +
                    "LEFT JOIN Prescription p ON a.patient_id = p.patient_id AND a.doctor_id = p.doctor_id AND DATE(a.date) = DATE(p.date_issued) " +
                    "WHERE a.patient_id = ? AND a.doctor_id = ? " +
                    "ORDER BY a.date DESC, a.time DESC";

            ResultSet appointmentResult = dbConnection.executePreparedQuery(
                    appointmentQuery,
                    patient.getPatientId(),
                    currentDoctor.getDoctorId()
            );

            history.append("APPOINTMENT & PRESCRIPTION HISTORY\n");
            history.append("===============================\n\n");

            boolean hasRecords = false;

            if (appointmentResult != null) {
                while (appointmentResult.next()) {
                    hasRecords = true;

                    // Format the appointment date and time
                    String date = appointmentResult.getDate("date").toString();
                    String time = appointmentResult.getTime("time").toString();
                    String status = appointmentResult.getString("status");
                    String type = appointmentResult.getString("type");

                    history.append("Date: ").append(date).append(" | Time: ").append(time).append("\n");
                    history.append("Type: ").append(type).append(" | Status: ").append(status).append("\n");

                    // Add diagnosis and remarks if available
                    String diagnosis = appointmentResult.getString("diagnosis");
                    String remarks = appointmentResult.getString("remarks");

                    if (diagnosis != null && !diagnosis.isEmpty()) {
                        history.append("Diagnosis: ").append(diagnosis).append("\n");
                    }

                    if (remarks != null && !remarks.isEmpty()) {
                        history.append("Remarks: ").append(remarks).append("\n");
                    }

                    history.append("------------------------------\n");
                }
                appointmentResult.close();
            }

            if (!hasRecords) {
                history.append("No appointment records found for this patient.");
            }

        } catch (SQLException e) {
            history.append("Error retrieving patient history: ").append(e.getMessage());
            e.printStackTrace();
        }

        // Display the history
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Patient Medical History");
        alert.setHeaderText("Medical History for " + patient.getName());

        // Create a scrollable text area for the content
        TextArea textArea = new TextArea(history.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefHeight(400);
        textArea.setPrefWidth(500);

        alert.getDialogPane().setContent(textArea);
        alert.setResizable(true);
        alert.showAndWait();
    }

    private void applyFilter() {
        String searchText = searchField.getText().toLowerCase();
        String filterOption = filterComboBox.getValue();

        filteredPatients.setPredicate(patient -> {
            // If filter text is empty, display all patients according to the combo box filter
            boolean matchesSearch = searchText == null || searchText.isEmpty() ||
                    patient.getName().toLowerCase().contains(searchText) ||
                    (patient.getEmail() != null && patient.getEmail().toLowerCase().contains(searchText));

            // Apply combo box filter
            boolean matchesComboFilter = true;

            if (filterOption != null) {
                switch (filterOption) {
                    case "Male Patients":
                        matchesComboFilter = "Male".equals(patient.getGender());
                        break;
                    case "Female Patients":
                        matchesComboFilter = "Female".equals(patient.getGender());
                        break;
                    case "Recent Patients":
                        // This would require additional data about when they were last seen
                        // For now, we'll skip this filter or implement it if we have timestamps
                        matchesComboFilter = true;
                        break;
                    default: // "All Patients"
                        matchesComboFilter = true;
                        break;
                }
            }

            return matchesSearch && matchesComboFilter;
        });

        // Update patient count label to show filtered count
        patientCountLabel.setText("(" + filteredPatients.size() + ")");
    }

    @FXML
    void backBtnOnAction(ActionEvent event) {
        // The back button would typically go back to the doctor dashboard
        // Since we're inside a BorderPane content area, we don't need to navigate
        // The parent controller will handle this
    }

    @FXML
    void searchBtnOnAction(ActionEvent event) {
        applyFilter();
    }

    @FXML
    void refreshBtnOnAction(ActionEvent event) {
        searchField.clear();
        filterComboBox.setValue("All Patients");
        loadData();
    }
}