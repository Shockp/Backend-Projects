package com.afb.expensetracker.command.validation;

import java.math.BigDecimal;

/**
 * Validator for monetary amounts.
 * <p>
 * Ensures that amount strings can be parsed as valid BigDecimal values
 * and are not negative.
 * </p>
 */
public class AmountValidator implements ArgumentValidator<BigDecimal> {

    @Override
    public BigDecimal validate(String input) throws ValidationException {
        if (input == null || input.trim().isEmpty()) {
            throw new ValidationException("Amount cannot be empty");
        }

        try {
            BigDecimal amount = new BigDecimal(input.trim());
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("Amount cannot be negative");
            }
            return amount;
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid amount format: " + e.getMessage());
        }
    }

    @Override
    public String getValidationMessage() {
        return "Invalid amount format. Use decimal format like 123.45";
    }
}
