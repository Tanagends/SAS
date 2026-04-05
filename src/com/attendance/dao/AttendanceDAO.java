package com.attendance.dao;

import com.attendance.model.AttendanceStat;
import com.attendance.util.DBConnection;

import java.sql.*;
import java.util.*;

// Task 13: Removed mock data from getAttendanceStats() and getStudentAttendance()
public class AttendanceDAO {

    public boolean markAttendance(String rollNo, String subject, String date, String status) {
        // Use DB-specific upsert syntax: SQLite uses ON CONFLICT(...) DO UPDATE, MySQL uses ON DUPLICATE KEY UPDATE
        try (Connection conn = DBConnection.getConnection()) {
            String product = "";
            try {
                product = conn.getMetaData().getDatabaseProductName().toLowerCase();
            } catch (SQLException ignored) {}

            String sql;
            boolean isSqlite = product.contains("sqlite");
            if (isSqlite) {
                sql = "INSERT INTO attendance (student_roll, subject, date, status) VALUES (?,?,?,?) " +
                        "ON CONFLICT(student_roll,subject,date) DO UPDATE SET status=excluded.status";
            } else {
                sql = "INSERT INTO attendance (student_roll, subject, date, status) VALUES (?,?,?,?) " +
                        "ON DUPLICATE KEY UPDATE status=?";
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, rollNo);
                ps.setString(2, subject);
                ps.setString(3, date);
                ps.setString(4, status);
                if (!isSqlite) ps.setString(5, status);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("MarkAttendance error: " + e.getMessage());
        }
        return false;
    }

