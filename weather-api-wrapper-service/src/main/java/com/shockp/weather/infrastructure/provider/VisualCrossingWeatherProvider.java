package com.shockp.weather.infrastructure.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shockp.weather.application.port.WeatherProviderPort;
import com.shockp.weather.domain.model.Location;
import com.shockp.weather.domain.model.WeatherData;
import com.shockp.weather.domain.model.WeatherRequest;
import com.shockp.weather.domain.service.WeatherServiceException;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Visual Crossing Weather API provider implementation.
 * <p>
 * This class implements the {@link WeatherProviderPort} interface to provide weather data
 * from the Visual Crossing Weather API. It includes comprehensive error handling, input
 * validation, security measures, and proper response parsing.
 * </p>
 * <p>
 * Security features implemented:
 * <ul>
 *   <li>Input validation and sanitization to prevent injection attacks</li>
 *   <li>API key validation and secure handling</li>
 *   <li>URL encoding to prevent URL manipulation</li>
 *   <li>Response validation to prevent malicious data injection</li>
 *   <li>Timeout handling to prevent resource exhaustion</li>
 * </ul>
 * </p>
 * <p>
 * The provider supports both location-based and coordinate-based weather requests,
 * with proper error handling for various API response scenarios.
 * </p>
 *
 * @author Weather API Wrapper Service
 * @version 1.0
 * @since 1.0
 * @see com.shockp.weather.application.port.WeatherProviderPort
 * @see com.shockp.weather.domain.model.WeatherRequest
 * @see com.shockp.weather.domain.model.WeatherData
 */
@Component
public final class VisualCrossingWeatherProvider implements WeatherProviderPort {

    /** Logger for this class. */
    private static final Logger logger = LoggerFactory.getLogger(VisualCrossingWeatherProvider.class);

    /** Provider name constant. */
    private static final String PROVIDER_NAME = "Visual Crossing Weather API";

    /** Date format for API requests. */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** Pattern for validating API keys. */
    private static final Pattern API_KEY_PATTERN = Pattern.compile("^[A-Za-z0-9]{32,}$");

    /** Pattern for validating location strings. */
    private static final Pattern LOCATION_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-',.]+$");

    /** Maximum location string length to prevent DoS attacks. */
    private static final int MAX_LOCATION_LENGTH = 200;

    /** Minimum location string length. */
    private static final int MIN_LOCATION_LENGTH = 1;

    /** Default timeout for API requests in seconds. */
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;

    /** Maximum temperature value in Celsius. */
    private static final double MAX_TEMPERATURE = 60.0;

    /** Minimum temperature value in Celsius. */
    private static final double MIN_TEMPERATURE = -90.0;

    /** The WebClient for making HTTP requests. */
    private final WebClient webClient;

    /** The API key for Visual Crossing Weather API. */
    private final String apiKey;

    /** The base URL for the Visual Crossing Weather API. */
    private final String baseUrl;

    /** The ObjectMapper for JSON parsing. */
    private final ObjectMapper objectMapper;

    /** Provider availability status. */
    private volatile boolean isAvailable = true;

    /**
     * Constructs a new {@link VisualCrossingWeatherProvider} with the specified dependencies.
     *
     * @param webClient the WebClient for HTTP requests, must not be {@code null}
     * @param apiKey the Visual Crossing API key, must not be {@code null} or empty
     * @param baseUrl the base URL for the API, must not be {@code null} or empty
     * @throws NullPointerException if any parameter is {@code null}
     * @throws IllegalArgumentException if {@code apiKey} or {@code baseUrl} is empty or invalid
     */
    public VisualCrossingWeatherProvider(
            WebClient webClient,
            @Value("${weather.visualcrossing.api-key}") String apiKey,
            @Value("${weather.visualcrossing.base-url}") String baseUrl) {
        
        this.webClient = Objects.requireNonNull(webClient, "WebClient cannot be null");
        this.apiKey = validateAndSanitizeApiKey(apiKey);
        this.baseUrl = validateAndSanitizeBaseUrl(baseUrl);
        this.objectMapper = new ObjectMapper();
        
        logger.info("VisualCrossingWeatherProvider initialized with base URL: {}", baseUrl);
    }

