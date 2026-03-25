package com.attendance.dao;

import com.attendance.model.User;
import com.attendance.util.DBConnection;
import com.attendance.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Task 12: Security fix - passwords hashed with SHA-256, no hardcoded fallback credentials
public class UserDAO {

    public User login(String username, String password) {
        String hashedPassword = PasswordUtil.hashPassword(password);
        String sql = "SELECT user_id, username, role, email, phone FROM users WHERE username=? AND password=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hashedPassword);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
        }
        return null; // No hardcoded fallback - Task 12 fix
    }

    public boolean updateProfile(int userId, String email, String phone) {
        String sql = "UPDATE users SET email=?, phone=? WHERE user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, phone);
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Update profile error: " + e.getMessage());
        }
        return false;
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        String hashedOld = PasswordUtil.hashPassword(oldPassword);
        String sql = "SELECT user_id FROM users WHERE user_id=? AND password=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, hashedOld);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return false;

            String updateSql = "UPDATE users SET password=? WHERE user_id=?";
            try (PreparedStatement ps2 = conn.prepareStatement(updateSql)) {
                ps2.setString(1, PasswordUtil.hashPassword(newPassword));
                ps2.setInt(2, userId);
                return ps2.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Change password error: " + e.getMessage());
        }
        return false;
    }

    public User getUserById(int userId) {
        String sql = "SELECT user_id, username, role, email, phone FROM users WHERE user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role"));
                user.setEmail(rs.getString("email") != null ? rs.getString("email") : "");
                user.setPhone(rs.getString("phone") != null ? rs.getString("phone") : "");
                return user;
            }
        } catch (SQLException e) {
            System.err.println("GetUser error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Add a new user to the database.  
     * @return true if insertion succeeded (existing behavior)
     */
    public boolean addUser(String username, String password, String role, String email, String phone) {
        String sql = "INSERT INTO users (username, password, role, email, phone) VALUES (?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hashPassword(password));
            ps.setString(3, role);
            ps.setString(4, email);
            ps.setString(5, phone);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Add user error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Insert a new user and return the generated user_id, or -1 on failure.
     */
    public int createUser(String username, String password, String role, String email, String phone) {
        String sql = "INSERT INTO users (username, password, role, email, phone) VALUES (?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hashPassword(password));
            ps.setString(3, role);
            ps.setString(4, email);
            ps.setString(5, phone);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Create user error: " + e.getMessage());
        }
        return -1;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, role, email, phone FROM users ORDER BY role, username";
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
                users.add(u);
            }
        } catch (SQLException e) {
            System.err.println("GetAllUsers error: " + e.getMessage());
        }
        return users;
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Delete user error: " + e.getMessage());
        }
        return false;
    }
}
