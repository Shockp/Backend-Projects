package com.shockp.weather.domain.service;

import com.shockp.weather.application.port.RateLimiterPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link RateLimiterService} domain service.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimiterService Domain Service Tests")
class RateLimiterServiceTest {

    @Mock
    private RateLimiterPort rateLimiterPort;

    private RateLimiterService rateLimiterService;
    private static final int MAX_REQUESTS = 100;
    private static final Duration TIME_WINDOW = Duration.ofMinutes(1);

    @BeforeEach
    void setUp() {
        rateLimiterService = new RateLimiterService(rateLimiterPort, MAX_REQUESTS, TIME_WINDOW);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create rate limiter service with valid parameters")
        void shouldCreateRateLimiterServiceWithValidParameters() {
            // Given & When
            RateLimiterService service = new RateLimiterService(rateLimiterPort, MAX_REQUESTS, TIME_WINDOW);

            // Then
            assertNotNull(service);
            assertEquals(MAX_REQUESTS, service.getMaxRequests());
            assertEquals(TIME_WINDOW, service.getTimeWindow());
        }

        @Test
        @DisplayName("Should throw exception for null rate limiter port")
        void shouldThrowExceptionForNullRateLimiterPort() {
            // When & Then
            NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new RateLimiterService(null, MAX_REQUESTS, TIME_WINDOW)
            );
            assertEquals("Rate limiter port cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for null time window")
        void shouldThrowExceptionForNullTimeWindow() {
            // When & Then
            NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new RateLimiterService(rateLimiterPort, MAX_REQUESTS, null)
            );
            assertEquals("Time window cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for zero max requests")
        void shouldThrowExceptionForZeroMaxRequests() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new RateLimiterService(rateLimiterPort, 0, TIME_WINDOW)
            );
            assertEquals("Max requests must be greater than 0", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for negative max requests")
        void shouldThrowExceptionForNegativeMaxRequests() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new RateLimiterService(rateLimiterPort, -10, TIME_WINDOW)
            );
            assertEquals("Max requests must be greater than 0", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Check Rate Limit Tests")
    class CheckRateLimitTests {

        @Test
        @DisplayName("Should return true when tokens are available")
        void shouldReturnTrueWhenTokensAreAvailable() {
            // Given
            String clientId = "test-client";
            when(rateLimiterPort.getAvailableTokens(clientId)).thenReturn(10);

            // When
            boolean result = rateLimiterService.checkRateLimit(clientId);

            // Then
            assertTrue(result);
            verify(rateLimiterPort).getAvailableTokens(clientId);
        }

        @Test
        @DisplayName("Should return false when no tokens are available")
        void shouldReturnFalseWhenNoTokensAreAvailable() {
            // Given
            String clientId = "test-client";
            when(rateLimiterPort.getAvailableTokens(clientId)).thenReturn(0);

            // When
            boolean result = rateLimiterService.checkRateLimit(clientId);

            // Then
            assertFalse(result);
            verify(rateLimiterPort).getAvailableTokens(clientId);
        }

        @Test
        @DisplayName("Should throw exception for null client ID")
        void shouldThrowExceptionForNullClientId() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> rateLimiterService.checkRateLimit(null)
            );
            assertEquals("Client ID cannot be null or empty", exception.getMessage());
            verify(rateLimiterPort, never()).getAvailableTokens(any());
        }

