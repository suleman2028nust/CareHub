package com.example.db_project1;

public class Session {
    private int sessionId;
    private String title;
    private int doctorId;
    private String doctorName;
    private String sessionDateTime;
    private int maxBookings;

    public Session() {
    }

    public Session(int sessionId, String title, int doctorId, String doctorName, String sessionDateTime, int maxBookings) {
        this.sessionId = sessionId;
        this.title = title;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.sessionDateTime = sessionDateTime;
        this.maxBookings = maxBookings;
    }

    public Session(int sessionId, String title, Doctor doctor, String sessionDateTime, int maxBookings) {
        this.sessionId = sessionId;
        this.title = title;
        this.doctorId = doctor.getDoctorId();
        this.doctorName = doctor.getName();
        this.sessionDateTime = sessionDateTime;
        this.maxBookings = maxBookings;
    }

    // Getters
    public int getSessionId() {
        return sessionId;
    }

    public String getTitle() {
        return title;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getSessionDateTime() {
        return sessionDateTime;
    }

    public int getMaxBookings() {
        return maxBookings;
    }

    // Setters
    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public void setSessionDateTime(String sessionDateTime) {
        this.sessionDateTime = sessionDateTime;
    }

    public void setMaxBookings(int maxBookings) {
        this.maxBookings = maxBookings;
    }

    @Override
    public String toString() {
        return "Session{" +
                "sessionId=" + sessionId +
                ", title='" + title + '\'' +
                ", doctorId=" + doctorId +
                ", doctorName='" + doctorName + '\'' +
                ", sessionDateTime='" + sessionDateTime + '\'' +
                ", maxBookings=" + maxBookings +
                '}';
    }
}
