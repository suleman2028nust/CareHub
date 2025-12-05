package com.example.db_project1;

/**
 * This class represents an appointment with additional patient information for display in the doctor's appointment table
 */
public class AppointmentWithPatient {
    private int appointId;
    private int patientId;
    private int doctorId;
    private String patientName;
    private String date;
    private String time;
    private String specialization;
    private String status;
    private double consultationFee;
    private String type;

    public AppointmentWithPatient(int appointId, int patientId, int doctorId, String patientName,
                                  String date, String time, String specialization, String status,
                                  double consultationFee, String type) {
        this.appointId = appointId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.patientName = patientName;
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

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
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
        return "Appointment #" + appointId + " - " + patientName + " on " + date + " at " + time;
    }
}