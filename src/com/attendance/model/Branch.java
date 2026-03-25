package com.attendance.model;

public class Branch {
    private int branchId;
    private String branchName;

    public Branch() {}
    public Branch(int branchId, String branchName) {
        this.branchId = branchId; this.branchName = branchName;
    }

    public int getBranchId() { return branchId; }
    public void setBranchId(int branchId) { this.branchId = branchId; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    @Override public String toString() { return branchName; }
}
