package com.group01.asm2.repositories;

import com.group01.asm2.configs.DatabaseConfig;
import com.group01.asm2.enums.ActivityActionType;
import com.group01.asm2.enums.UserRole;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.ActivityLog;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogRepository {

    public ActivityLog createActivityLog(ActivityLog activityLog) {
        String sql = """
            INSERT INTO activity_logs (
                timestamp,
                actor_id,
                actor_role,
                action_type,
                target_entity,
                target_id,
                description
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING id, timestamp, actor_id, actor_role, action_type, target_entity, target_id, description
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            LocalDateTime timestamp = activityLog.getTimestamp() != null
                ? activityLog.getTimestamp()
                : LocalDateTime.now();

            stmt.setTimestamp(1, Timestamp.valueOf(timestamp));

            if (activityLog.getActorId() == null) {
                stmt.setNull(2, Types.INTEGER);
            } else {
                stmt.setInt(2, activityLog.getActorId());
            }

            if (activityLog.getActorRole() == null) {
                stmt.setNull(3, Types.VARCHAR);
            } else {
                stmt.setString(3, activityLog.getActorRole().name());
            }

            stmt.setString(4, activityLog.getActionType().name());
            stmt.setString(5, activityLog.getTargetEntity());

            if (activityLog.getTargetId() == null) {
                stmt.setNull(6, Types.INTEGER);
            } else {
                stmt.setInt(6, activityLog.getTargetId());
            }

            stmt.setString(7, activityLog.getDescription());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapActivityLog(rs);
                }
            }

            throw AppException.database("Failed to create activity log.");

        } catch (SQLException e) {
            throw AppException.database("Database error while creating activity log: " + e.getMessage());
        }
    }

    public ActivityLog readActivityLogById(Integer id) {
        String sql = """
            SELECT id, timestamp, actor_id, actor_role, action_type, target_entity, target_id, description
            FROM activity_logs
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapActivityLog(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw AppException.database("Database error while reading activity log: " + e.getMessage());
        }
    }

    public List<ActivityLog> readActivityLogsByActorId(Integer actorId) {
        String sql = """
            SELECT id, timestamp, actor_id, actor_role, action_type, target_entity, target_id, description
            FROM activity_logs
            WHERE actor_id = ?
            ORDER BY timestamp DESC, id DESC
        """;

        List<ActivityLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, actorId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapActivityLog(rs));
                }
            }

            return logs;

        } catch (SQLException e) {
            throw AppException.database("Database error while reading actor activity logs: " + e.getMessage());
        }
    }

    public List<ActivityLog> readActivityLogs() {
        String sql = """
            SELECT id, timestamp, actor_id, actor_role, action_type, target_entity, target_id, description
            FROM activity_logs
            ORDER BY timestamp DESC, id DESC
        """;

        List<ActivityLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                logs.add(mapActivityLog(rs));
            }

            return logs;

        } catch (SQLException e) {
            throw AppException.database("Database error while reading activity logs: " + e.getMessage());
        }
    }

    public List<ActivityLog> readActivityLogsByTarget(String targetEntity, Integer targetId) {
        String sql = """
            SELECT id, timestamp, actor_id, actor_role, action_type, target_entity, target_id, description
            FROM activity_logs
            WHERE target_entity = ?
              AND target_id = ?
            ORDER BY timestamp DESC, id DESC
        """;

        List<ActivityLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, targetEntity);
            stmt.setInt(2, targetId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapActivityLog(rs));
                }
            }

            return logs;

        } catch (SQLException e) {
            throw AppException.database("Database error while reading target activity logs: " + e.getMessage());
        }
    }

    private ActivityLog mapActivityLog(ResultSet rs) throws SQLException {
        ActivityLog activityLog = new ActivityLog();

        activityLog.setId(rs.getInt("id"));

        Timestamp timestamp = rs.getTimestamp("timestamp");
        if (timestamp != null) {
            activityLog.setTimestamp(timestamp.toLocalDateTime());
        }

        int actorId = rs.getInt("actor_id");
        if (rs.wasNull()) {
            activityLog.setActorId(null);
        } else {
            activityLog.setActorId(actorId);
        }

        String actorRole = rs.getString("actor_role");
        if (actorRole != null) {
            activityLog.setActorRole(UserRole.valueOf(actorRole));
        }

        String actionType = rs.getString("action_type");
        if (actionType != null) {
            activityLog.setActionType(ActivityActionType.valueOf(actionType));
        }

        activityLog.setTargetEntity(rs.getString("target_entity"));

        int targetId = rs.getInt("target_id");
        if (rs.wasNull()) {
            activityLog.setTargetId(null);
        } else {
            activityLog.setTargetId(targetId);
        }

        activityLog.setDescription(rs.getString("description"));

        return activityLog;
    }
}