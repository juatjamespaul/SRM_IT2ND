package com.scc.dao;

import com.scc.model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    // Insert a new activity log
    public boolean logAction(int userId, String actionType, String description) {
        String sql = """
                INSERT INTO activity_logs (user_id, action_type, description)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, actionType);
            pstmt.setString(3, description);

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Failed to save activity log: " + e.getMessage());
            return false;
        }
    }

    // Retrieve all activity logs, newest first
    public List<ActivityLog> getAllLogs() {
        String sql = """
                SELECT id, user_id, action_type, description, timestamp
                FROM activity_logs
                ORDER BY timestamp DESC, id DESC
                """;

        List<ActivityLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                logs.add(mapLog(rs));
            }

        } catch (SQLException e) {
            System.err.println("Failed to retrieve activity logs: " + e.getMessage());
        }

        return logs;
    }

    // Retrieve logs belonging to a specific user
    public List<ActivityLog> getLogsByUserId(int userId) {
        String sql = """
                SELECT id, user_id, action_type, description, timestamp
                FROM activity_logs
                WHERE user_id = ?
                ORDER BY timestamp DESC, id DESC
                """;

        List<ActivityLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapLog(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Failed to retrieve user activity logs: " + e.getMessage());
        }

        return logs;
    }

    // Clear historical logs
    public boolean clearAllLogs() {
        String sql = "DELETE FROM activity_logs";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Failed to clear activity logs: " + e.getMessage());
            return false;
        }
    }

    // Convert database result into ActivityLog object
    private ActivityLog mapLog(ResultSet rs) throws SQLException {
        return new ActivityLog(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("action_type"),
                rs.getString("description"),
                rs.getString("timestamp")
        );
    }
}