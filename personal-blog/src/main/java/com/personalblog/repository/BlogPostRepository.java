package com.personalblog.repository;

import com.personalblog.entity.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing BlogPost entities.
 * 
 * <p>This repository extends BaseRepository to inherit soft delete functionality,
 * audit capabilities, and common CRUD operations. It provides comprehensive
 * query methods for blog post management including content search, status filtering,
 * category and tag-based queries, SEO operations, and analytics.</p>
 * 
 * <p>All queries in this repository are soft delete aware and will only return
 * non-deleted entities unless explicitly specified otherwise.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Status-based filtering (DRAFT, PUBLISHED, ARCHIVED)</li>
 *   <li>Full-text search across title and content</li>
 *   <li>SEO-friendly slug-based routing</li>
 *   <li>Category and tag-based filtering</li>
 *   <li>Author-based queries with pagination</li>
 *   <li>Analytics and reporting methods</li>
 *   <li>Performance-optimized queries with fetch strategies</li>
 * </ul>
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 * @see BaseRepository
 * @see BlogPost
 */
@Validated
public interface BlogPostRepository extends BaseRepository<BlogPost, Long> {

    // ==================== Status-Based Queries ====================

    /**
     * Find all blog posts with the specified status.
     * Only returns active, non-deleted posts.
     * 
     * @param status the post status to filter by (must not be null)
     * @param pageable pagination information (must not be null)
     * @return page of blog posts with the specified status
     */
    @Query("SELECT bp FROM BlogPost bp WHERE bp.status = :status AND bp.deleted = false " +
           "ORDER BY bp.createdAt DESC")
    Page<BlogPost> findByStatusAndDeletedFalse(@Param("status") @NotNull BlogPost.Status status, 
                                              @NonNull Pageable pageable);

    /**
     * Find blog posts by status and author.
     * Useful for author dashboards and content management.
     * 
     * @param status the post status to filter by (must not be null)
     * @param authorId the author ID to filter by (must not be null)
     * @return list of blog posts matching the criteria
     */
    @Query("SELECT bp FROM BlogPost bp WHERE bp.status = :status AND bp.author.id = :authorId " +
           "AND bp.deleted = false ORDER BY bp.updatedAt DESC")
    List<BlogPost> findByStatusAndAuthorIdAndDeletedFalse(@Param("status") @NotNull BlogPost.Status status, 
                                                         @Param("authorId") @NotNull Long authorId);

    /**
     * Find published blog posts with author and category information.
     * Uses entity graph for optimized loading to prevent N+1 queries.
     * 
     * @param pageable pagination information (must not be null)
     * @return page of published blog posts with loaded relationships
     */
    @EntityGraph(attributePaths = {"author", "category", "tags"})
    @Query("SELECT bp FROM BlogPost bp WHERE bp.status = 'PUBLISHED' AND bp.deleted = false " +
           "AND bp.publishedDate <= CURRENT_TIMESTAMP ORDER BY bp.publishedDate DESC")
    Page<BlogPost> findPublishedPostsWithDetails(@NonNull Pageable pageable);

    /**
     * Find draft posts by author for content management.
     * 
     * @param authorId the author ID to filter by (must not be null)
     * @param pageable pagination information (must not be null)
     * @return page of draft posts by the specified author
     */
    @Query("SELECT bp FROM BlogPost bp WHERE bp.status = 'DRAFT' AND bp.author.id = :authorId " +
           "AND bp.deleted = false ORDER BY bp.updatedAt DESC")
    Page<BlogPost> findDraftsByAuthor(@Param("authorId") @NotNull Long authorId, 
                                     @NonNull Pageable pageable);

    // ==================== Content Search Queries ====================

