package com.example.shreyash;

public class Faculty {
    private String name;

    public String getDesignation() {
        return Designation;
    }

    private String Designation;
    private String mobile;
    private String email;


    // Required default constructor for Firebase
    public Faculty() {}

    public Faculty(String name, String Designation, String mobile, String email) {
        this.name = name;
        this.Designation = Designation;
        this.mobile = mobile;
        this.email = email;
    }

    public String getName() {
        return name;
    }


    public String getMobile() {
        return mobile;
    }

    public String getEmail() {
        return email;
    }
}
