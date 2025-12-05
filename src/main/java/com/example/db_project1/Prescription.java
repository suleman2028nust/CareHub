package com.example.db_project1;

public class Prescription {
    private int prescriptionId;
    private int patientId;
    private int doctorId;
    private String doctorName;
    private String specialization;
    private String dateIssued;
    private String diagnosis;
    private String remarks;

    public Prescription(int prescriptionId, int patientId, int doctorId, String doctorName,
                        String specialization, String dateIssued, String diagnosis, String remarks) {
        this.prescriptionId = prescriptionId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.specialization = specialization;
        this.dateIssued = dateIssued;
        this.diagnosis = diagnosis;
        this.remarks = remarks;
    }

    // Getters and setters
    public int getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(int prescriptionId) {
        this.prescriptionId = prescriptionId;
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

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getDateIssued() {
        return dateIssued;
    }

    public void setDateIssued(String dateIssued) {
        this.dateIssued = dateIssued;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        return "Prescription #" + prescriptionId + " by Dr. " + doctorName + " on " + dateIssued;
    }
}