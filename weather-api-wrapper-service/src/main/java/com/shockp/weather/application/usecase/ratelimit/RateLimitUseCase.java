package com.shockp.weather.application.usecase.ratelimit;

import java.util.Objects;
import java.util.regex.Pattern;

import com.shockp.weather.domain.service.RateLimiterService;
import com.shockp.weather.domain.service.WeatherServiceException;

/**
 * Use case for managing rate limiting operations in the weather service.
 * <p>
 * This use case provides a high-level interface for rate limiting operations,
 * including token consumption, availability checking, and limit management.
 * It implements security measures to prevent client ID injection attacks and
 * ensures proper validation of all inputs.
 * </p>
 * <p>
 * The use case follows the single responsibility principle and encapsulates all
 * rate limiting business logic, providing a clean interface for the application layer.
 * Rate limiting is essential for protecting the weather API from abuse and ensuring
 * fair usage among all clients.
 * </p>
 *
 * @author Weather API Wrapper Service
 * @version 1.0
 * @since 1.0
 * @see com.shockp.weather.domain.service.RateLimiterService
 * @see com.shockp.weather.application.usecase.ratelimit.RateLimitOperationException
 */
public final class RateLimitUseCase {

    /** Maximum allowed client ID length to prevent DoS attacks. */
    private static final int MAX_CLIENT_ID_LENGTH = 128;

    /** Minimum required client ID length. */
    private static final int MIN_CLIENT_ID_LENGTH = 1;

    /** Pattern for validating client IDs to prevent injection attacks. */
    private static final Pattern CLIENT_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9:_\\-\\.]+$");

    /** The rate limiter service for rate limiting operations. */
    private final RateLimiterService rateLimiterService;

    /**
     * Constructs a new {@link RateLimitUseCase} with the specified rate limiter service.
     *
     * @param rateLimiterService the rate limiter service for rate limiting operations, must not be {@code null}
     * @throws NullPointerException if {@code rateLimiterService} is {@code null}
     */
    public RateLimitUseCase(RateLimiterService rateLimiterService) {
        this.rateLimiterService = Objects.requireNonNull(rateLimiterService, "Rate limiter service cannot be null");
    }

    /**
     * Executes rate limiting check and token consumption for the specified client.
     * <p>
     * This method validates the client ID, checks if the client has available tokens,
     * and consumes a token if available. This is the primary method for rate limiting
     * enforcement in the weather API.
     * </p>
     *
     * @param clientId the unique client identifier, must not be {@code null} or empty
     * @return {@code true} if a token was successfully consumed, {@code false} if the limit is exceeded
     * @throws IllegalArgumentException if the client ID is invalid or empty
     * @throws RateLimitOperationException if the rate limiting operation fails
     */
    public boolean execute(String clientId) {
        validateAndSanitizeClientId(clientId);

        try {
            return rateLimiterService.consumeToken(clientId);
        } catch (WeatherServiceException e) {
            throw new RateLimitOperationException("Failed to execute rate limiting for client: " + clientId, e);
        }
    }

    /**
     * Checks if the specified client has available rate limit tokens without consuming them.
     * <p>
     * This method validates the client ID and checks the current rate limit status
     * without modifying the token count. Useful for monitoring and status checks.
     * </p>
     *
     * @param clientId the unique client identifier, must not be {@code null} or empty
     * @return {@code true} if the client has available tokens, {@code false} otherwise
     * @throws IllegalArgumentException if the client ID is invalid or empty
     * @throws RateLimitOperationException if the rate limit check operation fails
     */
    public boolean checkRateLimit(String clientId) {
        validateAndSanitizeClientId(clientId);

        try {
            return rateLimiterService.checkRateLimit(clientId);
        } catch (WeatherServiceException e) {
            throw new RateLimitOperationException("Failed to check rate limit for client: " + clientId, e);
        }
    }

    /**
     * Gets the number of remaining tokens for the specified client.
     * <p>
     * This method validates the client ID and returns the current number of
     * available tokens for the client without consuming any tokens.
     * </p>
     *
     * @param clientId the unique client identifier, must not be {@code null} or empty
     * @return the number of remaining tokens for the client
     * @throws IllegalArgumentException if the client ID is invalid or empty
     * @throws RateLimitOperationException if the token retrieval operation fails
     */
    public int getRemainingTokens(String clientId) {
        validateAndSanitizeClientId(clientId);

        try {
            return rateLimiterService.getRemainingTokens(clientId);
        } catch (WeatherServiceException e) {
            throw new RateLimitOperationException("Failed to get remaining tokens for client: " + clientId, e);
        }
    }

    /**
     * Resets the rate limit for the specified client.
     * <p>
     * This method validates the client ID and resets the client's rate limit counter,
     * allowing them to make requests up to the maximum limit again. This operation
     * should be used with caution and typically only by administrators or for
     * emergency situations.
     * </p>
     *
     * @param clientId the unique client identifier, must not be {@code null} or empty
     * @throws IllegalArgumentException if the client ID is invalid or empty
     * @throws RateLimitOperationException if the rate limit reset operation fails
     */
    public void resetLimit(String clientId) {
        validateAndSanitizeClientId(clientId);

        try {
            rateLimiterService.reset(clientId);
        } catch (WeatherServiceException e) {
            throw new RateLimitOperationException("Failed to reset rate limit for client: " + clientId, e);
        }
    }

    /**
     * Gets the maximum number of requests allowed per time window.
     * <p>
     * This method returns the configured maximum number of requests that any client
     * can make within the rate limiting time window.
     * </p>
     *
     * @return the maximum number of requests allowed per time window
     */
    public int getMaxRequests() {
        return rateLimiterService.getMaxRequests();
    }

    /**
     * Gets the time window duration for rate limiting.
     * <p>
     * This method returns the configured time window duration used for rate limiting
     * calculations.
     * </p>
     *
     * @return the time window duration for rate limiting
     */
    public java.time.Duration getTimeWindow() {
        return rateLimiterService.getTimeWindow();
    }

    /**
     * Validates and sanitizes the client ID.
     * <p>
     * This method performs comprehensive validation of the client ID:
     * <ul>
     *   <li>Ensures the client ID is not {@code null} or empty</li>
     *   <li>Validates client ID length is within acceptable bounds</li>
     *   <li>Checks client ID format to prevent injection attacks</li>
     *   <li>Trims whitespace from the client ID</li>
     * </ul>
     * </p>
     *
     * @param clientId the client ID to validate and sanitize
     * @throws IllegalArgumentException if the client ID is invalid
     */
    private void validateAndSanitizeClientId(String clientId) {
        if (clientId == null) {
            throw new IllegalArgumentException("Client ID cannot be null");
        }

        String sanitizedClientId = clientId.trim();
        
        if (sanitizedClientId.isEmpty()) {
            throw new IllegalArgumentException("Client ID cannot be empty");
        }

        if (sanitizedClientId.length() < MIN_CLIENT_ID_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Client ID must be at least %d characters long", MIN_CLIENT_ID_LENGTH));
        }

        if (sanitizedClientId.length() > MAX_CLIENT_ID_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Client ID cannot exceed %d characters", MAX_CLIENT_ID_LENGTH));
        }

        if (!CLIENT_ID_PATTERN.matcher(sanitizedClientId).matches()) {
            throw new IllegalArgumentException(
                "Client ID contains invalid characters. Only alphanumeric, colon, underscore, hyphen, and dot characters are allowed");
        }
    }
} 