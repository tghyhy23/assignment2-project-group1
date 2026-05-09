package com.group01.asm2.services;

import com.group01.asm2.core.SessionManager;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.Person;
import com.group01.asm2.security.Permission;

import java.util.function.Supplier;

public abstract class BaseService {
    protected final AuthorizationService authorizationService = AuthorizationService.getInstance();

    protected Person getCurrentUserOrThrow() {
        Person currentUser = SessionManager.getCurrentUser();

        if (currentUser == null) {
            throw AppException.authentication("Please log in first.");
        }

        return currentUser;
    }

    protected void requireCurrentUser(Permission permission) {
        authorizationService.require(getCurrentUserOrThrow(), permission);
    }

    protected <T> T secured(Permission permission, Supplier<T> task) {
        requireCurrentUser(permission);
        return task.get();
    }

    protected void secured(Permission permission, Runnable task) {
        requireCurrentUser(permission);
        task.run();
    }

    protected void requireOwnership(boolean isOwner, String message) {
        Person currentUser = getCurrentUserOrThrow();

        if (currentUser.isSystemAdministrator()) {
            return;
        }

        if (!isOwner) {
            throw AppException.authorization(message);
        }
    }
}