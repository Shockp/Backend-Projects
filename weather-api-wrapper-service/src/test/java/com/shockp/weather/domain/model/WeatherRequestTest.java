package com.shockp.weather.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link WeatherRequest} domain model.
 */
@DisplayName("WeatherRequest Domain Model Tests")
class WeatherRequestTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create weather request with valid parameters")
        void shouldCreateWeatherRequestWithValidParameters() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            LocalDate date = LocalDate.of(2024, 1, 15);
            boolean includeHourly = false;

            // When
            WeatherRequest request = new WeatherRequest(location, date, includeHourly);

            // Then
            assertNotNull(request);
            assertEquals(location, request.getLocation());
            assertEquals(date, request.getDate());
            assertEquals(includeHourly, request.isIncludeHourly());
        }

        @Test
        @DisplayName("Should create weather request with hourly data enabled")
        void shouldCreateWeatherRequestWithHourlyDataEnabled() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            LocalDate date = LocalDate.of(2024, 1, 15);
            boolean includeHourly = true;

            // When
            WeatherRequest request = new WeatherRequest(location, date, includeHourly);

            // Then
            assertTrue(request.isIncludeHourly());
        }

        @Test
        @DisplayName("Should throw exception for null location")
        void shouldThrowExceptionForNullLocation() {
            // Given
            LocalDate date = LocalDate.of(2024, 1, 15);

            // When & Then
            NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new WeatherRequest(null, date, false)
            );
            assertEquals("Location cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for null date")
        void shouldThrowExceptionForNullDate() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");

            // When & Then
            NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new WeatherRequest(location, null, false)
            );
            assertEquals("Date cannot be null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("Should return correct location")
        void shouldReturnCorrectLocation() {
            // Given
            Location expectedLocation = new Location(40.7128, -74.0060, "New York", "USA");
            LocalDate date = LocalDate.of(2024, 1, 15);
            WeatherRequest request = new WeatherRequest(expectedLocation, date, false);

            // When
            Location actualLocation = request.getLocation();

            // Then
            assertEquals(expectedLocation, actualLocation);
        }

        @Test
        @DisplayName("Should return correct date")
        void shouldReturnCorrectDate() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            LocalDate expectedDate = LocalDate.of(2024, 1, 15);
            WeatherRequest request = new WeatherRequest(location, expectedDate, false);

            // When
            LocalDate actualDate = request.getDate();

            // Then
            assertEquals(expectedDate, actualDate);
        }

        @Test
        @DisplayName("Should return correct include hourly flag")
        void shouldReturnCorrectIncludeHourlyFlag() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            LocalDate date = LocalDate.of(2024, 1, 15);
            boolean expectedIncludeHourly = true;
            WeatherRequest request = new WeatherRequest(location, date, expectedIncludeHourly);

            // When
            boolean actualIncludeHourly = request.isIncludeHourly();

            // Then
            assertEquals(expectedIncludeHourly, actualIncludeHourly);
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
            LocalDate date = LocalDate.of(2024, 1, 15);
            WeatherRequest request = new WeatherRequest(location, date, false);

            // When & Then
            assertEquals(request, request);
        }

        @Test
        @DisplayName("Should be equal to weather request with same values")
        void shouldBeEqualToWeatherRequestWithSameValues() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            LocalDate date = LocalDate.of(2024, 1, 15);
            WeatherRequest request1 = new WeatherRequest(location, date, false);
            WeatherRequest request2 = new WeatherRequest(location, date, false);

            // When & Then
            assertEquals(request1, request2);
            assertEquals(request2, request1);
        }

        @Test
        @DisplayName("Should not be equal to weather request with different location")
        void shouldNotBeEqualToWeatherRequestWithDifferentLocation() {
            // Given
            Location location1 = new Location(40.7128, -74.0060, "New York", "USA");
            Location location2 = new Location(40.7128, -74.0061, "New York", "USA");
            LocalDate date = LocalDate.of(2024, 1, 15);
            WeatherRequest request1 = new WeatherRequest(location1, date, false);
            WeatherRequest request2 = new WeatherRequest(location2, date, false);

            // When & Then
            assertNotEquals(request1, request2);
            assertNotEquals(request2, request1);
        }

        @Test
        @DisplayName("Should not be equal to weather request with different date")
        void shouldNotBeEqualToWeatherRequestWithDifferentDate() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            LocalDate date1 = LocalDate.of(2024, 1, 15);
            LocalDate date2 = LocalDate.of(2024, 1, 16);
            WeatherRequest request1 = new WeatherRequest(location, date1, false);
            WeatherRequest request2 = new WeatherRequest(location, date2, false);

            // When & Then
            assertNotEquals(request1, request2);
            assertNotEquals(request2, request1);
        }

        @Test
        @DisplayName("Should not be equal to weather request with different include hourly flag")
        void shouldNotBeEqualToWeatherRequestWithDifferentIncludeHourlyFlag() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            LocalDate date = LocalDate.of(2024, 1, 15);
            WeatherRequest request1 = new WeatherRequest(location, date, false);
            WeatherRequest request2 = new WeatherRequest(location, date, true);

            // When & Then
            assertNotEquals(request1, request2);
            assertNotEquals(request2, request1);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            LocalDate date = LocalDate.of(2024, 1, 15);
            WeatherRequest request = new WeatherRequest(location, date, false);

            // When & Then
            assertNotEquals(null, request);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            LocalDate date = LocalDate.of(2024, 1, 15);
            WeatherRequest request = new WeatherRequest(location, date, false);
            String differentType = "Not a WeatherRequest";

            // When & Then
            assertNotEquals(request, differentType);
        }

        @Test
        @DisplayName("Should have same hash code for equal objects")
        void shouldHaveSameHashCodeForEqualObjects() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            LocalDate date = LocalDate.of(2024, 1, 15);
            WeatherRequest request1 = new WeatherRequest(location, date, false);
            WeatherRequest request2 = new WeatherRequest(location, date, false);

            // When & Then
            assertEquals(request1.hashCode(), request2.hashCode());
        }

        @Test
        @DisplayName("Should have different hash code for different objects")
        void shouldHaveDifferentHashCodeForDifferentObjects() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            LocalDate date = LocalDate.of(2024, 1, 15);
            WeatherRequest request1 = new WeatherRequest(location, date, false);
            WeatherRequest request2 = new WeatherRequest(location, date, true);

            // When & Then
            assertNotEquals(request1.hashCode(), request2.hashCode());
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
            LocalDate date = LocalDate.of(2024, 1, 15);
            WeatherRequest request = new WeatherRequest(location, date, false);

            // When
            String result = request.toString();

            // Then
            assertTrue(result.contains("WeatherRequest{"));
            assertTrue(result.contains("location="));
            assertTrue(result.contains("date=2024-01-15"));
            assertTrue(result.contains("includeHourly=false"));
            assertTrue(result.endsWith("}"));
        }

        @Test
        @DisplayName("Should include all fields in string representation")
        void shouldIncludeAllFieldsInStringRepresentation() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            LocalDate date = LocalDate.of(2024, 1, 15);
            WeatherRequest request = new WeatherRequest(location, date, true);

            // When
            String result = request.toString();

            // Then
            assertTrue(result.contains("location="));
            assertTrue(result.contains("date="));
            assertTrue(result.contains("includeHourly="));
        }

        @Test
        @DisplayName("Should show correct include hourly value in string representation")
        void shouldShowCorrectIncludeHourlyValueInStringRepresentation() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            LocalDate date = LocalDate.of(2024, 1, 15);
            WeatherRequest request = new WeatherRequest(location, date, true);

            // When
            String result = request.toString();

            // Then
            assertTrue(result.contains("includeHourly=true"));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle past dates")
        void shouldHandlePastDates() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            LocalDate pastDate = LocalDate.of(2020, 1, 1);

            // When
            WeatherRequest request = new WeatherRequest(location, pastDate, false);

            // Then
            assertEquals(pastDate, request.getDate());
        }

        @Test
        @DisplayName("Should handle future dates")
        void shouldHandleFutureDates() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            LocalDate futureDate = LocalDate.of(2030, 12, 31);

            // When
            WeatherRequest request = new WeatherRequest(location, futureDate, false);

            // Then
            assertEquals(futureDate, request.getDate());
        }

        @Test
        @DisplayName("Should handle leap year dates")
        void shouldHandleLeapYearDates() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            LocalDate leapYearDate = LocalDate.of(2024, 2, 29);

            // When
            WeatherRequest request = new WeatherRequest(location, leapYearDate, false);

            // Then
            assertEquals(leapYearDate, request.getDate());
        }

        @Test
        @DisplayName("Should handle different locations")
        void shouldHandleDifferentLocations() {
            // Given
            Location location1 = new Location(40.7128, -74.0060, "New York", "USA");
            Location location2 = new Location(51.5074, -0.1278, "London", "UK");
            LocalDate date = LocalDate.of(2024, 1, 15);

            // When
            WeatherRequest request1 = new WeatherRequest(location1, date, false);
            WeatherRequest request2 = new WeatherRequest(location2, date, false);

            // Then
            assertNotEquals(request1, request2);
            assertEquals(location1, request1.getLocation());
            assertEquals(location2, request2.getLocation());
        }
    }
} 