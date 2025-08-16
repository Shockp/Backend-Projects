package com.personalblog.repository;

import com.personalblog.entity.Category;
import com.personalblog.repository.projection.CategoryWithCount;
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
 * Repository interface for Category entity operations.
 * Provides comprehensive data access methods for category management
 * with hierarchical support, SEO optimization, and performance enhancements.
 * 
 * Features:
 * - Hierarchical category operations (parent-child relationships)
 * - Slug-based queries for SEO-friendly URLs
 * - Display order management for custom sorting
 * - Blog post statistics and associations
 * - Search capabilities across names and descriptions
 * - Bulk operations for administrative tasks
 * - Performance-optimized queries with proper indexing
 * - Soft delete support inherited from BaseRepository
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface CategoryRepository extends BaseRepository<Category, Long> {

    // ==================== Basic Queries ====================

    /**
     * Find category by slug.
     * 
     * @param slug the URL-friendly slug
     * @return optional category if found and not deleted
     */
    @Query("SELECT c FROM Category c WHERE c.slug = :slug AND c.deleted = false")
    Optional<Category> findBySlug(@Param("slug") String slug);

    /**
     * Check if slug exists.
     * 
     * @param slug the slug to check
     * @return true if slug exists
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.slug = :slug AND c.deleted = false")
    boolean existsBySlug(@Param("slug") String slug);

    /**
     * Check slug existence excluding specific category.
     * 
     * @param slug      the slug to check
     * @param excludeId the category ID to exclude
     * @return true if slug exists for other categories
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.slug = :slug AND c.id != :excludeId AND c.deleted = false")
    boolean existsBySlugAndIdNot(@Param("slug") String slug, @Param("excludeId") Long excludeId);

    // ==================== Hierarchical Queries ====================

    /**
     * Find root categories (categories without parent).
     * 
     * @return list of root categories ordered by display order
     */
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.deleted = false ORDER BY c.displayOrder ASC")
    List<Category> findRootCategories();

    /**
     * Find root categories with pagination.
     * 
     * @param pageable pagination information
     * @return page of root categories
     */
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.deleted = false")
    Page<Category> findRootCategories(Pageable pageable);

    /**
     * Find children by parent ID.
     * 
     * @param parentId the parent category ID
     * @return list of child categories ordered by display order
     */
    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.deleted = false ORDER BY c.displayOrder ASC")
    List<Category> findByParentId(@Param("parentId") Long parentId);

    /**
     * Find children by parent ID with pagination.
     * 
     * @param parentId the parent category ID
     * @param pageable pagination information
     * @return page of child categories
     */
    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.deleted = false")
    Page<Category> findByParentId(@Param("parentId") Long parentId, Pageable pageable);

    /**
     * Find category hierarchy from root to specified category.
     * 
     * @param categoryId the category ID
     * @return list of categories from root to specified category
     */
    @Query(value = """
            WITH RECURSIVE category_hierarchy AS (
                SELECT id, name, slug, parent_id, 0 as level
                FROM categories
                WHERE id = :categoryId AND deleted = false
                UNION ALL
                SELECT c.id, c.name, c.slug, c.parent_id, ch.level + 1
                FROM categories c
                INNER JOIN category_hierarchy ch ON c.id = ch.parent_id
                WHERE c.deleted = false
            )
            SELECT * FROM category_hierarchy ORDER BY level DESC
            """, nativeQuery = true)
    List<Category> findCategoryHierarchy(@Param("categoryId") Long categoryId);

    /**
     * Find all descendants of a category.
     * 
     * @param parentId the parent category ID
     * @return list of all descendant categories
     */
    @Query(value = """
            WITH RECURSIVE category_descendants AS (
                SELECT id, name, slug, parent_id
                FROM categories
                WHERE parent_id = :parentId AND deleted = false
                UNION ALL
                SELECT c.id, c.name, c.slug, c.parent_id
                FROM categories c
                INNER JOIN category_descendants cd ON c.parent_id = cd.id
                WHERE c.deleted = false
            )
            SELECT * FROM category_descendants
            """, nativeQuery = true)
    List<Category> findAllDescendants(@Param("parentId") Long parentId);

    /**
     * Check if category has children.
     * 
     * @param categoryId the category ID
     * @return true if category has children
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.parent.id = :categoryId AND c.deleted = false")
    boolean hasChildren(@Param("categoryId") Long categoryId);

    /**
     * Count children of category.
     * 
     * @param categoryId the category ID
     * @return number of child categories
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.parent.id = :categoryId AND c.deleted = false")
    long countChildren(@Param("categoryId") Long categoryId);

    // ==================== Display Order Queries ====================

    /**
     * Find all categories ordered by display order.
     * 
     * @return list of categories ordered by display order
     */
    @Query("SELECT c FROM Category c WHERE c.deleted = false ORDER BY c.displayOrder ASC")
    List<Category> findAllByOrderByDisplayOrderAsc();

    /**
     * Find categories by display order range.
     * 
     * @param minOrder minimum display order
     * @param maxOrder maximum display order
     * @return list of categories in the range
     */
    @Query("SELECT c FROM Category c WHERE c.displayOrder BETWEEN :minOrder AND :maxOrder AND c.deleted = false ORDER BY c.displayOrder ASC")
    List<Category> findByDisplayOrderBetween(@Param("minOrder") Integer minOrder, @Param("maxOrder") Integer maxOrder);

    /**
     * Find next available display order.
     * 
     * @return next display order value
     */
    @Query("SELECT COALESCE(MAX(c.displayOrder), 0) + 1 FROM Category c WHERE c.deleted = false")
    Integer findNextDisplayOrder();

    /**
     * Find maximum display order for parent.
     * 
     * @param parentId the parent category ID
     * @return maximum display order among children
     */
    @Query("SELECT COALESCE(MAX(c.displayOrder), 0) FROM Category c WHERE c.parent.id = :parentId AND c.deleted = false")
    Integer findMaxDisplayOrderByParent(@Param("parentId") Long parentId);

    // ==================== Blog Post Statistics ====================

    /**
     * Find categories with post counts.
     * 
     * @return list of [Category, postCount] arrays
     */
    @Query("SELECT c, COUNT(bp) FROM Category c LEFT JOIN c.blogPosts bp WHERE c.deleted = false AND (bp.deleted = false OR bp IS NULL) GROUP BY c")
    List<Object[]> findCategoriesWithPostCounts();

    /**
     * Find categories with published post counts.
     * 
     * @return list of [Category, publishedPostCount] arrays
     */
    @Query("SELECT c, COUNT(bp) FROM Category c LEFT JOIN c.blogPosts bp WHERE c.deleted = false AND (bp.deleted = false AND bp.status = 'PUBLISHED' OR bp IS NULL) GROUP BY c")
    List<Object[]> findCategoriesWithPublishedPostCounts();

    /**
     * Count posts in category.
     * 
     * @param categoryId the category ID
     * @return number of posts in category
     */
    @Query("SELECT COUNT(bp) FROM BlogPost bp WHERE bp.category.id = :categoryId AND bp.deleted = false")
    long countPostsInCategory(@Param("categoryId") Long categoryId);

    /**
     * Count published posts in category.
     * 
     * @param categoryId the category ID
     * @return number of published posts in category
     */
    @Query("SELECT COUNT(bp) FROM BlogPost bp WHERE bp.category.id = :categoryId AND bp.status = 'PUBLISHED' AND bp.deleted = false")
    long countPublishedPostsInCategory(@Param("categoryId") Long categoryId);

    /**
     * Find popular categories by post count.
     * 
     * @param pageable pagination information
     * @return page of categories ordered by post count
     */
    @Query("SELECT c FROM Category c LEFT JOIN c.blogPosts bp WHERE c.deleted = false AND (bp.deleted = false OR bp IS NULL) GROUP BY c ORDER BY COUNT(bp) DESC")
    Page<Category> findPopularCategoriesByPostCount(Pageable pageable);

    /**
     * Find categories used in date range.
     * 
     * @param startDate start date
     * @param endDate   end date
     * @return list of categories used in the date range
     */
    @Query("SELECT DISTINCT c FROM Category c JOIN c.blogPosts bp WHERE c.deleted = false AND bp.deleted = false AND bp.publishedDate BETWEEN :startDate AND :endDate")
    List<Category> findCategoriesUsedInDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // ==================== Search Queries ====================

    /**
     * Search categories by name.
     * 
     * @param searchTerm the search term
     * @return list of matching categories
     */
    @Query("SELECT c FROM Category c WHERE c.deleted = false AND LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Category> searchByName(@Param("searchTerm") String searchTerm);

    /**
     * Search categories by name with pagination.
     * 
     * @param searchTerm the search term
     * @param pageable   pagination information
     * @return page of matching categories
     */
    @Query("SELECT c FROM Category c WHERE c.deleted = false AND LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Category> searchByName(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Search categories by name or description.
     * 
     * @param searchTerm the search term
     * @return list of matching categories
     */
    @Query("SELECT c FROM Category c WHERE c.deleted = false AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Category> searchByNameOrDescription(@Param("searchTerm") String searchTerm);

    // ==================== SEO Queries ====================

    /**
     * Find categories missing SEO metadata.
     * 
     * @return list of categories needing SEO optimization
     */
    @Query("SELECT c FROM Category c WHERE c.deleted = false AND (c.metaTitle IS NULL OR c.metaTitle = '' OR c.metaDescription IS NULL OR c.metaDescription = '')")
    List<Category> findCategoriesNeedingSeoOptimization();

    // ==================== Bulk Operations ====================

    /**
     * Update display order for category.
     * 
     * @param categoryId   the category ID
     * @param displayOrder the new display order
     * @return number of updated rows
     */
    @Modifying
    @Transactional
    @Query("UPDATE Category c SET c.displayOrder = :displayOrder WHERE c.id = :categoryId AND c.deleted = false")
    int updateDisplayOrder(@Param("categoryId") Long categoryId, @Param("displayOrder") Integer displayOrder);

    /**
     * Reorder categories by parent.
     * 
     * @param parentId the parent category ID
     * @return number of updated rows
     */
    @Modifying
    @Transactional
    @Query("UPDATE Category c SET c.displayOrder = (SELECT COUNT(c2) FROM Category c2 WHERE c2.parent.id = :parentId AND c2.id <= c.id AND c2.deleted = false) WHERE c.parent.id = :parentId AND c.deleted = false")
    int reorderCategoriesByParent(@Param("parentId") Long parentId);

    /**
     * Soft delete category and all its children.
     * 
     * @param categoryId the category ID
     * @return number of deleted categories
     */
    @Modifying
    @Transactional
    @Query(value = """
            WITH RECURSIVE category_tree AS (
                SELECT id FROM categories WHERE id = :categoryId
                UNION ALL
                SELECT c.id FROM categories c
                INNER JOIN category_tree ct ON c.parent_id = ct.id
            )
            UPDATE categories SET deleted = true, updated_at = CURRENT_TIMESTAMP
            WHERE id IN (SELECT id FROM category_tree) AND deleted = false
            """, nativeQuery = true)
    int softDeleteCategoryAndChildren(@Param("categoryId") Long categoryId);

    // ==================== Projection Methods ====================

    /**
     * Find all categories with post counts for navigation menus.
     * Uses projection for optimized performance.
     * 
     * @return list of categories with post count statistics
     */
    @Query("SELECT c.id as id, c.name as name, c.slug as slug, " +
           "c.description as description, c.displayOrder as displayOrder, " +
           "c.parent.id as parentId, c.parent.name as parentName, " +
           "c.createdAt as createdAt, c.updatedAt as updatedAt, " +
           "c.metaTitle as metaTitle, c.metaDescription as metaDescription, " +
           "COUNT(bp) as totalPostCount, " +
           "SUM(CASE WHEN bp.status = 'PUBLISHED' THEN 1 ELSE 0 END) as publishedPostCount " +
           "FROM Category c LEFT JOIN c.blogPosts bp " +
           "WHERE c.deleted = false AND (bp.deleted = false OR bp IS NULL) " +
           "GROUP BY c.id, c.name, c.slug, c.description, c.displayOrder, c.parent.id, c.parent.name, " +
           "c.createdAt, c.updatedAt, c.metaTitle, c.metaDescription " +
           "ORDER BY c.displayOrder")
    List<CategoryWithCount> findCategoriesWithCounts();

    /**
     * Find root categories with post counts.
     * 
     * @return list of root categories with post count statistics
     */
    @Query("SELECT c.id as id, c.name as name, c.slug as slug, " +
           "c.description as description, c.displayOrder as displayOrder, " +
           "c.parent.id as parentId, c.parent.name as parentName, " +
           "c.createdAt as createdAt, c.updatedAt as updatedAt, " +
           "c.metaTitle as metaTitle, c.metaDescription as metaDescription, " +
           "COUNT(bp) as totalPostCount, " +
           "SUM(CASE WHEN bp.status = 'PUBLISHED' THEN 1 ELSE 0 END) as publishedPostCount " +
           "FROM Category c LEFT JOIN c.blogPosts bp " +
           "WHERE c.parent IS NULL AND c.deleted = false AND (bp.deleted = false OR bp IS NULL) " +
           "GROUP BY c.id, c.name, c.slug, c.description, c.displayOrder, c.parent.id, c.parent.name, " +
           "c.createdAt, c.updatedAt, c.metaTitle, c.metaDescription " +
           "ORDER BY c.displayOrder")
    List<CategoryWithCount> findRootCategoriesWithCounts();

    /**
     * Find child categories with post counts by parent ID.
     * 
     * @param parentId the parent category ID
     * @return list of child categories with post count statistics
     */
    @Query("SELECT c.id as id, c.name as name, c.slug as slug, " +
           "c.description as description, c.displayOrder as displayOrder, " +
           "c.parent.id as parentId, c.parent.name as parentName, " +
           "c.createdAt as createdAt, c.updatedAt as updatedAt, " +
           "c.metaTitle as metaTitle, c.metaDescription as metaDescription, " +
           "COUNT(bp) as totalPostCount, " +
           "SUM(CASE WHEN bp.status = 'PUBLISHED' THEN 1 ELSE 0 END) as publishedPostCount " +
           "FROM Category c LEFT JOIN c.blogPosts bp " +
           "WHERE c.parent.id = :parentId AND c.deleted = false AND (bp.deleted = false OR bp IS NULL) " +
           "GROUP BY c.id, c.name, c.slug, c.description, c.displayOrder, c.parent.id, c.parent.name, " +
           "c.createdAt, c.updatedAt, c.metaTitle, c.metaDescription " +
           "ORDER BY c.displayOrder")
    List<CategoryWithCount> findChildCategoriesWithCounts(@Param("parentId") Long parentId);

    /**
     * Find categories with post counts, paginated.
     * 
     * @param pageable pagination information
     * @return page of categories with post count statistics
     */
    @Query("SELECT c.id as id, c.name as name, c.slug as slug, " +
           "c.description as description, c.displayOrder as displayOrder, " +
           "c.parent.id as parentId, c.parent.name as parentName, " +
           "c.createdAt as createdAt, c.updatedAt as updatedAt, " +
           "c.metaTitle as metaTitle, c.metaDescription as metaDescription, " +
           "COUNT(bp) as totalPostCount, " +
           "SUM(CASE WHEN bp.status = 'PUBLISHED' THEN 1 ELSE 0 END) as publishedPostCount " +
           "FROM Category c LEFT JOIN c.blogPosts bp " +
           "WHERE c.deleted = false AND (bp.deleted = false OR bp IS NULL) " +
           "GROUP BY c.id, c.name, c.slug, c.description, c.displayOrder, c.parent.id, c.parent.name, " +
           "c.createdAt, c.updatedAt, c.metaTitle, c.metaDescription")
    Page<CategoryWithCount> findCategoriesWithCounts(Pageable pageable);

    /**
     * Find popular categories with post counts ordered by published post count.
     * 
     * @param pageable pagination information
     * @return page of popular categories with post count statistics
     */
    @Query("SELECT c.id as id, c.name as name, c.slug as slug, " +
           "c.description as description, c.displayOrder as displayOrder, " +
           "c.parent.id as parentId, c.parent.name as parentName, " +
           "c.createdAt as createdAt, c.updatedAt as updatedAt, " +
           "c.metaTitle as metaTitle, c.metaDescription as metaDescription, " +
           "COUNT(bp) as totalPostCount, " +
           "SUM(CASE WHEN bp.status = 'PUBLISHED' THEN 1 ELSE 0 END) as publishedPostCount " +
           "FROM Category c LEFT JOIN c.blogPosts bp " +
           "WHERE c.deleted = false AND (bp.deleted = false OR bp IS NULL) " +
           "GROUP BY c.id, c.name, c.slug, c.description, c.displayOrder, c.parent.id, c.parent.name, " +
           "c.createdAt, c.updatedAt, c.metaTitle, c.metaDescription " +
           "ORDER BY publishedPostCount DESC")
    Page<CategoryWithCount> findPopularCategoriesWithCounts(Pageable pageable);

    /**
     * Search categories with post counts by name.
     * 
     * @param searchTerm the search term
     * @param pageable pagination information
     * @return page of matching categories with post count statistics
     */
    @Query("SELECT c.id as id, c.name as name, c.slug as slug, " +
           "c.description as description, c.displayOrder as displayOrder, " +
           "c.parent.id as parentId, c.parent.name as parentName, " +
           "c.createdAt as createdAt, c.updatedAt as updatedAt, " +
           "c.metaTitle as metaTitle, c.metaDescription as metaDescription, " +
           "COUNT(bp) as totalPostCount, " +
           "SUM(CASE WHEN bp.status = 'PUBLISHED' THEN 1 ELSE 0 END) as publishedPostCount " +
           "FROM Category c LEFT JOIN c.blogPosts bp " +
           "WHERE c.deleted = false AND (bp.deleted = false OR bp IS NULL) " +
           "AND LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "GROUP BY c.id, c.name, c.slug, c.description, c.displayOrder, c.parent.id, c.parent.name, " +
           "c.createdAt, c.updatedAt, c.metaTitle, c.metaDescription " +
           "ORDER BY c.displayOrder")
    Page<CategoryWithCount> searchCategoriesWithCounts(@Param("searchTerm") String searchTerm, Pageable pageable);
}