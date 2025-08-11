package com.personalblog.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * BlogPost entity representing a blog post with rich content,
 * metadata, and relationships to author, category, tags and comments.
 * 
 * Features:
 * - Title and slug (URL-friendly)
 * - Rich text content
 * - Excerpt/summary
 * - Publication status (DRAFT, PUBLISHED, ARCHIVED)
 * - Publication date
 * - Featured image URL
 * - SEO metadata (meta title, description, keywords)
 * - View count
 * - Estimated reading time
 * - Relationships: author, category, tags, comments
 * - Database indexes and caching for performance
 * 
 * Inherits audit fields (id, createdAt, updatedAt, deleted) from BaseEntity.
 * 
 * Validation groups:
 *  Create.class, Update.class, Publish.class
 * 
 * Named queries:
 *  findPublishedByDateDesc
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0a
 * @since 1.0.0
 */
@Entity
@Table(name = "blog_posts", indexes = {
    @Index(name = "idx_blogpost_title", columnList = "title"),
    @Index(name = "idx_blogpost_slug", columnList = "slug"),
    @Index(name = "idx_blogpost_status", columnList = "status"),
    @Index(name = "idx_blogpost_published_date", columnList = "published_date"),
    @Index(name = "idx_blogpost_status_published_date", columnList = "status, published_date")
})
@NamedQuery(
    name = "BlogPost.findPublishedByDateDesc",
    query = "SELECT b FROM BlogPost b WHERE b.status = :status ORDER BY b.publishedDate DESC"
)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "blogPosts")
public class BlogPost extends BaseEntity {

    // ==================== Validation Constants ====================
    
    public static final int TITLE_MIN_LENGTH = 3;
    public static final int TITLE_MAX_LENGTH = 100;
    public static final int CONTENT_MIN_LENGTH = 50;
    public static final int CONTENT_MAX_LENGTH = 50000;
    public static final int EXCERPT_MAX_LENGTH = 500;
    public static final int SLUG_MAX_LENGTH = 200;
    public static final int FEATURED_IMAGE_URL_MAX_LENGTH = 500;
    public static final int META_TITLE_MAX_LENGTH = 70;
    public static final int META_DESCRIPTION_MAX_LENGTH = 160;
    public static final int META_KEYWORDS_MAX_LENGTH = 255;

    public interface Create {}
    public interface Update {}
    public interface Publish {}
    
    public enum Status {
        DRAFT,
        PUBLISHED,
        ARCHIVED
    }

    // ==================== Core Fields ====================

    @NotBlank(groups = Create.class)
    @Size(min = TITLE_MIN_LENGTH, max = TITLE_MAX_LENGTH, groups = {Create.class, Update.class})
    @Pattern(
        regexp = "^[\\p{L}0-9 .,'\"!?()\\-:;]{3,100}$",
        message = "Title must be 3–100 characters, letters, numbers, punctuation, spaces only",
        groups = {Create.class, Update.class}
    )
    @Column(name = "title", nullable = false, length = TITLE_MAX_LENGTH)
    private String title;

    @NotBlank(groups = {Create.class, Publish.class})
    @Size(max = SLUG_MAX_LENGTH, groups = {Create.class, Update.class})
    @Pattern(
        regexp = "^[a-z0-9\\-]+$",
        message = "Slug must be lowercase alphanumeric and hyphens",
        groups = {Create.class, Update.class}
    )
    @Column(name = "slug", nullable = false, unique = true, length = SLUG_MAX_LENGTH)
    private String slug;

    @NotBlank(groups = Create.class)
    @Lob
    @Size(min = CONTENT_MIN_LENGTH, max = CONTENT_MAX_LENGTH, groups = {Create.class, Update.class})
    @Pattern(
        regexp = "^(?!.*<script)(?!.*<iframe)(?!.*javascript:)(?!.*on\\w+\\s*=)[\\s\\S]*$",
        message = "Content cannot contain script tags, iframes, javascript protocols, or event handlers",
        groups = {Create.class, Update.class}
    )
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Size(max = EXCERPT_MAX_LENGTH, groups = {Create.class, Update.class})
    @Pattern(
        regexp = "^[\\p{L}0-9 .,'\"!?()\\-:;\\n\\r]*$",
        message = "Excerpt must contain only letters, numbers, punctuation, spaces, and line breaks",
        groups = {Create.class, Update.class}
    )
    @Column(name = "excerpt", length = EXCERPT_MAX_LENGTH)
    private String excerpt;

    @NotNull(groups = {Create.class, Update.class})
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.DRAFT;

    // ==================== Publication & Media ====================

    @NotNull(groups = Publish.class)
    @Column(name = "published_date")
    private LocalDateTime publishedDate;

    @Size(max = FEATURED_IMAGE_URL_MAX_LENGTH, groups = {Create.class, Update.class})
    @Pattern(
        regexp = "^$|^https?://[\\w\\-]+(\\.[\\w\\-]+)+([\\w\\-\\.,@?^=%&:/~\\+#]*[\\w\\-\\@?^=%&/~\\+#])?$",
        message = "Must be a valid HTTP/HTTPS URL or empty",
        groups = {Create.class, Update.class}
    )
    @Column(name = "featured_image_url", length = FEATURED_IMAGE_URL_MAX_LENGTH)
    private String featuredImageUrl;

    // ==================== SEO Metadata ====================

    @Size(max = META_TITLE_MAX_LENGTH, groups = {Create.class, Update.class})
    @Column(name = "meta_title", length = META_TITLE_MAX_LENGTH)
    private String metaTitle;

