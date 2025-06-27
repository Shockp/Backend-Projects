package com.afb.expensetracker.command.validation;

/**
 * Interface for validating command line arguments.
 * <p>
 * Implementations of this interface provide specific validation logic
 * for different types of arguments (amounts, IDs, months, etc.).
 * </p>
 *
 * @param <T> The type of the validated result
 */
public interface ArgumentValidator<T> {

    /**
     * Validates the given input string and returns the parsed result.
     *
     * @param input The input string to validate
     * @return The validated and parsed result
     * @throws ValidationException If the input is invalid
     */
    T validate(String input) throws ValidationException;

    /**
     * Returns a human-readable error message for validation failures.
     *
     * @return The validation error message
     */
    String getValidationMessage();
}
