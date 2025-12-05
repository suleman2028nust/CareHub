//package com.example.db_project1;
//
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.fxml.Initializable;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.layout.BorderPane;
//import javafx.scene.layout.StackPane;
//import javafx.scene.layout.VBox;
//
//import java.io.IOException;
//import java.net.URL;
//import java.util.ResourceBundle;
//
//public class AdminDashboardController implements Initializable {
//
//    @FXML
//    private VBox sidebar;
//
//    @FXML
//    private StackPane contentArea;
//
//    @FXML
//    private Button btnDashboard;
//
//    @FXML
//    private Button btnDoctors;
//
//    @FXML
//    private Button btnSchedule;
//
//    @FXML
//    private Button btnAppointment;
//
//    @FXML
//    private Button logoutButton;
//
//    @FXML
//    private Label nameLabel;
//
//    @FXML
//    private Label emailLabel;
//
//    @FXML
//    private ImageView profileImageView;
//
//    private Admin currentAdmin;
//    private DatabaseConnection dbConnection;
//    private boolean initialLoadComplete = false;
//
//    @Override
//    public void initialize(URL url, ResourceBundle resourceBundle) {
//        dbConnection = new DatabaseConnection();
//
//        // Set default profile image
//        try {
//            profileImageView.setImage(new Image(getClass().getResourceAsStream("/images/admin-avatar.png")));
//        } catch (Exception e) {
//            System.out.println("Failed to load admin avatar: " + e.getMessage());
//            // Try to load a default avatar if specific one not found
//            try {
//                profileImageView.setImage(new Image(getClass().getResourceAsStream("/images/default-avatar.png")));
//            } catch (Exception ex) {
//                System.out.println("Failed to load default avatar: " + ex.getMessage());
//            }
//        }
//
//        // Don't load the home view yet - we'll do it when we have the admin data
//    }
//
//    public void setAdmin(Admin admin) {
//        this.currentAdmin = admin;
//        System.out.println("Admin set in dashboard: " + (admin != null ? admin.getName() : "null"));
//
//        // Update UI with admin info
//        if (admin != null) {
//            nameLabel.setText(admin.getName());
//            emailLabel.setText(admin.getEmail());
//
//            // Load admin profile picture if available
//            if (admin.getPic() != null && !admin.getPic().isEmpty()) {
//                try {
//                    profileImageView.setImage(new Image(admin.getPic()));
//                } catch (Exception e) {
//                    System.out.println("Failed to load admin profile picture: " + e.getMessage());
//                }
//            }
//
//            // Load the Admin Home view
//            try {
//                loadView("AdminHome.fxml");
//                initialLoadComplete = true;
//            } catch (IOException e) {
//                e.printStackTrace();
//                System.out.println("Failed to load AdminHome view: " + e.getMessage());
//            }
//        }
//    }
//
//    @FXML
//    void handleLogout() {
//        try {
//            // Close database connection
//            if (dbConnection != null) {
//                dbConnection.closeConnection();
//            }
//
//            // Navigate to login screen
//            Main.setRoot("login");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @FXML
//    void handleHome(ActionEvent event) {
//        try {
//            System.out.println("Home button pressed");
//            loadView("AdminHome.fxml");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @FXML
//    void handleDoctors(ActionEvent event) {
//        try {
//            System.out.println("Doctors button pressed");
//            loadView("AdminDoctors.fxml");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /*@FXML
//    void handleSchedule(ActionEvent event) {
//        try {
//            loadView("AdminSchedule.fxml");
//            System.out.println("Schedule button pressed");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//*/
//    @FXML
//    void handleAppointments(ActionEvent event) {
//        try {
//            System.out.println("Appointments button pressed");
//            loadView("AdminAppointment.fxml");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void loadView(String fxml) throws IOException {
//        // Reset all button styles
//        resetButtonStyles();
//
//        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
//        contentArea.getChildren().clear();
//        contentArea.getChildren().add(loader.load());
//
//        // Set active button style based on the loaded view
//        switch (fxml) {
//            case "AdminHome.fxml":
//                btnDashboard.setStyle("-fx-background-color: transparent; -fx-text-fill: #1976d2; -fx-font-weight: bold;");
//                break;
//            case "AdminDoctors.fxml":
//                btnDoctors.setStyle("-fx-background-color: transparent; -fx-text-fill: #1976d2; -fx-font-weight: bold;");
//                break;
//            /*case "AdminSchedule.fxml":
//                btnSchedule.setStyle("-fx-background-color: transparent; -fx-text-fill: #1976d2; -fx-font-weight: bold;");
//                break;*/
//            case "AdminAppointment.fxml":
//                btnAppointment.setStyle("-fx-background-color: transparent; -fx-text-fill: #1976d2; -fx-font-weight: bold;");
//                break;
//            default:
//                break;
//        }
//
//
//        // Pass admin data to loaded controller if needed
//        Object controller = loader.getController();
//
//
//        if (controller instanceof AdminHomeController) {
//            ((AdminHomeController) controller).setAdmin(currentAdmin);
//            ((AdminHomeController) controller).loadData();
//        } else if (controller instanceof AdminDoctorController) {
//            ((AdminDoctorController) controller).setAdmin(currentAdmin);
//            ((AdminDoctorController) controller).loadData();
//        }
//        /*else if (controller instanceof AdminScheduleController) {
//            ((AdminScheduleController) controller).setAdmin(currentAdmin);
//            ((AdminScheduleController) controller).loadData();
//        }*/
//        else if (controller instanceof AdminAppointmentController) {
//            ((AdminAppointmentController) controller).setAdmin(currentAdmin);
//            ((AdminAppointmentController) controller).loadData();
//        }
//    }
//
//    private void resetButtonStyles() {
//        String defaultStyle = "-fx-background-color: transparent; -fx-text-fill: #222;";
//        btnDashboard.setStyle(defaultStyle);
//        btnDoctors.setStyle(defaultStyle);
//        //btnSchedule.setStyle(defaultStyle);
//        btnAppointment.setStyle(defaultStyle);
//    }
//
//}
package com.example.db_project1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML
    private VBox sidebar;

    @FXML
    private StackPane contentArea;

    @FXML
    private Button btnDashboard;

    @FXML
    private Button btnDoctors;


    @FXML
    private Button btnAppointment;

    @FXML
    private Button logoutButton;

    @FXML
    private Label nameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private ImageView profileImageView;

    private Admin currentAdmin;
    private DatabaseConnection dbConnection;
    private boolean initialLoadComplete = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dbConnection = new DatabaseConnection();

        // Set default profile image
        try {
            profileImageView.setImage(new Image(getClass().getResourceAsStream("/images/admin-avatar.png")));
        } catch (Exception e) {
            System.out.println("Failed to load admin avatar: " + e.getMessage());
            // Try to load a default avatar if specific one not found
            try {
                profileImageView.setImage(new Image(getClass().getResourceAsStream("/images/default-avatar.png")));
            } catch (Exception ex) {
                System.out.println("Failed to load default avatar: " + ex.getMessage());
            }
        }

        // Don't load the home view yet - we'll do it when we have the admin data
    }

    public void setAdmin(Admin admin) {
        this.currentAdmin = admin;
        System.out.println("Admin set in dashboard: " + (admin != null ? admin.getName() : "null"));

        // Update UI with admin info
        if (admin != null) {
            nameLabel.setText(admin.getName());
            emailLabel.setText(admin.getEmail());

            // Load admin profile picture if available
            if (admin.getPic() != null && !admin.getPic().isEmpty()) {
                try {
                    profileImageView.setImage(new Image(admin.getPic()));
                } catch (Exception e) {
                    System.out.println("Failed to load admin profile picture: " + e.getMessage());
                }
            }

            // Load the Admin Home view
            try {
                loadView("AdminHome.fxml");
                initialLoadComplete = true;
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Failed to load AdminHome view: " + e.getMessage());
            }
        }
    }

    @FXML
    void handleLogout() {
        try {
            // Close database connection
            if (dbConnection != null) {
                dbConnection.closeConnection();
            }

            // Navigate to login screen
            Main.setRoot("login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleHome(ActionEvent event) {
        try {
            System.out.println("Home button pressed");
            loadView("AdminHome.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleDoctors(ActionEvent event) {
        try {
            System.out.println("Doctors button pressed");
            loadView("AdminDoctors.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    void handleAppointments(ActionEvent event) {
        try {
            System.out.println("Appointments button pressed");
            loadView("AdminAppointment.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load AdminAppointment view: " + e.getMessage());
        }
    }

    private void loadView(String fxml) throws IOException {
        // Reset all button styles
        resetButtonStyles();

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        contentArea.getChildren().clear();
        contentArea.getChildren().add(loader.load());

        // Set active button style based on the loaded view
        switch (fxml) {
            case "AdminHome.fxml":
                btnDashboard.setStyle("-fx-background-color: transparent; -fx-text-fill: #1976d2; -fx-font-weight: bold;");
                break;
            case "AdminDoctors.fxml":
                btnDoctors.setStyle("-fx-background-color: transparent; -fx-text-fill: #1976d2; -fx-font-weight: bold;");
                break;
            case "AdminAppointment.fxml":
                btnAppointment.setStyle("-fx-background-color: transparent; -fx-text-fill: #1976d2; -fx-font-weight: bold;");
                break;
            default:
                break;
        }

        // Pass admin data to loaded controller if needed
        Object controller = loader.getController();

        if (controller instanceof AdminHomeController) {
            ((AdminHomeController) controller).setAdmin(currentAdmin);
            ((AdminHomeController) controller).loadData();
        } else if (controller instanceof AdminDoctorController) {
            ((AdminDoctorController) controller).setAdmin(currentAdmin);
            ((AdminDoctorController) controller).loadData();
        } else if (controller instanceof AdminAppointmentController) {
            ((AdminAppointmentController) controller).setAdmin(currentAdmin);
            ((AdminAppointmentController) controller).loadData();
        }
    }

    private void resetButtonStyles() {
        String defaultStyle = "-fx-background-color: transparent; -fx-text-fill: #222;";
        btnDashboard.setStyle(defaultStyle);
        btnDoctors.setStyle(defaultStyle);
        btnAppointment.setStyle(defaultStyle);
    }
}