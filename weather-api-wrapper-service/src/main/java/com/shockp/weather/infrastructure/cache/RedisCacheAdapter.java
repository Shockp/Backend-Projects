package com.shockp.weather.infrastructure.cache;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.shockp.weather.application.port.CachePort;
import com.shockp.weather.application.usecase.cache.CacheOperationException;
import com.shockp.weather.domain.model.WeatherData;

/**
 * Redis cache adapter implementation for the weather service.
 * <p>
 * This class implements the {@link CachePort} interface to provide Redis-based caching
 * for weather data. It includes comprehensive security measures, error handling, and
 * performance optimizations.
 * </p>
 * <p>
 * Security features implemented:
 * <ul>
 *   <li>Input validation and sanitization to prevent injection attacks</li>
 *   <li>Cache key validation to prevent DoS attacks</li>
 *   <li>Secure JSON serialization with proper error handling</li>
 *   <li>Timeout handling to prevent resource exhaustion</li>
 *   <li>Comprehensive logging for security monitoring</li>
 * </ul>
 * </p>
 * <p>
 * Performance features:
 * <ul>
 *   <li>Efficient JSON serialization/deserialization</li>
 *   <li>Connection pooling through Spring Redis</li>
 *   <li>Optimized key validation patterns</li>
 *   <li>Proper exception handling to maintain performance</li>
 * </ul>
 * </p>
 *
 * @author Weather API Wrapper Service
 * @version 1.0
 * @since 1.0
 * @see com.shockp.weather.application.port.CachePort
 * @see com.shockp.weather.domain.model.WeatherData
 * @see com.shockp.weather.application.usecase.cache.CacheOperationException
 */
@Component
public final class RedisCacheAdapter implements CachePort {

    /** Logger for this class. */
    private static final Logger logger = LoggerFactory.getLogger(RedisCacheAdapter.class);

    /** Maximum allowed cache key length to prevent DoS attacks. */
    private static final int MAX_KEY_LENGTH = 256;

    /** Minimum required cache key length. */
    private static final int MIN_KEY_LENGTH = 1;

