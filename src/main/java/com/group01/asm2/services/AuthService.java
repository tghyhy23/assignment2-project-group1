package com.group01.asm2.services;

import com.group01.asm2.core.SessionManager;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.Person;
import com.group01.asm2.repositories.PersonRepository;
import com.group01.asm2.security.AppActionGuard;
import com.group01.asm2.utils.PasswordHasher;

import java.time.Duration;
import java.util.Optional;

public class AuthService {
    private final PersonRepository personRepository = new PersonRepository();

    public Person login(String usernameInput, String passwordInput) {
        String username = normalizeUsername(usernameInput);

        AppActionGuard.callLimited(
            "login:" + username,
            5,
            Duration.ofMinutes(5),
            () -> null
        );

        if (username.isBlank()) {
            throw AppException.validation("Username is required.");
        }

        if (passwordInput == null || passwordInput.isBlank()) {
            throw AppException.validation("Password is required.");
        }

        Optional<Person> optionalPerson = personRepository.findByUsername(username);

        if (optionalPerson.isEmpty()) {
            throw AppException.authentication("Invalid username or password.");
        }

        Person person = optionalPerson.get();

        if (!PasswordHasher.verify(passwordInput, person.getpassword())) {
            throw AppException.authentication("Invalid username or password.");
        }

        SessionManager.login(person);
        return person;
    }

    private String normalizeUsername(String usernameInput) {
        if (usernameInput == null) {
            return "";
        }

        return usernameInput.trim().toLowerCase();
    }
}