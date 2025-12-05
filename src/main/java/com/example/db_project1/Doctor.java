package com.example.db_project1;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Doctor {
    private int doctorId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String specialization;
    private int experience;
    private String language;
    private String email;
    private String phoneNo;
    private String schedule;
    private Image picture;
    private String password;
    private  String gender;
    private Date dob;

    public Doctor(int id, String name, String specialization, int experience, String language, String email, String phoneNo) {
        this.doctorId = id;
        String[] parts = name.split(" ");
        if (parts.length == 1) {
            this.firstName = parts[0];
            this.middleName = "";
            this.lastName = "";
        } else if (parts.length == 2) {
            this.firstName = parts[0];
            this.middleName = "";
            this.lastName = parts[1];  // <-- Fixed: assign lastName here
        } else {
            this.firstName = parts[0];
            this.lastName = parts[parts.length - 1];
            this.middleName = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length - 1));
        }
        this.specialization = specialization;
        this.experience = experience;
        this.language = language;
        this.email = email;
        this.phoneNo = phoneNo;
        this.schedule = "00:00-00:00";
        this.picture = null;
        this.password = "";
        this.gender = "";
    }

    public Date getDob() {
        return dob;
    }
    public void setDob(Date dob) {
        this.dob = dob;
    }
    public String getPassword(){
        return password;
    }
    public void setPassword(String password){
        this.password = password;
    }


    // Database connection info - adjust for your setup
    private static final String DB_URL = "jdbc:mysql://localhost:3306/hms";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "mysql2024";


    // Constructor including picture
    public Doctor(int doctorId, String firstName, String middleName, String lastName,
                  String specialization, int experience, String language,
                  String email, String phoneNo, String schedule, Image picture) {
        this.doctorId = doctorId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.specialization = specialization;
        this.experience = experience;
        this.language = language;
        this.email = email;
        this.phoneNo = phoneNo;
        this.schedule = schedule;
        this.picture = picture;
    }


    public Doctor() {
    }

    public Doctor(int doctorId, String name) {
        this.doctorId = doctorId;
        String[] parts = name.split(" ");
        if (parts.length == 1) {
            this.firstName = parts[0];
            this.middleName = "";
            this.lastName = "";
        } else if (parts.length == 2) {
            this.firstName = parts[0];
            this.middleName = "";
        }
    }

    // Getters and setters

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
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

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public Integer getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public Image getPicture() {
        return picture;
    }

    public void setPicture(Image picture) {
        this.picture = picture;
    }

    // Convenience method to get full name
    public String getName() {
        StringBuilder fullName = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) fullName.append(firstName).append(" ");
        if (middleName != null && !middleName.isEmpty()) fullName.append(middleName).append(" ");
        if (lastName != null && !lastName.isEmpty()) fullName.append(lastName);
        return fullName.toString().trim();
    }
    public void setName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            this.firstName = "";
            this.middleName = "";
            this.lastName = "";
            return;
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            this.firstName = parts[0];
            this.middleName = "";
            this.lastName = "";
        } else if (parts.length == 2) {
            this.firstName = parts[0];
            this.middleName = "";
            this.lastName = parts[1];
        } else {
            this.firstName = parts[0];
            this.lastName = parts[parts.length - 1];
            this.middleName = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length - 1));
        }
    }

    @Override
    public String toString() {
        return getName() + " (" + specialization + ")";
    }

    // =======================
    // Static method to get all doctors from DB, including picture
    public static List<Doctor> getAllDoctors() {
        List<Doctor> doctors = new ArrayList<>();

        String query = "SELECT doctor_id, first_name, middle_name, last_name, specialization,dob, experience, language, email, phoneNo, schedule, picture FROM doctor";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Doctor doctor = new Doctor();
                doctor.setDoctorId(rs.getInt("doctor_id")); // Correct column name from DB
                doctor.setFirstName(rs.getString("first_name"));
                doctor.setMiddleName(rs.getString("middle_name"));
                doctor.setLastName(rs.getString("last_name"));
                doctor.setSpecialization(rs.getString("specialization"));
                doctor.setDob(rs.getDate("dob"));
                doctor.setExperience(rs.getInt("experience"));
                doctor.setLanguage(rs.getString("language"));
                doctor.setEmail(rs.getString("email"));
                doctor.setPhoneNo(rs.getString("phoneNo")); // Check exact DB column name casing
                doctor.setSchedule(rs.getString("schedule"));

                // Read picture blob and convert to Image
                Blob blob = rs.getBlob("picture");
                if (blob != null) {
                    try (InputStream is = blob.getBinaryStream()) {
                        Image img = new Image(is);
                        doctor.setPicture(img);
                    } catch (Exception e) {
                        e.printStackTrace();
                        doctor.setPicture(null);
                    }
                } else {
                    doctor.setPicture(null);
                }

                doctors.add(doctor);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exceptions properly
        }

        return doctors;
    }

    // Instance method to save this doctor to DB (insert or update), including picture
    public boolean save() {
        Connection conn = null;
        CallableStatement cstmt = null;

        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            if (this.doctorId == 0) {
                // Call stored procedure for INSERT
                String sql = "{CALL AddDoctor(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
                cstmt = conn.prepareCall(sql);

                cstmt.setString(1, firstName);
                cstmt.setString(2, middleName != null && !middleName.isEmpty() ? middleName : null);
                cstmt.setString(3, lastName);
                cstmt.setString(4, specialization);
                cstmt.setDate(5, dob);
                cstmt.setInt(6, experience);
                cstmt.setString(7, language);
                cstmt.setString(8, schedule);
                cstmt.setString(9, phoneNo);
                cstmt.setString(10, email);
                cstmt.setString(11, password);  // Remember to hash passwords before saving!
                if (picture != null) {
                    cstmt.setBlob(12, imageToInputStream(picture));
                } else {
                    cstmt.setNull(12, Types.BLOB);
                }
                cstmt.setString(13, gender != null && !gender.isEmpty() ? gender : null);

                int affectedRows = cstmt.executeUpdate();

                // Optionally, you can retrieve the generated doctor_id if your procedure supports it
                // For now, just return success based on affected rows
                return affectedRows > 0;

            } else {
                // Call stored procedure for UPDATE (assuming you have one)
                // If you don't have an update stored procedure, you can keep your existing update logic here
                String sql = "{CALL UpdateDoctor(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
                cstmt = conn.prepareCall(sql);

                cstmt.setInt(1, doctorId);
                cstmt.setString(2, firstName);
                cstmt.setString(3, middleName != null && !middleName.isEmpty() ? middleName : null);
                cstmt.setString(4, lastName);
                cstmt.setString(5, specialization);
                cstmt.setDate(6, dob);
                cstmt.setInt(7, experience);
                cstmt.setString(8, language);
                cstmt.setString(9, schedule);
                cstmt.setString(10, phoneNo);
                cstmt.setString(11, email);
                cstmt.setString(12, password);
                if (picture != null) {
                    cstmt.setBlob(13, imageToInputStream(picture));
                } else {
                    cstmt.setNull(13, Types.BLOB);
                }
                cstmt.setString(14, gender != null && !gender.isEmpty() ? gender : null);

                int affectedRows = cstmt.executeUpdate();

                return affectedRows > 0;
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (cstmt != null) cstmt.close(); } catch (Exception ex) {}
            try { if (conn != null) conn.close(); } catch (Exception ex) {}
        }
    }

    // Utility method to convert JavaFX Image to InputStream for DB blob
    private InputStream imageToInputStream(Image image) throws IOException {
        if (image == null) return null;

        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(bImage, "png", bos);
        return new ByteArrayInputStream(bos.toByteArray());
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGender() {
        return gender;
    }
}
