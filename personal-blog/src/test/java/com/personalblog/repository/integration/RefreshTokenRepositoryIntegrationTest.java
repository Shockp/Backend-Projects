package com.personalblog.repository.integration;

import com.personalblog.entity.RefreshToken;
import com.personalblog.entity.Role;
import com.personalblog.entity.User;
import com.personalblog.repository.RefreshTokenRepository;
import com.personalblog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for RefreshTokenRepository using TestContainers with PostgreSQL.
 * 
 * <p>
 * These tests verify token management, security features, and concurrent
 * access scenarios with actual PostgreSQL database.
 * </p>
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @since 1.0.0
 */
@DisplayName("RefreshTokenRepository Integration Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RefreshTokenRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        // Clean up any existing data
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        testUser1 = new User();
        testUser1.setUsername("tokenuser1");
        testUser1.setEmail("tokenuser1@example.com");
        testUser1.setPassword("hashedpassword");
        testUser1.setAccountEnabled(true);
        testUser1.setEmailVerified(true);
        testUser1.setRoles(Set.of(Role.USER));
        testUser1 = userRepository.save(testUser1);

        testUser2 = new User();
        testUser2.setUsername("tokenuser2");
        testUser2.setEmail("tokenuser2@example.com");
        testUser2.setPassword("hashedpassword");
        testUser2.setAccountEnabled(true);
        testUser2.setEmailVerified(true);
        testUser2.setRoles(Set.of(Role.USER));
        testUser2 = userRepository.save(testUser2);
    }

    private RefreshToken createRefreshToken(User user, String deviceId, boolean expired, boolean revoked) {
        RefreshToken token = new RefreshToken();
        token.setTokenValue(UUID.randomUUID().toString());
        token.setUser(user);
        token.setDeviceId(deviceId);
        token.setDeviceName("Test Device");
        token.setIpAddress("192.168.1.100");
        token.setUserAgent("Test User Agent");
        token.setExpiryDate(expired ? LocalDateTime.now().minusDays(1) : LocalDateTime.now().plusDays(7));
        token.setExpired(expired);
        token.setRevoked(revoked);
        token.setFailedAttempts(0);
        token.setLastUsedAt(LocalDateTime.now());
        token.setMarkedForCleanup(false);
        return token;
    }

    @Nested
    @DisplayName("Token Validation")
    class TokenValidation {

        @Test
        @DisplayName("Should find valid tokens")
        @Transactional
        void shouldFindValidTokens() {
            // Given
            RefreshToken validToken = refreshTokenRepository.save(
                    createRefreshToken(testUser1, "device1", false, false));
            RefreshToken expiredToken = refreshTokenRepository.save(
                    createRefreshToken(testUser1, "device2", true, false));
            RefreshToken revokedToken = refreshTokenRepository.save(
                    createRefreshToken(testUser1, "device3", false, true));

            // When
            Optional<RefreshToken> foundValid = refreshTokenRepository.findValidTokenByValue(validToken.getTokenValue());
            Optional<RefreshToken> foundExpired = refreshTokenRepository.findValidTokenByValue(expiredToken.getTokenValue());
            Optional<RefreshToken> foundRevoked = refreshTokenRepository.findValidTokenByValue(revokedToken.getTokenValue());

            // Then
            assertThat(foundValid).isPresent();
            assertThat(foundValid.get().getTokenValue()).isEqualTo(validToken.getTokenValue());

            assertThat(foundExpired).isEmpty();
            assertThat(foundRevoked).isEmpty();
        }

        @Test
        @DisplayName("Should validate token expiry dates")
        @Transactional
        void shouldValidateTokenExpiryDates() {
            // Given
            RefreshToken futureToken = createRefreshToken(testUser1, "device1", false, false);
            futureToken.setExpiryDate(LocalDateTime.now().plusHours(1));
            futureToken = refreshTokenRepository.save(futureToken);

            RefreshToken pastToken = createRefreshToken(testUser1, "device2", false, false);
            pastToken.setExpiryDate(LocalDateTime.now().minusHours(1));
            pastToken = refreshTokenRepository.save(pastToken);

            // When
            Optional<RefreshToken> validFutureToken = refreshTokenRepository.findValidTokenByValue(futureToken.getTokenValue());
            Optional<RefreshToken> invalidPastToken = refreshTokenRepository.findValidTokenByValue(pastToken.getTokenValue());

            // Then
            assertThat(validFutureToken).isPresent();
            assertThat(invalidPastToken).isEmpty();
        }

        @Test
        @DisplayName("Should handle soft deleted tokens")
        @Transactional
        void shouldHandleSoftDeletedTokens() {
            // Given
            RefreshToken token = refreshTokenRepository.save(
                    createRefreshToken(testUser1, "device1", false, false));
            
            token.markAsDeleted();
            refreshTokenRepository.save(token);

            // When
            Optional<RefreshToken> foundToken = refreshTokenRepository.findValidTokenByValue(token.getTokenValue());

            // Then
            assertThat(foundToken).isEmpty();
        }
    }

    @Nested
    @DisplayName("User Session Management")
    class UserSessionManagement {

        @Test
        @DisplayName("Should find active tokens by user")
        @Transactional
        void shouldFindActiveTokensByUser() {
            // Given
            RefreshToken activeToken1 = refreshTokenRepository.save(
                    createRefreshToken(testUser1, "device1", false, false));
            RefreshToken activeToken2 = refreshTokenRepository.save(
                    createRefreshToken(testUser1, "device2", false, false));
            RefreshToken revokedToken = refreshTokenRepository.save(
                    createRefreshToken(testUser1, "device3", false, true));
            RefreshToken otherUserToken = refreshTokenRepository.save(
                    createRefreshToken(testUser2, "device4", false, false));

            // When
            List<RefreshToken> user1ActiveTokens = refreshTokenRepository.findValidTokensByUserId(testUser1.getId());
            List<RefreshToken> user2ActiveTokens = refreshTokenRepository.findValidTokensByUserId(testUser2.getId());

            // Then
            assertThat(user1ActiveTokens).hasSize(2);
            assertThat(user1ActiveTokens)
                    .extracting(RefreshToken::getDeviceId)
                    .containsExactlyInAnyOrder("device1", "device2");

            assertThat(user2ActiveTokens).hasSize(1);
            assertThat(user2ActiveTokens.get(0).getDeviceId()).isEqualTo("device4");
        }

        @Test
        @DisplayName("Should find tokens by user and device")
        @Transactional
        void shouldFindTokensByUserAndDevice() {
            // Given
            String deviceId = "unique-device";
            RefreshToken userToken = refreshTokenRepository.save(
                    createRefreshToken(testUser1, deviceId, false, false));
            RefreshToken otherUserToken = refreshTokenRepository.save(
                    createRefreshToken(testUser2, deviceId, false, false));

            // When
            List<RefreshToken> user1DeviceTokens = refreshTokenRepository.findByUserIdAndDeviceId(
                    testUser1.getId(), deviceId);
            List<RefreshToken> user2DeviceTokens = refreshTokenRepository.findByUserIdAndDeviceId(
                    testUser2.getId(), deviceId);

            // Then
            assertThat(user1DeviceTokens).hasSize(1);
            assertThat(user1DeviceTokens.get(0).getUser().getId()).isEqualTo(testUser1.getId());

            assertThat(user2DeviceTokens).hasSize(1);
            assertThat(user2DeviceTokens.get(0).getUser().getId()).isEqualTo(testUser2.getId());
        }

        @Test
        @DisplayName("Should order tokens by last used date")
        @Transactional
        void shouldOrderTokensByLastUsedDate() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            
            RefreshToken oldToken = createRefreshToken(testUser1, "device1", false, false);
            oldToken.setLastUsedAt(now.minusHours(2));
            oldToken = refreshTokenRepository.save(oldToken);

            RefreshToken recentToken = createRefreshToken(testUser1, "device2", false, false);
            recentToken.setLastUsedAt(now.minusMinutes(30));
            recentToken = refreshTokenRepository.save(recentToken);

            RefreshToken newestToken = createRefreshToken(testUser1, "device3", false, false);
            newestToken.setLastUsedAt(now);
            newestToken = refreshTokenRepository.save(newestToken);

            // When
            List<RefreshToken> activeTokens = refreshTokenRepository.findActiveTokensByUserId(testUser1.getId());

            // Then
            assertThat(activeTokens).hasSize(3);
            assertThat(activeTokens.get(0).getDeviceId()).isEqualTo("device3"); // Most recent first
            assertThat(activeTokens.get(1).getDeviceId()).isEqualTo("device2");
            assertThat(activeTokens.get(2).getDeviceId()).isEqualTo("device1");
        }
    }

    @Nested
    @DisplayName("Security Monitoring")
    class SecurityMonitoring {

        @Test
        @DisplayName("Should track tokens by IP address")
        @Transactional
        void shouldTrackTokensByIpAddress() {
            // Given
            String suspiciousIp = "192.168.1.200";
            
            RefreshToken normalToken = createRefreshToken(testUser1, "device1", false, false);
            normalToken.setIpAddress("192.168.1.100");
            refreshTokenRepository.save(normalToken);

            RefreshToken suspiciousToken1 = createRefreshToken(testUser1, "device2", false, false);
            suspiciousToken1.setIpAddress(suspiciousIp);
            refreshTokenRepository.save(suspiciousToken1);

            RefreshToken suspiciousToken2 = createRefreshToken(testUser2, "device3", false, false);
            suspiciousToken2.setIpAddress(suspiciousIp);
            refreshTokenRepository.save(suspiciousToken2);

            // When
            List<RefreshToken> tokensFromSuspiciousIp = refreshTokenRepository.findByIpAddress(suspiciousIp);

            // Then
            assertThat(tokensFromSuspiciousIp).hasSize(2);
            assertThat(tokensFromSuspiciousIp)
                    .allMatch(token -> suspiciousIp.equals(token.getIpAddress()));
        }

        @Test
        @DisplayName("Should find tokens with high failed attempts")
        @Transactional
        void shouldFindTokensWithHighFailedAttempts() {
            // Given
            RefreshToken normalToken = createRefreshToken(testUser1, "device1", false, false);
            normalToken.setFailedAttempts(2);
            refreshTokenRepository.save(normalToken);

            RefreshToken suspiciousToken1 = createRefreshToken(testUser1, "device2", false, false);
            suspiciousToken1.setFailedAttempts(5);
            refreshTokenRepository.save(suspiciousToken1);

            RefreshToken suspiciousToken2 = createRefreshToken(testUser2, "device3", false, false);
            suspiciousToken2.setFailedAttempts(10);
            refreshTokenRepository.save(suspiciousToken2);

            // When
            List<RefreshToken> suspiciousTokens = refreshTokenRepository.findTokensWithFailedAttempts(5);

            // Then
            assertThat(suspiciousTokens).hasSize(2);
            assertThat(suspiciousTokens)
                    .allMatch(token -> token.getFailedAttempts() >= 5);
        }
    }

    @Nested
    @DisplayName("Cleanup Operations")
    class CleanupOperations {

        @Test
        @DisplayName("Should find expired and marked tokens for cleanup")
        @Transactional
        void shouldFindExpiredAndMarkedTokensForCleanup() {
            // Given
            RefreshToken validToken = refreshTokenRepository.save(
                    createRefreshToken(testUser1, "device1", false, false));

            RefreshToken expiredToken = createRefreshToken(testUser1, "device2", true, false);
            expiredToken.setExpiryDate(LocalDateTime.now().minusDays(1));
            refreshTokenRepository.save(expiredToken);

            RefreshToken markedToken = createRefreshToken(testUser1, "device3", false, false);
            markedToken.setMarkedForCleanup(true);
            refreshTokenRepository.save(markedToken);

            // When
            List<RefreshToken> tokensForCleanup = refreshTokenRepository.findTokensEligibleForCleanup();

            // Then
            assertThat(tokensForCleanup).hasSize(2);
            assertThat(tokensForCleanup)
                    .extracting(RefreshToken::getDeviceId)
                    .containsExactlyInAnyOrder("device2", "device3");
        }

        @Test
        @DisplayName("Should permanently delete old soft-deleted tokens")
        @Transactional
        void shouldPermanentlyDeleteOldSoftDeletedTokens() {
            // Given
            RefreshToken recentDeletedToken = createRefreshToken(testUser1, "device1", false, false);
            recentDeletedToken.markAsDeleted();
            recentDeletedToken.setUpdatedAt(LocalDateTime.now().minusDays(1));
            refreshTokenRepository.save(recentDeletedToken);

            RefreshToken oldDeletedToken = createRefreshToken(testUser1, "device2", false, false);
            oldDeletedToken.markAsDeleted();
            oldDeletedToken.setUpdatedAt(LocalDateTime.now().minusDays(10));
            refreshTokenRepository.save(oldDeletedToken);

            RefreshToken activeToken = refreshTokenRepository.save(
                    createRefreshToken(testUser1, "device3", false, false));

            // When
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
            int deletedCount = refreshTokenRepository.cleanupOldTokens(cutoffDate);

            // Then
            assertThat(deletedCount).isEqualTo(1); // Only old deleted token

            // Verify remaining tokens
            List<RefreshToken> remainingTokens = refreshTokenRepository.findAll();
            assertThat(remainingTokens).hasSize(2); // Recent deleted + active
        }
    }

    @Nested
    @DisplayName("Bulk Operations")
    class BulkOperations {

        @Test
        @DisplayName("Should revoke all user tokens")
        @Transactional
        void shouldRevokeAllUserTokens() {
            // Given
            refreshTokenRepository.save(createRefreshToken(testUser1, "device1", false, false));
            refreshTokenRepository.save(createRefreshToken(testUser1, "device2", false, false));
            refreshTokenRepository.save(createRefreshToken(testUser2, "device3", false, false));

            // When
            int revokedCount = refreshTokenRepository.revokeAllUserTokens(testUser1.getId());

            // Then
            assertThat(revokedCount).isEqualTo(2);

            // Verify user1 tokens are revoked
            List<RefreshToken> user1Tokens = refreshTokenRepository.findValidTokensByUserId(testUser1.getId());
            assertThat(user1Tokens).isEmpty();

            // Verify user2 tokens are still active
            List<RefreshToken> user2Tokens = refreshTokenRepository.findValidTokensByUserId(testUser2.getId());
            assertThat(user2Tokens).hasSize(1);
        }

        @Test
        @DisplayName("Should handle bulk operations with large datasets")
        @Transactional
        void shouldHandleBulkOperationsWithLargeDatasets() {
            // Given - Create many tokens for user1
            int numberOfTokens = 50;
            for (int i = 0; i < numberOfTokens; i++) {
                refreshTokenRepository.save(createRefreshToken(testUser1, "device" + i, false, false));
            }

            // Create some tokens for user2
            refreshTokenRepository.save(createRefreshToken(testUser2, "device100", false, false));
            refreshTokenRepository.save(createRefreshToken(testUser2, "device101", false, false));

            // When
            int revokedCount = refreshTokenRepository.revokeAllUserTokens(testUser1.getId());

            // Then
            assertThat(revokedCount).isEqualTo(numberOfTokens);

            // Verify all user1 tokens are revoked
            List<RefreshToken> user1ActiveTokens = refreshTokenRepository.findValidTokensByUserId(testUser1.getId());
            assertThat(user1ActiveTokens).isEmpty();

            // Verify user2 tokens are unaffected
            List<RefreshToken> user2ActiveTokens = refreshTokenRepository.findValidTokensByUserId(testUser2.getId());
            assertThat(user2ActiveTokens).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Concurrent Operations")
    class ConcurrentOperations {

        @Test
        @DisplayName("Should handle concurrent token creation")
        void shouldHandleConcurrentTokenCreation() throws InterruptedException {
            // Given
            ExecutorService executor = Executors.newFixedThreadPool(5);
            int numberOfTokens = 20;

            // When - Concurrent token creation for same user
            CompletableFuture<RefreshToken>[] futures = new CompletableFuture[numberOfTokens];
            for (int i = 0; i < numberOfTokens; i++) {
                final int tokenIndex = i;
                futures[i] = CompletableFuture.supplyAsync(() -> {
                    RefreshToken token = createRefreshToken(testUser1, "device" + tokenIndex, false, false);
                    return refreshTokenRepository.save(token);
                }, executor);
            }

            CompletableFuture.allOf(futures).join();
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);

            // Then
            List<RefreshToken> userTokens = refreshTokenRepository.findValidTokensByUserId(testUser1.getId());
            assertThat(userTokens).hasSize(numberOfTokens);

            // Verify all device IDs are unique
            Set<String> deviceIds = userTokens.stream()
                    .map(RefreshToken::getDeviceId)
                    .collect(java.util.stream.Collectors.toSet());
            assertThat(deviceIds).hasSize(numberOfTokens);
        }

        @Test
        @DisplayName("Should handle concurrent token validation")
        void shouldHandleConcurrentTokenValidation() throws InterruptedException, ExecutionException {
            // Given
            RefreshToken token = refreshTokenRepository.save(
                    createRefreshToken(testUser1, "device1", false, false));
            
            ExecutorService executor = Executors.newFixedThreadPool(10);
            int numberOfValidations = 100;

            // When - Concurrent token validations
            CompletableFuture<Optional<RefreshToken>>[] futures = new CompletableFuture[numberOfValidations];
            for (int i = 0; i < numberOfValidations; i++) {
                futures[i] = CompletableFuture.supplyAsync(() -> {
                    return refreshTokenRepository.findValidTokenByValue(token.getTokenValue());
                }, executor);
            }

            CompletableFuture.allOf(futures).join();
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);

            // Then - All validations should succeed
            for (CompletableFuture<Optional<RefreshToken>> future : futures) {
                Optional<RefreshToken> result = future.get();
                assertThat(result).isPresent();
                assertThat(result.get().getTokenValue()).isEqualTo(token.getTokenValue());
            }
        }

        @Test
        @DisplayName("Should handle concurrent revocation operations")
        void shouldHandleConcurrentRevocationOperations() throws InterruptedException, ExecutionException {
            // Given - Create tokens for multiple users
            for (int i = 0; i < 5; i++) {
                refreshTokenRepository.save(createRefreshToken(testUser1, "user1-device" + i, false, false));
                refreshTokenRepository.save(createRefreshToken(testUser2, "user2-device" + i, false, false));
            }

            ExecutorService executor = Executors.newFixedThreadPool(2);

            // When - Concurrent revocation for both users
            CompletableFuture<Integer> user1Revocation = CompletableFuture.supplyAsync(() -> {
                return refreshTokenRepository.revokeAllUserTokens(testUser1.getId());
            }, executor);

            CompletableFuture<Integer> user2Revocation = CompletableFuture.supplyAsync(() -> {
                return refreshTokenRepository.revokeAllUserTokens(testUser2.getId());
            }, executor);

            CompletableFuture.allOf(user1Revocation, user2Revocation).join();
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);

            // Then
            assertThat(user1Revocation.get()).isEqualTo(5);
            assertThat(user2Revocation.get()).isEqualTo(5);

            // Verify all tokens are revoked
            List<RefreshToken> user1ActiveTokens = refreshTokenRepository.findValidTokensByUserId(testUser1.getId());
            List<RefreshToken> user2ActiveTokens = refreshTokenRepository.findValidTokensByUserId(testUser2.getId());

            assertThat(user1ActiveTokens).isEmpty();
            assertThat(user2ActiveTokens).isEmpty();
        }
    }

    @Nested
    @DisplayName("Performance and Optimization")
    class PerformanceAndOptimization {

        @Test
        @DisplayName("Should efficiently handle large token collections")
        @Transactional
        void shouldEfficientlyHandleLargeTokenCollections() {
            // Given - Create many tokens
            int numberOfTokens = 100;
            for (int i = 0; i < numberOfTokens; i++) {
                User user = i < 50 ? testUser1 : testUser2;
                refreshTokenRepository.save(createRefreshToken(user, "device" + i, false, false));
            }

            // When - Query operations
            long startTime = System.currentTimeMillis();
            
            List<RefreshToken> user1Tokens = refreshTokenRepository.findValidTokensByUserId(testUser1.getId());
            List<RefreshToken> user2Tokens = refreshTokenRepository.findValidTokensByUserId(testUser2.getId());
            List<RefreshToken> allTokens = refreshTokenRepository.findAll();
            
            long endTime = System.currentTimeMillis();

            // Then
            assertThat(user1Tokens).hasSize(50);
            assertThat(user2Tokens).hasSize(50);
            assertThat(allTokens).hasSize(numberOfTokens);
            assertThat(endTime - startTime).isLessThan(1000); // Should complete within 1 second
        }

        @Test
        @DisplayName("Should optimize cleanup operations")
        @Transactional
        void shouldOptimizeCleanupOperations() {
            // Given - Create tokens with various states
            LocalDateTime now = LocalDateTime.now();
            
            // Valid tokens
            for (int i = 0; i < 20; i++) {
                refreshTokenRepository.save(createRefreshToken(testUser1, "valid" + i, false, false));
            }
            
            // Expired tokens
            for (int i = 0; i < 15; i++) {
                RefreshToken expiredToken = createRefreshToken(testUser1, "expired" + i, true, false);
                expiredToken.setExpiryDate(now.minusDays(i + 1));
                refreshTokenRepository.save(expiredToken);
            }
            
            // Marked for cleanup
            for (int i = 0; i < 10; i++) {
                RefreshToken markedToken = createRefreshToken(testUser1, "marked" + i, false, false);
                markedToken.setMarkedForCleanup(true);
                refreshTokenRepository.save(markedToken);
            }

            // When - Cleanup operations
            long startTime = System.currentTimeMillis();
            
            List<RefreshToken> tokensForCleanup = refreshTokenRepository.findTokensEligibleForCleanup();
            
            long endTime = System.currentTimeMillis();

            // Then
            assertThat(tokensForCleanup).hasSize(25); // 15 expired + 10 marked
            assertThat(endTime - startTime).isLessThan(1000); // Should be efficient
        }
    }
}