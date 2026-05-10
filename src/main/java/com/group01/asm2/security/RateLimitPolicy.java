package com.group01.asm2.security;

import java.time.Duration;

public enum RateLimitPolicy {

    LOGIN(
        5,
        Duration.ofMinutes(5),
        "Too many login attempts. Please wait a few minutes and try again."
    ),

    DEFAULT_ACTION(
        30,
        Duration.ofMinutes(1),
        "You are doing that too fast. Please slow down."
    );

    private final int maxAttempts;
    private final Duration window;
    private final String errorMessage;

    RateLimitPolicy(int maxAttempts, Duration window, String errorMessage) {
        this.maxAttempts = maxAttempts;
        this.window = window;
        this.errorMessage = errorMessage;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public Duration getWindow() {
        return window;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}