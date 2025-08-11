package com.personalblog.entity;

/**
 * Enumeration representing the different statuses a comment can have
 * in the moderation workflow.
 * 
 * The comment moderation system follows this flow:
 * 1. PENDING - New comments await moderation
 * 2. APPROVED - Comments approved for public display
 * 3. REJECTED - Comments rejected but kept for audit
 * 4. SPAM - Comments marked as spam for filtering
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 */
public enum CommentStatus {
    
    /**
     * Comment is pending moderation approval.
     * This is the default status for new comments.
     */
    PENDING("Pending", "Comment is awaiting moderation"),
    
    /**
     * Comment has been approved and is visible to public.
     */
    APPROVED("Approved", "Comment is approved and visible"),
    
    /**
     * Comment has been rejected by moderator.
     * Kept for audit purposes but not displayed.
     */
    REJECTED("Rejected", "Comment has been rejected"),
    
    /**
     * Comment has been marked as spam.
     * Used for spam filtering and analytics.
     */
    SPAM("Spam", "Comment has been marked as spam");
    
    private final String displayName;
    private final String description;
    
    /**
     * Constructor for CommentStatus enum.
     * 
     * @param displayName The human-readable name for the status
     * @param description The description of what this status means
     */
    CommentStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * Gets the display name for this status.
     * 
     * @return The human-readable display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the description for this status.
     * 
     * @return The description of what this status means
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Checks if this status allows the comment to be publicly visible.
     * 
     * @return true if the comment should be displayed to users
     */
    public boolean isVisible() {
        return this == APPROVED;
    }
    
    /**
     * Checks if this status indicates the comment is awaiting action.
     * 
     * @return true if the comment needs moderation
     */
    public boolean isPending() {
        return this == PENDING;
    }
    
    /**
     * Checks if this status indicates the comment has been processed.
     * 
     * @return true if the comment has been moderated (approved, rejected, or marked as spam)
     */
    public boolean isProcessed() {
        return this != PENDING;
    }
    
    /**
     * Gets all statuses that allow comments to be displayed.
     * 
     * @return Array of visible comment statuses
     */
    public static CommentStatus[] getVisibleStatuses() {
        return new CommentStatus[]{APPROVED};
    }
    
    /**
     * Gets all statuses that indicate the comment needs moderation.
     * 
     * @return Array of pending comment statuses
     */
    public static CommentStatus[] getPendingStatuses() {
        return new CommentStatus[]{PENDING};
    }
    
    /**
     * Converts a string to CommentStatus enum (case-insensitive).
     * 
     * @param status The status string to convert
     * @return The corresponding CommentStatus enum
     * @throws IllegalArgumentException if the status is not recognized
     */
    public static CommentStatus fromString(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment status cannot be null or empty");
        }
        
        try {
            return valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown comment status: " + status);
        }
    }
    
    /**
     * Converts a display name to CommentStatus enum (case-insensitive).
     * 
     * @param displayName The display name to convert
     * @return The corresponding CommentStatus enum
     * @throws IllegalArgumentException if the display name is not recognized
     */
    public static CommentStatus fromDisplayName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment status display name cannot be null or empty");
        }
        
        for (CommentStatus status : values()) {
            if (status.displayName.equalsIgnoreCase(displayName.trim())) {
                return status;
            }
        }
        
        throw new IllegalArgumentException("Unknown comment status display name: " + displayName);
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}