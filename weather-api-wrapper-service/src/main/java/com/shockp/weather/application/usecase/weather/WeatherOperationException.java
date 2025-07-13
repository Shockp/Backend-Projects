package com.shockp.weather.application.usecase.weather;

import java.io.Serial;

/**
 * Exception thrown when weather operations fail in the weather service use cases.
 * <p>
 * This exception is used to indicate failures in weather operations within the
 * application layer use cases, providing a way to distinguish weather operation
 * errors from other types of exceptions. It extends {@link RuntimeException} to avoid
 * forcing callers to handle weather operation failures if they choose not to.
 * </p>
 * <p>
 * This exception should be used when weather operations (data retrieval, location
 * validation, provider communication) fail due to underlying infrastructure issues,
 * configuration problems, or other weather-related errors.
 * </p>
 *
 * @author Weather API Wrapper Service
 * @version 1.0
 * @since 1.0
 * @see com.shockp.weather.application.usecase.weather.GetWeatherUseCase
 */
public final class WeatherOperationException extends RuntimeException {

    /** Serial version UID for serialization compatibility. */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@link WeatherOperationException} with the specified detail message.
     *
     * @param message the detail message describing the weather operation failure, may be {@code null}
     */
    public WeatherOperationException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@link WeatherOperationException} with the specified detail message and cause.
     *
     * @param message the detail message describing the weather operation failure, may be {@code null}
     * @param cause the cause of the weather operation failure, may be {@code null}
     */
    public WeatherOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@link WeatherOperationException} with the specified cause.
     *
     * @param cause the cause of the weather operation failure, may be {@code null}
     */
    public WeatherOperationException(Throwable cause) {
        super(cause);
    }
} 