    /**
     * Search blog posts by title or content with case-insensitive matching.
     * Enhanced with input validation and security measures.
     * Note: Content field uses case-sensitive search due to CLOB limitations.
     * 
     * @param searchTerm the search term (must not be null or blank, min 2 chars for security)
     * @param pageable pagination information (must not be null)
     * @return page of blog posts matching the search criteria
     */
    @Query("SELECT bp FROM BlogPost bp WHERE bp.deleted = false AND bp.status = 'PUBLISHED' AND " +
           "(LOWER(bp.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "bp.content LIKE CONCAT('%', :searchTerm, '%') OR " +
           "LOWER(bp.excerpt) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<BlogPost> searchByTitleOrContent(@Param("searchTerm") @NotBlank @Size(min = 2, max = 100) String searchTerm, 
                                         @NonNull Pageable pageable);

    /**
     * Advanced search across multiple fields with status filter.
     * Supports searching in title, content, excerpt, and meta fields.
     * Note: Content field uses case-sensitive search due to CLOB limitations.
     * 
     * @param searchTerm the search term (must not be null or blank, min 2 chars for security)
     * @param status the post status to filter by (optional)
     * @param pageable pagination information (must not be null)
     * @return page of blog posts matching the search criteria
     */
    @Query("SELECT bp FROM BlogPost bp WHERE bp.deleted = false " +
           "AND (:status IS NULL OR bp.status = :status) " +
           "AND (LOWER(bp.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "bp.content LIKE CONCAT('%', :searchTerm, '%') OR " +
           "LOWER(bp.excerpt) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(bp.metaTitle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(bp.metaDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<BlogPost> advancedSearch(@Param("searchTerm") @NotBlank @Size(min = 2, max = 100) String searchTerm,
                                 @Param("status") BlogPost.Status status,
                                 @NonNull Pageable pageable);

    // ==================== SEO and Routing Queries ====================

    /**
     * Find a blog post by its slug for SEO-friendly URL routing.
     * Only returns published, non-deleted posts for public access.
     * 
     * @param slug the post slug (must not be null or blank)
     * @return optional containing the blog post if found and published
     */
    @EntityGraph(attributePaths = {"author", "category", "tags", "comments"})
    @Query("SELECT bp FROM BlogPost bp WHERE bp.slug = :slug AND bp.deleted = false " +
           "AND bp.status = 'PUBLISHED'")
    Optional<BlogPost> findBySlugAndPublished(@Param("slug") @NotBlank @Size(max = 255) String slug);

    /**
     * Find a blog post by slug regardless of status (for admin operations).
     * 
     * @param slug the post slug (must not be null or blank)
     * @return optional containing the blog post if found
     */
    @Query("SELECT bp FROM BlogPost bp WHERE bp.slug = :slug AND bp.deleted = false")
    Optional<BlogPost> findBySlugAndDeletedFalse(@Param("slug") @NotBlank @Size(max = 255) String slug);

    /**
     * Check if a slug exists (for validation during creation/update).
     * 
     * @param slug the slug to check (must not be null or blank)
     * @return true if slug exists
     */
    @Query("SELECT COUNT(bp) > 0 FROM BlogPost bp WHERE bp.slug = :slug AND bp.deleted = false")
    boolean existsBySlugAndDeletedFalse(@Param("slug") @NotBlank @Size(max = 255) String slug);

    /**
     * Check if a slug exists excluding a specific post (for update operations).
     * 
     * @param slug the slug to check (must not be null or blank)
     * @param excludeId the post ID to exclude from the check (must not be null)
     * @return true if slug exists for other posts
     */
    @Query("SELECT COUNT(bp) > 0 FROM BlogPost bp WHERE bp.slug = :slug " +
           "AND bp.id != :excludeId AND bp.deleted = false")
    boolean existsBySlugAndIdNotAndDeletedFalse(@Param("slug") @NotBlank @Size(max = 255) String slug, 
                                               @Param("excludeId") @NotNull Long excludeId);

    // ==================== Category-Based Queries ====================