    @Override
    public WeatherData getWeatherData(WeatherRequest request) {
        Objects.requireNonNull(request, "Weather request cannot be null");
        
        try {
            logger.debug("Retrieving weather data for request: {}", request);
            
            String url = buildUrl(request);
            logger.debug("Built API URL: {}", url.replace(apiKey, "***"));
            
            String response = makeApiRequest(url);
            WeatherData weatherData = parseResponse(response, request.getLocation());
            
            logger.debug("Successfully retrieved weather data: {}", weatherData);
            return weatherData;
            
        } catch (WebClientResponseException e) {
            handleApiError(e);
            throw new WeatherServiceException("Failed to retrieve weather data from Visual Crossing API", e);
        } catch (Exception e) {
            logger.error("Unexpected error while retrieving weather data for request: {}", request, e);
            throw new WeatherServiceException("Unexpected error while retrieving weather data", e);
        }
    }

    @Override
    public boolean isAvailable() {
        return isAvailable;
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    /**
     * Builds the API URL for the weather request.
     * <p>
     * This method constructs a properly formatted URL for the Visual Crossing Weather API,
     * including proper URL encoding and parameter validation.
     * </p>
     *
     * @param request the weather request, must not be {@code null}
     * @return the complete API URL
     * @throws IllegalArgumentException if the request parameters are invalid
     */
    private String buildUrl(WeatherRequest request) {
        Location location = request.getLocation();
        LocalDate date = request.getDate();
        
        // Build location string
        String locationString = buildLocationString(location);
        
        // Format date
        String dateString = date.format(DATE_FORMATTER);
        
        // Build URL with proper encoding
        return String.format("%s/%s/%s?key=%s&unitGroup=metric&contentType=json",
            baseUrl,
            URLEncoder.encode(locationString, StandardCharsets.UTF_8),
            dateString,
            URLEncoder.encode(apiKey, StandardCharsets.UTF_8));
    }

    /**
     * Builds a location string from the location object.
     * <p>
     * This method creates a location string suitable for the API, handling both
     * coordinate-based and name-based locations.
     * </p>
     *
     * @param location the location object, must not be {@code null}
     * @return the formatted location string
     * @throws IllegalArgumentException if the location is invalid
     */
    private String buildLocationString(Location location) {
        Objects.requireNonNull(location, "Location cannot be null");
        
        // If coordinates are provided, use them
        if (location.getLatitude() != 0.0 || location.getLongitude() != 0.0) {
            return String.format("%.6f,%.6f", location.getLatitude(), location.getLongitude());
        }
        
        // Otherwise, use city and country
        String city = validateAndSanitizeLocationPart(location.getCity(), "city");
        String country = validateAndSanitizeLocationPart(location.getCountry(), "country");
        
        return String.format("%s,%s", city, country);
    }

    /**
     * Makes the API request and returns the response.
     * <p>
     * This method handles the HTTP request with proper timeout and error handling.
     * </p>
     *
     * @param url the API URL to request
     * @return the API response as a string
     * @throws WebClientResponseException if the API returns an error response
     */
    private String makeApiRequest(String url) {
        return webClient.get()
            .uri(URI.create(url))
            .retrieve()
            .bodyToMono(String.class)
            .timeout(java.time.Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
            .block();
    }

    /**
     * Parses the API response and creates a WeatherData object.
     * <p>
     * This method validates the response structure and extracts weather information
     * with proper error handling for malformed responses.
     * </p>
     *
     * @param response the API response JSON string
     * @param location the location for the weather data
     * @return the parsed WeatherData object
     * @throws WeatherServiceException if the response cannot be parsed or is invalid
     */
    private WeatherData parseResponse(String response, Location location) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            
            // Validate response structure
            if (!rootNode.has("days") || !rootNode.get("days").isArray() || rootNode.get("days").size() == 0) {
                throw new WeatherServiceException("Invalid response structure: missing or empty 'days' array");
            }
            
            JsonNode dayNode = rootNode.get("days").get(0);
            
            // Extract weather data with validation
            double temperature = extractTemperature(dayNode);
            int humidity = extractHumidity(dayNode);
            String description = extractDescription(dayNode);
            
            return new WeatherData(temperature, humidity, description, location);
            
        } catch (Exception e) {
            logger.error("Failed to parse API response: {}", response, e);
            throw new WeatherServiceException("Failed to parse weather data response", e);
        }
    }

    /**
     * Extracts temperature from the response node.
     *
     * @param dayNode the day node from the response
     * @return the temperature in Celsius
     * @throws WeatherServiceException if temperature is missing or invalid
     */
    private double extractTemperature(JsonNode dayNode) {
        if (!dayNode.has("temp")) {
            throw new WeatherServiceException("Temperature data missing from response");
        }
        
        double temperature = dayNode.get("temp").asDouble();
        
        if (temperature < MIN_TEMPERATURE || temperature > MAX_TEMPERATURE) {
            throw new WeatherServiceException(
                String.format("Temperature out of valid range [%.1f, %.1f]: %.1f", 
                    MIN_TEMPERATURE, MAX_TEMPERATURE, temperature));
        }
        
        return temperature;
    }

