package com.personalblog.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicInteger;

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
 * - Email verification and login tracking
 * - Comprehensive Bean Validation
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0a
 * @since 1.0
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
    @JsonIgnore
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * User roles for authorization.
     * Sotred as comma-separated values for simplicity.
     */
    @Enumerated(EnumType.STRING)
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
    private boolean accountEnabled = true;

    /**
     * Whether the user account is locked.
     * Locked accounts cannot log in.
     */
    @Column(name = "account_locked", nullable = false)
    private boolean accountLocked = false;

    /**
     * Whether the user account is expired.
     * Expired accounts cannot log in.
     */
    @Column(name = "account_expired", nullable = false)
    private boolean accountExpired = false;

    /**
     * Whether the user account credentials are expired.
     * Expired credentials require password reset.
     */
    @Column(name = "credentials_expired", nullable = false)
    private boolean credentialsExpired = false;

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
    private boolean emailVerified = false;

    /**
     * The verification token for the user's email.
     */
    @JsonIgnore
    @Size(max = 255, message = "Verification token must not exceed 255 characters")
    @Column(name = "email_verification_token", length = 255)
    private String emailVerificationToken;

    /**
     * Expiration date for the email verification token.
     */
    @Column(name = "email_verification_token_expires_at")
    private LocalDateTime emailVerificationTokenExpiresAt;

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
     * IP address from the user's last login
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
    private Integer failedLoginAttempts = 0;

    /**
     * Atomic counter for thread-safe failed login attempts operations.
     * Transient field that mirrors the persistent failedLoginAttempts field.
     */
    @Transient
    private transient AtomicInteger atomicFailedLoginAttempts;

    // ==================== Password Reset ====================

    /**
     * Token used for password reset.
     * Should be cleared after use.
     */
    @JsonIgnore
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
        initializeAtomicCounter();
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
        initializeAtomicCounter();
    }

    /**
     * Constructor for creating a new user with basic information and roles.
     * 
     * @param username the user's username
     * @param email the user's email address
     * @param password the user's password
     * @param roles the user's roles
     */
    public User(String username, String email, String password, Set<Role> roles) {
        this(username, email, password);
        this.roles = new HashSet<>(roles);
    }

    // ==================== UserDetails Implementation ====================

    /**
     * Returns the authorities granted to the user.
     * Converts roles to Spring Security authorities.
     * 
     * @return a collection of granted authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
            .collect(Collectors.toSet());
    }

    /**
     * Returns the password used to authenticate the user.
     * 
     * @return the password
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the username used to authenticate the user.
     * 
     * @return the username
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Indicates whether the user's account has not expired.
     * 
     * @return true if the account is non-expired, false otherwise
     */
    @Override
    public boolean isAccountNonExpired() {
        return !accountExpired;
    }

    /**
     * Indicates whether the user is not locked
     * 
     * @return true if the account is non-locked, false otherwise
     */
    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    /**
     * Indicates whether the user's credentials are non-expired.
     * 
     * @return true if the credentials are non-expired, false otherwise
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return !credentialsExpired;
    }

    /**
     * Indicates whether the user is enabled.
     * 
     * @return true if the user is enabled, false otherwise
     */
    @Override
    public boolean isEnabled() {
        return accountEnabled;
    }

    // ==================== Utility Methods ====================

    /**
     * Checks if the user has a specific role.
     * 
     * @param role the role to check
     * @return true if the user has the role, false otherwise
     */
    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    /**
     * Adds a role to the user.
     * 
     * @param role the role to add
     */
    public void addRole(Role role) {
        if (role != null) {
            this.roles.add(role);
        }
    }

    /**
     * Removes a role from the user.
     * 
     * @param role the role to remove
     */
    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    /**
     * Checks if the user is an admin.
     * 
     * @return true if the user is an admin, false otherwise
     */
    public boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    /**
     * Checks if the user is an author.
     * 
     * @return true if the user is an author, false otherwise
     */
    public boolean isAuthor() {
        return hasRole(Role.AUTHOR);
    }

    /**
     * Checks if the user can write blog posts.
     * 
     * @return true if the user can write blog posts, false otherwise
     */
    public boolean canWriteBlogPosts() {
        return isAdmin() || isAuthor();
    }

    /**
     * Initializes the atomic counter with the current failed login attempts value.
     * Called during entity loading and construction.
     */
    private void initializeAtomicCounter() {
        int currentValue = (this.failedLoginAttempts != null) ? this.failedLoginAttempts : 0;
        this.atomicFailedLoginAttempts = new AtomicInteger(currentValue);
    }

    /**
     * Increments the failed login attempts counter atomically.
     * Thread-safe operation that updates both the atomic counter and persistent field.
     */
    public void incrementFailedLoginAttempts() {
        if (this.atomicFailedLoginAttempts == null) {
            initializeAtomicCounter();
        }
        this.failedLoginAttempts = this.atomicFailedLoginAttempts.incrementAndGet();
    }

    /**
     * Resets the failed login attempts counter atomically.
     * Thread-safe operation that updates both the atomic counter and persistent field.
     */
    public void resetFailedLoginAttempts() {
        if (this.atomicFailedLoginAttempts == null) {
            initializeAtomicCounter();
        }
        this.atomicFailedLoginAttempts.set(0);
        this.failedLoginAttempts = 0;
    }

    /**
     * Locks the user account.
     */
    public void lockAccount() {
        this.accountLocked = true;
    }

    /**
     * Unlocks the user account.
     */
    public void unlockAccount() {
        this.accountLocked = false;
    }

    /**
     * Marks the email as verified.
     */
    public void verifyEmail() {
        this.emailVerified = true;
        this.emailVerificationToken = null;
        this.emailVerificationTokenExpiresAt = null;
        this.emailVerifiedAt = LocalDateTime.now();
    }

    /**
     * Updates the last login information.
     * 
     * @param ipAddress The IP address of the login
     */
    public void updateLastLogin(String ipAddress) {
        this.lastLoginAt = LocalDateTime.now();
        this.lastLoginIp = ipAddress;
        resetFailedLoginAttempts();
    }

    // ==================== Getters and Setters ====================

    /**
     * Gets the user's email address.
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     * @param email the email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets the user's username.
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sets the user's password.
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the user's roles.
     * @return set of roles assigned to the user
     */
    public Set<Role> getRoles() {
        return roles;
    }

    /**
     * Sets the user's roles.
     * @param roles the set of roles to assign
     */
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    /**
     * Gets whether the account is enabled.
     * @return true if account is enabled, false otherwise
     */
    public boolean isAccountEnabled() {
        return accountEnabled;
    }

    /**
     * Sets whether the account is enabled.
     * @param accountEnabled true to enable account, false to disable
     */
    public void setAccountEnabled(boolean accountEnabled) {
        this.accountEnabled = accountEnabled;
    }

    /**
     * Gets whether the account is locked.
     * @return true if account is locked, false otherwise
     */
    public boolean isAccountLocked() {
        return accountLocked;
    }

    /**
     * Sets whether the account is locked.
     * @param accountLocked true to lock account, false to unlock
     */
    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    /**
     * Gets whether the account is expired.
     * @return true if account is expired, false otherwise
     */
    public boolean isAccountExpired() {
        return accountExpired;
    }

    /**
     * Sets whether the account is expired.
     * @param accountExpired true to expire account, false otherwise
     */
    public void setAccountExpired(boolean accountExpired) {
        this.accountExpired = accountExpired;
    }

    /**
     * Gets whether the credentials are expired.
     * @return true if credentials are expired, false otherwise
     */
    public boolean isCredentialsExpired() {
        return credentialsExpired;
    }

    /**
     * Sets whether the credentials are expired.
     * @param credentialsExpired true to expire credentials, false otherwise
     */
    public void setCredentialsExpired(boolean credentialsExpired) {
        this.credentialsExpired = credentialsExpired;
    }

    /**
     * Gets the user's biography.
     * @return the biography text
     */
    public String getBio() {
        return bio;
    }

    /**
     * Sets the user's biography.
     * @param bio the biography text to set
     */
    public void setBio(String bio) {
        this.bio = bio;
    }

    /**
     * Gets the URL of the user's avatar.
     * @return the avatar URL
     */
    public String getAvatarUrl() {
        return avatarUrl;
    }

    /**
     * Sets the URL of the user's avatar.
     * @param avatarUrl the avatar URL to set
     */
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    /**
     * Gets the user's website URL.
     * @return the website URL
     */
    public String getWebsiteUrl() {
        return websiteUrl;
    }

    /**
     * Sets the user's website URL.
     * @param websiteUrl the website URL to set
     */
    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    /**
     * Gets the user's Twitter/X profile URL.
     * @return the Twitter/X profile URL
     */
    public String getTwitterUrl() {
        return twitterUrl;
    }

    /**
     * Sets the user's Twitter/X profile URL.
     * @param twitterUrl the Twitter/X profile URL to set
     */
    public void setTwitterUrl(String twitterUrl) {
        this.twitterUrl = twitterUrl;
    }

    /**
     * Gets the user's LinkedIn profile URL.
     * @return the LinkedIn profile URL
     */
    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    /**
     * Sets the user's LinkedIn profile URL.
     * @param linkedinUrl the LinkedIn profile URL to set
     */
    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    /**
     * Gets the user's GitHub profile URL.
     * @return the GitHub profile URL
     */
    public String getGithubUrl() {
        return githubUrl;
    }

    /**
     * Sets the user's GitHub profile URL.
     * @param githubUrl the GitHub profile URL to set
     */
    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    /**
     * Gets the user's YouTube profile URL.
     * @return the YouTube profile URL
     */
    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    /**
     * Sets the user's YouTube profile URL.
     * @param youtubeUrl the YouTube profile URL to set
     */
    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    /**
     * Gets the user's Instagram profile URL.
     * @return the Instagram profile URL
     */
    public String getInstagramUrl() {
        return instagramUrl;
    }

    /**
     * Sets the user's Instagram profile URL.
     * @param instagramUrl the Instagram profile URL to set
     */
    public void setInstagramUrl(String instagramUrl) {
        this.instagramUrl = instagramUrl;
    }

    /**
     * Gets whether the email is verified.
     * @return true if email is verified, false otherwise
     */
    public boolean isEmailVerified() {
        return emailVerified;
    }

    /**
     * Sets whether the email is verified.
     * @param emailVerified true to mark email as verified, false otherwise
     */
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    /**
     * Gets the email verification token.
     * @return the email verification token
     */
    public String getEmailVerificationToken() {
        return emailVerificationToken;
    }

    /**
     * Sets the email verification token.
     * @param emailVerificationToken the email verification token to set
     */
    public void setEmailVerificationToken(String emailVerificationToken) {
        this.emailVerificationToken = emailVerificationToken;
    }

    /**
     * Gets the expiration date/time of the email verification token.
     * @return the email verification token expiration date/time
     */
    public LocalDateTime getEmailVerificationTokenExpiresAt() {
        return emailVerificationTokenExpiresAt;
    }

    /**
     * Sets the expiration date/time of the email verification token.
     * @param emailVerificationTokenExpiresAt the expiration date/time to set
     */
    public void setEmailVerificationTokenExpiresAt(LocalDateTime emailVerificationTokenExpiresAt) {
        this.emailVerificationTokenExpiresAt = emailVerificationTokenExpiresAt;
    }

    /**
     * Gets the date/time when the email was verified.
     * @return the email verification date/time
     */
    public LocalDateTime getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    /**
     * Sets the date/time when the email was verified.
     * @param emailVerifiedAt the verification date/time to set
     */
    public void setEmailVerifiedAt(LocalDateTime emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }

    /**
     * Gets the date/time of the last successful login.
     * @return the last login date/time
     */
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    /**
     * Sets the date/time of the last successful login.
     * @param lastLoginAt the last login date/time to set
     */
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    /**
     * Gets the IP address of the last login.
     * @return the last login IP address
     */
    public String getLastLoginIp() {
        return lastLoginIp;
    }

    /**
     * Sets the IP address of the last login.
     * @param lastLoginIp the last login IP address to set
     */
    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    /**
     * Gets the number of failed login attempts.
     * @return the number of failed login attempts
     */
    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    /**
     * Sets the number of failed login attempts.
     * @param failedLoginAttempts the number of failed attempts to set
     */
    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
        if (this.atomicFailedLoginAttempts == null) {
            initializeAtomicCounter();
        } else {
            this.atomicFailedLoginAttempts.set(failedLoginAttempts != null ? failedLoginAttempts : 0);
        }
    }

    /**
     * Gets the password reset token.
     * @return the password reset token
     */
    public String getPasswordResetToken() {
        return passwordResetToken;
    }

    /**
     * Sets the password reset token.
     * @param passwordResetToken the password reset token to set
     */
    public void setPasswordResetToken(String passwordResetToken) {
        this.passwordResetToken = passwordResetToken;
    }

    /**
     * Gets the expiration date/time of the password reset token.
     * @return the password reset token expiration date/time
     */
    public LocalDateTime getPasswordResetTokenExpiresAt() {
        return passwordResetTokenExpiresAt;
    }

    /**
     * Sets the expiration date/time of the password reset token.
     * @param passwordResetTokenExpiresAt the expiration date/time to set
     */
    public void setPasswordResetTokenExpiresAt(LocalDateTime passwordResetTokenExpiresAt) {
        this.passwordResetTokenExpiresAt = passwordResetTokenExpiresAt;
    }

    /**
     * Gets the set of blog posts authored by this user.
     * @return set of blog posts
     */
    public Set<BlogPost> getBlogPosts() {
        return blogPosts;
    }

    /**
     * Sets the blog posts authored by this user.
     * @param blogPosts the set of blog posts to set
     */
    public void setBlogPosts(Set<BlogPost> blogPosts) {
        this.blogPosts = blogPosts;
    }

    /**
     * Gets the set of comments made by this user.
     * @return set of comments
     */
    public Set<Comment> getComments() {
        return comments;
    }

    /**
     * Sets the comments made by this user.
     * @param comments the set of comments to set
     */
    public void setComments(Set<Comment> comments) {
        this.comments = comments;
    }

    /**
     * Gets the set of refresh tokens for this user.
     * @return set of refresh tokens
     */
    public Set<RefreshToken> getRefreshTokens() {
        return refreshTokens;
    }

    /**
     * Sets the refresh tokens for this user.
     * @param refreshTokens the set of refresh tokens to set
     */
    public void setRefreshTokens(Set<RefreshToken> refreshTokens) {
        this.refreshTokens = refreshTokens;
    }

    // ==================== Object Methods ====================

    /**
     * String representation of the user.
     * Excludes sensitive information like password.
     * 
     * @return string representation
     */
    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s', email='%s', " +
                              "roles=%s, enabled=%s, emailVerified=%s}",
                super.getId(), username, email, roles, accountEnabled, emailVerified);
    }
}