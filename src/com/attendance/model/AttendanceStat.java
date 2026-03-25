package com.attendance.model;

public class AttendanceStat {
    private String subject;
    private int attended;
    private int totalClasses;
    private double percentage;

    public AttendanceStat() {}

    public AttendanceStat(String subject, int attended, int totalClasses, double percentage) {
        this.subject = subject;
        this.attended = attended;
        this.totalClasses = totalClasses;
        this.percentage = percentage;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getAttended() {
        return attended;
    }

    public void setAttended(int attended) {
        this.attended = attended;
    }

    public int getTotalClasses() {
        return totalClasses;
    }

    public void setTotalClasses(int totalClasses) {
        this.totalClasses = totalClasses;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}