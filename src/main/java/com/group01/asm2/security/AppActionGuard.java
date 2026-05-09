package com.group01.asm2.security;

import java.time.Duration;
import java.util.function.Supplier;

public final class AppActionGuard {
    private static final AppRateLimiter RATE_LIMITER = new AppRateLimiter();

    private AppActionGuard() {
    }

    public static void runGlobal(Runnable action) {
        RATE_LIMITER.check("global", 300, Duration.ofMinutes(15));
        action.run();
    }

    public static <T> T callGlobal(Supplier<T> action) {
        RATE_LIMITER.check("global", 300, Duration.ofMinutes(15));
        return action.get();
    }

    public static void runLimited(String key, int maxActions, Duration duration, Runnable action) {
        RATE_LIMITER.check("global", 300, Duration.ofMinutes(15));
        RATE_LIMITER.check(key, maxActions, duration);
        action.run();
    }

    public static <T> T callLimited(String key, int maxActions, Duration duration, Supplier<T> action) {
        RATE_LIMITER.check("global", 300, Duration.ofMinutes(15));
        RATE_LIMITER.check(key, maxActions, duration);
        return action.get();
    }
}