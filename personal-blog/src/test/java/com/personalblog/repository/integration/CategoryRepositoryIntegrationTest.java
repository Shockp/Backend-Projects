package com.personalblog.repository.integration;

import com.personalblog.entity.BlogPost;
import com.personalblog.entity.Category;
import com.personalblog.entity.Role;
import com.personalblog.entity.User;
import com.personalblog.repository.BlogPostRepository;
import com.personalblog.repository.CategoryRepository;
import com.personalblog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for CategoryRepository using TestContainers with PostgreSQL.
 * 
 * <p>
 * These tests verify category hierarchy management, database operations,
 * and concurrent access scenarios with actual PostgreSQL database.
 * </p>
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @since 1.0.0
 */
@DisplayName("CategoryRepository Integration Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CategoryRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private UserRepository userRepository;

    private User testAuthor;

    @BeforeEach
    void setUp() {
        // Clean up any existing data
        blogPostRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Create test author for blog posts
        testAuthor = new User();
        testAuthor.setUsername("categoryintegrationauthor");
        testAuthor.setEmail("categoryintegration@example.com");
        testAuthor.setPassword("hashedpassword");
        testAuthor.setAccountEnabled(true);
        testAuthor.setEmailVerified(true);
        testAuthor.setRoles(Set.of(Role.AUTHOR));
        testAuthor = userRepository.save(testAuthor);
    }

    private Category createCategory(String name, String slug, Category parent, int displayOrder) {
        Category category = new Category();
        category.setName(name);
        category.setSlug(slug);
        category.setDescription("Description for " + name);
        category.setParent(parent);
        category.setDisplayOrder(displayOrder);
        return category;
    }

    private BlogPost createBlogPost(String title, String slug, Category category) {
        BlogPost post = new BlogPost();
        post.setTitle(title);
        post.setSlug(slug);
        post.setContent("Content for " + title);
        post.setExcerpt("Excerpt for " + title);
        post.setStatus(BlogPost.Status.PUBLISHED);
        post.setAuthor(testAuthor);
        post.setCategory(category);
        post.setPublishedDate(LocalDateTime.now());
        post.setReadingTimeMinutes(5);
        post.setViewCount(0L);
        return post;
    }

    @Nested
    @DisplayName("Hierarchy Management")
    class HierarchyManagement {

        @Test
        @DisplayName("Should create and manage category hierarchy")
        @Transactional
        void shouldCreateAndManageCategoryHierarchy() {
            // Given - Create a three-level hierarchy
            Category rootCategory = createCategory("Technology", "technology", null, 1);
            Category savedRoot = categoryRepository.save(rootCategory);

            Category childCategory = createCategory("Programming", "programming", savedRoot, 1);
            Category savedChild = categoryRepository.save(childCategory);

            Category grandchildCategory = createCategory("Java", "java", savedChild, 1);
            Category savedGrandchild = categoryRepository.save(grandchildCategory);

            // When - Query hierarchy
            List<Category> rootCategories = categoryRepository.findRootCategories();
            List<Category> childCategories = categoryRepository.findByParentId(savedRoot.getId());
            List<Category> grandchildCategories = categoryRepository.findByParentId(savedChild.getId());

            // Then
            assertThat(rootCategories).hasSize(1);
            assertThat(rootCategories.get(0).getName()).isEqualTo("Technology");

            assertThat(childCategories).hasSize(1);
            assertThat(childCategories.get(0).getName()).isEqualTo("Programming");
            assertThat(childCategories.get(0).getParent().getId()).isEqualTo(savedRoot.getId());

            assertThat(grandchildCategories).hasSize(1);
            assertThat(grandchildCategories.get(0).getName()).isEqualTo("Java");
            assertThat(grandchildCategories.get(0).getParent().getId()).isEqualTo(savedChild.getId());
        }

        @Test
        @DisplayName("Should handle category path queries with recursive CTE")
        @Transactional
        void shouldHandleCategoryPathQueriesWithRecursiveCTE() {
            // Given - Create hierarchy
            Category root = categoryRepository.save(createCategory("Root", "root", null, 1));
            Category child = categoryRepository.save(createCategory("Child", "child", root, 1));
            Category grandchild = categoryRepository.save(createCategory("Grandchild", "grandchild", child, 1));

            // When - Get category path
            List<Category> categoryPath = categoryRepository.findCategoryHierarchy(grandchild.getId());

            // Then - Should return path from root to target category
            assertThat(categoryPath).hasSize(3);
            
            // Verify the path contains the expected categories
            assertThat(categoryPath).extracting(Category::getName)
                    .containsExactlyInAnyOrder("Root", "Child", "Grandchild");
        }

        @Test
        @DisplayName("Should validate hierarchy integrity")
        @Transactional
        void shouldValidateHierarchyIntegrity() {
            // Given
            Category parent = categoryRepository.save(createCategory("Parent", "parent", null, 1));
            Category child = categoryRepository.save(createCategory("Child", "child", parent, 1));

            // When - Check if categories have children
            boolean parentHasChildren = categoryRepository.hasChildren(parent.getId());
            boolean childHasChildren = categoryRepository.hasChildren(child.getId());

            // Then
            assertThat(parentHasChildren).isTrue();
            assertThat(childHasChildren).isFalse();
        }

        @Test
        @DisplayName("Should handle deep hierarchy levels")
        @Transactional
        void shouldHandleDeepHierarchyLevels() {
            // Given - Create a 5-level deep hierarchy
            Category level1 = categoryRepository.save(createCategory("Level 1", "level-1", null, 1));
            Category level2 = categoryRepository.save(createCategory("Level 2", "level-2", level1, 1));
            Category level3 = categoryRepository.save(createCategory("Level 3", "level-3", level2, 1));
            Category level4 = categoryRepository.save(createCategory("Level 4", "level-4", level3, 1));
            Category level5 = categoryRepository.save(createCategory("Level 5", "level-5", level4, 1));

            // When - Navigate through hierarchy
            List<Category> level1Children = categoryRepository.findByParentId(level1.getId());
            List<Category> level4Children = categoryRepository.findByParentId(level4.getId());

            // Then
            assertThat(level1Children).hasSize(1);
            assertThat(level1Children.get(0).getName()).isEqualTo("Level 2");

            assertThat(level4Children).hasSize(1);
            assertThat(level4Children.get(0).getName()).isEqualTo("Level 5");

            // Verify path from deepest level
            List<Category> deepPath = categoryRepository.findCategoryPath(level5.getId());
            assertThat(deepPath).hasSize(5);
        }
    }

    @Nested
    @DisplayName("Category with Post Counts")
    class CategoryWithPostCounts {

        @Test
        @DisplayName("Should calculate post counts for categories")
        @Transactional
        void shouldCalculatePostCountsForCategories() {
            // Given
            Category tech = categoryRepository.save(createCategory("Technology", "technology", null, 1));
            Category lifestyle = categoryRepository.save(createCategory("Lifestyle", "lifestyle", null, 2));
            Category empty = categoryRepository.save(createCategory("Empty", "empty", null, 3));

            // Create posts in categories
            blogPostRepository.save(createBlogPost("Tech Post 1", "tech-post-1", tech));
            blogPostRepository.save(createBlogPost("Tech Post 2", "tech-post-2", tech));
            blogPostRepository.save(createBlogPost("Lifestyle Post", "lifestyle-post", lifestyle));

            // When
            List<Object[]> categoriesWithCounts = categoryRepository.findCategoriesWithPostCounts();

            // Then
            assertThat(categoriesWithCounts).hasSize(3);
            
            // Find each category in results and verify counts
            for (Object[] result : categoriesWithCounts) {
                Category category = (Category) result[0];
                Long count = (Long) result[1];
                
                switch (category.getName()) {
                    case "Technology" -> assertThat(count).isEqualTo(2L);
                    case "Lifestyle" -> assertThat(count).isEqualTo(1L);
                    case "Empty" -> assertThat(count).isEqualTo(0L);
                    default -> fail("Unexpected category: " + category.getName());
                }
            }
        }

        @Test
        @DisplayName("Should handle deleted posts in count calculations")
        @Transactional
        void shouldHandleDeletedPostsInCountCalculations() {
            // Given
            Category category = categoryRepository.save(createCategory("Test Category", "test-category", null, 1));
            
            BlogPost activePost = createBlogPost("Active Post", "active-post", category);
            BlogPost deletedPost = createBlogPost("Deleted Post", "deleted-post", category);
            deletedPost.markAsDeleted();
            
            blogPostRepository.save(activePost);
            blogPostRepository.save(deletedPost);

            // When
            List<Object[]> categoriesWithCounts = categoryRepository.findCategoriesWithPostCounts();

            // Then
            assertThat(categoriesWithCounts).hasSize(1);
            Object[] result = categoriesWithCounts.get(0);
            Category resultCategory = (Category) result[0];
            Long count = (Long) result[1];
            
            assertThat(resultCategory.getName()).isEqualTo("Test Category");
            assertThat(count).isEqualTo(1L); // Only active post should be counted
        }
    }

    @Nested
    @DisplayName("SEO and Routing")
    class SeoAndRouting {

        @Test
        @DisplayName("Should handle slug-based queries")
        @Transactional
        void shouldHandleSlugBasedQueries() {
            // Given
            Category category = categoryRepository.save(createCategory("Technology", "technology", null, 1));

            // When
            Optional<Category> foundBySlug = categoryRepository.findBySlugAndDeletedFalse("technology");
            boolean slugExists = categoryRepository.existsBySlugAndDeletedFalse("technology");
            boolean nonExistentSlugExists = categoryRepository.existsBySlugAndDeletedFalse("non-existent");

            // Then
            assertThat(foundBySlug).isPresent();
            assertThat(foundBySlug.get().getName()).isEqualTo("Technology");
            assertThat(slugExists).isTrue();
            assertThat(nonExistentSlugExists).isFalse();
        }

        @Test
        @DisplayName("Should handle slug uniqueness validation")
        @Transactional
        void shouldHandleSlugUniquenessValidation() {
            // Given
            Category category1 = categoryRepository.save(createCategory("Technology", "technology", null, 1));
            Category category2 = createCategory("Different Tech", "technology", null, 2); // Same slug

            // When & Then - Should fail due to unique constraint
            assertThatThrownBy(() -> {
                categoryRepository.save(category2);
                categoryRepository.flush();
            }).isInstanceOf(Exception.class);

            // Verify original category still exists
            Optional<Category> existing = categoryRepository.findBySlugAndDeletedFalse("technology");
            assertThat(existing).isPresent();
            assertThat(existing.get().getName()).isEqualTo("Technology");
        }
    }

    @Nested
    @DisplayName("Concurrent Operations")
    class ConcurrentOperations {

        @Test
        @DisplayName("Should handle concurrent category creation")
        void shouldHandleConcurrentCategoryCreation() throws InterruptedException {
            // Given
            ExecutorService executor = Executors.newFixedThreadPool(5);
            int numberOfCategories = 20;

            // When - Concurrent category creation
            CompletableFuture<Category>[] futures = new CompletableFuture[numberOfCategories];
            for (int i = 0; i < numberOfCategories; i++) {
                final int categoryIndex = i;
                futures[i] = CompletableFuture.supplyAsync(() -> {
                    Category category = createCategory("Concurrent Category " + categoryIndex,
                            "concurrent-category-" + categoryIndex, null, categoryIndex);
                    return categoryRepository.save(category);
                }, executor);
            }

            CompletableFuture.allOf(futures).join();
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);

            // Then
            List<Category> allCategories = categoryRepository.findAll();
            assertThat(allCategories).hasSize(numberOfCategories);

            // Verify all slugs are unique
            Set<String> slugs = allCategories.stream()
                    .map(Category::getSlug)
                    .collect(java.util.stream.Collectors.toSet());
            assertThat(slugs).hasSize(numberOfCategories);
        }

        @Test
        @DisplayName("Should handle concurrent hierarchy modifications")
        void shouldHandleConcurrentHierarchyModifications() throws InterruptedException {
            // Given
            Category rootCategory = categoryRepository.save(createCategory("Root", "root", null, 1));
            ExecutorService executor = Executors.newFixedThreadPool(3);
            int numberOfChildren = 10;

            // When - Concurrent child category creation
            CompletableFuture<Category>[] futures = new CompletableFuture[numberOfChildren];
            for (int i = 0; i < numberOfChildren; i++) {
                final int childIndex = i;
                futures[i] = CompletableFuture.supplyAsync(() -> {
                    Category child = createCategory("Child " + childIndex,
                            "child-" + childIndex, rootCategory, childIndex);
                    return categoryRepository.save(child);
                }, executor);
            }

            CompletableFuture.allOf(futures).join();
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);

            // Then
            List<Category> children = categoryRepository.findByParentId(rootCategory.getId());
            assertThat(children).hasSize(numberOfChildren);

            boolean rootHasChildren = categoryRepository.hasChildren(rootCategory.getId());
            assertThat(rootHasChildren).isTrue();
        }
    }

    @Nested
    @DisplayName("Soft Delete Operations")
    class SoftDeleteOperations {

        @Test
        @DisplayName("Should handle soft delete with hierarchy")
        @Transactional
        void shouldHandleSoftDeleteWithHierarchy() {
            // Given
            Category parent = categoryRepository.save(createCategory("Parent", "parent", null, 1));
            Category child = categoryRepository.save(createCategory("Child", "child", parent, 1));

            // When - Soft delete parent
            parent.markAsDeleted();
            categoryRepository.save(parent);

            // Then
            List<Category> rootCategories = categoryRepository.findRootCategories();
            assertThat(rootCategories).isEmpty(); // Parent should not appear

            // Child should still exist but be orphaned in queries
            Optional<Category> childById = categoryRepository.findById(child.getId());
            assertThat(childById).isPresent();
            assertThat(childById.get().getParent().getId()).isEqualTo(parent.getId()); // Relationship preserved

            // But parent queries should not find the deleted parent
            List<Category> childrenOfDeletedParent = categoryRepository.findByParentId(parent.getId());
            assertThat(childrenOfDeletedParent).hasSize(1); // Child is not deleted
        }

        @Test
        @DisplayName("Should handle category deletion with associated posts")
        @Transactional
        void shouldHandleCategoryDeletionWithAssociatedPosts() {
            // Given
            Category category = categoryRepository.save(createCategory("Category with Posts", "category-with-posts", null, 1));
            BlogPost post = createBlogPost("Post in Category", "post-in-category", category);
            blogPostRepository.save(post);

            // When - Soft delete category
            category.markAsDeleted();
            categoryRepository.save(category);

            // Then
            Optional<Category> deletedCategory = categoryRepository.findBySlugAndDeletedFalse("category-with-posts");
            assertThat(deletedCategory).isEmpty();

            // Post should still exist and reference the category
            Optional<BlogPost> existingPost = blogPostRepository.findBySlugAndDeletedFalse("post-in-category");
            assertThat(existingPost).isPresent();
            assertThat(existingPost.get().getCategory().getId()).isEqualTo(category.getId());

            // Category count queries should not include deleted categories
            List<Object[]> categoriesWithCounts = categoryRepository.findCategoriesWithPostCounts();
            assertThat(categoriesWithCounts).isEmpty(); // No active categories
        }
    }

    @Nested
    @DisplayName("Performance and Optimization")
    class PerformanceAndOptimization {

        @Test
        @DisplayName("Should efficiently handle large category hierarchies")
        @Transactional
        void shouldEfficientlyHandleLargeCategoryHierarchies() {
            // Given - Create a wide hierarchy (many siblings)
            Category root = categoryRepository.save(createCategory("Root", "root", null, 1));
            
            int numberOfSiblings = 50;
            for (int i = 0; i < numberOfSiblings; i++) {
                Category sibling = createCategory("Sibling " + i, "sibling-" + i, root, i);
                categoryRepository.save(sibling);
            }

            // When - Query all children
            List<Category> children = categoryRepository.findByParentId(root.getId());

            // Then
            assertThat(children).hasSize(numberOfSiblings);
            
            // Verify ordering
            for (int i = 0; i < numberOfSiblings; i++) {
                assertThat(children.get(i).getDisplayOrder()).isEqualTo(i);
                assertThat(children.get(i).getName()).isEqualTo("Sibling " + i);
            }
        }

        @Test
        @DisplayName("Should optimize recursive queries for deep hierarchies")
        @Transactional
        void shouldOptimizeRecursiveQueriesForDeepHierarchies() {
            // Given - Create a deep hierarchy
            Category current = categoryRepository.save(createCategory("Level 0", "level-0", null, 1));
            
            int depth = 10;
            for (int i = 1; i <= depth; i++) {
                Category next = createCategory("Level " + i, "level-" + i, current, 1);
                current = categoryRepository.save(next);
            }

            // When - Get path for deepest category
            long startTime = System.currentTimeMillis();
            List<Category> categoryPath = categoryRepository.findCategoryHierarchy(current.getId());
            long endTime = System.currentTimeMillis();

            // Then
            assertThat(categoryPath).hasSize(depth + 1); // 0 to depth inclusive
            assertThat(endTime - startTime).isLessThan(1000); // Should complete within 1 second
            
            // Verify path contains all expected levels
            assertThat(categoryPath).extracting(Category::getName)
                    .contains("Level 0", "Level " + depth);
        }
    }
}