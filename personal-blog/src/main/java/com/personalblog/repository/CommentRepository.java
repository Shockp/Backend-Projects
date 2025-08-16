package com.personalblog.repository;

import com.personalblog.entity.Comment;
import com.personalblog.entity.CommentStatus;
import com.personalblog.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Comment entity operations.
 * Provides comprehensive data access methods for comment management
 * with moderation workflow, hierarchical structure, and security features.
 * 
 * Features:
 * - Comment status management and moderation workflow
 * - Hierarchical comment structure (replies)
 * - Blog post associations and filtering
 * - Author-based queries (registered users and guests)
 * - Security tracking (IP address, user agent)
 * - Comment statistics and analytics
 * - Spam detection and filtering
 * - Bulk moderation operations
 * - Performance-optimized queries with proper indexing
 * - Soft delete support inherited from BaseRepository
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface CommentRepository extends BaseRepository<Comment, Long> {

    // ==================== Blog Post Association Queries ====================

    /**
     * Find comments by blog post ID.
     * 
     * @param blogPostId the blog post ID
     * @return list of comments for the blog post
     */
    @Query("SELECT c FROM Comment c WHERE c.blogPost.id = :blogPostId AND c.deleted = false ORDER BY c.createdAt ASC")
    List<Comment> findByBlogPostId(@Param("blogPostId") Long blogPostId);

    /**
     * Find comments by blog post ID with pagination.
     * 
     * @param blogPostId the blog post ID
     * @param pageable pagination information
     * @return page of comments for the blog post
     */
    @Query("SELECT c FROM Comment c WHERE c.blogPost.id = :blogPostId AND c.deleted = false")
    Page<Comment> findByBlogPostId(@Param("blogPostId") Long blogPostId, Pageable pageable);

    /**
     * Find approved comments by blog post ID.
     * 
     * @param blogPostId the blog post ID
     * @return list of approved comments for the blog post
     */
    @Query("SELECT c FROM Comment c WHERE c.blogPost.id = :blogPostId AND c.status = 'APPROVED' AND c.deleted = false ORDER BY c.createdAt ASC")
    List<Comment> findApprovedByBlogPostId(@Param("blogPostId") Long blogPostId);

    /**
     * Find approved comments by blog post ID with pagination.
     * 
     * @param blogPostId the blog post ID
     * @param pageable pagination information
     * @return page of approved comments for the blog post
     */
    @Query("SELECT c FROM Comment c WHERE c.blogPost.id = :blogPostId AND c.status = 'APPROVED' AND c.deleted = false")
    Page<Comment> findApprovedByBlogPostId(@Param("blogPostId") Long blogPostId, Pageable pageable);

    // ==================== Status-Based Queries ====================

    /**
     * Find comments by status.
     * 
     * @param status the comment status
     * @return list of comments with the specified status
     */
    @Query("SELECT c FROM Comment c WHERE c.status = :status AND c.deleted = false ORDER BY c.createdAt DESC")
    List<Comment> findByStatus(@Param("status") CommentStatus status);

    /**
     * Find comments by status with pagination.
     * 
     * @param status the comment status
     * @param pageable pagination information
     * @return page of comments with the specified status
     */
    @Query("SELECT c FROM Comment c WHERE c.status = :status AND c.deleted = false")
    Page<Comment> findByStatus(@Param("status") CommentStatus status, Pageable pageable);

    /**
     * Count comments by status.
     * 
     * @param status the comment status
     * @return number of comments with the specified status
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.status = :status AND c.deleted = false")
    long countByStatus(@Param("status") CommentStatus status);

    /**
     * Find comments pending moderation.
     * 
     * @return list of comments pending moderation
     */
    @Query("SELECT c FROM Comment c WHERE c.status = 'PENDING' AND c.deleted = false ORDER BY c.createdAt ASC")
    List<Comment> findCommentsPendingModeration();

    /**
     * Find comments pending moderation with pagination.
     * 
     * @param pageable pagination information
     * @return page of comments pending moderation
     */
    @Query("SELECT c FROM Comment c WHERE c.status = 'PENDING' AND c.deleted = false")
    Page<Comment> findCommentsPendingModeration(Pageable pageable);

    // ==================== Author-Based Queries ====================

    /**
     * Find comments by author user.
     * 
     * @param authorUser the author user
     * @return list of comments by the user
     */
    @Query("SELECT c FROM Comment c WHERE c.authorUser = :authorUser AND c.deleted = false ORDER BY c.createdAt DESC")
    List<Comment> findByAuthorUser(@Param("authorUser") User authorUser);

    /**
     * Find comments by author user with pagination.
     * 
     * @param authorUser the author user
     * @param pageable pagination information
     * @return page of comments by the user
     */
    @Query("SELECT c FROM Comment c WHERE c.authorUser = :authorUser AND c.deleted = false")
    Page<Comment> findByAuthorUser(@Param("authorUser") User authorUser, Pageable pageable);

    /**
     * Find comments by author user ID.
     * 
     * @param authorUserId the author user ID
     * @return list of comments by the user
     */
    @Query("SELECT c FROM Comment c WHERE c.authorUser.id = :authorUserId AND c.deleted = false ORDER BY c.createdAt DESC")
    List<Comment> findByAuthorUserId(@Param("authorUserId") Long authorUserId);

    /**
     * Find guest comments.
     * 
     * @return list of guest comments
     */
    @Query("SELECT c FROM Comment c WHERE c.authorUser IS NULL AND c.deleted = false ORDER BY c.createdAt DESC")
    List<Comment> findGuestComments();

    /**
     * Find guest comments with pagination.
     * 
     * @param pageable pagination information
     * @return page of guest comments
     */
    @Query("SELECT c FROM Comment c WHERE c.authorUser IS NULL AND c.deleted = false")
    Page<Comment> findGuestComments(Pageable pageable);

    /**
     * Find comments by guest email.
     * 
     * @param guestEmail the guest email
     * @return list of comments by the guest email
     */
    @Query("SELECT c FROM Comment c WHERE c.authorEmail = :guestEmail AND c.deleted = false ORDER BY c.createdAt DESC")
    List<Comment> findByGuestEmail(@Param("guestEmail") String guestEmail);

    // ==================== Hierarchical Queries ====================

    /**
     * Find top level comments for blog post.
     * 
     * @param blogPostId the blog post ID
     * @return list of top level comments
     */
    @Query("SELECT c FROM Comment c WHERE c.blogPost.id = :blogPostId AND c.parentComment IS NULL AND c.deleted = false ORDER BY c.createdAt ASC")
    List<Comment> findTopLevelComments(@Param("blogPostId") Long blogPostId);

    /**
     * Find top level comments for blog post with pagination.
     * 
     * @param blogPostId the blog post ID
     * @param pageable pagination information
     * @return page of top level comments
     */
    @Query("SELECT c FROM Comment c WHERE c.blogPost.id = :blogPostId AND c.parentComment IS NULL AND c.deleted = false")
    Page<Comment> findTopLevelComments(@Param("blogPostId") Long blogPostId, Pageable pageable);

    /**
     * Find replies by parent comment ID.
     * 
     * @param parentId the parent comment ID
     * @return list of reply comments
     */
    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentId AND c.deleted = false ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentId(@Param("parentId") Long parentId);

    /**
     * Find replies by parent comment ID with pagination.
     * 
     * @param parentId the parent comment ID
     * @param pageable pagination information
     * @return page of reply comments
     */
    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentId AND c.deleted = false")
    Page<Comment> findRepliesByParentId(@Param("parentId") Long parentId, Pageable pageable);

    /**
     * Count replies by parent comment ID.
     * 
     * @param parentId the parent comment ID
     * @return number of replies
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.parentComment.id = :parentId AND c.deleted = false")
    long countRepliesByParentId(@Param("parentId") Long parentId);

    /**
     * Find comment thread (from root to specified comment).
     * 
     * @param commentId the comment ID
     * @return list of comments in the thread
     */
    @Query(value = """
        WITH RECURSIVE comment_thread AS (
            SELECT id, content, parent_comment_id, 0 as level
            FROM comments 
            WHERE id = :commentId AND deleted = false
            UNION ALL
            SELECT c.id, c.content, c.parent_comment_id, ct.level + 1
            FROM comments c
            INNER JOIN comment_thread ct ON c.id = ct.parent_comment_id
            WHERE c.deleted = false
        )
        SELECT * FROM comment_thread ORDER BY level DESC
        """, nativeQuery = true)
    List<Comment> findCommentThread(@Param("commentId") Long commentId);

    // ==================== Security and Tracking Queries ====================

    /**
     * Find comments by IP address.
     * 
     * @param ipAddress the IP address
     * @return list of comments from the IP address
     */
    @Query("SELECT c FROM Comment c WHERE c.ipAddress = :ipAddress AND c.deleted = false ORDER BY c.createdAt DESC")
    List<Comment> findByIpAddress(@Param("ipAddress") String ipAddress);

    /**
     * Find comments by IP address with pagination.
     * 
     * @param ipAddress the IP address
     * @param pageable pagination information
     * @return page of comments from the IP address
     */
    @Query("SELECT c FROM Comment c WHERE c.ipAddress = :ipAddress AND c.deleted = false")
    Page<Comment> findByIpAddress(@Param("ipAddress") String ipAddress, Pageable pageable);

    /**
     * Find suspicious comments by IP (multiple comments from same IP).
     * 
     * @param ipAddress the IP address
     * @param threshold minimum number of comments to be considered suspicious
     * @return list of suspicious comments
     */
    @Query("SELECT c FROM Comment c WHERE c.ipAddress = :ipAddress AND c.deleted = false AND (SELECT COUNT(c2) FROM Comment c2 WHERE c2.ipAddress = :ipAddress AND c2.deleted = false) >= :threshold ORDER BY c.createdAt DESC")
    List<Comment> findSuspiciousCommentsByIp(@Param("ipAddress") String ipAddress, @Param("threshold") Integer threshold);

    /**
     * Find comments by user agent pattern.
     * 
     * @param userAgentPattern the user agent pattern
     * @return list of comments matching the user agent pattern
     */
    @Query("SELECT c FROM Comment c WHERE c.userAgent LIKE :userAgentPattern AND c.deleted = false ORDER BY c.createdAt DESC")
    List<Comment> findByUserAgentPattern(@Param("userAgentPattern") String userAgentPattern);

    // ==================== Date Range Queries ====================

    /**
     * Find comments in date range.
     * 
     * @param startDate start date
     * @param endDate end date
     * @return list of comments in the date range
     */
    @Query("SELECT c FROM Comment c WHERE c.createdAt BETWEEN :startDate AND :endDate AND c.deleted = false ORDER BY c.createdAt DESC")
    List<Comment> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find comments in date range with pagination.
     * 
     * @param startDate start date
     * @param endDate end date
     * @param pageable pagination information
     * @return page of comments in the date range
     */
    @Query("SELECT c FROM Comment c WHERE c.createdAt BETWEEN :startDate AND :endDate AND c.deleted = false")
    Page<Comment> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    /**
     * Find recent comments.
     * 
     * @param since the date threshold
     * @return list of recent comments
     */
    @Query("SELECT c FROM Comment c WHERE c.createdAt >= :since AND c.deleted = false ORDER BY c.createdAt DESC")
    List<Comment> findRecentComments(@Param("since") LocalDateTime since);

    /**
     * Find recent comments with pagination.
     * 
     * @param since the date threshold
     * @param pageable pagination information
     * @return page of recent comments
     */
    @Query("SELECT c FROM Comment c WHERE c.createdAt >= :since AND c.deleted = false")
    Page<Comment> findRecentComments(@Param("since") LocalDateTime since, Pageable pageable);

    // ==================== Statistics Queries ====================

    /**
     * Count comments by blog post.
     * 
     * @param blogPostId the blog post ID
     * @return number of comments for the blog post
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.blogPost.id = :blogPostId AND c.deleted = false")
    long countByBlogPostId(@Param("blogPostId") Long blogPostId);

    /**
     * Count approved comments by blog post.
     * 
     * @param blogPostId the blog post ID
     * @return number of approved comments for the blog post
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.blogPost.id = :blogPostId AND c.status = 'APPROVED' AND c.deleted = false")
    long countApprovedByBlogPostId(@Param("blogPostId") Long blogPostId);

    /**
     * Get comment statistics by status.
     * 
     * @return list of [CommentStatus, count] arrays
     */
    @Query("SELECT c.status, COUNT(c) FROM Comment c WHERE c.deleted = false GROUP BY c.status")
    List<Object[]> getCommentStatisticsByStatus();

    /**
     * Get daily comment counts.
     * 
     * @param startDate start date
     * @param endDate end date
     * @return list of [date, count] arrays
     */
    @Query(value = """
        SELECT DATE(created_at) as comment_date, COUNT(*) as comment_count
        FROM comments 
        WHERE created_at BETWEEN :startDate AND :endDate AND deleted = false
        GROUP BY DATE(created_at)
        ORDER BY comment_date
        """, nativeQuery = true)
    List<Object[]> getDailyCommentCounts(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // ==================== Moderation Operations ====================

    /**
     * Bulk approve comments.
     * 
     * @param commentIds list of comment IDs to approve
     * @return number of approved comments
     */
    @Modifying
    @Transactional
    @Query("UPDATE Comment c SET c.status = 'APPROVED' WHERE c.id IN :commentIds AND c.deleted = false")
    int bulkApproveComments(@Param("commentIds") List<Long> commentIds);

    /**
     * Bulk reject comments.
     * 
     * @param commentIds list of comment IDs to reject
     * @return number of rejected comments
     */
    @Modifying
    @Transactional
    @Query("UPDATE Comment c SET c.status = 'REJECTED' WHERE c.id IN :commentIds AND c.deleted = false")
    int bulkRejectComments(@Param("commentIds") List<Long> commentIds);

    /**
     * Bulk mark comments as spam.
     * 
     * @param commentIds list of comment IDs to mark as spam
     * @return number of comments marked as spam
     */
    @Modifying
    @Transactional
    @Query("UPDATE Comment c SET c.status = 'SPAM' WHERE c.id IN :commentIds AND c.deleted = false")
    int bulkMarkAsSpam(@Param("commentIds") List<Long> commentIds);

    /**
     * Find spam comments.
     * 
     * @return list of spam comments
     */
    @Query("SELECT c FROM Comment c WHERE c.status = 'SPAM' AND c.deleted = false ORDER BY c.createdAt DESC")
    List<Comment> findSpamComments();

    // ==================== Search Queries ====================

    /**
     * Search comments by content.
     * 
     * @param searchTerm the search term
     * @return list of matching comments
     */
    @Query("SELECT c FROM Comment c WHERE c.deleted = false AND LOWER(c.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY c.createdAt DESC")
    List<Comment> searchByContent(@Param("searchTerm") String searchTerm);

    /**
     * Search comments by content with pagination.
     * 
     * @param searchTerm the search term
     * @param pageable pagination information
     * @return page of matching comments
     */
    @Query("SELECT c FROM Comment c WHERE c.deleted = false AND LOWER(c.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Comment> searchByContent(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Search comments by author name.
     * 
     * @param authorName the author name
     * @return list of matching comments
     */
    @Query("SELECT c FROM Comment c WHERE c.deleted = false AND (LOWER(c.authorName) LIKE LOWER(CONCAT('%', :authorName, '%')) OR (c.authorUser IS NOT NULL AND LOWER(c.authorUser.username) LIKE LOWER(CONCAT('%', :authorName, '%')))) ORDER BY c.createdAt DESC")
    List<Comment> searchByAuthorName(@Param("authorName") String authorName);

    // ==================== Bulk Operations ====================

    /**
     * Soft delete comment and all its replies.
     * 
     * @param commentId the comment ID
     * @return number of deleted comments
     */
    @Modifying
    @Transactional
    @Query(value = """
        WITH RECURSIVE comment_tree AS (
            SELECT id FROM comments WHERE id = :commentId
            UNION ALL
            SELECT c.id FROM comments c
            INNER JOIN comment_tree ct ON c.parent_comment_id = ct.id
        )
        UPDATE comments SET deleted = true, updated_at = CURRENT_TIMESTAMP 
        WHERE id IN (SELECT id FROM comment_tree) AND deleted = false
        """, nativeQuery = true)
    int softDeleteCommentAndReplies(@Param("commentId") Long commentId);
}