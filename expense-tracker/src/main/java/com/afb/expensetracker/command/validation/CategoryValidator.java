package com.afb.expensetracker.command.validation;

import com.afb.expensetracker.model.ExpenseCategory;

/**
 * Validator for expense categories.
 * <p>
 * Ensures that category strings match valid ExpenseCategory values,
 * supporting both enum names and display names.
 * </p>
 */
public class CategoryValidator implements ArgumentValidator<ExpenseCategory> {

    @Override
    public ExpenseCategory validate(String input) throws ValidationException {
        if (input == null || input.trim().isEmpty()) {
            throw new ValidationException("Category cannot be empty");
        }

        try {
            return ExpenseCategory.fromString(input.trim());
        } catch (IllegalArgumentException e1) {
            try {
                return ExpenseCategory.fromCategoryName(input.trim());
            } catch (IllegalArgumentException e2) {
                throw new ValidationException("Invalid category format: " + e2.getMessage());
            }
        }
    }

    @Override
    public String getValidationMessage() {
        return "Invalid category format. Use either an enum name or a category display name";
    }
}
