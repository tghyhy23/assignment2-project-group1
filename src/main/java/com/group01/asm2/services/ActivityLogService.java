package com.group01.asm2.services;

import com.group01.asm2.core.SessionManager;
import com.group01.asm2.enums.ActivityActionType;
import com.group01.asm2.models.ActivityLog;
import com.group01.asm2.models.Person;
import com.group01.asm2.repositories.ActivityLogRepository;

import java.time.LocalDateTime;

/**
 * @author Group 01
 */
public class ActivityLogService extends BaseService {
    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService() {
        this(new ActivityLogRepository());
    }

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    public ActivityLog createActivityLog(ActivityActionType actionType, String targetEntity,
                                         Integer targetId, String description) {
        // 1. Read current actor if available
        Person currentUser = SessionManager.getCurrentUser();

        // 2. Build activity log
        ActivityLog log = new ActivityLog();

        log.setTimestamp(LocalDateTime.now());
        log.setActionType(actionType);
        log.setTargetEntity(targetEntity);
        log.setTargetId(targetId);
        log.setDescription(description);

        if (currentUser != null) {
            log.setActorId(currentUser.getId());
            log.setActorRole(currentUser.getRole());
        }

        // 3. Save append-only log
        return activityLogRepository.createActivityLog(log);
    }
}