package com.personalblog.entity;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for the Category entity.
 * 
 * Tests cover:
 * - Entity construction and initialization
 * - Bean validation constraints for all fields
 * - Hierarchical relationships (parent-child)
 * - Utility methods (addChild, removeChild, addBlogPost, removeBlogPost)
 * - Relationships with BlogPost entities
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
@DisplayName("Category Entity Tests")
class CategoryTest {

    private Validator validator;
    private Category category;

    @BeforeEach
    void setUp() {
        // Set locale to English for consistent validation messages
        java.util.Locale.setDefault(java.util.Locale.ENGLISH);
        
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        // Create a valid category for testing
        category = new Category("Technology", "technology");
    }

    @AfterEach
    void tearDown() {
        category = null;
    }

    // ==================== Constructor Tests ====================

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create category with default constructor")
        void shouldCreateCategoryWithDefaultConstructor() {
            // When
            Category defaultCategory = new Category();

            // Then
            assertThat(defaultCategory)
                .isNotNull()
                .satisfies(cat -> {
                    assertThat(cat.getColorCode()).isEqualTo("#ffffff");
                    assertThat(cat.getDisplayOrder()).isZero();
                    assertThat(cat.getChildren()).isNotNull().isEmpty();
                    assertThat(cat.getBlogPosts()).isNotNull().isEmpty();
                });
        }

        @Test
        @DisplayName("Should create category with name and slug constructor")
        void shouldCreateCategoryWithNameAndSlug() {
            // Given
            String name = "Programming";
            String slug = "programming";

            // When
            Category testCategory = new Category(name, slug);

            // Then
            assertThat(testCategory)
                .isNotNull()
                .satisfies(cat -> {
                    assertThat(cat.getName()).isEqualTo(name);
                    assertThat(cat.getSlug()).isEqualTo(slug);
                    assertThat(cat.getColorCode()).isEqualTo("#ffffff");
                    assertThat(cat.getDisplayOrder()).isZero();
                });
        }

