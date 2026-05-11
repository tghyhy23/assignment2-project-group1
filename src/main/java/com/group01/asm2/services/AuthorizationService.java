package com.group01.asm2.services;

import com.group01.asm2.core.SessionManager;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.Person;
import com.group01.asm2.security.Permission;
import com.group01.asm2.security.RolePermissionPolicy;

public final class AuthorizationService {
    private AuthorizationService() {
    }

    public static void requireAuthenticated() {
        if (!SessionManager.isLoggedIn()) {
            throw AppException.authentication("Please login first.");
        }
    }

    public static Person getCurrentUserOrThrow() {
        requireAuthenticated();
        return SessionManager.getCurrentUser();
    }

    public static boolean hasPermission(Permission permission) {
        if (!SessionManager.isLoggedIn()) {
            return false;
        }

        Person currentUser = SessionManager.getCurrentUser();

        return RolePermissionPolicy.hasPermission(
            currentUser.getRole(),
            permission
        );
    }

    public static void requirePermission(Permission permission) {
        requireAuthenticated();

        if (!hasPermission(permission)) {
            throw AppException.authorization("You are not authorized to perform this action.");
        }
    }

    public static void requireOwner(Integer ownerId) {
        Person currentUser = getCurrentUserOrThrow();

        if (ownerId == null || !ownerId.equals(currentUser.getId())) {
            throw AppException.authorization("You can only manage your own resource.");
        }
    }

    public static void requireOwnerOrPermission(Integer ownerId, Permission fallbackPermission) {
        Person currentUser = getCurrentUserOrThrow();

        boolean isOwner = ownerId != null && ownerId.equals(currentUser.getId());
        boolean hasFallbackPermission = hasPermission(fallbackPermission);

        if (!isOwner && !hasFallbackPermission) {
            throw AppException.authorization("You are not authorized to perform this action.");
        }
    }
}