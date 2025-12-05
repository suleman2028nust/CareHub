package com.example.db_project1;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.StringConverter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DoctorPrescriptionsController implements Initializable {

    // FXML Controls
    @FXML private Button backBtn;
    @FXML private Label prescriptionCountLabel;
    @FXML private TextField searchField;
    @FXML private DatePicker datePicker;
    @FXML private Button searchBtn;
    @FXML private Button newPrescriptionBtn;
    @FXML private Button refreshBtn;

    // Prescription Table
    @FXML private TableView<PrescriptionData> prescriptionsTable;
    @FXML private TableColumn<PrescriptionData, Integer> idColumn;
    @FXML private TableColumn<PrescriptionData, String> patientNameColumn;
    @FXML private TableColumn<PrescriptionData, String> dateIssuedColumn;
    @FXML private TableColumn<PrescriptionData, String> diagnosisColumn;
    @FXML private TableColumn<PrescriptionData, String> medicationsColumn;
    @FXML private TableColumn<PrescriptionData, PrescriptionData> actionsColumn;

    // Prescription Form
    @FXML private VBox prescriptionFormContainer;
    @FXML private ComboBox<PatientData> patientComboBox;
    @FXML private DatePicker prescriptionDate;
    @FXML private TextField diagnosisField;
    @FXML private ComboBox<MedicationData> medicationComboBox;
    @FXML private Button addMedicationBtn;
    @FXML private ListView<MedicationDetails> selectedMedicationsListView;
    @FXML private TextArea remarksTextArea;
    @FXML private Button cancelBtn;
    @FXML private Button savePrescriptionBtn;

    // Add Medication Dialog
    @FXML private VBox addMedicationDialog;
    @FXML private TextField dosageField;
    @FXML private TextField frequencyField;
    @FXML private TextField durationField;
    @FXML private Button cancelMedicationBtn;
    @FXML private Button confirmMedicationBtn;

    // View Prescription Dialog
    @FXML private VBox viewPrescriptionDialog;
    @FXML private Label prescriptionIdLabel;
    @FXML private Label patientNameLabel;
    @FXML private Label dateIssuedLabel;
    @FXML private Label diagnosisLabel;
    @FXML private TableView<MedicationDetails> medicationsTableView;
    @FXML private TableColumn<MedicationDetails, String> medNameColumn;
    @FXML private TableColumn<MedicationDetails, String> medDosageColumn;
    @FXML private TableColumn<MedicationDetails, String> medFrequencyColumn;
    @FXML private TableColumn<MedicationDetails, String> medDurationColumn;
    @FXML private TextArea viewRemarksTextArea;
    @FXML private Button printBtn;
    @FXML private Button closeDetailsBtn;

    // Instance variables
    private DatabaseConnection dbConnection;
    private Doctor currentDoctor;
    private ObservableList<PrescriptionData> prescriptionsList;
    private ObservableList<PatientData> patientsList;
    private ObservableList<MedicationData> medicationsList;
    private ObservableList<MedicationDetails> selectedMedications;
    private MedicationData currentlySelectedMedication;
    private PrescriptionData currentlyViewedPrescription;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    /**
     * Initialize the controller
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dbConnection = new DatabaseConnection();
        prescriptionsList = FXCollections.observableArrayList();
        patientsList = FXCollections.observableArrayList();
        medicationsList = FXCollections.observableArrayList();
        selectedMedications = FXCollections.observableArrayList();

        // Initialize table columns
        initializeTableColumns();

        // Initialize combo boxes
        initializeComboBoxes();

        // Set up date picker
        prescriptionDate.setValue(LocalDate.now());

        // Set up medication list view
        setupSelectedMedicationsListView();

        // Set up medication table view for viewing prescription
        setupMedicationsTableView();

        // Set the prescriptions list directly to the table
        prescriptionsTable.setItems(prescriptionsList);
        ObservableList<PrescriptionData> testData = FXCollections.observableArrayList(
                new PrescriptionData(6, 1, 1, "Hassan Jamal", "Dr. Smith", "2025-05-18", "Flu", "Rest", Collections.emptyList())
        );
        prescriptionsTable.setItems(testData);

        // Optionally, hide or disable any pagination controls if present
        // (You can comment out or remove any pagination-related code)

        // Load data for the current doctor (if set)
        // You may want to call loadData() here, or call it after setting the doctor
    }


    public void setDoctor(Doctor doctor) {
        this.currentDoctor = doctor;
        System.out.println("Doctor set: " + doctor.getName());
    }

    public void loadData() {
        if (currentDoctor != null) {
            loadPrescriptions();
            prescriptionsTable.setItems(prescriptionsList);
            loadPatients();
            loadMedications();

        } else {
            System.err.println("Error: Cannot load data, doctor is not set");
        }
    }

    private void initializeTableColumns() {
        // For Integer property, wrap with ReadOnlyObjectWrapper
        idColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getPrescriptionId())
        );

        // For String properties, wrap with SimpleStringProperty
        patientNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPatientName())
        );

        dateIssuedColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDateIssued())
        );

        diagnosisColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDiagnosis())
        );

        medicationsColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMedicationsText())
        );

        // Setup actions column with buttons (assuming you have this method)
        setupActionsColumn();
    }


    private void setupActionsColumn() {
        Callback<TableColumn<PrescriptionData, PrescriptionData>, TableCell<PrescriptionData, PrescriptionData>> cellFactory =
                new Callback<>() {
                    @Override
                    public TableCell<PrescriptionData, PrescriptionData> call(TableColumn<PrescriptionData, PrescriptionData> param) {
                        return new TableCell<>() {
                            private final Button viewBtn = new Button("View");
                            private final Button deleteBtn = new Button("Delete");

                            {
                                viewBtn.setStyle("-fx-background-color: #2d7ae5; -fx-text-fill: white;");
                                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                                viewBtn.setOnAction((ActionEvent event) -> {
                                    PrescriptionData data = getTableView().getItems().get(getIndex());
                                    viewPrescriptionDetails(data);
                                });

                                deleteBtn.setOnAction((ActionEvent event) -> {
                                    PrescriptionData data = getTableView().getItems().get(getIndex());
                                    deletePrescription(data);
                                });
                            }

                            @Override
                            protected void updateItem(PrescriptionData prescription, boolean empty) {
                                super.updateItem(prescription, empty);
                                if (empty) {
                                    setGraphic(null);
                                } else {
                                    // Create a toolbar with both buttons
                                    ToolBar toolbar = new ToolBar(viewBtn, deleteBtn);
                                    toolbar.setStyle("-fx-background-color: transparent; -fx-spacing: 5;");
                                    setGraphic(toolbar);
                                }
                            }
                        };
                    }
                };
        actionsColumn.setCellFactory(cellFactory);
    }

    private void initializeComboBoxes() {
        // Set up patient combo box
        patientComboBox.setConverter(new StringConverter<PatientData>() {
            @Override
            public String toString(PatientData patient) {
                return patient != null ? patient.getName() : "";
            }

            @Override
            public PatientData fromString(String string) {
                return null; // Not needed for this use case
            }
        });

        // Set up medication combo box
        medicationComboBox.setConverter(new StringConverter<MedicationData>() {
            @Override
            public String toString(MedicationData medication) {
                return medication != null ? medication.getName() : "";
            }

            @Override
            public MedicationData fromString(String string) {
                return null; // Not needed for this use case
            }
        });
    }

    private void setupSelectedMedicationsListView() {
        selectedMedicationsListView.setCellFactory(lv -> new ListCell<MedicationDetails>() {
            private final Button removeButton = new Button("Remove");

            {
                removeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                removeButton.setOnAction(event -> {
                    MedicationDetails item = getItem();
                    selectedMedications.remove(item);
                });
            }

            @Override
            protected void updateItem(MedicationDetails item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    String details = item.getName() + " - " + item.getDosage() +
                            " - " + item.getFrequency() + " - " + item.getDuration();
                    setText(details);

                    // Create a toolbar with the remove button
                    ToolBar toolbar = new ToolBar(removeButton);
                    toolbar.setStyle("-fx-background-color: transparent;");
                    setGraphic(toolbar);
                }
            }
        });

        selectedMedicationsListView.setItems(selectedMedications);
    }

    private void setupMedicationsTableView() {
        medNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        medDosageColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDosage()));
        medFrequencyColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFrequency()));
        medDurationColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDuration()));
    }

    // Load prescriptions for the current doctor
    private void loadPrescriptions() {
        prescriptionsList.clear();

        try {
            String query = "SELECT p.prescription_id, p.patient_id, p.date_issued, p.diagnosis, p.remarks, " +
                    "pt.name AS patient_name " +
                    "FROM Prescription p " +
                    "JOIN Patient pt ON p.patient_id = pt.patient_id " +
                    "WHERE p.doctor_id = ? " +
                    "ORDER BY p.date_issued DESC";

            try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(query)) {
                stmt.setInt(1, currentDoctor.getDoctorId());

                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    int prescriptionId = rs.getInt("prescription_id");
                    int patientId = rs.getInt("patient_id");
                    String patientName = rs.getString("patient_name");
                    String dateIssued = rs.getDate("date_issued").toString();
                    String diagnosis = rs.getString("diagnosis");
                    String remarks = rs.getString("remarks");

                    // Get medications for this prescription
                    List<MedicationDetails> medications = getMedicationsForPrescription(prescriptionId);

                    PrescriptionData prescriptionData = new PrescriptionData(
                            prescriptionId, patientId, currentDoctor.getDoctorId(),
                            patientName, currentDoctor.getName(), dateIssued,
                            diagnosis, remarks, medications
                    );

                    prescriptionsList.add(prescriptionData);
                }
            }
            System.out.println("Loaded " + prescriptionsList.size() + " prescriptions");

            // Update table and count label
            prescriptionsTable.setItems(prescriptionsList);
            prescriptionCountLabel.setText("(" + prescriptionsList.size() + ")");
            prescriptionsTable.refresh();
            System.out.println("Loaded " + prescriptionsList.size() + " prescriptions");
            for (PrescriptionData p : prescriptionsList) {
                System.out.println("Prescription ID: " + p.getPrescriptionId() + ", Patient: " + p.getPatientName());
            }


        } catch (SQLException e) {
            System.err.println("Error loading prescriptions: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading prescriptions",
                    "There was an error loading prescriptions from the database: " + e.getMessage());
        }
    }


    // Get medications for a specific prescription
    private List<MedicationDetails> getMedicationsForPrescription(int prescriptionId) {
        List<MedicationDetails> medications = new ArrayList<>();

        try {
            String query = "SELECT m.name, m.dosage, m.frequency, m.duration " +
                    "FROM PrescriptionMedication pm " +
                    "JOIN Medication m ON pm.medication_id = m.medication_id " +
                    "WHERE pm.prescription_id = ?";

            try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(query)) {
                stmt.setInt(1, prescriptionId);

                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String name = rs.getString("name");
                    String dosage = rs.getString("dosage");
                    String frequency = rs.getString("frequency");
                    String duration = rs.getString("duration");

                    medications.add(new MedicationDetails(0, name, dosage, frequency, duration));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error loading medications for prescription: " + e.getMessage());
        }

        return medications;
    }

    // Load patients
    private void loadPatients() {
        patientsList.clear();

        try {
            String query = "SELECT p.patient_id, p.name, p.gender, p.dob " +
                    "FROM Patient p " +
                    "JOIN Appointment a ON p.patient_id = a.patient_id " +
                    "WHERE a.doctor_id = ? " +
                    "GROUP BY p.patient_id " +
                    "ORDER BY p.name";

            try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(query)) {
                stmt.setInt(1, currentDoctor.getDoctorId());

                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    int patientId = rs.getInt("patient_id");
                    String name = rs.getString("name");
                    String gender = rs.getString("gender");
                    String dob = rs.getDate("dob").toString();

                    patientsList.add(new PatientData(patientId, name, gender, dob));
                }
            }

            patientComboBox.setItems(patientsList);

        } catch (SQLException e) {
            System.err.println("Error loading patients: " + e.getMessage());
        }
    }

    // Load medications
    private void loadMedications() {
        medicationsList.clear();

        try {
            String query = "SELECT * FROM Medication ORDER BY name";

            try (Statement stmt = dbConnection.getConnection().createStatement()) {
                ResultSet rs = stmt.executeQuery(query);

                while (rs.next()) {
                    int medicationId = rs.getInt("medication_id");
                    String name = rs.getString("name");
                    String dosage = rs.getString("dosage");
                    String frequency = rs.getString("frequency");
                    String duration = rs.getString("duration");

                    medicationsList.add(new MedicationData(medicationId, name, dosage, frequency, duration));
                }
            }

            medicationComboBox.setItems(medicationsList);

        } catch (SQLException e) {
            System.err.println("Error loading medications: " + e.getMessage());
        }
    }

    @FXML
    private void backBtnOnAction(ActionEvent event) {
//        try {
//            SceneController.switchToDoctorDashboard(event, currentDoctor);
//        } catch (IOException e) {
//            System.err.println("Error navigating back: " + e.getMessage());
//        }
    }

    @FXML
    private void searchBtnOnAction(ActionEvent event) {
        String searchText = searchField.getText().toLowerCase().trim();
        LocalDate filterDate = datePicker.getValue();

        if (searchText.isEmpty() && filterDate == null) {
            prescriptionsTable.setItems(prescriptionsList);
            prescriptionCountLabel.setText("(" + prescriptionsList.size() + ")");
            return;
        }

        ObservableList<PrescriptionData> filteredList = FXCollections.observableArrayList();

        for (PrescriptionData prescription : prescriptionsList) {
            boolean matchesSearchText = searchText.isEmpty() ||
                    prescription.getPatientName().toLowerCase().contains(searchText) ||
                    prescription.getDiagnosis().toLowerCase().contains(searchText);

            boolean matchesFilterDate = filterDate == null ||
                    prescription.getDateIssued().equals(filterDate.format(dateFormatter));

            if (matchesSearchText && matchesFilterDate) {
                filteredList.add(prescription);
            }
        }

        prescriptionsTable.setItems(filteredList);
        prescriptionCountLabel.setText("(" + filteredList.size() + ")");
    }

    @FXML
    private void refreshBtnOnAction(ActionEvent event) {
        searchField.clear();
        datePicker.setValue(null);
        loadData();
    }

    @FXML
    private void newPrescriptionBtnOnAction(ActionEvent event) {
        // Reset form
        patientComboBox.getSelectionModel().clearSelection();
        prescriptionDate.setValue(LocalDate.now());
        diagnosisField.clear();
        medicationComboBox.getSelectionModel().clearSelection();
        selectedMedications.clear();
        remarksTextArea.clear();

        // Show the form
        prescriptionFormContainer.setVisible(true);
        viewPrescriptionDialog.setVisible(false);
        addMedicationDialog.setVisible(false);
    }

    @FXML
    private void cancelBtnOnAction(ActionEvent event) {
        prescriptionFormContainer.setVisible(false);
    }

    @FXML
    private void addMedicationBtnOnAction(ActionEvent event) {
        MedicationData selectedMedication = medicationComboBox.getSelectionModel().getSelectedItem();

        if (selectedMedication == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "No Medication Selected",
                    "Please select a medication from the dropdown menu.");
            return;
        }

        currentlySelectedMedication = selectedMedication;

        // Pre-fill fields with the selected medication's default values
        dosageField.setText(selectedMedication.getDosage());
        frequencyField.setText(selectedMedication.getFrequency());
        durationField.setText(selectedMedication.getDuration());

        // Show the add medication dialog
        addMedicationDialog.setVisible(true);
    }

    @FXML
    private void cancelMedicationBtnOnAction(ActionEvent event) {
        addMedicationDialog.setVisible(false);
    }

    @FXML
    private void confirmMedicationBtnOnAction(ActionEvent event) {
        if (currentlySelectedMedication == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No Medication Selected",
                    "There was an error with the selected medication.");
            addMedicationDialog.setVisible(false);
            return;
        }

        String dosage = dosageField.getText().trim();
        String frequency = frequencyField.getText().trim();
        String duration = durationField.getText().trim();

        if (dosage.isEmpty() || frequency.isEmpty() || duration.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Missing Information",
                    "Please provide all details for the medication.");
            return;
        }

        // Add to selected medications list
        MedicationDetails newMedication = new MedicationDetails(
                currentlySelectedMedication.getId(),
                currentlySelectedMedication.getName(),
                dosage,
                frequency,
                duration
        );

        // Check if this medication is already in the list
        boolean alreadyExists = false;
        for (MedicationDetails med : selectedMedications) {
            if (med.getId() == newMedication.getId()) {
                alreadyExists = true;
                break;
            }
        }

        if (alreadyExists) {
            showAlert(Alert.AlertType.WARNING, "Duplicate Medication", "Medication Already Added",
                    "This medication is already in the prescription. Please edit the existing entry or select a different medication.");
        } else {
            selectedMedications.add(newMedication);
            addMedicationDialog.setVisible(false);

            // Clear selection
            medicationComboBox.getSelectionModel().clearSelection();
        }
    }

    @FXML
    private void savePrescriptionBtnOnAction(ActionEvent event) {
        PatientData selectedPatient = patientComboBox.getSelectionModel().getSelectedItem();
        LocalDate date = prescriptionDate.getValue();
        String diagnosis = diagnosisField.getText().trim();
        String remarks = remarksTextArea.getText().trim();

        // Validate inputs
        if (selectedPatient == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "No Patient Selected",
                    "Please select a patient for this prescription.");
            return;
        }

        if (date == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "No Date Selected",
                    "Please select a date for this prescription.");
            return;
        }

        if (diagnosis.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "No Diagnosis",
                    "Please enter a diagnosis for this prescription.");
            return;
        }

        if (selectedMedications.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "No Medications",
                    "Please add at least one medication to the prescription.");
            return;
        }

        // Save the prescription to the database
        try {
            // Start transaction
            Connection conn = dbConnection.getConnection();
            conn.setAutoCommit(false);

            try {
                // Insert the prescription
                String prescriptionInsert = "INSERT INTO Prescription (patient_id, doctor_id, date_issued, diagnosis, remarks) " +
                        "VALUES (?, ?, ?, ?, ?)";

                int prescriptionId;

                try (PreparedStatement pstmt = conn.prepareStatement(prescriptionInsert, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setInt(1, selectedPatient.getId());
                    pstmt.setInt(2, currentDoctor.getDoctorId());
                    pstmt.setString(3, date.format(dateFormatter));
                    pstmt.setString(4, diagnosis);
                    pstmt.setString(5, remarks);

                    pstmt.executeUpdate();

                    // Get the generated prescription ID
                    ResultSet generatedKeys = pstmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        prescriptionId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Failed to get prescription ID");
                    }
                }

                // Insert the medications
                String medicationInsert = "INSERT INTO PrescriptionMedication (prescription_id, medication_id) VALUES (?, ?)";

                // First, check if we need to create any new medications
                Map<Integer, Integer> medicationIdMap = new HashMap<>();

                for (MedicationDetails med : selectedMedications) {
                    int originalId = med.getId();

                    // Check if we need to create a new medication record (if details differ from original)
                    boolean needsNewRecord = true;

                    for (MedicationData originalMed : medicationsList) {
                        if (originalMed.getId() == originalId &&
                                originalMed.getDosage().equals(med.getDosage()) &&
                                originalMed.getFrequency().equals(med.getFrequency()) &&
                                originalMed.getDuration().equals(med.getDuration())) {

                            // We can use the original medication record
                            medicationIdMap.put(originalId, originalId);
                            needsNewRecord = false;
                            break;
                        }
                    }

                    if (needsNewRecord) {
                        // Create a new medication record
                        String medInsert = "INSERT INTO Medication (name, dosage, frequency, duration) VALUES (?, ?, ?, ?)";

                        try (PreparedStatement pstmt = conn.prepareStatement(medInsert, Statement.RETURN_GENERATED_KEYS)) {
                            pstmt.setString(1, med.getName());
                            pstmt.setString(2, med.getDosage());
                            pstmt.setString(3, med.getFrequency());
                            pstmt.setString(4, med.getDuration());

                            pstmt.executeUpdate();

                            // Get the generated medication ID
                            ResultSet generatedKeys = pstmt.getGeneratedKeys();
                            if (generatedKeys.next()) {
                                int newMedicationId = generatedKeys.getInt(1);
                                medicationIdMap.put(originalId, newMedicationId);
                            } else {
                                throw new SQLException("Failed to get medication ID");
                            }
                        }
                    }
                }

                // Now insert the prescription-medication relationships
                try (PreparedStatement pstmt = conn.prepareStatement(medicationInsert)) {
                    for (MedicationDetails med : selectedMedications) {
                        int medId = medicationIdMap.get(med.getId());

                        pstmt.setInt(1, prescriptionId);
                        pstmt.setInt(2, medId);
                        pstmt.addBatch();
                    }

                    pstmt.executeBatch();
                }

                // Commit the transaction
                conn.commit();

                showAlert(Alert.AlertType.INFORMATION, "Success", "Prescription Saved",
                        "The prescription has been successfully saved.");

                // Refresh data and hide the form
                loadData();
                prescriptionFormContainer.setVisible(false);

            } catch (SQLException e) {
                // Rollback in case of error
                conn.rollback();
                throw e;
            } finally {
                // Restore auto-commit
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.err.println("Error saving prescription: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error Saving Prescription",
                    "There was an error saving the prescription: " + e.getMessage());
        }
    }

    private void viewPrescriptionDetails(PrescriptionData prescription) {
        currentlyViewedPrescription = prescription;

        // Populate the details
        prescriptionIdLabel.setText("(ID: " + prescription.getPrescriptionId() + ")");
        patientNameLabel.setText(prescription.getPatientName());
        dateIssuedLabel.setText(prescription.getDateIssued());
        diagnosisLabel.setText(prescription.getDiagnosis());
        viewRemarksTextArea.setText(prescription.getRemarks());

        // Set up the medications table
        ObservableList<MedicationDetails> medicationsData = FXCollections.observableArrayList(prescription.getMedications());
        medicationsTableView.setItems(medicationsData);

        // Show the view dialog
        viewPrescriptionDialog.setVisible(true);
        prescriptionFormContainer.setVisible(false);
        addMedicationDialog.setVisible(false);
    }

    @FXML
    private void closeDetailsBtnOnAction(ActionEvent event) {
        viewPrescriptionDialog.setVisible(false);
    }

    @FXML
    private void printBtnOnAction(ActionEvent event) {
        if (currentlyViewedPrescription == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No Prescription Selected",
                    "There was an error with the selected prescription.");
            return;
        }

        // Create a file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Prescription");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        fileChooser.setInitialFileName("Prescription_" + currentlyViewedPrescription.getPrescriptionId() + ".txt");

        // Show save dialog
        File file = fileChooser.showSaveDialog(printBtn.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // Write prescription information
                writer.println("=======================================");
                writer.println("           PRESCRIPTION SLIP           ");
                writer.println("=======================================");
                writer.println();
                writer.println("Prescription ID: " + currentlyViewedPrescription.getPrescriptionId());
                writer.println("Date: " + currentlyViewedPrescription.getDateIssued());
                writer.println();
                writer.println("Doctor: " + currentlyViewedPrescription.getDoctorName());
                writer.println("Specialization: " + currentDoctor.getSpecialization());
                writer.println();
                writer.println("Patient: " + currentlyViewedPrescription.getPatientName());
                writer.println();
                writer.println("Diagnosis: " + currentlyViewedPrescription.getDiagnosis());
                writer.println();
                writer.println("---------------------------------------");
                writer.println("MEDICATIONS:");
                writer.println("---------------------------------------");

                // Continuing from where the file left off
                for (MedicationDetails med : currentlyViewedPrescription.getMedications()) {
                    writer.println("* " + med.getName());
                    writer.println("  Dosage: " + med.getDosage());
                    writer.println("  Frequency: " + med.getFrequency());
                    writer.println("  Duration: " + med.getDuration());
                    writer.println();
                }

                writer.println("---------------------------------------");
                if (!currentlyViewedPrescription.getRemarks().isEmpty()) {
                    writer.println("REMARKS:");
                    writer.println(currentlyViewedPrescription.getRemarks());
                    writer.println("---------------------------------------");
                }

                writer.println();
                writer.println("Signature: ____________________");
                writer.println();
                writer.println("Please take medicines as prescribed.");
                writer.println("=======================================");

                showAlert(Alert.AlertType.INFORMATION, "Success", "Prescription Saved",
                        "The prescription has been saved to " + file.getAbsolutePath());

            } catch (IOException e) {
                System.err.println("Error saving prescription file: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to Save File",
                        "There was an error saving the prescription file: " + e.getMessage());
            }
        }
    }

    private void deletePrescription(PrescriptionData prescription) {
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Prescription");
        alert.setContentText("Are you sure you want to delete this prescription? This action cannot be undone.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                // Start transaction
                Connection conn = dbConnection.getConnection();
                conn.setAutoCommit(false);

                try {
                    // Delete prescription-medication relationships first
                    String deletePrescMeds = "DELETE FROM PrescriptionMedication WHERE prescription_id = ?";

                    try (PreparedStatement pstmt = conn.prepareStatement(deletePrescMeds)) {
                        pstmt.setInt(1, prescription.getPrescriptionId());
                        pstmt.executeUpdate();
                    }

                    // Then delete the prescription
                    String deletePrescription = "DELETE FROM Prescription WHERE prescription_id = ?";

                    try (PreparedStatement pstmt = conn.prepareStatement(deletePrescription)) {
                        pstmt.setInt(1, prescription.getPrescriptionId());
                        pstmt.executeUpdate();
                    }

                    // Commit the transaction
                    conn.commit();

                    showAlert(Alert.AlertType.INFORMATION, "Success", "Prescription Deleted",
                            "The prescription has been successfully deleted.");

                    // Refresh data
                    loadData();

                } catch (SQLException e) {
                    // Rollback in case of error
                    conn.rollback();
                    throw e;
                } finally {
                    // Restore auto-commit
                    conn.setAutoCommit(true);
                }

            } catch (SQLException e) {
                System.err.println("Error deleting prescription: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Database Error", "Error Deleting Prescription",
                        "There was an error deleting the prescription: " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Helper classes for managing data
    public static class PrescriptionData {
        private final int prescriptionId;
        private final int patientId;
        private final int doctorId;
        private final String patientName;
        private final String doctorName;
        private final String dateIssued;
        private final String diagnosis;
        private final String remarks;
        private final List<MedicationDetails> medications;

        public PrescriptionData(int prescriptionId, int patientId, int doctorId, String patientName,
                                String doctorName, String dateIssued, String diagnosis, String remarks,
                                List<MedicationDetails> medications) {
            this.prescriptionId = prescriptionId;
            this.patientId = patientId;
            this.doctorId = doctorId;
            this.patientName = patientName;
            this.doctorName = doctorName;
            this.dateIssued = dateIssued;
            this.diagnosis = diagnosis;
            this.remarks = remarks;
            this.medications = medications;
        }

        public int getPrescriptionId() {
            return prescriptionId;
        }

        public int getPatientId() {
            return patientId;
        }

        public int getDoctorId() {
            return doctorId;
        }

        public String getPatientName() {
            return patientName;
        }

        public String getDoctorName() {
            return doctorName;
        }

        public String getDateIssued() {
            return dateIssued;
        }

        public String getDiagnosis() {
            return diagnosis;
        }

        public String getRemarks() {
            return remarks != null ? remarks : "";
        }

        public List<MedicationDetails> getMedications() {
            return medications;
        }

        public String getMedicationsText() {
            if (medications.isEmpty()) {
                return "None";
            }

            StringBuilder sb = new StringBuilder();
            int count = 0;
            for (MedicationDetails med : medications) {
                if (count > 0) {
                    sb.append(", ");
                }
                sb.append(med.getName());
                count++;

                // Only show first 2 medications in the table
                if (count >= 2) {
                    if (medications.size() > 2) {
                        sb.append(" (+").append(medications.size() - 2).append(" more)");
                    }
                    break;
                }
            }
            return sb.toString();
        }
    }

    public static class PatientData {
        private final int id;
        private final String name;
        private final String gender;
        private final String dob;

        public PatientData(int id, String name, String gender, String dob) {
            this.id = id;
            this.name = name;
            this.gender = gender;
            this.dob = dob;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getGender() {
            return gender;
        }

        public String getDob() {
            return dob;
        }
    }

    public static class MedicationData {
        private final int id;
        private final String name;
        private final String dosage;
        private final String frequency;
        private final String duration;

        public MedicationData(int id, String name, String dosage, String frequency, String duration) {
            this.id = id;
            this.name = name;
            this.dosage = dosage;
            this.frequency = frequency;
            this.duration = duration;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDosage() {
            return dosage;
        }

        public String getFrequency() {
            return frequency;
        }

        public String getDuration() {
            return duration;
        }
    }

    public static class MedicationDetails {
        private final int id;
        private final String name;
        private final String dosage;
        private final String frequency;
        private final String duration;

        public MedicationDetails(int id, String name, String dosage, String frequency, String duration) {
            this.id = id;
            this.name = name;
            this.dosage = dosage;
            this.frequency = frequency;
            this.duration = duration;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDosage() {
            return dosage;
        }

        public String getFrequency() {
            return frequency;
        }

        public String getDuration() {
            return duration;
        }
    }

    // Method to export all prescriptions to a file
    @FXML
    private void exportAllPrescriptionsOnAction(ActionEvent event) {
        // Create a file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export All Prescriptions");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        fileChooser.setInitialFileName("All_Prescriptions_" + currentDoctor.getName().replace(" ", "_") + ".txt");

        // Show save dialog
        File file = fileChooser.showSaveDialog(prescriptionsTable.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("=======================================");
                writer.println("         ALL PRESCRIPTIONS REPORT      ");
                writer.println("=======================================");
                writer.println("Doctor: " + currentDoctor.getName());
                writer.println("Specialization: " + currentDoctor.getSpecialization());
                writer.println("Date Generated: " + LocalDate.now().format(dateFormatter));
                writer.println("Total Prescriptions: " + prescriptionsList.size());
                writer.println("=======================================");
                writer.println();

                // Write each prescription
                for (PrescriptionData prescription : prescriptionsList) {
                    writer.println("---------------------------------------");
                    writer.println("PRESCRIPTION #" + prescription.getPrescriptionId());
                    writer.println("---------------------------------------");
                    writer.println("Patient: " + prescription.getPatientName());
                    writer.println("Date Issued: " + prescription.getDateIssued());
                    writer.println("Diagnosis: " + prescription.getDiagnosis());
                    writer.println();
                    writer.println("Medications:");

                    for (MedicationDetails med : prescription.getMedications()) {
                        writer.println("* " + med.getName());
                        writer.println("  Dosage: " + med.getDosage());
                        writer.println("  Frequency: " + med.getFrequency());
                        writer.println("  Duration: " + med.getDuration());
                    }

                    writer.println();
                    if (!prescription.getRemarks().isEmpty()) {
                        writer.println("Remarks: " + prescription.getRemarks());
                        writer.println();
                    }
                }

                writer.println("=======================================");
                writer.println("          END OF REPORT               ");
                writer.println("=======================================");

                showAlert(Alert.AlertType.INFORMATION, "Success", "Prescriptions Exported",
                        "All prescriptions have been exported to " + file.getAbsolutePath());

            } catch (IOException e) {
                System.err.println("Error exporting prescriptions: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to Export Prescriptions",
                        "There was an error exporting the prescriptions: " + e.getMessage());
            }
        }
    }

    // Method to show statistics about prescriptions
    @FXML
    private void showStatisticsOnAction(ActionEvent event) {
        // Count prescriptions by date
        Map<String, Integer> prescriptionsByDate = new HashMap<>();

        // Count most common diagnoses
        Map<String, Integer> diagnosisCounts = new HashMap<>();

        // Count most prescribed medications
        Map<String, Integer> medicationCounts = new HashMap<>();

        // Collect data
        for (PrescriptionData prescription : prescriptionsList) {
            // Count by date
            String date = prescription.getDateIssued();
            prescriptionsByDate.put(date, prescriptionsByDate.getOrDefault(date, 0) + 1);

            // Count diagnoses
            String diagnosis = prescription.getDiagnosis();
            diagnosisCounts.put(diagnosis, diagnosisCounts.getOrDefault(diagnosis, 0) + 1);

            // Count medications
            for (MedicationDetails med : prescription.getMedications()) {
                String medName = med.getName();
                medicationCounts.put(medName, medicationCounts.getOrDefault(medName, 0) + 1);
            }
        }

        // Create statistics dialog
        Alert statisticsDialog = new Alert(Alert.AlertType.INFORMATION);
        statisticsDialog.setTitle("Prescription Statistics");
        statisticsDialog.setHeaderText("Prescription Statistics for Dr. " + currentDoctor.getName());

        // Create content
        StringBuilder content = new StringBuilder();
        content.append("Total Prescriptions: ").append(prescriptionsList.size()).append("\n\n");

        // Most common diagnoses
        content.append("Top Diagnoses:\n");
        diagnosisCounts.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .limit(5)
                .forEach(entry -> content.append("- ").append(entry.getKey())
                        .append(": ").append(entry.getValue()).append("\n"));
        content.append("\n");

        // Most prescribed medications
        content.append("Top Medications:\n");
        medicationCounts.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .limit(5)
                .forEach(entry -> content.append("- ").append(entry.getKey())
                        .append(": ").append(entry.getValue()).append("\n"));

        statisticsDialog.setContentText(content.toString());
        statisticsDialog.showAndWait();
    }
}