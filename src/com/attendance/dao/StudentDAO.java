package com.attendance.dao;

import com.attendance.model.Student;
import com.attendance.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Task 11 & 14: Removed all mock/hardcoded data
public class StudentDAO {

    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT s.*, b.branch_name FROM students s LEFT JOIN branches b ON s.branch_id=b.branch_id ORDER BY s.roll_no";
        try (Connection conn = DBConnection.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapStudent(rs));
            }
        } catch (SQLException e) {
            System.err.println("GetAllStudents error: " + e.getMessage());
        }
        return list;
    }

    // Task 11 Fix: No mock data fallback
    public List<Student> getStudentsBySection(String section) {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT s.*, b.branch_name FROM students s LEFT JOIN branches b ON s.branch_id=b.branch_id WHERE s.section=? ORDER BY s.roll_no";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, section);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapStudent(rs));
            }
        } catch (SQLException e) {
            System.err.println("GetStudentsBySection error: " + e.getMessage());
        }
        return list;
    }

    public List<Student> getStudentsBySectionAndYear(String section, int year) {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT s.*, b.branch_name FROM students s LEFT JOIN branches b ON s.branch_id=b.branch_id WHERE s.section=? AND s.year=? ORDER BY s.roll_no";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, section);
            ps.setInt(2, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapStudent(rs));
            }
        } catch (SQLException e) {
            System.err.println("GetStudentsBySectionAndYear error: " + e.getMessage());
        }
        return list;
    }

    // Task 14 Fix: No hardcoded fallback "101"
    public String getRollNumberByUsername(String username) {
        String sql = "SELECT s.roll_no FROM students s JOIN users u ON s.user_id=u.user_id WHERE u.username=?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getString("roll_no");
        } catch (SQLException e) {
            System.err.println("GetRollNumberByUsername error: " + e.getMessage());
        }
        return null;
    }

    public Student getStudentByUsername(String username) {
        String sql = "SELECT s.*, b.branch_name FROM students s JOIN users u ON s.user_id=u.user_id LEFT JOIN branches b ON s.branch_id=b.branch_id WHERE u.username=?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return mapStudent(rs);
        } catch (SQLException e) {
            System.err.println("GetStudentByUsername error: " + e.getMessage());
        }
        return null;
    }

    // Task 7 Fix: Validate duplicate roll numbers before inserting
    public boolean isRollNumberExists(String rollNo) {
        String sql = "SELECT COUNT(*) FROM students WHERE roll_no=?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNo);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("IsRollExists error: " + e.getMessage());
        }
        return false;
    }

    public boolean insertStudent(int userId, String name, String rollNo, int branchId, int year, String section) {
        String sql = "INSERT INTO students (user_id, student_name, roll_no, branch_id, year, section) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, name);
            ps.setString(3, rollNo);
            ps.setInt(4, branchId);
            ps.setInt(5, year);
            ps.setString(6, section);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("InsertStudent error: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteStudent(String rollNo) {
        String sql = "DELETE FROM students WHERE roll_no=?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DeleteStudent error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Updates student name, year, and section (roll_no and branch are immutable
     * keys).
     */
    public boolean updateStudent(String rollNo, String name, int year, String section) {
        String sql = "UPDATE students SET student_name=?, year=?, section=? WHERE roll_no=?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, year);
            ps.setString(3, section);
            ps.setString(4, rollNo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("UpdateStudent error: " + e.getMessage());
        }
        return false;
    }

    /** Resets a student's login password (SHA-256 hashed). */
    public boolean resetStudentPassword(String rollNo, String newPassword) {
        String hashedPw = com.attendance.util.PasswordUtil.hashPassword(newPassword);
        String sql = "UPDATE users u JOIN students s ON u.user_id=s.user_id SET u.password=? WHERE s.roll_no=?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPw);
            ps.setString(2, rollNo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ResetStudentPassword error: " + e.getMessage());
        }
        return false;
    }

    public int getTotalStudents() {
        String sql = "SELECT COUNT(*) FROM students";
        try (Connection conn = DBConnection.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("GetTotalStudents error: " + e.getMessage());
        }
        return 0;
    }

    public List<String> getAllSections() {
        List<String> sections = new ArrayList<>();
        String sql = "SELECT DISTINCT section FROM students ORDER BY section";
        try (Connection conn = DBConnection.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                sections.add(rs.getString("section"));
        } catch (SQLException e) {
            System.err.println("GetAllSections error: " + e.getMessage());
        }
        return sections;
    }

    /**
     * Looks up a single Student by roll number — used by StudentDashboard greeting.
     */
    public Student getStudentByRollNo(String rollNo) {
        String sql = "SELECT s.*, b.branch_name FROM students s " +
                "LEFT JOIN branches b ON s.branch_id=b.branch_id " +
                "WHERE s.roll_no=? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNo);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return mapStudent(rs);
        } catch (SQLException e) {
            System.err.println("GetStudentByRollNo error: " + e.getMessage());
        }
        return null;
    }

    private Student mapStudent(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setStudentId(rs.getInt("student_id"));
        s.setUserId(rs.getInt("user_id"));
        s.setStudentName(rs.getString("student_name"));
        s.setRollNo(rs.getString("roll_no"));
        s.setBranchId(rs.getInt("branch_id"));
        s.setYear(rs.getInt("year"));
        s.setSection(rs.getString("section"));
        try {
            s.setBranchName(rs.getString("branch_name"));
        } catch (SQLException ignored) {
        }
        return s;
    }
}
