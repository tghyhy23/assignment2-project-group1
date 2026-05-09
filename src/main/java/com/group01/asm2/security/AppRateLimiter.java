package com.group01.asm2.security;

import com.group01.asm2.exceptions.AppException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AppRateLimiter {
    private final Map<String, Deque<Instant>> actionHistory = new ConcurrentHashMap<>();

    public void check(String key, int maxActions, Duration duration) {
        Instant now = Instant.now();
        Instant windowStart = now.minus(duration);

        Deque<Instant> attempts = actionHistory.computeIfAbsent(key, ignored -> new ArrayDeque<>());

        synchronized (attempts) {
            while (!attempts.isEmpty() && attempts.peekFirst().isBefore(windowStart)) {
                attempts.removeFirst();
            }

            if (attempts.size() >= maxActions) {
                throw AppException.rateLimit("Too many attempts. Please try again later.");
            }

            attempts.addLast(now);
        }
    }
}