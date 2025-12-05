package com.example.db_project1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.net.URL;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class MyBillsController implements Initializable {

    @FXML
    private Label dateLabel;
    @FXML
    private ComboBox<String> statusFilterCombo;
    @FXML
    private DatePicker fromDatePicker;
    @FXML
    private DatePicker toDatePicker;
    @FXML
    private Label totalBillsLabel;
    @FXML
    private Label paidBillsLabel;
    @FXML
    private Label pendingBillsLabel;
    @FXML
    private Label totalAmountLabel;
    @FXML
    private TabPane billsTabPane;

    // All Bills Table
    @FXML
    private TableView<Bill> allBillsTable;
    @FXML
    private TableColumn<Bill, Integer> allBillNoCol;
    @FXML
    private TableColumn<Bill, String> allDateCol;
    @FXML
    private TableColumn<Bill, String> allTimeCol;
    @FXML
    private TableColumn<Bill, Double> allAmountCol;
    @FXML
    private TableColumn<Bill, String> allStatusCol;
    @FXML
    private TableColumn<Bill, String> allAdminCol;
    @FXML
    private TableColumn<Bill, Void> allActionsCol;

    // Paid Bills Table
    @FXML
    private TableView<Bill> paidBillsTable;
    @FXML
    private TableColumn<Bill, Integer> paidBillNoCol;
    @FXML
    private TableColumn<Bill, String> paidDateCol;
    @FXML
    private TableColumn<Bill, String> paidTimeCol;
    @FXML
    private TableColumn<Bill, Double> paidAmountCol;
    @FXML
    private TableColumn<Bill, String> paidAdminCol;
    @FXML
    private TableColumn<Bill, Void> paidActionsCol;

    // Pending Bills Table
    @FXML
    private TableView<Bill> pendingBillsTable;
    @FXML
    private TableColumn<Bill, Integer> pendingBillNoCol;
    @FXML
    private TableColumn<Bill, String> pendingDateCol;
    @FXML
    private TableColumn<Bill, String> pendingTimeCol;
    @FXML
    private TableColumn<Bill, Double> pendingAmountCol;
    @FXML
    private TableColumn<Bill, Void> pendingActionsCol;

    // Cancelled Bills Table
    @FXML
    private TableView<Bill> cancelledBillsTable;
    @FXML
    private TableColumn<Bill, Integer> cancelledBillNoCol;
    @FXML
    private TableColumn<Bill, String> cancelledDateCol;
    @FXML
    private TableColumn<Bill, String> cancelledTimeCol;
    @FXML
    private TableColumn<Bill, Double> cancelledAmountCol;
    @FXML
    private TableColumn<Bill, String> cancelledAdminCol;
    @FXML
    private TableColumn<Bill, Void> cancelledActionsCol;

    // Bill Details Section
    @FXML
    private VBox billDetailsPane;
    @FXML
    private Label detailsBillNoLabel;
    @FXML
    private Label detailsDateLabel;
    @FXML
    private Label detailsTimeLabel;
    @FXML
    private Label detailsAmountLabel;
    @FXML
    private Label detailsStatusLabel;
    @FXML
    private Label detailsAdminLabel;
    @FXML
    private Button payBillBtn;
    @FXML
    private Button downloadReceiptBtn;

    private Patient currentPatient;
    private DatabaseConnection dbConnection;
    private ObservableList<Bill> allBills = FXCollections.observableArrayList();
    private ObservableList<Bill> paidBills = FXCollections.observableArrayList();
    private ObservableList<Bill> pendingBills = FXCollections.observableArrayList();
    private ObservableList<Bill> cancelledBills = FXCollections.observableArrayList();
    private Bill selectedBill;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dbConnection = new DatabaseConnection();

        // Set current date
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        // Setup tables
        setupAllBillsTable();
        setupPaidBillsTable();
        setupPendingBillsTable();
        setupCancelledBillsTable();

        // Initialize filter combobox
        statusFilterCombo.getSelectionModel().selectFirst();
    }

    public void setPatient(Patient patient) {
        this.currentPatient = patient;
    }

    public void loadData() {
        if (currentPatient == null) {
            return;
        }

        loadBills();
        updateSummary();
    }

    private void loadBills() {
        // Clear previous data
        allBills.clear();
        paidBills.clear();
        pendingBills.clear();
        cancelledBills.clear();

        if (currentPatient == null) {
            System.out.println("Current patient is null. Cannot load bills.");
            return;
        }

        System.out.println("Loading bills...");
        System.out.println("Patient ID: " + currentPatient.getPatientId());


        String query = "SELECT b.bill_no, b.date, b.time, b.amount, b.payment_status, " +
                "CONCAT(a.first_name, ' ', a.last_name) as admin_name " +
                "FROM Bill b LEFT JOIN Admin a ON b.admin_id = a.admin_id " +
                "WHERE b.patient_id = ? ORDER BY b.date DESC, b.time DESC";

        try {
            ResultSet rs = dbConnection.executePreparedQuery(query, currentPatient.getPatientId());
            int count = 0;
            while (rs != null && rs.next()) {
                int billNo = rs.getInt("bill_no");
                String date = rs.getDate("date").toString();
                String time = rs.getTime("time").toString();
                double amount = rs.getDouble("amount");
                String status = rs.getString("payment_status");
                String adminName = rs.getString("admin_name");
                if (adminName == null) {
                    adminName = "N/A";
                }

                Bill bill = new Bill(billNo, currentPatient.getPatientId(), date, time, amount, status, adminName);

                // Add to all bills list
                allBills.add(bill);

                // Add to specific status list
                switch (status) {
                    case "Paid":
                        paidBills.add(bill);
                        break;
                    case "Pending":
                        pendingBills.add(bill);
                        break;
                    case "Cancelled":
                        cancelledBills.add(bill);
                        break;
                }
                count++;
            }

            // Update table views
            allBillsTable.setItems(allBills);
            paidBillsTable.setItems(paidBills);
            pendingBillsTable.setItems(pendingBills);
            cancelledBillsTable.setItems(cancelledBills);
            System.out.println("Total bills fetched: " + count);
            System.out.println("Paid: " + paidBills.size() +
                    ", Pending: " + pendingBills.size() +
                    ", Cancelled: " + cancelledBills.size());

        } catch (SQLException e) {
            System.err.println("Error loading bills: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load bills", e.getMessage());
        }
    }

    private void updateSummary() {
        int totalBillsCount = allBills.size();
        int paidBillsCount = paidBills.size();
        int pendingBillsCount = pendingBills.size();

        double totalAmount = 0;
        for (Bill bill : allBills) {
            totalAmount += bill.getAmount();
        }

        totalBillsLabel.setText(String.valueOf(totalBillsCount));
        paidBillsLabel.setText(String.valueOf(paidBillsCount));
        pendingBillsLabel.setText(String.valueOf(pendingBillsCount));
        totalAmountLabel.setText(String.format("$%.2f", totalAmount));
    }

    @FXML
    private void handleApplyFilter() {
        String status = statusFilterCombo.getValue();
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        // Create filtered lists
        ObservableList<Bill> filteredAll = FXCollections.observableArrayList();
        ObservableList<Bill> filteredPaid = FXCollections.observableArrayList();
        ObservableList<Bill> filteredPending = FXCollections.observableArrayList();
        ObservableList<Bill> filteredCancelled = FXCollections.observableArrayList();

        for (Bill bill : allBills) {
            LocalDate billDate = LocalDate.parse(bill.getDate());
            boolean statusMatch = "All".equals(status) || bill.getStatus().equals(status);
            boolean dateMatch = true;

            if (fromDate != null && toDate != null) {
                dateMatch = !billDate.isBefore(fromDate) && !billDate.isAfter(toDate);
            } else if (fromDate != null) {
                dateMatch = !billDate.isBefore(fromDate);
            } else if (toDate != null) {
                dateMatch = !billDate.isAfter(toDate);
            }

            if (statusMatch && dateMatch) {
                filteredAll.add(bill);

                switch (bill.getStatus()) {
                    case "Paid":
                        filteredPaid.add(bill);
                        break;
                    case "Pending":
                        filteredPending.add(bill);
                        break;
                    case "Cancelled":
                        filteredCancelled.add(bill);
                        break;
                }
            }
        }

        // Update tables with filtered data
        allBillsTable.setItems(filteredAll);
        paidBillsTable.setItems(filteredPaid);
        pendingBillsTable.setItems(filteredPending);
        cancelledBillsTable.setItems(filteredCancelled);
    }

    @FXML
    private void handleClearFilter() {
        statusFilterCombo.getSelectionModel().selectFirst();
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);

        // Reset to original data
        allBillsTable.setItems(allBills);
        paidBillsTable.setItems(paidBills);
        pendingBillsTable.setItems(pendingBills);
        cancelledBillsTable.setItems(cancelledBills);
    }

    @FXML
    private void handleCloseDetails() {
        billDetailsPane.setVisible(false);
        billDetailsPane.setManaged(false);
    }

    @FXML
    private void handlePayBill() {
        if (selectedBill == null) {
            return;
        }

        // Only allow payment for pending bills
        if (!"Pending".equals(selectedBill.getStatus())) {
            showAlert(Alert.AlertType.INFORMATION, "Payment Status",
                    "Cannot process payment",
                    "This bill is already " + selectedBill.getStatus().toLowerCase() + ".");
            return;
        }

        // Show payment confirmation dialog
        Alert confirmPayment = new Alert(Alert.AlertType.CONFIRMATION);
        confirmPayment.setTitle("Payment Confirmation");
        confirmPayment.setHeaderText("Confirm Bill Payment");
        confirmPayment.setContentText("Are you sure you want to pay Bill #" + selectedBill.getBillNo() +
                " for $" + String.format("%.2f", selectedBill.getAmount()) + "?");

        confirmPayment.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                processPayment(selectedBill);
            }
        });
    }

    private void processPayment(Bill bill) {
        // In a real application, this would integrate with a payment gateway
        // For simplicity, we'll just update the database directly

        String query = "UPDATE Bill SET payment_status = 'Paid' WHERE bill_no = ?";

        int result = dbConnection.executePreparedUpdate(query, bill.getBillNo());

        if (result > 0) {
            showAlert(Alert.AlertType.INFORMATION, "Payment Successful",
                    "Bill payment was successful",
                    "Bill #" + bill.getBillNo() + " has been marked as paid.");

            // Reload data to refresh the tables
            loadData();

            // Update details view
            selectedBill.setStatus("Paid");
            displayBillDetails(selectedBill);
        } else {
            showAlert(Alert.AlertType.ERROR, "Payment Failed",
                    "Bill payment could not be processed",
                    "Please try again later or contact support.");
        }
    }

    @FXML
    private void handleDownloadReceipt() {
        if (selectedBill == null) {
            return;
        }

        if (!"Paid".equals(selectedBill.getStatus())) {
            showAlert(Alert.AlertType.INFORMATION, "Receipt Download",
                    "Cannot download receipt",
                    "Receipts are only available for paid bills.");
            return;
        }

        // In a real application, this would generate a PDF receipt
        showAlert(Alert.AlertType.INFORMATION, "Receipt Download",
                "Receipt download initiated",
                "Your receipt for Bill #" + selectedBill.getBillNo() + " will be downloaded shortly.");
    }

    private void viewBillDetails(Bill bill) {
        selectedBill = bill;
        displayBillDetails(bill);
    }

    private void displayBillDetails(Bill bill) {
        detailsBillNoLabel.setText(String.valueOf(bill.getBillNo()));
        detailsDateLabel.setText(bill.getDate());
        detailsTimeLabel.setText(bill.getTime());
        detailsAmountLabel.setText(String.format("$%.2f", bill.getAmount()));
        detailsStatusLabel.setText(bill.getStatus());
        detailsAdminLabel.setText(bill.getAdminName());

        // Show/hide pay button based on status
        payBillBtn.setVisible("Pending".equals(bill.getStatus()));
        payBillBtn.setManaged("Pending".equals(bill.getStatus()));

        // Show/hide download receipt button based on status
        downloadReceiptBtn.setVisible("Paid".equals(bill.getStatus()));
        downloadReceiptBtn.setManaged("Paid".equals(bill.getStatus()));

        // Show details pane
        billDetailsPane.setVisible(true);
        billDetailsPane.setManaged(true);
    }

    private void setupAllBillsTable() {
        allBillNoCol.setCellValueFactory(new PropertyValueFactory<>("billNo"));
        allDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        allTimeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        allAmountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        allStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        allAdminCol.setCellValueFactory(new PropertyValueFactory<>("adminName"));

        setupActionsColumn(allActionsCol);
    }

    private void setupPaidBillsTable() {
        paidBillNoCol.setCellValueFactory(new PropertyValueFactory<>("billNo"));
        paidDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        paidTimeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        paidAmountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        paidAdminCol.setCellValueFactory(new PropertyValueFactory<>("adminName"));

        setupActionsColumn(paidActionsCol);
    }

    private void setupPendingBillsTable() {
        pendingBillNoCol.setCellValueFactory(new PropertyValueFactory<>("billNo"));
        pendingDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        pendingTimeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        pendingAmountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        setupActionsColumn(pendingActionsCol);
    }

    private void setupCancelledBillsTable() {
        cancelledBillNoCol.setCellValueFactory(new PropertyValueFactory<>("billNo"));
        cancelledDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        cancelledTimeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        cancelledAmountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        cancelledAdminCol.setCellValueFactory(new PropertyValueFactory<>("adminName"));

        setupActionsColumn(cancelledActionsCol);
    }

    private void setupActionsColumn(TableColumn<Bill, Void> column) {
        Callback<TableColumn<Bill, Void>, TableCell<Bill, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Bill, Void> call(final TableColumn<Bill, Void> param) {
                return new TableCell<>() {
                    private final Button viewBtn = new Button("View Details");
                    private final Button payBtn = new Button("Pay");
                    private final HBox pane = new HBox(5);

                    {
                        viewBtn.setStyle("-fx-background-color: #2d7ae5; -fx-text-fill: white;");
                        payBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

                        viewBtn.setOnAction(event -> {
                            Bill bill = getTableView().getItems().get(getIndex());
                            viewBillDetails(bill);
                        });

                        payBtn.setOnAction(event -> {
                            Bill bill = getTableView().getItems().get(getIndex());
                            if ("Pending".equals(bill.getStatus())) {
                                selectedBill = bill;
                                handlePayBill();
                            }
                        });

                        pane.getChildren().add(viewBtn);
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                        }
                        else {
                            Bill bill = getTableView().getItems().get(getIndex());


                            // Only show pay button for pending bills
                            if ("Pending".equals(bill.getStatus()) && !pane.getChildren().contains(payBtn)) {
                                pane.getChildren().add(payBtn);
                            } else if (!"Pending".equals(bill.getStatus()) && pane.getChildren().contains(payBtn)) {
                                pane.getChildren().remove(payBtn);
                            }
                            setGraphic(pane);
                        }
                    }
                };
            }
        };

        column.setCellFactory(cellFactory);
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}