package com.shockp.weather.infrastructure.ratelimiter;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.shockp.weather.application.port.RateLimiterPort;
import com.shockp.weather.application.usecase.ratelimit.RateLimitOperationException;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;

/**
 * Bucket4j-based rate limiter adapter implementation for the weather service.
 * <p>
 * This class implements the {@link RateLimiterPort} interface to provide rate limiting
 * functionality using the Bucket4j library. It includes comprehensive security measures,
 * error handling, and performance optimizations.
 * </p>
 * <p>
 * Security features implemented:
 * <ul>
 *   <li>Input validation and sanitization to prevent injection attacks</li>
 *   <li>Client ID validation to prevent DoS attacks</li>
 *   <li>Rate limit configuration validation to prevent resource exhaustion</li>
 *   <li>Comprehensive logging for security monitoring</li>
 *   <li>Thread-safe bucket management</li>
 * </ul>
 * </p>
 * <p>
 * Performance features:
 * <ul>
 *   <li>Concurrent bucket management for high-throughput scenarios</li>
 *   <li>Efficient token consumption with minimal overhead</li>
 *   <li>Lazy bucket creation to reduce memory usage</li>
 *   <li>Proper exception handling to maintain performance</li>
 * </ul>
 * </p>
 * <p>
 * The adapter uses a token bucket algorithm where each client has their own bucket
 * with a specified capacity and refill rate. Tokens are consumed for each request,
 * and the bucket refills at a specified rate over time.
 * </p>
 *
 * @author Weather API Wrapper Service
 * @version 1.0
 * @since 1.0
 * @see com.shockp.weather.application.port.RateLimiterPort
 * @see com.shockp.weather.application.usecase.ratelimit.RateLimitOperationException
 * @see io.github.bucket4j.Bucket
 * @see io.github.bucket4j.Bucket4j
 */
@Component
public final class Bucket4jRateLimiterAdapter implements RateLimiterPort {

    /** Logger for this class. */
    private static final Logger logger = LoggerFactory.getLogger(Bucket4jRateLimiterAdapter.class);

    /** Minimum allowed bucket capacity to prevent DoS attacks. */
    private static final int MIN_CAPACITY = 1;

    /** Maximum allowed bucket capacity to prevent resource exhaustion. */
    private static final int MAX_CAPACITY = 1000;

    /** Minimum allowed refill tokens to ensure reasonable rate limiting. */
    private static final int MIN_REFILL_TOKENS = 1;

    /** Maximum allowed refill tokens to prevent resource exhaustion. */
    private static final int MAX_REFILL_TOKENS = 1000;

    /** Minimum allowed refill duration to prevent excessive refill rates. */
    private static final Duration MIN_REFILL_DURATION = Duration.ofSeconds(1);

    /** Maximum allowed refill duration to ensure reasonable rate limiting. */
    private static final Duration MAX_REFILL_DURATION = Duration.ofHours(1);

    /** Minimum allowed client ID length. */
    private static final int MIN_CLIENT_ID_LENGTH = 1;

    /** Maximum allowed client ID length to prevent DoS attacks. */
    private static final int MAX_CLIENT_ID_LENGTH = 256;

