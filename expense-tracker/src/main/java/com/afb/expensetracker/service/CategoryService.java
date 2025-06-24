package com.afb.expensetracker.service;

import com.afb.expensetracker.model.ExpenseCategory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class that provides operations related to expense categories.
 * <p>
 * This service offers methods for retrieving and validating expense categories.
 * </p>
 */
public class CategoryService {
    /**
     * Retrieves all available expense categories.
     *
     * @return A list of all expense categories
     */
    public List<ExpenseCategory> getAllCategories() {
        return Arrays.asList(ExpenseCategory.values());
    }

    /**
     * Retrieves all category names.
     * <p>
     * Returns a list of user-friendly category names for all categories,
     * suitable for presentation in a user interface.
     * </p>
     *
     * @return A list of category display names
     */
    public List<String> getCategoryNames() {
        return getAllCategories().stream()
                .map(ExpenseCategory::getCategoryName)
                .collect(Collectors.toList());
    }

    /**
     * Finds a category by its category name.
     * <p>
     * Attempts to match the provided name with the category name of a category.
     * The matching is case-insensitive.
     * </p>
     *
     * @param categoryName The display name to search for
     * @return The matching category, or null if no match is found
     */
    public ExpenseCategory findCategoryByName(String categoryName) {
        try {
            return ExpenseCategory.fromCategoryName(categoryName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Validates whether a given string is a valid category name.
     * <p>
     * Checks if the provided string matches either an enum name or
     * a category name of any expense category.
     * </p>
     *
     * @param categoryName The category name to validate
     * @return true if the name is valid, false otherwise
     */
    public boolean isValidCategoryName(String categoryName) {
        try {
            ExpenseCategory.fromString(categoryName);
            return true;
        } catch (IllegalArgumentException e1) {
            try {
                ExpenseCategory.fromCategoryName(categoryName);
                return true;
            } catch (IllegalArgumentException e2) {
                return false;
            }
        }
    }
}
