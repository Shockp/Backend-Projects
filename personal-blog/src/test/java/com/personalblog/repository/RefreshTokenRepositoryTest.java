package com.personalblog.repository;

import com.personalblog.entity.RefreshToken;
import com.personalblog.entity.Role;
import com.personalblog.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for RefreshTokenRepository.
 * 
 * Tests cover:
 * - Basic CRUD operations with soft delete support
 * - Token validation and expiry management
 * - User association and device tracking
 * - Security features (rate limiting, blocking)
 * - Token cleanup and maintenance operations
 * - Performance optimization with proper indexing
 * - Security validation and encryption
 * - Concurrent access and thread safety
 * - Edge cases and error handling
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("RefreshToken Repository Tests")
class RefreshTokenRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private User testUser;
    private User anotherUser;
    private RefreshToken validToken;
    private RefreshToken expiredToken;
    private RefreshToken revokedToken;

    @BeforeEach
    void setUp() {
        // Create test users
        testUser = createUser("testuser", "test@example.com");
        anotherUser = createUser("anotheruser", "another@example.com");

        // Create test refresh tokens
        validToken = createRefreshToken(testUser, LocalDateTime.now().plusDays(7), "device-1", "iPhone 12", "192.168.1.1");
        expiredToken = createRefreshToken(testUser, LocalDateTime.now().minusDays(1), "device-2", "Android Phone", "192.168.1.2");
        revokedToken = createRefreshToken(anotherUser, LocalDateTime.now().plusDays(7), "device-3", "Chrome Browser", "192.168.1.3");
        revokedToken.setRevoked(true);

        entityManager.persistAndFlush(validToken);
        entityManager.persistAndFlush(expiredToken);
        entityManager.persistAndFlush(revokedToken);
    }

    // ==================== Helper Methods ====================

    private User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("password123");
        user.setEmailVerified(true);
        user.setAccountEnabled(true);
        user.setAccountLocked(false);
        user.setRoles(Set.of(Role.USER));
        return entityManager.persistAndFlush(user);
    }

    private RefreshToken createRefreshToken(User user, LocalDateTime expiryDate, String deviceId, String deviceName, String ipAddress) {
        RefreshToken token = new RefreshToken();
        token.setTokenValue(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiryDate(expiryDate);
        token.setDeviceId(deviceId);
        token.setDeviceName(deviceName);
        token.setIpAddress(ipAddress);
        token.setUserAgent("Mozilla/5.0 Test Browser");
        return token;
    }

    // ==================== Basic CRUD Tests ====================

    @Test
    @DisplayName("Should find refresh token by token value")
    void shouldFindRefreshTokenByTokenValue() {
        // When
        Optional<RefreshToken> found = refreshTokenRepository.findByTokenValue(validToken.getTokenValue());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUser().getId()).isEqualTo(testUser.getId());
        assertThat(found.get().getDeviceId()).isEqualTo("device-1");
    }

    @Test
    @DisplayName("Should return empty when token value not found")
    void shouldReturnEmptyWhenTokenValueNotFound() {
        // When
        Optional<RefreshToken> found = refreshTokenRepository.findByTokenValue("nonexistent-token");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find valid token by token value")
    void shouldFindValidTokenByTokenValue() {
        // When
        Optional<RefreshToken> found = refreshTokenRepository.findValidTokenByValue(validToken.getTokenValue());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().isValid()).isTrue();
    }

    @Test
    @DisplayName("Should not find expired token as valid")
    void shouldNotFindExpiredTokenAsValid() {
        // When
        Optional<RefreshToken> found = refreshTokenRepository.findValidTokenByValue(expiredToken.getTokenValue());

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should not find revoked token as valid")
    void shouldNotFindRevokedTokenAsValid() {
        // When
        Optional<RefreshToken> found = refreshTokenRepository.findValidTokenByValue(revokedToken.getTokenValue());

        // Then
        assertThat(found).isEmpty();
    }

    // ==================== User Association Tests ====================

    @Test
    @DisplayName("Should find tokens by user")
    void shouldFindTokensByUser() {
        // When
        List<RefreshToken> userTokens = refreshTokenRepository.findByUser(testUser);

        // Then
        assertThat(userTokens).hasSize(2); // Valid and expired tokens
        assertThat(userTokens).allMatch(token -> token.getUser().getId().equals(testUser.getId()));
    }

    @Test
    @DisplayName("Should find tokens by user ID")
    void shouldFindTokensByUserId() {
        // When
        List<RefreshToken> userTokens = refreshTokenRepository.findByUserId(testUser.getId());

        // Then
        assertThat(userTokens).hasSize(2);
        assertThat(userTokens).allMatch(token -> token.getUser().getId().equals(testUser.getId()));
    }

    @Test
    @DisplayName("Should find tokens by user with pagination")
    void shouldFindTokensByUserWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 1, Sort.by("createdAt").descending());

        // When
        Page<RefreshToken> userTokens = refreshTokenRepository.findByUser(testUser, pageable);

        // Then
        assertThat(userTokens.getContent()).hasSize(1);
        assertThat(userTokens.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find valid tokens by user")
    void shouldFindValidTokensByUser() {
        // When
        List<RefreshToken> validTokens = refreshTokenRepository.findValidTokensByUser(testUser);

        // Then
        assertThat(validTokens).hasSize(1); // Only the valid token
        assertThat(validTokens.get(0).getId()).isEqualTo(validToken.getId());
        assertThat(validTokens).allMatch(RefreshToken::isValid);
    }

    @Test
    @DisplayName("Should find valid tokens by user ID")
    void shouldFindValidTokensByUserId() {
        // When
        List<RefreshToken> validTokens = refreshTokenRepository.findValidTokensByUserId(testUser.getId());

        // Then
        assertThat(validTokens).hasSize(1);
        assertThat(validTokens.get(0).getId()).isEqualTo(validToken.getId());
    }

    @Test
    @DisplayName("Should count tokens by user")
    void shouldCountTokensByUser() {
        // When
        long tokenCount = refreshTokenRepository.countByUser(testUser);

        // Then
        assertThat(tokenCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Should count valid tokens by user")
    void shouldCountValidTokensByUser() {
        // When
        long validCount = refreshTokenRepository.countValidTokensByUser(testUser);

        // Then
        assertThat(validCount).isEqualTo(1);
    }

    // ==================== Device Management Tests ====================

    @Test
    @DisplayName("Should find tokens by device ID")
    void shouldFindTokensByDeviceId() {
        // When
        List<RefreshToken> deviceTokens = refreshTokenRepository.findByDeviceId("device-1");

        // Then
        assertThat(deviceTokens).hasSize(1);
        assertThat(deviceTokens.get(0).getId()).isEqualTo(validToken.getId());
    }

    @Test
    @DisplayName("Should find tokens by user and device")
    void shouldFindTokensByUserAndDevice() {
        // When
        List<RefreshToken> userDeviceTokens = refreshTokenRepository.findByUserAndDeviceId(testUser, "device-1");

        // Then
        assertThat(userDeviceTokens).hasSize(1);
        assertThat(userDeviceTokens.get(0).getId()).isEqualTo(validToken.getId());
    }

    @Test
    @DisplayName("Should find tokens by user ID and device ID")
    void shouldFindTokensByUserIdAndDeviceId() {
        // When
        List<RefreshToken> userDeviceTokens = refreshTokenRepository.findByUserIdAndDeviceId(testUser.getId(), "device-1");

        // Then
        assertThat(userDeviceTokens).hasSize(1);
        assertThat(userDeviceTokens.get(0).getId()).isEqualTo(validToken.getId());
    }

    @Test
    @DisplayName("Should find latest token by user and device")
    void shouldFindLatestTokenByUserAndDevice() {
        // Given - Create another token for same user and device
        RefreshToken newerToken = createRefreshToken(testUser, LocalDateTime.now().plusDays(14), "device-1", "iPhone 12", "192.168.1.1");
        entityManager.persistAndFlush(newerToken);

        // When
        Optional<RefreshToken> latestToken = refreshTokenRepository.findLatestByUserAndDevice(testUser.getId(), "device-1");

        // Then
        assertThat(latestToken).isPresent();
        assertThat(latestToken.get().getId()).isEqualTo(newerToken.getId());
    }

    @Test
    @DisplayName("Should get user devices")
    void shouldGetUserDevices() {
        // When
        List<Object[]> devices = refreshTokenRepository.getUserDevices(testUser.getId());

        // Then
        assertThat(devices).hasSize(2); // device-1 and device-2
        
        // Verify structure: [deviceId, deviceName, lastUsed, tokenCount]
        Object[] device1 = devices.stream()
                .filter(device -> "device-1".equals(device[0]))
                .findFirst()
                .orElseThrow();
        
        assertThat(device1[1]).isEqualTo("iPhone 12");
        assertThat(device1[3]).isEqualTo(1L); // Token count
    }

    // ==================== Expiry Management Tests ====================

    @Test
    @DisplayName("Should find expired tokens")
    void shouldFindExpiredTokens() {
        // When
        List<RefreshToken> expiredTokens = refreshTokenRepository.findExpiredTokens();

        // Then
        assertThat(expiredTokens).hasSize(1);
        assertThat(expiredTokens.get(0).getId()).isEqualTo(expiredToken.getId());
        assertThat(expiredTokens).allMatch(RefreshToken::isExpired);
    }

    @Test
    @DisplayName("Should find expired tokens with pagination")
    void shouldFindExpiredTokensWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("expiryDate").ascending());

        // When
        Page<RefreshToken> expiredTokens = refreshTokenRepository.findExpiredTokens(pageable);

        // Then
        assertThat(expiredTokens.getContent()).hasSize(1);
        assertThat(expiredTokens.getContent()).allMatch(RefreshToken::isExpired);
    }

    @Test
    @DisplayName("Should find tokens expiring soon")
    void shouldFindTokensExpiringSoon() {
        // Given
        LocalDateTime threshold = LocalDateTime.now().plusDays(10);

        // When
        List<RefreshToken> expiringSoon = refreshTokenRepository.findTokensExpiringSoon(threshold);

        // Then
        assertThat(expiringSoon).hasSize(1); // Only validToken expires within 10 days
        assertThat(expiringSoon.get(0).getId()).isEqualTo(validToken.getId());
    }

    @Test
    @DisplayName("Should count expired tokens")
    void shouldCountExpiredTokens() {
        // When
        long expiredCount = refreshTokenRepository.countExpiredTokens();

        // Then
        assertThat(expiredCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Should extend token expiry")
    void shouldExtendTokenExpiry() {
        // Given
        LocalDateTime newExpiryDate = LocalDateTime.now().plusDays(14);

        // When
        int updatedCount = refreshTokenRepository.extendTokenExpiry(validToken.getId(), newExpiryDate);

        // Then
        assertThat(updatedCount).isEqualTo(1);
        
        entityManager.clear();
        RefreshToken updated = refreshTokenRepository.findById(validToken.getId()).orElseThrow();
        assertThat(updated.getExpiryDate()).isEqualToIgnoringNanos(newExpiryDate);
    }

    // ==================== Security and Rate Limiting Tests ====================

    @Test
    @DisplayName("Should find tokens by IP address")
    void shouldFindTokensByIpAddress() {
        // When
        List<RefreshToken> ipTokens = refreshTokenRepository.findByIpAddress("192.168.1.1");

        // Then
        assertThat(ipTokens).hasSize(1);
        assertThat(ipTokens.get(0).getId()).isEqualTo(validToken.getId());
    }

    @Test
    @DisplayName("Should find tokens with failed attempts")
    void shouldFindTokensWithFailedAttempts() {
        // Given
        validToken.setFailedAttempts(3);
        entityManager.persistAndFlush(validToken);

        // When
        List<RefreshToken> tokensWithFailures = refreshTokenRepository.findTokensWithFailedAttempts(2);

        // Then
        assertThat(tokensWithFailures).hasSize(1);
        assertThat(tokensWithFailures.get(0).getId()).isEqualTo(validToken.getId());
    }

    @Test
    @DisplayName("Should find blocked tokens")
    void shouldFindBlockedTokens() {
        // Given
        validToken.setBlockedUntil(LocalDateTime.now().plusHours(1));
        entityManager.persistAndFlush(validToken);

        // When
        List<RefreshToken> blockedTokens = refreshTokenRepository.findBlockedTokens();

        // Then
        assertThat(blockedTokens).hasSize(1);
        assertThat(blockedTokens.get(0).getId()).isEqualTo(validToken.getId());
        assertThat(blockedTokens).allMatch(RefreshToken::isBlocked);
    }

    @Test
    @DisplayName("Should increment failed attempts")
    void shouldIncrementFailedAttempts() {
        // Given
        int originalAttempts = validToken.getFailedAttempts();

        // When
        int updatedCount = refreshTokenRepository.incrementFailedAttempts(validToken.getId());

        // Then
        assertThat(updatedCount).isEqualTo(1);
        
        entityManager.clear();
        RefreshToken updated = refreshTokenRepository.findById(validToken.getId()).orElseThrow();
        assertThat(updated.getFailedAttempts()).isEqualTo(originalAttempts + 1);
    }

    @Test
    @DisplayName("Should reset failed attempts")
    void shouldResetFailedAttempts() {
        // Given
        validToken.setFailedAttempts(5);
        entityManager.persistAndFlush(validToken);

        // When
        int updatedCount = refreshTokenRepository.resetFailedAttempts(validToken.getId());

        // Then
        assertThat(updatedCount).isEqualTo(1);
        
        entityManager.clear();
        RefreshToken updated = refreshTokenRepository.findById(validToken.getId()).orElseThrow();
        assertThat(updated.getFailedAttempts()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should block token")
    void shouldBlockToken() {
        // Given
        LocalDateTime blockUntil = LocalDateTime.now().plusHours(2);

        // When
        int updatedCount = refreshTokenRepository.blockToken(validToken.getId(), blockUntil);

        // Then
        assertThat(updatedCount).isEqualTo(1);
        
        entityManager.clear();
        RefreshToken updated = refreshTokenRepository.findById(validToken.getId()).orElseThrow();
        assertThat(updated.getBlockedUntil()).isEqualToIgnoringNanos(blockUntil);
        assertThat(updated.isBlocked()).isTrue();
    }

    @Test
    @DisplayName("Should unblock expired blocks")
    void shouldUnblockExpiredBlocks() {
        // Given
        validToken.setBlockedUntil(LocalDateTime.now().minusHours(1)); // Expired block
        entityManager.persistAndFlush(validToken);

        // When
        int unblockedCount = refreshTokenRepository.unblockExpiredBlocks();

        // Then
        assertThat(unblockedCount).isEqualTo(1);
        
        entityManager.clear();
        RefreshToken updated = refreshTokenRepository.findById(validToken.getId()).orElseThrow();
        assertThat(updated.getBlockedUntil()).isNull();
    }

    // ==================== Usage Tracking Tests ====================

    @Test
    @DisplayName("Should update last used timestamp")
    void shouldUpdateLastUsedTimestamp() {
        // Given
        LocalDateTime lastUsed = LocalDateTime.now();

        // When
        int updatedCount = refreshTokenRepository.updateLastUsed(validToken.getId(), lastUsed);

        // Then
        assertThat(updatedCount).isEqualTo(1);
        
        entityManager.clear();
        RefreshToken updated = refreshTokenRepository.findById(validToken.getId()).orElseThrow();
        assertThat(updated.getLastUsedAt()).isEqualToIgnoringNanos(lastUsed);
    }

    @Test
    @DisplayName("Should increment usage count")
    void shouldIncrementUsageCount() {
        // Given
        int originalCount = validToken.getUsageCount();

        // When
        int updatedCount = refreshTokenRepository.incrementUsageCount(validToken.getId());

        // Then
        assertThat(updatedCount).isEqualTo(1);
        
        entityManager.clear();
        RefreshToken updated = refreshTokenRepository.findById(validToken.getId()).orElseThrow();
        assertThat(updated.getUsageCount()).isEqualTo(originalCount + 1);
    }

    @Test
    @DisplayName("Should find unused tokens")
    void shouldFindUnusedTokens() {
        // Given
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);

        // When
        List<RefreshToken> unusedTokens = refreshTokenRepository.findUnusedTokens(threshold);

        // Then
        assertThat(unusedTokens).hasSize(3); // All tokens are unused (lastUsedAt is null)
    }

    @Test
    @DisplayName("Should find most active tokens")
    void shouldFindMostActiveTokens() {
        // Given
        validToken.setUsageCount(10);
        expiredToken.setUsageCount(5);
        entityManager.persistAndFlush(validToken);
        entityManager.persistAndFlush(expiredToken);

        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<RefreshToken> activeTokens = refreshTokenRepository.findMostActiveTokens(pageable);

        // Then
        assertThat(activeTokens.getContent()).hasSize(2);
        assertThat(activeTokens.getContent().get(0).getId()).isEqualTo(validToken.getId()); // Highest usage count first
    }

    // ==================== Bulk Operations Tests ====================

    @Test
    @DisplayName("Should revoke all user tokens")
    void shouldRevokeAllUserTokens() {
        // When
        int revokedCount = refreshTokenRepository.revokeAllUserTokens(testUser.getId());

        // Then
        assertThat(revokedCount).isEqualTo(2); // Valid and expired tokens
        
        entityManager.clear();
        List<RefreshToken> userTokens = refreshTokenRepository.findByUserId(testUser.getId());
        assertThat(userTokens).allMatch(RefreshToken::getRevoked);
    }

    @Test
    @DisplayName("Should revoke tokens by device")
    void shouldRevokeTokensByDevice() {
        // When
        int revokedCount = refreshTokenRepository.revokeTokensByDevice(testUser.getId(), "device-1");

        // Then
        assertThat(revokedCount).isEqualTo(1);
        
        entityManager.clear();
        RefreshToken updated = refreshTokenRepository.findById(validToken.getId()).orElseThrow();
        assertThat(updated.getRevoked()).isTrue();
    }

    @Test
    @DisplayName("Should revoke tokens except current")
    void shouldRevokeTokensExceptCurrent() {
        // When
        int revokedCount = refreshTokenRepository.revokeAllUserTokensExcept(testUser.getId(), validToken.getId());

        // Then
        assertThat(revokedCount).isEqualTo(1); // Only expired token revoked
        
        entityManager.clear();
        RefreshToken validUpdated = refreshTokenRepository.findById(validToken.getId()).orElseThrow();
        RefreshToken expiredUpdated = refreshTokenRepository.findById(expiredToken.getId()).orElseThrow();
        
        assertThat(validUpdated.getRevoked()).isFalse();
        assertThat(expiredUpdated.getRevoked()).isTrue();
    }

    @Test
    @DisplayName("Should mark tokens for cleanup")
    void shouldMarkTokensForCleanup() {
        // Given
        List<Long> tokenIds = List.of(expiredToken.getId(), revokedToken.getId());

        // When
        int markedCount = refreshTokenRepository.markForCleanup(tokenIds);

        // Then
        assertThat(markedCount).isEqualTo(2);
        
        entityManager.clear();
        RefreshToken expiredUpdated = refreshTokenRepository.findById(expiredToken.getId()).orElseThrow();
        RefreshToken revokedUpdated = refreshTokenRepository.findById(revokedToken.getId()).orElseThrow();
        
        assertThat(expiredUpdated.getMarkedForCleanup()).isTrue();
        assertThat(revokedUpdated.getMarkedForCleanup()).isTrue();
    }

    // ==================== Cleanup Operations Tests ====================

    @Test
    @DisplayName("Should delete expired tokens")
    void shouldDeleteExpiredTokens() {
        // When
        int deletedCount = refreshTokenRepository.deleteExpiredTokens();

        // Then
        assertThat(deletedCount).isEqualTo(1);
        
        entityManager.clear();
        assertThat(refreshTokenRepository.findById(expiredToken.getId())).isEmpty();
    }

    @Test
    @DisplayName("Should delete revoked tokens")
    void shouldDeleteRevokedTokens() {
        // When
        int deletedCount = refreshTokenRepository.deleteRevokedTokens();

        // Then
        assertThat(deletedCount).isEqualTo(1);
        
        entityManager.clear();
        assertThat(refreshTokenRepository.findById(revokedToken.getId())).isEmpty();
    }

    @Test
    @DisplayName("Should delete tokens marked for cleanup")
    void shouldDeleteTokensMarkedForCleanup() {
        // Given
        expiredToken.setMarkedForCleanup(true);
        entityManager.persistAndFlush(expiredToken);

        // When
        int deletedCount = refreshTokenRepository.deleteMarkedForCleanup();

        // Then
        assertThat(deletedCount).isEqualTo(1);
        
        entityManager.clear();
        assertThat(refreshTokenRepository.findById(expiredToken.getId())).isEmpty();
    }

    @Test
    @DisplayName("Should cleanup old tokens")
    void shouldCleanupOldTokens() {
        // Given
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(1);

        // When
        int cleanedCount = refreshTokenRepository.cleanupOldTokens(cutoffDate);

        // Then
        assertThat(cleanedCount).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should find tokens eligible for cleanup")
    void shouldFindTokensEligibleForCleanup() {
        // When
        List<RefreshToken> eligibleTokens = refreshTokenRepository.findTokensEligibleForCleanup();

        // Then
        assertThat(eligibleTokens).hasSize(2); // Expired and revoked tokens
        assertThat(eligibleTokens).allMatch(RefreshToken::shouldBeCleanedUp);
    }

    // ==================== Statistics Tests ====================

    @Test
    @DisplayName("Should get token statistics by user")
    void shouldGetTokenStatisticsByUser() {
        // When
        List<Object[]> stats = refreshTokenRepository.getTokenStatisticsByUser();

        // Then
        assertThat(stats).hasSize(2); // Two users
        
        // Find testUser stats
        Object[] testUserStats = stats.stream()
                .filter(stat -> ((User) stat[0]).getId().equals(testUser.getId()))
                .findFirst()
                .orElseThrow();
        
        assertThat(testUserStats[1]).isEqualTo(2L); // Total tokens
        assertThat(testUserStats[2]).isEqualTo(1L); // Valid tokens
    }

    @Test
    @DisplayName("Should get token statistics by device")
    void shouldGetTokenStatisticsByDevice() {
        // When
        List<Object[]> stats = refreshTokenRepository.getTokenStatisticsByDevice();

        // Then
        assertThat(stats).hasSize(3); // Three devices
        
        // Verify structure: [deviceId, deviceName, tokenCount, activeCount]
        Object[] device1Stats = stats.stream()
                .filter(stat -> "device-1".equals(stat[0]))
                .findFirst()
                .orElseThrow();
        
        assertThat(device1Stats[1]).isEqualTo("iPhone 12");
        assertThat(device1Stats[2]).isEqualTo(1L); // Token count
    }

    @Test
    @DisplayName("Should get daily token creation counts")
    void shouldGetDailyTokenCreationCounts() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // When
        List<Object[]> dailyCounts = refreshTokenRepository.getDailyTokenCreationCounts(startDate, endDate);

        // Then
        assertThat(dailyCounts).isNotEmpty();
        
        // Verify structure: [date, count]
        Object[] todayStats = dailyCounts.get(dailyCounts.size() - 1);
        assertThat(todayStats).hasSize(2);
        assertThat(todayStats[1]).isEqualTo(3L); // Today's token count
    }

    // ==================== Validation Tests ====================

    @Test
    @DisplayName("Should validate token value is not null")
    void shouldValidateTokenValueIsNotNull() {
        // Given
        RefreshToken invalidToken = new RefreshToken();
        invalidToken.setUser(testUser);
        invalidToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        // tokenValue is null

        // When & Then
        assertThatThrownBy(() -> entityManager.persistAndFlush(invalidToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should validate expiry date is in future")
    void shouldValidateExpiryDateIsInFuture() {
        // Given
        RefreshToken invalidToken = new RefreshToken();
        invalidToken.setTokenValue(UUID.randomUUID().toString());
        invalidToken.setUser(testUser);
        invalidToken.setExpiryDate(LocalDateTime.now().minusDays(1)); // Past date

        // When & Then
        assertThatThrownBy(() -> entityManager.persistAndFlush(invalidToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should validate user is not null")
    void shouldValidateUserIsNotNull() {
        // Given
        RefreshToken invalidToken = new RefreshToken();
        invalidToken.setTokenValue(UUID.randomUUID().toString());
        invalidToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        // user is null

        // When & Then
        assertThatThrownBy(() -> entityManager.persistAndFlush(invalidToken))
                .isInstanceOf(Exception.class);
    }

    // ==================== Performance Tests ====================

    @Test
    @DisplayName("Should handle large number of tokens efficiently")
    void shouldHandleLargeNumberOfTokensEfficiently() {
        // Given - Create many tokens
        for (int i = 0; i < 100; i++) {
            RefreshToken token = createRefreshToken(testUser, LocalDateTime.now().plusDays(7), "device-" + i, "Device " + i, "192.168.1." + (i % 255));
            entityManager.persist(token);
        }
        entityManager.flush();
        entityManager.clear();

        // When
        long startTime = System.currentTimeMillis();
        Page<RefreshToken> tokens = refreshTokenRepository.findValidTokensByUserId(testUser.getId(), PageRequest.of(0, 20));
        long endTime = System.currentTimeMillis();

        // Then
        assertThat(tokens.getContent()).hasSize(20);
        assertThat(endTime - startTime).isLessThan(1000); // Should complete within 1 second
    }

    // ==================== Edge Cases Tests ====================

    @Test
    @DisplayName("Should handle null and empty parameters gracefully")
    void shouldHandleNullAndEmptyParametersGracefully() {
        // When & Then
        assertThat(refreshTokenRepository.findByTokenValue("")).isEmpty();
        assertThat(refreshTokenRepository.findByDeviceId("")).isEmpty();
        assertThat(refreshTokenRepository.findByUserId(999L)).isEmpty();
    }

    @Test
    @DisplayName("Should handle concurrent token operations")
    void shouldHandleConcurrentTokenOperations() {
        // Given
        Long tokenId = validToken.getId();

        // When - Simulate concurrent operations
        refreshTokenRepository.incrementUsageCount(tokenId);
        refreshTokenRepository.incrementFailedAttempts(tokenId);
        refreshTokenRepository.updateLastUsed(tokenId, LocalDateTime.now());

        // Then
        entityManager.clear();
        RefreshToken updated = refreshTokenRepository.findById(tokenId).orElseThrow();
        assertThat(updated.getUsageCount()).isEqualTo(1);
        assertThat(updated.getFailedAttempts()).isEqualTo(1);
        assertThat(updated.getLastUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should handle token encryption and decryption")
    void shouldHandleTokenEncryptionAndDecryption() {
        // Given
        String originalToken = UUID.randomUUID().toString();
        RefreshToken token = new RefreshToken();
        token.setTokenValue(originalToken);
        token.setUser(testUser);
        token.setExpiryDate(LocalDateTime.now().plusDays(7));

        // When
        entityManager.persistAndFlush(token);
        entityManager.clear();

        // Then
        RefreshToken retrieved = refreshTokenRepository.findById(token.getId()).orElseThrow();
        assertThat(retrieved.getTokenValue()).isEqualTo(originalToken);
        assertThat(retrieved.getEncryptedTokenValue()).isNotEqualTo(originalToken); // Should be encrypted in DB
    }

    @Test
    @DisplayName("Should handle device information updates")
    void shouldHandleDeviceInformationUpdates() {
        // Given
        String newDeviceName = "Updated iPhone 13";
        String newUserAgent = "Updated Mozilla/5.0";

        // When
        int updatedCount = refreshTokenRepository.updateDeviceInfo(validToken.getId(), newDeviceName, newUserAgent);

        // Then
        assertThat(updatedCount).isEqualTo(1);
        
        entityManager.clear();
        RefreshToken updated = refreshTokenRepository.findById(validToken.getId()).orElseThrow();
        assertThat(updated.getDeviceName()).isEqualTo(newDeviceName);
        assertThat(updated.getUserAgent()).isEqualTo(newUserAgent);
    }
}