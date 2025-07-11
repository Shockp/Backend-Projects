package com.shockp.weather.domain.service;

import com.shockp.weather.application.port.CachePort;
import com.shockp.weather.domain.model.Location;
import com.shockp.weather.domain.model.WeatherData;
import com.shockp.weather.domain.model.WeatherRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link CacheService} domain service.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CacheService Domain Service Tests")
class CacheServiceTest {

    @Mock
    private CachePort cachePort;

    private CacheService cacheService;
    private static final Duration CACHE_TIMEOUT = Duration.ofMinutes(30);

    @BeforeEach
    void setUp() {
        cacheService = new CacheService(cachePort, CACHE_TIMEOUT);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create cache service with valid parameters")
        void shouldCreateCacheServiceWithValidParameters() {
            // Given & When
            CacheService service = new CacheService(cachePort, CACHE_TIMEOUT);

            // Then
            assertNotNull(service);
            assertEquals(CACHE_TIMEOUT, service.getCacheTimeout());
        }

        @Test
        @DisplayName("Should throw exception for null cache port")
        void shouldThrowExceptionForNullCachePort() {
            // When & Then
            NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new CacheService(null, CACHE_TIMEOUT)
            );
            assertEquals("Cache port cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for null cache timeout")
        void shouldThrowExceptionForNullCacheTimeout() {
            // When & Then
            NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new CacheService(cachePort, null)
            );
            assertEquals("Cache timeout cannot be null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Get Operation Tests")
    class GetOperationTests {

        @Test
        @DisplayName("Should retrieve weather data from cache")
        void shouldRetrieveWeatherDataFromCache() {
            // Given
            String key = "test-key";
            WeatherData expectedData = createSampleWeatherData();
            when(cachePort.get(key)).thenReturn(Optional.of(expectedData));

            // When
            Optional<WeatherData> result = cacheService.get(key);

            // Then
            assertTrue(result.isPresent());
            assertEquals(expectedData, result.get());
            verify(cachePort).get(key);
        }

        @Test
        @DisplayName("Should return empty when cache miss")
        void shouldReturnEmptyWhenCacheMiss() {
            // Given
            String key = "test-key";
            when(cachePort.get(key)).thenReturn(Optional.empty());

            // When
            Optional<WeatherData> result = cacheService.get(key);

            // Then
            assertFalse(result.isPresent());
            verify(cachePort).get(key);
        }

        @Test
        @DisplayName("Should throw exception for null key")
        void shouldThrowExceptionForNullKey() {
            // When & Then
            NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> cacheService.get(null)
            );
            assertEquals("Cache key cannot be null", exception.getMessage());
            verify(cachePort, never()).get(any());
        }

        @Test
        @DisplayName("Should throw WeatherServiceException when cache operation fails")
        void shouldThrowWeatherServiceExceptionWhenCacheOperationFails() {
            // Given
            String key = "test-key";
            when(cachePort.get(key)).thenThrow(new RuntimeException("Cache error"));

            // When & Then
            WeatherServiceException exception = assertThrows(
                WeatherServiceException.class,
                () -> cacheService.get(key)
            );
            assertTrue(exception.getMessage().contains("Failed to retrieve weather data from cache for key: " + key));
            assertNotNull(exception.getCause());
        }
    }

    @Nested
    @DisplayName("Put Operation Tests")
    class PutOperationTests {

        @Test
        @DisplayName("Should store weather data with default timeout")
        void shouldStoreWeatherDataWithDefaultTimeout() {
            // Given
            String key = "test-key";
            WeatherData data = createSampleWeatherData();

            // When
            cacheService.put(key, data);

            // Then
            verify(cachePort).put(key, data, CACHE_TIMEOUT);
        }

        @Test
        @DisplayName("Should store weather data with custom timeout")
        void shouldStoreWeatherDataWithCustomTimeout() {
            // Given
            String key = "test-key";
            WeatherData data = createSampleWeatherData();
            Duration customTimeout = Duration.ofHours(1);

            // When
            cacheService.put(key, data, customTimeout);

            // Then
            verify(cachePort).put(key, data, customTimeout);
        }

        @Test
        @DisplayName("Should throw exception for null key in put operation")
        void shouldThrowExceptionForNullKeyInPutOperation() {
            // Given
            WeatherData data = createSampleWeatherData();

            // When & Then
            NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> cacheService.put(null, data)
            );
            assertEquals("Cache key cannot be null", exception.getMessage());
            verify(cachePort, never()).put(any(), any(), any());
        }

