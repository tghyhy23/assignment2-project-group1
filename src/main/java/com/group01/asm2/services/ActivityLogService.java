package com.group01.asm2.services;

import com.group01.asm2.core.SessionManager;
import com.group01.asm2.enums.ActivityActionType;
import com.group01.asm2.enums.UserRole;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.ActivityLog;
import com.group01.asm2.models.Person;
import com.group01.asm2.repositories.ActivityLogRepository;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Group 01
 */
public class ActivityLogService extends BaseService {
    private static final int DEFAULT_LOG_LIMIT = 200;
    private static final int MAX_DESCRIPTION_LENGTH = 500;

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService() {
        this(new ActivityLogRepository());
    }

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    public ActivityLog createActivityLog(ActivityActionType actionType,
                                         String targetEntity,
                                         Integer targetId,
                                         String description) {
        ActivityLog activityLog = buildActivityLogFromCurrentUser(
            actionType,
            targetEntity,
            targetId,
            description
        );

        return activityLogRepository.createActivityLog(activityLog);
    }

    public ActivityLog createActivityLog(Connection connection,
                                         ActivityActionType actionType,
                                         String targetEntity,
                                         Integer targetId,
                                         String description) {
        if (connection == null) {
            throw AppException.database("Database connection is required for transaction-safe activity logging.");
        }

        ActivityLog activityLog = buildActivityLogFromCurrentUser(
            actionType,
            targetEntity,
            targetId,
            description
        );

        return activityLogRepository.createActivityLog(connection, activityLog);
    }

    public ActivityLog createSystemActivityLog(ActivityActionType actionType,
                                               String targetEntity,
                                               Integer targetId,
                                               String description) {
        ActivityLog activityLog = buildActivityLog(
            null,
            actionType,
            targetEntity,
            targetId,
            description
        );

        return activityLogRepository.createActivityLog(activityLog);
    }

    public ActivityLog createSystemActivityLog(Connection connection,
                                               ActivityActionType actionType,
                                               String targetEntity,
                                               Integer targetId,
                                               String description) {
        if (connection == null) {
            throw AppException.database("Database connection is required for transaction-safe activity logging.");
        }

        ActivityLog activityLog = buildActivityLog(
            null,
            actionType,
            targetEntity,
            targetId,
            description
        );

        return activityLogRepository.createActivityLog(connection, activityLog);
    }

    public List<ActivityLog> readMyActivityLogs() {
        Person currentUser = getLoggedInUserOrThrow();

        return activityLogRepository.readActivityLogsByActorId(
            currentUser.getId(),
            DEFAULT_LOG_LIMIT
        );
    }

    public List<ActivityLog> readActivityLogsByActorId(Integer actorId) {
        Person currentUser = getLoggedInUserOrThrow();

        if (actorId == null) {
            throw AppException.validation("Actor id is required.");
        }

        boolean isOwnLog = actorId.equals(currentUser.getId());
        boolean isSystemAdmin = currentUser.getRole() == UserRole.SYSTEM_ADMINISTRATOR;

        if (!isOwnLog && !isSystemAdmin) {
            throw AppException.authorization("You can only view your own activity history.");
        }

        return activityLogRepository.readActivityLogsByActorId(actorId, DEFAULT_LOG_LIMIT);
    }

    public List<ActivityLog> readAllActivityLogs() {
        Person currentUser = getLoggedInUserOrThrow();

        if (currentUser.getRole() != UserRole.SYSTEM_ADMINISTRATOR) {
            throw AppException.authorization("Only the system administrator can view the full system activity log.");
        }

        return activityLogRepository.readActivityLogs(DEFAULT_LOG_LIMIT);
    }

    public ActivityLog readActivityLogById(Integer id) {
        Person currentUser = getLoggedInUserOrThrow();

        if (id == null) {
            throw AppException.validation("Activity log id is required.");
        }

        ActivityLog log = activityLogRepository.readActivityLogById(id);

        if (log == null) {
            throw AppException.validation("Activity log does not exist.");
        }

        boolean isOwnLog = log.getActorId() != null && log.getActorId().equals(currentUser.getId());
        boolean isSystemAdmin = currentUser.getRole() == UserRole.SYSTEM_ADMINISTRATOR;

        if (!isOwnLog && !isSystemAdmin) {
            throw AppException.authorization("You can only view your own activity history.");
        }

        return log;
    }

    public List<ActivityLog> readActivityLogsByTarget(String targetEntity, Integer targetId) {
        Person currentUser = getLoggedInUserOrThrow();

        if (currentUser.getRole() != UserRole.SYSTEM_ADMINISTRATOR) {
            throw AppException.authorization("Only the system administrator can view activity logs by target.");
        }

        if (targetEntity == null || targetEntity.isBlank()) {
            throw AppException.validation("Target entity is required.");
        }

        if (targetId == null) {
            throw AppException.validation("Target id is required.");
        }

        return activityLogRepository.readActivityLogsByTarget(targetEntity.trim(), targetId);
    }

    private ActivityLog buildActivityLogFromCurrentUser(ActivityActionType actionType,
                                                        String targetEntity,
                                                        Integer targetId,
                                                        String description) {
        Person currentUser = SessionManager.getCurrentUser();

        return buildActivityLog(
            currentUser,
            actionType,
            targetEntity,
            targetId,
            description
        );
    }

    private ActivityLog buildActivityLog(Person actor,
                                         ActivityActionType actionType,
                                         String targetEntity,
                                         Integer targetId,
                                         String description) {
        validateActivityLogInput(actionType, targetEntity, description);

        ActivityLog activityLog = new ActivityLog();

        activityLog.setTimestamp(LocalDateTime.now());
        activityLog.setActionType(actionType);
        activityLog.setTargetEntity(targetEntity.trim());
        activityLog.setTargetId(targetId);
        activityLog.setDescription(description.trim());

        if (actor != null) {
            activityLog.setActorId(actor.getId());
            activityLog.setActorRole(actor.getRole());
        }

        return activityLog;
    }

    private void validateActivityLogInput(ActivityActionType actionType,
                                          String targetEntity,
                                          String description) {
        if (actionType == null) {
            throw AppException.validation("Activity action type is required.");
        }

        if (targetEntity == null || targetEntity.isBlank()) {
            throw AppException.validation("Activity target entity is required.");
        }

        if (description == null || description.isBlank()) {
            throw AppException.validation("Activity description is required.");
        }

        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw AppException.validation("Activity description must not exceed " + MAX_DESCRIPTION_LENGTH + " characters.");
        }
    }

    private Person getLoggedInUserOrThrow() {
        Person currentUser = SessionManager.getCurrentUser();

        if (currentUser == null) {
            throw AppException.authorization("You must log in to access activity logs.");
        }

        return currentUser;
    }
}