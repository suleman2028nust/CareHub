package com.example.db_project1;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class AllDoctorsController implements Initializable {

    @FXML
    private Label dateLabel;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchBtn;

    @FXML
    private Button clearBtn;

    @FXML
    private Label doctorCountLabel;

    @FXML
    private TableView<Doctor> doctorsTable;

    @FXML
    private TableColumn<Doctor, Integer> doctorIdCol;

    @FXML
    private TableColumn<Doctor, String> doctorNameCol;

    @FXML
    private TableColumn<Doctor, String> specializationCol;

    @FXML
    private TableColumn<Doctor, Integer> experienceCol;

    @FXML
    private TableColumn<Doctor, String> languageCol;

    @FXML
    private TableColumn<Doctor, String> emailCol;

    @FXML
    private TableColumn<Doctor, Void> actionsCol;

    @FXML
    private Pagination doctorsPagination;

    private final int ROWS_PER_PAGE = 10;
    private Patient currentPatient;
    private DatabaseConnection dbConnection;
    private ObservableList<Doctor> allDoctors = FXCollections.observableArrayList();
    private ObservableList<Doctor> filteredDoctors = FXCollections.observableArrayList();
    private String searchTerm = "";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set up columns exactly as in your test
        doctorIdCol.setCellValueFactory(new PropertyValueFactory<>("doctorId"));
        doctorNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        specializationCol.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        experienceCol.setCellValueFactory(new PropertyValueFactory<>("experience"));
        languageCol.setCellValueFactory(new PropertyValueFactory<>("language"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        addActionButtonsToTable();

        // Load all doctors from the database
        loadAllDoctors();

        // Set all doctors directly to the table (no pagination)
        doctorsTable.setItems(filteredDoctors);

        // Hide pagination for now
        doctorsPagination.setVisible(false);

        // Update doctor count label
        doctorCountLabel.setText(String.valueOf(filteredDoctors.size()));
    }



    // This method creates the content for each page
    public void setPatient(Patient patient) {
        System.out.println("Setting patient: " + (patient != null ? patient.getName() : "null"));
        this.currentPatient = patient;
        // Load data immediately after setting the patient
        loadData();
    }

    public void loadData() {
        System.out.println("Loading doctor data...");
        loadAllDoctors();

        // If search term was provided earlier, apply it now
        if (!searchTerm.isEmpty()) {
            searchField.setText(searchTerm);
            handleSearchButton(new ActionEvent());
        }
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
        searchField.setText(searchTerm);
    }

    private void loadAllDoctors() {
        allDoctors.clear();
        filteredDoctors.clear();

        String query = "SELECT id, name, specialization, experience, language, email, phone_no FROM doctor_table_view";
        System.out.println("Executing query: " + query);

        try (Connection conn = dbConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Database connection is null.");
                return;
            }

            try (PreparedStatement statement = conn.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {

                int rowCount = 0;
                while (resultSet.next()) {
                    rowCount++;
                    Doctor doctor = new Doctor(
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getString("specialization"),
                            resultSet.getInt("experience"),
                            resultSet.getString("language"),
                            resultSet.getString("email"),
                            resultSet.getString("phone_no")
                    );
                    allDoctors.add(doctor);
                }
                System.out.println("Loaded " + rowCount + " doctors from database");

                // Copy all doctors to filteredDoctors (for potential future filtering)
                filteredDoctors.addAll(allDoctors);

                // Update doctor count label
                doctorCountLabel.setText(String.valueOf(filteredDoctors.size()));

                // Set all doctors directly to the TableView (no pagination)
                doctorsTable.setItems(FXCollections.observableArrayList(filteredDoctors));

                // Hide pagination control since we are displaying all at once
                doctorsPagination.setVisible(false);

            } catch (SQLException e) {
                System.err.println("Error executing query: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void addActionButtonsToTable() {
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button bookBtn = new Button("Book");
            private final HBox buttonsBox = new HBox(5, viewBtn, bookBtn);

            {
                viewBtn.setStyle("-fx-background-color: #2d7ae5; -fx-text-fill: white;");
                bookBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");

                viewBtn.setOnAction(event -> {
                    Doctor doctor = getTableView().getItems().get(getIndex());
                    showDoctorDetails(doctor);
                });

                bookBtn.setOnAction(event -> {
                    Doctor doctor = getTableView().getItems().get(getIndex());
                    bookAppointment(doctor);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonsBox);
                }
            }
        });
    }
    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filteredDoctors.size());

        ObservableList<Doctor> pageItems = FXCollections.observableArrayList(filteredDoctors.subList(fromIndex, toIndex));
        doctorsTable.setItems(pageItems);

        System.out.println("Created page " + pageIndex + " with " + pageItems.size() + " doctors.");
        return doctorsTable;
    }

    private void showDoctorDetails(Doctor doctor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("doctor-details-view.fxml"));
            // Load the root layout from the FXML file (likely AnchorPane or similar)
            Parent root = loader.load();

            // Create a Dialog and set the content
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Doctor Details");

            // Create a new DialogPane and add the root as its content
            DialogPane dialogPane = new DialogPane();
            dialogPane.setContent(root);
            dialog.setDialogPane(dialogPane);

            // Optionally pass the doctor object to the controller
        /*
        DoctorDetailsController controller = loader.getController();
        controller.setDoctor(doctor);
        */

            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading doctor details dialog: " + e.getMessage());
            showAlert("Error", "Application Error", "Failed to load doctor details dialog: " + e.getMessage());
        }
    }

    private void bookAppointment(Doctor doctor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("BookAppointment.fxml"));
            Parent root = loader.load();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Book Appointment");

            DialogPane dialogPane = new DialogPane();
            dialogPane.setContent(root);
            dialog.setDialogPane(dialogPane);

            // Optional: Pass doctor and patient to the FXML controller
             BookAppointmentController controller = loader.getController();
            // controller.setDoctor(doctor);
             controller.setPatient(currentPatient);

            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading book appointment dialog: " + e.getMessage());
            showAlert("Error", "Application Error", "Failed to load booking dialog: " + e.getMessage());
        }
    }

    @FXML
    void handleSearchButton(ActionEvent event) {
        String searchText = searchField.getText().trim().toLowerCase();
        System.out.println("Searching for: " + searchText);

        if (searchText.isEmpty()) {
            filteredDoctors.setAll(allDoctors);
        } else {
            filteredDoctors.clear();

            for (Doctor doctor : allDoctors) {
                if (doctor.getName().toLowerCase().contains(searchText) ||
                        doctor.getSpecialization().toLowerCase().contains(searchText) ||
                        doctor.getEmail().toLowerCase().contains(searchText)) {
                    filteredDoctors.add(doctor);
                }
            }
        }

        System.out.println("Found " + filteredDoctors.size() + " matching doctors");

        // Update doctor count
        doctorCountLabel.setText(String.valueOf(filteredDoctors.size()));

        // Update pagination
        int pageCount = Math.max(1, (filteredDoctors.size() + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE);
        doctorsPagination.setPageCount(pageCount);
        doctorsPagination.setCurrentPageIndex(0);

        // Apply first page to table
        createPage(0);
    }

    @FXML
    void handleClearButton(ActionEvent event) {
        searchField.clear();
        filteredDoctors.setAll(allDoctors);

        // Update doctor count
        doctorCountLabel.setText(String.valueOf(filteredDoctors.size()));

        // Update pagination
        int pageCount = Math.max(1, (filteredDoctors.size() + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE);
        doctorsPagination.setPageCount(pageCount);
        doctorsPagination.setCurrentPageIndex(0);

        // Apply first page to table
        createPage(0);
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}