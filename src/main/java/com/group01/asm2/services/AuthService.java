package com.group01.asm2.services;

import com.group01.asm2.core.SessionManager;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.Person;
import com.group01.asm2.repositories.PersonRepository;
import com.group01.asm2.utils.InputValidator;
import com.group01.asm2.utils.PasswordHasher;

public class AuthService {
    private final PersonRepository personRepository = new PersonRepository();

    public Person login(String usernameInput, String passwordInput) {
        String username = InputValidator.requireUsername(usernameInput);
        String password = InputValidator.requirePassword(passwordInput);

        Person person = personRepository.findByUsername(username)
            .orElseThrow(() -> AppException.authentication("Invalid username or password."));

        if (!PasswordHasher.verify(password, person.getPassword())) {
            throw AppException.authentication("Invalid username or password.");
        }

        SessionManager.login(person);
        return person;
    }
}