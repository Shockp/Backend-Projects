package com.personalblog.repository;

import com.personalblog.entity.RefreshToken;
import com.personalblog.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for RefreshToken entity operations.
 * Provides comprehensive data access methods for JWT refresh token management
 * with security features, device tracking, and cleanup operations.
 * 
 * Features:
 * - Token validation and expiry management
 * - User association and device tracking
 * - Security features (rate limiting, blocking)
 * - Token cleanup and maintenance operations
 * - Usage tracking and analytics
 * - Bulk operations for administrative tasks
 * - Performance-optimized queries with proper indexing
 * - Soft delete support inherited from BaseRepository
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface RefreshTokenRepository extends BaseRepository<RefreshToken, Long> {

    // ==================== Basic Token Queries ====================

    /**
     * Find refresh token by token value.
     * 
     * @param tokenValue the token value
     * @return optional refresh token if found and not deleted
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.encryptedTokenValue = :tokenValue AND rt.deleted = false")
    Optional<RefreshToken> findByTokenValue(@Param("tokenValue") String tokenValue);

    /**
     * Find valid token by token value.
     * 
     * @param tokenValue the token value
     * @return optional valid refresh token
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.encryptedTokenValue = :tokenValue AND rt.deleted = false AND rt.revoked = false AND rt.expiryDate > CURRENT_TIMESTAMP AND (rt.blockedUntil IS NULL OR rt.blockedUntil < CURRENT_TIMESTAMP)")
    Optional<RefreshToken> findValidTokenByValue(@Param("tokenValue") String tokenValue);

    // ==================== User Association Queries ====================

    /**
     * Find tokens by user.
     * 
     * @param user the user
     * @return list of tokens for the user
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.deleted = false ORDER BY rt.createdAt DESC")
    List<RefreshToken> findByUser(@Param("user") User user);

    /**
     * Find tokens by user ID.
     * 
     * @param userId the user ID
     * @return list of tokens for the user
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.deleted = false ORDER BY rt.createdAt DESC")
    List<RefreshToken> findByUserId(@Param("userId") Long userId);

    /**
     * Find tokens by user with pagination.
     * 
     * @param user the user
     * @param pageable pagination information
     * @return page of tokens for the user
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.deleted = false")
    Page<RefreshToken> findByUser(@Param("user") User user, Pageable pageable);

    /**
     * Find valid tokens by user.
     * 
     * @param user the user
     * @return list of valid tokens for the user
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.deleted = false AND rt.revoked = false AND rt.expiryDate > CURRENT_TIMESTAMP AND (rt.blockedUntil IS NULL OR rt.blockedUntil < CURRENT_TIMESTAMP) ORDER BY rt.createdAt DESC")
    List<RefreshToken> findValidTokensByUser(@Param("user") User user);

    /**
     * Find valid tokens by user ID.
     * 
     * @param userId the user ID
     * @return list of valid tokens for the user
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.deleted = false AND rt.revoked = false AND rt.expiryDate > CURRENT_TIMESTAMP AND (rt.blockedUntil IS NULL OR rt.blockedUntil < CURRENT_TIMESTAMP) ORDER BY rt.createdAt DESC")
    List<RefreshToken> findValidTokensByUserId(@Param("userId") Long userId);

    /**
     * Find valid tokens by user ID with pagination.
     * 
     * @param userId the user ID
     * @param pageable pagination information
     * @return page of valid tokens for the user
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.deleted = false AND rt.revoked = false AND rt.expiryDate > CURRENT_TIMESTAMP AND (rt.blockedUntil IS NULL OR rt.blockedUntil < CURRENT_TIMESTAMP)")
    Page<RefreshToken> findValidTokensByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find active tokens by user ID ordered by last used date.
     * 
     * @param userId the user ID
     * @return list of active tokens for the user ordered by last used date (most recent first)
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.deleted = false AND rt.revoked = false AND rt.expiryDate > CURRENT_TIMESTAMP AND (rt.blockedUntil IS NULL OR rt.blockedUntil < CURRENT_TIMESTAMP) ORDER BY rt.lastUsedAt DESC")
    List<RefreshToken> findActiveTokensByUserId(@Param("userId") Long userId);

    /**
     * Count tokens by user.
     * 
     * @param user the user
     * @return number of tokens for the user
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user = :user AND rt.deleted = false")
    long countByUser(@Param("user") User user);

    /**
     * Count valid tokens by user.
     * 
     * @param user the user
     * @return number of valid tokens for the user
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user = :user AND rt.deleted = false AND rt.revoked = false AND rt.expiryDate > CURRENT_TIMESTAMP AND (rt.blockedUntil IS NULL OR rt.blockedUntil < CURRENT_TIMESTAMP)")
    long countValidTokensByUser(@Param("user") User user);

    // ==================== Device Management Queries ====================

    /**
     * Find tokens by device ID.
     * 
     * @param deviceId the device ID
     * @return list of tokens for the device
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.deviceId = :deviceId AND rt.deleted = false ORDER BY rt.createdAt DESC")
    List<RefreshToken> findByDeviceId(@Param("deviceId") String deviceId);

    /**
     * Find tokens by user and device.
     * 
     * @param user the user
     * @param deviceId the device ID
     * @return list of tokens for the user and device
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.deviceId = :deviceId AND rt.deleted = false ORDER BY rt.createdAt DESC")
    List<RefreshToken> findByUserAndDeviceId(@Param("user") User user, @Param("deviceId") String deviceId);

    /**
     * Find tokens by user ID and device ID.
     * 
     * @param userId the user ID
     * @param deviceId the device ID
     * @return list of tokens for the user and device
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.deviceId = :deviceId AND rt.deleted = false ORDER BY rt.createdAt DESC")
    List<RefreshToken> findByUserIdAndDeviceId(@Param("userId") Long userId, @Param("deviceId") String deviceId);

    /**
     * Find latest token by user and device.
     * 
     * @param userId the user ID
     * @param deviceId the device ID
     * @return optional latest token for the user and device
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.deviceId = :deviceId AND rt.deleted = false ORDER BY rt.createdAt DESC LIMIT 1")
    Optional<RefreshToken> findLatestByUserAndDevice(@Param("userId") Long userId, @Param("deviceId") String deviceId);

    /**
     * Get user devices.
     * 
     * @param userId the user ID
     * @return list of [deviceId, deviceName, lastUsed, tokenCount] arrays
     */
    @Query("SELECT rt.deviceId, rt.deviceName, MAX(rt.lastUsedAt), COUNT(rt) FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.deleted = false GROUP BY rt.deviceId, rt.deviceName ORDER BY MAX(rt.lastUsedAt) DESC")
    List<Object[]> getUserDevices(@Param("userId") Long userId);

    // ==================== Expiry Management Queries ====================

    /**
     * Find expired tokens.
     * 
     * @return list of expired tokens
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiryDate <= CURRENT_TIMESTAMP AND rt.deleted = false ORDER BY rt.expiryDate ASC")
    List<RefreshToken> findExpiredTokens();

    /**
     * Find expired tokens with pagination.
     * 
     * @param pageable pagination information
     * @return page of expired tokens
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiryDate <= CURRENT_TIMESTAMP AND rt.deleted = false")
    Page<RefreshToken> findExpiredTokens(Pageable pageable);

    /**
     * Find tokens expiring soon.
     * 
     * @param threshold the expiry threshold
     * @return list of tokens expiring soon
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiryDate <= :threshold AND rt.expiryDate > CURRENT_TIMESTAMP AND rt.deleted = false ORDER BY rt.expiryDate ASC")
    List<RefreshToken> findTokensExpiringSoon(@Param("threshold") LocalDateTime threshold);

    /**
     * Count expired tokens.
     * 
     * @return number of expired tokens
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.expiryDate <= CURRENT_TIMESTAMP AND rt.deleted = false")
    long countExpiredTokens();

    /**
     * Extend token expiry.
     * 
     * @param tokenId the token ID
     * @param newExpiryDate the new expiry date
     * @return number of updated tokens
     */
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.expiryDate = :newExpiryDate WHERE rt.id = :tokenId AND rt.deleted = false")
    int extendTokenExpiry(@Param("tokenId") Long tokenId, @Param("newExpiryDate") LocalDateTime newExpiryDate);

    // ==================== Security and Rate Limiting Queries ====================

    /**
     * Find tokens by IP address.
     * 
     * @param ipAddress the IP address
     * @return list of tokens from the IP address
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.ipAddress = :ipAddress AND rt.deleted = false ORDER BY rt.createdAt DESC")
    List<RefreshToken> findByIpAddress(@Param("ipAddress") String ipAddress);

    /**
     * Find tokens with failed attempts.
     * 
     * @param threshold minimum number of failed attempts
     * @return list of tokens with failed attempts above threshold
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.failedAttempts >= :threshold AND rt.deleted = false ORDER BY rt.failedAttempts DESC")
    List<RefreshToken> findTokensWithFailedAttempts(@Param("threshold") Integer threshold);

    /**
     * Find blocked tokens.
     * 
     * @return list of currently blocked tokens
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.blockedUntil IS NOT NULL AND rt.blockedUntil > CURRENT_TIMESTAMP AND rt.deleted = false ORDER BY rt.blockedUntil DESC")
    List<RefreshToken> findBlockedTokens();

    /**
     * Increment failed attempts.
     * 
     * @param tokenId the token ID
     * @return number of updated tokens
     */
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.failedAttempts = rt.failedAttempts + 1 WHERE rt.id = :tokenId AND rt.deleted = false")
    int incrementFailedAttempts(@Param("tokenId") Long tokenId);

    /**
     * Reset failed attempts.
     * 
     * @param tokenId the token ID
     * @return number of updated tokens
     */
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.failedAttempts = 0 WHERE rt.id = :tokenId AND rt.deleted = false")
    int resetFailedAttempts(@Param("tokenId") Long tokenId);

    /**
     * Block token.
     * 
     * @param tokenId the token ID
     * @param blockUntil the block expiry time
     * @return number of updated tokens
     */
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.blockedUntil = :blockUntil WHERE rt.id = :tokenId AND rt.deleted = false")
    int blockToken(@Param("tokenId") Long tokenId, @Param("blockUntil") LocalDateTime blockUntil);

    /**
     * Unblock expired blocks.
     * 
     * @return number of unblocked tokens
     */
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.blockedUntil = NULL WHERE rt.blockedUntil IS NOT NULL AND rt.blockedUntil <= CURRENT_TIMESTAMP AND rt.deleted = false")
    int unblockExpiredBlocks();

    // ==================== Usage Tracking Queries ====================

    /**
     * Update last used timestamp.
     * 
     * @param tokenId the token ID
     * @param lastUsed the last used timestamp
     * @return number of updated tokens
     */
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.lastUsedAt = :lastUsed WHERE rt.id = :tokenId AND rt.deleted = false")
    int updateLastUsed(@Param("tokenId") Long tokenId, @Param("lastUsed") LocalDateTime lastUsed);

    /**
     * Increment usage count.
     * 
     * @param tokenId the token ID
     * @return number of updated tokens
     */
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.usageCount = rt.usageCount + 1 WHERE rt.id = :tokenId AND rt.deleted = false")
    int incrementUsageCount(@Param("tokenId") Long tokenId);

    /**
     * Find unused tokens.
     * 
     * @param threshold the date threshold
     * @return list of unused tokens
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE (rt.lastUsedAt IS NULL OR rt.lastUsedAt < :threshold) AND rt.deleted = false ORDER BY rt.createdAt ASC")
    List<RefreshToken> findUnusedTokens(@Param("threshold") LocalDateTime threshold);

    /**
     * Find most active tokens.
     * 
     * @param pageable pagination information
     * @return page of most active tokens
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.deleted = false ORDER BY rt.usageCount DESC")
    Page<RefreshToken> findMostActiveTokens(Pageable pageable);

    // ==================== Bulk Operations ====================

    /**
     * Revoke all user tokens.
     * 
     * @param userId the user ID
     * @return number of revoked tokens
     */
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.id = :userId AND rt.deleted = false")
    int revokeAllUserTokens(@Param("userId") Long userId);

    /**
     * Revoke tokens by device.
     * 
     * @param userId the user ID
     * @param deviceId the device ID
     * @return number of revoked tokens
     */
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.id = :userId AND rt.deviceId = :deviceId AND rt.deleted = false")
    int revokeTokensByDevice(@Param("userId") Long userId, @Param("deviceId") String deviceId);

    /**
     * Revoke all user tokens except current.
     * 
     * @param userId the user ID
     * @param currentTokenId the current token ID to keep
     * @return number of revoked tokens
     */
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.id = :userId AND rt.id != :currentTokenId AND rt.deleted = false")
    int revokeAllUserTokensExcept(@Param("userId") Long userId, @Param("currentTokenId") Long currentTokenId);

    /**
     * Mark tokens for cleanup.
     * 
     * @param tokenIds list of token IDs
     * @return number of marked tokens
     */
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.markedForCleanup = true WHERE rt.id IN :tokenIds AND rt.deleted = false")
    int markForCleanup(@Param("tokenIds") List<Long> tokenIds);

    // ==================== Cleanup Operations ====================

    /**
     * Delete expired tokens.
     * 
     * @return number of deleted tokens
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate <= CURRENT_TIMESTAMP")
    int deleteExpiredTokens();

    /**
     * Delete revoked tokens.
     * 
     * @return number of deleted tokens
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.revoked = true")
    int deleteRevokedTokens();

    /**
     * Delete tokens marked for cleanup.
     * 
     * @return number of deleted tokens
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.markedForCleanup = true")
    int deleteMarkedForCleanup();

    /**
     * Cleanup old tokens.
     * 
     * @param cutoffDate the cutoff date
     * @return number of cleaned tokens
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE (rt.expiryDate <= CURRENT_TIMESTAMP OR rt.revoked = true OR rt.markedForCleanup = true) AND rt.createdAt < :cutoffDate")
    int cleanupOldTokens(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find tokens eligible for cleanup.
     * 
     * @return list of tokens eligible for cleanup
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiryDate <= CURRENT_TIMESTAMP OR rt.revoked = true OR rt.markedForCleanup = true ORDER BY rt.createdAt ASC")
    List<RefreshToken> findTokensEligibleForCleanup();

    // ==================== Statistics Queries ====================

    /**
     * Get token statistics by user.
     * 
     * @return list of [User, totalTokens, validTokens] arrays
     */
    @Query("SELECT rt.user, COUNT(rt), SUM(CASE WHEN rt.revoked = false AND rt.expiryDate > CURRENT_TIMESTAMP AND (rt.blockedUntil IS NULL OR rt.blockedUntil < CURRENT_TIMESTAMP) THEN 1 ELSE 0 END) FROM RefreshToken rt WHERE rt.deleted = false GROUP BY rt.user")
    List<Object[]> getTokenStatisticsByUser();

    /**
     * Get token statistics by device.
     * 
     * @return list of [deviceId, deviceName, tokenCount, activeCount] arrays
     */
    @Query("SELECT rt.deviceId, rt.deviceName, COUNT(rt), SUM(CASE WHEN rt.revoked = false AND rt.expiryDate > CURRENT_TIMESTAMP THEN 1 ELSE 0 END) FROM RefreshToken rt WHERE rt.deleted = false GROUP BY rt.deviceId, rt.deviceName")
    List<Object[]> getTokenStatisticsByDevice();

    /**
     * Get daily token creation counts.
     * 
     * @param startDate start date
     * @param endDate end date
     * @return list of [date, count] arrays
     */
    @Query(value = """
        SELECT DATE(created_at) as token_date, COUNT(*) as token_count
        FROM refresh_tokens 
        WHERE created_at BETWEEN :startDate AND :endDate AND deleted = false
        GROUP BY DATE(created_at)
        ORDER BY token_date
        """, nativeQuery = true)
    List<Object[]> getDailyTokenCreationCounts(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Update device information.
     * 
     * @param tokenId the token ID
     * @param deviceName the device name
     * @param userAgent the user agent
     * @return number of updated tokens
     */
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.deviceName = :deviceName, rt.userAgent = :userAgent WHERE rt.id = :tokenId AND rt.deleted = false")
    int updateDeviceInfo(@Param("tokenId") Long tokenId, @Param("deviceName") String deviceName, @Param("userAgent") String userAgent);
}