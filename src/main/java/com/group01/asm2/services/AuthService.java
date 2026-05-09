package com.group01.asm2.services;

import com.group01.asm2.core.SessionManager;
import com.group01.asm2.enums.UserRole;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.Person;
import com.group01.asm2.models.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class AuthService {
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final Map<String, DemoAccount> demoAccounts = new HashMap<>();

    public AuthService() {
        seedDemoAccounts();
    }

    public Person login(String usernameInput, String passwordInput) {
        if (passwordInput == null || passwordInput.isBlank()) {
            throw AppException.validation("Password is required.");
        }

        DemoAccount demoAccount = demoAccounts.get(usernameInput);

        if (demoAccount == null) {
            throw AppException.authentication("Invalid email or password.");
        }

        // TEMPORARY: plain password check.
        // Later, replace this with PasswordHasher.verify(passwordInput, person.getPasswordHash()).
        if (!demoAccount.rawPassword.equals(passwordInput)) {
            throw AppException.authentication("Invalid email or password.");
        }

        SessionManager.login(demoAccount.person);

        return demoAccount.person;
    }

    public void logout() {
        SessionManager.logout();
    }

    private String normalizeEmail(String emailInput) {
        if (emailInput == null || emailInput.trim().isEmpty()) {
            throw AppException.validation("Email is required.");
        }

        String email = emailInput.trim().toLowerCase();

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw AppException.validation("Please enter a valid email address.");
        }

        return email;
    }

    private void seedDemoAccounts() {
        LocalDateTime now = LocalDateTime.now();

        User buyer = new User(
            1,
            "Buyer Demo",
            LocalDate.of(2000, 1, 1),
            "buyer@bidblitz.com",
            "0900000001",
            "buyer",
            null,
            UserRole.BUYER,
            now,
            now,
            BigDecimal.valueOf(1000),
            4.5,
            0
        );

        User seller = new User(
            2,
            "Seller Demo",
            LocalDate.of(1998, 1, 1),
            "seller@bidblitz.com",
            "0900000002",
            "seller",
            null,
            UserRole.SELLER,
            now,
            now,
            BigDecimal.valueOf(500),
            4.8,
            12
        );

        Person auctionAdmin = new Person(
            3,
            "Auction Admin Demo",
            LocalDate.of(1995, 1, 1),
            "auctionadmin@bidblitz.com",
            "0900000003",
            "auctionadmin",
            null,
            UserRole.AUCTION_ADMINISTRATOR,
            now,
            now
        );

        Person systemAdmin = new Person(
            4,
            "System Admin Demo",
            LocalDate.of(1990, 1, 1),
            "systemadmin@bidblitz.com",
            "0900000004",
            "systemadmin",
            null,
            UserRole.SYSTEM_ADMINISTRATOR,
            now,
            now
        );

        addDemoAccount(buyer, "123456");
        addDemoAccount(seller, "123456");
        addDemoAccount(auctionAdmin, "123456");
        addDemoAccount(systemAdmin, "123456");
    }

    private void addDemoAccount(Person person, String rawPassword) {
        demoAccounts.put(person.getEmail().trim().toLowerCase(), new DemoAccount(person, rawPassword));
    }

    private static class DemoAccount {
        private final Person person;
        private final String rawPassword;

        private DemoAccount(Person person, String rawPassword) {
            this.person = person;
            this.rawPassword = rawPassword;
        }
    }
}