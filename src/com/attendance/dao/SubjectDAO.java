package com.attendance.dao;

import com.attendance.model.Subject;
import com.attendance.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Task 5: Implemented getAllSubjects() method
public class SubjectDAO {

    // Task 5 Fix: Full implementation
    public List<Subject> getAllSubjects() {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT subject_id, subject_name FROM subjects ORDER BY subject_name";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                subjects.add(new Subject(rs.getInt("subject_id"), rs.getString("subject_name")));
            }
        } catch (SQLException e) {
            System.err.println("GetAllSubjects error: " + e.getMessage());
        }
        return subjects;
    }

    public boolean insertSubject(String subjectName) {
        String sql = "INSERT INTO subjects (subject_name) VALUES (?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, subjectName);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("InsertSubject error: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteSubject(int subjectId) {
        String sql = "DELETE FROM subjects WHERE subject_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DeleteSubject error: " + e.getMessage());
        }
        return false;
    }
}
