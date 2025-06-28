package com.afb.expensetracker.command.validation;

/**
 * Validator for description strings.
 * <p>
 * Ensures that descriptions are not null or empty after trimming.
 * </p>
 */
public class DescriptionValidator implements ArgumentValidator<String> {

    @Override
    public String validate(String input) throws ValidationException {
        if (input == null || input.trim().isEmpty()) {
            throw new ValidationException("Description cannot be empty");
        }
        return input.trim();
    }

    @Override
    public String getValidationMessage() {
        return "Description cannot be empty";
    }
}
