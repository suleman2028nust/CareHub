package com.example.db_project1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.net.URL;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class MyPrescriptionsController implements Initializable {
    @FXML
    private Label dateLabel;

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private ComboBox<Doctor> doctorFilterCombo;

    @FXML
    private Button applyFilterBtn;

    @FXML
    private Button clearFilterBtn;

    @FXML
    private TableView<Prescription> prescriptionsTable;

    @FXML
    private TableColumn<Prescription, Integer> prescriptionIdCol;

    @FXML
    private TableColumn<Prescription, String> doctorNameCol;

    @FXML
    private TableColumn<Prescription, String> dateIssuedCol;

    @FXML
    private TableColumn<Prescription, String> diagnosisCol;

    @FXML
    private TableColumn<Prescription, Void> actionsCol;

    @FXML
    private VBox prescriptionDetailsPane;

    @FXML
    private Button closeDetailsBtn;

    @FXML
    private Label detailsPrescriptionIdLabel;

    @FXML
    private Label detailsDateIssuedLabel;

    @FXML
    private Label detailsDoctorLabel;

    @FXML
    private Label detailsDiagnosisLabel;

    @FXML
    private Label detailsRemarksLabel;

    @FXML
    private TableView<Medication> medicationsTable;

    @FXML
    private TableColumn<Medication, String> medicationNameCol;

    @FXML
    private TableColumn<Medication, String> dosageCol;

    @FXML
    private TableColumn<Medication, String> frequencyCol;

    @FXML
    private TableColumn<Medication, String> durationCol;

    @FXML
    private Button printPrescriptionBtn;

    private Patient currentPatient;
    private DatabaseConnection dbConnection;
    private ObservableList<Prescription> prescriptionsList = FXCollections.observableArrayList();
    private ObservableList<Medication> medicationsList = FXCollections.observableArrayList();
    private ObservableList<Doctor> doctorsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set current date
        dateLabel.setText(LocalDate.now().toString());

        // Initialize database connection
        dbConnection = new DatabaseConnection();

        // Initialize table columns
        prescriptionIdCol.setCellValueFactory(new PropertyValueFactory<>("prescriptionId"));
        doctorNameCol.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        dateIssuedCol.setCellValueFactory(new PropertyValueFactory<>("dateIssued"));
        diagnosisCol.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));

        // Initialize medications table columns
        medicationNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        dosageCol.setCellValueFactory(new PropertyValueFactory<>("dosage"));
        frequencyCol.setCellValueFactory(new PropertyValueFactory<>("frequency"));
        durationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));

        // Set up the actions column with a view details button
        setupActionsColumn();

        // Load doctors for filter
        loadDoctors();
    }

    public void setPatient(Patient patient) {
        this.currentPatient = patient;
    }

    public void loadData() {
        if (currentPatient != null) {
            loadPrescriptions();
        }
    }

    private void loadDoctors() {
        doctorsList.clear();
        String query = "SELECT doctor_id, first_name, mid_name, last_name, specialization FROM Doctor ORDER BY first_name";

        try {
            ResultSet rs = dbConnection.executeQuery(query);

            while (rs != null && rs.next()) {
                String firstName = rs.getString("first_name");
                String middleName = rs.getString("mid_name");
                String lastName = rs.getString("last_name");

                Doctor doctor = new Doctor(
                        rs.getInt("doctor_id"),
                        firstName != null ? firstName : "",
                        middleName != null ? middleName : "",
                        lastName != null ? lastName : "",
                        rs.getString("specialization"),
                        0,          // experience default
                        "",         // language default
                        "",         // email default
                        "",         // phoneNo default
                        "",         // schedule default
                        null        // picture default
                );

                doctorsList.add(doctor);
            }

            if (rs != null) rs.close();

            doctorFilterCombo.setItems(doctorsList);

        } catch (SQLException e) {
            System.err.println("Error loading doctors: " + e.getMessage());
        }
    }

    private void loadPrescriptions() {
        prescriptionsList.clear();

        StringBuilder queryBuilder = new StringBuilder(
                "SELECT p.prescription_id, p.date_issued, p.remarks, p.diagnosis, " +
                        "d.doctor_id, CONCAT_WS(' ', d.first_name, d.mid_name, d.last_name) AS doctor_name, d.specialization " +
                        "FROM Prescription p " +
                        "JOIN Doctor d ON p.doctor_id = d.doctor_id " +
                        "WHERE p.patient_id = ? "
        );


        // Apply date filter if set
        if (fromDatePicker.getValue() != null) {
            queryBuilder.append("AND p.date_issued >= ? ");
        }

        if (toDatePicker.getValue() != null) {
            queryBuilder.append("AND p.date_issued <= ? ");
        }

        // Apply doctor filter if set
        if (doctorFilterCombo.getValue() != null) {
            queryBuilder.append("AND p.doctor_id = ? ");
        }

        queryBuilder.append("ORDER BY p.date_issued DESC");

        try {
            int paramCount = 1;
            Object[] params = new Object[4]; // Maximum 4 parameters
            params[0] = currentPatient.getPatientId();

            if (fromDatePicker.getValue() != null) {
                params[paramCount++] = Date.valueOf(fromDatePicker.getValue());
            }

            if (toDatePicker.getValue() != null) {
                params[paramCount++] = Date.valueOf(toDatePicker.getValue());
            }

            if (doctorFilterCombo.getValue() != null) {
                params[paramCount++] = doctorFilterCombo.getValue().getDoctorId();
            }

            // Create final array with exact size
            Object[] finalParams = new Object[paramCount];
            System.arraycopy(params, 0, finalParams, 0, paramCount);

            ResultSet rs = dbConnection.executePreparedQuery(queryBuilder.toString(), finalParams);

            while (rs != null && rs.next()) {
                Prescription prescription = new Prescription(
                        rs.getInt("prescription_id"),
                        currentPatient.getPatientId(),
                        rs.getInt("doctor_id"),
                        rs.getString("doctor_name"),
                        rs.getString("specialization"),
                        rs.getDate("date_issued").toString(),
                        rs.getString("diagnosis"),
                        rs.getString("remarks")
                );
                prescriptionsList.add(prescription);
            }

            if (rs != null) {
                rs.close();
            }

            prescriptionsTable.setItems(prescriptionsList);

        } catch (SQLException e) {
            System.err.println("Error loading prescriptions: " + e.getMessage());
        }
    }

    private void setupActionsColumn() {
        Callback<TableColumn<Prescription, Void>, TableCell<Prescription, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Prescription, Void> call(final TableColumn<Prescription, Void> param) {
                return new TableCell<>() {
                    private final Button viewBtn = new Button("View Details");

                    {
                        viewBtn.setStyle("-fx-background-color: #2d7ae5; -fx-text-fill: white;");
                        viewBtn.setOnAction((ActionEvent event) -> {
                            Prescription prescription = getTableView().getItems().get(getIndex());
                            showPrescriptionDetails(prescription);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(viewBtn);
                        }
                    }
                };
            }
        };

        actionsCol.setCellFactory(cellFactory);
    }

    private void showPrescriptionDetails(Prescription prescription) {
        // Set the prescription details
        detailsPrescriptionIdLabel.setText(String.valueOf(prescription.getPrescriptionId()));
        detailsDateIssuedLabel.setText(prescription.getDateIssued());
        detailsDoctorLabel.setText(prescription.getDoctorName() + " (" + prescription.getSpecialization() + ")");
        detailsDiagnosisLabel.setText(prescription.getDiagnosis());
        detailsRemarksLabel.setText(prescription.getRemarks());

        // Load medications for this prescription
        loadMedicationsForPrescription(prescription.getPrescriptionId());

        // Show the details pane
        prescriptionDetailsPane.setVisible(true);
        prescriptionDetailsPane.setManaged(true);
    }

    private void loadMedicationsForPrescription(int prescriptionId) {
        medicationsList.clear();

        String query =
                "SELECT m.medication_id, m.name, m.dosage, m.frequency, m.duration " +
                        "FROM PrescriptionMedication pm " +
                        "JOIN Medication m ON pm.medication_id = m.medication_id " +
                        "WHERE pm.prescription_id = ?";

        try {
            ResultSet rs = dbConnection.executePreparedQuery(query, prescriptionId);

            while (rs != null && rs.next()) {
                Medication medication = new Medication(
                        rs.getInt("medication_id"),
                        rs.getString("name"),
                        rs.getString("dosage"),
                        rs.getString("frequency"),
                        rs.getString("duration")
                );
                medicationsList.add(medication);
            }

            if (rs != null) {
                rs.close();
            }

            medicationsTable.setItems(medicationsList);

        } catch (SQLException e) {
            System.err.println("Error loading medications: " + e.getMessage());
        }
    }

    @FXML
    private void handleApplyFilter() {
        loadPrescriptions();
    }

    @FXML
    private void handleClearFilter() {
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        doctorFilterCombo.setValue(null);
        loadPrescriptions();
    }

    @FXML
    private void handleCloseDetails() {
        prescriptionDetailsPane.setVisible(false);
        prescriptionDetailsPane.setManaged(false);
    }

    @FXML
    private void handlePrintPrescription() {
        // Implement printing functionality here
        // This would typically generate a PDF or send to a printer
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Print Prescription");
        alert.setHeaderText(null);
        alert.setContentText("Prescription sent to printer!");
        alert.showAndWait();
    }
}