    /**
     * Find blog posts by category with status filter.
     * Uses entity graph for optimized loading.
     * 
     * @param categoryId the category ID to filter by (must not be null)
     * @param status the post status to filter by (must not be null)
     * @param pageable pagination information (must not be null)
     * @return page of blog posts in the specified category
     */
    @EntityGraph(attributePaths = {"author", "category"})
    @Query("SELECT bp FROM BlogPost bp WHERE bp.category.id = :categoryId " +
           "AND bp.status = :status AND bp.deleted = false " +
           "ORDER BY bp.publishedDate DESC NULLS LAST, bp.createdAt DESC")
    Page<BlogPost> findByCategoryIdAndStatus(@Param("categoryId") @NotNull Long categoryId, 
                                           @Param("status") @NotNull BlogPost.Status status, 
                                           @NonNull Pageable pageable);

    /**
     * Find published blog posts by category slug for public category pages.
     * 
     * @param categorySlug the category slug (must not be null or blank)
     * @param pageable pagination information (must not be null)
     * @return page of published blog posts in the specified category
     */
    @EntityGraph(attributePaths = {"author", "category"})
    @Query("SELECT bp FROM BlogPost bp JOIN bp.category c WHERE c.slug = :categorySlug " +
           "AND bp.status = 'PUBLISHED' AND bp.deleted = false AND c.deleted = false " +
           "ORDER BY bp.publishedDate DESC")
    Page<BlogPost> findPublishedByCategorySlug(@Param("categorySlug") @NotBlank @Size(max = 255) String categorySlug, 
                                              @NonNull Pageable pageable);

    // ==================== Tag-Based Queries ====================

    /**
     * Find blog posts that contain any of the specified tags.
     * 
     * @param tagIds the list of tag IDs to filter by (must not be null or empty)
     * @param status the post status to filter by (must not be null)
     * @param pageable pagination information (must not be null)
     * @return page of blog posts containing the specified tags
     */
    @Query("SELECT DISTINCT bp FROM BlogPost bp JOIN bp.tags t " +
           "WHERE t.id IN :tagIds AND bp.status = :status AND bp.deleted = false " +
           "ORDER BY bp.publishedDate DESC NULLS LAST, bp.createdAt DESC")
    Page<BlogPost> findByTagIdsAndStatus(@Param("tagIds") @NotNull List<Long> tagIds, 
                                       @Param("status") @NotNull BlogPost.Status status, 
                                       @NonNull Pageable pageable);

    /**
     * Find published blog posts by tag slug for public tag pages.
     * 
     * @param tagSlug the tag slug (must not be null or blank)
     * @param pageable pagination information (must not be null)
     * @return page of published blog posts with the specified tag
     */
    @EntityGraph(attributePaths = {"author", "category"})
    @Query("SELECT DISTINCT bp FROM BlogPost bp JOIN bp.tags t WHERE t.slug = :tagSlug " +
           "AND bp.status = 'PUBLISHED' AND bp.deleted = false AND t.deleted = false " +
           "ORDER BY bp.publishedDate DESC")
    Page<BlogPost> findPublishedByTagSlug(@Param("tagSlug") @NotBlank @Size(max = 255) String tagSlug, 
                                         @NonNull Pageable pageable);

    /**
     * Find blog posts that contain all specified tags (AND operation).
     * 
     * @param tagIds the list of tag IDs that must all be present (must not be null or empty)
     * @param status the post status to filter by (must not be null)
     * @param pageable pagination information (must not be null)
     * @return page of blog posts containing all specified tags
     */
    @Query("SELECT bp FROM BlogPost bp WHERE bp.status = :status AND bp.deleted = false " +
           "AND (SELECT COUNT(t) FROM bp.tags t WHERE t.id IN :tagIds) = :tagCount " +
           "ORDER BY bp.publishedDate DESC NULLS LAST, bp.createdAt DESC")
    Page<BlogPost> findByAllTagIdsAndStatus(@Param("tagIds") @NotNull List<Long> tagIds,
                                           @Param("tagCount") long tagCount,
                                           @Param("status") @NotNull BlogPost.Status status, 
                                           @NonNull Pageable pageable);

