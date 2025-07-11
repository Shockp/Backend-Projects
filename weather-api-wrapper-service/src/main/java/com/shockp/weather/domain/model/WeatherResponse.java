package com.shockp.weather.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a response containing weather data and metadata about the response.
 * 
 * <p>This class encapsulates weather data along with information about whether the data
 * was retrieved from cache and when the response was generated. It provides a complete
 * response object for weather API requests.</p>
 * 
 * <p>Instances of this class are immutable and thread-safe.</p>
 * 
 * @author Weather API Wrapper Service
 * @version 1.0
 * @since 1.0
 */
public final class WeatherResponse {
    
    /** The weather data contained in this response. */
    private final WeatherData weatherData;
    
    /** Whether this response was retrieved from cache. */
    private final boolean cached;
    
    /** The timestamp when this response was generated. */
    private final LocalDateTime timestamp;

    /**
     * Constructs a new {@link WeatherResponse} with the specified weather data and cache status.
     * 
     * <p>This constructor creates a response object containing the provided weather data,
     * indicates whether the data was retrieved from cache, and sets the response timestamp
     * to the current time.</p>
     * 
     * @param weatherData the weather data to include in the response; must not be {@code null}
     * @param cached whether the weather data was retrieved from cache
     * @throws NullPointerException if {@code weatherData} is {@code null}
     */
    public WeatherResponse(WeatherData weatherData, boolean cached) {
        this.weatherData = Objects.requireNonNull(weatherData, "Weather data cannot be null");
        this.cached = cached;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Returns the weather data contained in this response.
     * 
     * @return the weather data, never {@code null}
     */
    public WeatherData getWeatherData() {
        return weatherData;
    }

    /**
     * Returns whether this response was retrieved from cache.
     * 
     * @return {@code true} if the data was cached, {@code false} otherwise
     */
    public boolean isCached() {
        return cached;
    }

    /**
     * Returns the timestamp when this response was generated.
     * 
     * @return the response timestamp, never {@code null}
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return String.format("WeatherResponse{weatherData=%s, cached=%s, timestamp=%s}",
            weatherData, cached, timestamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WeatherResponse other)) {
            return false;
        }
        return Objects.equals(weatherData, other.weatherData) &&
               cached == other.cached &&
               Objects.equals(timestamp, other.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(weatherData, cached, timestamp);
    }
} 