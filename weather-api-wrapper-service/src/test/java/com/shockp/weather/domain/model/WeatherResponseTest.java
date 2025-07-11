package com.shockp.weather.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link WeatherResponse} domain model.
 */
@DisplayName("WeatherResponse Domain Model Tests")
class WeatherResponseTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create weather response with valid parameters")
        void shouldCreateWeatherResponseWithValidParameters() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(25.5, 65, "Sunny", location);
            boolean cached = false;

            // When
            WeatherResponse response = new WeatherResponse(weatherData, cached);

            // Then
            assertNotNull(response);
            assertEquals(weatherData, response.getWeatherData());
            assertEquals(cached, response.isCached());
            assertNotNull(response.getTimestamp());
        }

        @Test
        @DisplayName("Should create cached weather response")
        void shouldCreateCachedWeatherResponse() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(25.5, 65, "Sunny", location);
            boolean cached = true;

            // When
            WeatherResponse response = new WeatherResponse(weatherData, cached);

            // Then
            assertTrue(response.isCached());
        }

        @Test
        @DisplayName("Should throw exception for null weather data")
        void shouldThrowExceptionForNullWeatherData() {
            // When & Then
            NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new WeatherResponse(null, false)
            );
            assertEquals("Weather data cannot be null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("Should return correct weather data")
        void shouldReturnCorrectWeatherData() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData expectedWeatherData = new WeatherData(25.5, 65, "Sunny", location);
            WeatherResponse response = new WeatherResponse(expectedWeatherData, false);

            // When
            WeatherData actualWeatherData = response.getWeatherData();

            // Then
            assertEquals(expectedWeatherData, actualWeatherData);
        }

        @Test
        @DisplayName("Should return correct cached flag")
        void shouldReturnCorrectCachedFlag() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(25.5, 65, "Sunny", location);
            boolean expectedCached = true;
            WeatherResponse response = new WeatherResponse(weatherData, expectedCached);

            // When
            boolean actualCached = response.isCached();

            // Then
            assertEquals(expectedCached, actualCached);
        }

        @Test
        @DisplayName("Should return non-null timestamp")
        void shouldReturnNonNullTimestamp() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(25.5, 65, "Sunny", location);
            WeatherResponse response = new WeatherResponse(weatherData, false);

            // When
            var timestamp = response.getTimestamp();

            // Then
            assertNotNull(timestamp);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(25.5, 65, "Sunny", location);
            WeatherResponse response = new WeatherResponse(weatherData, false);

            // When & Then
            assertEquals(response, response);
        }

        @Test
        @DisplayName("Should be equal to weather response with same values")
        void shouldBeEqualToWeatherResponseWithSameValues() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(25.5, 65, "Sunny", location);
            WeatherResponse response1 = new WeatherResponse(weatherData, false);
            WeatherResponse response2 = new WeatherResponse(weatherData, false);

            // When & Then
            assertEquals(response1, response2);
            assertEquals(response2, response1);
        }

        @Test
        @DisplayName("Should not be equal to weather response with different weather data")
        void shouldNotBeEqualToWeatherResponseWithDifferentWeatherData() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData1 = new WeatherData(25.5, 65, "Sunny", location);
            WeatherData weatherData2 = new WeatherData(26.0, 65, "Sunny", location);
            WeatherResponse response1 = new WeatherResponse(weatherData1, false);
            WeatherResponse response2 = new WeatherResponse(weatherData2, false);

            // When & Then
            assertNotEquals(response1, response2);
            assertNotEquals(response2, response1);
        }

        @Test
        @DisplayName("Should not be equal to weather response with different cached flag")
        void shouldNotBeEqualToWeatherResponseWithDifferentCachedFlag() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(25.5, 65, "Sunny", location);
            WeatherResponse response1 = new WeatherResponse(weatherData, false);
            WeatherResponse response2 = new WeatherResponse(weatherData, true);

            // When & Then
            assertNotEquals(response1, response2);
            assertNotEquals(response2, response1);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(25.5, 65, "Sunny", location);
            WeatherResponse response = new WeatherResponse(weatherData, false);

            // When & Then
            assertNotEquals(null, response);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(25.5, 65, "Sunny", location);
            WeatherResponse response = new WeatherResponse(weatherData, false);
            String differentType = "Not a WeatherResponse";

            // When & Then
            assertNotEquals(response, differentType);
        }

        @Test
        @DisplayName("Should have same hash code for equal objects")
        void shouldHaveSameHashCodeForEqualObjects() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(25.5, 65, "Sunny", location);
            WeatherResponse response1 = new WeatherResponse(weatherData, false);
            WeatherResponse response2 = new WeatherResponse(weatherData, false);

            // When & Then
            assertEquals(response1.hashCode(), response2.hashCode());
        }

        @Test
        @DisplayName("Should have different hash code for different objects")
        void shouldHaveDifferentHashCodeForDifferentObjects() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(25.5, 65, "Sunny", location);
            WeatherResponse response1 = new WeatherResponse(weatherData, false);
            WeatherResponse response2 = new WeatherResponse(weatherData, true);

            // When & Then
            assertNotEquals(response1.hashCode(), response2.hashCode());
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return formatted string representation")
        void shouldReturnFormattedStringRepresentation() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(25.5, 65, "Sunny", location);
            WeatherResponse response = new WeatherResponse(weatherData, false);

            // When
            String result = response.toString();

            // Then
            assertTrue(result.contains("WeatherResponse{"));
            assertTrue(result.contains("weatherData="));
            assertTrue(result.contains("cached=false"));
            assertTrue(result.contains("timestamp="));
            assertTrue(result.endsWith("}"));
        }

        @Test
        @DisplayName("Should include all fields in string representation")
        void shouldIncludeAllFieldsInStringRepresentation() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(25.5, 65, "Sunny", location);
            WeatherResponse response = new WeatherResponse(weatherData, true);

            // When
            String result = response.toString();

            // Then
            assertTrue(result.contains("weatherData="));
            assertTrue(result.contains("cached="));
            assertTrue(result.contains("timestamp="));
        }

        @Test
        @DisplayName("Should show correct cached value in string representation")
        void shouldShowCorrectCachedValueInStringRepresentation() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(25.5, 65, "Sunny", location);
            WeatherResponse response = new WeatherResponse(weatherData, true);

            // When
            String result = response.toString();

            // Then
            assertTrue(result.contains("cached=true"));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle different weather data types")
        void shouldHandleDifferentWeatherDataTypes() {
            // Given
            Location location1 = new Location(40.7128, -74.0060, "New York", "USA");
            Location location2 = new Location(51.5074, -0.1278, "London", "UK");
            WeatherData weatherData1 = new WeatherData(25.5, 65, "Sunny", location1);
            WeatherData weatherData2 = new WeatherData(15.0, 80, "Rainy", location2);

            // When
            WeatherResponse response1 = new WeatherResponse(weatherData1, false);
            WeatherResponse response2 = new WeatherResponse(weatherData2, false);

            // Then
            assertNotEquals(response1, response2);
            assertEquals(weatherData1, response1.getWeatherData());
            assertEquals(weatherData2, response2.getWeatherData());
        }

        @Test
        @DisplayName("Should handle extreme temperature values")
        void shouldHandleExtremeTemperatureValues() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData hotWeather = new WeatherData(45.0, 30, "Very Hot", location);
            WeatherData coldWeather = new WeatherData(-30.0, 90, "Very Cold", location);

            // When
            WeatherResponse hotResponse = new WeatherResponse(hotWeather, false);
            WeatherResponse coldResponse = new WeatherResponse(coldWeather, false);

            // Then
            assertNotEquals(hotResponse, coldResponse);
            assertEquals(45.0, hotResponse.getWeatherData().getTemperature());
            assertEquals(-30.0, coldResponse.getWeatherData().getTemperature());
        }

        @Test
        @DisplayName("Should handle boundary humidity values")
        void shouldHandleBoundaryHumidityValues() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData dryWeather = new WeatherData(25.5, 0, "Very Dry", location);
            WeatherData humidWeather = new WeatherData(25.5, 100, "Very Humid", location);

            // When
            WeatherResponse dryResponse = new WeatherResponse(dryWeather, false);
            WeatherResponse humidResponse = new WeatherResponse(humidWeather, false);

            // Then
            assertNotEquals(dryResponse, humidResponse);
            assertEquals(0, dryResponse.getWeatherData().getHumidity());
            assertEquals(100, humidResponse.getWeatherData().getHumidity());
        }

        @Test
        @DisplayName("Should handle empty description")
        void shouldHandleEmptyDescription() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(25.5, 65, "Clear", location);

            // When
            WeatherResponse response = new WeatherResponse(weatherData, false);

            // Then
            assertEquals("Clear", response.getWeatherData().getDescription());
        }

        @Test
        @DisplayName("Should handle special characters in description")
        void shouldHandleSpecialCharactersInDescription() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(25.5, 65, "Partly Cloudy with Rain", location);

            // When
            WeatherResponse response = new WeatherResponse(weatherData, false);

            // Then
            assertEquals("Partly Cloudy with Rain", response.getWeatherData().getDescription());
        }
    }

    @Nested
    @DisplayName("Timestamp Tests")
    class TimestampTests {

        @Test
        @DisplayName("Should set timestamp to current time")
        void shouldSetTimestampToCurrentTime() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(25.5, 65, "Sunny", location);

            // When
            WeatherResponse response = new WeatherResponse(weatherData, false);

            // Then
            assertNotNull(response.getTimestamp());
            // The timestamp should be very close to the current time (within 1 second)
            assertTrue(Math.abs(System.currentTimeMillis() - response.getTimestamp().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()) < 1000);
        }

        @Test
        @DisplayName("Should have different timestamps for different responses")
        void shouldHaveDifferentTimestampsForDifferentResponses() throws InterruptedException {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(25.5, 65, "Sunny", location);

            // When
            WeatherResponse response1 = new WeatherResponse(weatherData, false);
            Thread.sleep(10); // Small delay to ensure different timestamps
            WeatherResponse response2 = new WeatherResponse(weatherData, false);

            // Then
            assertNotEquals(response1.getTimestamp(), response2.getTimestamp());
        }
    }
} 