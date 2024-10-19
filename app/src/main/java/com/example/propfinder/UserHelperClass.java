package com.example.propfinder;

public class UserHelperClass {
    private String fullName;
    private String email;
    private String address;
    private String password;
    private String gender;
    private String birthDate;
    private String phone;
    private String imageUrl;

    // Constructor
    public UserHelperClass(String fullName, String email, String address, String password, String gender, String birthDate, String phone, String imageUrl) {
        this.fullName = fullName;
        this.email = email;
        this.address = address;
        this.password = password;
        this.gender = gender;
        this.birthDate = birthDate;
        this.phone = phone;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters (optional, depending on how you want to access the fields)
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
