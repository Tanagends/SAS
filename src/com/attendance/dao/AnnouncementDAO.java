package com.attendance.dao;

import com.attendance.model.Announcement;
import com.attendance.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Task 1: Uses created_at column consistently
public class AnnouncementDAO {

    // Task 1 Fix: Uses created_at (not date_posted) for consistency with schema
    public List<Announcement> getAllAnnouncements() {
        List<Announcement> list = new ArrayList<>();
        String sql = "SELECT id, title, message, created_at FROM announcements ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Announcement a = new Announcement();
                a.setId(rs.getInt("id"));
                a.setTitle(rs.getString("title"));
                a.setMessage(rs.getString("message"));
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) a.setCreatedAt(ts.toLocalDateTime());
                list.add(a);
            }
        } catch (SQLException e) {
            System.err.println("GetAnnouncements error: " + e.getMessage());
        }
        return list;
    }

    public boolean addAnnouncement(String title, String message) {
        String sql = "INSERT INTO announcements (title, message, date_posted) VALUES (?,?,CURDATE())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, message);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("AddAnnouncement error: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteAnnouncement(int id) {
        String sql = "DELETE FROM announcements WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DeleteAnnouncement error: " + e.getMessage());
        }
        return false;
    }

    public int getTotalAnnouncements() {
        String sql = "SELECT COUNT(*) FROM announcements";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("GetTotalAnnouncements error: " + e.getMessage());
        }
        return 0;
    }
}
