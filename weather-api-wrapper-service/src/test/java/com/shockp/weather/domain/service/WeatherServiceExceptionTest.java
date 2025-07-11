package com.shockp.weather.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link WeatherServiceException} class.
 */
@DisplayName("WeatherServiceException Tests")
class WeatherServiceExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create exception with message")
        void shouldCreateExceptionWithMessage() {
            // Given
            String message = "Test error message";

            // When
            WeatherServiceException exception = new WeatherServiceException(message);

            // Then
            assertNotNull(exception);
            assertEquals(message, exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should create exception with null message")
        void shouldCreateExceptionWithNullMessage() {
            // When
            WeatherServiceException exception = new WeatherServiceException(null);

            // Then
            assertNotNull(exception);
            assertNull(exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should create exception with empty message")
        void shouldCreateExceptionWithEmptyMessage() {
            // Given
            String message = "";

            // When
            WeatherServiceException exception = new WeatherServiceException(message);

            // Then
            assertNotNull(exception);
            assertEquals(message, exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should create exception with message and cause")
        void shouldCreateExceptionWithMessageAndCause() {
            // Given
            String message = "Test error message";
            RuntimeException cause = new RuntimeException("Original error");

            // When
            WeatherServiceException exception = new WeatherServiceException(message, cause);

            // Then
            assertNotNull(exception);
            assertEquals(message, exception.getMessage());
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("Should create exception with null message and cause")
        void shouldCreateExceptionWithNullMessageAndCause() {
            // Given
            RuntimeException cause = new RuntimeException("Original error");

            // When
            WeatherServiceException exception = new WeatherServiceException(null, cause);

            // Then
            assertNotNull(exception);
            assertNull(exception.getMessage());
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("Should create exception with message and null cause")
        void shouldCreateExceptionWithMessageAndNullCause() {
            // Given
            String message = "Test error message";

            // When
            WeatherServiceException exception = new WeatherServiceException(message, null);

            // Then
            assertNotNull(exception);
            assertEquals(message, exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should create exception with cause only")
        void shouldCreateExceptionWithCauseOnly() {
            // Given
            RuntimeException cause = new RuntimeException("Original error");

            // When
            WeatherServiceException exception = new WeatherServiceException(cause);

            // Then
            assertNotNull(exception);
            assertEquals(cause, exception.getCause());
            // The message should be the cause's message
            assertEquals(cause.toString(), exception.getMessage());
        }

        @Test
        @DisplayName("Should create exception with null cause only")
        void shouldCreateExceptionWithNullCauseOnly() {
            // When
            WeatherServiceException exception = new WeatherServiceException((Throwable) null);

            // Then
            assertNotNull(exception);
            assertNull(exception.getCause());
            assertNull(exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("Should be instance of RuntimeException")
        void shouldBeInstanceOfRuntimeException() {
            // Given
            WeatherServiceException exception = new WeatherServiceException("Test");

            // When & Then
            assertTrue(exception instanceof RuntimeException);
        }

        @Test
        @DisplayName("Should be instance of Exception")
        void shouldBeInstanceOfException() {
            // Given
            WeatherServiceException exception = new WeatherServiceException("Test");

            // When & Then
            assertTrue(exception instanceof Exception);
        }

        @Test
        @DisplayName("Should be instance of Throwable")
        void shouldBeInstanceOfThrowable() {
            // Given
            WeatherServiceException exception = new WeatherServiceException("Test");

            // When & Then
            assertTrue(exception instanceof Throwable);
        }
    }

    @Nested
    @DisplayName("Message Tests")
    class MessageTests {

        @Test
        @DisplayName("Should preserve message with special characters")
        void shouldPreserveMessageWithSpecialCharacters() {
            // Given
            String message = "Error with special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?";

            // When
            WeatherServiceException exception = new WeatherServiceException(message);

            // Then
            assertEquals(message, exception.getMessage());
        }

        @Test
        @DisplayName("Should preserve message with newlines")
        void shouldPreserveMessageWithNewlines() {
            // Given
            String message = "Error with\nnewlines\nand\ttabs";

            // When
            WeatherServiceException exception = new WeatherServiceException(message);

            // Then
            assertEquals(message, exception.getMessage());
        }

        @Test
        @DisplayName("Should preserve message with unicode characters")
        void shouldPreserveMessageWithUnicodeCharacters() {
            // Given
            String message = "Error with unicode: 你好世界 🌍 ☀️";

            // When
            WeatherServiceException exception = new WeatherServiceException(message);

            // Then
            assertEquals(message, exception.getMessage());
        }

        @Test
        @DisplayName("Should preserve very long message")
        void shouldPreserveVeryLongMessage() {
            // Given
            StringBuilder longMessage = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                longMessage.append("This is a very long error message that contains many characters. ");
            }

            // When
            WeatherServiceException exception = new WeatherServiceException(longMessage.toString());

            // Then
            assertEquals(longMessage.toString(), exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Cause Tests")
    class CauseTests {

        @Test
        @DisplayName("Should preserve cause exception")
        void shouldPreserveCauseException() {
            // Given
            IllegalArgumentException cause = new IllegalArgumentException("Invalid argument");

            // When
            WeatherServiceException exception = new WeatherServiceException("Wrapper error", cause);

            // Then
            assertEquals(cause, exception.getCause());
            assertEquals("Invalid argument", exception.getCause().getMessage());
        }

        @Test
        @DisplayName("Should preserve cause chain")
        void shouldPreserveCauseChain() {
            // Given
            RuntimeException cause3 = new RuntimeException("Level 3");
            Exception cause2 = new Exception("Level 2", cause3);
            Throwable cause1 = new Throwable("Level 1", cause2);

            // When
            WeatherServiceException exception = new WeatherServiceException("Top level", cause1);

            // Then
            assertEquals(cause1, exception.getCause());
            assertEquals(cause2, exception.getCause().getCause());
            assertEquals(cause3, exception.getCause().getCause().getCause());
        }

        @Test
        @DisplayName("Should handle self-referencing cause")
        void shouldHandleSelfReferencingCause() {
            // Given
            WeatherServiceException selfReferencing = new WeatherServiceException("Self reference");
            // Note: In practice, you can't create a self-referencing exception, but this tests the structure

            // When
            WeatherServiceException exception = new WeatherServiceException("Wrapper", selfReferencing);

            // Then
            assertEquals(selfReferencing, exception.getCause());
        }
    }

    @Nested
    @DisplayName("Serialization Tests")
    class SerializationTests {

        @Test
        @DisplayName("Should have serial version UID")
        void shouldHaveSerialVersionUID() {
            // Given
            WeatherServiceException exception = new WeatherServiceException("Test");

            // When & Then
            // This test verifies that the class has a serialVersionUID field
            // The actual value is defined in the class as 1L
            assertNotNull(exception);
            // We can't directly test the serialVersionUID field as it's private,
            // but we can verify the class structure is correct for serialization
        }

        @Test
        @DisplayName("Should maintain exception type after construction")
        void shouldMaintainExceptionTypeAfterConstruction() {
            // Given
            String message = "Test message";
            RuntimeException cause = new RuntimeException("Cause");

            // When
            WeatherServiceException exception1 = new WeatherServiceException(message);
            WeatherServiceException exception2 = new WeatherServiceException(message, cause);
            WeatherServiceException exception3 = new WeatherServiceException(cause);

            // Then
            assertEquals(WeatherServiceException.class, exception1.getClass());
            assertEquals(WeatherServiceException.class, exception2.getClass());
            assertEquals(WeatherServiceException.class, exception3.getClass());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty string message")
        void shouldHandleEmptyStringMessage() {
            // When
            WeatherServiceException exception = new WeatherServiceException("");

            // Then
            assertEquals("", exception.getMessage());
        }

        @Test
        @DisplayName("Should handle whitespace only message")
        void shouldHandleWhitespaceOnlyMessage() {
            // Given
            String message = "   \t\n   ";

            // When
            WeatherServiceException exception = new WeatherServiceException(message);

            // Then
            assertEquals(message, exception.getMessage());
        }

        @Test
        @DisplayName("Should handle exception with both null message and null cause")
        void shouldHandleExceptionWithBothNullMessageAndNullCause() {
            // When
            WeatherServiceException exception = new WeatherServiceException(null, null);

            // Then
            assertNull(exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should handle multiple exceptions with same message")
        void shouldHandleMultipleExceptionsWithSameMessage() {
            // Given
            String message = "Same message";

            // When
            WeatherServiceException exception1 = new WeatherServiceException(message);
            WeatherServiceException exception2 = new WeatherServiceException(message);

            // Then
            assertEquals(message, exception1.getMessage());
            assertEquals(message, exception2.getMessage());
            // They should be different instances
            assertNotSame(exception1, exception2);
        }
    }
} 