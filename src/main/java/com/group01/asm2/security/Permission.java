package com.group01.asm2.security;

public enum Permission {
    READ_AUCTION("auction", "read"),
    CREATE_BID("bid", "create"),

    READ_OWN_PROFILE("profile", "read_own"),
    UPDATE_OWN_PROFILE("profile", "update_own"),

    CREATE_ITEM("item", "create"),
    UPDATE_OWN_ITEM("item", "update_own"),
    DELETE_OWN_ITEM("item", "delete_own"),

    CREATE_AUCTION("auction", "create"),
    UPDATE_OWN_AUCTION("auction", "update_own"),
    PROCESS_AUCTION("auction", "process"),

    CREATE_CATEGORY("category", "create"),
    UPDATE_CATEGORY("category", "update"),
    DELETE_CATEGORY("category", "delete"),

    CREATE_TOP_UP_REQUEST("top_up_request", "create"),
    APPROVE_TOP_UP_REQUEST("top_up_request", "approve"),

    READ_SELLER_REPORT("seller_report", "read_own"),
    READ_AUCTION_REPORT("auction_report", "read"),
    READ_SYSTEM_REPORT("system_report", "read"),
    EXPORT_SYSTEM_REPORT("system_report", "export"),

    CREATE_USER("user", "create"),
    READ_ANY_USER("user", "read_any"),
    UPDATE_USER("user", "update"),
    DELETE_USER("user", "delete"),

    READ_OWN_ACTIVITY_LOG("activity_log", "read_own"),
    READ_ANY_ACTIVITY_LOG("activity_log", "read_any");

    private final String resource;
    private final String action;

    Permission(String resource, String action) {
        this.resource = resource;
        this.action = action;
    }

    public String getResource() {
        return resource;
    }

    public String getAction() {
        return action;
    }
}