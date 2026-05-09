package com.group01.asm2.security;

import java.time.Duration;

public enum RateLimitPolicy {
    GLOBAL("global", 300, Duration.ofMinutes(15)),
    LOGIN("login", 5, Duration.ofMinutes(5)),
    SIGNUP("signup", 5, Duration.ofMinutes(10)),
    SEARCH("search", 60, Duration.ofMinutes(1)),
    PLACE_BID("place-bid", 10, Duration.ofMinutes(1)),
    CREATE_ITEM("create-item", 20, Duration.ofHours(1)),
    TOP_UP_REQUEST("top-up-request", 5, Duration.ofHours(1)),
    APPROVE_TOP_UP("approve-top-up", 30, Duration.ofMinutes(10)),
    EXPORT_REPORT("export-report", 10, Duration.ofHours(1)),
    PROCESS_AUCTIONS("process-auctions", 10, Duration.ofMinutes(10));

    private final String keyPrefix;
    private final int maxActions;
    private final Duration duration;

    RateLimitPolicy(String keyPrefix, int maxActions, Duration duration) {
        this.keyPrefix = keyPrefix;
        this.maxActions = maxActions;
        this.duration = duration;
    }

    public String buildKey(String identity) {
        String safeIdentity = identity == null || identity.isBlank()
            ? "anonymous"
            : identity.trim().toLowerCase();

        return keyPrefix + ":" + safeIdentity;
    }

    public int getMaxActions() {
        return maxActions;
    }

    public Duration getDuration() {
        return duration;
    }
}