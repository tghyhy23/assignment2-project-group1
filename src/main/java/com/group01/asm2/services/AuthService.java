package com.group01.asm2.services;

import com.group01.asm2.core.SessionManager;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.Person;
import com.group01.asm2.repositories.PersonRepository;
import com.group01.asm2.utils.PasswordHasher;

public class AuthService {
    private final PersonRepository personRepository = new PersonRepository();

    public Person login(String usernameInput, String passwordInput) {
        String username = normalizeUsername(usernameInput);

        if (passwordInput == null || passwordInput.isBlank()) {
            throw AppException.validation("Password is required.");
        }

        Person person = personRepository
            .findByUsername(username)
            .orElseThrow(() -> AppException.authentication("Invalid username or password."));

        boolean passwordMatches = PasswordHasher.verify(passwordInput, person.getpassword());

        if (!passwordMatches) {
            throw AppException.authentication("Invalid username or password.");
        }

        SessionManager.login(person);

        return person;
    }

    public void logout() {
        SessionManager.logout();
    }

    private String normalizeUsername(String usernameInput) {
        if (usernameInput == null || usernameInput.trim().isEmpty()) {
            throw AppException.validation("Username is required.");
        }

        return usernameInput.trim().toLowerCase();
    }
}