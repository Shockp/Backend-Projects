package com.personalblog.repository.projection;

import com.personalblog.entity.BlogPost;

import java.time.LocalDateTime;

/**
 * Projection interface for BlogPost summary data.
 * 
 * <p>This projection provides a lightweight view of blog post data optimized for
 * read-only operations such as listing posts, search results, and dashboard views.
 * It includes only essential fields to minimize data transfer and improve performance.</p>
 * 
 * <p>Usage examples:</p>
 * <pre>{@code
 * // In repository method
 * @Query("SELECT bp.id as id, bp.title as title, bp.slug as slug, " +
 *        "bp.status as status, bp.createdAt as createdAt, bp.publishedDate as publishedDate, " +
 *        "bp.viewCount as viewCount, bp.readingTimeMinutes as readingTimeMinutes, " +
 *        "bp.excerpt as excerpt, bp.featuredImageUrl as featuredImageUrl, " +
 *        "a.username as authorUsername, a.displayName as authorDisplayName, " +
 *        "c.name as categoryName, c.slug as categorySlug " +
 *        "FROM BlogPost bp JOIN bp.author a JOIN bp.category c " +
 *        "WHERE bp.status = 'PUBLISHED' AND bp.deleted = false")
 * Page<BlogPostSummary> findPublishedPostSummaries(Pageable pageable);
 * }</pre>
 * 
 * <p>Benefits:</p>
 * <ul>
 *   <li>Reduced memory footprint compared to full entity loading</li>
 *   <li>Faster query execution with selective field loading</li>
 *   <li>Prevents accidental lazy loading of relationships</li>
 *   <li>Type-safe access to projected fields</li>
 *   <li>Optimized for JSON serialization in REST APIs</li>
 * </ul>
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 * @see BlogPost
 */
public interface BlogPostSummary {

    /**
     * Gets the blog post ID.
     * 
     * @return the unique identifier of the blog post
     */
    Long getId();

    /**
     * Gets the blog post title.
     * 
     * @return the title of the blog post
     */
    String getTitle();

    /**
     * Gets the blog post slug.
     * 
     * @return the URL-friendly slug for SEO routing
     */
    String getSlug();

    /**
     * Gets the blog post status.
     * 
     * @return the current status (DRAFT, PUBLISHED, ARCHIVED)
     */
    BlogPost.Status getStatus();

    /**
     * Gets the creation timestamp.
     * 
     * @return when the blog post was created
     */
    LocalDateTime getCreatedAt();

    /**
     * Gets the published date.
     * 
     * @return when the blog post was published (null for drafts)
     */
    LocalDateTime getPublishedDate();

    /**
     * Gets the view count.
     * 
     * @return number of times the post has been viewed
     */
    Integer getViewCount();

    /**
     * Gets the estimated reading time.
     * 
     * @return estimated reading time in minutes
     */
    Integer getReadingTimeMinutes();

    /**
     * Gets the post excerpt.
     * 
     * @return short summary or excerpt of the post
     */
    String getExcerpt();

    /**
     * Gets the featured image URL.
     * 
     * @return URL of the featured image (null if none)
     */
    String getFeaturedImageUrl();

    /**
     * Gets the author username.
     * 
     * @return username of the post author
     */
    String getAuthorUsername();

    /**
     * Gets the author display name.
     * 
     * @return display name of the post author
     */
    String getAuthorDisplayName();

    /**
     * Gets the category name.
     * 
     * @return name of the post category
     */
    String getCategoryName();

    /**
     * Gets the category slug.
     * 
     * @return URL-friendly slug of the post category
     */
    String getCategorySlug();
}