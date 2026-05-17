package com.group01.asm2.services;

import com.group01.asm2.constants.ActivityTarget;
import com.group01.asm2.core.SessionManager;
import com.group01.asm2.enums.ActivityActionType;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.Person;
import com.group01.asm2.repositories.PersonRepository;
import com.group01.asm2.utils.InputValidator;
import com.group01.asm2.utils.PasswordHasher;

/**
 * @author Group 01
 */

public class AuthService {
    private final PersonRepository personRepository;
    private final ActivityLogService activityLogService;

    public AuthService() {
        this(new PersonRepository(), new ActivityLogService());
    }

    public AuthService(PersonRepository personRepository, ActivityLogService activityLogService) {
        this.personRepository = personRepository;
        this.activityLogService = activityLogService;
    }

    public Person login(String usernameInput, String passwordInput) {
        // 1. Validate input
        String username = InputValidator.requireUsername(usernameInput);
        String password = InputValidator.requirePassword(passwordInput);

        // 2. Find account
        Person person = personRepository.findByUsername(username)
            .orElseThrow(() -> AppException.authentication("Invalid username or password."));

        // 3. Verify password
        if (!PasswordHasher.verify(password, person.getPassword())) {
            throw AppException.authentication("Invalid username or password.");
        }

        // 4. Save logged-in user to session
        SessionManager.login(person);

        // 5. Write activity log after login succeeds
        activityLogService.createActivityLog(
            ActivityActionType.LOGIN,
            ActivityTarget.SESSION,
            person.getId(),
            "Logged in successfully."
        );

        return person;
    }

    public void logout() {
        // 1. Log before clearing session
        Person currentUser = SessionManager.getCurrentUser();

        if (currentUser != null) {
            activityLogService.createActivityLog(
                ActivityActionType.LOGOUT,
                ActivityTarget.SESSION,
                currentUser.getId(),
                "Logged out successfully."
            );
        }

        // 2. Clear session
        SessionManager.logout();
    }
}