    @Size(max = META_DESCRIPTION_MAX_LENGTH, groups = {Create.class, Update.class})
    @Column(name = "meta_description", length = META_DESCRIPTION_MAX_LENGTH)
    private String metaDescription;

    @Size(max = META_KEYWORDS_MAX_LENGTH, groups = {Create.class, Update.class})
    @Column(name = "meta_keywords", length = META_KEYWORDS_MAX_LENGTH)
    private String metaKeywords;

     // ==================== Statistics ====================

    @Min(value = 0, groups = Update.class)
    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    /**
     * Atomic counter for thread-safe view count operations.
     * Transient field that mirrors the persistent viewCount field.
     */
    @Transient
    private transient AtomicLong atomicViewCount;

    @Min(value = 0, groups = Update.class)
    @Column(name = "reading_time_minutes", nullable = false)
    private Integer readingTimeMinutes = 0;

     // ==================== Relationships ====================

     @NotNull(groups = {Create.class, Update.class})
     @ManyToOne(fetch = FetchType.LAZY, optional = false)
     @JoinColumn(name = "author_id", nullable = false)
     private User author;

     @NotNull(groups = {Create.class, Update.class})
     @ManyToOne(fetch = FetchType.LAZY, optional = false)
     @JoinColumn(name = "category_id", nullable = false)
     private Category category;

     @ManyToMany(fetch = FetchType.LAZY)
     @JoinTable(
        name = "blog_post_tags",
        joinColumns = @JoinColumn(name = "blog_post_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id"),
        indexes = {
            @Index(name = "idx_blogpost_tag_blogpost", columnList = "blog_post_id"),
            @Index(name = "idx_blogpost_tag_tag", columnList = "tag_id")
        }
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "blogPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

    // ==================== Constructors ====================

    public BlogPost() {
        super();
        initializeAtomicCounter();
    }

    public BlogPost(String title, String slug, String content, User author, Category category) {
        this();
        this.title = title;
        this.slug = slug;
        this.content = content;
        this.author = author;
        this.category = category;
        initializeAtomicCounter();
    }

    public BlogPost(String title, String slug, String content, User author, Category category, Set<Tag> tags) {
        this(title, slug, content, author, category);
        this.tags = tags;
        initializeAtomicCounter();
    }

    // ==================== Getters and Setters ====================

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDateTime publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getFeaturedImageUrl() {
        return featuredImageUrl;
    }

    public void setFeaturedImageUrl(String featuredImageUrl) {
        this.featuredImageUrl = featuredImageUrl;
    }

    public String getMetaTitle() {
        return metaTitle;
    }

    public void setMetaTitle(String metaTitle) {
        this.metaTitle = metaTitle;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    public String getMetaKeywords() {
        return metaKeywords;
    }

    public void setMetaKeywords(String metaKeywords) {
        this.metaKeywords = metaKeywords;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
        if (atomicViewCount != null) {
            atomicViewCount.set(viewCount);
        }
    }

    public Integer getReadingTimeMinutes() {
        return readingTimeMinutes;
    }

    public void setReadingTimeMinutes(Integer readingTimeMinutes) {
        this.readingTimeMinutes = readingTimeMinutes;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public Set<Comment> getComments() {
        return comments;
    }

    public void setComments(Set<Comment> comments) {
        this.comments = comments;
    }

    // ==================== Utility Methods ====================

    /**
     * Increment view count by one.
     */
    public void incrementViewCount() {
        if (atomicViewCount == null) {
            initializeAtomicCounter();
        }
        this.viewCount = atomicViewCount.incrementAndGet();
    }

    /**
     * Initialize the atomic counter with the current view count value.
     */
    private void initializeAtomicCounter() {
        this.atomicViewCount = new AtomicLong(this.viewCount != null ? this.viewCount : 0L);
    }

    /**
     * Calculate reading time based on word count.
     * 
     * @param wordsPerMinute reading speed based on average words per minute
     */
    public void calculateReadingTime(int wordsPerMinute) {
        if (content != null && wordsPerMinute > 0) {
            int wordCount = content.split("\\s+").length;
            this.readingTimeMinutes = (int) Math.ceil((double) wordCount / wordsPerMinute);
        }
    }

    // ==================== Relationship Helper Methods ====================

    /**
     * Add a tag to this blog post.
     * Note: Bidirectional relationship maintenance should be handled at the service layer.
     * 
     * @param tag the tag to add
     */
    public void addTag(Tag tag) {
        if (tag != null) {
            tags.add(tag);
        }
    }

    /**
     * Remove a tag from this blog post.
     * Note: Bidirectional relationship maintenance should be handled at the service layer.
     * 
     * @param tag the tag to remove
     */
    public void removeTag(Tag tag) {
        if (tag != null) {
            tags.remove(tag);
        }
    }

    /**
     * Add a comment to this blog post.
     * Note: Bidirectional relationship maintenance should be handled at the service layer.
     * 
     * @param comment the comment to add
     */
    public void addComment(Comment comment) {
        if (comment != null) {
            comments.add(comment);
        }
    }

    /**
     * Remove a comment from this blog post.
     * Note: Bidirectional relationship maintenance should be handled at the service layer.
     * 
     * @param comment the comment to remove
     */
    public void removeComment(Comment comment) {
        if (comment != null) {
            comments.remove(comment);
        }
    }

    // ==================== Object Methods ====================

    /**
     * Returns a string representation of the blog post.
     * 
     * @return a string representation of the blog post
     */
    @Override
    public String toString() {
        return String.format("BlogPost{id=%d, title='%s', slug='%s', status=%s, publishedAt=%s}",
                             getId(), title, slug, status, publishedDate);
    }
}