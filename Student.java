package com.example.shreyash;

import java.util.Map;

public class Student {
    private String name;
    private String enrollment;
    private String mobile;
    private String email;


    // Required default constructor for Firebase
    public Student() {}

    public Student(String name, String enrollment, String mobile, String email) {
        this.name = name;
        this.enrollment = enrollment;
        this.mobile = mobile;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEnrollment() {
        return enrollment;
    }

    public String getMobile() {
        return mobile;
    }

    public String getEmail() {
        return email;
    }
}
