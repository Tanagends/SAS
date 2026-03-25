package com.attendance.model;

public class Student {
    private int studentId;
    private int userId;
    private String studentName;
    private String rollNo;
    private int branchId;
    private String branchName;
    private int year;
    private String section;

    public Student() {}

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }
    public int getBranchId() { return branchId; }
    public void setBranchId(int branchId) { this.branchId = branchId; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
}
