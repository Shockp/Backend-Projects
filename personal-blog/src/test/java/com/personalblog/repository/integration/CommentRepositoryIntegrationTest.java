package com.personalblog.repository.integration;

import com.personalblog.entity.BlogPost;
import com.personalblog.entity.Category;
import com.personalblog.entity.Comment;
import com.personalblog.entity.CommentStatus;
import com.personalblog.entity.Role;
import com.personalblog.entity.User;
import com.personalblog.repository.BlogPostRepository;
import com.personalblog.repository.CategoryRepository;
import com.personalblog.repository.CommentRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for CommentRepository using TestContainers with PostgreSQL.
 * 
 * <p>
 * These tests verify comment threading, moderation workflows, and concurrent
 * access scenarios with actual PostgreSQL database.
 * </p>
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @since 1.0.0
 */
@DisplayName("CommentRepository Integration Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CommentRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private User testAuthor;
    private User testCommenter;
    private Category testCategory;
    private BlogPost testBlogPost;

    @BeforeEach
    void setUp() {
        // Clean up any existing data
        commentRepository.deleteAll();
        blogPostRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Create test author
        testAuthor = new User();
        testAuthor.setUsername("commentintegrationauthor");
        testAuthor.setEmail("commentintegration@example.com");
        testAuthor.setPassword("hashedpassword");
        testAuthor.setAccountEnabled(true);
        testAuthor.setEmailVerified(true);
        testAuthor.setRoles(Set.of(Role.AUTHOR));
        testAuthor = userRepository.save(testAuthor);

        // Create test commenter
        testCommenter = new User();
        testCommenter.setUsername("testcommenter");
        testCommenter.setEmail("commenter@example.com");
        testCommenter.setPassword("hashedpassword");
        testCommenter.setAccountEnabled(true);
        testCommenter.setEmailVerified(true);
        testCommenter.setRoles(Set.of(Role.USER));
        testCommenter = userRepository.save(testCommenter);

        // Create test category
        testCategory = new Category();
        testCategory.setName("Comment Test Category");
        testCategory.setSlug("comment-test-category");
        testCategory.setDescription("Category for comment integration testing");
        testCategory.setDisplayOrder(1);
        testCategory = categoryRepository.save(testCategory);

        // Create test blog post
        testBlogPost = new BlogPost();
        testBlogPost.setTitle("Test Blog Post for Comments");
        testBlogPost.setSlug("test-blog-post-comments");
        testBlogPost.setContent("This is a test blog post for comment integration testing");
        testBlogPost.setExcerpt("Test excerpt");
        testBlogPost.setStatus(BlogPost.Status.PUBLISHED);
        testBlogPost.setAuthor(testAuthor);
        testBlogPost.setCategory(testCategory);
        testBlogPost.setPublishedDate(LocalDateTime.now());
        testBlogPost.setReadingTimeMinutes(5);
        testBlogPost.setViewCount(0L);
        testBlogPost = blogPostRepository.save(testBlogPost);
    }

    private Comment createRegisteredUserComment(String content, CommentStatus status, Comment parent) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setStatus(status);
        comment.setBlogPost(testBlogPost);
        comment.setAuthorUser(testCommenter);
        comment.setParentComment(parent);
        comment.setIpAddress("192.168.1.100");
        comment.setUserAgent("Test User Agent");
        return comment;
    }

    private Comment createGuestComment(String content, String authorName, String authorEmail, CommentStatus status, Comment parent) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setStatus(status);
        comment.setBlogPost(testBlogPost);
        comment.setAuthorName(authorName);
        comment.setAuthorEmail(authorEmail);
        comment.setParentComment(parent);
        comment.setIpAddress("192.168.1.101");
        comment.setUserAgent("Guest User Agent");
        return comment;
    }

    @Nested
    @DisplayName("Comment Threading")
    class CommentThreading {

        @Test
        @DisplayName("Should handle comment threading correctly")
        @Transactional
        void shouldHandleCommentThreadingCorrectly() {
            // Given - Create a comment thread
            Comment rootComment = commentRepository.save(
                    createRegisteredUserComment("Root comment", CommentStatus.APPROVED, null));
            
            Comment reply1 = commentRepository.save(
                    createRegisteredUserComment("Reply 1 to root", CommentStatus.APPROVED, rootComment));
            
            Comment reply2 = commentRepository.save(
                    createRegisteredUserComment("Reply 2 to root", CommentStatus.APPROVED, rootComment));
            
            Comment nestedReply = commentRepository.save(
                    createRegisteredUserComment("Nested reply to reply 1", CommentStatus.APPROVED, reply1));

            // When - Query root comments and replies
            Page<Comment> rootComments = commentRepository.findRootCommentsByPostId(
                    testBlogPost.getId(), PageRequest.of(0, 10));
            
            List<Comment> repliesToRoot = commentRepository.findRepliesByParentId(rootComment.getId());
            List<Comment> repliesToReply1 = commentRepository.findRepliesByParentId(reply1.getId());

            // Then
            assertThat(rootComments.getContent()).hasSize(1);
            assertThat(rootComments.getContent().get(0).getContent()).isEqualTo("Root comment");

            assertThat(repliesToRoot).hasSize(2);
            assertThat(repliesToRoot)
                    .extracting(Comment::getContent)
                    .containsExactlyInAnyOrder("Reply 1 to root", "Reply 2 to root");

            assertThat(repliesToReply1).hasSize(1);
            assertThat(repliesToReply1.get(0).getContent()).isEqualTo("Nested reply to reply 1");
        }

        @Test
        @DisplayName("Should maintain thread integrity with deep nesting")
        @Transactional
        void shouldMaintainThreadIntegrityWithDeepNesting() {
            // Given - Create a deep comment thread
            Comment current = commentRepository.save(
                    createRegisteredUserComment("Level 0 comment", CommentStatus.APPROVED, null));

            int depth = 5;
            for (int i = 1; i <= depth; i++) {
                Comment next = createRegisteredUserComment("Level " + i + " comment", CommentStatus.APPROVED, current);
                current = commentRepository.save(next);
            }

            // When - Navigate through the thread
            Page<Comment> rootComments = commentRepository.findRootCommentsByPostId(
                    testBlogPost.getId(), PageRequest.of(0, 10));

            // Then
            assertThat(rootComments.getContent()).hasSize(1);
            assertThat(rootComments.getContent().get(0).getContent()).isEqualTo("Level 0 comment");

            // Verify the chain
            Comment currentComment = rootComments.getContent().get(0);
            for (int i = 1; i <= depth; i++) {
                List<Comment> replies = commentRepository.findRepliesByParentId(currentComment.getId());
                assertThat(replies).hasSize(1);
                assertThat(replies.get(0).getContent()).isEqualTo("Level " + i + " comment");
                currentComment = replies.get(0);
            }
        }
    }

    @Nested
    @DisplayName("Comment Moderation")
    class CommentModeration {

        @Test
        @DisplayName("Should handle comment moderation workflow")
        @Transactional
        void shouldHandleCommentModerationWorkflow() {
            // Given
            Comment pendingComment = commentRepository.save(
                    createRegisteredUserComment("Pending comment", CommentStatus.PENDING, null));
            Comment approvedComment = commentRepository.save(
                    createRegisteredUserComment("Approved comment", CommentStatus.APPROVED, null));
            Comment rejectedComment = commentRepository.save(
                    createRegisteredUserComment("Rejected comment", CommentStatus.REJECTED, null));
            Comment spamComment = commentRepository.save(
                    createRegisteredUserComment("Spam comment", CommentStatus.SPAM, null));

            // When - Query by status
            Page<Comment> pendingComments = commentRepository.findByStatusAndDeletedFalse(
                    CommentStatus.PENDING, PageRequest.of(0, 10));
            Page<Comment> approvedComments = commentRepository.findByStatusAndDeletedFalse(
                    CommentStatus.APPROVED, PageRequest.of(0, 10));
            List<Comment> pendingForReview = commentRepository.findPendingComments(CommentStatus.PENDING);

            // Then
            assertThat(pendingComments.getContent()).hasSize(1);
            assertThat(pendingComments.getContent().get(0).getContent()).isEqualTo("Pending comment");

            assertThat(approvedComments.getContent()).hasSize(1);
            assertThat(approvedComments.getContent().get(0).getContent()).isEqualTo("Approved comment");

            assertThat(pendingForReview).hasSize(1);
            assertThat(pendingForReview.get(0).getContent()).isEqualTo("Pending comment");
        }

        @Test
        @DisplayName("Should count comments by status")
        @Transactional
        void shouldCountCommentsByStatus() {
            // Given
            commentRepository.save(createRegisteredUserComment("Approved 1", CommentStatus.APPROVED, null));
            commentRepository.save(createRegisteredUserComment("Approved 2", CommentStatus.APPROVED, null));
            commentRepository.save(createRegisteredUserComment("Pending 1", CommentStatus.PENDING, null));
            commentRepository.save(createRegisteredUserComment("Spam 1", CommentStatus.SPAM, null));

            // When
            long approvedCount = commentRepository.countByPostIdAndStatus(testBlogPost.getId(), CommentStatus.APPROVED);
            long pendingCount = commentRepository.countByPostIdAndStatus(testBlogPost.getId(), CommentStatus.PENDING);
            long spamCount = commentRepository.countByPostIdAndStatus(testBlogPost.getId(), CommentStatus.SPAM);

            // Then
            assertThat(approvedCount).isEqualTo(2);
            assertThat(pendingCount).isEqualTo(1);
            assertThat(spamCount).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("User-Based Queries")
    class UserBasedQueries {

        @Test
        @DisplayName("Should find comments by registered user")
        @Transactional
        void shouldFindCommentsByRegisteredUser() {
            // Given
            commentRepository.save(createRegisteredUserComment("User comment 1", CommentStatus.APPROVED, null));
            commentRepository.save(createRegisteredUserComment("User comment 2", CommentStatus.APPROVED, null));
            commentRepository.save(createGuestComment("Guest comment", "Guest", "guest@example.com", CommentStatus.APPROVED, null));

            // When
            Page<Comment> userComments = commentRepository.findByAuthorUserId(
                    testCommenter.getId(), PageRequest.of(0, 10));

            // Then
            assertThat(userComments.getContent()).hasSize(2);
            assertThat(userComments.getContent())
                    .allMatch(comment -> comment.getAuthorUser().getId().equals(testCommenter.getId()));
        }

        @Test
        @DisplayName("Should find comments by guest email")
        @Transactional
        void shouldFindCommentsByGuestEmail() {
            // Given
            String guestEmail = "guest@example.com";
            commentRepository.save(createGuestComment("Guest comment 1", "Guest", guestEmail, CommentStatus.APPROVED, null));
            commentRepository.save(createGuestComment("Guest comment 2", "Guest", guestEmail, CommentStatus.APPROVED, null));
            commentRepository.save(createRegisteredUserComment("User comment", CommentStatus.APPROVED, null));

            // When
            Page<Comment> guestComments = commentRepository.findByAuthorEmail(
                    guestEmail, PageRequest.of(0, 10));

            // Then
            assertThat(guestComments.getContent()).hasSize(2);
            assertThat(guestComments.getContent())
                    .allMatch(comment -> guestEmail.equals(comment.getAuthorEmail()));
        }
    }

    @Nested
    @DisplayName("Security and Monitoring")
    class SecurityAndMonitoring {

        @Test
        @DisplayName("Should track comments by IP address")
        @Transactional
        void shouldTrackCommentsByIpAddress() {
            // Given
            String suspiciousIp = "192.168.1.200";
            commentRepository.save(createGuestComment("Comment 1", "User1", "user1@example.com", CommentStatus.APPROVED, null));
            
            Comment suspiciousComment1 = createGuestComment("Suspicious 1", "Spammer", "spam1@example.com", CommentStatus.SPAM, null);
            suspiciousComment1.setIpAddress(suspiciousIp);
            commentRepository.save(suspiciousComment1);
            
            Comment suspiciousComment2 = createGuestComment("Suspicious 2", "Spammer", "spam2@example.com", CommentStatus.SPAM, null);
            suspiciousComment2.setIpAddress(suspiciousIp);
            commentRepository.save(suspiciousComment2);

            // When
            List<Comment> commentsFromSuspiciousIp = commentRepository.findByIpAddress(suspiciousIp);

            // Then
            assertThat(commentsFromSuspiciousIp).hasSize(2);
            assertThat(commentsFromSuspiciousIp)
                    .allMatch(comment -> suspiciousIp.equals(comment.getIpAddress()));
        }

        @Test
        @DisplayName("Should find recent comments for monitoring")
        @Transactional
        void shouldFindRecentCommentsForMonitoring() {
            // Given
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);
            
            Comment oldComment = createRegisteredUserComment("Old comment", CommentStatus.APPROVED, null);
            oldComment.setCreatedAt(LocalDateTime.now().minusHours(2));
            commentRepository.save(oldComment);
            
            Comment recentComment = createRegisteredUserComment("Recent comment", CommentStatus.APPROVED, null);
            commentRepository.save(recentComment);

            // When
            List<Comment> recentComments = commentRepository.findRecentComments(CommentStatus.APPROVED, cutoffTime);

            // Then
            assertThat(recentComments).hasSize(1);
            assertThat(recentComments.get(0).getContent()).isEqualTo("Recent comment");
        }
    }

    @Nested
    @DisplayName("Concurrent Operations")
    class ConcurrentOperations {

        @Test
        @DisplayName("Should handle concurrent comment creation")
        void shouldHandleConcurrentCommentCreation() throws InterruptedException {
            // Given
            ExecutorService executor = Executors.newFixedThreadPool(5);
            int numberOfComments = 20;

            // When - Concurrent comment creation
            CompletableFuture<Comment>[] futures = new CompletableFuture[numberOfComments];
            for (int i = 0; i < numberOfComments; i++) {
                final int commentIndex = i;
                futures[i] = CompletableFuture.supplyAsync(() -> {
                    Comment comment = createRegisteredUserComment(
                            "Concurrent comment " + commentIndex, 
                            CommentStatus.APPROVED, 
                            null);
                    return commentRepository.save(comment);
                }, executor);
            }

            CompletableFuture.allOf(futures).join();
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);

            // Then
            Page<Comment> allComments = commentRepository.findRootCommentsByPostId(
                    testBlogPost.getId(), PageRequest.of(0, 50));
            assertThat(allComments.getContent()).hasSize(numberOfComments);
        }

        @Test
        @DisplayName("Should handle concurrent moderation operations")
        void shouldHandleConcurrentModerationOperations() throws InterruptedException {
            // Given - Create pending comments
            Comment[] pendingComments = new Comment[10];
            for (int i = 0; i < 10; i++) {
                pendingComments[i] = commentRepository.save(
                        createRegisteredUserComment("Pending comment " + i, CommentStatus.PENDING, null));
            }

            ExecutorService executor = Executors.newFixedThreadPool(3);

            // When - Concurrent moderation (approve/reject)
            CompletableFuture<?>[] futures = new CompletableFuture[10];
            for (int i = 0; i < 10; i++) {
                final int commentIndex = i;
                futures[i] = CompletableFuture.runAsync(() -> {
                    Comment comment = commentRepository.findById(pendingComments[commentIndex].getId()).orElseThrow();
                    comment.setStatus(commentIndex % 2 == 0 ? CommentStatus.APPROVED : CommentStatus.REJECTED);
                    commentRepository.save(comment);
                }, executor);
            }

            CompletableFuture.allOf(futures).join();
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);

            // Then
            long approvedCount = commentRepository.countByPostIdAndStatus(testBlogPost.getId(), CommentStatus.APPROVED);
            long rejectedCount = commentRepository.countByPostIdAndStatus(testBlogPost.getId(), CommentStatus.REJECTED);
            long pendingCount = commentRepository.countByPostIdAndStatus(testBlogPost.getId(), CommentStatus.PENDING);

            assertThat(approvedCount).isEqualTo(5);
            assertThat(rejectedCount).isEqualTo(5);
            assertThat(pendingCount).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Soft Delete Operations")
    class SoftDeleteOperations {

        @Test
        @DisplayName("Should handle comment soft delete")
        @Transactional
        void shouldHandleCommentSoftDelete() {
            // Given
            Comment comment = commentRepository.save(
                    createRegisteredUserComment("Comment to delete", CommentStatus.APPROVED, null));
            Comment reply = commentRepository.save(
                    createRegisteredUserComment("Reply to deleted comment", CommentStatus.APPROVED, comment));

            // When - Soft delete parent comment
            comment.markAsDeleted();
            commentRepository.save(comment);

            // Then
            Page<Comment> rootComments = commentRepository.findRootCommentsByPostId(
                    testBlogPost.getId(), PageRequest.of(0, 10));
            assertThat(rootComments.getContent()).isEmpty(); // Deleted comment not shown

            // Reply should still exist but be orphaned in queries
            Optional<Comment> replyById = commentRepository.findById(reply.getId());
            assertThat(replyById).isPresent();
            assertThat(replyById.get().getParentComment().getId()).isEqualTo(comment.getId());

            // But parent queries should handle deleted parents gracefully
            List<Comment> repliesToDeleted = commentRepository.findRepliesByParentId(comment.getId());
            assertThat(repliesToDeleted).hasSize(1); // Reply is not deleted
        }

        @Test
        @DisplayName("Should exclude deleted comments from counts")
        @Transactional
        void shouldExcludeDeletedCommentsFromCounts() {
            // Given
            Comment activeComment = commentRepository.save(
                    createRegisteredUserComment("Active comment", CommentStatus.APPROVED, null));
            Comment deletedComment = commentRepository.save(
                    createRegisteredUserComment("Deleted comment", CommentStatus.APPROVED, null));
            
            deletedComment.markAsDeleted();
            commentRepository.save(deletedComment);

            // When
            long approvedCount = commentRepository.countByPostIdAndStatus(testBlogPost.getId(), CommentStatus.APPROVED);

            // Then
            assertThat(approvedCount).isEqualTo(1); // Only active comment counted
        }
    }

    @Nested
    @DisplayName("Performance and Optimization")
    class PerformanceAndOptimization {

        @Test
        @DisplayName("Should efficiently handle large comment threads")
        @Transactional
        void shouldEfficientlyHandleLargeCommentThreads() {
            // Given - Create many root comments
            int numberOfRootComments = 50;
            for (int i = 0; i < numberOfRootComments; i++) {
                commentRepository.save(createRegisteredUserComment(
                        "Root comment " + i, CommentStatus.APPROVED, null));
            }

            // When - Paginate through comments
            Pageable firstPage = PageRequest.of(0, 10);
            Pageable secondPage = PageRequest.of(1, 10);

            Page<Comment> firstPageResults = commentRepository.findRootCommentsByPostId(
                    testBlogPost.getId(), firstPage);
            Page<Comment> secondPageResults = commentRepository.findRootCommentsByPostId(
                    testBlogPost.getId(), secondPage);

            // Then
            assertThat(firstPageResults.getContent()).hasSize(10);
            assertThat(secondPageResults.getContent()).hasSize(10);
            assertThat(firstPageResults.getTotalElements()).isEqualTo(numberOfRootComments);

            // Verify no overlap between pages
            Set<Long> firstPageIds = firstPageResults.getContent().stream()
                    .map(Comment::getId)
                    .collect(java.util.stream.Collectors.toSet());
            Set<Long> secondPageIds = secondPageResults.getContent().stream()
                    .map(Comment::getId)
                    .collect(java.util.stream.Collectors.toSet());

            assertThat(firstPageIds).doesNotContainAnyElementsOf(secondPageIds);
        }

        @Test
        @DisplayName("Should optimize queries with proper indexing")
        @Transactional
        void shouldOptimizeQueriesWithProperIndexing() {
            // Given - Create comments with various statuses and timestamps
            LocalDateTime now = LocalDateTime.now();
            for (int i = 0; i < 20; i++) {
                Comment comment = createRegisteredUserComment(
                        "Comment " + i, 
                        i % 3 == 0 ? CommentStatus.APPROVED : CommentStatus.PENDING, 
                        null);
                comment.setCreatedAt(now.minusMinutes(i));
                commentRepository.save(comment);
            }

            // When - Perform various queries that should use indexes
            long startTime = System.currentTimeMillis();
            
            Page<Comment> approvedComments = commentRepository.findByStatusAndDeletedFalse(
                    CommentStatus.APPROVED, PageRequest.of(0, 10));
            List<Comment> recentComments = commentRepository.findRecentComments(
                    CommentStatus.APPROVED, now.minusMinutes(10));
            long approvedCount = commentRepository.countByPostIdAndStatus(
                    testBlogPost.getId(), CommentStatus.APPROVED);
            
            long endTime = System.currentTimeMillis();

            // Then - Queries should complete quickly
            assertThat(endTime - startTime).isLessThan(1000);
            assertThat(approvedComments.getContent()).isNotEmpty();
            assertThat(recentComments).isNotEmpty();
            assertThat(approvedCount).isGreaterThan(0);
        }
    }
}