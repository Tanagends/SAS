package com.attendance.dao;

import com.attendance.model.Branch;
import com.attendance.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Task 4: Added getAllBranches() method
public class BranchDAO {

    public boolean insertBranch(String branchName) {
        String sql = "INSERT INTO branches (branch_name) VALUES (?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, branchName);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("InsertBranch error: " + e.getMessage());
        }
        return false;
    }

    // Task 4 Fix: Added this missing method
    public List<Branch> getAllBranches() {
        List<Branch> branches = new ArrayList<>();
        String sql = "SELECT branch_id, branch_name FROM branches ORDER BY branch_name";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                branches.add(new Branch(rs.getInt("branch_id"), rs.getString("branch_name")));
            }
        } catch (SQLException e) {
            System.err.println("GetAllBranches error: " + e.getMessage());
        }
        return branches;
    }

    public boolean deleteBranch(int branchId) {
        String sql = "DELETE FROM branches WHERE branch_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DeleteBranch error: " + e.getMessage());
        }
        return false;
    }
}
