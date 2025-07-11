package com.shockp.weather.domain.service;

import java.util.Objects;
import java.util.Optional;

import com.shockp.weather.application.port.WeatherProviderPort;
import com.shockp.weather.domain.model.WeatherData;
import com.shockp.weather.domain.model.WeatherRequest;
import com.shockp.weather.domain.model.WeatherResponse;

/**
 * Domain service for orchestrating weather data operations in the weather service.
 * <p>
 * This service coordinates between weather data providers and caching to provide
 * a complete weather data retrieval experience. It follows the domain service
 * pattern and orchestrates the interaction between weather provider port and cache service.
 * Rate limiting is handled at the use case level, not in this domain service.
 * </p>
 *
 * @author Weather API Wrapper Service
 * @see com.shockp.weather.application.port.WeatherProviderPort
 * @see com.shockp.weather.domain.service.CacheService
 * @see com.shockp.weather.domain.model.WeatherRequest
 * @see com.shockp.weather.domain.model.WeatherResponse
 */
public class WeatherService {
    
    /** The weather provider port for external weather data retrieval. */
    private final WeatherProviderPort weatherProviderPort;
    
    /** The cache service for weather data caching operations. */
    private final CacheService cacheService;

    /**
     * Constructs a new {@link WeatherService} with the specified dependencies.
     *
     * @param weatherProviderPort the weather provider port implementation, must not be {@code null}
     * @param cacheService the cache service for data caching, must not be {@code null}
     * @throws NullPointerException if any parameter is {@code null}
     */
    public WeatherService(WeatherProviderPort weatherProviderPort, CacheService cacheService) {
        this.weatherProviderPort = Objects.requireNonNull(weatherProviderPort, "Weather provider port cannot be null");
        this.cacheService = Objects.requireNonNull(cacheService, "Cache service cannot be null");
    }

    /**
     * Retrieves weather data for the specified request, with caching.
     * <p>
     * This method orchestrates the complete weather data retrieval process:
     * <ol>
     *   <li>Validates the weather request</li>
     *   <li>Checks cache for existing data</li>
     *   <li>If cache miss, retrieves from weather provider</li>
     *   <li>Stores fresh data in cache</li>
     *   <li>Returns weather response with cache status</li>
     * </ol>
     * </p>
     *
     * @param request the {@link WeatherRequest} containing location, date, and options; must not be {@code null}
     * @return a {@link WeatherResponse} containing the weather data and cache status
     * @throws NullPointerException if {@code request} is {@code null}
     * @throws IllegalArgumentException if the request is invalid
     * @throws WeatherServiceException if weather data retrieval fails
     */
    public WeatherResponse getWeather(WeatherRequest request) {
        validateRequest(request);
        
        String cacheKey = cacheService.generateKey(request);
        
        // Check cache first
        Optional<WeatherData> cachedData = cacheService.get(cacheKey);
        if (cachedData.isPresent()) {
            return new WeatherResponse(cachedData.get(), true);
        }
        
        // Cache miss - retrieve from provider
        WeatherData weatherData = retrieveFromProvider(request);
        
        // Cache the fresh data
        cacheService.put(cacheKey, weatherData);
        
        return new WeatherResponse(weatherData, false);
    }

    /**
     * Validates the weather request for completeness and correctness.
     *
     * @param request the {@link WeatherRequest} to validate, must not be {@code null}
     * @throws NullPointerException if {@code request} is {@code null}
     * @throws IllegalArgumentException if the request is invalid
     */
    public void validateRequest(WeatherRequest request) {
        Objects.requireNonNull(request, "Weather request cannot be null");
    }

    /**
     * Retrieves weather data from the external provider.
     * <p>
     * This method handles the interaction with the weather provider port,
     * including availability checks and error handling.
     * </p>
     *
     * @param request the {@link WeatherRequest} to retrieve data for, must not be {@code null}
     * @return the {@link WeatherData} from the provider
     * @throws WeatherServiceException if the provider is unavailable or returns an error
     */
    private WeatherData retrieveFromProvider(WeatherRequest request) {
        if (!weatherProviderPort.isAvailable()) {
            throw new WeatherServiceException("Weather provider is currently unavailable: " + 
                weatherProviderPort.getProviderName());
        }
        
        try {
            return weatherProviderPort.getWeatherData(request);
        } catch (Exception e) {
            throw new WeatherServiceException("Failed to retrieve weather data from provider: " + 
                weatherProviderPort.getProviderName(), e);
        }
    }

    /**
     * Checks if the weather service is available by verifying provider availability.
     *
     * @return {@code true} if the weather service is available, {@code false} otherwise
     */
    public boolean isAvailable() {
        return weatherProviderPort.isAvailable();
    }

    /**
     * Gets the name of the current weather provider.
     *
     * @return the provider name, never {@code null} or empty
     */
    public String getProviderName() {
        return weatherProviderPort.getProviderName();
    }
} 