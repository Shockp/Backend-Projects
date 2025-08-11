package com.personalblog.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for Tag entity.
 * 
 * Tests cover:
 * - Entity validation with Bean Validation API
 * - Business logic methods
 * - Relationship management
 * - Object methods (equals, hashCode, toString)
 * - Constructor behavior
 * - Edge cases and boundary conditions
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 */
@DisplayName("Tag Entity Tests")
class TagTest {

    private Validator validator;
    private Tag validTag;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        // Create a valid tag for testing
        validTag = new Tag("Java Programming", "java-programming");
        validTag.setDescription("All about Java programming language");
        validTag.setColorCode("#FF5733");
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor should initialize with default values")
        void defaultConstructor_ShouldInitializeWithDefaults() {
            Tag tag = new Tag();
            
            assertThat(tag.getName()).isNull();
            assertThat(tag.getSlug()).isNull();
            assertThat(tag.getDescription()).isNull();
            assertThat(tag.getColorCode()).isNull();
            assertThat(tag.getUsageCount()).isEqualTo(0);
            assertThat(tag.getBlogPosts()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Constructor with name and slug should set required fields")
        void constructorWithNameAndSlug_ShouldSetRequiredFields() {
            String name = "Spring Boot";
            String slug = "spring-boot";
            
            Tag tag = new Tag(name, slug);
            
            assertThat(tag.getName()).isEqualTo(name);
            assertThat(tag.getSlug()).isEqualTo(slug);
            assertThat(tag.getUsageCount()).isEqualTo(0);
            assertThat(tag.getBlogPosts()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Constructor with all fields should set all provided values")
        void constructorWithAllFields_ShouldSetAllValues() {
            String name = "React";
            String slug = "react";
            String description = "React JavaScript library";
            String colorCode = "#61DAFB";
            
            Tag tag = new Tag(name, slug, description, colorCode);
            
            assertThat(tag.getName()).isEqualTo(name);
            assertThat(tag.getSlug()).isEqualTo(slug);
            assertThat(tag.getDescription()).isEqualTo(description);
            assertThat(tag.getColorCode()).isEqualTo(colorCode);
            assertThat(tag.getUsageCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Nested
        @DisplayName("Name Validation")
        class NameValidationTests {

            @ParameterizedTest
            @NullAndEmptySource
            @ValueSource(strings = {" ", "\t", "\n"})
            @DisplayName("Name should not be blank")
            void name_ShouldNotBeBlank(String invalidName) {
                validTag.setName(invalidName);
                
                Set<ConstraintViolation<Tag>> violations = validator.validate(validTag);
                
                assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Tag name is required");
            }

            @Test
            @DisplayName("Name should not be too short")
            void name_ShouldNotBeTooShort() {
                validTag.setName("A");
                
                Set<ConstraintViolation<Tag>> violations = validator.validate(validTag);
                
                assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Tag name must be between 2 and 50 characters");
            }

            @Test
            @DisplayName("Name should not be too long")
            void name_ShouldNotBeTooLong() {
                validTag.setName("A".repeat(51));
                
                Set<ConstraintViolation<Tag>> violations = validator.validate(validTag);
                
                assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Tag name must be between 2 and 50 characters");
            }

            @ParameterizedTest
            @ValueSource(strings = {"AB", "Valid Tag Name", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"})
            @DisplayName("Name should accept valid lengths")
            void name_ShouldAcceptValidLengths(String validName) {
                validTag.setName(validName);
                
                Set<ConstraintViolation<Tag>> violations = validator.validate(validTag);
                
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("name"))
                    .isEmpty();
            }
        }

        @Nested
        @DisplayName("Slug Validation")
        class SlugValidationTests {

            @ParameterizedTest
            @NullAndEmptySource
            @ValueSource(strings = {" ", "\t", "\n"})
            @DisplayName("Slug should not be blank")
            void slug_ShouldNotBeBlank(String invalidSlug) {
                validTag.setSlug(invalidSlug);
                
                Set<ConstraintViolation<Tag>> violations = validator.validate(validTag);
                
                assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Tag slug is required");
            }

            @ParameterizedTest
            @ValueSource(strings = {"UPPERCASE", "with spaces", "with_underscore", "with.dot", "with@symbol"})
            @DisplayName("Slug should only contain lowercase letters, numbers, and hyphens")
            void slug_ShouldOnlyContainValidCharacters(String invalidSlug) {
                validTag.setSlug(invalidSlug);
                
                Set<ConstraintViolation<Tag>> violations = validator.validate(validTag);
                
                assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Tag slug must contain only lowercase letters, numbers, and hyphens");
            }

            @ParameterizedTest
            @ValueSource(strings = {"valid-slug", "slug123", "a-b-c-123", "simple"})
            @DisplayName("Slug should accept valid formats")
            void slug_ShouldAcceptValidFormats(String validSlug) {
                validTag.setSlug(validSlug);
                
                Set<ConstraintViolation<Tag>> violations = validator.validate(validTag);
                
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("slug"))
                    .isEmpty();
            }
        }

        @Nested
        @DisplayName("Description Validation")
        class DescriptionValidationTests {

            @Test
            @DisplayName("Description can be null")
            void description_CanBeNull() {
                validTag.setDescription(null);
                
                Set<ConstraintViolation<Tag>> violations = validator.validate(validTag);
                
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("description"))
                    .isEmpty();
            }

            @Test
            @DisplayName("Description should not exceed maximum length")
            void description_ShouldNotExceedMaxLength() {
                validTag.setDescription("A".repeat(501));
                
                Set<ConstraintViolation<Tag>> violations = validator.validate(validTag);
                
                assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Tag description must be at most 500 characters");
            }

            @Test
            @DisplayName("Description should accept valid length")
            void description_ShouldAcceptValidLength() {
                validTag.setDescription("A".repeat(500));
                
                Set<ConstraintViolation<Tag>> violations = validator.validate(validTag);
                
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("description"))
                    .isEmpty();
            }
        }

        @Nested
        @DisplayName("Color Code Validation")
        class ColorCodeValidationTests {

            @Test
            @DisplayName("Color code can be null")
            void colorCode_CanBeNull() {
                validTag.setColorCode(null);
                
                Set<ConstraintViolation<Tag>> violations = validator.validate(validTag);
                
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("colorCode"))
                    .isEmpty();
            }

            @ParameterizedTest
            @ValueSource(strings = {"FF5733", "#FF573", "#FF57333", "#GG5733", "invalid"})
            @DisplayName("Color code should match hex pattern")
            void colorCode_ShouldMatchHexPattern(String invalidColorCode) {
                validTag.setColorCode(invalidColorCode);
                
                Set<ConstraintViolation<Tag>> violations = validator.validate(validTag);
                
                assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Color code must be a valid hex color (e.g., #FF5733)");
            }

            @ParameterizedTest
            @ValueSource(strings = {"#FF5733", "#000000", "#FFFFFF", "#123ABC", "#ff5733"})
            @DisplayName("Color code should accept valid hex colors")
            void colorCode_ShouldAcceptValidHexColors(String validColorCode) {
                validTag.setColorCode(validColorCode);
                
                Set<ConstraintViolation<Tag>> violations = validator.validate(validTag);
                
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("colorCode"))
                    .isEmpty();
            }
        }

        @Nested
        @DisplayName("Usage Count Validation")
        class UsageCountValidationTests {

            @Test
            @DisplayName("Usage count should not be negative")
            void usageCount_ShouldNotBeNegative() {
                validTag.setUsageCount(-1);
                
                Set<ConstraintViolation<Tag>> violations = validator.validate(validTag);
                
                assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Usage count cannot be negative");
            }

            @ParameterizedTest
            @ValueSource(ints = {0, 1, 100, 1000})
            @DisplayName("Usage count should accept non-negative values")
            void usageCount_ShouldAcceptNonNegativeValues(int validUsageCount) {
                validTag.setUsageCount(validUsageCount);
                
                Set<ConstraintViolation<Tag>> violations = validator.validate(validTag);
                
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("usageCount"))
                    .isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Increment usage count should increase count by one and return new value")
        void incrementUsageCount_ShouldIncreaseByOne() {
            int initialCount = validTag.getUsageCount();
            
            int newValue = validTag.incrementUsageCount();
            
            assertThat(newValue).isEqualTo(initialCount + 1);
            assertThat(validTag.getUsageCount()).isEqualTo(initialCount + 1);
        }

        @Test
        @DisplayName("Decrement usage count should decrease count by one and return new value")
        void decrementUsageCount_ShouldDecreaseByOne() {
            validTag.setUsageCount(5);
            
            int newValue = validTag.decrementUsageCount();
            
            assertThat(newValue).isEqualTo(4);
            assertThat(validTag.getUsageCount()).isEqualTo(4);
        }

        @Test
        @DisplayName("Decrement usage count should not go below zero")
        void decrementUsageCount_ShouldNotGoBelowZero() {
            validTag.setUsageCount(0);
            
            int newValue = validTag.decrementUsageCount();
            
            assertThat(newValue).isEqualTo(0);
            assertThat(validTag.getUsageCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should atomically set usage count and return previous value")
        void setUsageCountAtomic_ShouldReturnPreviousValue() {
            validTag.setUsageCount(10);
            
            int previousValue = validTag.setUsageCountAtomic(25);
            
            assertThat(previousValue).isEqualTo(10);
            assertThat(validTag.getUsageCount()).isEqualTo(25);
        }

        @Test
        @DisplayName("Should throw exception when setting negative usage count atomically")
        void setUsageCountAtomic_ShouldThrowExceptionForNegativeValue() {
            assertThatThrownBy(() -> validTag.setUsageCountAtomic(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Usage count cannot be negative");
        }

        @Test
        @DisplayName("Should add positive delta to usage count")
        void addToUsageCount_ShouldAddPositiveDelta() {
            validTag.setUsageCount(10);
            
            int newValue = validTag.addToUsageCount(5);
            
            assertThat(newValue).isEqualTo(15);
            assertThat(validTag.getUsageCount()).isEqualTo(15);
        }

        @Test
        @DisplayName("Should add negative delta to usage count but not go below zero")
        void addToUsageCount_ShouldNotGoBelowZero() {
            validTag.setUsageCount(3);
            
            int newValue = validTag.addToUsageCount(-5);
            
            assertThat(newValue).isEqualTo(0);
            assertThat(validTag.getUsageCount()).isEqualTo(0);
        }

        @ParameterizedTest
        @CsvSource({
            "0, false",
            "1, true",
            "5, true",
            "100, true"
        })
        @DisplayName("Is in use should return correct boolean based on usage count")
        void isInUse_ShouldReturnCorrectBoolean(int usageCount, boolean expectedResult) {
            validTag.setUsageCount(usageCount);
            
            assertThat(validTag.isInUse()).isEqualTo(expectedResult);
        }

        @Test
        @DisplayName("Get display name with count should format correctly")
        void getDisplayNameWithCount_ShouldFormatCorrectly() {
            validTag.setUsageCount(42);
            
            String displayName = validTag.getDisplayNameWithCount();
            
            assertThat(displayName).isEqualTo("Java Programming (42)");
        }
    }

    @Nested
    @DisplayName("Object Methods Tests")
    class ObjectMethodsTests {

        @Test
        @DisplayName("Equals should return true for same object")
        void equals_ShouldReturnTrueForSameObject() {
            assertThat(validTag).isEqualTo(validTag);
        }

        @Test
        @DisplayName("Equals should return false for null")
        void equals_ShouldReturnFalseForNull() {
            assertThat(validTag).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Equals should return false for different class")
        void equals_ShouldReturnFalseForDifferentClass() {
            assertThat(validTag).isNotEqualTo("not a tag");
        }

        @Test
        @DisplayName("Equals should return true for same slug")
        void equals_ShouldReturnTrueForSameSlug() {
            Tag otherTag = new Tag("Different Name", "java-programming");
            
            assertThat(validTag).isEqualTo(otherTag);
        }

        @Test
        @DisplayName("Equals should return false for different slug")
        void equals_ShouldReturnFalseForDifferentSlug() {
            Tag otherTag = new Tag("Java Programming", "different-slug");
            
            assertThat(validTag).isNotEqualTo(otherTag);
        }

        @Test
        @DisplayName("Hash code should be consistent")
        void hashCode_ShouldBeConsistent() {
            int hashCode1 = validTag.hashCode();
            int hashCode2 = validTag.hashCode();
            
            assertThat(hashCode1).isEqualTo(hashCode2);
        }

        @Test
        @DisplayName("Hash code should be equal for equal objects")
        void hashCode_ShouldBeEqualForEqualObjects() {
            Tag otherTag = new Tag("Different Name", "java-programming");
            
            assertThat(validTag.hashCode()).isEqualTo(otherTag.hashCode());
        }

        @Test
        @DisplayName("ToString should contain key information")
        void toString_ShouldContainKeyInformation() {
            // Set ID for testing (normally set by JPA)
            validTag.setId(1L);
            
            String toString = validTag.toString();
            
            assertThat(toString)
                .contains("Tag{")
                .contains("id=1")
                .contains("name='Java Programming'")
                .contains("slug='java-programming'")
                .contains("usageCount=0");
        }
    }

    @Nested
    @DisplayName("Relationship Tests")
    class RelationshipTests {

        @Test
        @DisplayName("Blog posts should be initialized as empty set")
        void blogPosts_ShouldBeInitializedAsEmptySet() {
            Tag tag = new Tag();
            
            assertThat(tag.getBlogPosts())
                .isNotNull()
                .isEmpty();
        }

        @Test
        @DisplayName("Should be able to set blog posts")
        void shouldBeAbleToSetBlogPosts() {
            // Create mock blog posts (we can't create real BlogPost objects without full setup)
            Set<BlogPost> blogPosts = Set.of();
            
            validTag.setBlogPosts(blogPosts);
            
            assertThat(validTag.getBlogPosts()).isSameAs(blogPosts);
        }
    }

    @Nested
    @DisplayName("Thread Safety and Concurrency Tests")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Should handle concurrent increments correctly")
        void concurrentIncrements_ShouldBeThreadSafe() throws InterruptedException {
            validTag.setUsageCount(0);
            int numberOfThreads = 10;
            int incrementsPerThread = 100;
            ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
            CountDownLatch latch = new CountDownLatch(numberOfThreads);

            for (int i = 0; i < numberOfThreads; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < incrementsPerThread; j++) {
                            validTag.incrementUsageCount();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            executor.shutdown();
            assertThat(validTag.getUsageCount()).isEqualTo(numberOfThreads * incrementsPerThread);
        }

        @Test
        @DisplayName("Should handle concurrent decrements correctly")
        void concurrentDecrements_ShouldBeThreadSafe() throws InterruptedException {
            int initialCount = 1000;
            validTag.setUsageCount(initialCount);
            int numberOfThreads = 10;
            int decrementsPerThread = 50;
            ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
            CountDownLatch latch = new CountDownLatch(numberOfThreads);

            for (int i = 0; i < numberOfThreads; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < decrementsPerThread; j++) {
                            validTag.decrementUsageCount();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            executor.shutdown();
            assertThat(validTag.getUsageCount()).isEqualTo(initialCount - (numberOfThreads * decrementsPerThread));
        }

        @Test
        @DisplayName("Should handle mixed concurrent operations correctly")
        void mixedConcurrentOperations_ShouldBeThreadSafe() throws InterruptedException {
            validTag.setUsageCount(500);
            int numberOfThreads = 20;
            ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
            CountDownLatch latch = new CountDownLatch(numberOfThreads);
            AtomicInteger incrementCount = new AtomicInteger(0);
            AtomicInteger decrementCount = new AtomicInteger(0);

            for (int i = 0; i < numberOfThreads; i++) {
                final int threadIndex = i;
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < 50; j++) {
                            if (threadIndex % 2 == 0) {
                                validTag.incrementUsageCount();
                                incrementCount.incrementAndGet();
                            } else {
                                validTag.decrementUsageCount();
                                decrementCount.incrementAndGet();
                            }
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            executor.shutdown();
            
            int expectedCount = Math.max(0, 500 + incrementCount.get() - decrementCount.get());
            assertThat(validTag.getUsageCount()).isEqualTo(expectedCount);
            assertThat(validTag.getUsageCount()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Should handle concurrent atomic operations correctly")
        void concurrentAtomicOperations_ShouldBeThreadSafe() throws InterruptedException {
            validTag.setUsageCount(0);
            int numberOfThreads = 10;
            ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
            CountDownLatch latch = new CountDownLatch(numberOfThreads);

            for (int i = 0; i < numberOfThreads; i++) {
                final int delta = (i % 2 == 0) ? 10 : -5;
                executor.submit(() -> {
                    try {
                        validTag.addToUsageCount(delta);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            executor.shutdown();
            assertThat(validTag.getUsageCount()).isGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Multiple increment and decrement operations should work correctly")
        void multipleIncrementDecrement_ShouldWorkCorrectly() {
            // Start with 0
            assertThat(validTag.getUsageCount()).isEqualTo(0);
            
            // Increment multiple times
            validTag.incrementUsageCount();
            validTag.incrementUsageCount();
            validTag.incrementUsageCount();
            assertThat(validTag.getUsageCount()).isEqualTo(3);
            
            // Decrement multiple times
            validTag.decrementUsageCount();
            validTag.decrementUsageCount();
            assertThat(validTag.getUsageCount()).isEqualTo(1);
            
            // Try to go below zero
            validTag.decrementUsageCount();
            validTag.decrementUsageCount();
            assertThat(validTag.getUsageCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle null values gracefully")
        void shouldHandleNullValues() {
            Tag tag = new Tag();

            assertThatCode(() -> {
                tag.setName(null);
                tag.setSlug(null);
                tag.setDescription(null);
                tag.setColorCode(null);
                tag.setUsageCount(null);
            }).doesNotThrowAnyException();
            
            // Usage count should default to 0 when null is set
            assertThat(tag.getUsageCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle empty collections")
        void shouldHandleEmptyCollections() {
            validTag.setBlogPosts(new HashSet<>());

            assertThat(validTag.getBlogPosts()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Should initialize atomic counter after entity loading")
        void shouldInitializeAtomicCounterAfterEntityLoading() {
            // Simulate entity loaded from database
            Tag loadedTag = new Tag();
            loadedTag.setUsageCount(42);

            // First atomic operation should initialize counter
            int newValue = loadedTag.incrementUsageCount();

            assertThat(newValue).isEqualTo(43);
            assertThat(loadedTag.getUsageCount()).isEqualTo(43);
        }

        @Test
        @DisplayName("Tag with minimum valid values should pass validation")
        void tagWithMinimumValidValues_ShouldPassValidation() {
            Tag minimalTag = new Tag("AB", "ab");
            
            Set<ConstraintViolation<Tag>> violations = validator.validate(minimalTag);
            
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Tag with maximum valid values should pass validation")
        void tagWithMaximumValidValues_ShouldPassValidation() {
            Tag maximalTag = new Tag(
                "A".repeat(50),
                "a".repeat(100)
            );
            maximalTag.setDescription("A".repeat(500));
            maximalTag.setColorCode("#FFFFFF");
            
            Set<ConstraintViolation<Tag>> violations = validator.validate(maximalTag);
            
            assertThat(violations).isEmpty();
        }
    }
}