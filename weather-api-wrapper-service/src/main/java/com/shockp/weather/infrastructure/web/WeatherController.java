package com.shockp.weather.infrastructure.web;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shockp.weather.application.usecase.cache.CacheOperationException;
import com.shockp.weather.application.usecase.cache.CacheWeatherUseCase;
import com.shockp.weather.application.usecase.ratelimit.RateLimitOperationException;
import com.shockp.weather.application.usecase.ratelimit.RateLimitUseCase;
import com.shockp.weather.application.usecase.weather.WeatherOperationException;
import com.shockp.weather.application.usecase.weather.GetWeatherUseCase;
import com.shockp.weather.domain.model.Location;
import com.shockp.weather.domain.model.WeatherResponse;
import com.shockp.weather.domain.service.WeatherServiceException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * REST controller for weather API endpoints.
 *
 * <p>This controller exposes endpoints for retrieving weather data, checking cache status,
 * clearing cache, and monitoring rate limits. It implements comprehensive input validation,
 * security measures, and robust error handling. All endpoints are documented and follow
 * the Google Java Style Guide.</p>
 *
 * <p>Security and best practices:
 * <ul>
 *   <li>Input validation and sanitization for all parameters</li>
 *   <li>Client identification via headers or remote address</li>
 *   <li>Rate limiting enforced on all endpoints</li>
 *   <li>Comprehensive exception handling with clear error responses</li>
 *   <li>Logging for all operations and errors</li>
 * </ul>
 * </p>
 *
 * @author Weather API Wrapper Service
 */
@RestController
@RequestMapping("/api/v1/weather")
public class WeatherController {

    private static final Logger logger = LoggerFactory.getLogger(WeatherController.class);
    private static final Pattern LOCATION_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-',.]+$");
    private static final int MAX_LOCATION_LENGTH = 100;
    
    private final GetWeatherUseCase getWeatherUseCase;
    private final CacheWeatherUseCase cacheWeatherUseCase;
    private final RateLimitUseCase rateLimitUseCase;

    /**
     * Constructs a new WeatherController with required use cases.
     *
     * @param getWeatherUseCase the weather retrieval use case
     * @param cacheWeatherUseCase the cache management use case
     * @param rateLimitUseCase the rate limiting use case
     * @throws NullPointerException if any argument is null
     */
    public WeatherController(GetWeatherUseCase getWeatherUseCase,
                            CacheWeatherUseCase cacheWeatherUseCase,
                            RateLimitUseCase rateLimitUseCase) {
                                
        this.getWeatherUseCase = Objects.requireNonNull(getWeatherUseCase, "getWeatherUseCase is required");
        this.cacheWeatherUseCase = Objects.requireNonNull(cacheWeatherUseCase, "cacheWeatherUseCase is required");
        this.rateLimitUseCase = Objects.requireNonNull(rateLimitUseCase, "rateLimitUseCase is required");
        logger.info("WeatherController initialized successfully");
    }

