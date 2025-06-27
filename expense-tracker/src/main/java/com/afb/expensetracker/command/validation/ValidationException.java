package com.afb.expensetracker.command.validation;

/**
 * Exception thrown when command line argument validation fails.
 * <p>
 * This custom exception provides specific error messages for validation
 * failures, helping users understand what went wrong with their input.
 * </p>
 */
public class ValidationException extends Exception {
    /**
     * Constructs a ValidationException with the specified detail message.
     *
     * @param message The detail message explaining the validation failure
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a ValidationException with the specified detail message and cause.
     *
     * @param message The detail message explaining the validation failure
     * @param cause The cause of the validation failure
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
