package com.personalblog.repository.projection;

import java.time.LocalDateTime;

/**
 * Projection interface for Category data with post count statistics.
 * 
 * <p>This projection provides category information along with associated post counts,
 * optimized for navigation menus, category listings, and administrative dashboards.
 * It includes both total and published post counts for comprehensive statistics.</p>
 * 
 * <p>Usage examples:</p>
 * <pre>{@code
 * // In repository method for navigation menu
 * @Query("SELECT c.id as id, c.name as name, c.slug as slug, " +
 *        "c.description as description, c.displayOrder as displayOrder, " +
 *        "c.parent.id as parentId, c.parent.name as parentName, " +
 *        "COUNT(bp) as totalPostCount, " +
 *        "SUM(CASE WHEN bp.status = 'PUBLISHED' THEN 1 ELSE 0 END) as publishedPostCount " +
 *        "FROM Category c LEFT JOIN c.blogPosts bp " +
 *        "WHERE c.deleted = false AND (bp.deleted = false OR bp IS NULL) " +
 *        "GROUP BY c ORDER BY c.displayOrder")
 * List<CategoryWithCount> findCategoriesWithCounts();
 * 
 * // For hierarchical category tree
 * @Query("SELECT c.id as id, c.name as name, c.slug as slug, " +
 *        "c.parent.id as parentId, COUNT(bp) as publishedPostCount " +
 *        "FROM Category c LEFT JOIN c.blogPosts bp " +
 *        "WHERE c.deleted = false AND (bp.status = 'PUBLISHED' OR bp IS NULL) " +
 *        "GROUP BY c ORDER BY c.displayOrder")
 * List<CategoryWithCount> findCategoryTreeWithPublishedCounts();
 * }</pre>
 * 
 * <p>Benefits:</p>
 * <ul>
 *   <li>Efficient loading of category data with statistics in single query</li>
 *   <li>Prevents N+1 queries when displaying category lists with counts</li>
 *   <li>Optimized for navigation components and admin interfaces</li>
 *   <li>Supports hierarchical category structures</li>
 *   <li>Type-safe access to aggregated data</li>
 * </ul>
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 * @see com.personalblog.entity.Category
 */
public interface CategoryWithCount {

    /**
     * Gets the category ID.
     * 
     * @return the unique identifier of the category
     */
    Long getId();

    /**
     * Gets the category name.
     * 
     * @return the display name of the category
     */
    String getName();

    /**
     * Gets the category slug.
     * 
     * @return the URL-friendly slug for SEO routing
     */
    String getSlug();

    /**
     * Gets the category description.
     * 
     * @return the description of the category (may be null)
     */
    String getDescription();

    /**
     * Gets the display order.
     * 
     * @return the sort order for displaying categories
     */
    Integer getDisplayOrder();

    /**
     * Gets the parent category ID.
     * 
     * @return the ID of the parent category (null for root categories)
     */
    Long getParentId();

    /**
     * Gets the parent category name.
     * 
     * @return the name of the parent category (null for root categories)
     */
    String getParentName();

    /**
     * Gets the total post count.
     * 
     * @return total number of posts in this category (all statuses)
     */
    Long getTotalPostCount();

    /**
     * Gets the published post count.
     * 
     * @return number of published posts in this category
     */
    Long getPublishedPostCount();

    /**
     * Gets the category creation timestamp.
     * 
     * @return when the category was created
     */
    LocalDateTime getCreatedAt();

    /**
     * Gets the last update timestamp.
     * 
     * @return when the category was last updated
     */
    LocalDateTime getUpdatedAt();

    /**
     * Gets the meta title for SEO.
     * 
     * @return the SEO meta title (may be null)
     */
    String getMetaTitle();

    /**
     * Gets the meta description for SEO.
     * 
     * @return the SEO meta description (may be null)
     */
    String getMetaDescription();

    /**
     * Checks if the category has child categories.
     * 
     * @return true if this category has children
     */
    default boolean hasChildren() {
        return getTotalPostCount() != null && getTotalPostCount() > 0;
    }

    /**
     * Checks if the category has published posts.
     * 
     * @return true if this category has published posts
     */
    default boolean hasPublishedPosts() {
        return getPublishedPostCount() != null && getPublishedPostCount() > 0;
    }

    /**
     * Gets the draft post count.
     * 
     * @return number of draft posts (calculated from total - published)
     */
    default Long getDraftPostCount() {
        Long total = getTotalPostCount();
        Long published = getPublishedPostCount();
        if (total == null || published == null) {
            return 0L;
        }
        return total - published;
    }
}