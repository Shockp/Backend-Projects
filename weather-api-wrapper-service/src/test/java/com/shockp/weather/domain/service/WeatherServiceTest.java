package com.shockp.weather.domain.service;

import com.shockp.weather.application.port.WeatherProviderPort;
import com.shockp.weather.domain.model.Location;
import com.shockp.weather.domain.model.WeatherData;
import com.shockp.weather.domain.model.WeatherRequest;
import com.shockp.weather.domain.model.WeatherResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link WeatherService} domain service.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WeatherService Domain Service Tests")
class WeatherServiceTest {

    @Mock
    private WeatherProviderPort weatherProviderPort;

    @Mock
    private CacheService cacheService;

    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        weatherService = new WeatherService(weatherProviderPort, cacheService);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create weather service with valid parameters")
        void shouldCreateWeatherServiceWithValidParameters() {
            // Given & When
            WeatherService service = new WeatherService(weatherProviderPort, cacheService);

            // Then
            assertNotNull(service);
        }

        @Test
        @DisplayName("Should throw exception for null weather provider port")
        void shouldThrowExceptionForNullWeatherProviderPort() {
            // When & Then
            NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new WeatherService(null, cacheService)
            );
            assertEquals("Weather provider port cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for null cache service")
        void shouldThrowExceptionForNullCacheService() {
            // When & Then
            NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new WeatherService(weatherProviderPort, null)
            );
            assertEquals("Cache service cannot be null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Get Weather Tests")
    class GetWeatherTests {

        @Test
        @DisplayName("Should return cached weather data when available")
        void shouldReturnCachedWeatherDataWhenAvailable() {
            // Given
            WeatherRequest request = createSampleWeatherRequest();
            WeatherData cachedData = createSampleWeatherData();
            String cacheKey = "weather:40.7128:-74.006:New York:2024-01-15:false";
            
            when(cacheService.generateKey(request)).thenReturn(cacheKey);
            when(cacheService.get(cacheKey)).thenReturn(Optional.of(cachedData));

            // When
            WeatherResponse response = weatherService.getWeather(request);

            // Then
            assertNotNull(response);
            assertEquals(cachedData, response.getWeatherData());
            assertTrue(response.isCached());
            verify(cacheService).generateKey(request);
            verify(cacheService).get(cacheKey);
            verify(weatherProviderPort, never()).getWeatherData(any());
            verify(cacheService, never()).put(any(), any());
        }

        @Test
        @DisplayName("Should retrieve from provider and cache when cache miss")
        void shouldRetrieveFromProviderAndCacheWhenCacheMiss() {
            // Given
            WeatherRequest request = createSampleWeatherRequest();
            WeatherData freshData = createSampleWeatherData();
            String cacheKey = "weather:40.7128:-74.006:New York:2024-01-15:false";
            
            when(cacheService.generateKey(request)).thenReturn(cacheKey);
            when(cacheService.get(cacheKey)).thenReturn(Optional.empty());
            when(weatherProviderPort.isAvailable()).thenReturn(true);
            when(weatherProviderPort.getWeatherData(request)).thenReturn(freshData);

            // When
            WeatherResponse response = weatherService.getWeather(request);

            // Then
            assertNotNull(response);
            assertEquals(freshData, response.getWeatherData());
            assertFalse(response.isCached());
            verify(cacheService).generateKey(request);
            verify(cacheService).get(cacheKey);
            verify(weatherProviderPort).isAvailable();
            verify(weatherProviderPort).getWeatherData(request);
            verify(cacheService).put(cacheKey, freshData);
        }

        @Test
        @DisplayName("Should throw exception for null request")
        void shouldThrowExceptionForNullRequest() {
            // When & Then
            NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> weatherService.getWeather(null)
            );
            assertEquals("Weather request cannot be null", exception.getMessage());
            verify(cacheService, never()).generateKey(any());
        }

        @Test
        @DisplayName("Should throw WeatherServiceException when provider is unavailable")
        void shouldThrowWeatherServiceExceptionWhenProviderIsUnavailable() {
            // Given
            WeatherRequest request = createSampleWeatherRequest();
            String cacheKey = "weather:40.7128:-74.006:New York:2024-01-15:false";
            
            when(cacheService.generateKey(request)).thenReturn(cacheKey);
            when(cacheService.get(cacheKey)).thenReturn(Optional.empty());
            when(weatherProviderPort.isAvailable()).thenReturn(false);
            when(weatherProviderPort.getProviderName()).thenReturn("TestProvider");

            // When & Then
            WeatherServiceException exception = assertThrows(
                WeatherServiceException.class,
                () -> weatherService.getWeather(request)
            );
            assertTrue(exception.getMessage().contains("Weather provider is currently unavailable: TestProvider"));
            verify(weatherProviderPort).isAvailable();
            verify(weatherProviderPort, never()).getWeatherData(any());
        }

