package com.example.db_project1;

public class DoctorAppointment {
    private int appointId;
    private int patientId;
    private String patientName;
    private int doctorId;
    private String doctorName; // ✅ ADD THIS
    private String date;
    private String time;
    private String type;
    private String status;

    public DoctorAppointment(int appointId, int patientId, String patientName, int doctorId,
                             String doctorName, String date, String time, String type, String status) {
        this.appointId = appointId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorName = doctorName; // ✅ SET IT
        this.date = date;
        this.time = time;
        this.type = type;
        this.status = status;
    }

    // ✅ Getter and Setter for doctorName
    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    // Existing getters/setters below...

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

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Appointment #" + appointId + " - Patient: " + patientName + " on " + date + " at " + time;
    }

    public String getReason() {
        return "Reason: " + status;
    }
}
