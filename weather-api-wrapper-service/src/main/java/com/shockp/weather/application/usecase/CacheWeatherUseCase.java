package com.shockp.weather.application.usecase;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import com.shockp.weather.domain.model.WeatherData;
import com.shockp.weather.domain.service.CacheService;
import com.shockp.weather.domain.service.WeatherServiceException;

/**
 * Use case for managing weather data caching operations.
 * <p>
 * This use case provides a high-level interface for caching weather data, including
 * storing, retrieving, and invalidating cached weather information. It implements
 * security measures to prevent cache key injection attacks and ensures proper
 * validation of all inputs.
 * </p>
 * <p>
 * The use case follows the single responsibility principle and encapsulates all
 * caching business logic, providing a clean interface for the application layer.
 * </p>
 *
 * @author Weather API Wrapper Service
 * @version 1.0
 * @since 1.0
 * @see com.shockp.weather.domain.service.CacheService
 * @see com.shockp.weather.domain.model.WeatherData
 * @see com.shockp.weather.application.usecase.CacheOperationException
 */
public final class CacheWeatherUseCase {

    /** Maximum allowed cache key length to prevent DoS attacks. */
    private static final int MAX_KEY_LENGTH = 256;

    /** Minimum required cache key length. */
    private static final int MIN_KEY_LENGTH = 1;