    // ==================== Author-Based Queries ====================

    /**
     * Find blog posts by author with status filter.
     * 
     * @param authorId the author ID to filter by (must not be null)
     * @param status the post status to filter by (optional)
     * @param pageable pagination information (must not be null)
     * @return page of blog posts by the specified author
     */
    @Query("SELECT bp FROM BlogPost bp WHERE bp.author.id = :authorId " +
           "AND (:status IS NULL OR bp.status = :status) AND bp.deleted = false " +
           "ORDER BY bp.updatedAt DESC")
    Page<BlogPost> findByAuthorIdAndStatus(@Param("authorId") @NotNull Long authorId,
                                          @Param("status") BlogPost.Status status,
                                          @NonNull Pageable pageable);

    /**
     * Find published blog posts by author username for public author pages.
     * 
     * @param username the author username (must not be null or blank)
     * @param pageable pagination information (must not be null)
     * @return page of published blog posts by the specified author
     */
    @EntityGraph(attributePaths = {"author", "category"})
    @Query("SELECT bp FROM BlogPost bp JOIN bp.author a WHERE a.username = :username " +
           "AND bp.status = 'PUBLISHED' AND bp.deleted = false AND a.deleted = false " +
           "ORDER BY bp.publishedDate DESC")
    Page<BlogPost> findPublishedByAuthorUsername(@Param("username") @NotBlank @Size(min = 3, max = 50) String username, 
                                                @NonNull Pageable pageable);

    // ==================== Analytics and Statistics Queries ====================

    /**
     * Count blog posts by status.
     * 
     * @param status the post status to count (must not be null)
     * @return number of blog posts with the specified status
     */
    @Query("SELECT COUNT(bp) FROM BlogPost bp WHERE bp.status = :status AND bp.deleted = false")
    long countByStatus(@Param("status") @NotNull BlogPost.Status status);

    /**
     * Count blog posts by author and date range.
     * Used for author analytics and productivity tracking.
     * 
     * @param authorId the author ID to filter by (must not be null)
     * @param startDate the start date of the range (must not be null)
     * @param endDate the end date of the range (must not be null)
     * @return number of blog posts by author in the date range
     */
    @Query("SELECT COUNT(bp) FROM BlogPost bp WHERE bp.author.id = :authorId " +
           "AND bp.createdAt BETWEEN :startDate AND :endDate AND bp.deleted = false")
    long countByAuthorAndDateRange(@Param("authorId") @NotNull Long authorId, 
                                  @Param("startDate") @NotNull LocalDateTime startDate, 
                                  @Param("endDate") @NotNull LocalDateTime endDate);

    /**
     * Count published blog posts by category.
     * 
     * @param categoryId the category ID to filter by (must not be null)
     * @return number of published blog posts in the category
     */
    @Query("SELECT COUNT(bp) FROM BlogPost bp WHERE bp.category.id = :categoryId " +
           "AND bp.status = 'PUBLISHED' AND bp.deleted = false")
    long countPublishedByCategory(@Param("categoryId") @NotNull Long categoryId);

    /**
     * Find most viewed blog posts for analytics.
     * 
     * @param pageable pagination information (must not be null)
     * @return page of blog posts ordered by view count
     */
    @Query("SELECT bp FROM BlogPost bp WHERE bp.status = 'PUBLISHED' AND bp.deleted = false " +
           "ORDER BY bp.viewCount DESC NULLS LAST, bp.publishedDate DESC")
    Page<BlogPost> findMostViewedPosts(@NonNull Pageable pageable);

    /**
     * Find recently published blog posts.
     * 
     * @param since the date since when to find posts (must not be null)
     * @param pageable pagination information (must not be null)
     * @return page of recently published blog posts
     */
    @EntityGraph(attributePaths = {"author", "category"})
    @Query("SELECT bp FROM BlogPost bp WHERE bp.status = 'PUBLISHED' AND bp.deleted = false " +
           "AND bp.publishedDate >= :since ORDER BY bp.publishedDate DESC")
    Page<BlogPost> findRecentlyPublished(@Param("since") @NotNull LocalDateTime since, 
                                        @NonNull Pageable pageable);