        @Test
        @DisplayName("Should throw WeatherServiceException when provider throws exception")
        void shouldThrowWeatherServiceExceptionWhenProviderThrowsException() {
            // Given
            WeatherRequest request = createSampleWeatherRequest();
            String cacheKey = "weather:40.7128:-74.006:New York:2024-01-15:false";
            
            when(cacheService.generateKey(request)).thenReturn(cacheKey);
            when(cacheService.get(cacheKey)).thenReturn(Optional.empty());
            when(weatherProviderPort.isAvailable()).thenReturn(true);
            when(weatherProviderPort.getProviderName()).thenReturn("TestProvider");
            when(weatherProviderPort.getWeatherData(request)).thenThrow(new RuntimeException("Provider error"));

            // When & Then
            WeatherServiceException exception = assertThrows(
                WeatherServiceException.class,
                () -> weatherService.getWeather(request)
            );
            assertTrue(exception.getMessage().contains("Failed to retrieve weather data from provider: TestProvider"));
            assertNotNull(exception.getCause());
            verify(weatherProviderPort).isAvailable();
            verify(weatherProviderPort).getWeatherData(request);
            verify(cacheService, never()).put(any(), any());
        }
    }

    @Nested
    @DisplayName("Validate Request Tests")
    class ValidateRequestTests {

        @Test
        @DisplayName("Should validate request successfully")
        void shouldValidateRequestSuccessfully() {
            // Given
            WeatherRequest request = createSampleWeatherRequest();

            // When & Then
            assertDoesNotThrow(() -> weatherService.validateRequest(request));
        }

        @Test
        @DisplayName("Should throw exception for null request")
        void shouldThrowExceptionForNullRequest() {
            // When & Then
            NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> weatherService.validateRequest(null)
            );
            assertEquals("Weather request cannot be null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Provider Availability Tests")
    class ProviderAvailabilityTests {

        @Test
        @DisplayName("Should return true when provider is available")
        void shouldReturnTrueWhenProviderIsAvailable() {
            // Given
            when(weatherProviderPort.isAvailable()).thenReturn(true);

            // When
            boolean result = weatherService.isAvailable();

            // Then
            assertTrue(result);
            verify(weatherProviderPort).isAvailable();
        }

        @Test
        @DisplayName("Should return false when provider is unavailable")
        void shouldReturnFalseWhenProviderIsUnavailable() {
            // Given
            when(weatherProviderPort.isAvailable()).thenReturn(false);

            // When
            boolean result = weatherService.isAvailable();

            // Then
            assertFalse(result);
            verify(weatherProviderPort).isAvailable();
        }
    }

    @Nested
    @DisplayName("Provider Name Tests")
    class ProviderNameTests {

        @Test
        @DisplayName("Should return provider name")
        void shouldReturnProviderName() {
            // Given
            String expectedName = "TestProvider";
            when(weatherProviderPort.getProviderName()).thenReturn(expectedName);

            // When
            String result = weatherService.getProviderName();

            // Then
            assertEquals(expectedName, result);
            verify(weatherProviderPort).getProviderName();
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle complete weather retrieval flow")
        void shouldHandleCompleteWeatherRetrievalFlow() {
            // Given
            WeatherRequest request = createSampleWeatherRequest();
            WeatherData freshData = createSampleWeatherData();
            String cacheKey = "weather:40.7128:-74.006:New York:2024-01-15:false";
            
            when(cacheService.generateKey(request)).thenReturn(cacheKey);
            when(cacheService.get(cacheKey)).thenReturn(Optional.empty());
            when(weatherProviderPort.isAvailable()).thenReturn(true);
            when(weatherProviderPort.getWeatherData(request)).thenReturn(freshData);

            // When
            WeatherResponse response = weatherService.getWeather(request);

            // Then
            assertNotNull(response);
            assertEquals(freshData, response.getWeatherData());
            assertFalse(response.isCached());
            
            // Verify the complete flow
            verify(cacheService).generateKey(request);
            verify(cacheService).get(cacheKey);
            verify(weatherProviderPort).isAvailable();
            verify(weatherProviderPort).getWeatherData(request);
            verify(cacheService).put(cacheKey, freshData);
        }

        @Test
        @DisplayName("Should handle cache hit scenario")
        void shouldHandleCacheHitScenario() {
            // Given
            WeatherRequest request = createSampleWeatherRequest();
            WeatherData cachedData = createSampleWeatherData();
            String cacheKey = "weather:40.7128:-74.006:New York:2024-01-15:false";
            
            when(cacheService.generateKey(request)).thenReturn(cacheKey);
            when(cacheService.get(cacheKey)).thenReturn(Optional.of(cachedData));

            // When
            WeatherResponse response = weatherService.getWeather(request);

            // Then
            assertNotNull(response);
            assertEquals(cachedData, response.getWeatherData());
            assertTrue(response.isCached());
            
            // Verify cache hit flow (no provider calls)
            verify(cacheService).generateKey(request);
            verify(cacheService).get(cacheKey);
            verify(weatherProviderPort, never()).isAvailable();
            verify(weatherProviderPort, never()).getWeatherData(any());
            verify(cacheService, never()).put(any(), any());
        }
    }

    /**
     * Creates a sample weather request for testing.
     *
     * @return a sample {@link WeatherRequest} instance
     */
    private WeatherRequest createSampleWeatherRequest() {
        Location location = new Location(40.7128, -74.0060, "New York", "USA");
        LocalDate date = LocalDate.of(2024, 1, 15);
        return new WeatherRequest(location, date, false);
    }

    /**
     * Creates a sample weather data object for testing.
     *
     * @return a sample {@link WeatherData} instance
     */
    private WeatherData createSampleWeatherData() {
        Location location = new Location(40.7128, -74.0060, "New York", "USA");
        return new WeatherData(25.5, 65, "Sunny", location);
    }
} 