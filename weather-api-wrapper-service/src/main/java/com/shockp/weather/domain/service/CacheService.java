package com.shockp.weather.domain.service;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

import com.shockp.weather.application.port.CachePort;
import com.shockp.weather.domain.model.WeatherData;
import com.shockp.weather.domain.model.WeatherRequest;

/**
 * Domain service for managing weather data caching operations.
 * <p>
 * This service provides a high-level interface for caching weather data, including
 * key generation, cache operations, and timeout management. It follows the domain
 * service pattern and uses the {@link CachePort} for actual cache operations.
 * </p>
 *
 * @author Weather API Wrapper Service
 * @see com.shockp.weather.application.port.CachePort
 * @see com.shockp.weather.domain.model.WeatherData
 * @see com.shockp.weather.domain.model.WeatherRequest
 */
public class CacheService {
    
    /** The cache port for actual cache operations. */
    private final CachePort cachePort;
    
    /** The default cache timeout duration. */
    private final Duration cacheTimeout;

    /**
     * Constructs a new {@link CacheService} with the specified cache port and timeout.
     *
     * @param cachePort the cache port implementation, must not be {@code null}
     * @param cacheTimeout the default cache timeout duration, must not be {@code null}
     * @throws NullPointerException if either parameter is {@code null}
     */
    public CacheService(CachePort cachePort, Duration cacheTimeout) {
        this.cachePort = Objects.requireNonNull(cachePort, "Cache port cannot be null");
        this.cacheTimeout = Objects.requireNonNull(cacheTimeout, "Cache timeout cannot be null");
    }

    /**
     * Retrieves weather data from the cache by key.
     *
     * @param key the cache key, must not be {@code null}
     * @return an {@link Optional} containing the {@link WeatherData} if present, or empty if not found
     * @throws NullPointerException if {@code key} is {@code null}
     * @throws WeatherServiceException if cache retrieval fails
     */
    public Optional<WeatherData> get(String key) {
        Objects.requireNonNull(key, "Cache key cannot be null");
        try {
            return cachePort.get(key);
        } catch (Exception e) {
            throw new WeatherServiceException("Failed to retrieve weather data from cache for key: " + key, e);
        }
    }

    /**
     * Stores weather data in the cache with the default timeout.
     *
     * @param key the cache key, must not be {@code null}
     * @param data the {@link WeatherData} to cache, must not be {@code null}
     * @throws NullPointerException if either parameter is {@code null}
     * @throws WeatherServiceException if cache storage fails
     */
    public void put(String key, WeatherData data) {
        put(key, data, cacheTimeout);
    }

    /**
     * Stores weather data in the cache with a specified time-to-live (TTL).
     *
     * @param key the cache key, must not be {@code null}
     * @param data the {@link WeatherData} to cache, must not be {@code null}
     * @param ttl the time-to-live duration, must not be {@code null}
     * @throws NullPointerException if any parameter is {@code null}
     * @throws WeatherServiceException if cache storage fails
     */
    public void put(String key, WeatherData data, Duration ttl) {
        Objects.requireNonNull(key, "Cache key cannot be null");
        Objects.requireNonNull(data, "Weather data cannot be null");
        Objects.requireNonNull(ttl, "TTL cannot be null");
        try {
            cachePort.put(key, data, ttl);
        } catch (Exception e) {
            throw new WeatherServiceException("Failed to store weather data in cache for key: " + key, e);
        }
    }

    /**
     * Evicts (removes) a specific cache entry by key.
     *
     * @param key the cache key to evict, must not be {@code null}
     * @throws NullPointerException if {@code key} is {@code null}
     * @throws WeatherServiceException if cache eviction fails
     */
    public void evict(String key) {
        Objects.requireNonNull(key, "Cache key cannot be null");
        try {
            cachePort.delete(key);
        } catch (Exception e) {
            throw new WeatherServiceException("Failed to evict weather data from cache for key: " + key, e);
        }
    }

    /**
     * Clears all cache entries.
     * <p>
     * This operation removes all cached weather data. Use with caution as it may
     * be expensive depending on the cache implementation and the number of entries.
     * </p>
     *
     * @throws WeatherServiceException if cache clearing fails
     */
    public void clear() {
        try {
            cachePort.clear();
        } catch (Exception e) {
            throw new WeatherServiceException("Failed to clear cache", e);
        }
    }

    /**
     * Generates a cache key for the specified weather request.
     * <p>
     * The generated key is based on the location, date, and hourly inclusion flag
     * to ensure uniqueness for different weather requests.
     * </p>
     *
     * @param request the {@link WeatherRequest} to generate a key for, must not be {@code null}
     * @return a unique cache key string
     * @throws NullPointerException if {@code request} is {@code null}
     */
    public String generateKey(WeatherRequest request) {
        Objects.requireNonNull(request, "Weather request cannot be null");
        
        return String.format("weather:%s:%s:%s:%s:%s",
            request.getLocation().getLatitude(),
            request.getLocation().getLongitude(),
            request.getLocation().getCity(),
            request.getDate(),
            request.isIncludeHourly());
    }

    /**
     * Gets the default cache timeout duration.
     *
     * @return the default cache timeout duration
     */
    public Duration getCacheTimeout() {
        return cacheTimeout;
    }
} 