        @Test
        @DisplayName("Should throw exception for null data in put operation")
        void shouldThrowExceptionForNullDataInPutOperation() {
            // Given
            String key = "test-key";

            // When & Then
            NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> cacheService.put(key, null)
            );
            assertEquals("Weather data cannot be null", exception.getMessage());
            verify(cachePort, never()).put(any(), any(), any());
        }

        @Test
        @DisplayName("Should throw exception for null TTL in put operation")
        void shouldThrowExceptionForNullTtlInPutOperation() {
            // Given
            String key = "test-key";
            WeatherData data = createSampleWeatherData();

            // When & Then
            NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> cacheService.put(key, data, null)
            );
            assertEquals("TTL cannot be null", exception.getMessage());
            verify(cachePort, never()).put(any(), any(), any());
        }

        @Test
        @DisplayName("Should throw WeatherServiceException when put operation fails")
        void shouldThrowWeatherServiceExceptionWhenPutOperationFails() {
            // Given
            String key = "test-key";
            WeatherData data = createSampleWeatherData();
            doThrow(new RuntimeException("Cache error")).when(cachePort).put(key, data, CACHE_TIMEOUT);

            // When & Then
            WeatherServiceException exception = assertThrows(
                WeatherServiceException.class,
                () -> cacheService.put(key, data)
            );
            assertTrue(exception.getMessage().contains("Failed to store weather data in cache for key: " + key));
            assertNotNull(exception.getCause());
        }
    }

    @Nested
    @DisplayName("Evict Operation Tests")
    class EvictOperationTests {

        @Test
        @DisplayName("Should evict cache entry")
        void shouldEvictCacheEntry() {
            // Given
            String key = "test-key";

            // When
            cacheService.evict(key);

            // Then
            verify(cachePort).delete(key);
        }

        @Test
        @DisplayName("Should throw exception for null key in evict operation")
        void shouldThrowExceptionForNullKeyInEvictOperation() {
            // When & Then
            NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> cacheService.evict(null)
            );
            assertEquals("Cache key cannot be null", exception.getMessage());
            verify(cachePort, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw WeatherServiceException when evict operation fails")
        void shouldThrowWeatherServiceExceptionWhenEvictOperationFails() {
            // Given
            String key = "test-key";
            doThrow(new RuntimeException("Cache error")).when(cachePort).delete(key);

            // When & Then
            WeatherServiceException exception = assertThrows(
                WeatherServiceException.class,
                () -> cacheService.evict(key)
            );
            assertTrue(exception.getMessage().contains("Failed to evict weather data from cache for key: " + key));
            assertNotNull(exception.getCause());
        }
    }

    @Nested
    @DisplayName("Clear Operation Tests")
    class ClearOperationTests {

        @Test
        @DisplayName("Should clear all cache entries")
        void shouldClearAllCacheEntries() {
            // When
            cacheService.clear();

            // Then
            verify(cachePort).clear();
        }

        @Test
        @DisplayName("Should throw WeatherServiceException when clear operation fails")
        void shouldThrowWeatherServiceExceptionWhenClearOperationFails() {
            // Given
            doThrow(new RuntimeException("Cache error")).when(cachePort).clear();

            // When & Then
            WeatherServiceException exception = assertThrows(
                WeatherServiceException.class,
                () -> cacheService.clear()
            );
            assertTrue(exception.getMessage().contains("Failed to clear cache"));
            assertNotNull(exception.getCause());
        }
    }

    @Nested
    @DisplayName("Key Generation Tests")
    class KeyGenerationTests {

        @Test
        @DisplayName("Should generate cache key for weather request")
        void shouldGenerateCacheKeyForWeatherRequest() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            LocalDate date = LocalDate.of(2024, 1, 15);
            WeatherRequest request = new WeatherRequest(location, date, false);

            // When
            String key = cacheService.generateKey(request);

            // Then
            String expectedKey = "weather:40.7128:-74.006:New York:2024-01-15:false";
            assertEquals(expectedKey, key);
        }

        @Test
        @DisplayName("Should generate different keys for different requests")
        void shouldGenerateDifferentKeysForDifferentRequests() {
            // Given
            Location location1 = new Location(40.7128, -74.0060, "New York", "USA");
            Location location2 = new Location(40.7128, -74.0060, "New York", "USA");
            LocalDate date = LocalDate.of(2024, 1, 15);
            WeatherRequest request1 = new WeatherRequest(location1, date, false);
            WeatherRequest request2 = new WeatherRequest(location2, date, true);

            // When
            String key1 = cacheService.generateKey(request1);
            String key2 = cacheService.generateKey(request2);

            // Then
            assertNotEquals(key1, key2);
            assertTrue(key1.endsWith(":false"));
            assertTrue(key2.endsWith(":true"));
        }

        @Test
        @DisplayName("Should throw exception for null weather request")
        void shouldThrowExceptionForNullWeatherRequest() {
            // When & Then
            NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> cacheService.generateKey(null)
            );
            assertEquals("Weather request cannot be null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("Should return cache timeout")
        void shouldReturnCacheTimeout() {
            // When
            Duration timeout = cacheService.getCacheTimeout();

            // Then
            assertEquals(CACHE_TIMEOUT, timeout);
        }
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