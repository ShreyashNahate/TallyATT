package com.example.shreyash;

public class AttendanceRecord2 {
    private String enrollmentNumber;
    private String name;
    private String subject;
    private int presentCount;
    private int absentCount;
    private int totalLecture;

    public int getPercentage() {
        return percentage;
    }

    public int getTotalLecture() {
        return totalLecture;
    }

    private int percentage;

    public AttendanceRecord2(String enrollmentNumber, String name, String subject, int presentCount, int absentCount,int totalLecture,int percentage) {
        this.enrollmentNumber = enrollmentNumber;
        this.name = name;
        this.subject = subject;
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.percentage=percentage;
        this.totalLecture=totalLecture;
    }

    public String getEnrollmentNumber() { return enrollmentNumber; }
    public String getName() { return name; }
    public String getSubject() { return subject; }
    public int getPresentCount() { return presentCount; }
    public int getAbsentCount() { return absentCount; }
}
