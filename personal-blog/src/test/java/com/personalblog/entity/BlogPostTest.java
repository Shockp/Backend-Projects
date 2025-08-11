package com.personalblog.entity;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.EnumSource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for the BlogPost entity.
 * 
 * Tests cover:
 * - Entity construction and initialization
 * - Bean validation constraints with validation groups
 * - Utility methods (incrementViewCount, calculateReadingTime)
 * - Relationships with User, Category, Tag, and Comment entities
 * - Status management and publication workflow
 * - SEO metadata handling
 * - Object methods (toString, equals, hashCode)
 * - Edge cases and error conditions
 * 
 * Following BDD patterns with given-when-then structure.
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 */
@DisplayName("BlogPost Entity Tests")
class BlogPostTest {

    private Validator validator;
    private BlogPost blogPost;
    private User mockAuthor;
    private Category mockCategory;

    @BeforeEach
    void setUp() {
        // Set locale to English for consistent validation messages
        java.util.Locale.setDefault(java.util.Locale.ENGLISH);
        
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        // Create mock entities for relationships
        mockAuthor = createMockUser();
        mockCategory = createMockCategory();
        
        // Create a valid blog post for testing
        blogPost = new BlogPost(
            "Test Blog Post Title",
            "test-blog-post-title",
            "This is a comprehensive test content for the blog post that meets the minimum length requirement of 50 characters and contains valid characters only.",
            mockAuthor,
            mockCategory
        );
    }

    @AfterEach
    void tearDown() {
        blogPost = null;
        mockAuthor = null;
        mockCategory = null;
    }

    // ==================== Constructor Tests ====================

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create blog post with default constructor")
        void shouldCreateBlogPostWithDefaultConstructor() {
            // When
            BlogPost defaultPost = new BlogPost();

            // Then
            assertThat(defaultPost)
                .isNotNull()
                .satisfies(post -> {
                    assertThat(post.getStatus()).isEqualTo(BlogPost.Status.DRAFT);
                    assertThat(post.getViewCount()).isZero();
                    assertThat(post.getReadingTimeMinutes()).isZero();
                    assertThat(post.getTags()).isNotNull().isEmpty();
                    assertThat(post.getComments()).isNotNull().isEmpty();
                });
        }

        @Test
        @DisplayName("Should create blog post with basic constructor")
        void shouldCreateBlogPostWithBasicConstructor() {
            // Given
            String title = "New Blog Post";
            String slug = "new-blog-post";
            String content = "This is the content of the new blog post with sufficient length to meet validation requirements.";

            // When
            BlogPost newPost = new BlogPost(title, slug, content, mockAuthor, mockCategory);

            // Then
            assertThat(newPost)
                .isNotNull()
                .satisfies(post -> {
                    assertThat(post.getTitle()).isEqualTo(title);
                    assertThat(post.getSlug()).isEqualTo(slug);
                    assertThat(post.getContent()).isEqualTo(content);
                    assertThat(post.getAuthor()).isEqualTo(mockAuthor);
                    assertThat(post.getCategory()).isEqualTo(mockCategory);
                    assertThat(post.getStatus()).isEqualTo(BlogPost.Status.DRAFT);
                    assertThat(post.getTags()).isNotNull().isEmpty();
                });
        }

