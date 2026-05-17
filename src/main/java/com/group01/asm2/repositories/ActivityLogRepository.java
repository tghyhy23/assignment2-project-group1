package com.group01.asm2.repositories;

import com.group01.asm2.db.SqlExecutor;
import com.group01.asm2.enums.ActivityActionType;
import com.group01.asm2.enums.UserRole;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Group 01
 */
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

        return SqlExecutor.queryOne(
            sql,
            ps -> bindCreateActivityLog(ps, activityLog),
            this::mapActivityLog
        ).orElseThrow(() -> AppException.database("Failed to create activity log."));
    }

    public ActivityLog createActivityLog(Connection connection, ActivityLog activityLog) {
        if (connection == null) {
            throw AppException.database("Database connection is required to create activity log.");
        }

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

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bindCreateActivityLog(ps, activityLog);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapActivityLog(rs);
                }
            }

            throw AppException.database("Failed to create activity log.");
        } catch (AppException ex) {
            throw ex;
        } catch (Exception ex) {
            throw AppException.database("Failed to create activity log: " + ex.getMessage());
        }
    }

    public ActivityLog readActivityLogById(Integer id) {
        String sql = """
            SELECT id, timestamp, actor_id, actor_role, action_type, target_entity, target_id, description
            FROM activity_logs
            WHERE id = ?
        """;

        return SqlExecutor.queryOne(
            sql,
            ps -> ps.setInt(1, id),
            this::mapActivityLog
        ).orElse(null);
    }

    public List<ActivityLog> readActivityLogsByActorId(Integer actorId) {
        String sql = """
            SELECT id, timestamp, actor_id, actor_role, action_type, target_entity, target_id, description
            FROM activity_logs
            WHERE actor_id = ?
            ORDER BY timestamp DESC, id DESC
        """;

        return SqlExecutor.queryMany(
            sql,
            ps -> ps.setInt(1, actorId),
            this::mapActivityLog
        );
    }

    public List<ActivityLog> readActivityLogsByActorId(Integer actorId, int limit) {
        String sql = """
            SELECT id, timestamp, actor_id, actor_role, action_type, target_entity, target_id, description
            FROM activity_logs
            WHERE actor_id = ?
            ORDER BY timestamp DESC, id DESC
            LIMIT ?
        """;

        return SqlExecutor.queryMany(
            sql,
            ps -> {
                ps.setInt(1, actorId);
                ps.setInt(2, limit);
            },
            this::mapActivityLog
        );
    }

    public List<ActivityLog> readActivityLogs() {
        String sql = """
            SELECT id, timestamp, actor_id, actor_role, action_type, target_entity, target_id, description
            FROM activity_logs
            ORDER BY timestamp DESC, id DESC
        """;

        return SqlExecutor.queryMany(
            sql,
            ps -> {
            },
            this::mapActivityLog
        );
    }

    public List<ActivityLog> readActivityLogs(int limit) {
        String sql = """
            SELECT id, timestamp, actor_id, actor_role, action_type, target_entity, target_id, description
            FROM activity_logs
            ORDER BY timestamp DESC, id DESC
            LIMIT ?
        """;

        return SqlExecutor.queryMany(
            sql,
            ps -> ps.setInt(1, limit),
            this::mapActivityLog
        );
    }

    public List<ActivityLog> readActivityLogsByTarget(String targetEntity, Integer targetId) {
        String sql = """
            SELECT id, timestamp, actor_id, actor_role, action_type, target_entity, target_id, description
            FROM activity_logs
            WHERE target_entity = ?
              AND target_id = ?
            ORDER BY timestamp DESC, id DESC
        """;

        return SqlExecutor.queryMany(
            sql,
            ps -> {
                ps.setString(1, targetEntity);
                ps.setInt(2, targetId);
            },
            this::mapActivityLog
        );
    }

    private void bindCreateActivityLog(PreparedStatement ps, ActivityLog activityLog) throws Exception {
        LocalDateTime timestamp = activityLog.getTimestamp() != null
            ? activityLog.getTimestamp()
            : LocalDateTime.now();

        ps.setTimestamp(1, Timestamp.valueOf(timestamp));

        if (activityLog.getActorId() == null) {
            ps.setNull(2, Types.INTEGER);
        } else {
            ps.setInt(2, activityLog.getActorId());
        }

        if (activityLog.getActorRole() == null) {
            ps.setNull(3, Types.VARCHAR);
        } else {
            ps.setString(3, activityLog.getActorRole().name());
        }

        ps.setString(4, activityLog.getActionType().name());
        ps.setString(5, activityLog.getTargetEntity());

        if (activityLog.getTargetId() == null) {
            ps.setNull(6, Types.INTEGER);
        } else {
            ps.setInt(6, activityLog.getTargetId());
        }

        ps.setString(7, activityLog.getDescription());
    }

    private ActivityLog mapActivityLog(ResultSet rs) throws Exception {
        ActivityLog activityLog = new ActivityLog();

        activityLog.setId(rs.getInt("id"));

        Timestamp timestamp = rs.getTimestamp("timestamp");
        if (timestamp != null) {
            activityLog.setTimestamp(timestamp.toLocalDateTime());
        }

        int actorId = rs.getInt("actor_id");
        activityLog.setActorId(rs.wasNull() ? null : actorId);

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
        activityLog.setTargetId(rs.wasNull() ? null : targetId);

        activityLog.setDescription(rs.getString("description"));

        return activityLog;
    }
}