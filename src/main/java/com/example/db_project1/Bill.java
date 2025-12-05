package com.example.db_project1;

public class Bill {
    private int billNo;
    private int patientId;
    private String date;
    private String time;
    private double amount;
    private String status;
    private String adminName;

    public Bill(int billNo, int patientId, String date, String time, double amount, String status, String adminName) {
        this.billNo = billNo;
        this.patientId = patientId;
        this.date = date;
        this.time = time;
        this.amount = amount;
        this.status = status;
        this.adminName = adminName;
    }

    // Getters and setters
    public int getBillNo() {
        return billNo;
    }

    public void setBillNo(int billNo) {
        this.billNo = billNo;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    @Override
    public String toString() {
        return "Bill #" + billNo + " - $" + amount + " (" + status + ")";
    }
}