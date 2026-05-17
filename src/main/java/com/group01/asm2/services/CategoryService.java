package com.group01.asm2.services;

import com.group01.asm2.constants.ActivityTarget;
import com.group01.asm2.enums.ActivityActionType;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.Category;
import com.group01.asm2.repositories.CategoryRepository;
import com.group01.asm2.security.Permission;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Group 01
 */
public class CategoryService extends BaseService {
    private static final int MAX_NAME_LENGTH = 80;
    private static final int MAX_DESCRIPTION_LENGTH = 500;

    private final CategoryRepository categoryRepository;
    private final ActivityLogService activityLogService;

    public CategoryService() {
        this(new CategoryRepository(), new ActivityLogService());
    }

    public CategoryService(CategoryRepository categoryRepository) {
        this(categoryRepository, new ActivityLogService());
    }

    public CategoryService(CategoryRepository categoryRepository, ActivityLogService activityLogService) {
        this.categoryRepository = categoryRepository;
        this.activityLogService = activityLogService;
    }

    public Category createCategory(String name, String description, BigDecimal commissionRate) {
        // 1. Check current user permission
        requireCurrentUser(Permission.CREATE_CATEGORY);

        // 2. Normalize request data
        String normalizedName = normalizeRequiredText(name, "Category name", MAX_NAME_LENGTH);
        String normalizedDescription = normalizeOptionalText(description, "Category description", MAX_DESCRIPTION_LENGTH);

        // 3. Validate commission rate
        validateCommissionRate(commissionRate);

        // 4. Check duplicated category name
        if (categoryRepository.existsByNameExceptId(normalizedName, null)) {
            throw AppException.conflict("Category name already exists.");
        }

        // 5. Build category model
        Category category = new Category();
        category.setName(normalizedName);
        category.setDescription(normalizedDescription);
        category.setCommissionRate(commissionRate);

        // 6. Save category
        Category createdCategory = categoryRepository.createCategory(category);

        // 7. Create activity log
        activityLogService.createActivityLog(
            ActivityActionType.CREATE_CATEGORY,
            ActivityTarget.CATEGORY,
            createdCategory.getId(),
            "Created category: " + createdCategory.getName() + "."
        );

        return createdCategory;
    }

    public Category readCategory(Integer categoryId) {
        // 1. Validate target category ID
        Integer validCategoryId = validateCategoryId(categoryId);

        // 2. Check existing category
        Category category = categoryRepository.readCategoryById(validCategoryId);
        if (category == null) {
            throw AppException.notFound("Category not found.");
        }

        // 3. Return category
        return category;
    }

    public List<Category> readCategories() {
        // 1. Read all categories
        return categoryRepository.readCategories();
    }

    public Category updateCategory(Integer categoryId, String name, String description, BigDecimal commissionRate) {
        // 1. Check current user permission
        requireCurrentUser(Permission.UPDATE_CATEGORY);

        // 2. Validate target category ID
        Integer validCategoryId = validateCategoryId(categoryId);

        // 3. Check existing category
        Category existingCategory = categoryRepository.readCategoryById(validCategoryId);
        if (existingCategory == null) {
            throw AppException.notFound("Category not found.");
        }

        // 4. Normalize and validate request data
        String normalizedName = normalizeRequiredText(name, "Category name", MAX_NAME_LENGTH);
        String normalizedDescription = normalizeOptionalText(description, "Category description", MAX_DESCRIPTION_LENGTH);
        validateCommissionRate(commissionRate);

        // 5. Check duplicated category name
        if (categoryRepository.existsByNameExceptId(normalizedName, validCategoryId)) {
            throw AppException.conflict("Category name already exists.");
        }

        // 6. Apply allowed category updates
        existingCategory.setName(normalizedName);
        existingCategory.setDescription(normalizedDescription);
        existingCategory.setCommissionRate(commissionRate);

        // 7. Save category
        Category updatedCategory = categoryRepository.updateCategory(existingCategory);
        if (updatedCategory == null) {
            throw AppException.notFound("Category not found.");
        }

        // 8. Create activity log
        activityLogService.createActivityLog(
            ActivityActionType.UPDATE_CATEGORY,
            ActivityTarget.CATEGORY,
            updatedCategory.getId(),
            "Updated category: " + updatedCategory.getName() + "."
        );

        return updatedCategory;
    }

    public void deleteCategory(Integer categoryId) {
        // 1. Check current user permission
        requireCurrentUser(Permission.DELETE_CATEGORY);

        // 2. Validate target category ID
        Integer validCategoryId = validateCategoryId(categoryId);

        // 3. Check existing category
        Category existingCategory = categoryRepository.readCategoryById(validCategoryId);
        if (existingCategory == null) {
            throw AppException.notFound("Category not found.");
        }

        // 4. Prevent deleting category if existing items use it
        if (categoryRepository.isCategoryUsedByItems(validCategoryId)) {
            throw AppException.conflict("Cannot delete category because it is used by existing items.");
        }

        // 5. Delete category
        categoryRepository.deleteCategory(validCategoryId);

        // 6. Create activity log
        activityLogService.createActivityLog(
            ActivityActionType.DELETE_CATEGORY,
            ActivityTarget.CATEGORY,
            validCategoryId,
            "Deleted category: " + existingCategory.getName() + "."
        );
    }

    private Integer validateCategoryId(Integer categoryId) {
        if (categoryId == null || categoryId <= 0) {
            throw AppException.validation("Category ID must be a positive number.");
        }

        return categoryId;
    }

    private String normalizeRequiredText(String value, String fieldName, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            throw AppException.validation(fieldName + " is required.");
        }

        String normalizedValue = value.trim();

        if (normalizedValue.length() > maxLength) {
            throw AppException.validation(fieldName + " must not exceed " + maxLength + " characters.");
        }

        return normalizedValue;
    }

    private String normalizeOptionalText(String value, String fieldName, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String normalizedValue = value.trim();

        if (normalizedValue.length() > maxLength) {
            throw AppException.validation(fieldName + " must not exceed " + maxLength + " characters.");
        }

        return normalizedValue;
    }

    private void validateCommissionRate(BigDecimal commissionRate) {
        if (commissionRate == null) {
            throw AppException.validation("Commission rate is required.");
        }

        if (commissionRate.compareTo(BigDecimal.ZERO) < 0) {
            throw AppException.validation("Commission rate cannot be negative.");
        }

        if (commissionRate.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw AppException.validation("Commission rate cannot exceed 100.");
        }
    }
}