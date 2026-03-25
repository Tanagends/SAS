package com.attendance.dao;

import com.attendance.model.User;
import com.attendance.util.DBConnection;
import com.attendance.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FacultyDAO — manages faculty user accounts.
 * Faculty are stored in the 'users' table with role='FACULTY'.
 */
public class FacultyDAO {

    /** Returns all users with role FACULTY. */
    public List<User> getAllFaculty() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT user_id, username, role, email, phone FROM users WHERE role='FACULTY' ORDER BY username";
        try (Connection conn = DBConnection.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setUsername(rs.getString("username"));
                u.setRole(rs.getString("role"));
                u.setEmail(rs.getString("email") != null ? rs.getString("email") : "");
                u.setPhone(rs.getString("phone") != null ? rs.getString("phone") : "");
                list.add(u);
            }
        } catch (SQLException e) {
            System.err.println("GetAllFaculty error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Creates a new faculty user account.
     * Returns the generated user_id, or -1 on failure.
     */
    public int createFaculty(String username, String password, String email, String phone) {
        String sql = "INSERT INTO users (username, password, role, email, phone) VALUES (?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hashPassword(password));
            ps.setString(3, "FACULTY");
            ps.setString(4, email);
            ps.setString(5, phone);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("CreateFaculty error: " + e.getMessage());
        }
        return -1;
    }

    /** Resets a faculty member's password. Returns true on success. */
    public boolean resetPassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password=? WHERE user_id=? AND role='FACULTY'";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, PasswordUtil.hashPassword(newPassword));
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ResetFacultyPassword error: " + e.getMessage());
        }
        return false;
    }

    /** Deletes a faculty user account. Returns true on success. */
    public boolean deleteFaculty(int userId) {
        String sql = "DELETE FROM users WHERE user_id=? AND role='FACULTY'";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DeleteFaculty error: " + e.getMessage());
        }
        return false;
    }

    /** Checks if a username already exists (to prevent duplicates). */
    public boolean isUsernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username=?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            System.err.println("IsUsernameExists error: " + e.getMessage());
        }
        return false;
    }
}
