package com.attendance.dao;

import com.attendance.util.DBConnection;

import java.sql.*;
import java.util.*;

public class MarksDAO {

    public boolean enterMarks(String rollNo, String subject, String examType, double marksObtained, double maxMarks, String remarks) {
        String sql = "INSERT INTO marks (student_roll, subject, exam_type, marks_obtained, max_marks, remarks) VALUES (?,?,?,?,?,?) " +
                     "ON DUPLICATE KEY UPDATE marks_obtained=?, max_marks=?, remarks=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNo);
            ps.setString(2, subject);
            ps.setString(3, examType);
            ps.setDouble(4, marksObtained);
            ps.setDouble(5, maxMarks);
            ps.setString(6, remarks);
            ps.setDouble(7, marksObtained);
            ps.setDouble(8, maxMarks);
            ps.setString(9, remarks);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("EnterMarks error: " + e.getMessage());
        }
        return false;
    }

    public List<Map<String, Object>> getStudentMarks(String rollNo) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT subject, exam_type, marks_obtained, max_marks, remarks FROM marks WHERE student_roll=? ORDER BY subject, exam_type";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNo);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("subject", rs.getString("subject"));
                row.put("exam_type", rs.getString("exam_type"));
                row.put("marks_obtained", rs.getDouble("marks_obtained"));
                row.put("max_marks", rs.getDouble("max_marks"));
                row.put("remarks", rs.getString("remarks"));
                list.add(row);
            }
        } catch (SQLException e) {
            System.err.println("GetStudentMarks error: " + e.getMessage());
        }
        return list;
    }

    public List<Map<String, Object>> getMarksBySubject(String subject, String examType) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT m.student_roll, s.student_name, m.marks_obtained, m.max_marks, m.remarks FROM marks m " +
                     "JOIN students s ON m.student_roll=s.roll_no WHERE m.subject=? AND m.exam_type=? ORDER BY s.roll_no";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, subject);
            ps.setString(2, examType);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("roll", rs.getString("student_roll"));
                row.put("name", rs.getString("student_name"));
                row.put("marks_obtained", rs.getDouble("marks_obtained"));
                row.put("max_marks", rs.getDouble("max_marks"));
                row.put("remarks", rs.getString("remarks"));
                list.add(row);
            }
        } catch (SQLException e) {
            System.err.println("GetMarksBySubject error: " + e.getMessage());
        }
        return list;
    }
}
