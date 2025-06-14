package client;

/**
 * Custom checked exception for GitHub API errors.
 * Extends Exception to force callers to handle API-specific error conditions.
 * Provides constructors for different error scenarios with and without
 * cause chaining.
 */
public class GitHubApiException extends Exception {
    /**
     * Constructor for creating exception with a descriptive error message.
     * Used for API-specific errors where the root cause is already known.
     *
     * @param message Descriptive error message explaining what went wrong.
     */
    public GitHubApiException(String message) {
        super(message);
    }

    /**
     * Constructor for creating exception with message and cause.
     * Used for wrapping lower-level exceptions (IOExceptions, etc.) while
     * preserving context.
     *
     * @param message Descriptive error message explaining what went wrong.
     * @param cause The underlying cause of this exception.
     */
    public GitHubApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