        @Test
        @DisplayName("Should create blog post with tags constructor")
        void shouldCreateBlogPostWithTagsConstructor() {
            // Given
            String title = "Tagged Blog Post";
            String slug = "tagged-blog-post";
            String content = "This is the content of the tagged blog post with sufficient length to meet validation requirements.";
            Set<Tag> tags = createMockTags();

            // When
            BlogPost taggedPost = new BlogPost(title, slug, content, mockAuthor, mockCategory, tags);

            // Then
            assertThat(taggedPost)
                .isNotNull()
                .satisfies(post -> {
                    assertThat(post.getTitle()).isEqualTo(title);
                    assertThat(post.getSlug()).isEqualTo(slug);
                    assertThat(post.getContent()).isEqualTo(content);
                    assertThat(post.getAuthor()).isEqualTo(mockAuthor);
                    assertThat(post.getCategory()).isEqualTo(mockCategory);
                    assertThat(post.getTags()).isEqualTo(tags);
                    assertThat(post.getStatus()).isEqualTo(BlogPost.Status.DRAFT);
                });
        }
    }

    // ==================== Validation Tests ====================

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Nested
        @DisplayName("Title Validation")
        class TitleValidationTests {

            @Test
            @DisplayName("Should pass validation with valid title for Create group")
            void shouldPassValidationWithValidTitleForCreateGroup() {
                // Given
                blogPost.setTitle("Valid Blog Post Title");

                // When
                Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Create.class);

                // Then
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("title"))
                    .isEmpty();
            }

            @ParameterizedTest
            @NullAndEmptySource
            @ValueSource(strings = {" ", "  ", "\t", "\n"})
            @DisplayName("Should fail validation with blank title for Create group")
            void shouldFailValidationWithBlankTitleForCreateGroup(String invalidTitle) {
                // Given
                blogPost.setTitle(invalidTitle);

                // When
                Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Create.class);

                // Then
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("title"))
                    .isNotEmpty()
                    .anySatisfy(violation -> 
                        assertThat(violation.getMessage()).containsAnyOf("must not be blank", "no debe estar vacío", "ne doit pas être vide")
                    );
            }

            @ParameterizedTest
            @ValueSource(strings = {"AB", "A", ""})
            @DisplayName("Should fail validation with title too short")
            void shouldFailValidationWithTitleTooShort(String shortTitle) {
                // Given
                blogPost.setTitle(shortTitle);

                // When
                Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Create.class);

                // Then
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("title"))
                    .isNotEmpty();
            }

            @Test
            @DisplayName("Should fail validation with title too long")
            void shouldFailValidationWithTitleTooLong() {
                // Given
                String longTitle = "A".repeat(101);
                blogPost.setTitle(longTitle);

                // When
                Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Create.class);

                // Then
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("title"))
                    .isNotEmpty();
            }

            @ParameterizedTest
            @ValueSource(strings = {"Title with <script>", "Title with @#$%", "Title with [brackets]"})
            @DisplayName("Should fail validation with invalid title characters")
            void shouldFailValidationWithInvalidTitleCharacters(String invalidTitle) {
                // Given
                blogPost.setTitle(invalidTitle);

                // When
                Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Create.class);

                // Then
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("title"))
                    .isNotEmpty();
            }
        }

        @Nested
        @DisplayName("Slug Validation")
        class SlugValidationTests {

            @Test
            @DisplayName("Should pass validation with valid slug")
            void shouldPassValidationWithValidSlug() {
                // Given
                blogPost.setSlug("valid-blog-post-slug-123");

                // When
                Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Create.class);

                // Then
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("slug"))
                    .isEmpty();
            }

            @ParameterizedTest
            @NullAndEmptySource
            @ValueSource(strings = {" ", "  ", "\t", "\n"})
            @DisplayName("Should fail validation with blank slug for Create and Publish groups")
            void shouldFailValidationWithBlankSlugForCreateAndPublishGroups(String invalidSlug) {
                // Given
                blogPost.setSlug(invalidSlug);

                // When
                Set<ConstraintViolation<BlogPost>> createViolations = validator.validate(blogPost, BlogPost.Create.class);
                Set<ConstraintViolation<BlogPost>> publishViolations = validator.validate(blogPost, BlogPost.Publish.class);

                // Then
                assertThat(createViolations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("slug"))
                    .isNotEmpty();
                assertThat(publishViolations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("slug"))
                    .isNotEmpty();
            }

            @ParameterizedTest
            @ValueSource(strings = {"Slug-With-Uppercase", "slug with spaces", "slug_with_underscores", "slug@with#symbols"})
            @DisplayName("Should fail validation with invalid slug format")
            void shouldFailValidationWithInvalidSlugFormat(String invalidSlug) {
                // Given
                blogPost.setSlug(invalidSlug);

                // When
                Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Create.class);

                // Then
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("slug"))
                    .isNotEmpty();
            }

            @Test
            @DisplayName("Should fail validation with slug too long")
            void shouldFailValidationWithSlugTooLong() {
                // Given
                String longSlug = "a".repeat(201);
                blogPost.setSlug(longSlug);

                // When
                Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Create.class);

                // Then
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("slug"))
                    .isNotEmpty();
            }
        }

        @Nested
        @DisplayName("Content Validation")
        class ContentValidationTests {

            @Test
            @DisplayName("Should pass validation with valid content")
            void shouldPassValidationWithValidContent() {
                // Given
                String validContent = "This is a valid blog post content that meets all the validation requirements including minimum length and character restrictions.";
                blogPost.setContent(validContent);

                // When
                Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Create.class);

                // Then
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("content"))
                    .isEmpty();
            }

            @ParameterizedTest
            @NullAndEmptySource
            @ValueSource(strings = {" ", "  ", "\t", "\n"})
            @DisplayName("Should fail validation with blank content for Create group")
            void shouldFailValidationWithBlankContentForCreateGroup(String invalidContent) {
                // Given
                blogPost.setContent(invalidContent);

                // When
                Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Create.class);

                // Then
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("content"))
                    .isNotEmpty();
            }

            @Test
            @DisplayName("Should fail validation with content too short")
            void shouldFailValidationWithContentTooShort() {
                // Given
                String shortContent = "Short content";
                blogPost.setContent(shortContent);

                // When
                Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Create.class);

                // Then
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("content"))
                    .isNotEmpty();
            }

            @Test
            @DisplayName("Should fail validation with content too long")
            void shouldFailValidationWithContentTooLong() {
                // Given
                String longContent = "A".repeat(50001);
                blogPost.setContent(longContent);

                // When
                Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Create.class);

                // Then
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("content"))
                    .isNotEmpty();
            }
        }

        @Nested
        @DisplayName("Status Validation")
        class StatusValidationTests {

            @Test
            @DisplayName("Should pass validation with valid status")
            void shouldPassValidationWithValidStatus() {
                // Given
                blogPost.setStatus(BlogPost.Status.PUBLISHED);

                // When
                Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Create.class);

                // Then
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("status"))
                    .isEmpty();
            }

            @Test
            @DisplayName("Should fail validation with null status")
            void shouldFailValidationWithNullStatus() {
                // Given
                blogPost.setStatus(null);

                // When
                Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Create.class);

                // Then
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("status"))
                    .isNotEmpty();
            }
        }

        @Nested
        @DisplayName("Published Date Validation")
        class PublishedDateValidationTests {

            @Test
            @DisplayName("Should pass validation with published date for Publish group")
            void shouldPassValidationWithPublishedDateForPublishGroup() {
                // Given
                blogPost.setPublishedDate(LocalDateTime.now());

                // When
                Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Publish.class);

                // Then
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("publishedDate"))
                    .isEmpty();
            }

            @Test
            @DisplayName("Should fail validation with null published date for Publish group")
            void shouldFailValidationWithNullPublishedDateForPublishGroup() {
                // Given
                blogPost.setPublishedDate(null);

                // When
                Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Publish.class);

                // Then
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("publishedDate"))
                    .isNotEmpty();
            }
        }

        @Nested
        @DisplayName("SEO Metadata Validation")
        class SEOMetadataValidationTests {

            @Test
            @DisplayName("Should pass validation with valid meta title")
            void shouldPassValidationWithValidMetaTitle() {
                // Given
                blogPost.setMetaTitle("Valid Meta Title for SEO");

                // When
                Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Create.class);

                // Then
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("metaTitle"))
                    .isEmpty();
            }

            @Test
            @DisplayName("Should fail validation with meta title too long")
            void shouldFailValidationWithMetaTitleTooLong() {
                // Given
                String longMetaTitle = "A".repeat(71);
                blogPost.setMetaTitle(longMetaTitle);

                // When
                Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Create.class);

                // Then
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("metaTitle"))
                    .isNotEmpty();
            }

            @Test
            @DisplayName("Should pass validation with valid meta description")
            void shouldPassValidationWithValidMetaDescription() {
                // Given
                blogPost.setMetaDescription("This is a valid meta description for SEO purposes that provides a good summary of the blog post content.");

                // When
                Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Create.class);

                // Then
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("metaDescription"))
                    .isEmpty();
            }

            @Test
            @DisplayName("Should fail validation with meta description too long")
            void shouldFailValidationWithMetaDescriptionTooLong() {
                // Given
                String longMetaDescription = "A".repeat(161);
                blogPost.setMetaDescription(longMetaDescription);

                // When
                Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Create.class);

                // Then
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("metaDescription"))
                    .isNotEmpty();
            }
        }

        @Nested
        @DisplayName("Statistics Validation")
        class StatisticsValidationTests {

            @Test
            @DisplayName("Should pass validation with valid view count")
            void shouldPassValidationWithValidViewCount() {
                // Given
                blogPost.setViewCount(100L);

                // When
                Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Update.class);

                // Then
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("viewCount"))
                    .isEmpty();
            }

            @Test
            @DisplayName("Should fail validation with negative view count")
            void shouldFailValidationWithNegativeViewCount() {
                // Given
                blogPost.setViewCount(-1L);

                // When
                Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Update.class);

                // Then
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("viewCount"))
                    .isNotEmpty();
            }

            @Test
            @DisplayName("Should pass validation with valid reading time")
            void shouldPassValidationWithValidReadingTime() {
                // Given
                blogPost.setReadingTimeMinutes(5);

                // When
                Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Update.class);

                // Then
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("readingTimeMinutes"))
                    .isEmpty();
            }

            @Test
            @DisplayName("Should fail validation with negative reading time")
            void shouldFailValidationWithNegativeReadingTime() {
                // Given
                blogPost.setReadingTimeMinutes(-1);

                // When
                Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Update.class);

                // Then
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("readingTimeMinutes"))
                    .isNotEmpty();
            }
        }

        @Nested
        @DisplayName("Relationship Validation")
        class RelationshipValidationTests {

            @Test
            @DisplayName("Should pass validation with valid author")
            void shouldPassValidationWithValidAuthor() {
                // Given
                blogPost.setAuthor(mockAuthor);

                // When
                Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Create.class);

                // Then
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("author"))
                    .isEmpty();
            }

            @Test
            @DisplayName("Should fail validation with null author")
            void shouldFailValidationWithNullAuthor() {
                // Given
                blogPost.setAuthor(null);

                // When
                Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Create.class);

                // Then
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("author"))
                    .isNotEmpty();
            }

            @Test
            @DisplayName("Should pass validation with valid category")
            void shouldPassValidationWithValidCategory() {
                // Given
                blogPost.setCategory(mockCategory);

                // When
                Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Create.class);

                // Then
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("category"))
                    .isEmpty();
            }

            @Test
            @DisplayName("Should fail validation with null category")
            void shouldFailValidationWithNullCategory() {
                // Given
                blogPost.setCategory(null);

                // When
                Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Create.class);

                // Then
                assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("category"))
                    .isNotEmpty();
            }
        }
    }

    // ==================== Utility Methods Tests ====================

    @Nested
    @DisplayName("Utility Methods Tests")
    class UtilityMethodsTests {

        @Test
        @DisplayName("Should increment view count correctly")
        void shouldIncrementViewCountCorrectly() {
            // Given
            Long initialViewCount = blogPost.getViewCount();

            // When
            blogPost.incrementViewCount();

            // Then
            assertThat(blogPost.getViewCount()).isEqualTo(initialViewCount + 1);
        }

        @Test
        @DisplayName("Should increment view count multiple times")
        void shouldIncrementViewCountMultipleTimes() {
            // Given
            Long initialViewCount = blogPost.getViewCount();
            int incrementCount = 5;

            // When
            for (int i = 0; i < incrementCount; i++) {
                blogPost.incrementViewCount();
            }

            // Then
            assertThat(blogPost.getViewCount()).isEqualTo(initialViewCount + incrementCount);
        }

        @ParameterizedTest
        @CsvSource({
            "100, 200, 1",
            "200, 200, 1",
            "300, 200, 2",
            "500, 200, 3",
            "1000, 200, 5",
            "1500, 250, 6"
        })
        @DisplayName("Should calculate reading time correctly")
        void shouldCalculateReadingTimeCorrectly(int wordCount, int wordsPerMinute, int expectedMinutes) {
            // Given
            String content = generateContentWithWordCount(wordCount);
            blogPost.setContent(content);

            // When
            blogPost.calculateReadingTime(wordsPerMinute);

            // Then
            assertThat(blogPost.getReadingTimeMinutes()).isEqualTo(expectedMinutes);
        }

        @Test
        @DisplayName("Should handle null content for reading time calculation")
        void shouldHandleNullContentForReadingTimeCalculation() {
            // Given
            blogPost.setContent(null);
            int initialReadingTime = blogPost.getReadingTimeMinutes();

            // When
            blogPost.calculateReadingTime(200);

            // Then
            assertThat(blogPost.getReadingTimeMinutes()).isEqualTo(initialReadingTime);
        }

        @Test
        @DisplayName("Should handle zero words per minute for reading time calculation")
        void shouldHandleZeroWordsPerMinuteForReadingTimeCalculation() {
            // Given
            String content = "This is a test content with multiple words for testing.";
            blogPost.setContent(content);
            int initialReadingTime = blogPost.getReadingTimeMinutes();

            // When
            blogPost.calculateReadingTime(0);

            // Then
            assertThat(blogPost.getReadingTimeMinutes()).isEqualTo(initialReadingTime);
        }

        @Test
        @DisplayName("Should handle negative words per minute for reading time calculation")
        void shouldHandleNegativeWordsPerMinuteForReadingTimeCalculation() {
            // Given
            String content = "This is a test content with multiple words for testing.";
            blogPost.setContent(content);
            int initialReadingTime = blogPost.getReadingTimeMinutes();

            // When
            blogPost.calculateReadingTime(-100);

            // Then
            assertThat(blogPost.getReadingTimeMinutes()).isEqualTo(initialReadingTime);
        }
    }

    // ==================== Status Management Tests ====================

    @Nested
    @DisplayName("Status Management Tests")
    class StatusManagementTests {

        @ParameterizedTest
        @EnumSource(BlogPost.Status.class)
        @DisplayName("Should set and get status correctly")
        void shouldSetAndGetStatusCorrectly(BlogPost.Status status) {
            // When
            blogPost.setStatus(status);

            // Then
            assertThat(blogPost.getStatus()).isEqualTo(status);
        }

        @Test
        @DisplayName("Should have DRAFT as default status")
        void shouldHaveDraftAsDefaultStatus() {
            // Given
            BlogPost newPost = new BlogPost();

            // Then
            assertThat(newPost.getStatus()).isEqualTo(BlogPost.Status.DRAFT);
        }

        @Test
        @DisplayName("Should maintain status when setting other fields")
        void shouldMaintainStatusWhenSettingOtherFields() {
            // Given
            blogPost.setStatus(BlogPost.Status.PUBLISHED);

            // When
            blogPost.setTitle("New Title");
            blogPost.setContent("New content that meets the minimum length requirement for validation purposes and contains only valid characters.");

            // Then
            assertThat(blogPost.getStatus()).isEqualTo(BlogPost.Status.PUBLISHED);
        }
    }

    // ==================== Relationship Management Tests ====================

    @Nested
    @DisplayName("Relationship Management Tests")
    class RelationshipManagementTests {

        @Test
        @DisplayName("Should manage tags collection correctly")
        void shouldManageTagsCollectionCorrectly() {
            // Given
            Set<Tag> tags = createMockTags();

            // When
            blogPost.setTags(tags);

            // Then
            assertThat(blogPost.getTags())
                .isNotNull()
                .hasSize(tags.size())
                .containsExactlyInAnyOrderElementsOf(tags);
        }

        @Test
        @DisplayName("Should handle empty tags collection")
        void shouldHandleEmptyTagsCollection() {
            // Given
            Set<Tag> emptyTags = new HashSet<>();

            // When
            blogPost.setTags(emptyTags);

            // Then
            assertThat(blogPost.getTags())
                .isNotNull()
                .isEmpty();
        }

        @Test
        @DisplayName("Should manage comments collection correctly")
        void shouldManageCommentsCollectionCorrectly() {
            // Given
            Set<Comment> comments = createMockComments();

            // When
            blogPost.setComments(comments);

            // Then
            assertThat(blogPost.getComments())
                .isNotNull()
                .hasSize(comments.size())
                .containsExactlyInAnyOrderElementsOf(comments);
        }

        @Test
        @DisplayName("Should handle empty comments collection")
        void shouldHandleEmptyCommentsCollection() {
            // Given
            Set<Comment> emptyComments = new HashSet<>();

            // When
            blogPost.setComments(emptyComments);

            // Then
            assertThat(blogPost.getComments())
                .isNotNull()
                .isEmpty();
        }
    }

    // ==================== Object Methods Tests ====================

    @Nested
    @DisplayName("Object Methods Tests")
    class ObjectMethodsTests {

        @Nested
        @DisplayName("ToString Tests")
        class ToStringTests {

            @Test
            @DisplayName("Should generate correct toString representation")
            void shouldGenerateCorrectToStringRepresentation() {
                // Given
                blogPost.setPublishedDate(LocalDateTime.of(2025, 1, 15, 10, 30));

                // When
                String toString = blogPost.toString();

                // Then
                assertThat(toString)
                    .contains("BlogPost{")
                    .contains("title='Test Blog Post Title'")
                    .contains("slug='test-blog-post-title'")
                    .contains("status=DRAFT")
                    .contains("publishedAt=2025-01-15T10:30");
            }

            @Test
            @DisplayName("Should handle null published date in toString")
            void shouldHandleNullPublishedDateInToString() {
                // Given
                blogPost.setPublishedDate(null);

                // When
                String toString = blogPost.toString();

                // Then
                assertThat(toString)
                    .contains("BlogPost{")
                    .contains("publishedAt=null");
            }
        }

        @Nested
        @DisplayName("Equals Tests")
        class EqualsTests {

            @Test
            @DisplayName("Should return true for blog posts with same id")
            void shouldReturnTrueForBlogPostsWithSameId() throws Exception {
                // Given
                BlogPost post1 = new BlogPost("Post 1", "post-1", "Content for post 1 with sufficient length to meet validation requirements.", mockAuthor, mockCategory);
                BlogPost post2 = new BlogPost("Post 2", "post-2", "Content for post 2 with sufficient length to meet validation requirements.", mockAuthor, mockCategory);
                
                // Set same ID using reflection
                java.lang.reflect.Field idField = BaseEntity.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(post1, 1L);
                idField.set(post2, 1L);

                // When & Then
                assertThat(post1).isEqualTo(post2);
                assertThat(post2).isEqualTo(post1);
            }

            @Test
            @DisplayName("Should return false for blog posts with different ids")
            void shouldReturnFalseForBlogPostsWithDifferentIds() throws Exception {
                // Given
                BlogPost post1 = new BlogPost("Same Post", "same-post", "Same content with sufficient length to meet validation requirements.", mockAuthor, mockCategory);
                BlogPost post2 = new BlogPost("Same Post", "same-post", "Same content with sufficient length to meet validation requirements.", mockAuthor, mockCategory);
                
                // Set different IDs using reflection
                java.lang.reflect.Field idField = BaseEntity.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(post1, 1L);
                idField.set(post2, 2L);

                // When & Then
                assertThat(post1).isNotEqualTo(post2);
            }

            @Test
            @DisplayName("Should return false when comparing with null")
            void shouldReturnFalseWhenComparingWithNull() {
                // When & Then
                assertThat(blogPost).isNotEqualTo(null);
            }

            @Test
            @DisplayName("Should return false when comparing with different class")
            void shouldReturnFalseWhenComparingWithDifferentClass() {
                // Given
                String notABlogPost = "not a blog post";

                // When & Then
                assertThat(blogPost).isNotEqualTo(notABlogPost);
            }

            @Test
            @DisplayName("Should return false when id is null")
            void shouldReturnFalseWhenIdIsNull() {
                // Given
                BlogPost post1 = new BlogPost("Post 1", "post-1", "Content 1 with sufficient length.", mockAuthor, mockCategory);
                BlogPost post2 = new BlogPost("Post 1", "post-1", "Content 1 with sufficient length.", mockAuthor, mockCategory);
                // IDs are null by default

                // When & Then
                assertThat(post1).isNotEqualTo(post2);
            }
        }

        @Nested
        @DisplayName("HashCode Tests")
        class HashCodeTests {

            @Test
            @DisplayName("Should return same hash code for equal objects")
            void shouldReturnSameHashCodeForEqualObjects() throws Exception {
                // Given
                BlogPost post1 = new BlogPost("Post 1", "post-1", "Content 1 with sufficient length.", mockAuthor, mockCategory);
                BlogPost post2 = new BlogPost("Post 2", "post-2", "Content 2 with sufficient length.", mockAuthor, mockCategory);
                
                // Set same ID using reflection
                java.lang.reflect.Field idField = BaseEntity.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(post1, 1L);
                idField.set(post2, 1L);

                // When & Then
                assertThat(post1.hashCode()).isEqualTo(post2.hashCode());
            }

            @Test
            @DisplayName("Should handle null id in hashCode")
            void shouldHandleNullIdInHashCode() {
                // Given - ID is null by default

                // When & Then
                assertThat(blogPost.hashCode()).isEqualTo(31);
            }

            @Test
            @DisplayName("Should return different hash codes for different ids")
            void shouldReturnDifferentHashCodesForDifferentIds() throws Exception {
                // Given
                BlogPost post1 = new BlogPost("Post 1", "post-1", "Content 1 with sufficient length.", mockAuthor, mockCategory);
                BlogPost post2 = new BlogPost("Post 2", "post-2", "Content 2 with sufficient length.", mockAuthor, mockCategory);
                
                // Set different IDs using reflection
                java.lang.reflect.Field idField = BaseEntity.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(post1, 1L);
                idField.set(post2, 2L);

                // When & Then
                assertThat(post1.hashCode()).isNotEqualTo(post2.hashCode());
            }
        }
    }

    // ==================== Edge Cases and Error Conditions ====================

    @Nested
    @DisplayName("Edge Cases and Error Conditions")
    class EdgeCasesAndErrorConditionsTests {

        @Test
        @DisplayName("Should handle maximum valid values")
        void shouldHandleMaximumValidValues() {
            // Given
            String maxTitle = "A".repeat(100);
            String maxSlug = "a".repeat(200);
            String maxContent = "A".repeat(50000);
            String maxExcerpt = "A".repeat(500);
            String maxMetaTitle = "A".repeat(70);
            String maxMetaDescription = "A".repeat(160);
            String maxMetaKeywords = "A".repeat(255);
            String maxFeaturedImageUrl = "https://example.com/" + "A".repeat(480);

            // When
            blogPost.setTitle(maxTitle);
            blogPost.setSlug(maxSlug);
            blogPost.setContent(maxContent);
            blogPost.setExcerpt(maxExcerpt);
            blogPost.setMetaTitle(maxMetaTitle);
            blogPost.setMetaDescription(maxMetaDescription);
            blogPost.setMetaKeywords(maxMetaKeywords);
            blogPost.setFeaturedImageUrl(maxFeaturedImageUrl);

            // Then
            Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Create.class);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should handle minimum valid values")
        void shouldHandleMinimumValidValues() {
            // Given
            String minTitle = "ABC";
            String minSlug = "a";
            String minContent = "A".repeat(50);

            // When
            blogPost.setTitle(minTitle);
            blogPost.setSlug(minSlug);
            blogPost.setContent(minContent);

            // Then
            Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost, BlogPost.Create.class);
            assertThat(violations)
                .filteredOn(v -> Set.of("title", "slug", "content").contains(v.getPropertyPath().toString()))
                .isEmpty();
        }

        @Test
        @DisplayName("Should handle null collections gracefully")
        void shouldHandleNullCollectionsGracefully() {
            // Given
            BlogPost postWithNullCollections = new BlogPost();
            postWithNullCollections.setTitle("Test Post");
            postWithNullCollections.setSlug("test-post");
            postWithNullCollections.setContent("Test content with sufficient length to meet validation requirements.");
            postWithNullCollections.setAuthor(mockAuthor);
            postWithNullCollections.setCategory(mockCategory);

            // When & Then
            assertThat(postWithNullCollections.getTags()).isNotNull().isEmpty();
            assertThat(postWithNullCollections.getComments()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Should handle special characters in content")
        void shouldHandleSpecialCharactersInContent() {
            // Given
            String specialContent = "Content with special chars: àáâãäåæçèéêë ñòóôõöøùúûüý ¡¢£¤¥¦§¨©ª«¬®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ";

            // When
            blogPost.setContent(specialContent);
            Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost);

            // Then
            assertThat(violations).isEmpty();
            assertThat(blogPost.getContent()).isEqualTo(specialContent);
        }

        @Test
        @DisplayName("Should handle emoji in title and content")
        void shouldHandleEmojiInTitleAndContent() {
            // Given
            String emojiTitle = "My Amazing Blog Post 🚀✨🎉";
            String emojiContent = "This is content with emojis 😊👍🔥 and it should work perfectly fine with sufficient length to meet validation requirements.";

            // When
            blogPost.setTitle(emojiTitle);
            blogPost.setContent(emojiContent);
            Set<ConstraintViolation<BlogPost>> violations = validator.validate(blogPost);

            // Then
            assertThat(violations).isEmpty();
            assertThat(blogPost.getTitle()).isEqualTo(emojiTitle);
            assertThat(blogPost.getContent()).isEqualTo(emojiContent);
        }

        @Test
        @DisplayName("Should handle concurrent view count increments")
        void shouldHandleConcurrentViewCountIncrements() {
            // Given
            Long initialViewCount = blogPost.getViewCount();
            int threadCount = 10;
            int incrementsPerThread = 100;

            // When
            // Simulate concurrent increments (in a real scenario, this would need proper synchronization)
            for (int i = 0; i < threadCount * incrementsPerThread; i++) {
                blogPost.incrementViewCount();
            }

            // Then
            assertThat(blogPost.getViewCount()).isEqualTo(initialViewCount + (threadCount * incrementsPerThread));
        }

        @Test
        @DisplayName("Should handle performance with large content")
        void shouldHandlePerformanceWithLargeContent() {
            // Given
            String largeContent = generateContentWithWordCount(5000); // Large but valid content
            long startTime = System.currentTimeMillis();

            // When
            blogPost.setContent(largeContent);
            blogPost.calculateReadingTime(200); // Calculate with 200 words per minute
            long endTime = System.currentTimeMillis();

            // Then
            assertThat(endTime - startTime).isLessThan(100); // Should complete within 100ms
            assertThat(blogPost.getReadingTimeMinutes()).isGreaterThan(0);
            assertThat(blogPost.getContent()).isEqualTo(largeContent);
        }

        @Test
        @DisplayName("Should handle rapid status changes")
        void shouldHandleRapidStatusChanges() {
            // Given
            BlogPost.Status[] statuses = BlogPost.Status.values();

            // When & Then
            for (int i = 0; i < 1000; i++) {
                BlogPost.Status status = statuses[i % statuses.length];
                blogPost.setStatus(status);
                assertThat(blogPost.getStatus()).isEqualTo(status);
            }
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Creates a mock User for testing purposes.
     */
    private User createMockUser() {
        User user = new User();
        user.setUsername("testauthor");
        user.setEmail("author@example.com");
        return user;
    }

    /**
     * Creates a mock Category for testing purposes.
     */
    private Category createMockCategory() {
        Category category = new Category();
        // Since Category is not implemented, we'll use a simple mock
        return category;
    }

    /**
     * Creates a set of mock Tags for testing purposes.
     */
    private Set<Tag> createMockTags() {
        Set<Tag> tags = new HashSet<>();
        Tag tag1 = new Tag();
        Tag tag2 = new Tag();
        tags.add(tag1);
        tags.add(tag2);
        return tags;
    }

    /**
     * Creates a set of mock Comments for testing purposes.
     */
    private Set<Comment> createMockComments() {
        Set<Comment> comments = new HashSet<>();
        Comment comment1 = new Comment();
        Comment comment2 = new Comment();
        comments.add(comment1);
        comments.add(comment2);
        return comments;
    }

    /**
     * Generates content with a specific word count for testing reading time calculation.
     */
    private String generateContentWithWordCount(int wordCount) {
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < wordCount; i++) {
            content.append("word");
            if (i < wordCount - 1) {
                content.append(" ");
            }
        }
        return content.toString();
    }
}