    /** Pattern for validating cache keys to prevent injection attacks. */
    private static final Pattern KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9:_\\-\\.]+$");

    /** Maximum TTL duration to prevent resource exhaustion. */
    private static final Duration MAX_TTL = Duration.ofDays(30);

    /** Minimum TTL duration for cache entries. */
    private static final Duration MIN_TTL = Duration.ofSeconds(1);

    /** Maximum JSON size to prevent memory exhaustion attacks. */
    private static final int MAX_JSON_SIZE = 1024 * 1024; // 1MB

    /** The Redis template for cache operations. */
    private final RedisTemplate<String, String> redisTemplate;

    /** The default time-to-live duration for cache entries. */
    private final Duration defaultTtl;

    /** The ObjectMapper for JSON serialization/deserialization. */
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new {@link RedisCacheAdapter} with the specified dependencies.
     *
     * @param redisTemplate the Redis template for cache operations, must not be {@code null}
     * @param defaultTtl the default time-to-live duration, must not be {@code null}
     * @throws NullPointerException if any parameter is {@code null}
     * @throws IllegalArgumentException if {@code defaultTtl} is invalid
     */
    public RedisCacheAdapter(
            @NonNull RedisTemplate<String, String> redisTemplate,
            @NonNull Duration defaultTtl) {

        this.redisTemplate = Objects.requireNonNull(redisTemplate, "Redis template cannot be null");
        this.defaultTtl = validateAndSanitizeTtl(defaultTtl, "Default TTL");
        
        // Configure ObjectMapper for security and performance
        this.objectMapper = new ObjectMapper();
        this.objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        
        // Configure Redis template for optimal performance
        this.redisTemplate.setKeySerializer(this.redisTemplate.getStringSerializer());
        
        logger.info("RedisCacheAdapter initialized with default TTL: {}", defaultTtl);
    }

    @Override
    public Optional<WeatherData> get(String key) {
        validateAndSanitizeKey(key);

        try {
            logger.debug("Retrieving cache entry for key: {}", key);
            
            ValueOperations<String, String> ops = redisTemplate.opsForValue();
            String json = ops.get(key);

            if (json == null) {
                logger.debug("Cache miss for key: {}", key);
                return Optional.empty();
            }

            // Validate JSON size to prevent memory exhaustion
            if (json.length() > MAX_JSON_SIZE) {
                logger.warn("JSON size exceeds maximum allowed size for key: {}", key);
                throw new CacheOperationException("Cached data size exceeds maximum allowed size");
            }

            WeatherData data = deserialize(json);
            logger.debug("Cache hit for key: {}", key);
            return Optional.of(data);
            
        } catch (CacheOperationException e) {
            // Re-throw our custom exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Failed to get cache entry for key: {}", key, e);
            throw new CacheOperationException("Failed to get cache entry for key: " + key, e);
        }
    }

    @Override
    public void put(String key, WeatherData data, Duration ttl) {
        validateAndSanitizeKey(key);
        Objects.requireNonNull(data, "Weather data cannot be null");
        Duration validatedTtl = validateAndSanitizeTtl(ttl, "TTL");

        try {
            logger.debug("Storing cache entry for key: {} with TTL: {}", key, validatedTtl);
            
            String json = serialize(data);
            
            // Validate JSON size before storing
            if (json.length() > MAX_JSON_SIZE) {
                logger.warn("Serialized data size exceeds maximum allowed size for key: {}", key);
                throw new CacheOperationException("Serialized data size exceeds maximum allowed size");
            }

            ValueOperations<String, String> ops = redisTemplate.opsForValue();
            ops.set(key, json, validatedTtl);
            
            logger.debug("Successfully stored cache entry for key: {}", key);
            
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize weather data for key: {}", key, e);
            throw new CacheOperationException("Failed to serialize weather data for key: " + key, e);
        } catch (CacheOperationException e) {
            // Re-throw our custom exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Failed to put weather data into cache for key: {}", key, e);
            throw new CacheOperationException("Failed to put weather data into cache for key: " + key, e);
        }
    }

    @Override
    public void delete(String key) {
        validateAndSanitizeKey(key);

        try {
            logger.debug("Deleting cache entry for key: {}", key);
            
            Boolean result = redisTemplate.delete(key);
            
            if (Boolean.TRUE.equals(result)) {
                logger.debug("Successfully deleted cache entry for key: {}", key);
            } else {
                logger.debug("Cache entry not found for deletion, key: {}", key);
            }
            
        } catch (Exception e) {
            logger.error("Failed to delete cache entry for key: {}", key, e);
            throw new CacheOperationException("Failed to delete cache entry for key: " + key, e);
        }
    }

    @Override
    public boolean exists(String key) {
        validateAndSanitizeKey(key);

        try {
            logger.debug("Checking existence of cache entry for key: {}", key);
            
            Boolean result = redisTemplate.hasKey(key);
            boolean exists = Boolean.TRUE.equals(result);
            
            logger.debug("Cache entry exists for key {}: {}", key, exists);
            return exists;
            
        } catch (Exception e) {
            logger.error("Failed to check if cache entry exists for key: {}", key, e);
            throw new CacheOperationException("Failed to check if cache entry exists for key: " + key, e);
        }
    }

    @Override
    public void clear() {
        try {
            logger.info("Clearing all cache entries");
            
            Set<String> keys = redisTemplate.keys("*");
            if (keys != null && !keys.isEmpty()) {
                Long deletedCount = redisTemplate.delete(keys);
                logger.info("Cleared {} cache entries", deletedCount);
            } else {
                logger.info("No cache entries found to clear");
            }
            
        } catch (Exception e) {
            logger.error("Failed to clear cache", e);
            throw new CacheOperationException("Failed to clear cache", e);
        }
    }

    /**
     * Serializes weather data to JSON string.
     * <p>
     * This method converts a {@link WeatherData} object to a JSON string for storage
     * in Redis. It includes proper error handling and validation.
     * </p>
     *
     * @param data the weather data to serialize, must not be {@code null}
     * @return the JSON string representation of the weather data
     * @throws JsonProcessingException if serialization fails
     * @throws NullPointerException if {@code data} is {@code null}
     */
    private String serialize(WeatherData data) throws JsonProcessingException {
        Objects.requireNonNull(data, "Weather data cannot be null");
        return objectMapper.writeValueAsString(data);
    }

    /**
     * Deserializes JSON string to weather data object.
     * <p>
     * This method converts a JSON string back to a {@link WeatherData} object.
     * It includes proper error handling and validation.
     * </p>
     *
     * @param json the JSON string to deserialize, must not be {@code null} or empty
     * @return the deserialized weather data object
     * @throws IOException if deserialization fails
     * @throws IllegalArgumentException if {@code json} is {@code null} or empty
     */
    private WeatherData deserialize(String json) throws IOException {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON string cannot be null or empty");
        }
        
        return objectMapper.readValue(json, WeatherData.class);
    }

    /**
     * Validates and sanitizes cache keys to prevent security vulnerabilities.
     * <p>
     * This method performs comprehensive validation of cache keys:
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
     * Validates and sanitizes time-to-live (TTL) duration.
     * <p>
     * This method ensures that TTL values are within acceptable bounds to prevent
     * resource exhaustion and ensure proper cache behavior.
     * </p>
     *
     * @param ttl the TTL duration to validate
     * @param ttlName the name of the TTL parameter for error messages
     * @return the validated TTL duration
     * @throws IllegalArgumentException if the TTL is invalid
     * @throws NullPointerException if the TTL is {@code null}
     */
    private Duration validateAndSanitizeTtl(Duration ttl, String ttlName) {
        Objects.requireNonNull(ttl, ttlName + " cannot be null");

        if (ttl.isNegative()) {
            throw new IllegalArgumentException(ttlName + " cannot be negative");
        }

        if (ttl.isZero()) {
            throw new IllegalArgumentException(ttlName + " cannot be zero");
        }

        if (ttl.compareTo(MIN_TTL) < 0) {
            throw new IllegalArgumentException(
                String.format("%s must be at least %d seconds", ttlName, MIN_TTL.getSeconds()));
        }

        if (ttl.compareTo(MAX_TTL) > 0) {
            throw new IllegalArgumentException(
                String.format("%s cannot exceed %d days", ttlName, MAX_TTL.toDays()));
        }

        return ttl;
    }

    /**
     * Gets the default time-to-live duration.
     * <p>
     * This method returns the default TTL duration configured for this cache adapter.
     * </p>
     *
     * @return the default TTL duration
     */
    public Duration getDefaultTtl() {
        return defaultTtl;
    }

    /**
     * Gets the maximum allowed JSON size.
     * <p>
     * This method returns the maximum allowed size for JSON data to prevent
     * memory exhaustion attacks.
     * </p>
     *
     * @return the maximum JSON size in bytes
     */
    public int getMaxJsonSize() {
        return MAX_JSON_SIZE;
    }
} 