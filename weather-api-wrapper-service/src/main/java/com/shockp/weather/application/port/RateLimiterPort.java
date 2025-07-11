package com.shockp.weather.application.port;

/**
 * Port interface for rate limiting operations in the weather service.
 * <p>
 * This interface defines the contract for rate limiting, allowing for different
 * implementations (e.g., Bucket4j, in-memory). It follows the hexagonal architecture
 * principle, decoupling the domain from infrastructure.
 * </p>
 *
 * @author Weather API Wrapper Service
 * @see com.shockp.weather.domain.service.RateLimiterService
 */
public interface RateLimiterPort {

    /**
     * Attempts to consume a rate limit token for the specified client.
     *
     * @param clientId the unique client identifier, must not be {@code null}
     * @return {@code true} if a token was successfully consumed, {@code false} if the limit is exceeded
     */
    boolean tryConsume(String clientId);

    /**
     * Gets the number of available tokens for the specified client.
     *
     * @param clientId the unique client identifier, must not be {@code null}
     * @return the number of available tokens
     */
    int getAvailableTokens(String clientId);

    /**
     * Resets the rate limit for the specified client.
     *
     * @param clientId the unique client identifier, must not be {@code null}
     */
    void reset(String clientId);
} 