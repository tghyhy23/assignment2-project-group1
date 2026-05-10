package com.group01.asm2.utils;

import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.security.InputSanitizer;

public final class InputValidator {

    private InputValidator() {
    }

    public static String requireUsername(String value) {
        String username = InputSanitizer.normalizeUsername(value);

        if (username.isBlank()) {
            throw AppException.validation("Username is required.");
        }

        if (username.length() < 3 || username.length() > 80) {
            throw AppException.validation("Username must be between 3 and 80 characters.");
        }

        if (!username.matches("^[a-z0-9._-]+$")) {
            throw AppException.validation(
                "Username can only contain letters, numbers, dots, underscores, and hyphens."
            );
        }

        return username;
    }

    public static String requirePassword(String value) {
        if (value == null || value.isBlank()) {
            throw AppException.validation("Password is required.");
        }

        return value;
    }
}