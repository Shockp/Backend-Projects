package com.personalblog.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Comment entity representing user comments on blog posts.
 * 
 * Features:
 * - Content with validation
 * - Author information (registered user or guest)
 * - Comment status for moderation
 * - Hierarchical structure (replies to comments)
 * - IP address and user agent tracking for security
 * - Blog post relationship
 * - Moderation workflow support
 * 
 * Inherits audit fields (id, createdAt, updatedAt, version) from BaseEntity.
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "comments", indexes = {
    @Index(name = "idx_comment_post_id", columnList = "blog_post_id"),
    @Index(name = "idx_comment_status", columnList = "status"),
    @Index(name = "idx_comment_author_user_id", columnList = "author_user_id"),
    @Index(name = "idx_comment_parent_id", columnList = "parent_comment_id"),
    @Index(name = "idx_comment_created_at", columnList = "created_at")
})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Comment extends BaseEntity {
    
    // ==================== Constants ====================
    
    private static final int CONTENT_MIN_LENGTH = 1;
    private static final int CONTENT_MAX_LENGTH = 2000;
    private static final int AUTHOR_NAME_MAX_LENGTH = 100;
    private static final int AUTHOR_EMAIL_MAX_LENGTH = 255;
    private static final int AUTHOR_WEBSITE_MAX_LENGTH = 500;
    private static final int IP_ADDRESS_MAX_LENGTH = 45;
    private static final int USER_AGENT_MAX_LENGTH = 500;
    
    // ==================== Core Fields ====================

    /**
     * Comment content.
     * Required field with length validation.
     */
    @NotBlank(message = "Comment content is required")
    @Size(min = CONTENT_MIN_LENGTH, max = CONTENT_MAX_LENGTH, 
          message = "Comment content must be between {min} and {max} characters")
    @Pattern(regexp = "^[\\p{L}0-9 .,'\"!?()\\-:;\\n\\r\\t]*$",
             message = "Comment content contains invalid characters")
    @Column(name = "content", nullable = false, length = CONTENT_MAX_LENGTH)
    private String content;

    /**
     * Comment status for moderation workflow.
     * Defaults to PENDING for new comments.
     */
    @NotNull(message = "Comment status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CommentStatus status = CommentStatus.PENDING;

    // ==================== Author Information ====================

    /**
     * Registered user who authored this comment.
     * Optional - null for guest comments.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_id", foreignKey = @ForeignKey(name = "fk_comment_author_user"))
    private User authorUser;

    /**
     * Guest author name for non-registered users.
     * Required when authorUser is null.
     */
    @Size(max = AUTHOR_NAME_MAX_LENGTH, 
          message = "Author name must not exceed {max} characters")
    @Pattern(regexp = "^[\\p{L}0-9 .,'\"\\-]*$",
             message = "Author name contains invalid characters")
    @Column(name = "author_name", length = AUTHOR_NAME_MAX_LENGTH)
    private String authorName;

    /**
     * Guest author email for non-registered users.
     * Required when authorUser is null.
     */
    @Email(message = "Author email must be valid")
    @Size(max = AUTHOR_EMAIL_MAX_LENGTH, 
          message = "Author email must not exceed {max} characters")
    @Column(name = "author_email", length = AUTHOR_EMAIL_MAX_LENGTH)
    private String authorEmail;

    /**
     * Optional website URL for guest authors.
     */
    @Size(max = AUTHOR_WEBSITE_MAX_LENGTH, 
          message = "Author website must not exceed {max} characters")
    @Pattern(regexp = "^$|^https?://[\\w\\-]+(\\.[\\w\\-]+)+([\\w\\-\\.,@?^=%&:/~\\+#]*[\\w\\-\\@?^=%&/~\\+#])?$",
             message = "Author website must be a valid HTTP/HTTPS URL or empty")
    @Column(name = "author_website", length = AUTHOR_WEBSITE_MAX_LENGTH)
    private String authorWebsite;

    // ==================== Relationships ====================

    /**
     * Blog post this comment belongs to.
     * Required relationship.
     */
    @NotNull(message = "Blog post is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blog_post_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_comment_blog_post"))
    private BlogPost blogPost;

    /**
     * Parent comment for reply structure.
     * Null for top-level comments.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id", 
                foreignKey = @ForeignKey(name = "fk_comment_parent"))
    private Comment parentComment;

    /**
     * Child replies to this comment.
     * Cascade operations for reply management.
     */
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<Comment> replies = new ArrayList<>();

    // ==================== Security & Tracking ====================

    /**
     * IP address of the comment author for security tracking.
     * Stored for moderation and spam prevention.
     */
    @Size(max = IP_ADDRESS_MAX_LENGTH, 
          message = "IP address must not exceed {max} characters")
    @Pattern(regexp = "^$|^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$",
             message = "IP address must be a valid IPv4 or IPv6 address")
    @Column(name = "ip_address", length = IP_ADDRESS_MAX_LENGTH)
    private String ipAddress;

    /**
     * User agent string for browser/device identification.
     * Used for analytics and security purposes.
     */
    @Size(max = USER_AGENT_MAX_LENGTH, 
          message = "User agent must not exceed {max} characters")
    @Column(name = "user_agent", length = USER_AGENT_MAX_LENGTH)
    private String userAgent;

    // ==================== Constructors ====================

    /**
     * Default constructor for JPA.
     */
    public Comment() {
        super();
        this.replies = new ArrayList<>();
    }

    /**
     * Constructor for registered user comments.
     * 
     * @param content The comment content
     * @param authorUser The registered user author
     * @param blogPost The blog post being commented on
     */
    public Comment(String content, User authorUser, BlogPost blogPost) {
        this();
        this.content = content;
        this.authorUser = authorUser;
        this.blogPost = blogPost;
    }

    /**
     * Constructor for guest comments.
     * 
     * @param content The comment content
     * @param authorName The guest author name
     * @param authorEmail The guest author email
     * @param blogPost The blog post being commented on
     */
    public Comment(String content, String authorName, String authorEmail, BlogPost blogPost) {
        this();
        this.content = content;
        this.authorName = authorName;
        this.authorEmail = authorEmail;
        this.blogPost = blogPost;
    }

    // ==================== Getters and Setters ====================

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public CommentStatus getStatus() {
        return status;
    }

    public void setStatus(CommentStatus status) {
        this.status = status;
    }

    public User getAuthorUser() {
        return authorUser;
    }

    public void setAuthorUser(User authorUser) {
        this.authorUser = authorUser;
        // Clear guest fields when setting registered user
        if (authorUser != null) {
            this.authorName = null;
            this.authorEmail = null;
            this.authorWebsite = null;
        }
    }

    public String getAuthorName() {
        if (authorUser != null) {
            return authorUser.getUsername();
        }
        return authorName != null ? authorName : "Anonymous";
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public String getAuthorWebsite() {
        return authorWebsite;
    }

    public void setAuthorWebsite(String authorWebsite) {
        this.authorWebsite = authorWebsite;
    }

    public BlogPost getBlogPost() {
        return blogPost;
    }

    public void setBlogPost(BlogPost blogPost) {
        this.blogPost = blogPost;
    }

    public Comment getParentComment() {
        return parentComment;
    }

    public void setParentComment(Comment parentComment) {
        this.parentComment = parentComment;
    }

    public List<Comment> getReplies() {
        return replies;
    }

    public void setReplies(List<Comment> replies) {
        this.replies = replies != null ? replies : new ArrayList<>();
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    // ==================== Business Methods ====================

    /**
     * Checks if this comment is approved.
     * 
     * @return true if the comment status is APPROVED
     */
    public boolean isApproved() {
        return CommentStatus.APPROVED.equals(this.status);
    }

    /**
     * Checks if this comment is from a registered user.
     * 
     * @return true if authorUser is not null
     */
    public boolean isFromRegisteredUser() {
        return this.authorUser != null;
    }

    /**
     * Checks if this comment is a reply to another comment.
     * 
     * @return true if parentComment is not null
     */
    public boolean isReply() {
        return this.parentComment != null;
    }

    /**
     * Checks if this comment has replies.
     * 
     * @return true if replies list is not empty
     */
    public boolean hasReplies() {
        return this.replies != null && !this.replies.isEmpty();
    }

    /**
     * Gets the display name for the comment author.
     * Returns the registered user's display name or guest name.
     * 
     * @return The author display name
     */
    public String getAuthorDisplayName() {
        if (this.authorUser != null) {
            return this.authorUser.getUsername();
        }
        return this.authorName != null ? this.authorName : "Anonymous";
    }

    /**
     * Gets the author email for display or notifications.
     * Returns the registered user's email or guest email.
     * 
     * @return The author email
     */
    public String getAuthorDisplayEmail() {
        if (this.authorUser != null) {
            return this.authorUser.getEmail();
        }
        return this.authorEmail;
    }

    /**
     * Adds a reply to this comment.
     * Establishes bidirectional relationship.
     * 
     * @param reply The reply comment to add
     */
    public void addReply(Comment reply) {
        if (reply != null) {
            this.replies.add(reply);
            reply.setParentComment(this);
            reply.setBlogPost(this.blogPost); // Ensure reply has same blog post
        }
    }

    /**
     * Removes a reply from this comment.
     * Clears bidirectional relationship.
     * 
     * @param reply The reply comment to remove
     */
    public void removeReply(Comment reply) {
        if (reply != null && this.replies.contains(reply)) {
            this.replies.remove(reply);
            reply.setParentComment(null);
        }
    }

    /**
     * Approves this comment.
     * Sets status to APPROVED.
     */
    public void approve() {
        this.status = CommentStatus.APPROVED;
    }

    /**
     * Rejects this comment.
     * Sets status to REJECTED.
     */
    public void reject() {
        this.status = CommentStatus.REJECTED;
    }

    /**
     * Marks this comment as spam.
     * Sets status to SPAM.
     */
    public void markAsSpam() {
        this.status = CommentStatus.SPAM;
    }

    /**
     * Gets the depth level of this comment in the reply hierarchy.
     * Top-level comments have depth 0, replies have depth 1, etc.
     * 
     * @return The comment depth level
     */
    public int getDepthLevel() {
        int depth = 0;
        Comment current = this.parentComment;
        while (current != null) {
            depth++;
            current = current.getParentComment();
        }
        return depth;
    }

    /**
     * Gets the root comment of this comment thread.
     * Returns this comment if it's already a root comment.
     * 
     * @return The root comment in the hierarchy
     */
    public Comment getRootComment() {
        Comment current = this;
        while (current.getParentComment() != null) {
            current = current.getParentComment();
        }
        return current;
    }

    /**
     * Gets the total number of replies in this comment's subtree.
     * 
     * @return The total count of all nested replies
     */
    public int getTotalReplyCount() {
        int count = this.replies.size();
        for (Comment reply : this.replies) {
            count += reply.getTotalReplyCount();
        }
        return count;
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Comment comment = (Comment) obj;
        return Objects.equals(getId(), comment.getId()) && getId() != null;
    }

    @Override
    public int hashCode() {
        return getId() != null ? Objects.hash(getId()) : 31;
    }

    @Override
    public String toString() {
        return String.format(
            "Comment{id=%s, content='%s', status=%s, authorUser=%s, authorName='%s', " +
            "blogPost=%s, parentComment=%s, repliesCount=%d, createdAt=%s}",
            getId(),
            content != null ? (content.length() > 50 ? content.substring(0, 50) + "..." : content) : null,
            status,
            authorUser != null ? authorUser.getUsername() : null,
            authorName,
            blogPost != null ? blogPost.getTitle() : null,
            parentComment != null ? parentComment.getId() : null,
            replies != null ? replies.size() : 0,
            getCreatedAt()
        );
    }

    // ==================== Validation Methods ====================

    /**
     * Validates that either authorUser or guest author information is provided.
     * Called before persist/update operations.
     */
    @PrePersist
    @PreUpdate
    private void validateAuthorInformation() {
        if (authorUser == null && (authorName == null || authorName.trim().isEmpty())) {
            throw new IllegalStateException("Either authorUser or authorName must be provided");
        }
        if (authorUser == null && (authorEmail == null || authorEmail.trim().isEmpty())) {
            throw new IllegalStateException("Guest comments must have an email address");
        }
    }

    /**
     * Validates comment hierarchy to prevent circular references.
     * Called before persist/update operations.
     */
    @PrePersist
    @PreUpdate
    private void validateCommentHierarchy() {
        if (parentComment != null) {
            // Prevent self-reference
            if (Objects.equals(getId(), parentComment.getId())) {
                throw new IllegalStateException("Comment cannot be a reply to itself");
            }
            
            // Prevent circular references
            Comment current = parentComment;
            while (current != null) {
                if (Objects.equals(getId(), current.getId())) {
                    throw new IllegalStateException("Circular reference detected in comment hierarchy");
                }
                current = current.getParentComment();
            }
            
            // Ensure reply belongs to same blog post
            if (blogPost != null && parentComment.getBlogPost() != null && 
                !Objects.equals(blogPost.getId(), parentComment.getBlogPost().getId())) {
                throw new IllegalStateException("Reply must belong to the same blog post as parent comment");
            }
        }
    }

    /**
     * Validates content for security and quality.
     * Called before persist/update operations.
     */
    @PrePersist
    @PreUpdate
    private void validateContent() {
        if (content != null) {
            // Check for potential XSS patterns
            String lowerContent = content.toLowerCase();
            if (lowerContent.contains("<script") || lowerContent.contains("javascript:") || 
                lowerContent.contains("<iframe") || lowerContent.contains("onclick=") ||
                lowerContent.contains("onerror=") || lowerContent.contains("onload=")) {
                throw new IllegalStateException("Comment content contains potentially malicious code");
            }
        }
    }
}