    // ==================== Featured Content Queries ====================

    /**
     * Find featured blog posts for homepage display.
     * Assumes featured posts have a specific meta field or high view count.
     * 
     * @param pageable pagination information (must not be null)
     * @return page of featured blog posts
     */
    @EntityGraph(attributePaths = {"author", "category"})
    @Query("SELECT bp FROM BlogPost bp WHERE bp.status = 'PUBLISHED' AND bp.deleted = false " +
           "AND bp.featuredImageUrl IS NOT NULL " +
           "ORDER BY bp.viewCount DESC NULLS LAST, bp.publishedDate DESC")
    Page<BlogPost> findFeaturedPosts(@NonNull Pageable pageable);

    // ==================== Bulk Operations ====================

    /**
     * Update view count for a blog post.
     * Thread-safe operation for analytics.
     * 
     * @param postId the post ID to update (must not be null)
     * @return number of updated records
     */
    @Modifying
    @Transactional
    @Query("UPDATE BlogPost bp SET bp.viewCount = bp.viewCount + 1 " +
           "WHERE bp.id = :postId AND bp.deleted = false")
    int incrementViewCount(@Param("postId") @NotNull Long postId);

    /**
     * Bulk update post status.
     * Used for publishing or archiving multiple posts.
     * 
     * @param postIds the list of post IDs to update (must not be null or empty)
     * @param newStatus the new status to set (must not be null)
     * @return number of updated records
     */
    @Modifying
    @Transactional
    @Query("UPDATE BlogPost bp SET bp.status = :newStatus, bp.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE bp.id IN :postIds AND bp.deleted = false")
    int bulkUpdateStatus(@Param("postIds") @NotNull List<Long> postIds, 
                        @Param("newStatus") @NotNull BlogPost.Status newStatus);

    /**
     * Update published date when publishing posts.
     * 
     * @param postIds the list of post IDs to update (must not be null or empty)
     * @return number of updated records
     */
    @Modifying
    @Transactional
    @Query("UPDATE BlogPost bp SET bp.publishedDate = CURRENT_TIMESTAMP, " +
           "bp.status = 'PUBLISHED', bp.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE bp.id IN :postIds AND bp.deleted = false AND bp.status = 'DRAFT'")
    int publishPosts(@Param("postIds") @NotNull List<Long> postIds);

    // ==================== Utility and Validation Methods ====================

    /**
     * Check if a user has any blog posts.
     * Used for user deletion validation.
     * 
     * @param authorId the author ID to check (must not be null)
     * @return true if the author has any blog posts
     */
    @Query("SELECT COUNT(bp) > 0 FROM BlogPost bp WHERE bp.author.id = :authorId AND bp.deleted = false")
    boolean existsByAuthorId(@Param("authorId") @NotNull Long authorId);

    /**
     * Find blog posts scheduled for future publication.
     * Used for scheduled publishing features.
     * 
     * @param pageable pagination information (must not be null)
     * @return page of blog posts scheduled for future publication
     */
    @Query("SELECT bp FROM BlogPost bp WHERE bp.status = 'PUBLISHED' AND bp.deleted = false " +
           "AND bp.publishedDate > CURRENT_TIMESTAMP ORDER BY bp.publishedDate ASC")
    Page<BlogPost> findScheduledPosts(@NonNull Pageable pageable);

    /**
     * Find blog posts that need reading time calculation.
     * Used for maintenance tasks.
     * 
     * @return list of blog posts without reading time
     */
    @Query("SELECT bp FROM BlogPost bp WHERE bp.readingTimeMinutes IS NULL " +
           "AND bp.deleted = false ORDER BY bp.updatedAt DESC")
    List<BlogPost> findPostsWithoutReadingTime();
}