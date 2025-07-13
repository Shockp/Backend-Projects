package com.shockp.weather.application.usecase.weather;

import java.time.LocalDate;
import java.util.Objects;
import java.util.regex.Pattern;

import com.shockp.weather.domain.model.Location;
import com.shockp.weather.domain.model.WeatherRequest;
import com.shockp.weather.domain.model.WeatherResponse;
import com.shockp.weather.domain.service.WeatherService;
import com.shockp.weather.domain.service.WeatherServiceException;

/**
 * Use case for retrieving weather data in the weather service.
 * <p>
 * This use case provides a high-level interface for weather data retrieval operations,
 * including location-based and coordinate-based weather requests. It implements
 * security measures to prevent injection attacks and ensures proper validation
 * of all inputs before processing weather requests.
 * </p>
 * <p>
 * The use case follows the single responsibility principle and encapsulates all
 * weather retrieval business logic, providing a clean interface for the application layer.
 * It handles both location name-based and coordinate-based weather requests with
 * comprehensive validation and error handling.
 * </p>
 *
 * @author Weather API Wrapper Service
 * @version 1.0
 * @since 1.0
 * @see com.shockp.weather.domain.service.WeatherService
 * @see com.shockp.weather.domain.model.WeatherRequest
 * @see com.shockp.weather.domain.model.WeatherResponse
 * @see com.shockp.weather.application.usecase.weather.WeatherOperationException
 */
public final class GetWeatherUseCase {

    /** Maximum allowed city name length to prevent DoS attacks. */
    private static final int MAX_CITY_LENGTH = 100;

    /** Maximum allowed country name length to prevent DoS attacks. */
    private static final int MAX_COUNTRY_LENGTH = 100;

    /** Minimum required city name length. */
    private static final int MIN_CITY_LENGTH = 1;

    /** Minimum required country name length. */
    private static final int MIN_COUNTRY_LENGTH = 1;

    /** Pattern for validating city names to prevent injection attacks. */
    private static final Pattern CITY_PATTERN = Pattern.compile("^[a-zA-Z\\s\\-']+$");

    /** Pattern for validating country names to prevent injection attacks. */
    private static final Pattern COUNTRY_PATTERN = Pattern.compile("^[a-zA-Z\\s\\-']+$");

    /** Maximum number of days in the future for weather requests. */
    private static final int MAX_FUTURE_DAYS = 30;

    /** Maximum number of days in the past for weather requests. */
    private static final int MAX_PAST_DAYS = 365;

    /** The weather service for weather data operations. */
    private final WeatherService weatherService;

    /**
     * Constructs a new {@link GetWeatherUseCase} with the specified weather service.
     *
     * @param weatherService the weather service for weather data operations, must not be {@code null}
     * @throws NullPointerException if {@code weatherService} is {@code null}
     */
    public GetWeatherUseCase(WeatherService weatherService) {
        this.weatherService = Objects.requireNonNull(weatherService, "Weather service cannot be null");
    }

    /**
     * Executes weather data retrieval for a location-based request.
     * <p>
     * This method validates the input parameters, creates a weather request,
     * and retrieves weather data for the specified location and date.
     * </p>
     *
     * @param city the city name, must not be {@code null} or empty
     * @param country the country name, must not be {@code null} or empty
     * @param date the date for weather data, must not be {@code null}
     * @param includeHourly whether to include hourly weather data
     * @return a {@link WeatherResponse} containing the weather data and cache status
     * @throws IllegalArgumentException if any parameter is invalid
     * @throws WeatherOperationException if the weather retrieval operation fails
     */
    public WeatherResponse execute(String city, String country, LocalDate date, boolean includeHourly) {
        validateAndSanitizeCity(city);
        validateAndSanitizeCountry(country);
        validateDate(date);

        try {
            Location location = new Location(0.0, 0.0, city, country); // Coordinates will be resolved by provider
            WeatherRequest request = new WeatherRequest(location, date, includeHourly);
            return weatherService.getWeather(request);
        } catch (WeatherServiceException e) {
            throw new WeatherOperationException("Failed to retrieve weather data for location: " + city + ", " + country, e);
        }
    }