    /**
     * Extracts humidity from the response node.
     *
     * @param dayNode the day node from the response
     * @return the humidity percentage
     * @throws WeatherServiceException if humidity is missing or invalid
     */
    private int extractHumidity(JsonNode dayNode) {
        if (!dayNode.has("humidity")) {
            throw new WeatherServiceException("Humidity data missing from response");
        }
        
        int humidity = dayNode.get("humidity").asInt();
        
        if (humidity < 0 || humidity > 100) {
            throw new WeatherServiceException(
                String.format("Humidity out of valid range [0, 100]: %d", humidity));
        }
        
        return humidity;
    }

    /**
     * Extracts description from the response node.
     *
     * @param dayNode the day node from the response
     * @return the weather description
     * @throws WeatherServiceException if description is missing or invalid
     */
    private String extractDescription(JsonNode dayNode) {
        if (!dayNode.has("conditions")) {
            throw new WeatherServiceException("Weather conditions missing from response");
        }
        
        String description = dayNode.get("conditions").asText().trim();
        
        if (description.isEmpty()) {
            throw new WeatherServiceException("Weather description is empty");
        }
        
        return description;
    }

    /**
     * Handles API errors and updates provider availability.
     *
     * @param exception the WebClient response exception
     * @throws WeatherServiceException with appropriate error message
     */
    private void handleApiError(WebClientResponseException exception) {
        int statusCode = exception.getStatusCode().value();
        String responseBody = exception.getResponseBodyAsString();
        
        logger.error("Visual Crossing API error - Status: {}, Response: {}", statusCode, responseBody);
        
        // Mark provider as unavailable for certain error types
        if (statusCode >= 500) {
            isAvailable = false;
            logger.warn("Marking Visual Crossing provider as unavailable due to server error");
        }
        
        String errorMessage = switch (statusCode) {
            case 400 -> "Invalid request parameters";
            case 401 -> "Invalid API key";
            case 403 -> "API key does not have permission for this request";
            case 429 -> "Rate limit exceeded";
            case 500 -> "Internal server error";
            case 503 -> "Service temporarily unavailable";
            default -> "API request failed with status: " + statusCode;
        };
        
        throw new WeatherServiceException(errorMessage, exception);
    }

    /**
     * Validates and sanitizes the API key.
     *
     * @param apiKey the API key to validate
     * @return the sanitized API key
     * @throws IllegalArgumentException if the API key is invalid
     */
    private String validateAndSanitizeApiKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key cannot be null or empty");
        }
        
        String sanitizedKey = apiKey.trim();
        
        if (!API_KEY_PATTERN.matcher(sanitizedKey).matches()) {
            throw new IllegalArgumentException("API key format is invalid");
        }
        
        return sanitizedKey;
    }

    /**
     * Validates and sanitizes the base URL.
     *
     * @param baseUrl the base URL to validate
     * @return the sanitized base URL
     * @throws IllegalArgumentException if the base URL is invalid
     */
    private String validateAndSanitizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Base URL cannot be null or empty");
        }
        
        String sanitizedUrl = baseUrl.trim();
        
        if (!sanitizedUrl.startsWith("https://")) {
            throw new IllegalArgumentException("Base URL must use HTTPS protocol");
        }
        
        return sanitizedUrl;
    }

    /**
     * Validates and sanitizes location parts (city/country).
     *
     * @param locationPart the location part to validate
     * @param partName the name of the part for error messages
     * @return the sanitized location part
     * @throws IllegalArgumentException if the location part is invalid
     */
    private String validateAndSanitizeLocationPart(String locationPart, String partName) {
        if (locationPart == null || locationPart.trim().isEmpty()) {
            throw new IllegalArgumentException(partName + " cannot be null or empty");
        }
        
        String sanitized = locationPart.trim();
        
        if (sanitized.length() < MIN_LOCATION_LENGTH) {
            throw new IllegalArgumentException(
                String.format("%s must be at least %d characters long", partName, MIN_LOCATION_LENGTH));
        }
        
        if (sanitized.length() > MAX_LOCATION_LENGTH) {
            throw new IllegalArgumentException(
                String.format("%s cannot exceed %d characters", partName, MAX_LOCATION_LENGTH));
        }
        
        if (!LOCATION_PATTERN.matcher(sanitized).matches()) {
            throw new IllegalArgumentException(
                String.format("%s contains invalid characters", partName));
        }
        
        return sanitized;
    }
} 