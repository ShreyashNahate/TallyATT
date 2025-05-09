package com.example.shreyash;

public class AttendanceRecord {

    private String enrollmentNumber;
    private String status;
    private String timestamp;



    private String name;

    private String subject;
    public String getSubject() {
        return subject;
    }



    public AttendanceRecord(String enrollmentNumber, String status, String timestamp,String selectedSubject ,String name) {
        this.enrollmentNumber=enrollmentNumber;
        this.status=status;
        this.timestamp=timestamp;
        this.subject = selectedSubject;
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public String getEnrollmentNumber() {
        return enrollmentNumber;
    }

    public String getStatus() {
        return status;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
