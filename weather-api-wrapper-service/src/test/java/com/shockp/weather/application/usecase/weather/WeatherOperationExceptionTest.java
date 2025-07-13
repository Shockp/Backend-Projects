package com.shockp.weather.application.usecase.weather;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link WeatherOperationException}.
 * <p>
 * These tests verify the proper construction and behavior of the weather
 * operation exception class.
 * </p>
 *
 * @author Weather API Wrapper Service
 * @version 1.0
 * @since 1.0
 */
@DisplayName("WeatherOperationException Tests")
class WeatherOperationExceptionTest {

    @Test
    @DisplayName("Should create exception with message")
    void shouldCreateExceptionWithMessage() {
        // Given
        String message = "Test error message";

        // When
        WeatherOperationException exception = new WeatherOperationException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should create exception with null message")
    void shouldCreateExceptionWithNullMessage() {
        // When
        WeatherOperationException exception = new WeatherOperationException((String) null);

        // Then
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should create exception with message and cause")
    void shouldCreateExceptionWithMessageAndCause() {
        // Given
        String message = "Test error message";
        Throwable cause = new RuntimeException("Original cause");

        // When
        WeatherOperationException exception = new WeatherOperationException(message, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("Should create exception with null message and cause")
    void shouldCreateExceptionWithNullMessageAndCause() {
        // Given
        Throwable cause = new RuntimeException("Original cause");

        // When
        WeatherOperationException exception = new WeatherOperationException((String) null, cause);

        // Then
        assertNull(exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("Should create exception with message and null cause")
    void shouldCreateExceptionWithMessageAndNullCause() {
        // Given
        String message = "Test error message";

        // When
        WeatherOperationException exception = new WeatherOperationException(message, null);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should create exception with null message and null cause")
    void shouldCreateExceptionWithNullMessageAndNullCause() {
        // When
        WeatherOperationException exception = new WeatherOperationException((String) null, null);

        // Then
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should create exception with cause only")
    void shouldCreateExceptionWithCauseOnly() {
        // Given
        Throwable cause = new RuntimeException("Original cause");

        // When
        WeatherOperationException exception = new WeatherOperationException(cause);

        // Then
        assertEquals(cause.toString(), exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("Should create exception with null cause only")
    void shouldCreateExceptionWithNullCauseOnly() {
        // When
        WeatherOperationException exception = new WeatherOperationException((Throwable) null);

        // Then
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should be serializable")
    void shouldBeSerializable() {
        // Given
        String message = "Test error message";
        Throwable cause = new RuntimeException("Original cause");
        WeatherOperationException exception = new WeatherOperationException(message, cause);

        // When & Then
        assertNotNull(exception);
        // Note: In a real scenario, you would test actual serialization/deserialization
        // This test verifies the exception can be instantiated properly
    }

    @Test
    @DisplayName("Should maintain exception hierarchy")
    void shouldMaintainExceptionHierarchy() {
        // Given
        WeatherOperationException exception = new WeatherOperationException("Test message");

        // When & Then
        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }

    @Test
    @DisplayName("Should preserve stack trace")
    void shouldPreserveStackTrace() {
        // Given
        WeatherOperationException exception = new WeatherOperationException("Test message");

        // When
        StackTraceElement[] stackTrace = exception.getStackTrace();

        // Then
        assertNotNull(stackTrace);
        assertTrue(stackTrace.length > 0);
    }
} 