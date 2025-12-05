package com.example.db_project1;

import java.time.LocalDate;

public class Patient {
    private String password;
    private int patientId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String name;
    private LocalDate dob;
    private String gender;
    private String bloodGroup;
    private String insuranceType;
    private String email;
    private String address;
    private String phoneNo;

    public Patient(int patientId, String firstName, String middleName, String lastName,
                   String name, LocalDate dob, String gender, String bloodGroup,
                   String insuranceType, String email, String address, String phoneNo) {
        this.patientId = patientId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.name = name;
        this.dob = dob;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
        this.insuranceType = insuranceType;
        this.email = email;
        this.address = address;
        this.phoneNo = phoneNo;
    }


    ///  ///////////////////////////////////////////////////////
    public Patient() {

    }
    /// /////////////////////////////////////////////////////////

    // Getters and setters
    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getInsuranceType() {
        return insuranceType;
    }

    public void setInsuranceType(String insuranceType) {
        this.insuranceType = insuranceType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    @Override
    public String toString() {
        return name;
    }



    public void setPassword(String password) {
        this.password=password;
    }
    /// /////////////////////////////////////////////
}