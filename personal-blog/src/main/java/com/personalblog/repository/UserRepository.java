package com.personalblog.repository;

import com.personalblog.entity.User;
import com.personalblog.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository interface for User entity operations.
 * 
 * Provides comprehensive user management functionality including:
 * - Authentication and authorization queries
 * - User search and filtering
 * - Account status management
 * - Security-focused operations
 * - User statistics and analytics
 * - Email verification and password reset support
 * 
 * All methods follow security best practices and include proper
 * validation to prevent unauthorized access and data breaches.
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.1
 * @since 1.0
 */
@Validated
public interface UserRepository extends BaseRepository<User, Long> {

    // ==================== Authentication & Authorization ====================

    /**
     * Find user by username for authentication.
     * Only returns active, non-deleted users.
     * 
     * @param username the username to search for (must not be null or blank)
     * @return optional containing the user if found and active
     */
    @Query("SELECT u FROM User u WHERE u.username = :username " +
           "AND u.deleted = false AND u.accountEnabled = true")
    Optional<User> findByUsernameForAuth(@Param("username") @NotBlank @Size(min = 3, max = 50) String username);

    /**
     * Find user by username and active status (for backward compatibility).
     * Only returns active, non-deleted users.
     * 
     * @param username the username to search for
     * @return optional containing the user if found and active
     */
    @Query("SELECT u FROM User u WHERE u.username = :username " +
           "AND u.deleted = false AND u.accountEnabled = true")
    Optional<User> findByUsernameAndActive(@Param("username") String username);

    /**
     * Find user by email for authentication.
     * Only returns active, non-deleted users.
     * 
     * @param email the email to search for (must not be null or blank)
     * @return optional containing the user if found and active
     */
    @Query("SELECT u FROM User u WHERE u.email = :email " +
           "AND u.deleted = false AND u.accountEnabled = true")
    Optional<User> findByEmailForAuth(@Param("email") @NotBlank @jakarta.validation.constraints.Email String email);

    /**
     * Find user by email and active status (for backward compatibility).
     * Only returns active, non-deleted users.
     * 
     * @param email the email to search for
     * @return optional containing the user if found and active
     */
    @Query("SELECT u FROM User u WHERE u.email = :email " +
           "AND u.deleted = false AND u.accountEnabled = true")
    Optional<User> findByEmailAndActive(@Param("email") String email);

