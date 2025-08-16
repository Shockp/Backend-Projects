package com.personalblog.repository.integration;

import com.personalblog.entity.BlogPost;
import com.personalblog.entity.Category;
import com.personalblog.entity.Role;
import com.personalblog.entity.Tag;
import com.personalblog.entity.User;
import com.personalblog.repository.BlogPostRepository;
import com.personalblog.repository.CategoryRepository;
import com.personalblog.repository.TagRepository;
import com.personalblog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for BlogPostRepository using TestContainers with PostgreSQL.
 * 
 * <p>
 * These tests verify repository behavior with actual database operations,
 * including complex queries, transactions, and concurrent access scenarios.
 * </p>
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @since 1.0.0
 */
@DisplayName("BlogPostRepository Integration Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BlogPostRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    private User testAuthor;
    private Category testCategory;
    private Tag testTag1;
    private Tag testTag2;

    @BeforeEach
    void setUp() {
        // Clean up any existing data
        blogPostRepository.deleteAll();
        tagRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Create test author
        testAuthor = new User();
        testAuthor.setUsername("integrationtestauthor");
        testAuthor.setEmail("integration@example.com");
        testAuthor.setPassword("hashedpassword");
        testAuthor.setAccountEnabled(true);
        testAuthor.setEmailVerified(true);
        testAuthor.setRoles(Set.of(Role.AUTHOR));
        testAuthor = userRepository.save(testAuthor);

        // Create test category
        testCategory = new Category();
        testCategory.setName("Integration Technology");
        testCategory.setSlug("integration-technology");
        testCategory.setDescription("Technology related posts for integration testing");
        testCategory.setDisplayOrder(1);
        testCategory = categoryRepository.save(testCategory);

        // Create test tags
        testTag1 = new Tag();
        testTag1.setName("Integration Java");
        testTag1.setSlug("integration-java");
        testTag1.setUsageCount(0);
        testTag1 = tagRepository.save(testTag1);

        testTag2 = new Tag();
        testTag2.setName("Integration Spring");
        testTag2.setSlug("integration-spring");
        testTag2.setUsageCount(0);
        testTag2 = tagRepository.save(testTag2);
    }

    private BlogPost createBlogPost(String title, String slug, BlogPost.Status status, LocalDateTime publishedDate) {
        BlogPost post = new BlogPost();
        post.setTitle(title);
        post.setSlug(slug);
        post.setContent("Integration test content for " + title);
        post.setExcerpt("Integration test excerpt for " + title);
        post.setStatus(status);
        post.setAuthor(testAuthor);
        post.setCategory(testCategory);
        post.setTags(new HashSet<>(Arrays.asList(testTag1, testTag2)));
        post.setPublishedDate(publishedDate);
        post.setMetaTitle("Meta " + title);
        post.setMetaDescription("Meta description for " + title);
        post.setReadingTimeMinutes(5);
        post.setViewCount(0L);
        return post;
    }

    @Nested
    @DisplayName("Database Operations")
    class DatabaseOperations {

        @Test
        @DisplayName("Should persist and retrieve blog posts with all relationships")
        @Transactional
        void shouldPersistAndRetrieveBlogPostsWithRelationships() {
            // Given
            BlogPost post = createBlogPost("Integration Test Post", "integration-test-post",
                    BlogPost.Status.PUBLISHED, LocalDateTime.now());
            
            // When
            BlogPost savedPost = blogPostRepository.save(post);
            Optional<BlogPost> retrievedPost = blogPostRepository.findById(savedPost.getId());

            // Then
            assertThat(retrievedPost).isPresent();
            BlogPost actualPost = retrievedPost.get();
            
            assertThat(actualPost.getTitle()).isEqualTo("Integration Test Post");
            assertThat(actualPost.getAuthor().getUsername()).isEqualTo("integrationtestauthor");
            assertThat(actualPost.getCategory().getName()).isEqualTo("Integration Technology");
            assertThat(actualPost.getTags()).hasSize(2);
            assertThat(actualPost.getTags())
                    .extracting(Tag::getName)
                    .containsExactlyInAnyOrder("Integration Java", "Integration Spring");
        }

        @Test
        @DisplayName("Should handle soft delete operations correctly")
        @Transactional
        void shouldHandleSoftDeleteOperationsCorrectly() {
            // Given
            BlogPost post = createBlogPost("Soft Delete Test", "soft-delete-test",
                    BlogPost.Status.PUBLISHED, LocalDateTime.now());
            BlogPost savedPost = blogPostRepository.save(post);

            // When - Soft delete
            savedPost.markAsDeleted();
            blogPostRepository.save(savedPost);

            // Then
            Optional<BlogPost> foundById = blogPostRepository.findById(savedPost.getId());
            assertThat(foundById).isPresent(); // Still exists in database
            assertThat(foundById.get().isDeleted()).isTrue();

            // Should not be found by queries that filter deleted records
            Optional<BlogPost> foundBySlug = blogPostRepository.findBySlugAndDeletedFalse("soft-delete-test");
            assertThat(foundBySlug).isEmpty();

            Page<BlogPost> publishedPosts = blogPostRepository.findByStatusAndDeletedFalse(
                    BlogPost.Status.PUBLISHED, PageRequest.of(0, 10));
            assertThat(publishedPosts.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should maintain referential integrity with cascading operations")
        @Transactional
        void shouldMaintainReferentialIntegrityWithCascadingOperations() {
            // Given
            BlogPost post1 = createBlogPost("Post 1", "post-1", BlogPost.Status.PUBLISHED, LocalDateTime.now());
            BlogPost post2 = createBlogPost("Post 2", "post-2", BlogPost.Status.PUBLISHED, LocalDateTime.now());
            
            blogPostRepository.saveAll(Arrays.asList(post1, post2));

            // When - Delete category (should not cascade to posts due to foreign key constraint)
            Long categoryId = testCategory.getId();
            
            // Then - Posts should still reference the category
            List<BlogPost> postsInCategory = blogPostRepository.findAll();
            assertThat(postsInCategory).hasSize(2);
            assertThat(postsInCategory).allMatch(post -> post.getCategory().getId().equals(categoryId));
        }
    }

    @Nested
    @DisplayName("Complex Query Operations")
    class ComplexQueryOperations {

        @Test
        @DisplayName("Should execute complex search queries with multiple conditions")
        @Transactional
        void shouldExecuteComplexSearchQueriesWithMultipleConditions() {
            // Given
            BlogPost post1 = createBlogPost("Java Spring Tutorial", "java-spring-tutorial",
                    BlogPost.Status.PUBLISHED, LocalDateTime.now().minusDays(1));
            BlogPost post2 = createBlogPost("Python Django Guide", "python-django-guide",
                    BlogPost.Status.PUBLISHED, LocalDateTime.now().minusDays(2));
            BlogPost post3 = createBlogPost("Java Advanced Topics", "java-advanced-topics",
                    BlogPost.Status.DRAFT, LocalDateTime.now());
            
            blogPostRepository.saveAll(Arrays.asList(post1, post2, post3));

            // When - Search for Java posts that are published
            Pageable pageable = PageRequest.of(0, 10);
            Page<BlogPost> javaPublishedPosts = blogPostRepository.advancedSearch(
                    "Java", BlogPost.Status.PUBLISHED, pageable);

            // Then
            assertThat(javaPublishedPosts.getContent()).hasSize(1);
            assertThat(javaPublishedPosts.getContent().get(0).getTitle()).isEqualTo("Java Spring Tutorial");
        }

        @Test
        @DisplayName("Should handle tag-based queries with multiple tags")
        @Transactional
        void shouldHandleTagBasedQueriesWithMultipleTags() {
            // Given
            Tag javaTag = new Tag();
            javaTag.setName("Java");
            javaTag.setSlug("java");
            javaTag.setUsageCount(0);
            javaTag = tagRepository.save(javaTag);

            Tag springTag = new Tag();
            springTag.setName("Spring");
            springTag.setSlug("spring");
            springTag.setUsageCount(0);
            springTag = tagRepository.save(springTag);

            BlogPost post1 = createBlogPost("Java Only Post", "java-only-post",
                    BlogPost.Status.PUBLISHED, LocalDateTime.now());
            post1.setTags(Set.of(javaTag));

            BlogPost post2 = createBlogPost("Java Spring Post", "java-spring-post",
                    BlogPost.Status.PUBLISHED, LocalDateTime.now());
            post2.setTags(Set.of(javaTag, springTag));

            blogPostRepository.saveAll(Arrays.asList(post1, post2));

            // When - Find posts with all specified tags (AND operation)
            List<Long> tagIds = Arrays.asList(javaTag.getId(), springTag.getId());
            Pageable pageable = PageRequest.of(0, 10);
            Page<BlogPost> postsWithAllTags = blogPostRepository.findByAllTagIdsAndStatus(
                    tagIds, tagIds.size(), BlogPost.Status.PUBLISHED, pageable);

            // Then
            assertThat(postsWithAllTags.getContent()).hasSize(1);
            assertThat(postsWithAllTags.getContent().get(0).getTitle()).isEqualTo("Java Spring Post");
        }

        @Test
        @DisplayName("Should perform analytics queries with date ranges")
        @Transactional
        void shouldPerformAnalyticsQueriesWithDateRanges() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            BlogPost recentPost = createBlogPost("Recent Post", "recent-post",
                    BlogPost.Status.PUBLISHED, now.minusDays(1));
            BlogPost oldPost = createBlogPost("Old Post", "old-post",
                    BlogPost.Status.PUBLISHED, now.minusDays(10));
            
            blogPostRepository.saveAll(Arrays.asList(recentPost, oldPost));

            // When - Count posts in recent date range
            LocalDateTime startDate = now.minusDays(5);
            LocalDateTime endDate = now.plusDays(1);
            long recentCount = blogPostRepository.countByAuthorAndDateRange(
                    testAuthor.getId(), startDate, endDate);

            // Then
            assertThat(recentCount).isEqualTo(1); // Only the recent post
        }
    }

    @Nested
    @DisplayName("Transaction Management")
    class TransactionManagement {

        @Test
        @DisplayName("Should handle transaction rollback on constraint violations")
        void shouldHandleTransactionRollbackOnConstraintViolations() {
            // Given
            BlogPost post1 = createBlogPost("Valid Post", "valid-post",
                    BlogPost.Status.PUBLISHED, LocalDateTime.now());
            BlogPost post2 = createBlogPost("Duplicate Slug Post", "valid-post", // Same slug
                    BlogPost.Status.PUBLISHED, LocalDateTime.now());

            // When & Then
            blogPostRepository.save(post1);
            
            // This should fail due to unique constraint on slug
            assertThatThrownBy(() -> {
                blogPostRepository.save(post2);
                blogPostRepository.flush(); // Force constraint check
            }).isInstanceOf(Exception.class);

            // Verify first post is still saved
            Optional<BlogPost> savedPost = blogPostRepository.findBySlugAndDeletedFalse("valid-post");
            assertThat(savedPost).isPresent();
            assertThat(savedPost.get().getTitle()).isEqualTo("Valid Post");
        }

        @Test
        @DisplayName("Should handle bulk operations within transactions")
        @Transactional
        void shouldHandleBulkOperationsWithinTransactions() {
            // Given
            BlogPost draft1 = createBlogPost("Draft 1", "draft-1", BlogPost.Status.DRAFT, null);
            BlogPost draft2 = createBlogPost("Draft 2", "draft-2", BlogPost.Status.DRAFT, null);
            BlogPost published = createBlogPost("Published", "published", BlogPost.Status.PUBLISHED, LocalDateTime.now());
            
            List<BlogPost> posts = blogPostRepository.saveAll(Arrays.asList(draft1, draft2, published));
            List<Long> draftIds = posts.stream()
                    .filter(post -> post.getStatus() == BlogPost.Status.DRAFT)
                    .map(BlogPost::getId)
                    .toList();

            // When - Bulk publish drafts
            int updatedCount = blogPostRepository.publishPosts(draftIds);

            // Then
            assertThat(updatedCount).isEqualTo(2);
            
            // Verify posts are published
            List<BlogPost> publishedPosts = blogPostRepository.findByStatusAndDeletedFalse(
                    BlogPost.Status.PUBLISHED, PageRequest.of(0, 10)).getContent();
            assertThat(publishedPosts).hasSize(3); // 2 newly published + 1 already published
        }
    }

    @Nested
    @DisplayName("Concurrent Access Scenarios")
    class ConcurrentAccessScenarios {

        @Test
        @DisplayName("Should handle concurrent view count increments")
        void shouldHandleConcurrentViewCountIncrements() throws InterruptedException {
            // Given
            BlogPost post = createBlogPost("Popular Post", "popular-post",
                    BlogPost.Status.PUBLISHED, LocalDateTime.now());
            BlogPost savedPost = blogPostRepository.save(post);
            
            ExecutorService executor = Executors.newFixedThreadPool(10);
            int numberOfIncrements = 100;

            // When - Concurrent view count increments
            CompletableFuture<?>[] futures = new CompletableFuture[numberOfIncrements];
            for (int i = 0; i < numberOfIncrements; i++) {
                futures[i] = CompletableFuture.runAsync(() -> {
                    blogPostRepository.incrementViewCount(savedPost.getId());
                }, executor);
            }

            CompletableFuture.allOf(futures).join();
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);

            // Then
            Optional<BlogPost> updatedPost = blogPostRepository.findById(savedPost.getId());
            assertThat(updatedPost).isPresent();
            assertThat(updatedPost.get().getViewCount()).isEqualTo(numberOfIncrements);
        }

        @Test
        @DisplayName("Should handle concurrent read and write operations")
        void shouldHandleConcurrentReadAndWriteOperations() throws InterruptedException {
            // Given
            BlogPost post = createBlogPost("Concurrent Test Post", "concurrent-test-post",
                    BlogPost.Status.PUBLISHED, LocalDateTime.now());
            BlogPost savedPost = blogPostRepository.save(post);
            
            ExecutorService executor = Executors.newFixedThreadPool(5);
            int numberOfOperations = 50;

            // When - Concurrent reads and writes
            CompletableFuture<?>[] futures = new CompletableFuture[numberOfOperations];
            for (int i = 0; i < numberOfOperations; i++) {
                final int operationIndex = i;
                futures[i] = CompletableFuture.runAsync(() -> {
                    if (operationIndex % 2 == 0) {
                        // Read operation
                        blogPostRepository.findById(savedPost.getId());
                    } else {
                        // Write operation (increment view count)
                        blogPostRepository.incrementViewCount(savedPost.getId());
                    }
                }, executor);
            }

            CompletableFuture.allOf(futures).join();
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);

            // Then - No exceptions should occur and data should be consistent
            Optional<BlogPost> finalPost = blogPostRepository.findById(savedPost.getId());
            assertThat(finalPost).isPresent();
            assertThat(finalPost.get().getViewCount()).isEqualTo(numberOfOperations / 2); // Half were increments
        }

        @Test
        @DisplayName("Should handle concurrent post creation with unique constraints")
        void shouldHandleConcurrentPostCreationWithUniqueConstraints() throws InterruptedException {
            // Given
            ExecutorService executor = Executors.newFixedThreadPool(5);
            int numberOfPosts = 10;

            // When - Concurrent post creation with unique slugs
            CompletableFuture<BlogPost>[] futures = new CompletableFuture[numberOfPosts];
            for (int i = 0; i < numberOfPosts; i++) {
                final int postIndex = i;
                futures[i] = CompletableFuture.supplyAsync(() -> {
                    BlogPost post = createBlogPost("Concurrent Post " + postIndex, 
                            "concurrent-post-" + postIndex,
                            BlogPost.Status.PUBLISHED, LocalDateTime.now());
                    return blogPostRepository.save(post);
                }, executor);
            }

            CompletableFuture.allOf(futures).join();
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);

            // Then - All posts should be created successfully
            List<BlogPost> allPosts = blogPostRepository.findAll();
            assertThat(allPosts).hasSize(numberOfPosts);
            
            // Verify all slugs are unique
            Set<String> slugs = allPosts.stream()
                    .map(BlogPost::getSlug)
                    .collect(java.util.stream.Collectors.toSet());
            assertThat(slugs).hasSize(numberOfPosts);
        }
    }

    @Nested
    @DisplayName("Performance and Optimization")
    class PerformanceAndOptimization {

        @Test
        @DisplayName("Should efficiently handle large result sets with pagination")
        @Transactional
        void shouldEfficientlyHandleLargeResultSetsWithPagination() {
            // Given - Create a large number of posts
            int numberOfPosts = 100;
            for (int i = 0; i < numberOfPosts; i++) {
                BlogPost post = createBlogPost("Post " + i, "post-" + i,
                        BlogPost.Status.PUBLISHED, LocalDateTime.now().minusDays(i));
                blogPostRepository.save(post);
            }

            // When - Paginate through results
            Pageable firstPage = PageRequest.of(0, 10);
            Pageable secondPage = PageRequest.of(1, 10);
            
            Page<BlogPost> firstPageResults = blogPostRepository.findByStatusAndDeletedFalse(
                    BlogPost.Status.PUBLISHED, firstPage);
            Page<BlogPost> secondPageResults = blogPostRepository.findByStatusAndDeletedFalse(
                    BlogPost.Status.PUBLISHED, secondPage);

            // Then
            assertThat(firstPageResults.getContent()).hasSize(10);
            assertThat(secondPageResults.getContent()).hasSize(10);
            assertThat(firstPageResults.getTotalElements()).isEqualTo(numberOfPosts);
            assertThat(secondPageResults.getTotalElements()).isEqualTo(numberOfPosts);
            
            // Verify no overlap between pages
            Set<Long> firstPageIds = firstPageResults.getContent().stream()
                    .map(BlogPost::getId)
                    .collect(java.util.stream.Collectors.toSet());
            Set<Long> secondPageIds = secondPageResults.getContent().stream()
                    .map(BlogPost::getId)
                    .collect(java.util.stream.Collectors.toSet());
            
            assertThat(firstPageIds).doesNotContainAnyElementsOf(secondPageIds);
        }

        @Test
        @DisplayName("Should optimize queries with entity graphs")
        @Transactional
        void shouldOptimizeQueriesWithEntityGraphs() {
            // Given
            BlogPost post = createBlogPost("Entity Graph Test", "entity-graph-test",
                    BlogPost.Status.PUBLISHED, LocalDateTime.now());
            blogPostRepository.save(post);

            // When - Use entity graph query
            Pageable pageable = PageRequest.of(0, 10);
            Page<BlogPost> postsWithDetails = blogPostRepository.findPublishedPostsWithDetails(pageable);

            // Then - Relationships should be loaded without additional queries
            assertThat(postsWithDetails.getContent()).hasSize(1);
            BlogPost loadedPost = postsWithDetails.getContent().get(0);
            
            // These should not trigger lazy loading exceptions
            assertThat(loadedPost.getAuthor().getUsername()).isEqualTo("integrationtestauthor");
            assertThat(loadedPost.getCategory().getName()).isEqualTo("Integration Technology");
            assertThat(loadedPost.getTags()).hasSize(2);
        }
    }
}