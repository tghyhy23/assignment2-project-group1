package com.group01.asm2.security;

import com.group01.asm2.enums.UserRole;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class RolePermissionPolicy {
    private static final Map<UserRole, Set<Permission>> ROLE_PERMISSIONS = Map.of(
        UserRole.BUYER,
        permissions(
            Permission.READ_AUCTION,
            Permission.CREATE_BID,
            Permission.READ_OWN_PROFILE,
            Permission.UPDATE_OWN_PROFILE,
            Permission.CREATE_TOP_UP_REQUEST,
            Permission.READ_OWN_ACTIVITY_LOG
        ),

        UserRole.SELLER,
        permissions(
            Permission.READ_AUCTION,
            Permission.READ_OWN_PROFILE,
            Permission.UPDATE_OWN_PROFILE,
            Permission.CREATE_ITEM,
            Permission.UPDATE_OWN_ITEM,
            Permission.DELETE_OWN_ITEM,
            Permission.CREATE_AUCTION,
            Permission.UPDATE_OWN_AUCTION,
            Permission.CREATE_TOP_UP_REQUEST,
            Permission.READ_SELLER_REPORT,
            Permission.READ_OWN_ACTIVITY_LOG
        ),

        UserRole.AUCTION_ADMINISTRATOR,
        permissions(
            Permission.READ_AUCTION,
            Permission.READ_ANY_USER,
            Permission.PROCESS_AUCTION,
            Permission.CREATE_CATEGORY,
            Permission.UPDATE_CATEGORY,
            Permission.DELETE_CATEGORY,
            Permission.APPROVE_TOP_UP_REQUEST,
            Permission.READ_AUCTION_REPORT,
            Permission.READ_SYSTEM_REPORT,
            Permission.READ_ANY_ACTIVITY_LOG
        ),

        UserRole.SYSTEM_ADMINISTRATOR,
        EnumSet.allOf(Permission.class)
    );

    private RolePermissionPolicy() {
    }

    public static boolean hasPermission(UserRole role, Permission permission) {
        if (role == null || permission == null) {
            return false;
        }

        return ROLE_PERMISSIONS
            .getOrDefault(role, Set.of())
            .contains(permission);
    }

    private static Set<Permission> permissions(Permission first, Permission... rest) {
        EnumSet<Permission> set = EnumSet.of(first, rest);
        return Set.copyOf(set);
    }
}