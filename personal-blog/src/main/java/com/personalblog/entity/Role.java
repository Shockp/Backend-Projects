package com.personalblog.entity;

/**
 * Enumeration of user roles in the personal blog application.
 * 
 * Roles define the level of access and permissions a user has:
 * - USER: Basic user who can read blog posts and comments
 * - AUTHOR: User who can write and manage their own blog posts
 * - ADMIN: Administrator with full access to all features
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 2025-08-04
 */
public enum Role {
    
    /**
     * Basic user role.
     * Can read blog posts, leave comments.
     */
    USER("User"),

    /**
     * Author role.
     * Can create, edit, and delete their own blog posts.
     * Includes all USER permissions.
     */
    AUTHOR("Author"),

    /**
     * Administrator role.
     * Full access to all blog features including user management.
     * Includes all USER and AUTHOR permissions.
     */
    ADMIN("Administrator");

    private final String displayName;

    /**
     * Constructor for Role enum.
     * 
     * @param displayName human-readable name for the role
     */
    Role(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the human-readable display name for the role.
     * 
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the authority name used by Spring Security.
     * 
     * @return the authority name ("ROLE_ENUMNAME")
     */
    public String getAuthority() {
        return "ROLE_" + this.name();
    }

    /**
     * Checks if this role has higher or equal privileges than the
     * specified role.
     * 
     * @param other the role to compare against
     * @return true if this role has higher or equal privileges
     */
    public boolean hasHigherOrEqualPrivileges(Role other) {
        return this.ordinal() >= other.ordinal();
    }
}
