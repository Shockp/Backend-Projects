package service;

/**
 * Custom exception for GitHub service layer errors.
 */
public class GitHubServiceException extends Exception {
    /**
     * Constructor for creating exceptions with a descriptive error message.
     * Used for service-specific errors where the root cause is known.
     *
     * @param message Descriptive error message.
     */
    public GitHubServiceException(String message) {
        super(message);
    }

    /**
     * Constructor for creating exception with a message and underlying cause.
     * Used for wrapping lower-level exceptions while preserving error context.
     *
     * @param message Descriptive error message.
     * @param cause Underlying exception that caused this error.
     */
    public GitHubServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
