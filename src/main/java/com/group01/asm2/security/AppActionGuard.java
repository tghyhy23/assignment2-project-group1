package com.group01.asm2.security;

import java.util.function.Supplier;

public final class AppActionGuard {
    private static final AppRateLimiter RATE_LIMITER = new AppRateLimiter();

    private AppActionGuard() {
    }

    public static void runGlobal(Runnable action) {
        RATE_LIMITER.check(
            RateLimitPolicy.GLOBAL.buildKey("app"),
            RateLimitPolicy.GLOBAL.getMaxActions(),
            RateLimitPolicy.GLOBAL.getDuration()
        );

        action.run();
    }

    public static <T> T callGlobal(Supplier<T> action) {
        RATE_LIMITER.check(
            RateLimitPolicy.GLOBAL.buildKey("app"),
            RateLimitPolicy.GLOBAL.getMaxActions(),
            RateLimitPolicy.GLOBAL.getDuration()
        );

        return action.get();
    }

    public static void runLimited(
        RateLimitPolicy policy,
        String identity,
        Runnable action
    ) {
        runGlobal();

        RATE_LIMITER.check(
            policy.buildKey(identity),
            policy.getMaxActions(),
            policy.getDuration()
        );

        action.run();
    }

    public static <T> T callLimited(
        RateLimitPolicy policy,
        String identity,
        Supplier<T> action
    ) {
        runGlobal();

        RATE_LIMITER.check(
            policy.buildKey(identity),
            policy.getMaxActions(),
            policy.getDuration()
        );

        return action.get();
    }

    private static void runGlobal() {
        RATE_LIMITER.check(
            RateLimitPolicy.GLOBAL.buildKey("app"),
            RateLimitPolicy.GLOBAL.getMaxActions(),
            RateLimitPolicy.GLOBAL.getDuration()
        );
    }
}