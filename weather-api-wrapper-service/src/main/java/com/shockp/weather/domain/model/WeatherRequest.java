package com.shockp.weather.domain.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a request for weather data for a specific location and date.
 * 
 * <p>This class encapsulates weather request information including location, date,
 * and whether to include hourly data. The location and date validation is handled
 * by their respective classes ({@link Location} and {@link LocalDate}).</p>
 * 
 * <p>Instances of this class are immutable and thread-safe.</p>
 * 
 * @author Weather API Wrapper Service
 * @version 1.0
 * @since 1.0
 */
public final class WeatherRequest {
    
    /** The location for which weather data is requested. */
    private final Location location;
    
    /** The date for which weather data is requested. */
    private final LocalDate date;
    
    /** Whether to include hourly weather data. */
    private final boolean includeHourly;

    /**
     * Constructs a new {@code WeatherRequest} by assigning the provided location, date,
     * and includeHourly flag to the corresponding fields.
     *
     * <p>This constructor ensures that both {@code location} and {@code date} are not null,
     * throwing a {@link NullPointerException} if either is null. The {@code includeHourly}
     * parameter is directly assigned. Validation of the location and date values themselves
     * is delegated to the {@link Location} and {@link LocalDate} classes, respectively.</p>
     *
     * @param location the location for which weather data is requested; must not be null
     * @param date the date for which weather data is requested; must not be null
     * @param includeHourly whether to include hourly weather data in the request
     * @throws NullPointerException if {@code location} or {@code date} is null
     */
    public WeatherRequest(Location location, LocalDate date, boolean includeHourly) {
        this.location = Objects.requireNonNull(location, "Location cannot be null");
        this.date = Objects.requireNonNull(date, "Date cannot be null");
        this.includeHourly = includeHourly;
    }

    /**
     * Returns the location for which weather data is requested.
     * 
     * @return the location, never {@code null}
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Returns the date for which weather data is requested.
     * 
     * @return the date, never {@code null}
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Returns whether hourly weather data should be included.
     * 
     * @return {@code true} if hourly data should be included, {@code false} otherwise
     */
    public boolean isIncludeHourly() {
        return includeHourly;
    }

    @Override
    public String toString() {
        return String.format("WeatherRequest{location=%s, date=%s, includeHourly=%s}", 
            location, date, includeHourly);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WeatherRequest other)) {
            return false;
        }
        return Objects.equals(location, other.location) &&
               Objects.equals(date, other.date) &&
               includeHourly == other.includeHourly;
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, date, includeHourly);
    }
} 