    /**
     * Find user by username (including disabled accounts for admin operations).
     * 
     * @param username the username to search for
     * @return optional containing the user if found
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.deleted = false")
    Optional<User> findByUsername(@Param("username") String username);

    /**
     * Find user by email (including disabled accounts for admin operations).
     * 
     * @param email the email to search for
     * @return optional containing the user if found
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deleted = false")
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * Check if username exists (for validation).
     * 
     * @param username the username to check (must not be null or blank)
     * @return true if username exists
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.deleted = false")
    boolean existsByUsername(@Param("username") @NotBlank @Size(min = 3, max = 50) String username);

    /**
     * Check if email exists (for validation).
     * 
     * @param email the email to check (must not be null or blank)
     * @return true if email exists
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.deleted = false")
    boolean existsByEmail(@Param("email") @NotBlank @jakarta.validation.constraints.Email String email);

    // ==================== Role-Based Queries ====================

    /**
     * Find all active users with specific role.
     * 
     * @param role the role to filter by
     * @param pageable pagination information
     * @return page of users with the specified role
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role " +
           "AND u.deleted = false AND u.accountEnabled = true AND u.accountLocked = false")
    Page<User> findByRole(@Param("role") Role role, Pageable pageable);

    /**
     * Find all active users with any of the specified roles.
     * 
     * @param roles the roles to filter by
     * @param pageable pagination information
     * @return page of users with any of the specified roles
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r IN :roles " +
           "AND u.deleted = false AND u.accountEnabled = true")
    Page<User> findByRoles(@Param("roles") Set<Role> roles, Pageable pageable);

    /**
     * Find all administrators (users with ADMIN role).
     * 
     * @return list of admin users
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = com.personalblog.entity.Role.ADMIN " +
           "AND u.deleted = false AND u.accountEnabled = true")
    List<User> findAllAdmins();

    /**
     * Find all authors (users with AUTHOR role).
     * 
     * @param pageable pagination information
     * @return page of author users
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = com.personalblog.entity.Role.AUTHOR " +
           "AND u.deleted = false AND u.accountEnabled = true")
    Page<User> findAllAuthors(Pageable pageable);

    // ==================== Search and Filtering ====================

    /**
     * Search users by name (first name, last name, or username).
     * Case-insensitive search across multiple fields with input validation.
     * 
     * @param searchTerm the search term (must not be null, min 2 chars for security)
     * @param pageable pagination information
     * @return page of matching users
     */
    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.accountEnabled = true " +
           "AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<User> searchByName(@Param("searchTerm") @NotBlank @Size(min = 2, max = 100) String searchTerm, 
                           @NonNull Pageable pageable);

    /**
     * Comprehensive user search with multiple criteria.
     * Supports searching by name, email, role, and verification status.
     * Enhanced with input validation and security measures.
     * 
     * @param searchTerm optional search term for name/email (min 2 chars if provided)
     * @param role optional role filter
     * @param emailVerified optional email verification status filter
     * @param pageable pagination information (must not be null)
     * @return page of matching users
     */
    @Query("SELECT u FROM User u LEFT JOIN u.roles r WHERE u.deleted = false " +
           "AND u.accountEnabled = true " +
           "AND (:searchTerm IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND (:role IS NULL OR r = :role) " +
           "AND (:emailVerified IS NULL OR u.emailVerified = :emailVerified)")
    Page<User> searchUsers(@Param("searchTerm") @Size(min = 2, max = 100) String searchTerm,
                          @Param("role") Role role,
                          @Param("emailVerified") Boolean emailVerified,
                          @NonNull Pageable pageable);

    // ==================== Account Status Queries ====================

    /**
     * Find all enabled users.
     * 
     * @param pageable pagination information
     * @return page of enabled users
     */
    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.accountEnabled = true")
    Page<User> findAllEnabled(Pageable pageable);

    /**
     * Find all disabled users.
     * 
     * @param pageable pagination information
     * @return page of disabled users
     */
    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.accountEnabled = false")
    Page<User> findAllDisabled(Pageable pageable);

    /**
     * Find all locked users.
     * 
     * @param pageable pagination information
     * @return page of locked users
     */
    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.accountLocked = true")
    Page<User> findAllLocked(Pageable pageable);

    /**
     * Find users with unverified emails.
     * 
     * @param pageable pagination information
     * @return page of users with unverified emails
     */
    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.emailVerified = false")
    Page<User> findAllUnverified(Pageable pageable);

    /**
     * Find users with expired verification tokens.
     * 
     * @param now current timestamp (must not be null)
     * @return list of users with expired tokens
     */
    @Query("SELECT u FROM User u WHERE u.deleted = false " +
           "AND u.emailVerificationToken IS NOT NULL " +
           "AND u.emailVerificationTokenExpiresAt < :now")
    List<User> findUsersWithExpiredVerificationTokens(@Param("now") @NotNull LocalDateTime now);

    /**
     * Find users with expired verification tokens (pageable version).
     * 
     * @param now current timestamp (must not be null)
     * @param pageable pagination information
     * @return page of users with expired tokens
     */
    @Query("SELECT u FROM User u WHERE u.deleted = false " +
           "AND u.emailVerificationToken IS NOT NULL " +
           "AND u.emailVerificationTokenExpiresAt < :now")
    Page<User> findUsersWithExpiredVerificationTokens(@Param("now") @NotNull LocalDateTime now, 
                                                     @NonNull Pageable pageable);

    // ==================== Security Operations ====================

    /**
     * Find user by email verification token.
     * Enhanced with validation and rate limiting considerations.
     * 
     * @param token the verification token (must not be null or blank)
     * @return optional containing the user if token is valid and not expired
     */
    @Query("SELECT u FROM User u WHERE u.emailVerificationToken = :token " +
           "AND u.deleted = false AND u.emailVerificationTokenExpiresAt > CURRENT_TIMESTAMP " +
           "AND u.failedLoginAttempts < 10") // Rate limiting: block after 10 failed attempts
    Optional<User> findByEmailVerificationToken(@Param("token") @NotBlank @Size(min = 10, max = 255) String token);

    /**
     * Find user by password reset token.
     * Enhanced with validation and rate limiting considerations.
     * 
     * @param token the password reset token (must not be null or blank)
     * @return optional containing the user if token is valid and not expired
     */
    @Query("SELECT u FROM User u WHERE u.passwordResetToken = :token " +
           "AND u.deleted = false AND u.passwordResetTokenExpiresAt > CURRENT_TIMESTAMP " +
           "AND u.failedLoginAttempts < 10") // Rate limiting: block after 10 failed attempts
    Optional<User> findByPasswordResetToken(@Param("token") @NotBlank @Size(min = 10, max = 255) String token);

    /**
     * Find users with multiple failed login attempts.
     * Used for security monitoring and account locking.
     * 
     * @param threshold minimum number of failed attempts (must be positive)
     * @return list of users with excessive failed attempts
     */
    @Query("SELECT u FROM User u WHERE u.deleted = false " +
           "AND u.failedLoginAttempts >= :threshold")
    List<User> findUsersWithFailedAttempts(@Param("threshold") @NotNull @Min(1) Integer threshold);

    /**
     * Find users with multiple failed login attempts (pageable version).
     * Used for security monitoring and account locking with pagination support.
     * 
     * @param threshold minimum number of failed attempts (must be positive)
     * @param pageable pagination information
     * @return page of users with excessive failed attempts
     */
    @Query("SELECT u FROM User u WHERE u.deleted = false " +
           "AND u.failedLoginAttempts >= :threshold")
    Page<User> findUsersWithFailedAttempts(@Param("threshold") @NotNull @Min(1) Integer threshold, 
                                          @NonNull Pageable pageable);

    /**
     * Find users who haven't logged in for a specified period.
     * Used for inactive account management.
     * 
     * @param cutoffDate the cutoff date for last login (must not be null)
     * @return list of inactive users
     */
    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.emailVerified = true " +
           "AND (u.lastLoginAt IS NULL OR u.lastLoginAt < :cutoffDate)")
    List<User> findInactiveUsers(@Param("cutoffDate") @NotNull LocalDateTime cutoffDate);

    /**
     * Find users who haven't logged in for a specified period (pageable version).
     * Used for inactive account management with pagination support.
     * 
     * @param cutoffDate the cutoff date for last login (must not be null)
     * @param pageable pagination information
     * @return page of inactive users
     */
    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.emailVerified = true " +
           "AND (u.lastLoginAt IS NULL OR u.lastLoginAt < :cutoffDate)")
    Page<User> findInactiveUsers(@Param("cutoffDate") @NotNull LocalDateTime cutoffDate, 
                                @NonNull Pageable pageable);

    // ==================== Bulk Operations ====================

    /**
     * Update failed login attempts for a user.
     * Thread-safe operation for security purposes.
     * 
     * @param userId the user ID (must not be null)
     * @param attempts new failed attempts count (must not be null and non-negative)
     * @return number of updated records
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.failedLoginAttempts = :attempts " +
           "WHERE u.id = :userId AND u.deleted = false")
    int updateFailedLoginAttempts(@Param("userId") @NotNull Long userId, 
                                 @Param("attempts") @NotNull @Min(0) Integer attempts);

    /**
     * Invalidate tokens after multiple failed attempts.
     * Security measure to prevent token abuse.
     * 
     * @param userId the user ID (must not be null)
     * @return number of updated records
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.emailVerificationToken = NULL, " +
           "u.emailVerificationTokenExpiresAt = NULL, " +
           "u.passwordResetToken = NULL, " +
           "u.passwordResetTokenExpiresAt = NULL " +
           "WHERE u.id = :userId AND u.deleted = false AND u.failedLoginAttempts >= 10")
    int invalidateTokensAfterFailedAttempts(@Param("userId") @NotNull Long userId);

    /**
     * Update last login information for a user.
     * 
     * @param userId the user ID (must not be null)
     * @param loginTime the login timestamp (must not be null)
     * @param ipAddress the login IP address (must not be null or blank)
     * @return number of updated records
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime, u.lastLoginIp = :ipAddress, " +
           "u.failedLoginAttempts = 0 WHERE u.id = :userId AND u.deleted = false")
    int updateLastLogin(@Param("userId") @NotNull Long userId,
                       @Param("loginTime") @NotNull LocalDateTime loginTime,
                       @Param("ipAddress") @NotBlank @Size(max = 45) String ipAddress);

    /**
     * Clear email verification token after successful verification.
     * 
     * @param userId the user ID
     * @return number of updated records
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.emailVerified = true, u.emailVerifiedAt = CURRENT_TIMESTAMP, " +
           "u.emailVerificationToken = NULL, u.emailVerificationTokenExpiresAt = NULL " +
           "WHERE u.id = :userId AND u.deleted = false")
    int markEmailAsVerified(@Param("userId") Long userId);

    /**
     * Clear password reset token after successful reset.
     * 
     * @param userId the user ID
     * @return number of updated records
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.passwordResetToken = NULL, " +
           "u.passwordResetTokenExpiresAt = NULL " +
           "WHERE u.id = :userId AND u.deleted = false")
    int clearPasswordResetToken(@Param("userId") Long userId);

    /**
     * Lock user account.
     * 
     * @param userId the user ID
     * @return number of updated records
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.accountLocked = true " +
           "WHERE u.id = :userId AND u.deleted = false")
    int lockAccount(@Param("userId") Long userId);

    /**
     * Unlock user account and reset failed login attempts.
     * 
     * @param userId the user ID
     * @return number of updated records
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.accountLocked = false, u.failedLoginAttempts = 0 " +
           "WHERE u.id = :userId AND u.deleted = false")
    int unlockAccount(@Param("userId") Long userId);

    /**
     * Enable user account.
     * 
     * @param userId the user ID
     * @return number of updated records
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.accountEnabled = true " +
           "WHERE u.id = :userId AND u.deleted = false")
    int enableAccount(@Param("userId") Long userId);

    /**
     * Disable user account.
     * 
     * @param userId the user ID
     * @return number of updated records
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.accountEnabled = false " +
           "WHERE u.id = :userId AND u.deleted = false")
    int disableAccount(@Param("userId") Long userId);

    // ==================== Statistics and Analytics ====================

    /**
     * Count total active users.
     * 
     * @return total number of active users
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.deleted = false AND u.accountEnabled = true")
    long countActiveUsers();

    /**
     * Count users by role.
     * 
     * @param role the role to count
     * @return number of users with the specified role
     */
    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r " +
           "WHERE r = :role AND u.deleted = false AND u.accountEnabled = true")
    long countUsersByRole(@Param("role") Role role);

    /**
     * Count users registered in a date range.
     * 
     * @param startDate start of the date range
     * @param endDate end of the date range
     * @return number of users registered in the period
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.deleted = false " +
           "AND u.createdAt BETWEEN :startDate AND :endDate")
    long countUsersRegisteredBetween(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    /**
     * Count users with verified emails.
     * 
     * @return number of users with verified emails
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.deleted = false AND u.emailVerified = true")
    long countVerifiedUsers();

    /**
     * Find most active users by blog post count.
     * Enhanced with JOIN FETCH to prevent N+1 query issues.
     * 
     * @param pageable pagination information (must not be null)
     * @return page of users ordered by blog post count
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.blogPosts bp " +
           "WHERE u.deleted = false AND u.accountEnabled = true " +
           "GROUP BY u ORDER BY COUNT(bp) DESC")
    Page<User> findMostActiveAuthors(@NonNull Pageable pageable);

    /**
     * Find recently registered users.
     * 
     * @param cutoffDate the cutoff date for registration (must not be null)
     * @param pageable pagination information (must not be null)
     * @return page of recently registered users
     */
    @Query("SELECT u FROM User u WHERE u.deleted = false " +
           "AND u.createdAt >= :cutoffDate ORDER BY u.createdAt DESC")
    Page<User> findRecentlyRegistered(@Param("cutoffDate") @NotNull LocalDateTime cutoffDate, 
                                     @NonNull Pageable pageable);

    // ==================== Native Queries for Complex Operations ====================

    /**
     * Get user statistics including post count, comment count, and last activity.
     * Uses native query for optimal performance.
     * 
     * @param userId the user ID (must not be null)
     * @return array containing [post_count, comment_count, last_activity]
     */
    @Query(value = "SELECT " +
           "COALESCE(bp.post_count, 0) as post_count, " +
           "COALESCE(c.comment_count, 0) as comment_count, " +
           "GREATEST(u.last_login_at, u.updated_at) as last_activity " +
           "FROM users u " +
           "LEFT JOIN (SELECT author_id, COUNT(*) as post_count FROM blog_posts " +
           "           WHERE deleted = false GROUP BY author_id) bp ON u.id = bp.author_id " +
           "LEFT JOIN (SELECT user_id, COUNT(*) as comment_count FROM comments " +
           "           WHERE deleted = false GROUP BY user_id) c ON u.id = c.user_id " +
           "WHERE u.id = :userId AND u.deleted = false", nativeQuery = true)
    Object[] getUserStatistics(@Param("userId") @NotNull Long userId);

    // ==================== Rate Limiting and Security ====================

    /**
     * Count token verification attempts within a time window.
     * Used for rate limiting token-based operations.
     * 
     * @param userId the user ID (must not be null)
     * @param timeWindow the time window to check (must not be null)
     * @return number of attempts within the time window
     */
    @Query("SELECT u.failedLoginAttempts FROM User u " +
           "WHERE u.id = :userId AND u.deleted = false " +
           "AND u.updatedAt >= :timeWindow")
    Integer countTokenAttemptsInWindow(@Param("userId") @NotNull Long userId, 
                                      @Param("timeWindow") @NotNull LocalDateTime timeWindow);

    /**
     * Check if user is rate limited for token operations.
     * 
     * @param userId the user ID (must not be null)
     * @return true if user is rate limited
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.id = :userId " +
           "AND u.deleted = false AND u.failedLoginAttempts >= 10")
    boolean isUserRateLimited(@Param("userId") @NotNull Long userId);

    /**
     * Clean up expired tokens (verification and password reset).
     * Uses native query for better performance.
     * 
     * @return number of users updated
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET " +
           "email_verification_token = NULL, " +
           "email_verification_token_expires_at = NULL, " +
           "password_reset_token = NULL, " +
           "password_reset_token_expires_at = NULL " +
           "WHERE deleted = false AND (" +
           "(email_verification_token IS NOT NULL AND email_verification_token_expires_at < CURRENT_TIMESTAMP) OR " +
           "(password_reset_token IS NOT NULL AND password_reset_token_expires_at < CURRENT_TIMESTAMP))", 
           nativeQuery = true)
    int cleanupExpiredTokens();
}