    // Task 13 Fix: No mock data fallback
    public List<AttendanceStat> getAttendanceStats(String rollNo) {
        List<AttendanceStat> stats = new ArrayList<>();
        String sql = "SELECT subject, COUNT(*) as total, " +
                "SUM(CASE WHEN status='PRESENT' THEN 1 ELSE 0 END) as attended " +
                "FROM attendance WHERE student_roll=? GROUP BY subject";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNo);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int total = rs.getInt("total");
                int attended = rs.getInt("attended");
                double pct = total > 0 ? (attended * 100.0 / total) : 0;
                stats.add(new AttendanceStat(rs.getString("subject"), total, attended, pct));
            }
        } catch (SQLException e) {
            System.err.println("GetAttendanceStats error: " + e.getMessage());
        }
        return stats;
    }

    // Task 13 Fix: No mock data for rollNo "101"
    public List<Map<String, String>> getStudentAttendance(String rollNo) {
        List<Map<String, String>> records = new ArrayList<>();
        String sql = "SELECT subject, date, status FROM attendance WHERE student_roll=? ORDER BY date DESC";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNo);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, String> row = new LinkedHashMap<>();
                row.put("subject", rs.getString("subject"));
                row.put("date", rs.getString("date"));
                row.put("status", rs.getString("status"));
                records.add(row);
            }
        } catch (SQLException e) {
            System.err.println("GetStudentAttendance error: " + e.getMessage());
        }
        return records;
    }

    public List<Map<String, String>> getAttendanceBySubjectAndDate(String subject, String date) {
        List<Map<String, String>> records = new ArrayList<>();

        // CRITICAL DEMO FIX:
        // The old query only returned rows that ALREADY exist in `attendance`.
        // If attendance hasn't been marked for this subject+date yet, UI shows an empty table.
        // For Edit Attendance we must always show all students, with a default status.
        String sql = "SELECT s.roll_no AS student_roll, s.student_name, " +
                "COALESCE(a.status, 'ABSENT') AS status " +
                "FROM students s " +
                "LEFT JOIN attendance a " +
                "  ON a.student_roll = s.roll_no AND a.subject = ? AND a.date = ? " +
                "ORDER BY s.roll_no";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, subject);
            ps.setString(2, date);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, String> row = new LinkedHashMap<>();
                row.put("roll", rs.getString("student_roll"));
                row.put("name", rs.getString("student_name"));
                row.put("status", rs.getString("status"));
                records.add(row);
            }
        } catch (SQLException e) {
            System.err.println("GetAttendanceBySubjectAndDate error: " + e.getMessage());
        }
        return records;
    }

    public int getTotalPresentToday() {
        // Use SQLite-compatible current date. DATE('now') returns YYYY-MM-DD which matches stored date strings.
        String sql = "SELECT COUNT(*) FROM attendance WHERE date=DATE('now') AND status='PRESENT'";
        try (Connection conn = DBConnection.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("GetTotalPresentToday error: " + e.getMessage());
        }
        return 0;
    }

    // Report: Overall attendance percentage across all students
    public double getOverallAttendancePercentage() {
        String sql = "SELECT COUNT(*) as total, SUM(CASE WHEN status='PRESENT' THEN 1 ELSE 0 END) as present FROM attendance";
        try (Connection conn = DBConnection.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                int total = rs.getInt("total");
                int present = rs.getInt("present");
                return total > 0 ? (present * 100.0 / total) : 0;
            }
        } catch (SQLException e) {
            System.err.println("GetOverallAttendance error: " + e.getMessage());
        }
        return 0;
    }

    // Report: Students with low attendance (< threshold%), optionally filtered by
    // section
    public List<Map<String, Object>> getLowAttendanceStudents(double threshold) {
        return getLowAttendanceStudents(threshold, null);
    }

    public List<Map<String, Object>> getLowAttendanceStudents(double threshold, String section) {
        List<Map<String, Object>> list = new ArrayList<>();
        boolean filterSection = (section != null && !section.isBlank() && !section.equals("All Sections"));
        String sql = "SELECT a.student_roll, s.student_name, s.section, COUNT(*) as total, " +
                "SUM(CASE WHEN a.status='PRESENT' THEN 1 ELSE 0 END) as present " +
                "FROM attendance a JOIN students s ON a.student_roll=s.roll_no " +
                (filterSection ? "WHERE s.section=? " : "") +
                "GROUP BY a.student_roll, s.student_name, s.section " +
                "HAVING (present*100.0/total) < ? ORDER BY (present*100.0/total)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            if (filterSection) {
                ps.setString(1, section);
                ps.setDouble(2, threshold);
            } else {
                ps.setDouble(1, threshold);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("roll", rs.getString("student_roll"));
                row.put("name", rs.getString("student_name"));
                row.put("section", rs.getString("section"));
                int total = rs.getInt("total");
                int present = rs.getInt("present");
                row.put("total", total);
                row.put("present", present);
                row.put("percentage", total > 0 ? String.format("%.1f", present * 100.0 / total) : "0");
                list.add(row);
            }
        } catch (SQLException e) {
            System.err.println("GetLowAttendance error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Report helper: returns overall attendance summary per student.
     * Columns provided in each map: roll, name, section, present, total, percentage.
     * 
     * Includes students with 0 attendance rows (present=0,total=0,percentage=0.0).
     */
    public List<Map<String, Object>> getStudentAttendanceSummary(String section) {
        List<Map<String, Object>> list = new ArrayList<>();
        boolean filterSection = (section != null && !section.isBlank() && !section.equals("All Sections"));

        String sql = "SELECT s.roll_no AS roll, s.student_name AS name, s.section AS section, " +
                "COUNT(a.attendance_id) AS total, " +
                "SUM(CASE WHEN a.status='PRESENT' THEN 1 ELSE 0 END) AS present " +
                "FROM students s " +
                "LEFT JOIN attendance a ON a.student_roll = s.roll_no " +
                (filterSection ? "WHERE s.section=? " : "") +
                "GROUP BY s.roll_no, s.student_name, s.section " +
                "ORDER BY s.roll_no";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            if (filterSection) {
                ps.setString(1, section);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int total = rs.getInt("total");
                int present = rs.getInt("present");
                double pct = total > 0 ? (present * 100.0 / total) : 0.0;

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("roll", rs.getString("roll"));
                row.put("name", rs.getString("name"));
                row.put("section", rs.getString("section"));
                row.put("present", present);
                row.put("total", total);
                row.put("percentage", String.format("%.1f", pct));
                list.add(row);
            }
        } catch (SQLException e) {
            System.err.println("getStudentAttendanceSummary error: " + e.getMessage());
        }
        return list;
    }
}
