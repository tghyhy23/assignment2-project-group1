package com.group01.asm2.security;

import com.group01.asm2.exceptions.AppException;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryRateLimiter {

    private static final ConcurrentHashMap<String, Deque<Instant>> ATTEMPTS = new ConcurrentHashMap<>();

    private InMemoryRateLimiter() {
    }

    public static void check(RateLimitPolicy policy, String identity) {
        String normalizedIdentity = InputSanitizer.normalizeRateLimitIdentity(identity);
        String key = policy.name() + ":" + normalizedIdentity;

        Instant now = Instant.now();
        Instant cutoff = now.minus(policy.getWindow());

        Deque<Instant> attempts = ATTEMPTS.computeIfAbsent(key, ignored -> new ArrayDeque<>());

        synchronized (attempts) {
            while (!attempts.isEmpty() && attempts.peekFirst().isBefore(cutoff)) {
                attempts.removeFirst();
            }

            if (attempts.size() >= policy.getMaxAttempts()) {
                throw AppException.validation(policy.getErrorMessage());
            }

            attempts.addLast(now);
        }
    }
}