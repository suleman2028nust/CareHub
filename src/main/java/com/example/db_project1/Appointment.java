package com.example.db_project1;

public class Appointment {
    private int appointId;
    private int patientId;
    private int doctorId;
    private String doctorName;
    private String date;
    private String time;
    private String specialization;
    private String status;
    private double consultationFee;
    private String type;

    // Constructor for creating an appointment
    public Appointment(int patientId, int doctorId, String date, String time,
                       String specialization, double consultationFee, String type) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.date = date;
        this.time = time;
        this.specialization = specialization;
        this.consultationFee = consultationFee;
        this.type = type;
        this.status = "Scheduled";
    }

    // Constructor for loading from database with doctor name
    public Appointment(int appointId, int patientId, String doctorName, String date,
                       String time, String specialization, String status) {
        this.appointId = appointId;
        this.patientId = patientId;
        this.doctorName = doctorName;
        this.date = date;
        this.time = time;
        this.specialization = specialization;
        this.status = status;
    }

    // Constructor for loading from database with all details
    public Appointment(int appointId, int patientId, int doctorId, String doctorName,
                       String date, String time, String specialization, String status,
                       double consultationFee, String type) {
        this.appointId = appointId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.date = date;
        this.time = time;
        this.specialization = specialization;
        this.status = status;
        this.consultationFee = consultationFee;
        this.type = type;
    }

    // Getters and setters
    public int getAppointId() {
        return appointId;
    }

    public void setAppointId(int appointId) {
        this.appointId = appointId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
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

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(double consultationFee) {
        this.consultationFee = consultationFee;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Appointment #" + appointId + " - " + doctorName + " on " + date + " at " + time;
    }
}