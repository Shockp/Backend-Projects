package com.shockp.weather.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents weather data for a specific location at a given time.
 * 
 * <p>This class encapsulates weather information including temperature, humidity,
 * description, timestamp, and location. It provides validation for data integrity
 * and ensures proper weather data representation.</p>
 * 
 * <p>Instances of this class are mutable and not thread-safe.</p>
 * 
 * @author Weather API Wrapper Service
 * @version 1.0
 * @since 1.0
 */
public final class WeatherData {
    
    /** Minimum valid humidity percentage. */
    private static final int MIN_HUMIDITY = 0;
    
    /** Maximum valid humidity percentage. */
    private static final int MAX_HUMIDITY = 100;

    /** The temperature in Celsius. */
    private final double temperature;
    
    /** The humidity percentage (0-100). */
    private final int humidity;
    
    /** The weather description. */
    private final String description;
    
    /** The timestamp when this weather data was recorded. */
    private final LocalDateTime timestamp;
    
    /** The location for this weather data. */
    private final Location location;

    /**
     * Constructs a new WeatherData with the specified weather information.
     * 
     * <p>Validates that:
     * <ul>
     *   <li>Humidity is between 0 and 100 percent</li>
     *   <li>Description is not null or empty</li>
     * </ul></p>
     * 
     * @param temperature the temperature in Celsius
     * @param humidity the humidity percentage (0-100)
     * @param description the weather description, must not be null or empty
     * @param location the location, must not be null
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public WeatherData(double temperature, int humidity, String description, Location location) {
        validateHumidity(humidity);
        validateDescription(description);

        this.temperature = temperature;
        this.humidity = humidity;
        this.description = description.trim();
        this.location = location;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Returns the temperature in Celsius.
     * 
     * @return the temperature value
     */
    public double getTemperature() {
        return temperature;
    }

    /**
     * Returns the humidity percentage.
     * 
     * @return the humidity value between 0 and 100
     */
    public int getHumidity() {
        return humidity;
    }

    /**
     * Returns the weather description.
     * 
     * @return the weather description, never null or empty
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the timestamp when this weather data was recorded.
     * 
     * @return the timestamp, never null
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the location for this weather data.
     * 
     * @return the location, never null
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Validates that humidity is within the valid range.
     * 
     * @param humidity the humidity to validate
     * @throws IllegalArgumentException if humidity is out of valid range
     */
    private static void validateHumidity(int humidity) {
        if (humidity < MIN_HUMIDITY || humidity > MAX_HUMIDITY) {
            throw new IllegalArgumentException(
                String.format("Humidity must be between %d and %d, got: %d", 
                    MIN_HUMIDITY, MAX_HUMIDITY, humidity)
            );
        }
    }

    /**
     * Validates that description is not null or empty.
     * 
     * @param description the description to validate
     * @throws IllegalArgumentException if description is null or empty
     */
    private static void validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
    }

    @Override
    public String toString() {
        return String.format("WeatherData{temperature=%.1f°C, humidity=%d%%, description='%s', timestamp=%s, location=%s}", 
            temperature, humidity, description, timestamp, location);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WeatherData other)) {
            return false;
        }
        return Double.compare(temperature, other.temperature) == 0 &&
               humidity == other.humidity &&
               Objects.equals(description, other.description) &&
               Objects.equals(timestamp, other.timestamp) &&
               Objects.equals(location, other.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(temperature, humidity, description, timestamp, location);
    }
} 