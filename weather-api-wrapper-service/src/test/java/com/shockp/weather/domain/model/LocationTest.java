package com.shockp.weather.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Location} domain model.
 */
@DisplayName("Location Domain Model Tests")
class LocationTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create location with valid parameters")
        void shouldCreateLocationWithValidParameters() {
            // Given
            double latitude = 40.7128;
            double longitude = -74.0060;
            String city = "New York";
            String country = "USA";

            // When
            Location location = new Location(latitude, longitude, city, country);

            // Then
            assertNotNull(location);
            assertEquals(latitude, location.getLatitude());
            assertEquals(longitude, location.getLongitude());
            assertEquals(city, location.getCity());
            assertEquals(country, location.getCountry());
        }

        @Test
        @DisplayName("Should trim whitespace from city and country")
        void shouldTrimWhitespaceFromCityAndCountry() {
            // Given
            String city = "  New York  ";
            String country = "  USA  ";

            // When
            Location location = new Location(40.7128, -74.0060, city, country);

            // Then
            assertEquals("New York", location.getCity());
            assertEquals("USA", location.getCountry());
        }

        @ParameterizedTest
        @ValueSource(doubles = {-90.1, 90.1, -91.0, 91.0})
        @DisplayName("Should throw exception for invalid latitude")
        void shouldThrowExceptionForInvalidLatitude(double invalidLatitude) {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Location(invalidLatitude, -74.0060, "New York", "USA")
            );
            assertTrue(exception.getMessage().contains("Latitude must be between"));
        }

        @ParameterizedTest
        @ValueSource(doubles = {-180.1, 180.1, -181.0, 181.0})
        @DisplayName("Should throw exception for invalid longitude")
        void shouldThrowExceptionForInvalidLongitude(double invalidLongitude) {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Location(40.7128, invalidLongitude, "New York", "USA")
            );
            assertTrue(exception.getMessage().contains("Longitude must be between"));
        }

        @Test
        @DisplayName("Should throw exception for null city")
        void shouldThrowExceptionForNullCity() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Location(40.7128, -74.0060, null, "USA")
            );
            assertEquals("City cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for empty city")
        void shouldThrowExceptionForEmptyCity() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Location(40.7128, -74.0060, "", "USA")
            );
            assertEquals("City cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for whitespace-only city")
        void shouldThrowExceptionForWhitespaceOnlyCity() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Location(40.7128, -74.0060, "   ", "USA")
            );
            assertEquals("City cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for null country")
        void shouldThrowExceptionForNullCountry() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Location(40.7128, -74.0060, "New York", null)
            );
            assertEquals("Country cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for empty country")
        void shouldThrowExceptionForEmptyCountry() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Location(40.7128, -74.0060, "New York", "")
            );
            assertEquals("Country cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for whitespace-only country")
        void shouldThrowExceptionForWhitespaceOnlyCountry() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Location(40.7128, -74.0060, "New York", "   ")
            );
            assertEquals("Country cannot be null or empty", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("Should return correct latitude")
        void shouldReturnCorrectLatitude() {
            // Given
            double expectedLatitude = 40.7128;
            Location location = new Location(expectedLatitude, -74.0060, "New York", "USA");

            // When
            double actualLatitude = location.getLatitude();

            // Then
            assertEquals(expectedLatitude, actualLatitude);
        }

        @Test
        @DisplayName("Should return correct longitude")
        void shouldReturnCorrectLongitude() {
            // Given
            double expectedLongitude = -74.0060;
            Location location = new Location(40.7128, expectedLongitude, "New York", "USA");

            // When
            double actualLongitude = location.getLongitude();

            // Then
            assertEquals(expectedLongitude, actualLongitude);
        }

        @Test
        @DisplayName("Should return correct city")
        void shouldReturnCorrectCity() {
            // Given
            String expectedCity = "New York";
            Location location = new Location(40.7128, -74.0060, expectedCity, "USA");

            // When
            String actualCity = location.getCity();

            // Then
            assertEquals(expectedCity, actualCity);
        }

        @Test
        @DisplayName("Should return correct country")
        void shouldReturnCorrectCountry() {
            // Given
            String expectedCountry = "USA";
            Location location = new Location(40.7128, -74.0060, "New York", expectedCountry);

            // When
            String actualCountry = location.getCountry();

            // Then
            assertEquals(expectedCountry, actualCountry);
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

            // When & Then
            assertEquals(location, location);
        }

        @Test
        @DisplayName("Should be equal to location with same coordinates")
        void shouldBeEqualToLocationWithSameCoordinates() {
            // Given
            Location location1 = new Location(40.7128, -74.0060, "New York", "USA");
            Location location2 = new Location(40.7128, -74.0060, "New York", "USA");

            // When & Then
            assertEquals(location1, location2);
            assertEquals(location2, location1);
        }

        @Test
        @DisplayName("Should not be equal to location with different coordinates")
        void shouldNotBeEqualToLocationWithDifferentCoordinates() {
            // Given
            Location location1 = new Location(40.7128, -74.0060, "New York", "USA");
            Location location2 = new Location(40.7128, -74.0061, "New York", "USA");

            // When & Then
            assertNotEquals(location1, location2);
            assertNotEquals(location2, location1);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");

            // When & Then
            assertNotEquals(null, location);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");
            String differentType = "Not a Location";

            // When & Then
            assertNotEquals(location, differentType);
        }

        @Test
        @DisplayName("Should have same hash code for equal objects")
        void shouldHaveSameHashCodeForEqualObjects() {
            // Given
            Location location1 = new Location(40.7128, -74.0060, "New York", "USA");
            Location location2 = new Location(40.7128, -74.0060, "New York", "USA");

            // When & Then
            assertEquals(location1.hashCode(), location2.hashCode());
        }

        @Test
        @DisplayName("Should have different hash code for different coordinates")
        void shouldHaveDifferentHashCodeForDifferentCoordinates() {
            // Given
            Location location1 = new Location(40.7128, -74.0060, "New York", "USA");
            Location location2 = new Location(40.7128, -74.0061, "New York", "USA");

            // When & Then
            assertNotEquals(location1.hashCode(), location2.hashCode());
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

            // When
            String result = location.toString();

            // Then
            assertTrue(result.contains("Location{"));
            assertTrue(result.contains("latitude=40.712800"));
            assertTrue(result.contains("longitude=-74.006000"));
            assertTrue(result.contains("city='New York'"));
            assertTrue(result.contains("country='USA'"));
            assertTrue(result.endsWith("}"));
        }

        @Test
        @DisplayName("Should include all fields in string representation")
        void shouldIncludeAllFieldsInStringRepresentation() {
            // Given
            Location location = new Location(40.7128, -74.0060, "New York", "USA");

            // When
            String result = location.toString();

            // Then
            assertTrue(result.contains("latitude="));
            assertTrue(result.contains("longitude="));
            assertTrue(result.contains("city="));
            assertTrue(result.contains("country="));
        }
    }

    @Nested
    @DisplayName("Boundary Tests")
    class BoundaryTests {

        @Test
        @DisplayName("Should accept minimum latitude")
        void shouldAcceptMinimumLatitude() {
            // Given & When
            Location location = new Location(-90.0, -74.0060, "New York", "USA");

            // Then
            assertEquals(-90.0, location.getLatitude());
        }

        @Test
        @DisplayName("Should accept maximum latitude")
        void shouldAcceptMaximumLatitude() {
            // Given & When
            Location location = new Location(90.0, -74.0060, "New York", "USA");

            // Then
            assertEquals(90.0, location.getLatitude());
        }

        @Test
        @DisplayName("Should accept minimum longitude")
        void shouldAcceptMinimumLongitude() {
            // Given & When
            Location location = new Location(40.7128, -180.0, "New York", "USA");

            // Then
            assertEquals(-180.0, location.getLongitude());
        }

        @Test
        @DisplayName("Should accept maximum longitude")
        void shouldAcceptMaximumLongitude() {
            // Given & When
            Location location = new Location(40.7128, 180.0, "New York", "USA");

            // Then
            assertEquals(180.0, location.getLongitude());
        }
    }
} 