package com.shockp.weather.application.usecase.ratelimit;

import java.io.Serial;

/**
 * Exception thrown when rate limiting operations fail in the weather service use cases.
 * <p>
 * This exception is used to indicate failures in rate limiting operations within the
 * application layer use cases, providing a way to distinguish rate limiting errors
 * from other types of exceptions. It extends {@link RuntimeException} to avoid
 * forcing callers to handle rate limiting failures if they choose not to.
 * </p>
 * <p>
 * This exception should be used when rate limiting operations (token consumption,
 * limit checking, reset) fail due to underlying infrastructure issues, configuration
 * problems, or other rate limiting-related errors.
 * </p>
 *
 * @author Weather API Wrapper Service
 * @version 1.0
 * @since 1.0
 * @see com.shockp.weather.application.usecase.ratelimit.RateLimitUseCase
 */
public final class RateLimitOperationException extends RuntimeException {

    /** Serial version UID for serialization compatibility. */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@link RateLimitOperationException} with the specified detail message.
     *
     * @param message the detail message describing the rate limiting operation failure, may be {@code null}
     */
    public RateLimitOperationException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@link RateLimitOperationException} with the specified detail message and cause.
     *
     * @param message the detail message describing the rate limiting operation failure, may be {@code null}
     * @param cause the cause of the rate limiting operation failure, may be {@code null}
     */
    public RateLimitOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@link RateLimitOperationException} with the specified cause.
     *
     * @param cause the cause of the rate limiting operation failure, may be {@code null}
     */
    public RateLimitOperationException(Throwable cause) {
        super(cause);
    }
} 