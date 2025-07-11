package com.shockp.weather.application.port;

import java.time.Duration;
import java.util.Optional;
import com.shockp.weather.domain.model.WeatherData;

/**
 * Port interface for cache operations in the weather service.
 * <p>
 * This interface defines the contract for caching weather data, allowing for
 * different cache implementations (e.g., Redis, in-memory). It follows the
 * hexagonal architecture principle, decoupling the domain from infrastructure.
 * </p>
 *
 * @author Weather API Wrapper Service
 * @see com.shockp.weather.domain.service.CacheService
 */
public interface CachePort {

    /**
     * Retrieves weather data from the cache by key.
     *
     * @param key the cache key, must not be {@code null}
     * @return an {@link Optional} containing the {@link WeatherData} if present, or empty if not found
     */
    Optional<WeatherData> get(String key);

    /**
     * Stores weather data in the cache with a specified time-to-live (TTL).
     *
     * @param key the cache key, must not be {@code null}
     * @param data the {@link WeatherData} to cache, must not be {@code null}
     * @param ttl the time-to-live duration, must not be {@code null}
     */
    void put(String key, WeatherData data, Duration ttl);

    /**
     * Deletes weather data from the cache by key.
     *
     * @param key the cache key, must not be {@code null}
     */
    void delete(String key);

    /**
     * Checks if a cache entry exists for the given key.
     *
     * @param key the cache key, must not be {@code null}
     * @return {@code true} if the cache entry exists, {@code false} otherwise
     */
    boolean exists(String key);
} 