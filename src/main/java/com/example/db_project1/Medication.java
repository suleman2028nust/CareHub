package com.example.db_project1;

public class Medication {
    private int medicationId;
    private String name;
    private String dosage;
    private String frequency;
    private String duration;

    public Medication(int medicationId, String name, String dosage, String frequency, String duration) {
        this.medicationId = medicationId;
        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
    }

    // Getters and setters
    public int getMedicationId() {
        return medicationId;
    }

    public void setMedicationId(int medicationId) {
        this.medicationId = medicationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return name + " - " + dosage;
    }
}