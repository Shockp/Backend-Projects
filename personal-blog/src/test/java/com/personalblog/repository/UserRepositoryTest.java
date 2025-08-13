package com.personalblog.repository;

import com.personalblog.entity.Role;
import com.personalblog.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive test suite for UserRepository.
 * 
 * Tests all custom query methods, security operations, and business logic
 * to ensure proper functionality and adherence to security best practices.
 * 
 * Uses H2 in-memory database for fast, isolated testing.
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User adminUser;
    private User authorUser;
    private User disabledUser;
    private User lockedUser;
    private User unverifiedUser;

    @BeforeEach
    void setUp() {
        // Create test users with different states
        testUser = createTestUser("testuser", "test@example.com", Set.of(Role.USER), true, false, true);
        adminUser = createTestUser("admin", "admin@example.com", Set.of(Role.ADMIN), true, false, true);
        authorUser = createTestUser("author", "author@example.com", Set.of(Role.AUTHOR), true, false, true);
        disabledUser = createTestUser("disabled", "disabled@example.com", Set.of(Role.USER), false, false, true);
        lockedUser = createTestUser("locked", "locked@example.com", Set.of(Role.USER), true, true, true);
        unverifiedUser = createTestUser("unverified", "unverified@example.com", Set.of(Role.USER), true, false, false);

        // Persist test data
        entityManager.persistAndFlush(testUser);
        entityManager.persistAndFlush(adminUser);
        entityManager.persistAndFlush(authorUser);
        entityManager.persistAndFlush(disabledUser);
        entityManager.persistAndFlush(lockedUser);
        entityManager.persistAndFlush(unverifiedUser);
        entityManager.clear();
    }

    // ==================== Authentication & Authorization Tests ====================

    @Test
    @DisplayName("Should find active user by username")
    void testFindByUsernameAndActive() {
        // When
        Optional<User> result = userRepository.findByUsernameAndActive("testuser");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
        assertThat(result.get().isAccountEnabled()).isTrue();
        assertThat(result.get().isDeleted()).isFalse();
    }

    @Test
    @DisplayName("Should not find disabled user by username")
    void testFindByUsernameAndActive_DisabledUser() {
        // When
        Optional<User> result = userRepository.findByUsernameAndActive("disabled");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find active user by email")
    void testFindByEmailAndActive() {
        // When
        Optional<User> result = userRepository.findByEmailAndActive("test@example.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        assertThat(result.get().isAccountEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should find user by username including disabled")
    void testFindByUsername() {
        // When
        Optional<User> result = userRepository.findByUsername("disabled");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("disabled");
        assertThat(result.get().isAccountEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should find user by email including disabled")
    void testFindByEmail() {
        // When
        Optional<User> result = userRepository.findByEmail("disabled@example.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("disabled@example.com");
        assertThat(result.get().isAccountEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should check if username exists")
    void testExistsByUsername() {
        // When & Then
        assertThat(userRepository.existsByUsername("testuser")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }

    @Test
    @DisplayName("Should check if email exists")
    void testExistsByEmail() {
        // When & Then
        assertThat(userRepository.existsByEmail("test@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
    }

    // ==================== Role-Based Query Tests ====================

    @Test
    @DisplayName("Should find users by role")
    void testFindByRole() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> adminUsers = userRepository.findByRole(Role.ADMIN, pageable);
        Page<User> authorUsers = userRepository.findByRole(Role.AUTHOR, pageable);
        Page<User> regularUsers = userRepository.findByRole(Role.USER, pageable);

        // Then
        assertThat(adminUsers.getContent()).hasSize(1);
        assertThat(adminUsers.getContent().get(0).getUsername()).isEqualTo("admin");
        
        assertThat(authorUsers.getContent()).hasSize(1);
        assertThat(authorUsers.getContent().get(0).getUsername()).isEqualTo("author");
        
        // Should only return enabled users
        assertThat(regularUsers.getContent()).hasSize(2); // testuser and unverified (both enabled)
    }

    @Test
    @DisplayName("Should find users by multiple roles")
    void testFindByRoles() {
        // Given
        Set<Role> roles = Set.of(Role.ADMIN, Role.AUTHOR);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> result = userRepository.findByRoles(roles, pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
            .extracting(User::getUsername)
            .containsExactlyInAnyOrder("admin", "author");
    }

    @Test
    @DisplayName("Should find all administrators")
    void testFindAllAdmins() {
        // When
        List<User> admins = userRepository.findAllAdmins();

        // Then
        assertThat(admins).hasSize(1);
        assertThat(admins.get(0).getUsername()).isEqualTo("admin");
        assertThat(admins.get(0).getRoles()).contains(Role.ADMIN);
    }

    @Test
    @DisplayName("Should find all authors")
    void testFindAllAuthors() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> authors = userRepository.findAllAuthors(pageable);

        // Then
        assertThat(authors.getContent()).hasSize(1);
        assertThat(authors.getContent().get(0).getUsername()).isEqualTo("author");
        assertThat(authors.getContent().get(0).getRoles()).contains(Role.AUTHOR);
    }

    // ==================== Search and Filtering Tests ====================

    @Test
    @DisplayName("Should search users by name")
    void testSearchByName() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> result = userRepository.searchByName("test", pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should search users by email")
    void testSearchByEmail() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> result = userRepository.searchByName("admin@example", pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("admin@example.com");
    }

    @Test
    @DisplayName("Should search users with multiple criteria")
    void testSearchUsers() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When - search by role
        Page<User> adminResult = userRepository.searchUsers(null, Role.ADMIN, null, pageable);
        
        // When - search by email verification status
        Page<User> unverifiedResult = userRepository.searchUsers(null, null, false, pageable);
        
        // When - search by term and role
        Page<User> combinedResult = userRepository.searchUsers("author", Role.AUTHOR, null, pageable);

        // Then
        assertThat(adminResult.getContent()).hasSize(1);
        assertThat(adminResult.getContent().get(0).getRoles()).contains(Role.ADMIN);
        
        assertThat(unverifiedResult.getContent()).hasSize(1);
        assertThat(unverifiedResult.getContent().get(0).isEmailVerified()).isFalse();
        
        assertThat(combinedResult.getContent()).hasSize(1);
        assertThat(combinedResult.getContent().get(0).getUsername()).isEqualTo("author");
    }

    // ==================== Account Status Query Tests ====================

    @Test
    @DisplayName("Should find all enabled users")
    void testFindAllEnabled() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> result = userRepository.findAllEnabled(pageable);

        // Then
        assertThat(result.getContent()).hasSize(5); // All except disabled user
        assertThat(result.getContent())
            .extracting(User::isAccountEnabled)
            .containsOnly(true);
    }

    @Test
    @DisplayName("Should find all disabled users")
    void testFindAllDisabled() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> result = userRepository.findAllDisabled(pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("disabled");
        assertThat(result.getContent().get(0).isAccountEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should find all locked users")
    void testFindAllLocked() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> result = userRepository.findAllLocked(pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("locked");
        assertThat(result.getContent().get(0).isAccountLocked()).isTrue();
    }

    @Test
    @DisplayName("Should find all unverified users")
    void testFindAllUnverified() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> result = userRepository.findAllUnverified(pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("unverified");
        assertThat(result.getContent().get(0).isEmailVerified()).isFalse();
    }

    @Test
    @DisplayName("Should find users with expired verification tokens")
    void testFindUsersWithExpiredVerificationTokens() {
        // Given - create user with expired token
        User expiredTokenUser = createTestUser("expired", "expired@example.com", Set.of(Role.USER), true, false, false);
        expiredTokenUser.setEmailVerificationToken("expired-token");
        expiredTokenUser.setEmailVerificationTokenExpiresAt(LocalDateTime.now().minusHours(1));
        entityManager.persistAndFlush(expiredTokenUser);
        entityManager.clear();

        // When
        List<User> result = userRepository.findUsersWithExpiredVerificationTokens(LocalDateTime.now());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("expired");
    }

    // ==================== Security Operation Tests ====================

    @Test
    @DisplayName("Should find user by valid email verification token")
    void testFindByEmailVerificationToken() {
        // Given - create user with valid token
        User tokenUser = createTestUser("tokenuser", "token@example.com", Set.of(Role.USER), true, false, false);
        tokenUser.setEmailVerificationToken("valid-token");
        tokenUser.setEmailVerificationTokenExpiresAt(LocalDateTime.now().plusHours(1));
        entityManager.persistAndFlush(tokenUser);
        entityManager.clear();

        // When
        Optional<User> result = userRepository.findByEmailVerificationToken("valid-token");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("tokenuser");
    }

    @Test
    @DisplayName("Should not find user by expired email verification token")
    void testFindByEmailVerificationToken_Expired() {
        // Given - create user with expired token
        User expiredTokenUser = createTestUser("expired", "expired@example.com", Set.of(Role.USER), true, false, false);
        expiredTokenUser.setEmailVerificationToken("expired-token");
        expiredTokenUser.setEmailVerificationTokenExpiresAt(LocalDateTime.now().minusHours(1));
        entityManager.persistAndFlush(expiredTokenUser);
        entityManager.clear();

        // When
        Optional<User> result = userRepository.findByEmailVerificationToken("expired-token");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find user by valid password reset token")
    void testFindByPasswordResetToken() {
        // Given - create user with valid reset token
        User resetUser = createTestUser("resetuser", "reset@example.com", Set.of(Role.USER), true, false, true);
        resetUser.setPasswordResetToken("reset-token");
        resetUser.setPasswordResetTokenExpiresAt(LocalDateTime.now().plusHours(1));
        entityManager.persistAndFlush(resetUser);
        entityManager.clear();

        // When
        Optional<User> result = userRepository.findByPasswordResetToken("reset-token");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("resetuser");
    }

    @Test
    @DisplayName("Should find users with failed login attempts")
    void testFindUsersWithFailedAttempts() {
        // Given - create user with failed attempts
        User failedUser = createTestUser("failed", "failed@example.com", Set.of(Role.USER), true, false, true);
        failedUser.setFailedLoginAttempts(5);
        entityManager.persistAndFlush(failedUser);
        entityManager.clear();

        // When
        List<User> result = userRepository.findUsersWithFailedAttempts(3);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("failed");
        assertThat(result.get(0).getFailedLoginAttempts()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should find inactive users")
    void testFindInactiveUsers() {
        // Given - create user with old last login
        User inactiveUser = createTestUser("inactive", "inactive@example.com", Set.of(Role.USER), true, false, true);
        inactiveUser.setLastLoginAt(LocalDateTime.now().minusDays(60));
        entityManager.persistAndFlush(inactiveUser);
        entityManager.clear();

        // When
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        List<User> result = userRepository.findInactiveUsers(cutoffDate);

        // Then
        assertThat(result).hasSize(6); // 5 users with null lastLoginAt + 1 with old login
        assertThat(result).extracting(User::getUsername)
            .contains("inactive", "testuser", "admin", "author", "disabled", "locked");
    }

    // ==================== Statistics Tests ====================

    @Test
    @DisplayName("Should count active users")
    void testCountActiveUsers() {
        // When
        long count = userRepository.countActiveUsers();

        // Then
        assertThat(count).isEqualTo(5); // All except disabled user
    }

    @Test
    @DisplayName("Should count users by role")
    void testCountUsersByRole() {
        // When
        long adminCount = userRepository.countUsersByRole(Role.ADMIN);
        long authorCount = userRepository.countUsersByRole(Role.AUTHOR);
        long userCount = userRepository.countUsersByRole(Role.USER);

        // Then
        assertThat(adminCount).isEqualTo(1);
        assertThat(authorCount).isEqualTo(1);
        assertThat(userCount).isEqualTo(3); // testuser, locked, unverified (all enabled)
    }

    @Test
    @DisplayName("Should count users registered between dates")
    void testCountUsersRegisteredBetween() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // When
        long count = userRepository.countUsersRegisteredBetween(startDate, endDate);

        // Then
        assertThat(count).isEqualTo(6); // All test users created today
    }

    @Test
    @DisplayName("Should count verified users")
    void testCountVerifiedUsers() {
        // When
        long count = userRepository.countVerifiedUsers();

        // Then
        assertThat(count).isEqualTo(5); // All except unverified user
    }

    @Test
    @DisplayName("Should find recently registered users")
    void testFindRecentlyRegistered() {
        // Given
        LocalDateTime cutoffDate = LocalDateTime.now().minusHours(1);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> result = userRepository.findRecentlyRegistered(cutoffDate, pageable);

        // Then
        assertThat(result.getContent()).hasSize(6); // All test users
        // Should be ordered by creation date descending
        assertThat(result.getContent().get(0).getCreatedAt())
            .isAfterOrEqualTo(result.getContent().get(result.getContent().size() - 1).getCreatedAt());
    }

    // ==================== Helper Methods ====================

    private User createTestUser(String username, String email, Set<Role> roles, 
                               boolean enabled, boolean locked, boolean emailVerified) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("encoded-password");
        user.setRoles(roles);
        user.setAccountEnabled(enabled);
        user.setAccountLocked(locked);
        user.setEmailVerified(emailVerified);
        user.setFailedLoginAttempts(0);
        return user;
    }
}