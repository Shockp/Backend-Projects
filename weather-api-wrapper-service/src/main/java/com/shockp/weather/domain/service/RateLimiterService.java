package com.shockp.weather.domain.service;

import java.time.Duration;
import java.util.Objects;

import com.shockp.weather.application.port.RateLimiterPort;

/**
 * Domain service for managing rate limiting operations in the weather service.
 * <p>
 * This service provides a high-level interface for rate limiting, including token
 * consumption, availability checking, and limit management. It follows the domain
 * service pattern and uses the {@link RateLimiterPort} for actual rate limiting operations.
 * </p>
 *
 * @author Weather API Wrapper Service
 * @see com.shockp.weather.application.port.RateLimiterPort
 */
public class RateLimiterService {
    
    /** The rate limiter port for actual rate limiting operations. */
    private final RateLimiterPort rateLimiterPort;
    
    /** The maximum number of requests allowed per time window. */
    private final int maxRequests;
    
    /** The time window duration for rate limiting. */
    private final Duration timeWindow;

    /**
     * Constructs a new {@link RateLimiterService} with the specified rate limiter port and configuration.
     *
     * @param rateLimiterPort the rate limiter port implementation, must not be {@code null}
     * @param maxRequests the maximum number of requests allowed per time window, must be greater than 0
     * @param timeWindow the time window duration for rate limiting, must not be {@code null}
     * @throws NullPointerException if {@code rateLimiterPort} or {@code timeWindow} is {@code null}
     * @throws IllegalArgumentException if {@code maxRequests} is less than or equal to 0
     */
    public RateLimiterService(RateLimiterPort rateLimiterPort, int maxRequests, Duration timeWindow) {
        validateRequests(maxRequests);

        this.rateLimiterPort = Objects.requireNonNull(rateLimiterPort, "Rate limiter port cannot be null");
        this.maxRequests = maxRequests;
        this.timeWindow = Objects.requireNonNull(timeWindow, "Time window cannot be null");
    }

    /**
     * Validates that the maximum requests value is positive.
     *
     * @param maxRequests the maximum requests value to validate
     * @throws IllegalArgumentException if {@code maxRequests} is less than or equal to 0
     */
    private void validateRequests(int maxRequests) {
        if (maxRequests <= 0) {
            throw new IllegalArgumentException("Max requests must be greater than 0");
        }
    }

    /**
     * Checks if the specified client has available rate limit tokens.
     *
     * @param clientId the unique client identifier, must not be {@code null} or empty
     * @return {@code true} if the client has available tokens, {@code false} otherwise
     * @throws IllegalArgumentException if {@code clientId} is {@code null} or empty
     * @throws WeatherServiceException if rate limit checking fails
     */
    public boolean checkRateLimit(String clientId) {
        validateClientId(clientId);
        try {
            return rateLimiterPort.getAvailableTokens(clientId.trim()) > 0;
        } catch (Exception e) {
            throw new WeatherServiceException("Failed to check rate limit for client: " + clientId, e);
        }
    }

    /**
     * Attempts to consume a rate limit token for the specified client.
     *
     * @param clientId the unique client identifier, must not be {@code null} or empty
     * @return {@code true} if a token was successfully consumed, {@code false} if the limit is exceeded
     * @throws IllegalArgumentException if {@code clientId} is {@code null} or empty
     * @throws WeatherServiceException if token consumption fails
     */
    public boolean consumeToken(String clientId) {
        validateClientId(clientId);
        try {
            return rateLimiterPort.tryConsume(clientId.trim());
        } catch (Exception e) {
            throw new WeatherServiceException("Failed to consume rate limit token for client: " + clientId, e);
        }
    }

    /**
     * Gets the number of remaining tokens for the specified client.
     *
     * @param clientId the unique client identifier, must not be {@code null} or empty
     * @return the number of remaining tokens
     * @throws IllegalArgumentException if {@code clientId} is {@code null} or empty
     * @throws WeatherServiceException if token retrieval fails
     */
    public int getRemainingTokens(String clientId) {
        validateClientId(clientId);
        try {
            return rateLimiterPort.getAvailableTokens(clientId.trim());
        } catch (Exception e) {
            throw new WeatherServiceException("Failed to get remaining tokens for client: " + clientId, e);
        }
    }

    /**
     * Resets the rate limit for the specified client.
     * <p>
     * This operation resets the client's rate limit counter, allowing them to make
     * requests up to the maximum limit again.
     * </p>
     *
     * @param clientId the unique client identifier, must not be {@code null} or empty
     * @throws IllegalArgumentException if {@code clientId} is {@code null} or empty
     * @throws WeatherServiceException if rate limit reset fails
     */
    public void reset(String clientId) {
        validateClientId(clientId);
        try {
            rateLimiterPort.reset(clientId.trim());
        } catch (Exception e) {
            throw new WeatherServiceException("Failed to reset rate limit for client: " + clientId, e);
        }
    }

    /**
     * Validates that the client ID is not {@code null} or empty.
     *
     * @param clientId the client ID to validate
     * @throws IllegalArgumentException if {@code clientId} is {@code null} or empty
     */
    private void validateClientId(String clientId) {
        if (clientId == null || clientId.isEmpty()) {
            throw new IllegalArgumentException("Client ID cannot be null or empty");
        }
    }

    /**
     * Gets the maximum number of requests allowed per time window.
     *
     * @return the maximum requests value
     */
    public int getMaxRequests() {
        return maxRequests;
    }

    /**
     * Gets the time window duration for rate limiting.
     *
     * @return the time window duration
     */
    public Duration getTimeWindow() {
        return timeWindow;
    }
} 