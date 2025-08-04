package com.personalblog.entity;

import com.personalblog.entity.Role;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User entity representing blog users with comprehensive profile and
 * security information.
 * 
 * This entity implements Spring Security's UserDetails interface
 * for authentication and includes all necessary fields for user
 * management, profiles, and social features.
 * 
 * Features:
 * - Basic user information (username, email, password)
 * - Security fields (password, roles, account status)
 * - Profile information (bio, avatar, website)
 * - Social media links
 * - Email verification and login traacking
 * - Comprehensive Bean Validation
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 2025-08-03
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_username", columnList = "username"),
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_created_at", columnList = "created_at")
})
public class User extends BaseEntity implements UserDetails {
    
    // ==================== Basic User Information ====================

    /**
     * Unique username for the user.
     * Used for login and public identification.
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Username can only contain letters and numbers")
    @Column(name = "username", nullable = false, unique = true, length = 20)
    private String username;

    /**
     * User's email address.
     * Used for login, notifications, and communication.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 320, message = "Email must be less than 320 characters")
    @Column(name = "email", nullable = false, unique = true, length = 320)
    private String email;

    // ==================== Security Fields ====================

    /**
     * Encrypted password.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * User roles for authorization.
     * Sotred as comma-separated values for simplicity.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "roles", nullable = false)
    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();

    // ==================== Account Status Fields ====================

    /**
     * Whether the user account is enabled.
     * Disabled accounts cannot log in.
     */
    @Column(name = "account_enabled", nullable = false)
    private boolean enabled = Boolean.TRUE;

    /**
     * Whether the user account is locked.
     * Locked accounts cannot log in.
     */
    @Column(name = "account_locked", nullable = false)
    private boolean locked = Boolean.FALSE;

    /**
     * Whether the user account is expired.
     * Expired accounts cannot log in.
     */
    @Column(name = "account_expired", nullable = false)
    private boolean expired = Boolean.FALSE;

    /**
     * Whether the user account credentials are expired.
     * Expired credentials require password reset.
     */
    @Column(name = "credentials_expired", nullable = false)
    private boolean credentialsExpired = Boolean.FALSE;

    // ==================== Profile Information ====================

    /**
     * User's biography or about section.
     */
    @Size(max = 1000, message = "Biography must not exceed 1000 characters")
    @Column(name = "bio", columnDefinition = "TEXT", length = 1000)
    private String bio;

    /**
     * URL or path to user's profile avatar image.
     */
    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    /**
     * User's website or personal blog URL.
     */
    @Size(max = 255, message = "Website URL must not exceed 255 characters")
    @Pattern(
    regexp = "^(https?://)?(www\\.)?[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}([/?#].*)?$|^$",
    message = "Invalid website URL format")
    @Column(name = "website_url", length = 255)
    private String websiteUrl;

    // ==================== Social Media Links ====================

    /**
     * Twitter/X profile URL.
     */
    @Size(max = 255, message = "Twitter/X profile URL must not exceed 255 characters")
    @Pattern(
    regexp = "^(https?://)?(www\\.)?(twitter\\.com|x\\.com)/[A-Za-z0-9_]{1,15}$",
    message = "Invalid Twitter/X profile URL")
    @Column(name = "twitter_url", length = 255)
    private String twitterUrl;

    /**
     * LinkedIn profile URL.
     */
    @Size(max = 255, message = "LinkedIn profile URL must not exceed 255 characters")
    @Pattern(
        regexp = "^(https?://)?(www\\.)?linkedin\\.com/in/[A-Za-z0-9-_%]+/?$",
        message = "Invalid LinkedIn profile URL")
    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;

    /**
     * GitHub profile URL.
     */
    @Size(max = 255, message = "GitHub profile URL must not exceed 255 characters")
    @Pattern(
        regexp = "^(https?://)?(www\\.)?github\\.com/(?=.{1,39}$)(?!-)[A-Za-z0-9-]+(?<!-)$",
        message = "Invalid GitHub profile URL")
    @Column(name = "github_url", length = 255)
    private String githubUrl;

    /**
     * YouTube profile URL.
     */
    @Size(max = 255, message = "YouTube profile URL must not exceed 255 characters")
    @Pattern(
        regexp = "^(https?://)?(www\\.)?youtube\\.com/(channel/UC[\\w-]{22}|user/[\\w-]+|c/[\\w-]+|@\\w+)(/)?$",
        message = "Invalid YouTube profile URL")
    @Column(name = "youtube_url", length = 255)
    private String youtubeUrl;

    /**
     * Instagram profile URL.
     */
    @Size(max = 255, message = "Instagram profile URL must not exceed 255 characters")
    @Pattern(
        regexp = "^(https?://)?(www\\.)?instagram\\.com/(?=.{1,30}$)(?![.])[A-Za-z0-9._]+(?<![.])$",
        message = "Invalid Instagram profile URL")
    @Column(name = "instagram_url", length = 255)
    private String instagramUrl;

    // ==================== Email Verification ====================

    /**
     * Whether the user's email is verified.
     */
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = Boolean.FALSE;

    /**
     * The verification token for the user's email.
     */
    @Size(max = 255, message = "Verification token must not exceed 255 characters")
    @Column(name = "email_verification_token", length = 255)
    private String emailVerificationToken;

    /**
     * The date and time when the user's email was verified.
     */
    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    // ==================== Login Tracking ====================

    /**
     * Timestamp of the user's last successful login.
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * IP adrdress from the user's last login
     */
    @Size(max = 45, message = "IP address must not exceed 45 characters")
    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    /**
     * Number of failed login attempts.
     * Used for account locking after multiple failures.
     */
    @Min(value = 0, message = "Failed login attempts must be non-negative")
    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    // ==================== Password Reset ====================

    /**
     * Token used for password reset.
     * Should be cleared after use.
     */
    @Size(max = 255, message = "Password reset token must not exceed 255 characters")
    @Column(name = "password_reset_token", length = 255)
    private String passwordResetToken;

    /**
     * Date and time when the password reset token expires.
     */
    @Column(name = "password_reset_token_expires_at")
    private LocalDateTime passwordResetTokenExpiresAt;

    // ==================== JPA Relationships ====================

    /**
     * Blog posts authored by this user.
     * One user can have many blog posts.
     */
    @OneToMany(
    mappedBy = "author", cascade = CascadeType.ALL, 
    fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<BlogPost> blogPosts = new HashSet<>();

    /**
     * Comments made by this user.
     * One user can have many comments.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, 
    fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

    /**
     * Refresh tokens for this user.
     * Used for JWT authentication.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, 
    fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<RefreshToken> refreshTokens = new HashSet<>();

    // ==================== Constructors ====================

    /**
     * Default constructor required by JPA.
     */
    public User() {
        super();
        this.roles.add(Role.USER);
    }

    /**
     * Constructor for creating a new user with basic information.
     * 
     * @param username the user's username
     * @param email the user's email address
     * @param password the user's password
     */
    public User(String username, String email, String password) {
        this();
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // ==================== UserDetails Implementation ====================
    // ==================== Utility Methods ====================
    // ==================== Getters and Setters ====================
    // ==================== Object Methods ====================

}