package com.group01.asm2.models;

import com.group01.asm2.enums.ActivityActionType;
import com.group01.asm2.enums.UserRole;

import java.time.LocalDateTime;

public class ActivityLog {
    private Integer id;
    private LocalDateTime timestamp;
    private Integer actorId;
    private UserRole actorRole;
    private ActivityActionType actionType;
    private String targetEntity;
    private Integer targetId;
    private String description;

    public ActivityLog() {
    }

    public ActivityLog(Integer id, LocalDateTime timestamp, Integer actorId, UserRole actorRole,
                       ActivityActionType actionType, String targetEntity, Integer targetId,
                       String description) {
        this.id = id;
        this.timestamp = timestamp;
        this.actorId = actorId;
        this.actorRole = actorRole;
        this.actionType = actionType;
        this.targetEntity = targetEntity;
        this.targetId = targetId;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Integer getActorId() {
        return actorId;
    }

    public UserRole getActorRole() {
        return actorRole;
    }

    public ActivityActionType getActionType() {
        return actionType;
    }

    public String getTargetEntity() {
        return targetEntity;
    }

    public Integer getTargetId() {
        return targetId;
    }

    public String getDescription() {
        return description;
    }
}