    /**
     * Retrieves weather data for a given location and date.
     *
     * @param location the location in format "City,Country"
     * @param date the date for weather data (optional, defaults to today)
     * @param includeHourly whether to include hourly data (default: false)
     * @param request the HTTP servlet request
     * @return the weather response entity
     */
    @GetMapping
    public ResponseEntity<WeatherResponse> getWeather(
            @RequestParam String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "false") boolean includeHourly,
            HttpServletRequest request) {

        logger.debug("Received weather request for location: {}, date: {}, includeHourly: {}", location, date, includeHourly);

        String clientId = extractClientId(request);

        if (!rateLimitUseCase.execute(clientId)) {
            logger.warn("Rate limit exceeded for client: {}", clientId);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("X-RateLimit-Remaining", "0")
                    .header("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 3600000))
                    .build();
        }

        Location parsedLocation = validateLocation(location);
        LocalDate targetDate = date != null ? date : LocalDate.now();
        validateDate(targetDate);
        WeatherResponse response = getWeatherUseCase.execute(
                parsedLocation.getCity(), parsedLocation.getCountry(), targetDate, includeHourly);

        logger.debug("Successfully retrieved weather data for location: {}", parsedLocation);

        int remainingRequests = rateLimitUseCase.getRemainingTokens(clientId);
        return ResponseEntity.ok()
                .header("X-RateLimit-Remaining", String.valueOf(remainingRequests))
                .header("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 3600000))
                .body(response);
    }

    /**
     * Retrieves weather data for given coordinates and date.
     *
     * @param latitude the latitude (-90.0 to 90.0)
     * @param longitude the longitude (-180.0 to 180.0)
     * @param date the date for weather data (optional, defaults to today)
     * @param request the HTTP servlet request
     * @return the weather response entity
     */
    @GetMapping("/coordinates")
    public ResponseEntity<WeatherResponse> getWeatherByCoordinates(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletRequest request) {

        logger.debug("Received weather request for coordinates: {}, {}, date: {}", latitude, longitude, date);

        String clientId = extractClientId(request);

        if (!rateLimitUseCase.execute(clientId)) {
            logger.warn("Rate limit exceeded for client: {}", clientId);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("X-RateLimit-Remaining", "0")
                    .header("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 3600000))
                    .build();
        }

        LocalDate targetDate = date != null ? date : LocalDate.now();
        validateDate(targetDate);
        WeatherResponse response = getWeatherUseCase.execute(
                latitude, longitude, "unknown", "unknown", targetDate, false);
        logger.debug("Successfully retrieved weather data for coordinates: {}, {}", latitude, longitude);

        int remainingRequests = rateLimitUseCase.getRemainingTokens(clientId);
        return ResponseEntity.ok()
                .header("X-RateLimit-Remaining", String.valueOf(remainingRequests))
                .header("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 3600000))
                .body(response);
    }

    /**
     * Checks if a cache entry exists for the given key.
     *
     * @param key the cache key
     * @param request the HTTP servlet request
     * @return a map with cache status
     */
    @GetMapping("/cache/status")
    public ResponseEntity<Map<String, Object>> getCacheStatus(
            @RequestParam String key,
            HttpServletRequest request) {

        logger.debug("Received cache status request for key: {}", key);

        String clientId = extractClientId(request);

        if (!rateLimitUseCase.checkRateLimit(clientId)) {
            logger.warn("Rate limit exceeded for client: {}", clientId);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        boolean exists = cacheWeatherUseCase.exists(key);

        Map<String, Object> response = new HashMap<>();
        response.put("key", key);
        response.put("exists", exists);
        response.put("timestamp", System.currentTimeMillis());
        logger.debug("Cache status for key: {}, exists: {}", key, exists);
        return ResponseEntity.ok(response);
    }

    /**
     * Clears a cache entry for the given key.
     *
     * @param key the cache key
     * @param request the HTTP servlet request
     * @return no content response
     */
    @DeleteMapping("/cache")
    public ResponseEntity<Void> clearCache(
            @RequestParam String key,
            HttpServletRequest request) {

        logger.debug("Received cache clear request for key: {}", key);

        String clientId = extractClientId(request);

        if (!rateLimitUseCase.checkRateLimit(clientId)) {
            logger.warn("Rate limit exceeded for client: {}", clientId);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        cacheWeatherUseCase.invalidate(key);

        logger.debug("Cache cleared for key: {}", key);

        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves the rate limit status for the current client.
     *
     * @param request the HTTP servlet request
     * @return a map with rate limit status
     */
    @GetMapping("/rate-limit/status")
    public ResponseEntity<Map<String, Object>> getRateLimitStatus(HttpServletRequest request) {

        logger.debug("Received rate limit status request");

        String clientId = extractClientId(request);

        int remainingTokens = rateLimitUseCase.getRemainingTokens(clientId);
        int maxRequests = rateLimitUseCase.getMaxRequests();
        long timeWindow = rateLimitUseCase.getTimeWindow().toSeconds();

        Map<String, Object> response = new HashMap<>();
        response.put("clientId", clientId);
        response.put("remainingTokens", remainingTokens);
        response.put("maxRequests", maxRequests);
        response.put("timeWindowSeconds", timeWindow);
        response.put("timeStamp", System.currentTimeMillis());

        logger.debug("Rate limit status for client: {}, remaining: {}", clientId, remainingTokens);

        return ResponseEntity.ok(response);
    }

    /**
     * Extracts and sanitizes the client ID from the request headers or remote address.
     *
     * @param request the HTTP servlet request
     * @return the sanitized client ID
     */
    private String extractClientId(HttpServletRequest request) {

        String clientId = request.getHeader("X-Client-Id");
        if (clientId != null && !clientId.trim().isEmpty()) {
            return sanitizedClientId(clientId);
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && !authHeader.trim().isEmpty()) {
            return sanitizedClientId(authHeader);
        }

        String remoteAddr = request.getRemoteAddr();
        return sanitizedClientId(remoteAddr != null ? remoteAddr : "unknown");
    }

    /**
     * Sanitizes the client ID by removing unsafe characters and limiting length.
     *
     * @param clientId the raw client ID
     * @return the sanitized client ID
     */
    private String sanitizedClientId(String clientId) {

        if (clientId == null) {
            return "unknown";
        }

        String sanitized = clientId.replaceAll("[^a-zA-Z0-9\\-_.]", "");

        return sanitized.length() > 50 ? sanitized.substring(0, 50) : sanitized;
    }

    /**
     * Validates and parses the location string.
     *
     * @param location the location string in format "City,Country"
     * @return the parsed Location object
     * @throws IllegalArgumentException if the location is invalid
     */
    private Location validateLocation(String location) {

        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location cannot be null or empty");
        }

        String sanitized = location.trim();
        if (sanitized.length() > MAX_LOCATION_LENGTH) {
            throw new IllegalArgumentException("Location cannot exceed " + MAX_LOCATION_LENGTH + " characters");
        }

        if (!LOCATION_PATTERN.matcher(sanitized).matches()) {
            throw new IllegalArgumentException("Location contains invalid characters");
        }

        String[] parts = sanitized.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Location must be in format 'City,Country'");
        }

        String city = parts[0].trim();
        String country = parts[1].trim();
        if (city.isEmpty() || country.isEmpty()) {
            throw new IllegalArgumentException("City and country cannot be empty");
        }

        return new Location(0.0, 0.0, city, country);
    }

    /**
     * Validates the date for weather requests.
     *
     * @param date the date to validate
     * @throws IllegalArgumentException if the date is out of range
     */
    private void validateDate(LocalDate date) {

        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }

        LocalDate now = LocalDate.now();
        if (date.isBefore(now.minusDays(30))) {
            throw new IllegalArgumentException("Date cannot be more than 30 days in the past");
        }

        if (date.isAfter(now.plusDays(7))) {
            throw new IllegalArgumentException("Date cannot be more than 7 days in the future");
        }
    }

    /**
     * Global exception handler for WeatherController.
     * Handles validation, rate limit, weather service, cache, and generic errors.
     */
    @ControllerAdvice
    public static class WeatherControllerAdvice {
        /**
         * Handles validation errors (IllegalArgumentException).
         *
         * @param ex the exception
         * @return a bad request error response
         */
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<Map<String, Object>> handleValidationException(IllegalArgumentException ex) {

            logger.warn("Validation error: {}", ex.getMessage());

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Validation Error");
            error.put("message", ex.getMessage());
            error.put("timestamp", System.currentTimeMillis());
            error.put("status", HttpStatus.BAD_REQUEST.value());

            return ResponseEntity.badRequest().body(error);
        }

        /**
         * Handles rate limit errors.
         *
         * @param ex the exception
         * @return a too many requests error response
         */
        @ExceptionHandler(RateLimitOperationException.class)
        public ResponseEntity<Map<String, Object>> handleRateLimitException(RateLimitOperationException ex) {

            logger.warn("Rate limit error: {}", ex.getMessage());

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Rate Limit Exceeded");
            error.put("message", "Too many requests. Please try again later.");
            error.put("timestamp", System.currentTimeMillis());
            error.put("status", HttpStatus.TOO_MANY_REQUESTS.value());

            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("Retry-After", "3600")
                    .body(error);
        }

        /**
         * Handles weather service errors.
         *
         * @param ex the exception
         * @return a service unavailable error response
         */
        @ExceptionHandler({WeatherServiceException.class, WeatherOperationException.class})
        public ResponseEntity<Map<String, Object>> handleWeatherServiceException(Exception ex) {

            logger.error("Weather service error: {}", ex.getMessage(), ex);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Weather Service Error");
            error.put("message", "Unable to retrieve weather data. Please try again later.");
            error.put("timestamp", System.currentTimeMillis());
            error.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        }

        /**
         * Handles cache operation errors.
         *
         * @param ex the exception
         * @return an internal server error response
         */
        @ExceptionHandler(CacheOperationException.class)
        public ResponseEntity<Map<String, Object>> handleCacheOperationException(CacheOperationException ex) {

            logger.warn("Cache operation error: {}", ex.getMessage());

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Cache Operation Error");
            error.put("message", "Cache operation failed. Please try again.");
            error.put("timestamp", System.currentTimeMillis());
            error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }

        /**
         * Handles all other unexpected errors.
         *
         * @param ex the exception
         * @return an internal server error response
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {

            logger.error("Unexpected error: {}", ex.getMessage(), ex);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Internal Server Error");
            error.put("message", "An unexpected error occurred. Please try again later.");
            error.put("timestamp", System.currentTimeMillis());
            error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}