package com.afb.expensetracker.command.validation;

/**
 * Validator for expense IDs.
 * <p>
 * Ensures that ID strings can be parsed as positive integers.
 * </p>
 */
public class IdValidator implements ArgumentValidator<Integer> {

    @Override
    public Integer validate(String input) throws ValidationException {
        if (input == null || input.trim().isEmpty()) {
            throw new ValidationException("ID cannot be empty");
        }

        try {
            int id = Integer.parseInt(input.trim());
            if (id <= 0) {
                throw new ValidationException("ID must be a positive integer");
            }
            return id;
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid ID format: " + e.getMessage());
        }
    }

    @Override
    public String getValidationMessage() {
        return "Invalid ID format. Use a positive integer";
    }
}
