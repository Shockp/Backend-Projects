package com.personalblog.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tag entity representing blog post tags for content organization and discovery.
 * 
 * Features:
 * - Name and slug (URL-friendly)
 * - Description for tag context
 * - Color code for UI display
 * - Usage count for popularity tracking
 * - Many-to-Many relationship with BlogPost
 * - Database indexes for performance
 * - Caching for frequently accessed tags
 * 
 * Inherits audit fields (id, createdAt, updatedAt, version) from BaseEntity.
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "tags", indexes = {
    @Index(name = "idx_tag_name", columnList = "name"),
    @Index(name = "idx_tag_slug", columnList = "slug"),
    @Index(name = "idx_tag_usage_count", columnList = "usage_count")
})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Tag extends BaseEntity {
    
    // ==================== Core Fields ====================

    /**
     * Tag name for display purposes.
     * Must be unique and between 2-50 characters.
     */
    @NotBlank(message = "Tag name is required")
    @Size(min = 2, max = 50, message = "Tag name must be between 2 and 50 characters")
    @Column(name = "name", nullable = false, length = 50, unique = true)
    private String name;

    /**
     * URL-friendly slug for the tag.
     * Used in URLs and must be unique.
     */
    @NotBlank(message = "Tag slug is required")
    @Size(min = 2, max = 100, message = "Tag slug must be between 2 and 100 characters")
    @Pattern(
        regexp = "^[a-z0-9-]+$",
        message = "Tag slug must contain only lowercase letters, numbers, and hyphens"
    )
    @Column(name = "slug", nullable = false, length = 100, unique = true)
    private String slug;

    /**
     * Optional description providing context about the tag.
     */
    @Size(max = 500, message = "Tag description must be at most 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Hex color code for UI display (e.g., #FF5733).
     * Used for tag styling in the frontend.
     */
    @Pattern(
        regexp = "^#[0-9A-Fa-f]{6}$",
        message = "Color code must be a valid hex color (e.g., #FF5733)"
    )
    @Column(name = "color_code", length = 7)
    private String colorCode;

    /**
     * Number of times this tag has been used.
     * Automatically updated when tags are assigned/removed from posts.
     * Uses AtomicInteger for thread-safe operations in high-concurrency scenarios.
     */
    @Min(value = 0, message = "Usage count cannot be negative")
    @Column(name = "usage_count", nullable = false)
    private volatile Integer usageCount = 0;
    
    /**
     * Transient atomic wrapper for thread-safe usage count operations.
     * Initialized lazily to avoid serialization issues.
     */
    @Transient
    private transient AtomicInteger atomicUsageCount;

    // ==================== Relationships ====================

    /**
     * Blog posts associated with this tag.
     * Many-to-Many relationship managed by BlogPost entity.
     */
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<BlogPost> blogPosts = new HashSet<>();

    // ==================== Constructors ====================

    /**
     * Default constructor for JPA.
     */
    public Tag() {
        super();
    }

    /**
     * Constructor with required fields.
     * 
     * @param name The tag name
     * @param slug The URL-friendly slug
     */
    public Tag(String name, String slug) {
        this();
        this.name = name;
        this.slug = slug;
        initializeAtomicCounter();
    }

    /**
     * Constructor with all main fields.
     * 
     * @param name The tag name
     * @param slug The URL-friendly slug
     * @param description The tag description
     * @param colorCode The hex color code
     */
    public Tag(String name, String slug, String description, String colorCode) {
        this(name, slug);
        this.description = description;
        this.colorCode = colorCode;
        initializeAtomicCounter();
    }

    // ==================== Getters and Setters ====================

    /**
     * Gets the tag name.
     * 
     * @return The tag name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the tag name.
     * 
     * @param name The tag name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the tag slug.
     * 
     * @return The URL-friendly slug
     */
    public String getSlug() {
        return slug;
    }

    /**
     * Sets the tag slug.
     * 
     * @param slug The URL-friendly slug to set
     */
    public void setSlug(String slug) {
        this.slug = slug;
    }

    /**
     * Gets the tag description.
     * 
     * @return The tag description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the tag description.
     * 
     * @param description The tag description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the color code.
     * 
     * @return The hex color code
     */
    public String getColorCode() {
        return colorCode;
    }

    /**
     * Sets the color code.
     * 
     * @param colorCode The hex color code to set
     */
    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    /**
     * Gets the usage count.
     * 
     * @return The number of times this tag has been used
     */
    public Integer getUsageCount() {
        return usageCount;
    }

    /**
     * Sets the usage count.
     * Thread-safe operation that updates both the persistent field and atomic counter.
     * 
     * @param usageCount The usage count to set
     */
    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount != null ? usageCount : 0;
        initializeAtomicCounter();
    }

    /**
     * Gets the associated blog posts.
     * 
     * @return Set of blog posts using this tag
     */
    public Set<BlogPost> getBlogPosts() {
        return blogPosts;
    }

    /**
     * Sets the associated blog posts.
     * 
     * @param blogPosts The set of blog posts to associate
     */
    public void setBlogPosts(Set<BlogPost> blogPosts) {
        this.blogPosts = blogPosts;
    }

    // ==================== Business Methods ====================

    /**
     * Initializes the atomic counter with the current usage count value.
     * Called during construction and when setting usage count.
     */
    private void initializeAtomicCounter() {
        if (this.atomicUsageCount == null) {
            this.atomicUsageCount = new AtomicInteger(this.usageCount != null ? this.usageCount : 0);
        } else {
            this.atomicUsageCount.set(this.usageCount != null ? this.usageCount : 0);
        }
    }

    /**
     * Ensures atomic counter is initialized before use.
     * Handles lazy initialization for entities loaded from database.
     */
    private void ensureAtomicCounterInitialized() {
        if (this.atomicUsageCount == null) {
            initializeAtomicCounter();
        }
    }

    /**
     * Increments the usage count when tag is assigned to a post.
     * Thread-safe operation using atomic increment.
     * 
     * @return The new usage count value
     */
    public int incrementUsageCount() {
        ensureAtomicCounterInitialized();
        int newValue = this.atomicUsageCount.incrementAndGet();
        this.usageCount = newValue;
        return newValue;
    }

    /**
     * Decrements the usage count when tag is removed from a post.
     * Thread-safe operation that ensures count doesn't go below zero.
     * 
     * @return The new usage count value
     */
    public int decrementUsageCount() {
        ensureAtomicCounterInitialized();
        int newValue = this.atomicUsageCount.updateAndGet(current -> Math.max(0, current - 1));
        this.usageCount = newValue;
        return newValue;
    }

    /**
     * Atomically sets the usage count to a specific value.
     * Thread-safe operation for bulk updates.
     * 
     * @param newCount The new usage count (must be non-negative)
     * @return The previous usage count value
     * @throws IllegalArgumentException if newCount is negative
     */
    public int setUsageCountAtomic(int newCount) {
        if (newCount < 0) {
            throw new IllegalArgumentException("Usage count cannot be negative");
        }
        ensureAtomicCounterInitialized();
        int previousValue = this.atomicUsageCount.getAndSet(newCount);
        this.usageCount = newCount;
        return previousValue;
    }

    /**
     * Atomically adds a delta to the usage count.
     * Thread-safe operation for bulk increments/decrements.
     * 
     * @param delta The amount to add (can be negative for decrement)
     * @return The new usage count value
     */
    public int addToUsageCount(int delta) {
        ensureAtomicCounterInitialized();
        int newValue = this.atomicUsageCount.updateAndGet(current -> Math.max(0, current + delta));
        this.usageCount = newValue;
        return newValue;
    }

    /**
     * Checks if this tag is currently in use (has associated blog posts).
     * 
     * @return true if tag is used by at least one blog post
     */
    public boolean isInUse() {
        return usageCount > 0;
    }

    /**
     * Gets the display name with usage count for admin interfaces.
     * 
     * @return Formatted string with name and usage count
     */
    public String getDisplayNameWithCount() {
        return String.format("%s (%d)", name, usageCount);
    }

    // ==================== Object Methods ====================

    /**
     * Compares this tag with another object for equality.
     * Two tags are equal if they have the same slug.
     * 
     * @param obj The object to compare with
     * @return true if objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Tag tag = (Tag) obj;
        return Objects.equals(slug, tag.slug);
    }

    /**
     * Generates hash code based on the slug.
     * 
     * @return Hash code for this tag
     */
    @Override
    public int hashCode() {
        return Objects.hash(slug);
    }

    /**
     * Returns a string representation of this tag.
     * 
     * @return String representation including key fields
     */
    @Override
    public String toString() {
        return String.format(
            "Tag{id=%d, name='%s', slug='%s', usageCount=%d, createdAt=%s}",
            getId(),
            name,
            slug,
            usageCount,
            getCreatedAt()
        );
    }
}