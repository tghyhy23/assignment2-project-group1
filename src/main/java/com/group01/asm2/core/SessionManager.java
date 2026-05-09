package com.group01.asm2.core;
import com.group01.asm2.models.Person;

public final class SessionManager {
    private static Person currentUser;

    private SessionManager() {
    }

    public static void login(Person person) {
        currentUser = person;
    }

    public static void logout() {
        currentUser = null;
    }

    public static Person getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static Integer getCurrentUserId() {
        return currentUser == null ? null : currentUser.getId();
    }
}