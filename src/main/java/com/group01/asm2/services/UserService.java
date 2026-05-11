package com.group01.asm2.services;

/**
 * @author Group 01
 */

import com.group01.asm2.core.SessionManager;
import com.group01.asm2.enums.UserRole;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.Person;
import com.group01.asm2.models.User;
import com.group01.asm2.repositories.UserRepository;
import com.group01.asm2.security.Permission;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

public class UserService extends BaseService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[A-Za-z0-9_]{3,80}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[0-9+()\\-\\s]{8,30}$"
    );

    private final UserRepository userRepository;

    public UserService() {
        this(new UserRepository());
    }

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User readUserProfile() {
        // 1. Read current logged-in user profile
        return readUserProfile(null);
    }

    public User readUserProfile(Integer userId) {
        // 1. Prepare current user and target user id
        Person currentUser = SessionManager.getCurrentUser();
        Integer targetUserId = userId;

        if (targetUserId == null) {
            currentUser = getCurrentUserOrThrow();
            targetUserId = currentUser.getId();
        }

        if (targetUserId == null || targetUserId <= 0) {
            throw AppException.validation("User ID is invalid.");
        }

        // 2. Read target user profile
        User user = userRepository.readUserById(targetUserId);

        if (user == null) {
            throw AppException.notFound("User profile not found.");
        }

        // 3. Return full profile if current user owns this profile
        if (currentUser != null && targetUserId.equals(currentUser.getId())) {
            return user;
        }

        // 4. Return full profile if current user has user-management permission
        if (
            currentUser != null &&
                (
                    currentUser.isSystemAdministrator() ||
                        AuthorizationService.hasPermission(Permission.MANAGE_USERS)
                )
        ) {
            return user;
        }

        // 5. Return public-safe profile for anonymous users or non-owners
        return toPublicUserProfile(user);
    }

    public List<User> readUsers() {
        // 1. Check authorization
        requireCurrentUser(Permission.MANAGE_USERS);

        // 2. Read all registered users
        return userRepository.readUsers();
    }

    public User updateUserProfile(
        Integer userId,
        String fullName,
        LocalDate dateOfBirth,
        String email,
        String phone,
        String username
    ) {
        // 1. Check current user and target profile
        Person currentUser = getCurrentUserOrThrow();
        Integer targetUserId = userId != null ? userId : currentUser.getId();

        if (targetUserId == null || targetUserId <= 0) {
            throw AppException.validation("User ID is invalid.");
        }

        // 2. Check ownership or admin permission
        requireOwnershipOrPermission(
            targetUserId,
            Permission.MANAGE_USERS,
            "You can only update your own profile."
        );

        // 3. Check existing user profile
        User existingUser = userRepository.readUserById(targetUserId);

        if (existingUser == null) {
            throw AppException.notFound("User profile not found.");
        }

        // 4. Validate request data
        String cleanFullName = validateFullName(fullName);
        validateDateOfBirth(dateOfBirth);
        String cleanEmail = validateEmail(email);
        String cleanPhone = validatePhone(phone);
        String cleanUsername = validateUsername(username);

        // 5. Check duplicated email and username
        if (userRepository.existsByEmailExceptId(cleanEmail, targetUserId)) {
            throw AppException.conflict("Email is already used by another account.");
        }

        if (userRepository.existsByUsernameExceptId(cleanUsername, targetUserId)) {
            throw AppException.conflict("Username is already used by another account.");
        }

        // 6. Apply allowed profile updates only
        existingUser.setFullName(cleanFullName);
        existingUser.setDateOfBirth(dateOfBirth);
        existingUser.setEmail(cleanEmail);
        existingUser.setPhone(cleanPhone);
        existingUser.setUsername(cleanUsername);

        // 7. Save profile
        return userRepository.updateUserProfile(existingUser);
    }

    public User updateUserProfile(
        String fullName,
        LocalDate dateOfBirth,
        String email,
        String phone,
        String username
    ) {
        // 1. Update current logged-in user profile
        return updateUserProfile(null, fullName, dateOfBirth, email, phone, username);
    }

    public int deleteUser(Integer userId) {
        // 1. Check authorization
        requireCurrentUser(Permission.MANAGE_USERS);

        // 2. Validate user id
        if (userId == null || userId <= 0) {
            throw AppException.validation("User ID is invalid.");
        }

        // 3. Check existing user
        User existingUser = userRepository.readUserById(userId);

        if (existingUser == null) {
            throw AppException.notFound("User profile not found.");
        }

        // 4. Delete user
        return userRepository.deleteUser(userId);
    }

    private User toPublicUserProfile(User user) {
        return new User(
            user.getId(),
            null,
            null,
            null,
            null,
            user.getUsername(),
            null,
            user.getRole(),
            null,
            null,
            BigDecimal.ZERO,
            user.getRating(),
            user.getCompletedSalesCount()
        );
    }

    private UserRole validateRegisteredUserRole(UserRole role) {
        if (role == null) {
            return UserRole.BUYER;
        }

        if (role != UserRole.BUYER && role != UserRole.SELLER) {
            throw AppException.validation("User role must be BUYER or SELLER.");
        }

        return role;
    }

    private String validateFullName(String fullName) {
        String cleanFullName = normalizeRequiredText(fullName, "Full name is required.");

        if (cleanFullName.length() > 120) {
            throw AppException.validation("Full name must not exceed 120 characters.");
        }

        return cleanFullName;
    }

    private void validateDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth != null && dateOfBirth.isAfter(LocalDate.now())) {
            throw AppException.validation("Date of birth cannot be in the future.");
        }
    }

    private String validateEmail(String email) {
        String cleanEmail = normalizeRequiredText(email, "Email is required.").toLowerCase();

        if (!EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            throw AppException.validation("Email format is invalid.");
        }

        return cleanEmail;
    }

    private String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }

        String cleanPhone = phone.trim();

        if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
            throw AppException.validation("Phone number format is invalid.");
        }

        return cleanPhone;
    }

    private String validateUsername(String username) {
        String cleanUsername = normalizeRequiredText(username, "Username is required.");

        if (!USERNAME_PATTERN.matcher(cleanUsername).matches()) {
            throw AppException.validation(
                "Username must be 3-80 characters and contain only letters, numbers, or underscores."
            );
        }

        return cleanUsername;
    }

    private String validatePassword(String password) {
        String cleanPassword = normalizeRequiredText(password, "Password is required.");

        if (cleanPassword.length() < 6) {
            throw AppException.validation("Password must contain at least 6 characters.");
        }

        return cleanPassword;
    }

    private BigDecimal validateBalance(BigDecimal balance) {
        if (balance == null) {
            return BigDecimal.ZERO;
        }

        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw AppException.validation("Balance cannot be negative.");
        }

        return balance;
    }

    private String normalizeRequiredText(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw AppException.validation(errorMessage);
        }

        return value.trim();
    }
}