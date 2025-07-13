package com.shockp.weather.application.usecase;

import java.io.Serial;

/**
 * Exception thrown when cache operations fail in the weather service use cases.
 * <p>
 * This exception is used to indicate failures in cache operations within the
 * application layer use cases, providing a way to distinguish cache operation
 * errors from other types of exceptions. It extends {@link RuntimeException}
 * to avoid forcing callers to handle cache operation failures if they choose
 * not to.
 * </p>
 * <p>
 * This exception should be used when cache operations (store, retrieve, invalidate,
 * clear) fail due to underlying infrastructure issues, configuration problems,
 * or other cache-related errors.
 * </p>
 *
 * @author Weather API Wrapper Service
 * @version 1.0
 * @since 1.0
 * @see com.shockp.weather.application.usecase.CacheWeatherUseCase
 */
public final class CacheOperationException extends RuntimeException {

    /** Serial version UID for serialization compatibility. */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@link CacheOperationException} with the specified detail message.
     *
     * @param message the detail message describing the cache operation failure, may be {@code null}
     */
    public CacheOperationException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@link CacheOperationException} with the specified detail message and cause.
     *
     * @param message the detail message describing the cache operation failure, may be {@code null}
     * @param cause the cause of the cache operation failure, may be {@code null}
     */
    public CacheOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@link CacheOperationException} with the specified cause.
     *
     * @param cause the cause of the cache operation failure, may be {@code null}
     */
    public CacheOperationException(Throwable cause) {
        super(cause);
    }
} 