package com.example.db_project1;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminDoctorController implements Initializable {

    @FXML
    private Label dateLabel;

    @FXML
    private Label allDoctorsLabel;

    @FXML
    private TableView<Doctor> doctorTable;

    @FXML
    private TableColumn<Doctor, ImageView> pictureColumn;

    @FXML
    private TableColumn<Doctor, Integer> idColumn;

    @FXML
    private TableColumn<Doctor, String> nameColumn;

    @FXML
    private TableColumn<Doctor, String> emailColumn;

    @FXML
    private TableColumn<Doctor, String> specialtiesColumn;

    @FXML
    private TableColumn<Doctor, String> eventsColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private Button addNewButton;

    @FXML
    private Button backButton;

    private Admin currentAdmin;
    private DatabaseConnection dbConnection;
    private ObservableList<Doctor> doctorList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("AdminDoctorController initializing...");

        try {
            // Initialize database connection
            dbConnection = new DatabaseConnection();

            // Set current date
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            if (dateLabel != null) {
                dateLabel.setText(today.format(formatter));
            } else {
                System.out.println("Warning: dateLabel is null");
            }

            if (doctorTable == null) {
                System.out.println("Error: doctorTable is null. Check your FXML file.");
                return;
            }

            // Initialize table columns
            initializeTableColumns();

            // Set up search button action
            if (searchButton != null) {
                searchButton.setOnAction(event -> searchDoctors());
            } else {
                System.out.println("Warning: searchButton is null");
            }

            // Set add new button action
            if (addNewButton != null) {
                addNewButton.setOnAction(event -> addNewDoctor());
            } else {
                System.out.println("Warning: addNewButton is null");
            }

            // Set back button action
            if (backButton != null) {
                backButton.setOnAction(event -> handleBack());
            } else {
                System.out.println("Warning: backButton is null");
            }

            // Set search field enter key action
            if (searchField != null) {
                searchField.setOnAction(event -> searchDoctors());
            } else {
                System.out.println("Warning: searchField is null");
            }

            System.out.println("AdminDoctorController initialization complete");
        } catch (Exception e) {
            System.out.println("Error during AdminDoctorController initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeTableColumns() {
        try {
            if (idColumn != null) {
                idColumn.setCellValueFactory(new PropertyValueFactory<>("doctorId"));
            } else {
                System.out.println("Warning: idColumn is null");
            }

            if (nameColumn != null) {
                nameColumn.setCellValueFactory(cellData -> {
                    try {
                        return new SimpleStringProperty(cellData.getValue().getName());
                    } catch (Exception e) {
                        System.out.println("Error setting name column: " + e.getMessage());
                        return new SimpleStringProperty("Error");
                    }
                });
            } else {
                System.out.println("Warning: nameColumn is null");
            }

            if (emailColumn != null) {
                emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
            } else {
                System.out.println("Warning: emailColumn is null");
            }

            if (specialtiesColumn != null) {
                specialtiesColumn.setCellValueFactory(new PropertyValueFactory<>("specialization"));
            } else {
                System.out.println("Warning: specialtiesColumn is null");
            }

            // Setup picture column to display doctor's image
            if (pictureColumn != null) {
                setupPictureColumn();
            } else {
                System.out.println("Warning: pictureColumn is null");
            }

            // Set up events column with buttons
            if (eventsColumn != null) {
                setupEventsColumn();
            } else {
                System.out.println("Warning: eventsColumn is null");
            }
        } catch (Exception e) {
            System.out.println("Error initializing table columns: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupPictureColumn() {
        pictureColumn.setCellValueFactory(cellData -> {
            try {
                Doctor doctor = cellData.getValue();
                if (doctor == null) {
                    return new SimpleObjectProperty<>(new ImageView());
                }

                ImageView imageView = new ImageView();
                imageView.setFitHeight(50);
                imageView.setFitWidth(50);
                imageView.setPreserveRatio(true);

                if (doctor.getPicture() != null) {
                    imageView.setImage(doctor.getPicture());
                } else {
                    // Set default image
                    try {
                        Image defaultImage = new Image(getClass().getResourceAsStream("/images/default-avatar.png"));
                        if (defaultImage != null) {
                            imageView.setImage(defaultImage);
                        } else {
                            System.out.println("Default image couldn't be loaded");
                        }
                    } catch (Exception e) {
                        System.out.println("Failed to load default doctor avatar: " + e.getMessage());
                    }
                }

                return new SimpleObjectProperty<>(imageView);
            } catch (Exception e) {
                System.out.println("Error in picture column factory: " + e.getMessage());
                return new SimpleObjectProperty<>(new ImageView());
            }
        });
    }

    private void setupEventsColumn() {
        eventsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");

            {
                // Style buttons
                viewButton.setStyle("-fx-background-color: #e3f0fc; -fx-text-fill: #1976d2;");
                editButton.setStyle("-fx-background-color: #e3f0fc; -fx-text-fill: #1976d2;");
                deleteButton.setStyle("-fx-background-color: #ffebee; -fx-text-fill: #f44336;");

                // Add button handlers
                viewButton.setOnAction(event -> {
                    try {
                        Doctor doctor = getTableView().getItems().get(getIndex());
                        showDoctorDetails(doctor);
                    } catch (Exception e) {
                        System.out.println("Error in view button handler: " + e.getMessage());
                    }
                });

                editButton.setOnAction(event -> {
                    try {
                        Doctor doctor = getTableView().getItems().get(getIndex());
                        editDoctor(doctor);
                    } catch (Exception e) {
                        System.out.println("Error in edit button handler: " + e.getMessage());
                    }
                });

                deleteButton.setOnAction(event -> {
                    try {
                        Doctor doctor = getTableView().getItems().get(getIndex());
                        deleteDoctor(doctor);
                    } catch (Exception e) {
                        System.out.println("Error in delete button handler: " + e.getMessage());
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    // Create an HBox to hold buttons with spacing
                    try {
                        javafx.scene.layout.HBox buttonsBox = new javafx.scene.layout.HBox(5);
                        buttonsBox.getChildren().addAll(viewButton, editButton, deleteButton);
                        setGraphic(buttonsBox);
                    } catch (Exception e) {
                        System.out.println("Error updating cell: " + e.getMessage());
                        setGraphic(null);
                    }
                }
            }
        });
    }

    public void setAdmin(Admin admin) {
        try {
            this.currentAdmin = admin;
            System.out.println("Admin set in doctor controller: " + (admin != null ? admin.getName() : "null"));

            // Load data once admin is set
            loadData();
        } catch (Exception e) {
            System.out.println("Error setting admin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadData() {
        try {
            System.out.println("Loading data for Admin Doctors view");
            loadDoctors("");
        } catch (Exception e) {
            System.out.println("Error loading data: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Data Loading Error", "Failed to load doctors data", e.getMessage());
        }
    }

    private void loadDoctors(String searchTerm) {
        doctorList.clear();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            if (conn == null) {
                System.out.println("Database connection is null");
                showErrorAlert("Database Error", "Could not connect to database", "Check your database connection settings.");
                return;
            }

            String query;

            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                query = "SELECT * FROM doctor ORDER BY first_name, last_name";
                pstmt = conn.prepareStatement(query);
            } else {
                query = "SELECT * FROM doctor WHERE first_name LIKE ? OR " +
                        "last_name LIKE ? OR " +
                        "email LIKE ? OR " +
                        "specialization LIKE ? " +
                        "ORDER BY first_name, last_name";
                pstmt = conn.prepareStatement(query);
                String searchPattern = "%" + searchTerm + "%";
                pstmt.setString(1, searchPattern);
                pstmt.setString(2, searchPattern);
                pstmt.setString(3, searchPattern);
                pstmt.setString(4, searchPattern);
            }

            System.out.println("Executing query: " + query.replace("?", "'%" + searchTerm + "%'"));
            rs = pstmt.executeQuery();
            System.out.println("Query executed");

            int count = 0;
            while (rs != null && rs.next()) {
                try {
                    Doctor doctor = new Doctor();
                    doctor.setDoctorId(rs.getInt("doctor_id"));
                    doctor.setFirstName(rs.getString("first_name"));
                    doctor.setMiddleName(rs.getString("mid_name"));
                    doctor.setLastName(rs.getString("last_name"));
                    doctor.setEmail(rs.getString("email"));
                   // doctor.setPhoneNo(rs.getString("contact_number"));
                    doctor.setSpecialization(rs.getString("specialization"));
                    doctor.setExperience(rs.getInt("experience"));
                    doctor.setLanguage(rs.getString("language"));
                    doctor.setSchedule(rs.getString("schedule"));

                    // Load image if available - with improved error handling
                    if (rs.getBlob("picture") != null) {
                        try {
                            Blob imageBlob = rs.getBlob("picture");
                            if (imageBlob != null && imageBlob.length() > 0) {
                                doctor.setPicture(new Image(imageBlob.getBinaryStream()));
                            }
                        } catch (Exception e) {
                            System.out.println("Failed to load doctor image: " + e.getMessage());
                            // Continue without the image rather than failing
                        }
                    }

                    doctorList.add(doctor);
                    count++;
                } catch (Exception e) {
                    System.out.println("Error processing doctor record: " + e.getMessage());
                    e.printStackTrace();
                    // Continue with the next record instead of failing completely
                }
            }

            System.out.println("Loaded " + count + " doctors");

            if (doctorTable != null) {
                doctorTable.setItems(doctorList);
            } else {
                System.out.println("Warning: doctorTable is null, cannot set items");
            }

            if (allDoctorsLabel != null) {
                allDoctorsLabel.setText("All Doctors (" + doctorList.size() + ")");
            } else {
                System.out.println("Warning: allDoctorsLabel is null");
            }

        } catch (SQLException e) {
            System.out.println("SQL Error loading doctors: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Database Error", "Failed to load doctors from database", e.getMessage());
        } catch (Exception e) {
            System.out.println("General error loading doctors: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Error", "An unexpected error occurred", e.getMessage());
        } finally {
            // Close resources
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                // Don't close conn as it's shared
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void searchDoctors() {
        try {
            String searchTerm = searchField.getText();
            loadDoctors(searchTerm);
        } catch (Exception e) {
            System.out.println("Error searching doctors: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Search Error", "Failed to search doctors", e.getMessage());
        }
    }

    private void showDoctorDetails(Doctor doctor) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Doctor Details");
            alert.setHeaderText("Information for Dr. " + doctor.getName());

            // Create content for the alert
            StringBuilder content = new StringBuilder();
            content.append("ID: ").append(doctor.getDoctorId()).append("\n");
            content.append("Name: ").append(doctor.getName()).append("\n");
            content.append("Email: ").append(doctor.getEmail()).append("\n");
            content.append("Phone: ").append(doctor.getPhoneNo() != null ? doctor.getPhoneNo() : "Not available").append("\n");
            content.append("Specialization: ").append(doctor.getSpecialization()).append("\n");
            content.append("Experience: ").append(doctor.getExperience()).append(" years\n");
            content.append("Languages: ").append(doctor.getLanguage() != null ? doctor.getLanguage() : "Not specified");

            if (doctor.getSchedule() != null && !doctor.getSchedule().isEmpty()) {
                content.append("\nSchedule: ").append(doctor.getSchedule());
            }

            alert.setContentText(content.toString());
            alert.showAndWait();
        } catch (Exception e) {
            System.out.println("Error showing doctor details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void editDoctor(Doctor doctor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("DoctorEditForm.fxml"));
            Parent root = loader.load();

            DoctorEditFormController controller = loader.getController();
            controller.setDoctor(doctor);
            controller.setAdminDoctorController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Doctor");
            stage.setScene(new Scene(root));

            // Wait until the edit window is closed
            stage.showAndWait();

            // Reload the data after edit window is closed
            loadData();

        } catch (IOException e) {
            System.out.println("Error opening doctor edit form: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("UI Error", "Failed to open doctor edit form", e.getMessage());
        }
    }

    private void deleteDoctor(Doctor doctor) {
        try {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Delete");
            confirmAlert.setHeaderText("Delete Doctor");
            confirmAlert.setContentText("Are you sure you want to delete Dr. " + doctor.getName() + "? This action cannot be undone.");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Connection conn = null;
                PreparedStatement checkStmt = null;
                PreparedStatement deleteAppStmt = null;
                PreparedStatement deleteDocStmt = null;
                ResultSet rs = null;

                try {
                    conn = dbConnection.getConnection();
                    if (conn == null) {
                        showErrorAlert("Database Error", "Could not connect to database", "Check your database connection settings.");
                        return;
                    }

                    // Start transaction
                    conn.setAutoCommit(false);

                    // Check if doctor has any appointments first
                    String checkQuery = "SELECT COUNT(*) as appointment_count FROM appointment WHERE doctor_id = ?";
                    checkStmt = conn.prepareStatement(checkQuery);
                    checkStmt.setInt(1, doctor.getDoctorId());
                    rs = checkStmt.executeQuery();

                    if (rs != null && rs.next()) {
                        int appointmentCount = rs.getInt("appointment_count");

                        if (appointmentCount > 0) {
                            // Ask for confirmation to delete appointments
                            Alert appointmentAlert = new Alert(Alert.AlertType.CONFIRMATION);
                            appointmentAlert.setTitle("Appointments Exist");
                            appointmentAlert.setHeaderText("Doctor Has Existing Appointments");
                            appointmentAlert.setContentText("This doctor has " + appointmentCount + " appointment(s). " +
                                    "Deleting this doctor will also delete all associated appointments. Continue?");

                            Optional<ButtonType> appointmentResult = appointmentAlert.showAndWait();
                            if (appointmentResult.isPresent() && appointmentResult.get() == ButtonType.OK) {
                                // Delete appointments first
                                String deleteAppointmentsQuery = "DELETE FROM appointment WHERE doctor_id = ?";
                                deleteAppStmt = conn.prepareStatement(deleteAppointmentsQuery);
                                deleteAppStmt.setInt(1, doctor.getDoctorId());
                                deleteAppStmt.executeUpdate();
                            } else {
                                // User canceled deletion
                                conn.rollback();
                                return;
                            }
                        }
                    }

                    // Delete the doctor from database
                    String query = "DELETE FROM doctor WHERE doctor_id = ?";
                    deleteDocStmt = conn.prepareStatement(query);
                    deleteDocStmt.setInt(1, doctor.getDoctorId());
                    int result2 = deleteDocStmt.executeUpdate();

                    if (result2 > 0) {
                        // Commit transaction
                        conn.commit();

                        // Remove from the list
                        doctorList.remove(doctor);
                        doctorTable.setItems(doctorList);
                        updateAllDoctorsLabel();

                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Success");
                        successAlert.setHeaderText(null);
                        successAlert.setContentText("Doctor deleted successfully!");
                        successAlert.showAndWait();
                    } else {
                        // Rollback if no doctor was deleted
                        conn.rollback();

                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Error");
                        errorAlert.setHeaderText(null);
                        errorAlert.setContentText("Failed to delete doctor from database. Please try again.");
                        errorAlert.showAndWait();
                    }
                } catch (SQLException e) {
                    try {
                        if (conn != null) {
                            conn.rollback();
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    e.printStackTrace();
                    showErrorAlert("Database Error", "Failed to delete doctor", e.getMessage());
                } finally {
                    try {
                        if (rs != null) rs.close();
                        if (checkStmt != null) checkStmt.close();
                        if (deleteAppStmt != null) deleteAppStmt.close();
                        if (deleteDocStmt != null) deleteDocStmt.close();
                        if (conn != null) {
                            conn.setAutoCommit(true);
                            // Don't close the connection here as it's a shared resource
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error in delete doctor process: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Error", "An unexpected error occurred", e.getMessage());
        }
    }

    @FXML
    private void addNewDoctor() {
        try {
            System.out.println("Opening add doctor form...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("adddoctor.fxml"));
            Parent root = loader.load();

            DoctorAddFormController controller = loader.getController();
            controller.setAdminDoctorController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add New Doctor");
            stage.setScene(new Scene(root));

            // Wait until the add window is closed
            stage.showAndWait();

            // Reload the data
            loadData();

        } catch (IOException e) {
            System.out.println("Error opening doctor add form: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("UI Error", "Failed to open add doctor form", e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error opening add form: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Error", "An unexpected error occurred", e.getMessage());
        }
    }

    // Method to be called by DoctorAddFormController after adding a new doctor
    public void addDoctorToTable(Doctor newDoctor) {
        try {
            // Add the new doctor to the list and update table
            doctorList.add(newDoctor);
            doctorTable.setItems(doctorList);
            updateAllDoctorsLabel();
        } catch (Exception e) {
            System.out.println("Error adding doctor to table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to be called by DoctorEditFormController after updating a doctor
    public void updateDoctorInTable(Doctor updatedDoctor) {
        try {
            // Find the doctor in the list and update it
            for (int i = 0; i < doctorList.size(); i++) {
                if (doctorList.get(i).getDoctorId() == updatedDoctor.getDoctorId()) {
                    doctorList.set(i, updatedDoctor);
                    break;
                }
            }
            doctorTable.refresh();
        } catch (Exception e) {
            System.out.println("Error updating doctor in table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateAllDoctorsLabel() {
        if (allDoctorsLabel != null) {
            allDoctorsLabel.setText("All Doctors (" + doctorList.size() + ")");
        }
    }

    @FXML
    private void handleBack() {
        try {
            System.out.println("Navigating back to admin dashboard...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminDashboard.fxml"));
            Parent root = loader.load();

            AdminDashboardController controller = loader.getController();
            controller.setAdmin(currentAdmin);

            Stage stage = (Stage) doctorTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            System.out.println("Error navigating back: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Failed to return to dashboard", e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error navigating back: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Error", "An unexpected error occurred", e.getMessage());
        }
    }

    private void showErrorAlert(String title, String header, String content) {
        try {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        } catch (Exception e) {
            System.out.println("Error showing alert: " + e.getMessage());
            e.printStackTrace();
            // If showing the alert fails, at least print to console
            System.out.println("ALERT - " + title + ": " + header + " - " + content);
        }
    }
}