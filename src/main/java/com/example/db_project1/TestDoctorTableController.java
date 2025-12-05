package com.example.db_project1;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;

public class TestDoctorTableController implements Initializable {

    @FXML private TableView<testdoctor> doctorsTable;
    @FXML private TableColumn<testdoctor, Integer> doctorIdCol;
    @FXML private TableColumn<testdoctor, String> doctorNameCol;
    @FXML private TableColumn<testdoctor, String> specializationCol;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        doctorIdCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("doctorId"));
        doctorNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        specializationCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("specialization"));

        doctorsTable.setItems(FXCollections.observableArrayList(
                new testdoctor(1, "Dr. Smith", "Cardiology"),
                new testdoctor(2, "Dr. Lee", "Neurology"),
                new testdoctor(3, "Dr. Patel", "Pediatrics")
        ));
    }
}