    /**
     * Executes weather data retrieval for a coordinate-based request.
     * <p>
     * This method validates the input parameters, creates a weather request,
     * and retrieves weather data for the specified coordinates and date.
     * </p>
     *
     * @param latitude the latitude coordinate in decimal degrees
     * @param longitude the longitude coordinate in decimal degrees
     * @param city the city name for reference, must not be {@code null} or empty
     * @param country the country name for reference, must not be {@code null} or empty
     * @param date the date for weather data, must not be {@code null}
     * @param includeHourly whether to include hourly weather data
     * @return a {@link WeatherResponse} containing the weather data and cache status
     * @throws IllegalArgumentException if any parameter is invalid
     * @throws WeatherOperationException if the weather retrieval operation fails
     */
    public WeatherResponse execute(double latitude, double longitude, String city, String country, 
                                 LocalDate date, boolean includeHourly) {
        validateCoordinates(latitude, longitude);
        validateAndSanitizeCity(city);
        validateAndSanitizeCountry(country);
        validateDate(date);

        try {
            Location location = new Location(latitude, longitude, city, country);
            WeatherRequest request = new WeatherRequest(location, date, includeHourly);
            return weatherService.getWeather(request);
        } catch (WeatherServiceException e) {
            throw new WeatherOperationException(
                String.format("Failed to retrieve weather data for coordinates: %.6f, %.6f", latitude, longitude), e);
        }
    }

    /**
     * Executes weather data retrieval for a custom weather request.
     * <p>
     * This method validates the weather request and retrieves weather data.
     * It provides the most flexible interface for weather data retrieval.
     * </p>
     *
     * @param request the {@link WeatherRequest} containing location, date, and options, must not be {@code null}
     * @return a {@link WeatherResponse} containing the weather data and cache status
     * @throws IllegalArgumentException if the request is invalid
     * @throws NullPointerException if {@code request} is {@code null}
     * @throws WeatherOperationException if the weather retrieval operation fails
     */
    public WeatherResponse execute(WeatherRequest request) {
        Objects.requireNonNull(request, "Weather request cannot be null");
        validateWeatherRequest(request);

        try {
            return weatherService.getWeather(request);
        } catch (WeatherServiceException e) {
            throw new WeatherOperationException("Failed to retrieve weather data for request: " + request, e);
        }
    }

    /**
     * Checks if the weather service is currently available.
     * <p>
     * This method verifies that the underlying weather provider is available
     * and can handle weather data requests.
     * </p>
     *
     * @return {@code true} if the weather service is available, {@code false} otherwise
     */
    public boolean isServiceAvailable() {
        return weatherService.isAvailable();
    }

    /**
     * Gets the name of the current weather provider.
     * <p>
     * This method returns the name of the weather provider being used
     * for weather data retrieval.
     * </p>
     *
     * @return the provider name, never {@code null} or empty
     */
    public String getProviderName() {
        return weatherService.getProviderName();
    }

    /**
     * Validates and sanitizes the city name.
     * <p>
     * This method performs comprehensive validation of the city name:
     * <ul>
     *   <li>Ensures the city name is not {@code null} or empty</li>
     *   <li>Validates city name length is within acceptable bounds</li>
     *   <li>Checks city name format to prevent injection attacks</li>
     *   <li>Trims whitespace from the city name</li>
     * </ul>
     * </p>
     *
     * @param city the city name to validate and sanitize
     * @throws IllegalArgumentException if the city name is invalid
     */
    private void validateAndSanitizeCity(String city) {
        if (city == null) {
            throw new IllegalArgumentException("City name cannot be null");
        }

        String sanitizedCity = city.trim();
        
        if (sanitizedCity.isEmpty()) {
            throw new IllegalArgumentException("City name cannot be empty");
        }

        if (sanitizedCity.length() < MIN_CITY_LENGTH) {
            throw new IllegalArgumentException(
                String.format("City name must be at least %d characters long", MIN_CITY_LENGTH));
        }

        if (sanitizedCity.length() > MAX_CITY_LENGTH) {
            throw new IllegalArgumentException(
                String.format("City name cannot exceed %d characters", MAX_CITY_LENGTH));
        }

        if (!CITY_PATTERN.matcher(sanitizedCity).matches()) {
            throw new IllegalArgumentException(
                "City name contains invalid characters. Only letters, spaces, hyphens, and apostrophes are allowed");
        }
    }