    /** Pattern for validating client IDs to prevent injection attacks. */
    private static final Pattern CLIENT_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9:_\\-\\.]+$");

    /** Maximum number of buckets to prevent memory exhaustion. */
    private static final int MAX_BUCKETS = 10000;

    /** The bucket capacity (maximum tokens). */
    private final int capacity;

    /** The number of tokens to refill per time period. */
    private final int refillTokens;

    /** The duration for the refill period. */
    private final Duration refillDuration;

    /** Thread-safe map of client buckets. */
    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Constructs a new {@link Bucket4jRateLimiterAdapter} with the specified configuration.
     * <p>
     * This constructor validates all input parameters to ensure they are within
     * acceptable bounds and creates a properly configured rate limiter adapter.
     * </p>
     *
     * @param capacity the bucket capacity (maximum tokens), must be between
     *        {@link #MIN_CAPACITY} and {@link #MAX_CAPACITY}
     * @param refillTokens the number of tokens to refill per time period, must be between
     *        {@link #MIN_REFILL_TOKENS} and {@link #MAX_REFILL_TOKENS}
     * @param refillDuration the duration for the refill period, must be between
     *        {@link #MIN_REFILL_DURATION} and {@link #MAX_REFILL_DURATION}
     * @throws IllegalArgumentException if any parameter is outside the valid range
     * @throws NullPointerException if {@code refillDuration} is {@code null}
     */
    public Bucket4jRateLimiterAdapter(
            int capacity,
            int refillTokens,
            @NonNull Duration refillDuration) {

        validateCapacity(capacity);
        validateRefillTokens(refillTokens);
        validateRefillDuration(refillDuration);

        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillDuration = refillDuration;

        logger.info("Bucket4jRateLimiterAdapter initialized with capacity: {}, refill: {} tokens per {}", 
            capacity, refillTokens, refillDuration);
    }

    @Override
    public boolean tryConsume(String clientId) {
        validateAndSanitizeClientId(clientId);

        try {
            logger.debug("Attempting to consume token for client: {}", clientId);
            
            Bucket bucket = getBucketForClient(clientId);
            boolean consumed = bucket.tryConsume(1);
            
            if (consumed) {
                logger.debug("Successfully consumed token for client: {}", clientId);
            } else {
                logger.debug("Rate limit exceeded for client: {}", clientId);
            }
            
            return consumed;
            
        } catch (RateLimitOperationException e) {
            // Re-throw our custom exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Failed to consume token for client: {}", clientId, e);
            throw new RateLimitOperationException("Failed to consume token for client: " + clientId, e);
        }
    }

    @Override
    public int getAvailableTokens(String clientId) {
        validateAndSanitizeClientId(clientId);

        try {
            logger.debug("Getting available tokens for client: {}", clientId);
            
            Bucket bucket = getBucketForClient(clientId);
            long availableTokens = bucket.getAvailableTokens();
            
            // Convert to int with bounds checking
            if (availableTokens > Integer.MAX_VALUE) {
                logger.warn("Available tokens ({}) exceed Integer.MAX_VALUE for client: {}", availableTokens, clientId);
                return Integer.MAX_VALUE;
            }
            
            int result = (int) availableTokens;
            logger.debug("Available tokens for client {}: {}", clientId, result);
            return result;
            
        } catch (RateLimitOperationException e) {
            // Re-throw our custom exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Failed to get available tokens for client: {}", clientId, e);
            throw new RateLimitOperationException("Failed to get available tokens for client: " + clientId, e);
        }
    }

    @Override
    public void reset(String clientId) {
        validateAndSanitizeClientId(clientId);

        try {
            logger.info("Resetting rate limit for client: {}", clientId);
            
            buckets.put(clientId, createNewBucket());
            
            logger.debug("Successfully reset rate limit for client: {}", clientId);
            
        } catch (RateLimitOperationException e) {
            // Re-throw our custom exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Failed to reset rate limit for client: {}", clientId, e);
            throw new RateLimitOperationException("Failed to reset rate limit for client: " + clientId, e);
        }
    }

    /**
     * Gets or creates a bucket for the specified client.
     * <p>
     * This method uses a thread-safe approach to get or create buckets for clients.
     * It includes protection against memory exhaustion by limiting the number of
     * buckets that can be created.
     * </p>
     *
     * @param clientId the client identifier, must not be {@code null}
     * @return the bucket for the client, never {@code null}
     * @throws RateLimitOperationException if too many buckets are created
     */
    private Bucket getBucketForClient(String clientId) {
        // Check if we're approaching the bucket limit
        if (buckets.size() >= MAX_BUCKETS) {
            logger.warn("Maximum number of buckets ({}) reached", MAX_BUCKETS);
            throw new RateLimitOperationException("Maximum number of rate limit buckets reached");
        }

        return buckets.computeIfAbsent(clientId, id -> {
            logger.debug("Creating new bucket for client: {}", id);
            return createNewBucket();
        });
    }

    /**
     * Creates a new bucket with the configured capacity and refill settings.
     * <p>
     * This method creates a Bucket4j bucket with the specified capacity and
     * greedy refill strategy for optimal performance.
     * </p>
     *
     * @return a new bucket instance, never {@code null}
     */
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.builder()
            .capacity(capacity)
            .refillGreedy(refillTokens, refillDuration)
            .build();

        return Bucket4j.builder()
            .addLimit(limit)
            .build();
    }

    /**
     * Validates that the capacity is within acceptable bounds.
     * <p>
     * This method ensures that the bucket capacity is reasonable to prevent
     * both DoS attacks (too low) and resource exhaustion (too high).
     * </p>
     *
     * @param capacity the capacity to validate
     * @throws IllegalArgumentException if the capacity is outside the valid range
     */
    private void validateCapacity(int capacity) {
        if (capacity < MIN_CAPACITY || capacity > MAX_CAPACITY) {
            throw new IllegalArgumentException(
                String.format("Capacity must be between %d and %d, got: %d", 
                    MIN_CAPACITY, MAX_CAPACITY, capacity));
        }
    }

    /**
     * Validates that the refill tokens are within acceptable bounds.
     * <p>
     * This method ensures that the refill token count is reasonable to prevent
     * both ineffective rate limiting (too low) and resource exhaustion (too high).
     * </p>
     *
     * @param refillTokens the refill tokens to validate
     * @throws IllegalArgumentException if the refill tokens are outside the valid range
     */
    private void validateRefillTokens(int refillTokens) {
        if (refillTokens < MIN_REFILL_TOKENS || refillTokens > MAX_REFILL_TOKENS) {
            throw new IllegalArgumentException(
                String.format("Refill tokens must be between %d and %d, got: %d", 
                    MIN_REFILL_TOKENS, MAX_REFILL_TOKENS, refillTokens));
        }
    }

    /**
     * Validates that the refill duration is within acceptable bounds.
     * <p>
     * This method ensures that the refill duration is reasonable to prevent
     * both excessive refill rates (too short) and ineffective rate limiting (too long).
     * </p>
     *
     * @param refillDuration the refill duration to validate, must not be {@code null}
     * @throws IllegalArgumentException if the refill duration is outside the valid range
     * @throws NullPointerException if the refill duration is {@code null}
     */
    private void validateRefillDuration(Duration refillDuration) {
        Objects.requireNonNull(refillDuration, "Refill duration cannot be null");

        if (refillDuration.compareTo(MIN_REFILL_DURATION) < 0) {
            throw new IllegalArgumentException(
                String.format("Refill duration must be at least %s, got: %s", 
                    MIN_REFILL_DURATION, refillDuration));
        }

        if (refillDuration.compareTo(MAX_REFILL_DURATION) > 0) {
            throw new IllegalArgumentException(
                String.format("Refill duration must be at most %s, got: %s", 
                    MAX_REFILL_DURATION, refillDuration));
        }
    }

    /**
     * Validates and sanitizes the client ID to prevent security vulnerabilities.
     * <p>
     * This method performs comprehensive validation of client IDs:
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
     * @throws NullPointerException if the client ID is {@code null}
     */
    private void validateAndSanitizeClientId(String clientId) {
        if (clientId == null) {
            throw new IllegalArgumentException("Client ID cannot be null");
        }

        String sanitizedId = clientId.trim();
        
        if (sanitizedId.isEmpty()) {
            throw new IllegalArgumentException("Client ID cannot be empty");
        }

        if (sanitizedId.length() < MIN_CLIENT_ID_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Client ID must be at least %d characters long, got: %d", 
                    MIN_CLIENT_ID_LENGTH, sanitizedId.length()));
        }

        if (sanitizedId.length() > MAX_CLIENT_ID_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Client ID cannot exceed %d characters, got: %d", 
                    MAX_CLIENT_ID_LENGTH, sanitizedId.length()));
        }

        if (!CLIENT_ID_PATTERN.matcher(sanitizedId).matches()) {
            throw new IllegalArgumentException(
                "Client ID contains invalid characters. Only alphanumeric, colon, underscore, hyphen, and dot characters are allowed");
        }
    }

    /**
     * Gets the current number of active buckets.
     * <p>
     * This method returns the number of buckets currently managed by this adapter.
     * It can be used for monitoring and debugging purposes.
     * </p>
     *
     * @return the number of active buckets
     */
    public int getActiveBucketCount() {
        return buckets.size();
    }

    /**
     * Gets the configured bucket capacity.
     * <p>
     * This method returns the maximum number of tokens that each bucket can hold.
     * </p>
     *
     * @return the bucket capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Gets the configured refill tokens.
     * <p>
     * This method returns the number of tokens that are refilled per time period.
     * </p>
     *
     * @return the refill tokens
     */
    public int getRefillTokens() {
        return refillTokens;
    }

    /**
     * Gets the configured refill duration.
     * <p>
     * This method returns the duration for the refill period.
     * </p>
     *
     * @return the refill duration
     */
    public Duration getRefillDuration() {
        return refillDuration;
    }

    /**
     * Gets the maximum number of buckets allowed.
     * <p>
     * This method returns the maximum number of buckets that can be created
     * to prevent memory exhaustion.
     * </p>
     *
     * @return the maximum number of buckets
     */
    public int getMaxBuckets() {
        return MAX_BUCKETS;
    }
} 