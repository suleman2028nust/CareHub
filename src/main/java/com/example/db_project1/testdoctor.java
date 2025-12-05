package com.example.db_project1;
public class testdoctor {
    private Integer doctorId;
    private String name;
    private String specialization;

    public testdoctor(Integer doctorId, String name, String specialization) {
        this.doctorId = doctorId;
        this.name = name;
        this.specialization = specialization;
    }

    public Integer getDoctorId() { return doctorId; }
    public String getName() { return name; }
    public String getSpecialization() { return specialization; }
}
