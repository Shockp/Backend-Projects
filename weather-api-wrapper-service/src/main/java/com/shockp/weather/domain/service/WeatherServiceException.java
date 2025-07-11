package com.shockp.weather.domain.service;

import java.io.Serial;

/**
 * Exception thrown when weather service operations fail.
 * <p>
 * This exception is used to indicate failures in weather data retrieval,
 * processing, or other weather service operations. It provides a way to
 * distinguish weather service errors from other types of exceptions.
 * </p>
 *
 * @author Weather API Wrapper Service
 */
public class WeatherServiceException extends RuntimeException {

    /** Serial version UID for serialization compatibility. */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@link WeatherServiceException} with the specified detail message.
     *
     * @param message the detail message, may be {@code null}
     */
    public WeatherServiceException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@link WeatherServiceException} with the specified detail message and cause.
     *
     * @param message the detail message, may be {@code null}
     * @param cause the cause, may be {@code null}
     */
    public WeatherServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@link WeatherServiceException} with the specified cause.
     *
     * @param cause the cause, may be {@code null}
     */
    public WeatherServiceException(Throwable cause) {
        super(cause);
    }
} 