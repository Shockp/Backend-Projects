package com.shockp.weather.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link WeatherData} domain model.
 */
@DisplayName("WeatherData Domain Model Tests")
class WeatherDataTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create weather data with valid parameters")
        void shouldCreateWeatherDataWithValidParameters() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            double temperature = 25.5;
            int humidity = 65;
            String description = "Sunny";

            // When
            WeatherData weatherData = new WeatherData(temperature, humidity, description, location);

            // Then
            assertNotNull(weatherData);
            assertEquals(temperature, weatherData.getTemperature());
            assertEquals(humidity, weatherData.getHumidity());
            assertEquals(description, weatherData.getDescription());
            assertEquals(location, weatherData.getLocation());
            assertNotNull(weatherData.getTimestamp());
        }

        @Test
        @DisplayName("Should trim whitespace from description")
        void shouldTrimWhitespaceFromDescription() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            String description = "  Sunny  ";

            // When
            WeatherData weatherData = new WeatherData(25.5, 65, description, location);

            // Then
            assertEquals("Sunny", weatherData.getDescription());
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 101, -10, 150})
        @DisplayName("Should throw exception for invalid humidity")
        void shouldThrowExceptionForInvalidHumidity(int invalidHumidity) {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new WeatherData(25.5, invalidHumidity, "Sunny", location)
            );
            assertTrue(exception.getMessage().contains("Humidity must be between"));
        }

        @Test
        @DisplayName("Should throw exception for null description")
        void shouldThrowExceptionForNullDescription() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new WeatherData(25.5, 65, null, location)
            );
            assertEquals("Description cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for empty description")
        void shouldThrowExceptionForEmptyDescription() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new WeatherData(25.5, 65, "", location)
            );
            assertEquals("Description cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for whitespace-only description")
        void shouldThrowExceptionForWhitespaceOnlyDescription() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new WeatherData(25.5, 65, "   ", location)
            );
            assertEquals("Description cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for null location")
        void shouldThrowExceptionForNullLocation() {
            // When & Then
            NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new WeatherData(25.5, 65, "Sunny", null)
            );
            assertEquals("Location cannot be null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("Should return correct temperature")
        void shouldReturnCorrectTemperature() {
            // Given
            double expectedTemperature = 25.5;
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(expectedTemperature, 65, "Sunny", location);

            // When
            double actualTemperature = weatherData.getTemperature();

            // Then
            assertEquals(expectedTemperature, actualTemperature);
        }

        @Test
        @DisplayName("Should return correct humidity")
        void shouldReturnCorrectHumidity() {
            // Given
            int expectedHumidity = 65;
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(25.5, expectedHumidity, "Sunny", location);

            // When
            int actualHumidity = weatherData.getHumidity();

            // Then
            assertEquals(expectedHumidity, actualHumidity);
        }

        @Test
        @DisplayName("Should return correct description")
        void shouldReturnCorrectDescription() {
            // Given
            String expectedDescription = "Sunny";
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(25.5, 65, expectedDescription, location);

            // When
            String actualDescription = weatherData.getDescription();

            // Then
            assertEquals(expectedDescription, actualDescription);
        }

        @Test
        @DisplayName("Should return correct location")
        void shouldReturnCorrectLocation() {
            // Given
            Location expectedLocation = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(25.5, 65, "Sunny", expectedLocation);

            // When
            Location actualLocation = weatherData.getLocation();

            // Then
            assertEquals(expectedLocation, actualLocation);
        }

        @Test
        @DisplayName("Should return non-null timestamp")
        void shouldReturnNonNullTimestamp() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(25.5, 65, "Sunny", location);

            // When
            var timestamp = weatherData.getTimestamp();

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

            // When & Then
            assertEquals(weatherData, weatherData);
        }

        @Test
        @DisplayName("Should be equal to weather data with same values")
        void shouldBeEqualToWeatherDataWithSameValues() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData1 = new WeatherData(25.5, 65, "Sunny", location);
            WeatherData weatherData2 = new WeatherData(25.5, 65, "Sunny", location);

            // When & Then
            assertEquals(weatherData1, weatherData2);
            assertEquals(weatherData2, weatherData1);
        }

        @Test
        @DisplayName("Should not be equal to weather data with different temperature")
        void shouldNotBeEqualToWeatherDataWithDifferentTemperature() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData1 = new WeatherData(25.5, 65, "Sunny", location);
            WeatherData weatherData2 = new WeatherData(26.0, 65, "Sunny", location);

            // When & Then
            assertNotEquals(weatherData1, weatherData2);
            assertNotEquals(weatherData2, weatherData1);
        }

        @Test
        @DisplayName("Should not be equal to weather data with different humidity")
        void shouldNotBeEqualToWeatherDataWithDifferentHumidity() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData1 = new WeatherData(25.5, 65, "Sunny", location);
            WeatherData weatherData2 = new WeatherData(25.5, 70, "Sunny", location);

            // When & Then
            assertNotEquals(weatherData1, weatherData2);
            assertNotEquals(weatherData2, weatherData1);
        }

        @Test
        @DisplayName("Should not be equal to weather data with different description")
        void shouldNotBeEqualToWeatherDataWithDifferentDescription() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData1 = new WeatherData(25.5, 65, "Sunny", location);
            WeatherData weatherData2 = new WeatherData(25.5, 65, "Cloudy", location);

            // When & Then
            assertNotEquals(weatherData1, weatherData2);
            assertNotEquals(weatherData2, weatherData1);
        }

        @Test
        @DisplayName("Should not be equal to weather data with different location")
        void shouldNotBeEqualToWeatherDataWithDifferentLocation() {
            // Given
            Location location1 = new Location(40.7128, -74.0060, "New York", "USA");
            Location location2 = new Location(40.7128, -74.0061, "New York", "USA");
            WeatherData weatherData1 = new WeatherData(25.5, 65, "Sunny", location1);
            WeatherData weatherData2 = new WeatherData(25.5, 65, "Sunny", location2);

            // When & Then
            assertNotEquals(weatherData1, weatherData2);
            assertNotEquals(weatherData2, weatherData1);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(25.5, 65, "Sunny", location);

            // When & Then
            assertNotEquals(null, weatherData);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(25.5, 65, "Sunny", location);
            String differentType = "Not a WeatherData";

            // When & Then
            assertNotEquals(differentType, weatherData);
        }

        @Test
        @DisplayName("Should have same hash code for equal objects")
        void shouldHaveSameHashCodeForEqualObjects() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData1 = new WeatherData(25.5, 65, "Sunny", location);
            WeatherData weatherData2 = new WeatherData(25.5, 65, "Sunny", location);

            // When & Then
            assertEquals(weatherData1.hashCode(), weatherData2.hashCode());
        }

        @Test
        @DisplayName("Should have different hash code for different objects")
        void shouldHaveDifferentHashCodeForDifferentObjects() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData1 = new WeatherData(25.5, 65, "Sunny", location);
            WeatherData weatherData2 = new WeatherData(26.0, 65, "Sunny", location);

            // When & Then
            assertNotEquals(weatherData1.hashCode(), weatherData2.hashCode());
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

            // When
            String result = weatherData.toString();

            // Then
            assertTrue(result.contains("WeatherData{"));
            assertTrue(result.contains("temperature=25.5°C"));
            assertTrue(result.contains("humidity=65%"));
            assertTrue(result.contains("description='Sunny'"));
            assertTrue(result.contains("location="));
            assertTrue(result.endsWith("}"));
        }

        @Test
        @DisplayName("Should include all fields in string representation")
        void shouldIncludeAllFieldsInStringRepresentation() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(25.5, 65, "Sunny", location);

            // When
            String result = weatherData.toString();

            // Then
            assertTrue(result.contains("temperature="));
            assertTrue(result.contains("humidity="));
            assertTrue(result.contains("description="));
            assertTrue(result.contains("timestamp="));
            assertTrue(result.contains("location="));
        }
    }

    @Nested
    @DisplayName("Boundary Tests")
    class BoundaryTests {

        @Test
        @DisplayName("Should accept minimum humidity")
        void shouldAcceptMinimumHumidity() {
            // Given & When
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(25.5, 0, "Sunny", location);

            // Then
            assertEquals(0, weatherData.getHumidity());
        }

        @Test
        @DisplayName("Should accept maximum humidity")
        void shouldAcceptMaximumHumidity() {
            // Given & When
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(25.5, 100, "Sunny", location);

            // Then
            assertEquals(100, weatherData.getHumidity());
        }

        @Test
        @DisplayName("Should accept negative temperature")
        void shouldAcceptNegativeTemperature() {
            // Given & When
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(-10.5, 65, "Cold", location);

            // Then
            assertEquals(-10.5, weatherData.getTemperature());
        }

        @Test
        @DisplayName("Should accept zero temperature")
        void shouldAcceptZeroTemperature() {
            // Given & When
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            WeatherData weatherData = new WeatherData(0.0, 65, "Freezing", location);

            // Then
            assertEquals(0.0, weatherData.getTemperature());
        }
    }
} 