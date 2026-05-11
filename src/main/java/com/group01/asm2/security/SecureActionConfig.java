package com.group01.asm2.security;

import com.group01.asm2.exceptions.AppException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record SecureActionConfig(
    RateLimitPolicy rateLimitPolicy,
    Permission permission,
    String identityNodeId,
    String messageNodeId
) {

    public static Optional<SecureActionConfig> from(Object userData) {
        if (!(userData instanceof String rawConfig) || rawConfig.isBlank()) {
            return Optional.empty();
        }

        Map<String, String> values = parse(rawConfig);

        RateLimitPolicy rateLimitPolicy = parseRateLimitPolicy(values.get("rate"));
        Permission permission = parsePermission(values.get("permission"));

        return Optional.of(new SecureActionConfig(
            rateLimitPolicy,
            permission,
            values.get("identity"),
            values.get("message")
        ));
    }

    private static RateLimitPolicy parseRateLimitPolicy(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return RateLimitPolicy.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw AppException.validation("Invalid rate limit policy: " + value);
        }
    }

    private static Permission parsePermission(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Permission.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw AppException.validation("Invalid permission: " + value);
        }
    }

    private static Map<String, String> parse(String rawConfig) {
        Map<String, String> values = new HashMap<>();

        String[] parts = rawConfig.split(";");

        for (String part : parts) {
            String[] pair = part.split("=", 2);

            if (pair.length == 2) {
                values.put(pair[0].trim(), pair[1].trim());
            }
        }

        return values;
    }
}