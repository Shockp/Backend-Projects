package com.shockp.weather.application.port;

import com.shockp.weather.domain.model.WeatherData;
import com.shockp.weather.domain.model.WeatherRequest;

/**
 * Port interface for weather data provider operations in the weather service.
 * <p>
 * This interface defines the contract for external weather data providers, allowing for
 * different implementations (e.g., Visual Crossing, OpenWeatherMap). It follows the
 * hexagonal architecture principle, decoupling the domain from infrastructure.
 * </p>
 *
 * @author Weather API Wrapper Service
 * @see com.shockp.weather.domain.service.WeatherService
 */
public interface WeatherProviderPort {

    /**
     * Retrieves weather data from the provider for the specified request.
     *
     * @param request the {@link WeatherRequest} containing location, date, and options; must not be {@code null}
     * @return the {@link WeatherData} for the request
     * @throws IllegalArgumentException if the request is invalid
     * @throws com.shockp.weather.domain.service.WeatherServiceException if the provider is unavailable or returns an error
     */
    WeatherData getWeatherData(WeatherRequest request);

    /**
     * Checks if the weather provider is currently available.
     *
     * @return {@code true} if the provider is available, {@code false} otherwise
     */
    boolean isAvailable();

    /**
     * Returns the name of the weather provider implementation.
     *
     * @return the provider name, never {@code null} or empty
     */
    String getProviderName();
} 