    /** Pattern for validating cache keys to prevent injection attacks. */
    private static final Pattern KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9:_\\-\\.]+$");

    /** The cache service for weather data operations. */
    private final CacheService cacheService;

    /**
     * Constructs a new {@link CacheWeatherUseCase} with the specified cache service.
     *
     * @param cacheService the cache service for weather data operations, must not be {@code null}
     * @throws NullPointerException if {@code cacheService} is {@code null}
     */
    public CacheWeatherUseCase(CacheService cacheService) {
        this.cacheService = Objects.requireNonNull(cacheService, "Cache service cannot be null");
    }

    /**
     * Stores weather data in the cache with the default timeout.
     * <p>
     * This method validates the input parameters, sanitizes the cache key, and
     * stores the weather data using the default cache timeout configured in the
     * cache service.
     * </p>
     *
     * @param key the cache key for storing the weather data, must not be {@code null} or empty
     * @param data the weather data to cache, must not be {@code null}
     * @throws IllegalArgumentException if the key is invalid or empty
     * @throws NullPointerException if {@code data} is {@code null}
     * @throws CacheOperationException if the cache operation fails
     */
    public void execute(String key, WeatherData data) {
        validateAndSanitizeKey(key);
        Objects.requireNonNull(data, "Weather data cannot be null");

        try {
            cacheService.put(key, data);
        } catch (WeatherServiceException e) {
            throw new CacheOperationException("Failed to store weather data in cache for key: " + key, e);
        }
    }

    /**
     * Stores weather data in the cache with a custom time-to-live (TTL).
     * <p>
     * This method validates the input parameters, sanitizes the cache key, and
     * stores the weather data with the specified TTL duration.
     * </p>
     *
     * @param key the cache key for storing the weather data, must not be {@code null} or empty
     * @param data the weather data to cache, must not be {@code null}
     * @param ttl the time-to-live duration for the cached data, must not be {@code null}
     * @throws IllegalArgumentException if the key is invalid or empty, or if TTL is negative
     * @throws NullPointerException if {@code data} or {@code ttl} is {@code null}
     * @throws CacheOperationException if the cache operation fails
     */
    public void execute(String key, WeatherData data, Duration ttl) {
        validateAndSanitizeKey(key);
        Objects.requireNonNull(data, "Weather data cannot be null");
        validateTtl(ttl);

        try {
            cacheService.put(key, data, ttl);
        } catch (WeatherServiceException e) {
            throw new CacheOperationException("Failed to store weather data in cache for key: " + key, e);
        }
    }

    /**
     * Retrieves weather data from the cache by key.
     * <p>
     * This method validates and sanitizes the cache key, then attempts to retrieve
     * the weather data from the cache. If no data is found, an empty {@link Optional}
     * is returned.
     * </p>
     *
     * @param key the cache key to retrieve data for, must not be {@code null} or empty
     * @return an {@link Optional} containing the {@link WeatherData} if present, or empty if not found
     * @throws IllegalArgumentException if the key is invalid or empty
     * @throws CacheOperationException if the cache retrieval operation fails
     */
    public Optional<WeatherData> retrieve(String key) {
        validateAndSanitizeKey(key);

        try {
            return cacheService.get(key);
        } catch (WeatherServiceException e) {
            throw new CacheOperationException("Failed to retrieve weather data from cache for key: " + key, e);
        }
    }

    /**
     * Invalidates (removes) weather data from the cache by key.
     * <p>
     * This method validates and sanitizes the cache key, then removes the
     * corresponding weather data from the cache.
     * </p>
     *
     * @param key the cache key to invalidate, must not be {@code null} or empty
     * @throws IllegalArgumentException if the key is invalid or empty
     * @throws CacheOperationException if the cache invalidation operation fails
     */
    public void invalidate(String key) {
        validateAndSanitizeKey(key);

        try {
            cacheService.evict(key);
        } catch (WeatherServiceException e) {
            throw new CacheOperationException("Failed to invalidate weather data from cache for key: " + key, e);
        }
    }

    /**
     * Checks if weather data exists in the cache for the given key.
     * <p>
     * This method validates and sanitizes the cache key, then checks if
     * weather data exists in the cache without retrieving the actual data.
     * </p>
     *
     * @param key the cache key to check, must not be {@code null} or empty
     * @return {@code true} if weather data exists for the key, {@code false} otherwise
     * @throws IllegalArgumentException if the key is invalid or empty
     * @throws CacheOperationException if the cache check operation fails
     */
    public boolean exists(String key) {
        validateAndSanitizeKey(key);

        try {
            Optional<WeatherData> data = cacheService.get(key);
            return data.isPresent();
        } catch (WeatherServiceException e) {
            throw new CacheOperationException("Failed to check weather data existence in cache for key: " + key, e);
        }
    }

    /**
     * Clears all weather data from the cache.
     * <p>
     * This operation removes all cached weather data. Use with caution as it may
     * be expensive depending on the cache implementation and the number of entries.
     * </p>
     *
     * @throws CacheOperationException if the cache clearing operation fails
     */
    public void clearAll() {
        try {
            cacheService.clear();
        } catch (WeatherServiceException e) {
            throw new CacheOperationException("Failed to clear all weather data from cache", e);
        }
    }

    /**
     * Validates and sanitizes the cache key.
     * <p>
     * This method performs comprehensive validation of the cache key:
     * <ul>
     *   <li>Ensures the key is not {@code null} or empty</li>
     *   <li>Validates key length is within acceptable bounds</li>
     *   <li>Checks key format to prevent injection attacks</li>
     *   <li>Trims whitespace from the key</li>
     * </ul>
     * </p>
     *
     * @param key the cache key to validate and sanitize
     * @throws IllegalArgumentException if the key is invalid
     */
    private void validateAndSanitizeKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Cache key cannot be null");
        }

        String sanitizedKey = key.trim();
        
        if (sanitizedKey.isEmpty()) {
            throw new IllegalArgumentException("Cache key cannot be empty");
        }

        if (sanitizedKey.length() < MIN_KEY_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Cache key must be at least %d characters long", MIN_KEY_LENGTH));
        }

        if (sanitizedKey.length() > MAX_KEY_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Cache key cannot exceed %d characters", MAX_KEY_LENGTH));
        }

        if (!KEY_PATTERN.matcher(sanitizedKey).matches()) {
            throw new IllegalArgumentException(
                "Cache key contains invalid characters. Only alphanumeric, colon, underscore, hyphen, and dot characters are allowed");
        }
    }

    /**
     * Validates the time-to-live (TTL) duration.
     *
     * @param ttl the TTL duration to validate
     * @throws IllegalArgumentException if the TTL is negative
     * @throws NullPointerException if the TTL is {@code null}
     */
    private void validateTtl(Duration ttl) {
        Objects.requireNonNull(ttl, "TTL cannot be null");
        
        if (ttl.isNegative()) {
            throw new IllegalArgumentException("TTL cannot be negative");
        }
    }
} 