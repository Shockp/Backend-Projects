package com.personalblog.repository.projection;

import java.time.LocalDateTime;

/**
 * Projection interface for Tag data optimized for tag cloud generation.
 * 
 * <p>This projection provides tag information with usage statistics specifically
 * designed for tag cloud components, popular tags widgets, and tag-based navigation.
 * It includes usage counts and relative popularity metrics for visual representation.</p>
 * 
 * <p>Usage examples:</p>
 * <pre>{@code
 * // In repository method for tag cloud
 * @Query("SELECT t.id as id, t.name as name, t.slug as slug, " +
 *        "t.usageCount as usageCount, t.colorCode as colorCode, " +
 *        "COUNT(bp) as actualPostCount, " +
 *        "ROUND((t.usageCount * 100.0 / (SELECT MAX(t2.usageCount) FROM Tag t2)), 2) as relativePopularity " +
 *        "FROM Tag t LEFT JOIN t.blogPosts bp " +
 *        "WHERE t.deleted = false AND t.usageCount > 0 " +
 *        "AND (bp.deleted = false AND bp.status = 'PUBLISHED' OR bp IS NULL) " +
 *        "GROUP BY t ORDER BY t.usageCount DESC")
 * List<TagCloudItem> findTagCloudData(@Param("limit") int limit);
 * 
 * // For popular tags widget
 * @Query("SELECT t.id as id, t.name as name, t.slug as slug, " +
 *        "t.usageCount as usageCount, t.description as description " +
 *        "FROM Tag t WHERE t.deleted = false AND t.usageCount >= :minUsage " +
 *        "ORDER BY t.usageCount DESC")
 * List<TagCloudItem> findPopularTags(@Param("minUsage") int minUsage, Pageable pageable);
 * }</pre>
 * 
 * <p>Benefits:</p>
 * <ul>
 *   <li>Optimized for tag cloud rendering with minimal data transfer</li>
 *   <li>Includes popularity metrics for visual scaling</li>
 *   <li>Supports color-coded tag displays</li>
 *   <li>Efficient loading for tag-based navigation components</li>
 *   <li>Type-safe access to usage statistics</li>
 * </ul>
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 * @see com.personalblog.entity.Tag
 */
public interface TagCloudItem {

    /**
     * Gets the tag ID.
     * 
     * @return the unique identifier of the tag
     */
    Long getId();

    /**
     * Gets the tag name.
     * 
     * @return the display name of the tag
     */
    String getName();

    /**
     * Gets the tag slug.
     * 
     * @return the URL-friendly slug for SEO routing
     */
    String getSlug();

    /**
     * Gets the usage count.
     * 
     * @return number of times this tag has been used
     */
    Integer getUsageCount();

    /**
     * Gets the actual post count.
     * 
     * @return actual number of published posts using this tag
     */
    Long getActualPostCount();

    /**
     * Gets the relative popularity percentage.
     * 
     * @return popularity as percentage relative to most popular tag (0-100)
     */
    Double getRelativePopularity();

    /**
     * Gets the color code for visual representation.
     * 
     * @return hex color code for tag display (may be null)
     */
    String getColorCode();

    /**
     * Gets the tag description.
     * 
     * @return description of the tag (may be null)
     */
    String getDescription();

    /**
     * Gets the tag creation timestamp.
     * 
     * @return when the tag was created
     */
    LocalDateTime getCreatedAt();

    /**
     * Gets the last update timestamp.
     * 
     * @return when the tag was last updated
     */
    LocalDateTime getUpdatedAt();

    /**
     * Calculates the font size scale for tag cloud display.
     * 
     * @param minSize minimum font size
     * @param maxSize maximum font size
     * @return calculated font size based on relative popularity
     */
    default double calculateFontSize(double minSize, double maxSize) {
        Double popularity = getRelativePopularity();
        if (popularity == null || popularity <= 0) {
            return minSize;
        }
        return minSize + (maxSize - minSize) * (popularity / 100.0);
    }

    /**
     * Gets the CSS class name based on popularity.
     * 
     * @return CSS class name for styling (tag-small, tag-medium, tag-large, tag-xl)
     */
    default String getCssClass() {
        Double popularity = getRelativePopularity();
        if (popularity == null) {
            return "tag-small";
        }
        if (popularity >= 80) {
            return "tag-xl";
        } else if (popularity >= 60) {
            return "tag-large";
        } else if (popularity >= 30) {
            return "tag-medium";
        } else {
            return "tag-small";
        }
    }

    /**
     * Checks if the tag is popular.
     * 
     * @param threshold popularity threshold (default 50%)
     * @return true if tag popularity is above threshold
     */
    default boolean isPopular(double threshold) {
        Double popularity = getRelativePopularity();
        return popularity != null && popularity >= threshold;
    }

    /**
     * Checks if the tag is popular (using default 50% threshold).
     * 
     * @return true if tag popularity is above 50%
     */
    default boolean isPopular() {
        return isPopular(50.0);
    }

    /**
     * Gets the display weight for tag cloud algorithms.
     * 
     * @return weight value for tag cloud positioning (1-10 scale)
     */
    default int getDisplayWeight() {
        Double popularity = getRelativePopularity();
        if (popularity == null) {
            return 1;
        }
        return Math.max(1, Math.min(10, (int) Math.ceil(popularity / 10.0)));
    }

    /**
     * Checks if usage count matches actual post count.
     * 
     * @return true if counts are synchronized
     */
    default boolean isUsageCountAccurate() {
        Integer usage = getUsageCount();
        Long actual = getActualPostCount();
        return usage != null && actual != null && usage.equals(actual.intValue());
    }
}