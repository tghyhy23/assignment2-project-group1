package com.group01.asm2.services;

import com.group01.asm2.core.SessionManager;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.Category;
import com.group01.asm2.models.Person;
import com.group01.asm2.models.User;
import com.group01.asm2.enums.UserRole;
import com.group01.asm2.repositories.CategoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Group 01
 */
class CategoryServiceTest {
    private FakeCategoryRepository categoryRepository;
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryRepository = new FakeCategoryRepository();
        categoryService = new CategoryService(categoryRepository);

        categoryRepository.seed(new Category(
            1,
            "Electronics",
            "Electronic items",
            new BigDecimal("5.00"),
            LocalDateTime.now(),
            LocalDateTime.now()
        ));

        categoryRepository.seed(new Category(
            2,
            "Books",
            "Books and study materials",
            new BigDecimal("3.50"),
            LocalDateTime.now(),
            LocalDateTime.now()
        ));
    }

    @AfterEach
    void tearDown() {
        SessionManager.logout();
    }

    @Test
    void readCategories_anonymousUser_success() {
        List<Category> categories = categoryService.readCategories();

        assertEquals(2, categories.size());
    }

    @Test
    void readCategory_validId_anonymousUser_success() {
        Category category = categoryService.readCategory(1);

        assertEquals("Electronics", category.getName());
        assertEquals(new BigDecimal("5.00"), category.getCommissionRate());
    }

    @Test
    void readCategory_invalidId_throwsException() {
        assertThrows(AppException.class, () -> categoryService.readCategory(null));
        assertThrows(AppException.class, () -> categoryService.readCategory(0));
        assertThrows(AppException.class, () -> categoryService.readCategory(-1));
    }

    @Test
    void readCategory_missingCategory_throwsException() {
        assertThrows(AppException.class, () -> categoryService.readCategory(999));
    }

    @Test
    void createCategory_systemAdmin_success() {
        loginAsSystemAdmin();

        Category createdCategory = categoryService.createCategory(
            "  Fashion  ",
            "  Clothes and accessories  ",
            new BigDecimal("7.25")
        );

        assertNotNull(createdCategory.getId());
        assertEquals("Fashion", createdCategory.getName());
        assertEquals("Clothes and accessories", createdCategory.getDescription());
        assertEquals(new BigDecimal("7.25"), createdCategory.getCommissionRate());
    }

    @Test
    void createCategory_blankDescription_savedAsNull() {
        loginAsSystemAdmin();

        Category createdCategory = categoryService.createCategory(
            "Furniture",
            "   ",
            new BigDecimal("4.00")
        );

        assertNull(createdCategory.getDescription());
    }

    @Test
    void createCategory_notLoggedIn_throwsException() {
        assertThrows(AppException.class, () -> categoryService.createCategory(
            "Fashion",
            "Clothes",
            new BigDecimal("5.00")
        ));
    }

    @Test
    void createCategory_normalUser_throwsException() {
        loginAsBuyer();

        assertThrows(AppException.class, () -> categoryService.createCategory(
            "Fashion",
            "Clothes",
            new BigDecimal("5.00")
        ));
    }

    @Test
    void createCategory_duplicateName_throwsException() {
        loginAsSystemAdmin();

        assertThrows(AppException.class, () -> categoryService.createCategory(
            "electronics",
            "Duplicate electronics category",
            new BigDecimal("5.00")
        ));
    }

    @Test
    void createCategory_invalidName_throwsException() {
        loginAsSystemAdmin();

        assertThrows(AppException.class, () -> categoryService.createCategory(
            "",
            "Description",
            new BigDecimal("5.00")
        ));

        assertThrows(AppException.class, () -> categoryService.createCategory(
            null,
            "Description",
            new BigDecimal("5.00")
        ));
    }

    @Test
    void createCategory_invalidCommissionRate_throwsException() {
        loginAsSystemAdmin();

        assertThrows(AppException.class, () -> categoryService.createCategory(
            "Valid name",
            "Description",
            null
        ));

        assertThrows(AppException.class, () -> categoryService.createCategory(
            "Valid name",
            "Description",
            new BigDecimal("-1.00")
        ));

        assertThrows(AppException.class, () -> categoryService.createCategory(
            "Valid name",
            "Description",
            new BigDecimal("101.00")
        ));
    }

    @Test
    void updateCategory_systemAdmin_success() {
        loginAsSystemAdmin();

        Category updatedCategory = categoryService.updateCategory(
            1,
            "  Updated Electronics  ",
            "  Updated description  ",
            new BigDecimal("6.50")
        );

        assertEquals(1, updatedCategory.getId());
        assertEquals("Updated Electronics", updatedCategory.getName());
        assertEquals("Updated description", updatedCategory.getDescription());
        assertEquals(new BigDecimal("6.50"), updatedCategory.getCommissionRate());
    }

    @Test
    void updateCategory_blankDescription_savedAsNull() {
        loginAsSystemAdmin();

        Category updatedCategory = categoryService.updateCategory(
            1,
            "Updated Electronics",
            "   ",
            new BigDecimal("6.50")
        );

        assertNull(updatedCategory.getDescription());
    }

    @Test
    void updateCategory_duplicateName_throwsException() {
        loginAsSystemAdmin();

        assertThrows(AppException.class, () -> categoryService.updateCategory(
            1,
            "Books",
            "Duplicate name",
            new BigDecimal("6.00")
        ));
    }

    @Test
    void updateCategory_notLoggedIn_throwsException() {
        assertThrows(AppException.class, () -> categoryService.updateCategory(
            1,
            "Updated Electronics",
            "Updated description",
            new BigDecimal("6.00")
        ));
    }

    @Test
    void updateCategory_normalUser_throwsException() {
        loginAsBuyer();

        assertThrows(AppException.class, () -> categoryService.updateCategory(
            1,
            "Updated Electronics",
            "Updated description",
            new BigDecimal("6.00")
        ));
    }

    @Test
    void updateCategory_invalidId_throwsException() {
        loginAsSystemAdmin();

        assertThrows(AppException.class, () -> categoryService.updateCategory(
            null,
            "Updated Electronics",
            "Updated description",
            new BigDecimal("6.00")
        ));

        assertThrows(AppException.class, () -> categoryService.updateCategory(
            0,
            "Updated Electronics",
            "Updated description",
            new BigDecimal("6.00")
        ));

        assertThrows(AppException.class, () -> categoryService.updateCategory(
            -1,
            "Updated Electronics",
            "Updated description",
            new BigDecimal("6.00")
        ));
    }

    @Test
    void updateCategory_missingCategory_throwsException() {
        loginAsSystemAdmin();

        assertThrows(AppException.class, () -> categoryService.updateCategory(
            999,
            "Missing",
            "Missing category",
            new BigDecimal("5.00")
        ));
    }

    @Test
    void deleteCategory_systemAdmin_success() {
        loginAsSystemAdmin();

        categoryService.deleteCategory(2);

        assertNull(categoryRepository.readCategoryById(2));
    }

    @Test
    void deleteCategory_notLoggedIn_throwsException() {
        assertThrows(AppException.class, () -> categoryService.deleteCategory(1));
    }

    @Test
    void deleteCategory_normalUser_throwsException() {
        loginAsBuyer();

        assertThrows(AppException.class, () -> categoryService.deleteCategory(1));
    }

    @Test
    void deleteCategory_invalidId_throwsException() {
        loginAsSystemAdmin();

        assertThrows(AppException.class, () -> categoryService.deleteCategory(null));
        assertThrows(AppException.class, () -> categoryService.deleteCategory(0));
        assertThrows(AppException.class, () -> categoryService.deleteCategory(-1));
    }

    @Test
    void deleteCategory_missingCategory_throwsException() {
        loginAsSystemAdmin();

        assertThrows(AppException.class, () -> categoryService.deleteCategory(999));
    }

    @Test
    void deleteCategory_usedByItem_throwsException() {
        loginAsSystemAdmin();
        categoryRepository.markUsedByItem(1);

        assertThrows(AppException.class, () -> categoryService.deleteCategory(1));
        assertNotNull(categoryRepository.readCategoryById(1));
    }

    private void loginAsSystemAdmin() {
        Person admin = new Person();

        admin.setId(100);
        admin.setFullName("System Admin");
        admin.setUsername("systemadmin");
        admin.setEmail("admin@bidblitz.com");
        admin.setRole(UserRole.SYSTEM_ADMINISTRATOR);

        SessionManager.login(admin);
    }

    private void loginAsBuyer() {
        User user = new User();
        user.setId(200);
        user.setUsername("buyer");
        user.setRole(UserRole.BUYER);

        SessionManager.login(user);
    }

    private static class FakeCategoryRepository extends CategoryRepository {
        private final Map<Integer, Category> categories = new LinkedHashMap<>();
        private final Set<Integer> usedCategoryIds = new HashSet<>();
        private int nextId = 1;

        void seed(Category category) {
            Category copiedCategory = copy(category);

            categories.put(copiedCategory.getId(), copiedCategory);

            if (copiedCategory.getId() >= nextId) {
                nextId = copiedCategory.getId() + 1;
            }
        }

        void markUsedByItem(Integer categoryId) {
            usedCategoryIds.add(categoryId);
        }

        @Override
        public Category createCategory(Category category) {
            Category copiedCategory = copy(category);

            copiedCategory.setId(nextId++);
            copiedCategory.setCreatedAt(LocalDateTime.now());
            copiedCategory.setUpdatedAt(LocalDateTime.now());

            categories.put(copiedCategory.getId(), copiedCategory);

            return copy(copiedCategory);
        }

        @Override
        public Category readCategoryById(Integer id) {
            Category category = categories.get(id);

            if (category == null) {
                return null;
            }

            return copy(category);
        }

        @Override
        public List<Category> readCategories() {
            List<Category> copiedCategories = new ArrayList<>();

            for (Category category : categories.values()) {
                copiedCategories.add(copy(category));
            }

            return copiedCategories;
        }

        @Override
        public Category updateCategory(Category category) {
            if (!categories.containsKey(category.getId())) {
                return null;
            }

            Category copiedCategory = copy(category);
            copiedCategory.setUpdatedAt(LocalDateTime.now());

            categories.put(copiedCategory.getId(), copiedCategory);

            return copy(copiedCategory);
        }

        @Override
        public void deleteCategory(Integer id) {
            categories.remove(id);
        }

        @Override
        public boolean existsByNameExceptId(String name, Integer excludedCategoryId) {
            for (Category category : categories.values()) {
                boolean sameName = category.getName().equalsIgnoreCase(name);
                boolean sameId = Objects.equals(category.getId(), excludedCategoryId);

                if (sameName && !sameId) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean isCategoryUsedByItems(Integer categoryId) {
            return usedCategoryIds.contains(categoryId);
        }

        private static Category copy(Category category) {
            if (category == null) {
                return null;
            }

            return new Category(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getCommissionRate(),
                category.getCreatedAt(),
                category.getUpdatedAt()
            );
        }
    }
}