package com.group01.asm2.services;

import com.group01.asm2.core.SessionManager;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.Person;
import com.group01.asm2.security.Permission;

import java.util.function.Supplier;

public abstract class BaseService {
    protected Person getCurrentUserOrThrow() {
        Person currentUser = SessionManager.getCurrentUser();

        if (currentUser == null) {
            throw AppException.authentication("Please log in first.");
        }

        return currentUser;
    }

    protected void requireCurrentUser(Permission permission) {
        AuthorizationService.requirePermission(permission);
    }

    protected <T> T secured(Permission permission, Supplier<T> task) {
        requireCurrentUser(permission);
        return task.get();
    }

    protected void secured(Permission permission, Runnable task) {
        requireCurrentUser(permission);
        task.run();
    }

    protected void requireOwnership(Integer ownerId, String message) {
        Person currentUser = getCurrentUserOrThrow();

        if (currentUser.isSystemAdministrator()) {
            return;
        }

        if (ownerId == null || !ownerId.equals(currentUser.getId())) {
            throw AppException.authorization(message);
        }
    }

    protected void requireOwnershipOrPermission(
        Integer ownerId,
        Permission fallbackPermission,
        String message
    ) {
        Person currentUser = getCurrentUserOrThrow();

        boolean isOwner = ownerId != null && ownerId.equals(currentUser.getId());
        boolean hasFallbackPermission = AuthorizationService.hasPermission(fallbackPermission);

        if (!isOwner && !hasFallbackPermission) {
            throw AppException.authorization(message);
        }
    }
}