        @Test
        @DisplayName("Should throw exception for empty client ID")
        void shouldThrowExceptionForEmptyClientId() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> rateLimiterService.checkRateLimit("")
            );
            assertEquals("Client ID cannot be null or empty", exception.getMessage());
            verify(rateLimiterPort, never()).getAvailableTokens(any());
        }

        @Test
        @DisplayName("Should trim whitespace from client ID")
        void shouldTrimWhitespaceFromClientId() {
            // Given
            String clientId = "  test-client  ";
            when(rateLimiterPort.getAvailableTokens("test-client")).thenReturn(10);

            // When
            boolean result = rateLimiterService.checkRateLimit(clientId);

            // Then
            assertTrue(result);
            verify(rateLimiterPort).getAvailableTokens("test-client");
        }

        @Test
        @DisplayName("Should throw WeatherServiceException when port operation fails")
        void shouldThrowWeatherServiceExceptionWhenPortOperationFails() {
            // Given
            String clientId = "test-client";
            when(rateLimiterPort.getAvailableTokens(clientId)).thenThrow(new RuntimeException("Port error"));

            // When & Then
            WeatherServiceException exception = assertThrows(
                WeatherServiceException.class,
                () -> rateLimiterService.checkRateLimit(clientId)
            );
            assertTrue(exception.getMessage().contains("Failed to check rate limit for client: " + clientId));
            assertNotNull(exception.getCause());
        }
    }

    @Nested
    @DisplayName("Consume Token Tests")
    class ConsumeTokenTests {

        @Test
        @DisplayName("Should return true when token is successfully consumed")
        void shouldReturnTrueWhenTokenIsSuccessfullyConsumed() {
            // Given
            String clientId = "test-client";
            when(rateLimiterPort.tryConsume(clientId)).thenReturn(true);

            // When
            boolean result = rateLimiterService.consumeToken(clientId);

            // Then
            assertTrue(result);
            verify(rateLimiterPort).tryConsume(clientId);
        }

        @Test
        @DisplayName("Should return false when token consumption fails")
        void shouldReturnFalseWhenTokenConsumptionFails() {
            // Given
            String clientId = "test-client";
            when(rateLimiterPort.tryConsume(clientId)).thenReturn(false);

            // When
            boolean result = rateLimiterService.consumeToken(clientId);

            // Then
            assertFalse(result);
            verify(rateLimiterPort).tryConsume(clientId);
        }

        @Test
        @DisplayName("Should throw exception for null client ID in consume token")
        void shouldThrowExceptionForNullClientIdInConsumeToken() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> rateLimiterService.consumeToken(null)
            );
            assertEquals("Client ID cannot be null or empty", exception.getMessage());
            verify(rateLimiterPort, never()).tryConsume(any());
        }

        @Test
        @DisplayName("Should throw exception for empty client ID in consume token")
        void shouldThrowExceptionForEmptyClientIdInConsumeToken() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> rateLimiterService.consumeToken("")
            );
            assertEquals("Client ID cannot be null or empty", exception.getMessage());
            verify(rateLimiterPort, never()).tryConsume(any());
        }

        @Test
        @DisplayName("Should trim whitespace from client ID in consume token")
        void shouldTrimWhitespaceFromClientIdInConsumeToken() {
            // Given
            String clientId = "  test-client  ";
            when(rateLimiterPort.tryConsume("test-client")).thenReturn(true);

            // When
            boolean result = rateLimiterService.consumeToken(clientId);

            // Then
            assertTrue(result);
            verify(rateLimiterPort).tryConsume("test-client");
        }

        @Test
        @DisplayName("Should throw WeatherServiceException when consume token operation fails")
        void shouldThrowWeatherServiceExceptionWhenConsumeTokenOperationFails() {
            // Given
            String clientId = "test-client";
            when(rateLimiterPort.tryConsume(clientId)).thenThrow(new RuntimeException("Port error"));

            // When & Then
            WeatherServiceException exception = assertThrows(
                WeatherServiceException.class,
                () -> rateLimiterService.consumeToken(clientId)
            );
            assertTrue(exception.getMessage().contains("Failed to consume rate limit token for client: " + clientId));
            assertNotNull(exception.getCause());
        }
    }

    @Nested
    @DisplayName("Get Remaining Tokens Tests")
    class GetRemainingTokensTests {

        @Test
        @DisplayName("Should return correct number of remaining tokens")
        void shouldReturnCorrectNumberOfRemainingTokens() {
            // Given
            String clientId = "test-client";
            int expectedTokens = 25;
            when(rateLimiterPort.getAvailableTokens(clientId)).thenReturn(expectedTokens);

            // When
            int result = rateLimiterService.getRemainingTokens(clientId);

            // Then
            assertEquals(expectedTokens, result);
            verify(rateLimiterPort).getAvailableTokens(clientId);
        }

        @Test
        @DisplayName("Should return zero when no tokens remaining")
        void shouldReturnZeroWhenNoTokensRemaining() {
            // Given
            String clientId = "test-client";
            when(rateLimiterPort.getAvailableTokens(clientId)).thenReturn(0);

            // When
            int result = rateLimiterService.getRemainingTokens(clientId);

            // Then
            assertEquals(0, result);
            verify(rateLimiterPort).getAvailableTokens(clientId);
        }

        @Test
        @DisplayName("Should throw exception for null client ID in get remaining tokens")
        void shouldThrowExceptionForNullClientIdInGetRemainingTokens() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> rateLimiterService.getRemainingTokens(null)
            );
            assertEquals("Client ID cannot be null or empty", exception.getMessage());
            verify(rateLimiterPort, never()).getAvailableTokens(any());
        }

        @Test
        @DisplayName("Should throw exception for empty client ID in get remaining tokens")
        void shouldThrowExceptionForEmptyClientIdInGetRemainingTokens() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> rateLimiterService.getRemainingTokens("")
            );
            assertEquals("Client ID cannot be null or empty", exception.getMessage());
            verify(rateLimiterPort, never()).getAvailableTokens(any());
        }

        @Test
        @DisplayName("Should trim whitespace from client ID in get remaining tokens")
        void shouldTrimWhitespaceFromClientIdInGetRemainingTokens() {
            // Given
            String clientId = "  test-client  ";
            when(rateLimiterPort.getAvailableTokens("test-client")).thenReturn(50);

            // When
            int result = rateLimiterService.getRemainingTokens(clientId);

            // Then
            assertEquals(50, result);
            verify(rateLimiterPort).getAvailableTokens("test-client");
        }

        @Test
        @DisplayName("Should throw WeatherServiceException when get remaining tokens operation fails")
        void shouldThrowWeatherServiceExceptionWhenGetRemainingTokensOperationFails() {
            // Given
            String clientId = "test-client";
            when(rateLimiterPort.getAvailableTokens(clientId)).thenThrow(new RuntimeException("Port error"));

            // When & Then
            WeatherServiceException exception = assertThrows(
                WeatherServiceException.class,
                () -> rateLimiterService.getRemainingTokens(clientId)
            );
            assertTrue(exception.getMessage().contains("Failed to get remaining tokens for client: " + clientId));
            assertNotNull(exception.getCause());
        }
    }

    @Nested
    @DisplayName("Reset Tests")
    class ResetTests {

        @Test
        @DisplayName("Should reset rate limit for client")
        void shouldResetRateLimitForClient() {
            // Given
            String clientId = "test-client";

            // When
            rateLimiterService.reset(clientId);

            // Then
            verify(rateLimiterPort).reset(clientId);
        }

        @Test
        @DisplayName("Should throw exception for null client ID in reset")
        void shouldThrowExceptionForNullClientIdInReset() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> rateLimiterService.reset(null)
            );
            assertEquals("Client ID cannot be null or empty", exception.getMessage());
            verify(rateLimiterPort, never()).reset(any());
        }

        @Test
        @DisplayName("Should throw exception for empty client ID in reset")
        void shouldThrowExceptionForEmptyClientIdInReset() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> rateLimiterService.reset("")
            );
            assertEquals("Client ID cannot be null or empty", exception.getMessage());
            verify(rateLimiterPort, never()).reset(any());
        }

        @Test
        @DisplayName("Should trim whitespace from client ID in reset")
        void shouldTrimWhitespaceFromClientIdInReset() {
            // Given
            String clientId = "  test-client  ";

            // When
            rateLimiterService.reset(clientId);

            // Then
            verify(rateLimiterPort).reset("test-client");
        }

        @Test
        @DisplayName("Should throw WeatherServiceException when reset operation fails")
        void shouldThrowWeatherServiceExceptionWhenResetOperationFails() {
            // Given
            String clientId = "test-client";
            doThrow(new RuntimeException("Port error")).when(rateLimiterPort).reset(clientId);

            // When & Then
            WeatherServiceException exception = assertThrows(
                WeatherServiceException.class,
                () -> rateLimiterService.reset(clientId)
            );
            assertTrue(exception.getMessage().contains("Failed to reset rate limit for client: " + clientId));
            assertNotNull(exception.getCause());
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("Should return correct max requests")
        void shouldReturnCorrectMaxRequests() {
            // When
            int result = rateLimiterService.getMaxRequests();

            // Then
            assertEquals(MAX_REQUESTS, result);
        }

        @Test
        @DisplayName("Should return correct time window")
        void shouldReturnCorrectTimeWindow() {
            // When
            Duration result = rateLimiterService.getTimeWindow();

            // Then
            assertEquals(TIME_WINDOW, result);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle complete rate limiting flow")
        void shouldHandleCompleteRateLimitingFlow() {
            // Given
            String clientId = "test-client";
            when(rateLimiterPort.getAvailableTokens(clientId)).thenReturn(10);
            when(rateLimiterPort.tryConsume(clientId)).thenReturn(true);

            // When
            boolean canCheck = rateLimiterService.checkRateLimit(clientId);
            boolean consumed = rateLimiterService.consumeToken(clientId);
            int remaining = rateLimiterService.getRemainingTokens(clientId);

            // Then
            assertTrue(canCheck);
            assertTrue(consumed);
            assertEquals(10, remaining);

            verify(rateLimiterPort).getAvailableTokens(clientId);
            verify(rateLimiterPort).tryConsume(clientId);
            verify(rateLimiterPort).getAvailableTokens(clientId);
        }

        @Test
        @DisplayName("Should handle rate limit exceeded scenario")
        void shouldHandleRateLimitExceededScenario() {
            // Given
            String clientId = "test-client";
            when(rateLimiterPort.getAvailableTokens(clientId)).thenReturn(0);
            when(rateLimiterPort.tryConsume(clientId)).thenReturn(false);

            // When
            boolean canCheck = rateLimiterService.checkRateLimit(clientId);
            boolean consumed = rateLimiterService.consumeToken(clientId);
            int remaining = rateLimiterService.getRemainingTokens(clientId);

            // Then
            assertFalse(canCheck);
            assertFalse(consumed);
            assertEquals(0, remaining);

            verify(rateLimiterPort).getAvailableTokens(clientId);
            verify(rateLimiterPort).tryConsume(clientId);
            verify(rateLimiterPort).getAvailableTokens(clientId);
        }
    }
} 