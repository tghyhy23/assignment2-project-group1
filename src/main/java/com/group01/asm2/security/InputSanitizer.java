package com.group01.asm2.security;

public final class InputSanitizer {

    private InputSanitizer() {
    }

    public static String sanitizeText(String value) {
        if (value == null) {
            return "";
        }

        return value
            .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "")
            .replace("\u0000", "");
    }

    public static String normalizeUsername(String value) {
        return sanitizeText(value).trim().toLowerCase();
    }

    public static String normalizeRateLimitIdentity(String value) {
        String normalized = normalizeUsername(value);

        if (normalized.isBlank()) {
            return "unknown";
        }

        return normalized;
    }
}