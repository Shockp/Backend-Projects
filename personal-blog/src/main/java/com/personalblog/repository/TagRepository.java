package com.personalblog.repository;

import com.personalblog.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Tag entity operations.
 * Provides comprehensive data access methods for tag management
 * with usage tracking, popularity metrics, and performance optimization.
 * 
 * Features:
 * - Tag usage count management and statistics
 * - Popular tags and tag cloud functionality
 * - Search capabilities across tag names and descriptions
 * - Blog post associations and filtering
 * - Thread-safe usage count operations
 * - Bulk operations for administrative tasks
 * - Performance-optimized queries with proper indexing
 * - Soft delete support inherited from BaseRepository
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface TagRepository extends BaseRepository<Tag, Long> {

    // ==================== Basic Queries ====================

    /**
     * Find tag by slug.
     * 
     * @param slug the URL-friendly slug
     * @return optional tag if found and not deleted
     */
    @Query("SELECT t FROM Tag t WHERE t.slug = :slug AND t.deleted = false")
    Optional<Tag> findBySlug(@Param("slug") String slug);

    /**
     * Find tag by name (case insensitive).
     * 
     * @param name the tag name
     * @return optional tag if found
     */
    @Query("SELECT t FROM Tag t WHERE LOWER(t.name) = LOWER(:name) AND t.deleted = false")
    Optional<Tag> findByNameIgnoreCase(@Param("name") String name);

    /**
     * Check if slug exists.
     * 
     * @param slug the slug to check
     * @return true if slug exists
     */
    @Query("SELECT COUNT(t) > 0 FROM Tag t WHERE t.slug = :slug AND t.deleted = false")
    boolean existsBySlug(@Param("slug") String slug);

    /**
     * Check if name exists (case insensitive).
     * 
     * @param name the name to check
     * @return true if name exists
     */
    @Query("SELECT COUNT(t) > 0 FROM Tag t WHERE LOWER(t.name) = LOWER(:name) AND t.deleted = false")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    /**
     * Check slug existence excluding specific tag.
     * 
     * @param slug the slug to check
     * @param excludeId the tag ID to exclude
     * @return true if slug exists for other tags
     */
    @Query("SELECT COUNT(t) > 0 FROM Tag t WHERE t.slug = :slug AND t.id != :excludeId AND t.deleted = false")
    boolean existsBySlugAndIdNot(@Param("slug") String slug, @Param("excludeId") Long excludeId);

    // ==================== Usage Count Queries ====================

    /**
     * Find tags by usage count range.
     * 
     * @param minCount minimum usage count
     * @param maxCount maximum usage count
     * @return list of tags in the usage count range
     */
    @Query("SELECT t FROM Tag t WHERE t.usageCount BETWEEN :minCount AND :maxCount AND t.deleted = false ORDER BY t.usageCount DESC")
    List<Tag> findByUsageCountBetween(@Param("minCount") Integer minCount, @Param("maxCount") Integer maxCount);

    /**
     * Find tags with usage count greater than threshold.
     * 
     * @param threshold minimum usage count
     * @return list of popular tags
     */
    @Query("SELECT t FROM Tag t WHERE t.usageCount > :threshold AND t.deleted = false ORDER BY t.usageCount DESC")
    List<Tag> findByUsageCountGreaterThan(@Param("threshold") Integer threshold);

    /**
     * Find unused tags.
     * 
     * @return list of tags with zero usage count
     */
    @Query("SELECT t FROM Tag t WHERE t.usageCount = 0 AND t.deleted = false ORDER BY t.createdAt DESC")
    List<Tag> findUnusedTags();

    /**
     * Increment usage count for tag.
     * 
     * @param tagId the tag ID
     * @return number of updated rows
     */
    @Modifying
    @Transactional
    @Query("UPDATE Tag t SET t.usageCount = t.usageCount + 1 WHERE t.id = :tagId AND t.deleted = false")
    int incrementUsageCount(@Param("tagId") Long tagId);

    /**
     * Decrement usage count for tag (minimum 0).
     * 
     * @param tagId the tag ID
     * @return number of updated rows
     */
    @Modifying
    @Transactional
    @Query("UPDATE Tag t SET t.usageCount = CASE WHEN t.usageCount > 0 THEN t.usageCount - 1 ELSE 0 END WHERE t.id = :tagId AND t.deleted = false")
    int decrementUsageCount(@Param("tagId") Long tagId);

    /**
     * Update usage count to specific value.
     * 
     * @param tagId the tag ID
     * @param usageCount the new usage count
     * @return number of updated rows
     */
    @Modifying
    @Transactional
    @Query("UPDATE Tag t SET t.usageCount = :usageCount WHERE t.id = :tagId AND t.deleted = false")
    int updateUsageCount(@Param("tagId") Long tagId, @Param("usageCount") Integer usageCount);

    // ==================== Popular Tags Queries ====================

    /**
     * Find most popular tags.
     * 
     * @param pageable pagination information
     * @return page of tags ordered by usage count
     */
    @Query("SELECT t FROM Tag t WHERE t.deleted = false ORDER BY t.usageCount DESC")
    Page<Tag> findMostPopular(Pageable pageable);

    /**
     * Find popular tags with minimum usage.
     * 
     * @param minUsage minimum usage count
     * @param pageable pagination information
     * @return page of popular tags
     */
    @Query("SELECT t FROM Tag t WHERE t.usageCount >= :minUsage AND t.deleted = false ORDER BY t.usageCount DESC")
    Page<Tag> findPopularTags(@Param("minUsage") Integer minUsage, Pageable pageable);

    /**
     * Get tag cloud data.
     * 
     * @return list of [Tag, usageCount] arrays for tag cloud
     */
    @Query("SELECT t, t.usageCount FROM Tag t WHERE t.deleted = false AND t.usageCount > 0 ORDER BY t.usageCount DESC")
    List<Object[]> getTagCloudData();

    /**
     * Get tag cloud data with limit.
     * 
     * @param limit maximum number of tags
     * @return list of [Tag, usageCount] arrays for tag cloud
     */
    @Query(value = "SELECT * FROM (SELECT t.*, t.usage_count FROM tags t WHERE t.deleted = false AND t.usage_count > 0 ORDER BY t.usage_count DESC) LIMIT :limit", nativeQuery = true)
    List<Object[]> getTagCloudData(@Param("limit") Integer limit);

    // ==================== Search Queries ====================

    /**
     * Search tags by name.
     * 
     * @param searchTerm the search term
     * @return list of matching tags
     */
    @Query("SELECT t FROM Tag t WHERE t.deleted = false AND LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY t.usageCount DESC")
    List<Tag> searchByName(@Param("searchTerm") String searchTerm);

    /**
     * Search tags by name with pagination.
     * 
     * @param searchTerm the search term
     * @param pageable pagination information
     * @return page of matching tags
     */
    @Query("SELECT t FROM Tag t WHERE t.deleted = false AND LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Tag> searchByName(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Search tags by name or description.
     * 
     * @param searchTerm the search term
     * @return list of matching tags
     */
    @Query("SELECT t FROM Tag t WHERE t.deleted = false AND (LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) ORDER BY t.usageCount DESC")
    List<Tag> searchByNameOrDescription(@Param("searchTerm") String searchTerm);

    /**
     * Search tags by name or description with pagination.
     * 
     * @param searchTerm the search term
     * @param pageable pagination information
     * @return page of matching tags
     */
    @Query("SELECT t FROM Tag t WHERE t.deleted = false AND (LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Tag> searchByNameOrDescription(@Param("searchTerm") String searchTerm, Pageable pageable);

    // ==================== Blog Post Association Queries ====================

    /**
     * Find tags by blog post ID.
     * 
     * @param blogPostId the blog post ID
     * @return list of tags associated with the blog post
     */
    @Query("SELECT t FROM Tag t JOIN t.blogPosts bp WHERE bp.id = :blogPostId AND t.deleted = false AND bp.deleted = false")
    List<Tag> findByBlogPostId(@Param("blogPostId") Long blogPostId);

    /**
     * Find tags used in published posts.
     * 
     * @return list of tags used in published posts
     */
    @Query("SELECT DISTINCT t FROM Tag t JOIN t.blogPosts bp WHERE bp.status = 'PUBLISHED' AND t.deleted = false AND bp.deleted = false ORDER BY t.usageCount DESC")
    List<Tag> findTagsUsedInPublishedPosts();

    /**
     * Find tags used in date range.
     * 
     * @param startDate start date
     * @param endDate end date
     * @return list of tags used in the date range
     */
    @Query("SELECT DISTINCT t FROM Tag t JOIN t.blogPosts bp WHERE bp.publishedDate BETWEEN :startDate AND :endDate AND t.deleted = false AND bp.deleted = false")
    List<Tag> findTagsUsedInDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Count posts using tag.
     * 
     * @param tagId the tag ID
     * @return number of posts using the tag
     */
    @Query("SELECT COUNT(bp) FROM BlogPost bp JOIN bp.tags t WHERE t.id = :tagId AND bp.deleted = false")
    long countPostsUsingTag(@Param("tagId") Long tagId);

    /**
     * Count published posts using tag.
     * 
     * @param tagId the tag ID
     * @return number of published posts using the tag
     */
    @Query("SELECT COUNT(bp) FROM BlogPost bp JOIN bp.tags t WHERE t.id = :tagId AND bp.status = 'PUBLISHED' AND bp.deleted = false")
    long countPublishedPostsUsingTag(@Param("tagId") Long tagId);

    // ==================== Statistics Queries ====================

    /**
     * Get tag usage statistics.
     * 
     * @return list of [Tag, usageCount, actualPostCount] arrays
     */
    @Query("SELECT t, t.usageCount, COUNT(bp) FROM Tag t LEFT JOIN t.blogPosts bp WHERE t.deleted = false AND (bp.deleted = false OR bp IS NULL) GROUP BY t ORDER BY t.usageCount DESC")
    List<Object[]> getTagUsageStatistics();

    /**
     * Find tags with discrepant usage counts.
     * 
     * @return list of tags where usage count doesn't match actual post count
     */
    @Query("SELECT t FROM Tag t LEFT JOIN t.blogPosts bp WHERE t.deleted = false AND (bp.deleted = false OR bp IS NULL) GROUP BY t HAVING t.usageCount != COUNT(bp)")
    List<Tag> findTagsWithDiscrepantUsageCounts();

    /**
     * Recalculate usage counts for all tags.
     * 
     * @return number of updated tags
     */
    @Modifying
    @Transactional
    @Query("UPDATE Tag t SET t.usageCount = (SELECT COUNT(bp) FROM BlogPost bp JOIN bp.tags t2 WHERE t2.id = t.id AND bp.deleted = false) WHERE t.deleted = false")
    int recalculateUsageCounts();

    // ==================== Sorting Queries ====================

    /**
     * Find all tags ordered by name.
     * 
     * @return list of tags ordered by name
     */
    @Query("SELECT t FROM Tag t WHERE t.deleted = false ORDER BY t.name ASC")
    List<Tag> findAllByOrderByNameAsc();

    /**
     * Find all tags ordered by usage count.
     * 
     * @return list of tags ordered by usage count descending
     */
    @Query("SELECT t FROM Tag t WHERE t.deleted = false ORDER BY t.usageCount DESC")
    List<Tag> findAllByOrderByUsageCountDesc();

    // ==================== Bulk Operations ====================

    /**
     * Bulk update color codes.
     * 
     * @param tagIds list of tag IDs
     * @param colorCode new color code
     * @return number of updated tags
     */
    @Modifying
    @Transactional
    @Query("UPDATE Tag t SET t.colorCode = :colorCode WHERE t.id IN :tagIds AND t.deleted = false")
    int bulkUpdateColorCode(@Param("tagIds") List<Long> tagIds, @Param("colorCode") String colorCode);

    /**
     * Merge tags (move all posts from source to target tag).
     * 
     * @param sourceTagId source tag ID
     * @param targetTagId target tag ID
     * @return number of posts moved
     */
    @Modifying
    @Transactional
    @Query(value = """
        UPDATE blog_post_tags SET tag_id = :targetTagId 
        WHERE tag_id = :sourceTagId AND blog_post_id NOT IN (
            SELECT blog_post_id FROM blog_post_tags WHERE tag_id = :targetTagId
        )
        """, nativeQuery = true)
    int mergeTags(@Param("sourceTagId") Long sourceTagId, @Param("targetTagId") Long targetTagId);

    /**
     * Cleanup unused tags.
     * 
     * @return number of deleted tags
     */
    @Modifying
    @Transactional
    @Query("UPDATE Tag t SET t.deleted = true WHERE t.usageCount = 0 AND t.deleted = false")
    int cleanupUnusedTags();
}