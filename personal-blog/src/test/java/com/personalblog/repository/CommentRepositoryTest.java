package com.personalblog.repository;

import com.personalblog.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for CommentRepository.
 * 
 * Tests cover:
 * - Basic CRUD operations with soft delete support
 * - Comment status management and moderation workflow
 * - Hierarchical comment structure (replies)
 * - Blog post associations and filtering
 * - Author-based queries (registered users and guests)
 * - Security tracking (IP address, user agent)
 * - Comment statistics and analytics
 * - Spam detection and filtering
 * - Performance optimization with proper indexing
 * - Security validation and input sanitization
 * - Edge cases and error handling
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Comment Repository Tests")
class CommentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommentRepository commentRepository;

    private User testUser;
    private User anotherUser;
    private Category testCategory;
    private BlogPost testPost;
    private Comment parentComment;
    private Comment childComment;
    private Comment guestComment;

    @BeforeEach
    void setUp() {
        // Create test users
        testUser = createUser("testuser", "test@example.com");
        anotherUser = createUser("anotheruser", "another@example.com");

        // Create test category
        testCategory = new Category();
        testCategory.setName("Technology");
        testCategory.setSlug("technology");
        testCategory.setDescription("Tech-related posts");
        testCategory.setDisplayOrder(1);
        entityManager.persistAndFlush(testCategory);

        // Create test blog post
        testPost = new BlogPost();
        testPost.setTitle("Test Blog Post");
        testPost.setSlug("test-blog-post");
        testPost.setContent("This is a test blog post content");
        testPost.setExcerpt("Test excerpt");
        testPost.setStatus(BlogPost.Status.PUBLISHED);
        testPost.setAuthor(testUser);
        testPost.setCategory(testCategory);
        testPost.setPublishedDate(LocalDateTime.now());
        entityManager.persistAndFlush(testPost);

        // Create test comments
        parentComment = createUserComment("This is a parent comment", testUser, testPost, CommentStatus.APPROVED);
        childComment = createUserComment("This is a child comment", anotherUser, testPost, CommentStatus.APPROVED);
        childComment.setParentComment(parentComment);
        
        guestComment = createGuestComment("This is a guest comment", "Guest User", "guest@example.com", testPost, CommentStatus.PENDING);

        entityManager.persistAndFlush(parentComment);
        entityManager.persistAndFlush(childComment);
        entityManager.persistAndFlush(guestComment);
    }

    // ==================== Helper Methods ====================

    private User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("password123");
        user.setEmailVerified(true);
        user.setAccountEnabled(true);
        user.setAccountLocked(false);
        user.setRoles(Set.of(Role.USER));
        return entityManager.persistAndFlush(user);
    }

    private Comment createUserComment(String content, User author, BlogPost post, CommentStatus status) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setAuthorUser(author);
        comment.setBlogPost(post);
        comment.setStatus(status);
        comment.setIpAddress("192.168.1.1");
        comment.setUserAgent("Mozilla/5.0 Test Browser");
        return comment;
    }

    private Comment createGuestComment(String content, String authorName, String authorEmail, BlogPost post, CommentStatus status) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setAuthorName(authorName);
        comment.setAuthorEmail(authorEmail);
        comment.setBlogPost(post);
        comment.setStatus(status);
        comment.setIpAddress("192.168.1.2");
        comment.setUserAgent("Mozilla/5.0 Guest Browser");
        return comment;
    }

    // ==================== Basic CRUD Tests ====================

    @Test
    @DisplayName("Should find comments by blog post ID")
    void shouldFindCommentsByBlogPostId() {
        // When
        List<Comment> comments = commentRepository.findByBlogPostId(testPost.getId());

        // Then
        assertThat(comments).hasSize(3);
        assertThat(comments).extracting(Comment::getContent)
                .containsExactlyInAnyOrder(
                    "This is a parent comment",
                    "This is a child comment", 
                    "This is a guest comment"
                );
    }

    @Test
    @DisplayName("Should find comments by blog post ID with pagination")
    void shouldFindCommentsByBlogPostIdWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 2, Sort.by("createdAt").ascending());

        // When
        Page<Comment> comments = commentRepository.findByBlogPostId(testPost.getId(), pageable);

        // Then
        assertThat(comments.getContent()).hasSize(2);
        assertThat(comments.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should find approved comments by blog post ID")
    void shouldFindApprovedCommentsByBlogPostId() {
        // When
        List<Comment> approvedComments = commentRepository.findApprovedByBlogPostId(testPost.getId());

        // Then
        assertThat(approvedComments).hasSize(2); // Parent and child comments are approved
        assertThat(approvedComments).allMatch(comment -> comment.getStatus() == CommentStatus.APPROVED);
    }

    @Test
    @DisplayName("Should find approved comments by blog post ID with pagination")
    void shouldFindApprovedCommentsByBlogPostIdWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").ascending());

        // When
        Page<Comment> approvedComments = commentRepository.findApprovedByBlogPostId(testPost.getId(), pageable);

        // Then
        assertThat(approvedComments.getContent()).hasSize(2);
        assertThat(approvedComments.getContent()).allMatch(comment -> comment.getStatus() == CommentStatus.APPROVED);
    }

    // ==================== Status-Based Tests ====================

    @Test
    @DisplayName("Should find comments by status")
    void shouldFindCommentsByStatus() {
        // When
        List<Comment> pendingComments = commentRepository.findByStatus(CommentStatus.PENDING);
        List<Comment> approvedComments = commentRepository.findByStatus(CommentStatus.APPROVED);

        // Then
        assertThat(pendingComments).hasSize(1);
        assertThat(pendingComments.get(0).getContent()).isEqualTo("This is a guest comment");
        
        assertThat(approvedComments).hasSize(2);
    }

    @Test
    @DisplayName("Should find comments by status with pagination")
    void shouldFindCommentsByStatusWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

        // When
        Page<Comment> approvedComments = commentRepository.findByStatus(CommentStatus.APPROVED, pageable);

        // Then
        assertThat(approvedComments.getContent()).hasSize(2);
        assertThat(approvedComments.getContent()).allMatch(comment -> comment.getStatus() == CommentStatus.APPROVED);
    }

    @Test
    @DisplayName("Should count comments by status")
    void shouldCountCommentsByStatus() {
        // When
        long pendingCount = commentRepository.countByStatus(CommentStatus.PENDING);
        long approvedCount = commentRepository.countByStatus(CommentStatus.APPROVED);
        long rejectedCount = commentRepository.countByStatus(CommentStatus.REJECTED);

        // Then
        assertThat(pendingCount).isEqualTo(1);
        assertThat(approvedCount).isEqualTo(2);
        assertThat(rejectedCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Should find comments pending moderation")
    void shouldFindCommentsPendingModeration() {
        // When
        List<Comment> pendingComments = commentRepository.findCommentsPendingModeration();

        // Then
        assertThat(pendingComments).hasSize(1);
        assertThat(pendingComments.get(0).getStatus()).isEqualTo(CommentStatus.PENDING);
    }

    @Test
    @DisplayName("Should find comments pending moderation with pagination")
    void shouldFindCommentsPendingModerationWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").ascending());

        // When
        Page<Comment> pendingComments = commentRepository.findCommentsPendingModeration(pageable);

        // Then
        assertThat(pendingComments.getContent()).hasSize(1);
        assertThat(pendingComments.getContent().get(0).getStatus()).isEqualTo(CommentStatus.PENDING);
    }

    // ==================== Author-Based Tests ====================

    @Test
    @DisplayName("Should find comments by author user")
    void shouldFindCommentsByAuthorUser() {
        // When
        List<Comment> userComments = commentRepository.findByAuthorUser(testUser);

        // Then
        assertThat(userComments).hasSize(1);
        assertThat(userComments.get(0).getContent()).isEqualTo("This is a parent comment");
    }

    @Test
    @DisplayName("Should find comments by author user with pagination")
    void shouldFindCommentsByAuthorUserWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

        // When
        Page<Comment> userComments = commentRepository.findByAuthorUser(testUser, pageable);

        // Then
        assertThat(userComments.getContent()).hasSize(1);
        assertThat(userComments.getContent().get(0).getAuthorUser()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("Should find comments by author user ID")
    void shouldFindCommentsByAuthorUserId() {
        // When
        List<Comment> userComments = commentRepository.findByAuthorUserId(testUser.getId());

        // Then
        assertThat(userComments).hasSize(1);
        assertThat(userComments.get(0).getAuthorUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("Should find guest comments")
    void shouldFindGuestComments() {
        // When
        List<Comment> guestComments = commentRepository.findGuestComments();

        // Then
        assertThat(guestComments).hasSize(1);
        assertThat(guestComments.get(0).getAuthorUser()).isNull();
        assertThat(guestComments.get(0).getAuthorName()).isEqualTo("Guest User");
    }

    @Test
    @DisplayName("Should find guest comments with pagination")
    void shouldFindGuestCommentsWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

        // When
        Page<Comment> guestComments = commentRepository.findGuestComments(pageable);

        // Then
        assertThat(guestComments.getContent()).hasSize(1);
        assertThat(guestComments.getContent().get(0).getAuthorUser()).isNull();
    }

    @Test
    @DisplayName("Should find comments by guest email")
    void shouldFindCommentsByGuestEmail() {
        // When
        List<Comment> emailComments = commentRepository.findByGuestEmail("guest@example.com");

        // Then
        assertThat(emailComments).hasSize(1);
        assertThat(emailComments.get(0).getAuthorEmail()).isEqualTo("guest@example.com");
    }

    // ==================== Hierarchical Tests ====================

    @Test
    @DisplayName("Should find top level comments")
    void shouldFindTopLevelComments() {
        // When
        List<Comment> topLevelComments = commentRepository.findTopLevelComments(testPost.getId());

        // Then
        assertThat(topLevelComments).hasSize(2); // Parent comment and guest comment
        assertThat(topLevelComments).allMatch(comment -> comment.getParentComment() == null);
    }

    @Test
    @DisplayName("Should find top level comments with pagination")
    void shouldFindTopLevelCommentsWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").ascending());

        // When
        Page<Comment> topLevelComments = commentRepository.findTopLevelComments(testPost.getId(), pageable);

        // Then
        assertThat(topLevelComments.getContent()).hasSize(2);
        assertThat(topLevelComments.getContent()).allMatch(comment -> comment.getParentComment() == null);
    }

    @Test
    @DisplayName("Should find replies to comment")
    void shouldFindRepliesToComment() {
        // When
        List<Comment> replies = commentRepository.findRepliesByParentId(parentComment.getId());

        // Then
        assertThat(replies).hasSize(1);
        assertThat(replies.get(0).getContent()).isEqualTo("This is a child comment");
        assertThat(replies.get(0).getParentComment().getId()).isEqualTo(parentComment.getId());
    }

    @Test
    @DisplayName("Should find replies to comment with pagination")
    void shouldFindRepliesToCommentWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").ascending());

        // When
        Page<Comment> replies = commentRepository.findRepliesByParentId(parentComment.getId(), pageable);

        // Then
        assertThat(replies.getContent()).hasSize(1);
        assertThat(replies.getContent().get(0).getParentComment().getId()).isEqualTo(parentComment.getId());
    }

    @Test
    @DisplayName("Should count replies to comment")
    void shouldCountRepliesToComment() {
        // When
        long replyCount = commentRepository.countRepliesByParentId(parentComment.getId());

        // Then
        assertThat(replyCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Should find comment thread")
    void shouldFindCommentThread() {
        // When
        List<Comment> thread = commentRepository.findCommentThread(childComment.getId());

        // Then
        assertThat(thread).hasSize(2); // Parent and child
        assertThat(thread.get(0).getId()).isEqualTo(parentComment.getId());
        assertThat(thread.get(1).getId()).isEqualTo(childComment.getId());
    }

    // ==================== Security and Tracking Tests ====================

    @Test
    @DisplayName("Should find comments by IP address")
    void shouldFindCommentsByIpAddress() {
        // When
        List<Comment> ipComments = commentRepository.findByIpAddress("192.168.1.1");

        // Then
        assertThat(ipComments).hasSize(1);
        assertThat(ipComments.get(0).getContent()).isEqualTo("This is a parent comment");
    }

    @Test
    @DisplayName("Should find comments by IP address with pagination")
    void shouldFindCommentsByIpAddressWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

        // When
        Page<Comment> ipComments = commentRepository.findByIpAddress("192.168.1.1", pageable);

        // Then
        assertThat(ipComments.getContent()).hasSize(1);
        assertThat(ipComments.getContent().get(0).getIpAddress()).isEqualTo("192.168.1.1");
    }

    @Test
    @DisplayName("Should find suspicious comments by IP")
    void shouldFindSuspiciousCommentsByIp() {
        // Given - Create multiple comments from same IP
        Comment suspiciousComment1 = createGuestComment("Suspicious 1", "Spammer", "spam1@example.com", testPost, CommentStatus.PENDING);
        Comment suspiciousComment2 = createGuestComment("Suspicious 2", "Spammer", "spam2@example.com", testPost, CommentStatus.PENDING);
        suspiciousComment1.setIpAddress("192.168.1.100");
        suspiciousComment2.setIpAddress("192.168.1.100");
        entityManager.persistAndFlush(suspiciousComment1);
        entityManager.persistAndFlush(suspiciousComment2);

        // When
        List<Comment> suspiciousComments = commentRepository.findSuspiciousCommentsByIp("192.168.1.100", 2);

        // Then
        assertThat(suspiciousComments).hasSize(2);
        assertThat(suspiciousComments).allMatch(comment -> comment.getIpAddress().equals("192.168.1.100"));
    }

    @Test
    @DisplayName("Should find comments by user agent pattern")
    void shouldFindCommentsByUserAgentPattern() {
        // When
        List<Comment> botComments = commentRepository.findByUserAgentPattern("%Test Browser%");

        // Then
        assertThat(botComments).hasSize(1);
        assertThat(botComments.get(0).getUserAgent()).contains("Test Browser");
    }

    // ==================== Date Range Tests ====================

    @Test
    @DisplayName("Should find comments in date range")
    void shouldFindCommentsInDateRange() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // When
        List<Comment> commentsInRange = commentRepository.findByCreatedAtBetween(startDate, endDate);

        // Then
        assertThat(commentsInRange).hasSize(3); // All comments created today
    }

    @Test
    @DisplayName("Should find comments in date range with pagination")
    void shouldFindCommentsInDateRangeWithPagination() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        Pageable pageable = PageRequest.of(0, 2, Sort.by("createdAt").descending());

        // When
        Page<Comment> commentsInRange = commentRepository.findByCreatedAtBetween(startDate, endDate, pageable);

        // Then
        assertThat(commentsInRange.getContent()).hasSize(2);
        assertThat(commentsInRange.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should find recent comments")
    void shouldFindRecentComments() {
        // Given
        LocalDateTime since = LocalDateTime.now().minusHours(1);

        // When
        List<Comment> recentComments = commentRepository.findRecentComments(since);

        // Then
        assertThat(recentComments).hasSize(3); // All comments are recent
    }

    @Test
    @DisplayName("Should find recent comments with pagination")
    void shouldFindRecentCommentsWithPagination() {
        // Given
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

        // When
        Page<Comment> recentComments = commentRepository.findRecentComments(since, pageable);

        // Then
        assertThat(recentComments.getContent()).hasSize(3);
    }

    // ==================== Statistics Tests ====================

    @Test
    @DisplayName("Should count comments by blog post")
    void shouldCountCommentsByBlogPost() {
        // When
        long commentCount = commentRepository.countByBlogPostId(testPost.getId());

        // Then
        assertThat(commentCount).isEqualTo(3);
    }

    @Test
    @DisplayName("Should count approved comments by blog post")
    void shouldCountApprovedCommentsByBlogPost() {
        // When
        long approvedCount = commentRepository.countApprovedByBlogPostId(testPost.getId());

        // Then
        assertThat(approvedCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Should get comment statistics by status")
    void shouldGetCommentStatisticsByStatus() {
        // When
        List<Object[]> stats = commentRepository.getCommentStatisticsByStatus();

        // Then
        assertThat(stats).hasSize(2); // PENDING and APPROVED statuses
        
        // Find APPROVED status stats
        Object[] approvedStats = stats.stream()
                .filter(stat -> stat[0] == CommentStatus.APPROVED)
                .findFirst()
                .orElseThrow();
        
        assertThat(approvedStats[1]).isEqualTo(2L); // Count
    }

    @Test
    @DisplayName("Should get daily comment counts")
    void shouldGetDailyCommentCounts() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // When
        List<Object[]> dailyCounts = commentRepository.getDailyCommentCounts(startDate, endDate);

        // Then
        assertThat(dailyCounts).isNotEmpty();
        
        // Verify structure: [date, count]
        Object[] todayStats = dailyCounts.get(dailyCounts.size() - 1);
        assertThat(todayStats).hasSize(2);
        assertThat(todayStats[1]).isEqualTo(3L); // Today's comment count
    }

    // ==================== Moderation Tests ====================

    @Test
    @DisplayName("Should bulk approve comments")
    void shouldBulkApproveComments() {
        // Given
        List<Long> commentIds = List.of(guestComment.getId());

        // When
        int approvedCount = commentRepository.bulkApproveComments(commentIds);

        // Then
        assertThat(approvedCount).isEqualTo(1);
        
        entityManager.clear();
        Comment approved = commentRepository.findById(guestComment.getId()).orElseThrow();
        assertThat(approved.getStatus()).isEqualTo(CommentStatus.APPROVED);
    }

    @Test
    @DisplayName("Should bulk reject comments")
    void shouldBulkRejectComments() {
        // Given
        List<Long> commentIds = List.of(guestComment.getId());

        // When
        int rejectedCount = commentRepository.bulkRejectComments(commentIds);

        // Then
        assertThat(rejectedCount).isEqualTo(1);
        
        entityManager.clear();
        Comment rejected = commentRepository.findById(guestComment.getId()).orElseThrow();
        assertThat(rejected.getStatus()).isEqualTo(CommentStatus.REJECTED);
    }

    @Test
    @DisplayName("Should bulk mark comments as spam")
    void shouldBulkMarkCommentsAsSpam() {
        // Given
        List<Long> commentIds = List.of(guestComment.getId());

        // When
        int spamCount = commentRepository.bulkMarkAsSpam(commentIds);

        // Then
        assertThat(spamCount).isEqualTo(1);
        
        entityManager.clear();
        Comment spam = commentRepository.findById(guestComment.getId()).orElseThrow();
        assertThat(spam.getStatus()).isEqualTo(CommentStatus.SPAM);
    }

    @Test
    @DisplayName("Should find spam comments")
    void shouldFindSpamComments() {
        // Given
        commentRepository.bulkMarkAsSpam(List.of(guestComment.getId()));
        entityManager.clear();

        // When
        List<Comment> spamComments = commentRepository.findSpamComments();

        // Then
        assertThat(spamComments).hasSize(1);
        assertThat(spamComments.get(0).getStatus()).isEqualTo(CommentStatus.SPAM);
    }

    // ==================== Search Tests ====================

    @Test
    @DisplayName("Should search comments by content")
    void shouldSearchCommentsByContent() {
        // When
        List<Comment> results = commentRepository.searchByContent("parent");

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getContent()).contains("parent");
    }

    @Test
    @DisplayName("Should search comments by content with pagination")
    void shouldSearchCommentsByContentWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

        // When
        Page<Comment> results = commentRepository.searchByContent("comment", pageable);

        // Then
        assertThat(results.getContent()).hasSize(3); // All comments contain "comment"
    }

    @Test
    @DisplayName("Should search comments by author name")
    void shouldSearchCommentsByAuthorName() {
        // When
        List<Comment> results = commentRepository.searchByAuthorName("Guest");

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAuthorName()).contains("Guest");
    }

    // ==================== Soft Delete Tests ====================

    @Test
    @DisplayName("Should not find soft deleted comments")
    void shouldNotFindSoftDeletedComments() {
        // Given
        commentRepository.softDeleteById(parentComment.getId());
        entityManager.clear();

        // When
        List<Comment> allActive = commentRepository.findAllActive();
        List<Comment> postComments = commentRepository.findByBlogPostId(testPost.getId());

        // Then
        assertThat(allActive).doesNotContain(parentComment);
        assertThat(postComments).hasSize(2); // Only child and guest comments
    }

    @Test
    @DisplayName("Should cascade soft delete to replies")
    void shouldCascadeSoftDeleteToReplies() {
        // When
        int deletedCount = commentRepository.softDeleteCommentAndReplies(parentComment.getId());

        // Then
        assertThat(deletedCount).isEqualTo(2); // Parent and child comment
        
        entityManager.clear();
        List<Comment> activeComments = commentRepository.findAllActive();
        assertThat(activeComments).hasSize(1); // Only guest comment remains
    }

    // ==================== Validation Tests ====================

    @Test
    @DisplayName("Should validate comment content length")
    void shouldValidateCommentContentLength() {
        // Given
        Comment invalidComment = new Comment();
        invalidComment.setContent(""); // Empty content
        invalidComment.setAuthorUser(testUser);
        invalidComment.setBlogPost(testPost);

        // When & Then
        assertThatThrownBy(() -> entityManager.persistAndFlush(invalidComment))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should validate guest comment email")
    void shouldValidateGuestCommentEmail() {
        // Given
        Comment invalidComment = new Comment();
        invalidComment.setContent("Valid content");
        invalidComment.setAuthorName("Guest");
        invalidComment.setAuthorEmail("invalid-email"); // Invalid email format
        invalidComment.setBlogPost(testPost);

        // When & Then
        assertThatThrownBy(() -> entityManager.persistAndFlush(invalidComment))
                .isInstanceOf(Exception.class);
    }

    // ==================== Performance Tests ====================

    @Test
    @DisplayName("Should handle large number of comments efficiently")
    void shouldHandleLargeNumberOfCommentsEfficiently() {
        // Given - Create many comments
        for (int i = 0; i < 100; i++) {
            Comment comment = createGuestComment("Comment " + i, "User " + i, "user" + i + "@example.com", testPost, CommentStatus.APPROVED);
            entityManager.persist(comment);
        }
        entityManager.flush();
        entityManager.clear();

        // When
        long startTime = System.currentTimeMillis();
        Page<Comment> comments = commentRepository.findApprovedByBlogPostId(testPost.getId(), PageRequest.of(0, 20));
        long endTime = System.currentTimeMillis();

        // Then
        assertThat(comments.getContent()).hasSize(20);
        assertThat(endTime - startTime).isLessThan(1000); // Should complete within 1 second
    }

    // ==================== Edge Cases Tests ====================

    @Test
    @DisplayName("Should handle null and empty parameters gracefully")
    void shouldHandleNullAndEmptyParametersGracefully() {
        // When & Then
        assertThat(commentRepository.findByBlogPostId(999L)).isEmpty();
        assertThat(commentRepository.searchByContent("")).isEmpty();
        assertThat(commentRepository.findByGuestEmail("")).isEmpty();
    }

    @Test
    @DisplayName("Should handle circular reference prevention in replies")
    void shouldHandleCircularReferencePreventionInReplies() {
        // Given
        Comment reply = createUserComment("Reply comment", testUser, testPost, CommentStatus.APPROVED);
        reply.setParentComment(parentComment);
        entityManager.persistAndFlush(reply);

        // When & Then - Attempting to make parent a reply to its own child should fail
        assertThatThrownBy(() -> {
            parentComment.setParentComment(reply);
            entityManager.persistAndFlush(parentComment);
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should handle deep comment nesting")
    void shouldHandleDeepCommentNesting() {
        // Given - Create a chain of replies
        Comment level1 = createUserComment("Level 1", testUser, testPost, CommentStatus.APPROVED);
        entityManager.persistAndFlush(level1);
        
        Comment level2 = createUserComment("Level 2", anotherUser, testPost, CommentStatus.APPROVED);
        level2.setParentComment(level1);
        entityManager.persistAndFlush(level2);
        
        Comment level3 = createUserComment("Level 3", testUser, testPost, CommentStatus.APPROVED);
        level3.setParentComment(level2);
        entityManager.persistAndFlush(level3);

        // When
        List<Comment> thread = commentRepository.findCommentThread(level3.getId());

        // Then
        assertThat(thread).hasSize(3);
        assertThat(thread.get(0).getId()).isEqualTo(level1.getId());
        assertThat(thread.get(1).getId()).isEqualTo(level2.getId());
        assertThat(thread.get(2).getId()).isEqualTo(level3.getId());
    }
}