package com.group01.asm2.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record SecureActionConfig(
    RateLimitPolicy rateLimitPolicy,
    String identityNodeId,
    String messageNodeId
) {

    public static Optional<SecureActionConfig> from(Object userData) {
        if (!(userData instanceof String rawConfig) || rawConfig.isBlank()) {
            return Optional.empty();
        }

        Map<String, String> values = parse(rawConfig);
        String rate = values.get("rate");

        if (rate == null || rate.isBlank()) {
            return Optional.empty();
        }

        try {
            RateLimitPolicy policy = RateLimitPolicy.valueOf(rate.trim().toUpperCase());

            return Optional.of(new SecureActionConfig(
                policy,
                values.get("identity"),
                values.get("message")
            ));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
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