    /**
     * Validates and sanitizes the country name.
     * <p>
     * This method performs comprehensive validation of the country name:
     * <ul>
     *   <li>Ensures the country name is not {@code null} or empty</li>
     *   <li>Validates country name length is within acceptable bounds</li>
     *   <li>Checks country name format to prevent injection attacks</li>
     *   <li>Trims whitespace from the country name</li>
     * </ul>
     * </p>
     *
     * @param country the country name to validate and sanitize
     * @throws IllegalArgumentException if the country name is invalid
     */
    private void validateAndSanitizeCountry(String country) {
        if (country == null) {
            throw new IllegalArgumentException("Country name cannot be null");
        }

        String sanitizedCountry = country.trim();
        
        if (sanitizedCountry.isEmpty()) {
            throw new IllegalArgumentException("Country name cannot be empty");
        }

        if (sanitizedCountry.length() < MIN_COUNTRY_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Country name must be at least %d characters long", MIN_COUNTRY_LENGTH));
        }

        if (sanitizedCountry.length() > MAX_COUNTRY_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Country name cannot exceed %d characters", MAX_COUNTRY_LENGTH));
        }

        if (!COUNTRY_PATTERN.matcher(sanitizedCountry).matches()) {
            throw new IllegalArgumentException(
                "Country name contains invalid characters. Only letters, spaces, hyphens, and apostrophes are allowed");
        }
    }

    /**
     * Validates coordinate values are within valid ranges.
     * <p>
     * This method ensures that latitude and longitude values are within
     * the valid geographical coordinate ranges.
     * </p>
     *
     * @param latitude the latitude coordinate to validate
     * @param longitude the longitude coordinate to validate
     * @throws IllegalArgumentException if coordinates are out of valid ranges
     */
    private void validateCoordinates(double latitude, double longitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException(
                String.format("Latitude must be between -90.0 and 90.0 degrees, got: %.6f", latitude));
        }

        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException(
                String.format("Longitude must be between -180.0 and 180.0 degrees, got: %.6f", longitude));
        }
    }

    /**
     * Validates the date for weather requests.
     * <p>
     * This method ensures that the requested date is within acceptable
     * bounds for weather data retrieval.
     * </p>
     *
     * @param date the date to validate
     * @throws IllegalArgumentException if the date is invalid
     * @throws NullPointerException if the date is {@code null}
     */
    private void validateDate(LocalDate date) {
        Objects.requireNonNull(date, "Date cannot be null");

        LocalDate today = LocalDate.now();
        LocalDate maxFutureDate = today.plusDays(MAX_FUTURE_DAYS);
        LocalDate maxPastDate = today.minusDays(MAX_PAST_DAYS);

        if (date.isAfter(maxFutureDate)) {
            throw new IllegalArgumentException(
                String.format("Date cannot be more than %d days in the future", MAX_FUTURE_DAYS));
        }

        if (date.isBefore(maxPastDate)) {
            throw new IllegalArgumentException(
                String.format("Date cannot be more than %d days in the past", MAX_PAST_DAYS));
        }
    }

    /**
     * Validates a weather request for completeness and correctness.
     * <p>
     * This method performs additional validation on the weather request
     * beyond what the domain models provide.
     * </p>
     *
     * @param request the weather request to validate
     * @throws IllegalArgumentException if the request is invalid
     */
    private void validateWeatherRequest(WeatherRequest request) {
        // Additional validation can be added here if needed
        // The domain models already provide comprehensive validation
        weatherService.validateRequest(request);
    }
} 