        @Test
        @DisplayName("Should create category with name, slug and description constructor")
        void shouldCreateCategoryWithNameSlugAndDescription() {
            // Given
            String name = "Web Development";
            String slug = "web-development";
            String description = "Articles about web development technologies";

            // When
            Category testCategory = new Category(name, slug, description);

            // Then
            assertThat(testCategory)
                .isNotNull()
                .satisfies(cat -> {
                    assertThat(cat.getName()).isEqualTo(name);
                    assertThat(cat.getSlug()).isEqualTo(slug);
                    assertThat(cat.getDescription()).isEqualTo(description);
                });
        }
    }

    // ==================== Validation Tests ====================

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Nested
        @DisplayName("Name Validation")
        class NameValidationTests {

            @Test
            @DisplayName("Should pass validation with valid name")
            void shouldPassValidationWithValidName() {
                // Given
                category.setName("Valid Category Name");

                // When
                Set<ConstraintViolation<Category>> violations = validator.validate(category);

                // Then
                assertThat(violations).isEmpty();
            }

            @ParameterizedTest
            @NullAndEmptySource
            @ValueSource(strings = {" ", "\t", "\n"})
            @DisplayName("Should fail validation with blank name")
            void shouldFailValidationWithBlankName(String invalidName) {
                // Given
                category.setName(invalidName);

                // When
                Set<ConstraintViolation<Category>> violations = validator.validate(category);

                // Then
                assertThat(violations)
                    .hasSize(1)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("must not be blank");
            }

            @Test
            @DisplayName("Should fail validation with name exceeding max length")
            void shouldFailValidationWithNameExceedingMaxLength() {
                // Given
                String longName = "a".repeat(101); // Exceeds 100 character limit
                category.setName(longName);

                // When
                Set<ConstraintViolation<Category>> violations = validator.validate(category);

                // Then
                assertThat(violations)
                    .hasSize(1)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Name must be at most 100 characters long");
            }

            @Test
            @DisplayName("Should pass validation with name at max length")
            void shouldPassValidationWithNameAtMaxLength() {
                // Given
                String maxLengthName = "a".repeat(100);
                category.setName(maxLengthName);

                // When
                Set<ConstraintViolation<Category>> violations = validator.validate(category);

                // Then
                assertThat(violations).isEmpty();
            }
        }

        @Nested
        @DisplayName("Slug Validation")
        class SlugValidationTests {

            @ParameterizedTest
            @ValueSource(strings = {"valid-slug", "technology", "web-development-2024", "a", "a-b-c-d-e-f-g-h-i-j"})
            @DisplayName("Should pass validation with valid slugs")
            void shouldPassValidationWithValidSlugs(String validSlug) {
                // Given
                category.setSlug(validSlug);

                // When
                Set<ConstraintViolation<Category>> violations = validator.validate(category);

                // Then
                assertThat(violations).isEmpty();
            }

            @ParameterizedTest
            @NullAndEmptySource
            @ValueSource(strings = {" ", "\t", "\n"})
            @DisplayName("Should fail validation with blank slug")
            void shouldFailValidationWithBlankSlug(String invalidSlug) {
                // Given
                category.setSlug(invalidSlug);

                // When
                Set<ConstraintViolation<Category>> violations = validator.validate(category);

                // Then
                assertThat(violations)
                    .hasSizeGreaterThanOrEqualTo(1)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("must not be blank");
            }

            @ParameterizedTest
            @ValueSource(strings = {"Invalid Slug", "slug_with_underscore", "UPPERCASE", "slug with spaces", "slug@symbol", "slug.dot"})
            @DisplayName("Should fail validation with invalid slug format")
            void shouldFailValidationWithInvalidSlugFormat(String invalidSlug) {
                // Given
                category.setSlug(invalidSlug);

                // When
                Set<ConstraintViolation<Category>> violations = validator.validate(category);

                // Then
                assertThat(violations)
                    .hasSizeGreaterThanOrEqualTo(1)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Slug must contain only lowercase letters, numbers, and hyphens");
            }

            @Test
            @DisplayName("Should fail validation with slug exceeding max length")
            void shouldFailValidationWithSlugExceedingMaxLength() {
                // Given
                String longSlug = "a".repeat(201); // Exceeds 200 character limit
                category.setSlug(longSlug);

                // When
                Set<ConstraintViolation<Category>> violations = validator.validate(category);

                // Then
                assertThat(violations)
                    .hasSizeGreaterThanOrEqualTo(1)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Slug must be at most 200 characters long");
            }
        }

        @Nested
        @DisplayName("Description Validation")
        class DescriptionValidationTests {

            @Test
            @DisplayName("Should pass validation with valid description")
            void shouldPassValidationWithValidDescription() {
                // Given
                category.setDescription("A comprehensive category for technology articles");

                // When
                Set<ConstraintViolation<Category>> violations = validator.validate(category);

                // Then
                assertThat(violations).isEmpty();
            }

            @Test
            @DisplayName("Should pass validation with null description")
            void shouldPassValidationWithNullDescription() {
                // Given
                category.setDescription(null);

                // When
                Set<ConstraintViolation<Category>> violations = validator.validate(category);

                // Then
                assertThat(violations).isEmpty();
            }

            @Test
            @DisplayName("Should fail validation with description exceeding max length")
            void shouldFailValidationWithDescriptionExceedingMaxLength() {
                // Given
                String longDescription = "a".repeat(501); // Exceeds 500 character limit
                category.setDescription(longDescription);

                // When
                Set<ConstraintViolation<Category>> violations = validator.validate(category);

                // Then
                assertThat(violations)
                    .hasSize(1)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Description must be at most 500 characters long");
            }
        }

        @Nested
        @DisplayName("Color Code Validation")
        class ColorCodeValidationTests {

            @ParameterizedTest
            @ValueSource(strings = {"#ffffff", "#000000", "#FF0000", "#00ff00", "#0000FF", "#abc", "#DEF", "#123"})
            @DisplayName("Should pass validation with valid color codes")
            void shouldPassValidationWithValidColorCodes(String validColorCode) {
                // Given
                category.setColorCode(validColorCode);

                // When
                Set<ConstraintViolation<Category>> violations = validator.validate(category);

                // Then
                assertThat(violations).isEmpty();
            }

            @ParameterizedTest
            @ValueSource(strings = {"ffffff", "#gggggg", "#12345", "#1234567", "red", "#", "##ffffff"})
            @DisplayName("Should fail validation with invalid color codes")
            void shouldFailValidationWithInvalidColorCodes(String invalidColorCode) {
                // Given
                category.setColorCode(invalidColorCode);

                // When
                Set<ConstraintViolation<Category>> violations = validator.validate(category);

                // Then
                assertThat(violations)
                    .hasSize(1)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Color code must be a valid hex code (#RRGGBB or #RGB)");
            }
        }

        @Nested
        @DisplayName("Display Order Validation")
        class DisplayOrderValidationTests {

            @ParameterizedTest
            @ValueSource(ints = {0, 1, 10, 100, 1000})
            @DisplayName("Should pass validation with valid display orders")
            void shouldPassValidationWithValidDisplayOrders(int validDisplayOrder) {
                // Given
                category.setDisplayOrder(validDisplayOrder);

                // When
                Set<ConstraintViolation<Category>> violations = validator.validate(category);

                // Then
                assertThat(violations).isEmpty();
            }

            @ParameterizedTest
            @ValueSource(ints = {-1, -10, -100})
            @DisplayName("Should fail validation with negative display orders")
            void shouldFailValidationWithNegativeDisplayOrders(int invalidDisplayOrder) {
                // Given
                category.setDisplayOrder(invalidDisplayOrder);

                // When
                Set<ConstraintViolation<Category>> violations = validator.validate(category);

                // Then
                assertThat(violations)
                    .hasSize(1)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("must be greater than or equal to 0");
            }
        }

        @Nested
        @DisplayName("SEO Metadata Validation")
        class SeoMetadataValidationTests {

            @Test
            @DisplayName("Should pass validation with valid meta title")
            void shouldPassValidationWithValidMetaTitle() {
                // Given
                category.setMetaTitle("Technology Articles - Personal Blog");

                // When
                Set<ConstraintViolation<Category>> violations = validator.validate(category);

                // Then
                assertThat(violations).isEmpty();
            }

            @Test
            @DisplayName("Should fail validation with meta title exceeding max length")
            void shouldFailValidationWithMetaTitleExceedingMaxLength() {
                // Given
                String longMetaTitle = "a".repeat(71); // Exceeds 70 character limit
                category.setMetaTitle(longMetaTitle);

                // When
                Set<ConstraintViolation<Category>> violations = validator.validate(category);

                // Then
                assertThat(violations)
                    .hasSize(1)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Meta title must be at most 70 characters long");
            }

            @Test
            @DisplayName("Should pass validation with valid meta description")
            void shouldPassValidationWithValidMetaDescription() {
                // Given
                category.setMetaDescription("Explore comprehensive technology articles covering programming, web development, and software engineering topics.");

                // When
                Set<ConstraintViolation<Category>> violations = validator.validate(category);

                // Then
                assertThat(violations).isEmpty();
            }

            @Test
            @DisplayName("Should fail validation with meta description exceeding max length")
            void shouldFailValidationWithMetaDescriptionExceedingMaxLength() {
                // Given
                String longMetaDescription = "a".repeat(161); // Exceeds 160 character limit
                category.setMetaDescription(longMetaDescription);

                // When
                Set<ConstraintViolation<Category>> violations = validator.validate(category);

                // Then
                assertThat(violations)
                    .hasSize(1)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Meta description must be at most 160 characters long");
            }

            @Test
            @DisplayName("Should pass validation with valid meta keywords")
            void shouldPassValidationWithValidMetaKeywords() {
                // Given
                category.setMetaKeywords("technology, programming, web development, software engineering, coding");

                // When
                Set<ConstraintViolation<Category>> violations = validator.validate(category);

                // Then
                assertThat(violations).isEmpty();
            }

            @Test
            @DisplayName("Should fail validation with meta keywords exceeding max length")
            void shouldFailValidationWithMetaKeywordsExceedingMaxLength() {
                // Given
                String longMetaKeywords = "a".repeat(256); // Exceeds 255 character limit
                category.setMetaKeywords(longMetaKeywords);

                // When
                Set<ConstraintViolation<Category>> violations = validator.validate(category);

                // Then
                assertThat(violations)
                    .hasSize(1)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Meta keywords must be at most 255 characters long");
            }
        }
    }

    // ==================== Hierarchy Tests ====================

    @Nested
    @DisplayName("Hierarchy Tests")
    class HierarchyTests {

        @Test
        @DisplayName("Should add child category successfully")
        void shouldAddChildCategorySuccessfully() {
            // Given
            Category parentCategory = new Category("Technology", "technology");
            Category childCategory = new Category("Programming", "programming");

            // When
            parentCategory.addChild(childCategory);

            // Then
            assertThat(parentCategory.getChildren())
                .hasSize(1)
                .contains(childCategory);
            assertThat(childCategory.getParent()).isEqualTo(parentCategory);
        }

        @Test
        @DisplayName("Should remove child category successfully")
        void shouldRemoveChildCategorySuccessfully() {
            // Given
            Category parentCategory = new Category("Technology", "technology");
            Category childCategory = new Category("Programming", "programming");
            parentCategory.addChild(childCategory);

            // When
            parentCategory.removeChild(childCategory);

            // Then
            assertThat(parentCategory.getChildren()).isEmpty();
            assertThat(childCategory.getParent()).isNull();
        }

        @Test
        @DisplayName("Should handle multiple children")
        void shouldHandleMultipleChildren() {
            // Given
            Category parentCategory = new Category("Technology", "technology");
            Category child1 = new Category("Programming", "programming");
            Category child2 = new Category("Web Development", "web-development");
            Category child3 = new Category("Mobile Development", "mobile-development");

            // When
            parentCategory.addChild(child1);
            parentCategory.addChild(child2);
            parentCategory.addChild(child3);

            // Then
            assertThat(parentCategory.getChildren())
                .hasSize(3)
                .containsExactlyInAnyOrder(child1, child2, child3);
            assertThat(child1.getParent()).isEqualTo(parentCategory);
            assertThat(child2.getParent()).isEqualTo(parentCategory);
            assertThat(child3.getParent()).isEqualTo(parentCategory);
        }

        @Test
        @DisplayName("Should handle deep hierarchy")
        void shouldHandleDeepHierarchy() {
            // Given
            Category grandParent = new Category("Technology", "technology");
            Category parent = new Category("Programming", "programming");
            Category child = new Category("Java", "java");

            // When
            grandParent.addChild(parent);
            parent.addChild(child);

            // Then
            assertThat(grandParent.getChildren()).containsExactly(parent);
            assertThat(parent.getParent()).isEqualTo(grandParent);
            assertThat(parent.getChildren()).containsExactly(child);
            assertThat(child.getParent()).isEqualTo(parent);
        }

        @Test
        @DisplayName("Should not add null child")
        void shouldNotAddNullChild() {
            // Given
            Category parentCategory = new Category("Technology", "technology");

            // When & Then
            assertThatThrownBy(() -> parentCategory.addChild(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Child category cannot be null");
        }

        @Test
        @DisplayName("Should handle removing non-existent child gracefully")
        void shouldHandleRemovingNonExistentChildGracefully() {
            // Given
            Category parentCategory = new Category("Technology", "technology");
            Category nonChildCategory = new Category("Programming", "programming");

            // When
            parentCategory.removeChild(nonChildCategory);

            // Then
            assertThat(parentCategory.getChildren()).isEmpty();
            assertThat(nonChildCategory.getParent()).isNull();
        }
    }

    // ==================== Blog Post Relationship Tests ====================

    @Nested
    @DisplayName("Blog Post Relationship Tests")
    class BlogPostRelationshipTests {

        private BlogPost createMockBlogPost(String title, String slug) {
            BlogPost blogPost = new BlogPost();
            // Set minimal required fields for testing
            return blogPost;
        }

        @Test
        @DisplayName("Should add blog post successfully")
        void shouldAddBlogPostSuccessfully() {
            // Given
            BlogPost blogPost = createMockBlogPost("Test Post", "test-post");

            // When
            category.addBlogPost(blogPost);

            // Then
            assertThat(category.getBlogPosts())
                .hasSize(1)
                .contains(blogPost);
            assertThat(blogPost.getCategory()).isEqualTo(category);
        }

        @Test
        @DisplayName("Should remove blog post successfully")
        void shouldRemoveBlogPostSuccessfully() {
            // Given
            BlogPost blogPost = createMockBlogPost("Test Post", "test-post");
            category.addBlogPost(blogPost);

            // When
            category.removeBlogPost(blogPost);

            // Then
            assertThat(category.getBlogPosts()).isEmpty();
            assertThat(blogPost.getCategory()).isNull();
        }

        @Test
        @DisplayName("Should handle multiple blog posts")
        void shouldHandleMultipleBlogPosts() {
            // Given
            BlogPost post1 = createMockBlogPost("Post 1", "post-1");
            BlogPost post2 = createMockBlogPost("Post 2", "post-2");
            BlogPost post3 = createMockBlogPost("Post 3", "post-3");

            // When
            category.addBlogPost(post1);
            category.addBlogPost(post2);
            category.addBlogPost(post3);

            // Then
            assertThat(category.getBlogPosts())
                .hasSize(3)
                .containsExactlyInAnyOrder(post1, post2, post3);
        }

        @Test
        @DisplayName("Should handle null blog post gracefully")
        void shouldHandleNullBlogPostGracefully() {
            // When
            category.addBlogPost(null);

            // Then
            assertThat(category.getBlogPosts()).isEmpty();
        }

        @Test
        @DisplayName("Should handle removing non-existent blog post gracefully")
        void shouldHandleRemovingNonExistentBlogPostGracefully() {
            // Given
            BlogPost nonAssociatedPost = createMockBlogPost("Non-associated Post", "non-associated-post");

            // When
            category.removeBlogPost(nonAssociatedPost);

            // Then
            assertThat(category.getBlogPosts()).isEmpty();
            assertThat(nonAssociatedPost.getCategory()).isNull();
        }
    }

    // ==================== Getters and Setters Tests ====================

    @Nested
    @DisplayName("Getters and Setters Tests")
    class GettersAndSettersTests {

        @Test
        @DisplayName("Should get and set name correctly")
        void shouldGetAndSetNameCorrectly() {
            // Given
            String newName = "Updated Category Name";

            // When
            category.setName(newName);

            // Then
            assertThat(category.getName()).isEqualTo(newName);
        }

        @Test
        @DisplayName("Should get and set slug correctly")
        void shouldGetAndSetSlugCorrectly() {
            // Given
            String newSlug = "updated-category-slug";

            // When
            category.setSlug(newSlug);

            // Then
            assertThat(category.getSlug()).isEqualTo(newSlug);
        }

        @Test
        @DisplayName("Should get and set description correctly")
        void shouldGetAndSetDescriptionCorrectly() {
            // Given
            String description = "Updated category description";

            // When
            category.setDescription(description);

            // Then
            assertThat(category.getDescription()).isEqualTo(description);
        }

        @Test
        @DisplayName("Should get and set color code correctly")
        void shouldGetAndSetColorCodeCorrectly() {
            // Given
            String colorCode = "#ff0000";

            // When
            category.setColorCode(colorCode);

            // Then
            assertThat(category.getColorCode()).isEqualTo(colorCode);
        }

        @Test
        @DisplayName("Should get and set display order correctly")
        void shouldGetAndSetDisplayOrderCorrectly() {
            // Given
            Integer displayOrder = 10;

            // When
            category.setDisplayOrder(displayOrder);

            // Then
            assertThat(category.getDisplayOrder()).isEqualTo(displayOrder);
        }

        @Test
        @DisplayName("Should get and set SEO metadata correctly")
        void shouldGetAndSetSeoMetadataCorrectly() {
            // Given
            String metaTitle = "Technology Category";
            String metaDescription = "Articles about technology and programming";
            String metaKeywords = "technology, programming, coding";

            // When
            category.setMetaTitle(metaTitle);
            category.setMetaDescription(metaDescription);
            category.setMetaKeywords(metaKeywords);

            // Then
            assertThat(category.getMetaTitle()).isEqualTo(metaTitle);
            assertThat(category.getMetaDescription()).isEqualTo(metaDescription);
            assertThat(category.getMetaKeywords()).isEqualTo(metaKeywords);
        }

        @Test
        @DisplayName("Should get and set parent correctly")
        void shouldGetAndSetParentCorrectly() {
            // Given
            Category parentCategory = new Category("Parent", "parent");

            // When
            category.setParent(parentCategory);

            // Then
            assertThat(category.getParent()).isEqualTo(parentCategory);
        }

        @Test
        @DisplayName("Should get and set children correctly")
        void shouldGetAndSetChildrenCorrectly() {
            // Given
            Set<Category> children = new HashSet<>();
            children.add(new Category("Child 1", "child-1"));
            children.add(new Category("Child 2", "child-2"));

            // When
            category.setChildren(children);

            // Then
            assertThat(category.getChildren()).isEqualTo(children);
        }

        @Test
        @DisplayName("Should get and set blog posts correctly")
        void shouldGetAndSetBlogPostsCorrectly() {
            // Given
            Set<BlogPost> blogPosts = new HashSet<>();
            blogPosts.add(createMockBlogPost("Post 1", "post-1"));
            blogPosts.add(createMockBlogPost("Post 2", "post-2"));

            // When
            category.setBlogPosts(blogPosts);

            // Then
            assertThat(category.getBlogPosts()).isEqualTo(blogPosts);
        }

        private BlogPost createMockBlogPost(String title, String slug) {
            return new BlogPost();
        }
    }

    // ==================== Object Methods Tests ====================

    @Nested
    @DisplayName("Object Methods Tests")
    class ObjectMethodsTests {

        @Nested
        @DisplayName("equals() Tests")
        class EqualsTests {

            @Test
            @DisplayName("Should return true for same instance")
            void shouldReturnTrueForSameInstance() {
                // When & Then
                assertThat(category.equals(category)).isTrue();
            }

            @Test
            @DisplayName("Should return false for null")
            void shouldReturnFalseForNull() {
                // When & Then
                assertThat(category.equals(null)).isFalse();
            }

            @Test
            @DisplayName("Should return false for different class")
            void shouldReturnFalseForDifferentClass() {
                // When & Then
                assertThat(category.equals("not a category")).isFalse();
            }

            @Test
            @DisplayName("Should return true for categories with same ID")
            void shouldReturnTrueForCategoriesWithSameId() {
                // Given
                Category category1 = new Category("Tech", "tech");
                Category category2 = new Category("Different", "different");
                
                // Simulate same ID (in real scenario, this would be set by JPA)
                // We'll use reflection to set the ID for testing
                try {
                    java.lang.reflect.Field idField = BaseEntity.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(category1, 1L);
                    idField.set(category2, 1L);
                } catch (Exception e) {
                    // If reflection fails, skip this test
                    org.junit.jupiter.api.Assumptions.assumeTrue(false, "Could not set ID via reflection");
                }

                // When & Then
                assertThat(category1.equals(category2)).isTrue();
            }

            @Test
            @DisplayName("Should return false for categories with different IDs")
            void shouldReturnFalseForCategoriesWithDifferentIds() {
                // Given
                Category category1 = new Category("Tech", "tech");
                Category category2 = new Category("Tech", "tech");
                
                // Simulate different IDs
                try {
                    java.lang.reflect.Field idField = BaseEntity.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(category1, 1L);
                    idField.set(category2, 2L);
                } catch (Exception e) {
                    org.junit.jupiter.api.Assumptions.assumeTrue(false, "Could not set ID via reflection");
                }

                // When & Then
                assertThat(category1.equals(category2)).isFalse();
            }
        }

        @Nested
        @DisplayName("hashCode() Tests")
        class HashCodeTests {

            @Test
            @DisplayName("Should be consistent across multiple calls")
            void shouldBeConsistentAcrossMultipleCalls() {
                // When
                int hashCode1 = category.hashCode();
                int hashCode2 = category.hashCode();

                // Then
                assertThat(hashCode1).isEqualTo(hashCode2);
            }

            @Test
            @DisplayName("Should return same hash code for equal objects")
            void shouldReturnSameHashCodeForEqualObjects() {
                // Given
                Category category1 = new Category("Tech", "tech");
                Category category2 = new Category("Different", "different");
                
                // Simulate same ID
                try {
                    java.lang.reflect.Field idField = BaseEntity.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(category1, 1L);
                    idField.set(category2, 1L);
                } catch (Exception e) {
                    org.junit.jupiter.api.Assumptions.assumeTrue(false, "Could not set ID via reflection");
                }

                // When & Then
                assertThat(category1.hashCode()).isEqualTo(category2.hashCode());
            }
        }

        @Nested
        @DisplayName("toString() Tests")
        class ToStringTests {

            @Test
            @DisplayName("Should return correct string representation")
            void shouldReturnCorrectStringRepresentation() {
                // Given
                category.setName("Technology");
                category.setSlug("technology");

                // When
                String result = category.toString();

                // Then
                assertThat(result)
                    .contains("Category{")
                    .contains("name='Technology'")
                    .contains("slug='technology'");
            }

            @Test
            @DisplayName("Should handle null values in toString")
            void shouldHandleNullValuesInToString() {
                // Given
                Category categoryWithNulls = new Category();
                categoryWithNulls.setName(null);
                categoryWithNulls.setSlug(null);

                // When
                String result = categoryWithNulls.toString();

                // Then
                assertThat(result)
                    .contains("Category{")
                    .contains("name='null'")
                    .contains("slug='null'");
            }
        }
    }

    // ==================== Utility Methods Tests ====================

    @Nested
    @DisplayName("Utility Methods Tests")
    class UtilityMethodsTests {

        @Nested
        @DisplayName("hasChildren() Tests")
        class HasChildrenTests {

            @Test
            @DisplayName("Should return false for category with no children")
            void shouldReturnFalseForCategoryWithNoChildren() {
                // When & Then
                assertThat(category.hasChildren()).isFalse();
            }

            @Test
            @DisplayName("Should return true for category with children")
            void shouldReturnTrueForCategoryWithChildren() {
                // Given
                Category child = new Category("Child", "child");
                category.addChild(child);

                // When & Then
                assertThat(category.hasChildren()).isTrue();
            }

            @Test
            @DisplayName("Should return false after removing all children")
            void shouldReturnFalseAfterRemovingAllChildren() {
                // Given
                Category child = new Category("Child", "child");
                category.addChild(child);
                assertThat(category.hasChildren()).isTrue();

                // When
                category.removeChild(child);

                // Then
                assertThat(category.hasChildren()).isFalse();
            }
        }

        @Nested
        @DisplayName("isRoot() Tests")
        class IsRootTests {

            @Test
            @DisplayName("Should return true for category with no parent")
            void shouldReturnTrueForCategoryWithNoParent() {
                // When & Then
                assertThat(category.isRoot()).isTrue();
            }

            @Test
            @DisplayName("Should return false for category with parent")
            void shouldReturnFalseForCategoryWithParent() {
                // Given
                Category parent = new Category("Parent", "parent");
                category.setParent(parent);

                // When & Then
                assertThat(category.isRoot()).isFalse();
            }
        }

        @Nested
        @DisplayName("getDepth() Tests")
        class GetDepthTests {

            @Test
            @DisplayName("Should return 0 for root category")
            void shouldReturnZeroForRootCategory() {
                // When & Then
                assertThat(category.getDepth()).isZero();
            }

            @Test
            @DisplayName("Should return 1 for first level child")
            void shouldReturnOneForFirstLevelChild() {
                // Given
                Category parent = new Category("Parent", "parent");
                category.setParent(parent);

                // When & Then
                assertThat(category.getDepth()).isEqualTo(1);
            }

            @Test
            @DisplayName("Should return correct depth for multi-level hierarchy")
            void shouldReturnCorrectDepthForMultiLevelHierarchy() {
                // Given
                Category grandParent = new Category("GrandParent", "grandparent");
                Category parent = new Category("Parent", "parent");
                Category child = new Category("Child", "child");
                
                parent.setParent(grandParent);
                child.setParent(parent);

                // When & Then
                assertThat(grandParent.getDepth()).isZero();
                assertThat(parent.getDepth()).isEqualTo(1);
                assertThat(child.getDepth()).isEqualTo(2);
            }
        }

        @Nested
        @DisplayName("Circular Reference Prevention Tests")
        class CircularReferencePreventionTests {

            @Test
            @DisplayName("Should prevent adding self as child")
            void shouldPreventAddingSelfAsChild() {
                // When & Then
                assertThatThrownBy(() -> category.addChild(category))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Child category cannot be a descendant of this category");
            }

            @Test
            @DisplayName("Should prevent circular reference in parent-child relationship")
            void shouldPreventCircularReferenceInParentChildRelationship() {
                // Given
                Category parent = new Category("Parent", "parent");
                Category child = new Category("Child", "child");
                
                parent.addChild(child);

                // When & Then - Attempting to make parent a child of its own child
                assertThatThrownBy(() -> child.addChild(parent))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Child category cannot be a descendant of this category");
            }

            @Test
            @DisplayName("Should prevent multi-level circular reference")
            void shouldPreventMultiLevelCircularReference() {
                // Given
                Category grandParent = new Category("GrandParent", "grandparent");
                Category parent = new Category("Parent", "parent");
                Category child = new Category("Child", "child");
                
                grandParent.addChild(parent);
                parent.addChild(child);

                // When & Then - Attempting to make grandparent a child of its descendant
                assertThatThrownBy(() -> child.addChild(grandParent))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Child category cannot be a descendant of this category");
            }

            @Test
            @DisplayName("Should throw exception when adding null child")
            void shouldThrowExceptionWhenAddingNullChild() {
                // When & Then
                assertThatThrownBy(() -> category.addChild(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Child category cannot be null");
            }
        }
    }

    // ==================== Edge Cases and Error Handling ====================

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandlingTests {

        @Test
        @DisplayName("Should handle null collections gracefully")
        void shouldHandleNullCollectionsGracefully() {
            // Given
            Category newCategory = new Category();
            newCategory.setChildren(null);
            newCategory.setBlogPosts(null);

            // When & Then - Methods should handle null collections without throwing exceptions
            assertThat(newCategory.hasChildren()).isFalse();
            
            // Verify collections are null when set to null
            assertThat(newCategory.getChildren()).isNull();
            assertThat(newCategory.getBlogPosts()).isNull();
        }

        @Test
        @DisplayName("Should handle empty collections initialization")
        void shouldHandleEmptyCollectionsInitialization() {
            // Given
            Category newCategory = new Category();

            // Then
            assertThat(newCategory.getChildren())
                .isNotNull()
                .isEmpty();
            assertThat(newCategory.getBlogPosts())
                .isNotNull()
                .isEmpty();
        }

        @Test
        @DisplayName("Should handle default values correctly")
        void shouldHandleDefaultValuesCorrectly() {
            // Given
            Category newCategory = new Category();

            // Then
            assertThat(newCategory.getColorCode()).isEqualTo("#ffffff");
            assertThat(newCategory.getDisplayOrder()).isZero();
        }

        @Test
        @DisplayName("Should handle boundary values for numeric fields")
        void shouldHandleBoundaryValuesForNumericFields() {
            // Given & When
            category.setDisplayOrder(Integer.MAX_VALUE);

            // Then
            assertThat(category.getDisplayOrder()).isEqualTo(Integer.MAX_VALUE);
            
            // Validation should still pass for max integer value
            Set<ConstraintViolation<Category>> violations = validator.validate(category);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should handle special characters in text fields")
        void shouldHandleSpecialCharactersInTextFields() {
            // Given
            String nameWithSpecialChars = "C++ & Java Programming";
            String descriptionWithSpecialChars = "Learn C++, Java, and other programming languages! (2024 edition)";

            // When
            category.setName(nameWithSpecialChars);
            category.setDescription(descriptionWithSpecialChars);

            // Then
            assertThat(category.getName()).isEqualTo(nameWithSpecialChars);
            assertThat(category.getDescription()).isEqualTo(descriptionWithSpecialChars);
            
            // Validation should pass
            Set<ConstraintViolation<Category>> violations = validator.validate(category);
            assertThat(violations).isEmpty();
        }
    }
}