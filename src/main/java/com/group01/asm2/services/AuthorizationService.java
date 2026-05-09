package com.group01.asm2.services;

import com.group01.asm2.enums.UserRole;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.Person;
import com.group01.asm2.security.Permission;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class AuthorizationService {
    private static final AuthorizationService INSTANCE = new AuthorizationService();

    private final Map<UserRole, Set<Permission>> permissions = new EnumMap<>(UserRole.class);

    private AuthorizationService() {
        registerBuyerPermissions();
        registerSellerPermissions();
        registerAuctionAdministratorPermissions();
        registerSystemAdministratorPermissions();
    }

    public static AuthorizationService getInstance() {
        return INSTANCE;
    }

    public boolean can(Person actor, Permission permission) {
        if (actor == null || actor.getRole() == null) {
            return false;
        }

        if (actor.getRole() == UserRole.SYSTEM_ADMINISTRATOR) {
            return true;
        }

        return permissions
            .getOrDefault(actor.getRole(), Set.of())
            .contains(permission);
    }

    public void require(Person actor, Permission permission) {
        if (!can(actor, permission)) {
            throw AppException.authorization("You are not authorized to perform this action.");
        }
    }

    private void registerBuyerPermissions() {
        allow(
            UserRole.BUYER,
            Permission.READ_AUCTION,
            Permission.CREATE_BID,
            Permission.READ_OWN_PROFILE,
            Permission.UPDATE_OWN_PROFILE,
            Permission.CREATE_TOP_UP_REQUEST
        );
    }

    private void registerSellerPermissions() {
        inherit(UserRole.SELLER, UserRole.BUYER);

        allow(
            UserRole.SELLER,
            Permission.CREATE_ITEM,
            Permission.UPDATE_OWN_ITEM,
            Permission.DELETE_OWN_ITEM,
            Permission.CREATE_AUCTION,
            Permission.UPDATE_OWN_AUCTION,
            Permission.READ_SELLER_REPORT
        );
    }

    private void registerAuctionAdministratorPermissions() {
        allow(
            UserRole.AUCTION_ADMINISTRATOR,
            Permission.CREATE_CATEGORY,
            Permission.UPDATE_CATEGORY,
            Permission.DELETE_CATEGORY,
            Permission.PROCESS_AUCTION,
            Permission.APPROVE_TOP_UP_REQUEST,
            Permission.READ_AUCTION_REPORT,
            Permission.READ_OWN_ACTIVITY_LOG
        );
    }

    private void registerSystemAdministratorPermissions() {
        inherit(UserRole.SYSTEM_ADMINISTRATOR, UserRole.AUCTION_ADMINISTRATOR);

        allow(
            UserRole.SYSTEM_ADMINISTRATOR,
            Permission.CREATE_USER,
            Permission.READ_ANY_USER,
            Permission.UPDATE_USER,
            Permission.DELETE_USER,
            Permission.READ_ANY_ACTIVITY_LOG,
            Permission.READ_SYSTEM_REPORT,
            Permission.EXPORT_SYSTEM_REPORT
        );
    }

    private void allow(UserRole role, Permission... rolePermissions) {
        permissions
            .computeIfAbsent(role, ignored -> EnumSet.noneOf(Permission.class))
            .addAll(Set.of(rolePermissions));
    }

    private void inherit(UserRole childRole, UserRole parentRole) {
        permissions
            .computeIfAbsent(childRole, ignored -> EnumSet.noneOf(Permission.class))
            .addAll(permissions.getOrDefault(parentRole, Set.of()));
    }
}