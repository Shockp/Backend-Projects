package com.personalblog.repository.integration;

import com.personalblog.entity.*;
import com.personalblog.repository.*;
import com.personalblog.repository.projection.CategoryWithCount;
import com.personalblog.repository.projection.TagCloudItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive integration test suite that tests all repositories together.
 * 
 * <p>
 * This test suite verifies cross-repository interactions, complex transactions,
 * and end-to-end data flow scenarios with actual PostgreSQL database.
 * </p>
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @since 1.0.0
 */
@DisplayName("Repository Integration Test Suite")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RepositoryIntegrationTestSuite extends BaseIntegrationTest {

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private User author;
    private User commenter;
    private Category rootCategory;
    private Category childCategory;
    private Tag javaTag;
    private Tag springTag;

    @BeforeEach
    void setUp() {
        // Clean up all data
        commentRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        blogPostRepository.deleteAll();
        tagRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        author = new User();
        author.setUsername("integrationauthor");
        author.setEmail("author@integration.com");
        author.setPassword("hashedpassword");
        author.setAccountEnabled(true);
        author.setEmailVerified(true);
        author.setRoles(Set.of(Role.AUTHOR));
        author = userRepository.save(author);

        commenter = new User();
        commenter.setUsername("integrationcommenter");
        commenter.setEmail("commenter@integration.com");
        commenter.setPassword("hashedpassword");
        commenter.setAccountEnabled(true);
        commenter.setEmailVerified(true);
        commenter.setRoles(Set.of(Role.USER));
        commenter = userRepository.save(commenter);

        // Create category hierarchy
        rootCategory = new Category();
        rootCategory.setName("Technology");
        rootCategory.setSlug("technology");
        rootCategory.setDescription("Technology articles");
        rootCategory.setDisplayOrder(1);
        rootCategory = categoryRepository.save(rootCategory);

        childCategory = new Category();
        childCategory.setName("Programming");
        childCategory.setSlug("programming");
        childCategory.setDescription("Programming tutorials");
        childCategory.setParent(rootCategory);
        childCategory.setDisplayOrder(1);
        childCategory = categoryRepository.save(childCategory);

        // Create tags
        javaTag = new Tag();
        javaTag.setName("Java");
        javaTag.setSlug("java");
        javaTag.setUsageCount(0);
        javaTag = tagRepository.save(javaTag);

        springTag = new Tag();
        springTag.setName("Spring");
        springTag.setSlug("spring");
        springTag.setUsageCount(0);
        springTag = tagRepository.save(springTag);
    }

    @Nested
    @DisplayName("Cross-Repository Interactions")
    class CrossRepositoryInteractions {

        @Test
        @DisplayName("Should create complete blog post with all relationships")
        @Transactional
        void shouldCreateCompleteBlogPostWithAllRelationships() {
            // Given - Create a blog post with all relationships
            BlogPost post = new BlogPost();
            post.setTitle("Complete Integration Test Post");
            post.setSlug("complete-integration-test-post");
            post.setContent("This post tests all repository integrations");
            post.setExcerpt("Integration test excerpt");
            post.setStatus(BlogPost.Status.PUBLISHED);
            post.setAuthor(author);
            post.setCategory(childCategory);
            post.setTags(new HashSet<>(Arrays.asList(javaTag, springTag)));
            post.setPublishedDate(LocalDateTime.now());
            post.setReadingTimeMinutes(10);
            post.setViewCount(0L);

            // When - Save the post
            BlogPost savedPost = blogPostRepository.save(post);

            // Create comments on the post
            Comment rootComment = new Comment();
            rootComment.setContent("Great article!");
            rootComment.setStatus(CommentStatus.APPROVED);
            rootComment.setBlogPost(savedPost);
            rootComment.setAuthorUser(commenter);
            rootComment.setIpAddress("192.168.1.100");
            rootComment.setUserAgent("Test Browser");
            Comment savedRootComment = commentRepository.save(rootComment);

            Comment reply = new Comment();
            reply.setContent("Thanks for the feedback!");
            reply.setStatus(CommentStatus.APPROVED);
            reply.setBlogPost(savedPost);
            reply.setAuthorUser(author);
            reply.setParentComment(savedRootComment);
            reply.setIpAddress("192.168.1.101");
            reply.setUserAgent("Author Browser");
            commentRepository.save(reply);

            // Create refresh token for author
            RefreshToken token = new RefreshToken();
            token.setTokenValue("integration-test-token");
            token.setUser(author);
            token.setDeviceId("integration-device");
            token.setDeviceName("Integration Test Device");
            token.setIpAddress("192.168.1.102");
            token.setUserAgent("Integration User Agent");
            token.setExpiryDate(LocalDateTime.now().plusDays(7));
            token.setExpired(false);
            token.setRevoked(false);
            token.setFailedAttempts(0);
            token.setLastUsedAt(LocalDateTime.now());
            token.setMarkedForCleanup(false);
            refreshTokenRepository.save(token);

            // Then - Verify all relationships are properly established
            // Verify post relationships
            BlogPost retrievedPost = blogPostRepository.findById(savedPost.getId()).orElseThrow();
            assertThat(retrievedPost.getAuthor().getUsername()).isEqualTo("integrationauthor");
            assertThat(retrievedPost.getCategory().getName()).isEqualTo("Programming");
            assertThat(retrievedPost.getTags()).hasSize(2);
            assertThat(retrievedPost.getTags())
                    .extracting(Tag::getName)
                    .containsExactlyInAnyOrder("Java", "Spring");

            // Verify category hierarchy
            Category retrievedCategory = categoryRepository.findById(childCategory.getId()).orElseThrow();
            assertThat(retrievedCategory.getParent().getName()).isEqualTo("Technology");

            // Verify comments
            List<Comment> postComments = commentRepository.findApprovedByBlogPostId(
                    savedPost.getId(), org.springframework.data.domain.PageRequest.of(0, 10)).getContent();
            assertThat(postComments).hasSize(1);
            assertThat(postComments.get(0).getContent()).isEqualTo("Great article!");

            List<Comment> replies = commentRepository.findRepliesByParentId(savedRootComment.getId());
            assertThat(replies).hasSize(1);
            assertThat(replies.get(0).getContent()).isEqualTo("Thanks for the feedback!");

            // Verify refresh token
            List<RefreshToken> authorTokens = refreshTokenRepository.findValidTokensByUserId(author.getId());
            assertThat(authorTokens).hasSize(1);
            assertThat(authorTokens.get(0).getDeviceId()).isEqualTo("integration-device");
        }

        @Test
        @DisplayName("Should handle cascading operations correctly")
        @Transactional
        void shouldHandleCascadingOperationsCorrectly() {
            // Given - Create blog post with relationships
            BlogPost post = new BlogPost();
            post.setTitle("Cascading Test Post");
            post.setSlug("cascading-test-post");
            post.setContent("Testing cascading operations");
            post.setExcerpt("Cascading test");
            post.setStatus(BlogPost.Status.PUBLISHED);
            post.setAuthor(author);
            post.setCategory(childCategory);
            post.setTags(Set.of(javaTag));
            post.setPublishedDate(LocalDateTime.now());
            post.setReadingTimeMinutes(5);
            post.setViewCount(0L);
            BlogPost savedPost = blogPostRepository.save(post);

            // Create comment
            Comment comment = new Comment();
            comment.setContent("Test comment");
            comment.setStatus(CommentStatus.APPROVED);
            comment.setBlogPost(savedPost);
            comment.setAuthorUser(commenter);
            comment.setIpAddress("192.168.1.100");
            comment.setUserAgent("Test Browser");
            commentRepository.save(comment);

            // When - Soft delete the post
            savedPost.markAsDeleted();
            blogPostRepository.save(savedPost);

            // Then - Verify soft delete behavior
            // Post should not be found by normal queries
            assertThat(blogPostRepository.findBySlugAndDeletedFalse("cascading-test-post")).isEmpty();

            // But should still exist in database
            assertThat(blogPostRepository.findById(savedPost.getId())).isPresent();

            // Comments should still exist and reference the post
            List<Comment> comments = commentRepository.findAll();
            assertThat(comments).hasSize(1);
            assertThat(comments.get(0).getBlogPost().getId()).isEqualTo(savedPost.getId());

            // Category and tags should be unaffected
            assertThat(categoryRepository.findById(childCategory.getId())).isPresent();
            assertThat(tagRepository.findById(javaTag.getId())).isPresent();
        }
    }

    @Nested
    @DisplayName("Complex Transaction Scenarios")
    class ComplexTransactionScenarios {

        @Test
        @DisplayName("Should handle complex multi-repository transaction")
        @Transactional
        void shouldHandleComplexMultiRepositoryTransaction() {
            // Given - Create multiple entities in a single transaction
            BlogPost post1 = createBlogPost("Transaction Post 1", "transaction-post-1");
            BlogPost post2 = createBlogPost("Transaction Post 2", "transaction-post-2");

            // When - Save all entities in transaction
            BlogPost savedPost1 = blogPostRepository.save(post1);
            BlogPost savedPost2 = blogPostRepository.save(post2);

            // Update tag usage counts
            javaTag.setUsageCount(javaTag.getUsageCount() + 2);
            springTag.setUsageCount(springTag.getUsageCount() + 2);
            tagRepository.save(javaTag);
            tagRepository.save(springTag);

            // Create comments for both posts
            Comment comment1 = createComment("Comment on post 1", savedPost1);
            Comment comment2 = createComment("Comment on post 2", savedPost2);
            commentRepository.save(comment1);
            commentRepository.save(comment2);

            // Then - Verify all operations succeeded
            assertThat(blogPostRepository.findAll()).hasSize(2);
            assertThat(commentRepository.findAll()).hasSize(2);
            
            Tag updatedJavaTag = tagRepository.findById(javaTag.getId()).orElseThrow();
            Tag updatedSpringTag = tagRepository.findById(springTag.getId()).orElseThrow();
            assertThat(updatedJavaTag.getUsageCount()).isEqualTo(2);
            assertThat(updatedSpringTag.getUsageCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should handle transaction rollback on constraint violation")
        void shouldHandleTransactionRollbackOnConstraintViolation() {
            // Given - Create a valid post first
            BlogPost validPost = createBlogPost("Valid Post", "valid-post");
            blogPostRepository.save(validPost);

            // When & Then - Try to create post with duplicate slug
            assertThatThrownBy(() -> {
                BlogPost duplicatePost = createBlogPost("Duplicate Post", "valid-post"); // Same slug
                blogPostRepository.save(duplicatePost);
                blogPostRepository.flush(); // Force constraint check
            }).isInstanceOf(Exception.class);

            // Verify original post still exists
            assertThat(blogPostRepository.findBySlugAndDeletedFalse("valid-post")).isPresent();
            assertThat(blogPostRepository.findAll()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Concurrent Access Scenarios")
    class ConcurrentAccessScenarios {

        @Test
        @DisplayName("Should handle concurrent operations across repositories")
        void shouldHandleConcurrentOperationsAcrossRepositories() throws InterruptedException {
            // Given
            BlogPost post = createBlogPost("Concurrent Test Post", "concurrent-test-post");
            BlogPost savedPost = blogPostRepository.save(post);

            ExecutorService executor = Executors.newFixedThreadPool(5);
            int numberOfOperations = 20;

            // When - Concurrent operations across different repositories
            CompletableFuture<?>[] futures = new CompletableFuture[numberOfOperations];
            for (int i = 0; i < numberOfOperations; i++) {
                final int operationIndex = i;
                futures[i] = CompletableFuture.runAsync(() -> {
                    switch (operationIndex % 4) {
                        case 0 -> {
                            // Increment view count
                            blogPostRepository.incrementViewCount(savedPost.getId());
                        }
                        case 1 -> {
                            // Create comment
                            Comment comment = createComment("Concurrent comment " + operationIndex, savedPost);
                            commentRepository.save(comment);
                        }
                        case 2 -> {
                            // Update tag usage
                            Tag tag = tagRepository.findById(javaTag.getId()).orElseThrow();
                            tag.setUsageCount(tag.getUsageCount() + 1);
                            tagRepository.save(tag);
                        }
                        case 3 -> {
                            // Create refresh token
                            RefreshToken token = new RefreshToken();
                            token.setTokenValue("concurrent-token-" + operationIndex);
                            token.setUser(author);
                            token.setDeviceId("device-" + operationIndex);
                            token.setDeviceName("Concurrent Device " + operationIndex);
                            token.setIpAddress("192.168.1." + (100 + operationIndex));
                            token.setUserAgent("Concurrent User Agent");
                            token.setExpiryDate(LocalDateTime.now().plusDays(7));
                            token.setExpired(false);
                            token.setRevoked(false);
                            token.setFailedAttempts(0);
                            token.setLastUsedAt(LocalDateTime.now());
                            token.setMarkedForCleanup(false);
                            refreshTokenRepository.save(token);
                        }
                    }
                }, executor);
            }

            CompletableFuture.allOf(futures).join();
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);

            // Then - Verify operations completed successfully
            BlogPost updatedPost = blogPostRepository.findById(savedPost.getId()).orElseThrow();
            assertThat(updatedPost.getViewCount()).isEqualTo(5); // 5 view count increments

            List<Comment> comments = commentRepository.findAll();
            assertThat(comments).hasSize(5); // 5 comments created

            Tag updatedJavaTag = tagRepository.findById(javaTag.getId()).orElseThrow();
            assertThat(updatedJavaTag.getUsageCount()).isEqualTo(5); // 5 usage increments

            List<RefreshToken> tokens = refreshTokenRepository.findValidTokensByUserId(author.getId());
            assertThat(tokens).hasSize(5); // 5 tokens created
        }
    }

    @Nested
    @DisplayName("Performance and Optimization")
    class PerformanceAndOptimization {

        @Test
        @DisplayName("Should efficiently handle complex queries across repositories")
        @Transactional
        void shouldEfficientlyHandleComplexQueriesAcrossRepositories() {
            // Given - Create a complex data set
            createComplexDataSet();

            // When - Execute complex queries
            long startTime = System.currentTimeMillis();

            // Complex blog post queries
            List<BlogPost> publishedPosts = blogPostRepository.findByStatusAndDeletedFalse(
                    BlogPost.Status.PUBLISHED, 
                    org.springframework.data.domain.PageRequest.of(0, 10)).getContent();

            // Category hierarchy queries
            List<Category> rootCategories = categoryRepository.findByParentIsNullAndDeletedFalseOrderByDisplayOrder();
            List<CategoryWithCount> categoriesWithCounts = categoryRepository.findRootCategoriesWithCounts();

            // Tag statistics
            List<TagCloudItem> tagsWithCounts = tagRepository.findTagsWithPostCounts();

            // Comment queries
            List<Comment> recentComments = commentRepository.findRecentComments(
                    LocalDateTime.now().minusHours(1));

            // Security queries
            List<RefreshToken> activeTokens = refreshTokenRepository.findValidTokensByUserId(author.getId());

            long endTime = System.currentTimeMillis();

            // Then - Verify performance and results
            assertThat(endTime - startTime).isLessThan(2000); // Should complete within 2 seconds

            assertThat(publishedPosts).isNotEmpty();
            assertThat(rootCategories).isNotEmpty();
            assertThat(categoriesWithCounts).isNotEmpty();
            assertThat(tagsWithCounts).isNotEmpty();
            assertThat(recentComments).isNotEmpty();
            assertThat(activeTokens).isNotEmpty();
        }

        private void createComplexDataSet() {
            // Create multiple blog posts
            for (int i = 0; i < 10; i++) {
                BlogPost post = createBlogPost("Performance Test Post " + i, "performance-test-post-" + i);
                BlogPost savedPost = blogPostRepository.save(post);

                // Create comments for each post
                for (int j = 0; j < 3; j++) {
                    Comment comment = createComment("Comment " + j + " on post " + i, savedPost);
                    commentRepository.save(comment);
                }
            }

            // Create additional categories
            for (int i = 0; i < 5; i++) {
                Category category = new Category();
                category.setName("Performance Category " + i);
                category.setSlug("performance-category-" + i);
                category.setDescription("Performance test category " + i);
                category.setDisplayOrder(i + 2);
                categoryRepository.save(category);
            }

            // Create additional tags
            for (int i = 0; i < 10; i++) {
                Tag tag = new Tag();
                tag.setName("Performance Tag " + i);
                tag.setSlug("performance-tag-" + i);
                tag.setUsageCount(i);
                tagRepository.save(tag);
            }

            // Create refresh tokens
            for (int i = 0; i < 5; i++) {
                RefreshToken token = new RefreshToken();
                token.setTokenValue("performance-token-" + i);
                token.setUser(author);
                token.setDeviceId("performance-device-" + i);
                token.setDeviceName("Performance Device " + i);
                token.setIpAddress("192.168.1." + (200 + i));
                token.setUserAgent("Performance User Agent");
                token.setExpiryDate(LocalDateTime.now().plusDays(7));
                token.setExpired(false);
                token.setRevoked(false);
                token.setFailedAttempts(0);
                token.setLastUsedAt(LocalDateTime.now());
                token.setMarkedForCleanup(false);
                refreshTokenRepository.save(token);
            }
        }
    }

    private BlogPost createBlogPost(String title, String slug) {
        BlogPost post = new BlogPost();
        post.setTitle(title);
        post.setSlug(slug);
        post.setContent("Content for " + title);
        post.setExcerpt("Excerpt for " + title);
        post.setStatus(BlogPost.Status.PUBLISHED);
        post.setAuthor(author);
        post.setCategory(childCategory);
        post.setTags(new HashSet<>(Arrays.asList(javaTag, springTag)));
        post.setPublishedDate(LocalDateTime.now());
        post.setReadingTimeMinutes(5);
        post.setViewCount(0L);
        return post;
    }

    private Comment createComment(String content, BlogPost post) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setStatus(CommentStatus.APPROVED);
        comment.setBlogPost(post);
        comment.setAuthorUser(commenter);
        comment.setIpAddress("192.168.1.100");
        comment.setUserAgent("Test Browser");
        return comment;
    }
}