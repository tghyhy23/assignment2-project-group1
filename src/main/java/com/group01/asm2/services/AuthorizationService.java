package com.group01.asm2.services;

import com.group01.asm2.enums.UserRole;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.Person;
import com.group01.asm2.security.SecurityAction;
import com.group01.asm2.security.SecurityResource;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class AuthorizationService {
    private static final AuthorizationService INSTANCE = new AuthorizationService();

    private final Map<UserRole, Set<String>> permissions = new EnumMap<>(UserRole.class);

    private AuthorizationService() {
        registerBuyerPermissions();
        registerSellerPermissions();
        registerAuctionAdministratorPermissions();
        registerSystemAdministratorPermissions();
    }

    public static AuthorizationService getInstance() {
        return INSTANCE;
    }

    public boolean can(Person actor, String action, String resource) {
        if (actor == null || actor.getRole() == null) {
            return false;
        }

        if (actor.getRole() == UserRole.SYSTEM_ADMINISTRATOR) {
            return true;
        }

        return permissions
            .getOrDefault(actor.getRole(), Set.of())
            .contains(key(action, resource));
    }

    public void require(Person actor, String action, String resource) {
        if (!can(actor, action, resource)) {
            throw AppException.authorization("You are not authorized to perform this action.");
        }
    }

    private void registerBuyerPermissions() {
        allow(UserRole.BUYER, SecurityAction.READ, SecurityResource.AUCTION);
        allow(UserRole.BUYER, SecurityAction.CREATE, SecurityResource.BID);
        allow(UserRole.BUYER, SecurityAction.READ_OWN, SecurityResource.PROFILE);
        allow(UserRole.BUYER, SecurityAction.UPDATE_OWN, SecurityResource.PROFILE);
        allow(UserRole.BUYER, SecurityAction.CREATE, SecurityResource.TOP_UP_REQUEST);
    }

    private void registerSellerPermissions() {
        inherit(UserRole.SELLER, UserRole.BUYER);

        allow(UserRole.SELLER, SecurityAction.CREATE, SecurityResource.ITEM);
        allow(UserRole.SELLER, SecurityAction.UPDATE_OWN, SecurityResource.ITEM);
        allow(UserRole.SELLER, SecurityAction.DELETE_OWN, SecurityResource.ITEM);
        allow(UserRole.SELLER, SecurityAction.CREATE, SecurityResource.AUCTION);
        allow(UserRole.SELLER, SecurityAction.UPDATE_OWN, SecurityResource.AUCTION);
        allow(UserRole.SELLER, SecurityAction.READ_OWN, SecurityResource.SELLER_REPORT);
    }

    private void registerAuctionAdministratorPermissions() {
        allow(UserRole.AUCTION_ADMINISTRATOR, SecurityAction.MODERATE, SecurityResource.ITEM);
        allow(UserRole.AUCTION_ADMINISTRATOR, SecurityAction.CREATE, SecurityResource.CATEGORY);
        allow(UserRole.AUCTION_ADMINISTRATOR, SecurityAction.UPDATE, SecurityResource.CATEGORY);
        allow(UserRole.AUCTION_ADMINISTRATOR, SecurityAction.DELETE, SecurityResource.CATEGORY);
        allow(UserRole.AUCTION_ADMINISTRATOR, SecurityAction.UPDATE, SecurityResource.AUCTION);
        allow(UserRole.AUCTION_ADMINISTRATOR, SecurityAction.PROCESS, SecurityResource.AUCTION);
        allow(UserRole.AUCTION_ADMINISTRATOR, SecurityAction.APPROVE, SecurityResource.TOP_UP_REQUEST);
        allow(UserRole.AUCTION_ADMINISTRATOR, SecurityAction.UPDATE, SecurityResource.PAYMENT);
        allow(UserRole.AUCTION_ADMINISTRATOR, SecurityAction.READ, SecurityResource.AUCTION_REPORT);
        allow(UserRole.AUCTION_ADMINISTRATOR, SecurityAction.READ_OWN, SecurityResource.ACTIVITY_LOG);
    }

    private void registerSystemAdministratorPermissions() {
        inherit(UserRole.SYSTEM_ADMINISTRATOR, UserRole.AUCTION_ADMINISTRATOR);

        allow(UserRole.SYSTEM_ADMINISTRATOR, SecurityAction.CREATE, SecurityResource.USER);
        allow(UserRole.SYSTEM_ADMINISTRATOR, SecurityAction.READ_ANY, SecurityResource.USER);
        allow(UserRole.SYSTEM_ADMINISTRATOR, SecurityAction.UPDATE, SecurityResource.USER);
        allow(UserRole.SYSTEM_ADMINISTRATOR, SecurityAction.DELETE, SecurityResource.USER);
        allow(UserRole.SYSTEM_ADMINISTRATOR, SecurityAction.READ_ANY, SecurityResource.ACTIVITY_LOG);
        allow(UserRole.SYSTEM_ADMINISTRATOR, SecurityAction.READ, SecurityResource.SYSTEM_REPORT);
        allow(UserRole.SYSTEM_ADMINISTRATOR, SecurityAction.EXPORT, SecurityResource.SYSTEM_REPORT);
    }

    private void allow(UserRole role, String action, String resource) {
        permissions.computeIfAbsent(role, ignored -> new HashSet<>()).add(key(action, resource));
    }

    private void inherit(UserRole childRole, UserRole parentRole) {
        permissions
            .computeIfAbsent(childRole, ignored -> new HashSet<>())
            .addAll(permissions.getOrDefault(parentRole, Set.of()));
    }

    private String key(String action, String resource) {
        return action + ":" + resource;
    }
}