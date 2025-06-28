package com.afb.expensetracker.command.validation;

/**
 * Validator for month values.
 * <p>
 * Ensures that month strings can be parsed as integers between 1 and 12.
 * </p>
 */
public class MonthValidator implements ArgumentValidator<Integer> {

    @Override
    public Integer validate(String input) throws ValidationException {
        if (input == null || input.trim().isEmpty()) {
            throw new ValidationException("Month cannot be empty");
        }

        try {
            int month = Integer.parseInt(input.trim());
            if (month < 1 || month > 12) {
                throw new ValidationException("Month must be a number between 1 and 12");
            }
            return month;
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid month format: " + e.getMessage());
        }
    }

    @Override
    public String getValidationMessage() {
        return "Invalid month format. Use a number between 1 and 12";
    }
}
