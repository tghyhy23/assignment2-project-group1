package com.group01.asm2.services;

import com.group01.asm2.core.SessionManager;
import com.group01.asm2.enums.UserRole;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.Person;
import com.group01.asm2.models.User;
import com.group01.asm2.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private FakeUserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        // 1. Prepare fake repository
        userRepository = new FakeUserRepository();

        // 2. Prepare service with fake repository
        userService = new UserService(userRepository);

        // 3. Clear session before each test
        SessionManager.logout();
    }

    @AfterEach
    void tearDown() {
        // 1. Clear session after each test
        SessionManager.logout();
    }

    @Test
    void readUserProfile_whenUserIsNotLoggedIn_shouldThrowException() {
        // 1. Read current profile without login
        AppException exception = assertThrows(
            AppException.class,
            () -> userService.readUserProfile()
        );

        // 2. Assert exception
        assertNotNull(exception.getMessage());
    }

    @Test
    void readUserProfile_whenLoggedInUserExists_shouldReturnFullProfile() {
        // 1. Prepare user
        User user = createUser(1, "Adam User", "adam", "adam@test.com");
        user.setBalance(new BigDecimal("250.00"));
        userRepository.save(user);

        // 2. Login user
        SessionManager.login(user);

        // 3. Read profile
        User result = userService.readUserProfile();

        // 4. Assert full private profile
        assertEquals(1, result.getId());
        assertEquals("Adam User", result.getFullName());
        assertEquals("adam", result.getUsername());
        assertEquals("adam@test.com", result.getEmail());
        assertEquals("+84 912345678", result.getPhone());
        assertEquals(new BigDecimal("250.00"), result.getBalance());
    }

    @Test
    void readUserProfile_whenLoggedInUserDoesNotExistInRepository_shouldThrowException() {
        // 1. Prepare logged-in user but do not save into repository
        User user = createUser(1, "Adam User", "adam", "adam@test.com");
        SessionManager.login(user);

        // 2. Read profile
        AppException exception = assertThrows(
            AppException.class,
            () -> userService.readUserProfile()
        );

        // 3. Assert exception
        assertNotNull(exception.getMessage());
    }

    @Test
    void readUserProfileById_whenAnonymousUserReadsSeller_shouldReturnPublicSafeProfile() {
        // 1. Prepare target user
        User seller = createUser(2, "Seller Full Name", "seller", "seller@test.com");
        seller.setBalance(new BigDecimal("500.00"));
        seller.setRating(4.8);
        seller.setCompletedSalesCount(12);
        userRepository.save(seller);

        // 2. Read profile without login
        User result = userService.readUserProfile(2);

        // 3. Assert only public-safe fields are returned
        assertEquals(2, result.getId());
        assertEquals("seller", result.getUsername());
        assertEquals(4.8, result.getRating());
        assertEquals(12, result.getCompletedSalesCount());

        assertNull(result.getFullName());
        assertNull(result.getDateOfBirth());
        assertNull(result.getEmail());
        assertNull(result.getPhone());
        assertNull(result.getPassword());
        assertEquals(BigDecimal.ZERO, result.getBalance());
    }

    @Test
    void readUserProfileById_whenLoggedInUserReadsAnotherUser_shouldReturnPublicSafeProfile() {
        // 1. Prepare users
        User viewer = createUser(1, "Viewer User", "viewer", "viewer@test.com");
        User target = createUser(2, "Target User", "target", "target@test.com");

        target.setBalance(new BigDecimal("700.00"));
        target.setRating(4.6);
        target.setCompletedSalesCount(8);

        userRepository.save(viewer);
        userRepository.save(target);

        // 2. Login viewer
        SessionManager.login(viewer);

        // 3. Read another user's profile
        User result = userService.readUserProfile(2);

        // 4. Assert public-safe profile
        assertEquals(2, result.getId());
        assertEquals("target", result.getUsername());
        assertEquals(4.6, result.getRating());
        assertEquals(8, result.getCompletedSalesCount());

        assertNull(result.getFullName());
        assertNull(result.getEmail());
        assertNull(result.getPhone());
        assertNull(result.getPassword());
        assertEquals(BigDecimal.ZERO, result.getBalance());
    }

    @Test
    void readUserProfileById_whenOwnerReadsOwnProfile_shouldReturnFullProfile() {
        // 1. Prepare user
        User user = createUser(1, "Adam User", "adam", "adam@test.com");
        user.setBalance(new BigDecimal("300.00"));
        userRepository.save(user);

        // 2. Login user
        SessionManager.login(user);

        // 3. Read own profile by id
        User result = userService.readUserProfile(1);

        // 4. Assert full profile
        assertEquals("Adam User", result.getFullName());
        assertEquals("adam@test.com", result.getEmail());
        assertEquals("+84 912345678", result.getPhone());
        assertEquals(new BigDecimal("300.00"), result.getBalance());
    }

    @Test
    void readUserProfileById_whenSystemAdminReadsUser_shouldReturnFullProfile() {
        // 1. Prepare admin and target user
        Person admin = createPerson(99, "System Admin", "admin", "admin@test.com", UserRole.SYSTEM_ADMINISTRATOR);
        User target = createUser(1, "Adam User", "adam", "adam@test.com");
        target.setBalance(new BigDecimal("400.00"));

        userRepository.save(target);

        // 2. Login system admin
        SessionManager.login(admin);

        // 3. Read target user
        User result = userService.readUserProfile(1);

        // 4. Assert full profile
        assertEquals("Adam User", result.getFullName());
        assertEquals("adam@test.com", result.getEmail());
        assertEquals("+84 912345678", result.getPhone());
        assertEquals(new BigDecimal("400.00"), result.getBalance());
    }

    @Test
    void readUserProfileById_whenUserIdIsNull_shouldThrowException() {
        // 1. Read profile with null id
        AppException exception = assertThrows(
            AppException.class,
            () -> userService.readUserProfile((Integer) null)
        );

        // 2. Assert exception
        assertNotNull(exception.getMessage());
    }

    @Test
    void readUserProfileById_whenUserIdIsZero_shouldThrowException() {
        // 1. Read profile with invalid id
        AppException exception = assertThrows(
            AppException.class,
            () -> userService.readUserProfile(0)
        );

        // 2. Assert exception
        assertNotNull(exception.getMessage());
    }

    @Test
    void readUserProfileById_whenUserIdIsNegative_shouldThrowException() {
        // 1. Read profile with invalid id
        AppException exception = assertThrows(
            AppException.class,
            () -> userService.readUserProfile(-1)
        );

        // 2. Assert exception
        assertNotNull(exception.getMessage());
    }

    @Test
    void readUserProfileById_whenUserDoesNotExist_shouldThrowException() {
        // 1. Read missing user
        AppException exception = assertThrows(
            AppException.class,
            () -> userService.readUserProfile(999)
        );

        // 2. Assert exception
        assertNotNull(exception.getMessage());
    }

    @Test
    void readUsers_whenSystemAdmin_shouldReturnAllUsers() {
        // 1. Prepare admin and users
        Person admin = createPerson(99, "System Admin", "admin", "admin@test.com", UserRole.SYSTEM_ADMINISTRATOR);
        User user1 = createUser(1, "Adam User", "adam", "adam@test.com");
        User user2 = createUser(2, "Bella User", "bella", "bella@test.com");

        userRepository.save(user1);
        userRepository.save(user2);

        // 2. Login system admin
        SessionManager.login(admin);

        // 3. Read users
        List<User> users = userService.readUsers();

        // 4. Assert users
        assertEquals(2, users.size());
    }

    @Test
    void readUsers_whenNormalUser_shouldThrowException() {
        // 1. Prepare normal user
        User user = createUser(1, "Adam User", "adam", "adam@test.com");
        userRepository.save(user);

        // 2. Login normal user
        SessionManager.login(user);

        // 3. Read all users
        AppException exception = assertThrows(
            AppException.class,
            () -> userService.readUsers()
        );

        // 4. Assert exception
        assertNotNull(exception.getMessage());
    }

    @Test
    void readUsers_whenNotLoggedIn_shouldThrowException() {
        // 1. Read all users without login
        AppException exception = assertThrows(
            AppException.class,
            () -> userService.readUsers()
        );

        // 2. Assert exception
        assertNotNull(exception.getMessage());
    }

    @Test
    void updateUserProfile_whenValidData_shouldUpdateCurrentUserProfile() {
        // 1. Prepare user
        User user = createUser(1, "Adam User", "adam", "adam@test.com");
        user.setBalance(new BigDecimal("250.00"));
        userRepository.save(user);

        // 2. Login user
        SessionManager.login(user);

        // 3. Update profile
        User result = userService.updateUserProfile(
            "Adam Updated",
            LocalDate.of(2000, 1, 1),
            "updated@test.com",
            "+84 987654321",
            "adam_updated"
        );

        // 4. Assert updated fields
        assertEquals("Adam Updated", result.getFullName());
        assertEquals(LocalDate.of(2000, 1, 1), result.getDateOfBirth());
        assertEquals("updated@test.com", result.getEmail());
        assertEquals("+84 987654321", result.getPhone());
        assertEquals("adam_updated", result.getUsername());

        // 5. Assert protected fields remain unchanged
        assertEquals(new BigDecimal("250.00"), result.getBalance());
        assertEquals(UserRole.BUYER, result.getRole());
    }

    @Test
    void updateUserProfile_whenInputHasExtraSpaces_shouldTrimAndNormalizeData() {
        // 1. Prepare user
        User user = createUser(1, "Adam User", "adam", "adam@test.com");
        userRepository.save(user);
        SessionManager.login(user);

        // 2. Update profile
        User result = userService.updateUserProfile(
            "  Adam Updated  ",
            LocalDate.of(2000, 1, 1),
            "  UPDATED@TEST.COM  ",
            "  +84 987654321  ",
            "  adam_updated  "
        );

        // 3. Assert normalized data
        assertEquals("Adam Updated", result.getFullName());
        assertEquals("updated@test.com", result.getEmail());
        assertEquals("+84 987654321", result.getPhone());
        assertEquals("adam_updated", result.getUsername());
    }

    @Test
    void updateUserProfile_whenPhoneIsBlank_shouldSavePhoneAsNull() {
        // 1. Prepare user
        User user = createUser(1, "Adam User", "adam", "adam@test.com");
        userRepository.save(user);
        SessionManager.login(user);

        // 2. Update profile with blank phone
        User result = userService.updateUserProfile(
            "Adam Updated",
            LocalDate.of(2000, 1, 1),
            "updated@test.com",
            " ",
            "adam_updated"
        );

        // 3. Assert phone is null
        assertNull(result.getPhone());
    }

    @Test
    void updateUserProfile_whenNotLoggedIn_shouldThrowException() {
        // 1. Update profile without login
        AppException exception = assertThrows(
            AppException.class,
            () -> userService.updateUserProfile(
                "Adam Updated",
                LocalDate.of(2000, 1, 1),
                "updated@test.com",
                "+84 987654321",
                "adam_updated"
            )
        );

        // 2. Assert exception
        assertNotNull(exception.getMessage());
    }

    @Test
    void updateUserProfile_whenTargetUserIdIsInvalid_shouldThrowException() {
        // 1. Prepare user
        User user = createUser(1, "Adam User", "adam", "adam@test.com");
        userRepository.save(user);
        SessionManager.login(user);

        // 2. Update invalid target user
        AppException exception = assertThrows(
            AppException.class,
            () -> userService.updateUserProfile(
                0,
                "Adam Updated",
                LocalDate.of(2000, 1, 1),
                "updated@test.com",
                "+84 987654321",
                "adam_updated"
            )
        );

        // 3. Assert exception
        assertNotNull(exception.getMessage());
    }

    @Test
    void updateUserProfile_whenTargetUserDoesNotExist_shouldThrowException() {
        // 1. Prepare system admin
        Person admin = createPerson(99, "System Admin", "admin", "admin@test.com", UserRole.SYSTEM_ADMINISTRATOR);
        SessionManager.login(admin);

        // 2. Update missing user
        AppException exception = assertThrows(
            AppException.class,
            () -> userService.updateUserProfile(
                999,
                "Adam Updated",
                LocalDate.of(2000, 1, 1),
                "updated@test.com",
                "+84 987654321",
                "adam_updated"
            )
        );

        // 3. Assert exception
        assertNotNull(exception.getMessage());
    }

    @Test
    void updateUserProfile_whenNormalUserUpdatesAnotherUser_shouldThrowException() {
        // 1. Prepare users
        User user1 = createUser(1, "Adam User", "adam", "adam@test.com");
        User user2 = createUser(2, "Bella User", "bella", "bella@test.com");

        userRepository.save(user1);
        userRepository.save(user2);

        // 2. Login first user
        SessionManager.login(user1);

        // 3. Try updating second user
        AppException exception = assertThrows(
            AppException.class,
            () -> userService.updateUserProfile(
                2,
                "Bella Updated",
                LocalDate.of(2000, 1, 1),
                "bella_updated@test.com",
                "+84 987654321",
                "bella_updated"
            )
        );

        // 4. Assert exception
        assertNotNull(exception.getMessage());
    }

    @Test
    void updateUserProfile_whenSystemAdminUpdatesAnotherUser_shouldUpdateProfile() {
        // 1. Prepare admin and target user
        Person admin = createPerson(99, "System Admin", "admin", "admin@test.com", UserRole.SYSTEM_ADMINISTRATOR);
        User target = createUser(1, "Adam User", "adam", "adam@test.com");

        userRepository.save(target);

        // 2. Login system admin
        SessionManager.login(admin);

        // 3. Update target user
        User result = userService.updateUserProfile(
            1,
            "Adam Updated",
            LocalDate.of(2000, 1, 1),
            "updated@test.com",
            "+84 987654321",
            "adam_updated"
        );

        // 4. Assert update
        assertEquals("Adam Updated", result.getFullName());
        assertEquals("updated@test.com", result.getEmail());
        assertEquals("adam_updated", result.getUsername());
    }

    @Test
    void updateUserProfile_whenFullNameIsNull_shouldThrowException() {
        // 1. Prepare user
        User user = createUser(1, "Adam User", "adam", "adam@test.com");
        userRepository.save(user);
        SessionManager.login(user);

        // 2. Update with invalid full name
        assertThrows(AppException.class, () -> userService.updateUserProfile(
            null,
            LocalDate.of(2000, 1, 1),
            "updated@test.com",
            "+84 987654321",
            "adam_updated"
        ));
    }

    @Test
    void updateUserProfile_whenFullNameIsBlank_shouldThrowException() {
        // 1. Prepare user
        User user = createUser(1, "Adam User", "adam", "adam@test.com");
        userRepository.save(user);
        SessionManager.login(user);

        // 2. Update with blank full name
        assertThrows(AppException.class, () -> userService.updateUserProfile(
            " ",
            LocalDate.of(2000, 1, 1),
            "updated@test.com",
            "+84 987654321",
            "adam_updated"
        ));
    }

    @Test
    void updateUserProfile_whenFullNameIsTooLong_shouldThrowException() {
        // 1. Prepare user
        User user = createUser(1, "Adam User", "adam", "adam@test.com");
        userRepository.save(user);
        SessionManager.login(user);

        String longFullName = "A".repeat(121);

        // 2. Update with long full name
        assertThrows(AppException.class, () -> userService.updateUserProfile(
            longFullName,
            LocalDate.of(2000, 1, 1),
            "updated@test.com",
            "+84 987654321",
            "adam_updated"
        ));
    }

    @Test
    void updateUserProfile_whenDateOfBirthIsInFuture_shouldThrowException() {
        // 1. Prepare user
        User user = createUser(1, "Adam User", "adam", "adam@test.com");
        userRepository.save(user);
        SessionManager.login(user);

        // 2. Update with future date of birth
        assertThrows(AppException.class, () -> userService.updateUserProfile(
            "Adam Updated",
            LocalDate.now().plusDays(1),
            "updated@test.com",
            "+84 987654321",
            "adam_updated"
        ));
    }

    @Test
    void updateUserProfile_whenEmailIsNull_shouldThrowException() {
        // 1. Prepare user
        User user = createUser(1, "Adam User", "adam", "adam@test.com");
        userRepository.save(user);
        SessionManager.login(user);

        // 2. Update with null email
        assertThrows(AppException.class, () -> userService.updateUserProfile(
            "Adam Updated",
            LocalDate.of(2000, 1, 1),
            null,
            "+84 987654321",
            "adam_updated"
        ));
    }

    @Test
    void updateUserProfile_whenEmailIsBlank_shouldThrowException() {
        // 1. Prepare user
        User user = createUser(1, "Adam User", "adam", "adam@test.com");
        userRepository.save(user);
        SessionManager.login(user);

        // 2. Update with blank email
        assertThrows(AppException.class, () -> userService.updateUserProfile(
            "Adam Updated",
            LocalDate.of(2000, 1, 1),
            " ",
            "+84 987654321",
            "adam_updated"
        ));
    }

    @Test
    void updateUserProfile_whenEmailFormatIsInvalid_shouldThrowException() {
        // 1. Prepare user
        User user = createUser(1, "Adam User", "adam", "adam@test.com");
        userRepository.save(user);
        SessionManager.login(user);

        // 2. Update with invalid email
        assertThrows(AppException.class, () -> userService.updateUserProfile(
            "Adam Updated",
            LocalDate.of(2000, 1, 1),
            "invalid-email",
            "+84 987654321",
            "adam_updated"
        ));
    }

    @Test
    void updateUserProfile_whenEmailAlreadyExists_shouldThrowException() {
        // 1. Prepare users
        User user1 = createUser(1, "Adam User", "adam", "adam@test.com");
        User user2 = createUser(2, "Bella User", "bella", "bella@test.com");

        userRepository.save(user1);
        userRepository.save(user2);

        // 2. Login first user
        SessionManager.login(user1);

        // 3. Update with duplicated email
        assertThrows(AppException.class, () -> userService.updateUserProfile(
            "Adam Updated",
            LocalDate.of(2000, 1, 1),
            "bella@test.com",
            "+84 987654321",
            "adam_updated"
        ));
    }

    @Test
    void updateUserProfile_whenPhoneFormatIsInvalid_shouldThrowException() {
        // 1. Prepare user
        User user = createUser(1, "Adam User", "adam", "adam@test.com");
        userRepository.save(user);
        SessionManager.login(user);

        // 2. Update with invalid phone
        assertThrows(AppException.class, () -> userService.updateUserProfile(
            "Adam Updated",
            LocalDate.of(2000, 1, 1),
            "updated@test.com",
            "abc-phone",
            "adam_updated"
        ));
    }

    @Test
    void updateUserProfile_whenUsernameIsNull_shouldThrowException() {
        // 1. Prepare user
        User user = createUser(1, "Adam User", "adam", "adam@test.com");
        userRepository.save(user);
        SessionManager.login(user);

        // 2. Update with null username
        assertThrows(AppException.class, () -> userService.updateUserProfile(
            "Adam Updated",
            LocalDate.of(2000, 1, 1),
            "updated@test.com",
            "+84 987654321",
            null
        ));
    }

    @Test
    void updateUserProfile_whenUsernameIsBlank_shouldThrowException() {
        // 1. Prepare user
        User user = createUser(1, "Adam User", "adam", "adam@test.com");
        userRepository.save(user);
        SessionManager.login(user);

        // 2. Update with blank username
        assertThrows(AppException.class, () -> userService.updateUserProfile(
            "Adam Updated",
            LocalDate.of(2000, 1, 1),
            "updated@test.com",
            "+84 987654321",
            " "
        ));
    }

    @Test
    void updateUserProfile_whenUsernameIsTooShort_shouldThrowException() {
        // 1. Prepare user
        User user = createUser(1, "Adam User", "adam", "adam@test.com");
        userRepository.save(user);
        SessionManager.login(user);

        // 2. Update with short username
        assertThrows(AppException.class, () -> userService.updateUserProfile(
            "Adam Updated",
            LocalDate.of(2000, 1, 1),
            "updated@test.com",
            "+84 987654321",
            "ab"
        ));
    }

    @Test
    void updateUserProfile_whenUsernameContainsInvalidCharacters_shouldThrowException() {
        // 1. Prepare user
        User user = createUser(1, "Adam User", "adam", "adam@test.com");
        userRepository.save(user);
        SessionManager.login(user);

        // 2. Update with invalid username
        assertThrows(AppException.class, () -> userService.updateUserProfile(
            "Adam Updated",
            LocalDate.of(2000, 1, 1),
            "updated@test.com",
            "+84 987654321",
            "bad username!"
        ));
    }

    @Test
    void updateUserProfile_whenUsernameAlreadyExists_shouldThrowException() {
        // 1. Prepare users
        User user1 = createUser(1, "Adam User", "adam", "adam@test.com");
        User user2 = createUser(2, "Bella User", "bella", "bella@test.com");

        userRepository.save(user1);
        userRepository.save(user2);

        // 2. Login first user
        SessionManager.login(user1);

        // 3. Update with duplicated username
        assertThrows(AppException.class, () -> userService.updateUserProfile(
            "Adam Updated",
            LocalDate.of(2000, 1, 1),
            "updated@test.com",
            "+84 987654321",
            "bella"
        ));
    }

    @Test
    void deleteUser_whenSystemAdminDeletesExistingUser_shouldReturnOneAndRemoveUser() {
        // 1. Prepare admin and target user
        Person admin = createPerson(99, "System Admin", "admin", "admin@test.com", UserRole.SYSTEM_ADMINISTRATOR);
        User target = createUser(1, "Adam User", "adam", "adam@test.com");

        userRepository.save(target);

        // 2. Login system admin
        SessionManager.login(admin);

        // 3. Delete user
        int result = userService.deleteUser(1);

        // 4. Assert delete result
        assertEquals(1, result);
        assertNull(userRepository.readUserById(1));
    }

    @Test
    void deleteUser_whenNotLoggedIn_shouldThrowException() {
        // 1. Delete without login
        assertThrows(AppException.class, () -> userService.deleteUser(1));
    }

    @Test
    void deleteUser_whenNormalUser_shouldThrowException() {
        // 1. Prepare normal user
        User user = createUser(1, "Adam User", "adam", "adam@test.com");
        userRepository.save(user);

        // 2. Login normal user
        SessionManager.login(user);

        // 3. Delete user
        assertThrows(AppException.class, () -> userService.deleteUser(1));
    }

    @Test
    void deleteUser_whenUserIdIsNull_shouldThrowException() {
        // 1. Prepare admin
        Person admin = createPerson(99, "System Admin", "admin", "admin@test.com", UserRole.SYSTEM_ADMINISTRATOR);
        SessionManager.login(admin);

        // 2. Delete invalid user
        assertThrows(AppException.class, () -> userService.deleteUser(null));
    }

    @Test
    void deleteUser_whenUserIdIsInvalid_shouldThrowException() {
        // 1. Prepare admin
        Person admin = createPerson(99, "System Admin", "admin", "admin@test.com", UserRole.SYSTEM_ADMINISTRATOR);
        SessionManager.login(admin);

        // 2. Delete invalid user
        assertThrows(AppException.class, () -> userService.deleteUser(0));
    }

    @Test
    void deleteUser_whenUserDoesNotExist_shouldThrowException() {
        // 1. Prepare admin
        Person admin = createPerson(99, "System Admin", "admin", "admin@test.com", UserRole.SYSTEM_ADMINISTRATOR);
        SessionManager.login(admin);

        // 2. Delete missing user
        assertThrows(AppException.class, () -> userService.deleteUser(999));
    }

    private User createUser(Integer id, String fullName, String username, String email) {
        return new User(
            id,
            fullName,
            LocalDate.of(2000, 1, 1),
            email,
            "+84 912345678",
            username,
            "password",
            UserRole.BUYER,
            LocalDateTime.now(),
            LocalDateTime.now(),
            BigDecimal.ZERO,
            0.0,
            0
        );
    }

    private Person createPerson(
        Integer id,
        String fullName,
        String username,
        String email,
        UserRole role
    ) {
        return new Person(
            id,
            fullName,
            LocalDate.of(1990, 1, 1),
            email,
            "+84 900000000",
            username,
            "password",
            role,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    private static class FakeUserRepository extends UserRepository {
        private final Map<Integer, User> users = new HashMap<>();

        void save(User user) {
            users.put(user.getId(), user);
        }

        @Override
        public User readUserById(Integer id) {
            return users.get(id);
        }

        @Override
        public List<User> readUsers() {
            return users.values().stream().toList();
        }

        @Override
        public User updateUserProfile(User user) {
            users.put(user.getId(), user);
            return user;
        }

        @Override
        public int deleteUser(Integer userId) {
            return users.remove(userId) == null ? 0 : 1;
        }

        @Override
        public boolean existsByEmailExceptId(String email, Integer excludedUserId) {
            return users.values()
                .stream()
                .anyMatch(user ->
                    user.getEmail().equalsIgnoreCase(email)
                        && !user.getId().equals(excludedUserId)
                );
        }

        @Override
        public boolean existsByUsernameExceptId(String username, Integer excludedUserId) {
            return users.values()
                .stream()
                .anyMatch(user ->
                    user.getUsername().equalsIgnoreCase(username)
                        && !user.getId().equals(excludedUserId)
                );
        }
    }
}