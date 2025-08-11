package com.personalblog.entity;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.security.core.GrantedAuthority;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for the User entity.
 * 
 * Tests cover:
 * - Entity construction and initialization
 * - UserDetails interface implementation
 * - Role management and authorization
 * - Account status management
 * - Email verification workflow
 * - Login tracking functionality
 * - Password reset functionality
 * - Bean validation constraints
 * - Utility methods and business logic
 * - Edge cases and error conditions
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 */
@DisplayName("User Entity Tests")
class UserTest {

    private Validator validator;
    private User user;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        // Create a valid user for testing
        user = new User("testuser", "test@example.com", "password123");
    }

    @AfterEach
    void tearDown() {
        user = null;
    }

    // ==================== Constructor Tests ====================

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create user with default constructor")
        void shouldCreateUserWithDefaultConstructor() {
            // When
            User defaultUser = new User();

            // Then
            assertThat(defaultUser)
                .isNotNull()
                .satisfies(u -> {
                    assertThat(u.getRoles()).containsExactly(Role.USER);
                    assertThat(u.isAccountEnabled()).isTrue();
                    assertThat(u.isAccountLocked()).isFalse();
                    assertThat(u.isAccountExpired()).isFalse();
                    assertThat(u.isCredentialsExpired()).isFalse();
                    assertThat(u.isEmailVerified()).isFalse();
                    assertThat(u.getFailedLoginAttempts()).isZero();
                });
        }

        @Test
        @DisplayName("Should create user with parameterized constructor")
        void shouldCreateUserWithParameterizedConstructor() {
            // Given
            String username = "newuser";
            String email = "newuser@example.com";
            String password = "newpassword123";

            // When
            User newUser = new User(username, email, password);

            // Then
            assertThat(newUser)
                .isNotNull()
                .satisfies(u -> {
                    assertThat(u.getUsername()).isEqualTo(username);
                    assertThat(u.getEmail()).isEqualTo(email);
                    assertThat(u.getPassword()).isEqualTo(password);
                    assertThat(u.getRoles()).containsExactly(Role.USER);
                    assertThat(u.isAccountEnabled()).isTrue();
                });
        }
    }

    // ==================== UserDetails Implementation Tests ====================

    @Nested
    @DisplayName("UserDetails Implementation Tests")
    class UserDetailsTests {

        @Test
        @DisplayName("Should return correct authorities for single role")
        void shouldReturnCorrectAuthoritiesForSingleRole() {
            // Given
            user.setRoles(Set.of(Role.USER));

            // When
            Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

            // Then
            assertThat(authorities)
                .hasSize(1)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
        }

        @Test
        @DisplayName("Should return correct authorities for multiple roles")
        void shouldReturnCorrectAuthoritiesForMultipleRoles() {
            // Given
            user.setRoles(Set.of(Role.USER, Role.AUTHOR, Role.ADMIN));

            // When
            Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

            // Then
            assertThat(authorities)
                .hasSize(3)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_AUTHOR", "ROLE_ADMIN");
        }

        @Test
        @DisplayName("Should return empty authorities for no roles")
        void shouldReturnEmptyAuthoritiesForNoRoles() {
            // Given
            user.setRoles(new HashSet<>());

            // When
            Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

            // Then
            assertThat(authorities).isEmpty();
        }

        @ParameterizedTest
        @CsvSource({
            "true, false, false, false, true",
            "false, false, false, false, false",
            "true, true, false, false, true",
            "true, false, true, false, true",
            "true, false, false, true, true"
        })
        @DisplayName("Should correctly determine account status")
        void shouldCorrectlyDetermineAccountStatus(
                boolean enabled, boolean locked, boolean expired, 
                boolean credentialsExpired, boolean expectedEnabled) {
            // Given
            user.setAccountEnabled(enabled);
            user.setAccountLocked(locked);
            user.setAccountExpired(expired);
            user.setCredentialsExpired(credentialsExpired);

            // When & Then
            assertThat(user.isEnabled()).isEqualTo(expectedEnabled);
            assertThat(user.isAccountNonLocked()).isEqualTo(!locked);
            assertThat(user.isAccountNonExpired()).isEqualTo(!expired);
            assertThat(user.isCredentialsNonExpired()).isEqualTo(!credentialsExpired);
        }

        @Test
        @DisplayName("Should handle default account status fields")
        void shouldHandleDefaultAccountStatusFields() {
            // Given
            User newUser = new User();
            // Boolean fields have default values: accountEnabled=true, others=false

            // When & Then - check default values
            assertThat(newUser.isEnabled()).isTrue();
            assertThat(newUser.isAccountNonLocked()).isTrue();
            assertThat(newUser.isAccountNonExpired()).isTrue();
            assertThat(newUser.isCredentialsNonExpired()).isTrue();
        }
    }

    // ==================== Role Management Tests ====================

    @Nested
    @DisplayName("Role Management Tests")
    class RoleManagementTests {

        @Test
        @DisplayName("Should add role successfully")
        void shouldAddRoleSuccessfully() {
            // Given
            user.setRoles(new HashSet<>(Set.of(Role.USER)));

            // When
            user.addRole(Role.AUTHOR);

            // Then
            assertThat(user.getRoles())
                .hasSize(2)
                .containsExactlyInAnyOrderElementsOf(Set.of(Role.USER, Role.AUTHOR));
        }

        @Test
        @DisplayName("Should not add null role")
        void shouldNotAddNullRole() {
            // Given
            Set<Role> originalRoles = new HashSet<>(user.getRoles());

            // When
            user.addRole(null);

            // Then
            assertThat(user.getRoles()).isEqualTo(originalRoles);
        }

        @Test
        @DisplayName("Should remove role successfully")
        void shouldRemoveRoleSuccessfully() {
            // Given
            user.setRoles(new HashSet<>(Set.of(Role.USER, Role.AUTHOR)));

            // When
            user.removeRole(Role.USER);

            // Then
            assertThat(user.getRoles())
                .hasSize(1)
                .containsExactly(Role.AUTHOR);
        }

        @Test
        @DisplayName("Should handle removing non-existent role")
        void shouldHandleRemovingNonExistentRole() {
            // Given
            user.setRoles(new HashSet<>(Set.of(Role.USER)));

            // When
            user.removeRole(Role.ADMIN);

            // Then
            assertThat(user.getRoles())
                .hasSize(1)
                .containsExactly(Role.USER);
        }

        @ParameterizedTest
        @CsvSource({
            "USER, true",
            "AUTHOR, false",
            "ADMIN, false"
        })
        @DisplayName("Should correctly check if user has role")
        void shouldCorrectlyCheckIfUserHasRole(Role roleToCheck, boolean expected) {
            // Given
            user.setRoles(Set.of(Role.USER));

            // When & Then
            assertThat(user.hasRole(roleToCheck)).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should correctly identify admin user")
        void shouldCorrectlyIdentifyAdminUser() {
            // Given
            user.setRoles(Set.of(Role.ADMIN));

            // When & Then
            assertThat(user.isAdmin()).isTrue();
            assertThat(user.canWriteBlogPosts()).isTrue();
        }

        @Test
        @DisplayName("Should correctly identify author user")
        void shouldCorrectlyIdentifyAuthorUser() {
            // Given
            user.setRoles(Set.of(Role.AUTHOR));

            // When & Then
            assertThat(user.isAuthor()).isTrue();
            assertThat(user.isAdmin()).isFalse();
            assertThat(user.canWriteBlogPosts()).isTrue();
        }

        @Test
        @DisplayName("Should correctly identify regular user")
        void shouldCorrectlyIdentifyRegularUser() {
            // Given
            user.setRoles(Set.of(Role.USER));

            // When & Then
            assertThat(user.isAuthor()).isFalse();
            assertThat(user.isAdmin()).isFalse();
            assertThat(user.canWriteBlogPosts()).isFalse();
        }
    }

    // ==================== Account Management Tests ====================

    @Nested
    @DisplayName("Account Management Tests")
    class AccountManagementTests {

        @Test
        @DisplayName("Should lock account successfully")
        void shouldLockAccountSuccessfully() {
            // When
            user.lockAccount();

            // Then
            assertThat(user.isAccountLocked()).isTrue();
            assertThat(user.isAccountNonLocked()).isFalse();
        }

        @Test
        @DisplayName("Should unlock account successfully")
        void shouldUnlockAccountSuccessfully() {
            // Given
            user.setAccountLocked(true);

            // When
            user.unlockAccount();

            // Then
            assertThat(user.isAccountLocked()).isFalse();
            assertThat(user.isAccountNonLocked()).isTrue();
        }

        @Test
        @DisplayName("Should increment failed login attempts")
        void shouldIncrementFailedLoginAttempts() {
            // Given
            User user = new User();
            assertThat(user.getFailedLoginAttempts()).isZero();

            // When
            user.incrementFailedLoginAttempts();

            // Then
            assertThat(user.getFailedLoginAttempts()).isEqualTo(1);

            // When - increment again
            user.incrementFailedLoginAttempts();

            // Then
            assertThat(user.getFailedLoginAttempts()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should handle null failed login attempts")
        void shouldHandleNullFailedLoginAttempts() {
            // Given
            user.setFailedLoginAttempts(null);

            // When
            user.incrementFailedLoginAttempts();

            // Then
            assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should reset failed login attempts")
        void shouldResetFailedLoginAttempts() {
            // Given
            user.setFailedLoginAttempts(5);

            // When
            user.resetFailedLoginAttempts();

            // Then
            assertThat(user.getFailedLoginAttempts()).isZero();
        }

        @Test
        @DisplayName("Should update last login information")
        void shouldUpdateLastLoginInformation() {
            // Given
            String ipAddress = "192.168.1.100";
            user.setFailedLoginAttempts(3);
            LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);

            // When
            user.updateLastLogin(ipAddress);

            // Then
            assertThat(user.getLastLoginIp()).isEqualTo(ipAddress);
            assertThat(user.getLastLoginAt())
                .isNotNull()
                .isAfter(beforeUpdate);
            assertThat(user.getFailedLoginAttempts()).isZero();
        }
    }

    // ==================== Email Verification Tests ====================

    @Nested
    @DisplayName("Email Verification Tests")
    class EmailVerificationTests {

        @Test
        @DisplayName("Should verify email successfully")
        void shouldVerifyEmailSuccessfully() {
            // Given
            user.setEmailVerificationToken("token123");
            user.setEmailVerificationTokenExpiresAt(LocalDateTime.now().plusHours(1));
            LocalDateTime beforeVerification = LocalDateTime.now().minusSeconds(1);

            // When
            user.verifyEmail();

            // Then
            assertThat(user.isEmailVerified()).isTrue();
            assertThat(user.getEmailVerificationToken()).isNull();
            assertThat(user.getEmailVerificationTokenExpiresAt()).isNull();
            assertThat(user.getEmailVerifiedAt())
                .isNotNull()
                .isAfter(beforeVerification);
        }

        @Test
        @DisplayName("Should handle email verification when already verified")
        void shouldHandleEmailVerificationWhenAlreadyVerified() {
            // Given
            user.setEmailVerified(true);
            LocalDateTime originalVerifiedAt = LocalDateTime.now().minusDays(1);
            user.setEmailVerifiedAt(originalVerifiedAt);

            // When
            user.verifyEmail();

            // Then
            assertThat(user.isEmailVerified()).isTrue();
            assertThat(user.getEmailVerifiedAt()).isAfter(originalVerifiedAt);
        }
    }

    // ==================== Validation Tests ====================

    @Nested
    @DisplayName("Bean Validation Tests")
    class ValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t", "\n"})
        @DisplayName("Should reject invalid usernames")
        void shouldRejectInvalidUsernames(String invalidUsername) {
            // Given
            user.setUsername(invalidUsername);

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations)
                .isNotEmpty()
                .extracting(ConstraintViolation::getMessage)
                .anyMatch(message -> message.contains("Username"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"ab", "a", "thisusernameistoolongtobevalid"})
        @DisplayName("Should reject usernames with invalid length")
        void shouldRejectUsernamesWithInvalidLength(String invalidUsername) {
            // Given
            user.setUsername(invalidUsername);

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations)
                .isNotEmpty()
                .extracting(ConstraintViolation::getMessage)
                .anyMatch(message -> message.contains("between 3 and 20 characters"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"user@name", "user name", "user-name", "user.name"})
        @DisplayName("Should reject usernames with invalid characters")
        void shouldRejectUsernamesWithInvalidCharacters(String invalidUsername) {
            // Given
            user.setUsername(invalidUsername);

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations)
                .isNotEmpty()
                .extracting(ConstraintViolation::getMessage)
                .anyMatch(message -> message.contains("letters and numbers"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"user123", "testUser", "USER", "user1", "a1b2c3"})
        @DisplayName("Should accept valid usernames")
        void shouldAcceptValidUsernames(String validUsername) {
            // Given
            user.setUsername(validUsername);

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations)
                .filteredOn(v -> v.getPropertyPath().toString().equals("username"))
                .isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"invalid-email", "@example.com", "user@", "user.example.com"})
        @DisplayName("Should reject invalid emails")
        void shouldRejectInvalidEmails(String invalidEmail) {
            // Given
            user.setEmail(invalidEmail);

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations)
                .isNotEmpty()
                .extracting(ConstraintViolation::getMessage)
                .anyMatch(message -> message.contains("email") || message.contains("Email"));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "user@example.com",
            "test.email@domain.co.uk",
            "user+tag@example.org",
            "firstname.lastname@company.com"
        })
        @DisplayName("Should accept valid emails")
        void shouldAcceptValidEmails(String validEmail) {
            // Given
            user.setEmail(validEmail);

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations)
                .filteredOn(v -> v.getPropertyPath().toString().equals("email"))
                .isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"short", "1234567"})
        @DisplayName("Should reject invalid passwords")
        void shouldRejectInvalidPasswords(String invalidPassword) {
            // Given
            user.setPassword(invalidPassword);

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations)
                .isNotEmpty()
                .extracting(ConstraintViolation::getMessage)
                .anyMatch(message -> message.contains("Password") || message.contains("password"));
        }

        @Test
        @DisplayName("Should reject bio exceeding maximum length")
        void shouldRejectBioExceedingMaximumLength() {
            // Given
            String longBio = "a".repeat(1001);
            user.setBio(longBio);

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations)
                .isNotEmpty()
                .extracting(ConstraintViolation::getMessage)
                .anyMatch(message -> message.contains("Biography") && message.contains("1000"));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "invalid-url",
            "http://",
            "https://",
            "www.",
            "example"
        })
        @DisplayName("Should reject invalid website URLs")
        void shouldRejectInvalidWebsiteUrls(String invalidUrl) {
            // Given
            user.setWebsiteUrl(invalidUrl);

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations)
                .isNotEmpty()
                .extracting(ConstraintViolation::getMessage)
                .anyMatch(message -> message.contains("website URL"));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "https://example.com",
            "http://www.example.com",
            "www.example.com",
            "example.com",
            "https://subdomain.example.co.uk/path?query=value#fragment",
            "" // Empty string should be valid
        })
        @DisplayName("Should accept valid website URLs")
        void shouldAcceptValidWebsiteUrls(String validUrl) {
            // Given
            user.setWebsiteUrl(validUrl);

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations)
                .filteredOn(v -> v.getPropertyPath().toString().equals("websiteUrl"))
                .isEmpty();
        }

        @Test
        @DisplayName("Should reject negative failed login attempts")
        void shouldRejectNegativeFailedLoginAttempts() {
            // Given
            user.setFailedLoginAttempts(-1);

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations)
                .isNotEmpty()
                .extracting(ConstraintViolation::getMessage)
                .anyMatch(message -> message.contains("non-negative"));
        }
    }

    // ==================== Social Media URL Validation Tests ====================

    @Nested
    @DisplayName("Social Media URL Validation Tests")
    class SocialMediaUrlValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "https://twitter.com/username",
            "http://www.twitter.com/user123",
            "https://x.com/user_name",
            "www.x.com/username"
        })
        @DisplayName("Should accept valid Twitter/X URLs")
        void shouldAcceptValidTwitterUrls(String validUrl) {
            // Given
            user.setTwitterUrl(validUrl);

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations)
                .filteredOn(v -> v.getPropertyPath().toString().equals("twitterUrl"))
                .isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "https://linkedin.com/in/username",
            "http://www.linkedin.com/in/user-name",
            "www.linkedin.com/in/user_name",
            "linkedin.com/in/username123"
        })
        @DisplayName("Should accept valid LinkedIn URLs")
        void shouldAcceptValidLinkedInUrls(String validUrl) {
            // Given
            user.setLinkedinUrl(validUrl);

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations)
                .filteredOn(v -> v.getPropertyPath().toString().equals("linkedinUrl"))
                .isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "https://github.com/username",
            "http://www.github.com/user-name",
            "www.github.com/user123",
            "github.com/username"
        })
        @DisplayName("Should accept valid GitHub URLs")
        void shouldAcceptValidGitHubUrls(String validUrl) {
            // Given
            user.setGithubUrl(validUrl);

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations)
                .filteredOn(v -> v.getPropertyPath().toString().equals("githubUrl"))
                .isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "https://instagram.com/username",
            "http://www.instagram.com/user.name",
            "www.instagram.com/user_123",
            "instagram.com/username"
        })
        @DisplayName("Should accept valid Instagram URLs")
        void shouldAcceptValidInstagramUrls(String validUrl) {
            // Given
            user.setInstagramUrl(validUrl);

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations)
                .filteredOn(v -> v.getPropertyPath().toString().equals("instagramUrl"))
                .isEmpty();
        }
    }

    // ==================== Edge Cases and Error Conditions ====================

    @Nested
    @DisplayName("Edge Cases and Error Conditions")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null roles collection")
        void shouldHandleNullRolesCollection() {
            // Given
            user.setRoles(null);

            // When & Then
            assertThatThrownBy(() -> user.getAuthorities())
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should handle empty roles collection")
        void shouldHandleEmptyRolesCollection() {
            // Given
            user.setRoles(new HashSet<>());

            // When
            Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

            // Then
            assertThat(authorities).isEmpty();
        }

        @Test
        @DisplayName("Should handle null role in hasRole method")
        void shouldHandleNullRoleInHasRoleMethod() {
            // When & Then
            assertThat(user.hasRole(null)).isFalse();
        }

        @Test
        @DisplayName("Should handle null IP address in updateLastLogin")
        void shouldHandleNullIpAddressInUpdateLastLogin() {
            // When
            user.updateLastLogin(null);

            // Then
            assertThat(user.getLastLoginIp()).isNull();
            assertThat(user.getLastLoginAt()).isNotNull();
        }

        @Test
        @DisplayName("Should handle mutable roles collection")
        void shouldHandleMutableRolesCollection() {
            // Given
            Set<Role> originalRoles = Set.of(Role.USER, Role.AUTHOR);
            user.setRoles(new HashSet<>(originalRoles));

            // When
            Set<Role> retrievedRoles = user.getRoles();
            retrievedRoles.add(Role.ADMIN);

            // Then - getRoles() returns mutable collection, so changes are reflected
            assertThat(user.getRoles()).contains(Role.ADMIN);
            assertThat(user.getRoles()).hasSize(3);
        }
    }

    // ==================== Integration and Business Logic Tests ====================

    @Nested
    @DisplayName("Integration and Business Logic Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should correctly implement complete user workflow")
        void shouldCorrectlyImplementCompleteUserWorkflow() {
            // Given - Create new user
            User newUser = new User("workflowuser", "workflow@example.com", "password123");

            // When & Then - Initial state
            assertThat(newUser)
                .satisfies(u -> {
                    assertThat(u.isEnabled()).isTrue();
                    assertThat(u.isAccountNonLocked()).isTrue();
                    assertThat(u.isEmailVerified()).isFalse();
                    assertThat(u.getRoles()).containsExactly(Role.USER);
                });

            // When - Add author role
            newUser.addRole(Role.AUTHOR);

            // Then - Should be able to write blog posts
            assertThat(newUser.canWriteBlogPosts()).isTrue();
            assertThat(newUser.isAuthor()).isTrue();

            // When - Simulate failed login attempts
            newUser.incrementFailedLoginAttempts();
            newUser.incrementFailedLoginAttempts();
            newUser.incrementFailedLoginAttempts();

            // Then - Should have 3 failed attempts
            assertThat(newUser.getFailedLoginAttempts()).isEqualTo(3);

            // When - Successful login
            newUser.updateLastLogin("192.168.1.1");

            // Then - Should reset failed attempts and update login info
            assertThat(newUser.getFailedLoginAttempts()).isZero();
            assertThat(newUser.getLastLoginIp()).isEqualTo("192.168.1.1");
            assertThat(newUser.getLastLoginAt()).isNotNull();

            // When - Verify email
            newUser.verifyEmail();

            // Then - Should be verified
            assertThat(newUser.isEmailVerified()).isTrue();
            assertThat(newUser.getEmailVerifiedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should correctly handle admin privileges escalation")
        void shouldCorrectlyHandleAdminPrivilegesEscalation() {
            // Given - Regular user
            User regularUser = new User("regular", "regular@example.com", "password123");

            // When & Then - Initially cannot write blog posts
            assertThat(regularUser.canWriteBlogPosts()).isFalse();
            assertThat(regularUser.isAdmin()).isFalse();
            assertThat(regularUser.isAuthor()).isFalse();

            // When - Promote to author
            regularUser.addRole(Role.AUTHOR);

            // Then - Can write blog posts but not admin
            assertThat(regularUser.canWriteBlogPosts()).isTrue();
            assertThat(regularUser.isAuthor()).isTrue();
            assertThat(regularUser.isAdmin()).isFalse();

            // When - Promote to admin
            regularUser.addRole(Role.ADMIN);

            // Then - Has all privileges
            assertThat(regularUser.canWriteBlogPosts()).isTrue();
            assertThat(regularUser.isAuthor()).isTrue();
            assertThat(regularUser.isAdmin()).isTrue();
            assertThat(regularUser.getAuthorities()).hasSize(3);
        }

        @Test
        @DisplayName("Should correctly handle account security states")
        void shouldCorrectlyHandleAccountSecurityStates() {
            // Given - Active user
            User secureUser = new User("secure", "secure@example.com", "password123");

            // When & Then - Initially secure and active
            assertThat(secureUser.isEnabled()).isTrue();
            assertThat(secureUser.isAccountNonLocked()).isTrue();
            assertThat(secureUser.isAccountNonExpired()).isTrue();
            assertThat(secureUser.isCredentialsNonExpired()).isTrue();

            // When - Lock account due to security breach
            secureUser.lockAccount();

            // Then - Should be locked but still enabled
            assertThat(secureUser.isEnabled()).isTrue();
            assertThat(secureUser.isAccountNonLocked()).isFalse();

            // When - Unlock account
            secureUser.unlockAccount();

            // Then - Should be fully accessible again
            assertThat(secureUser.isEnabled()).isTrue();
            assertThat(secureUser.isAccountNonLocked()).isTrue();

            // When - Disable account
            secureUser.setAccountEnabled(false);

            // Then - Should not be enabled
            assertThat(secureUser.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("Should handle password reset workflow")
        void shouldHandlePasswordResetWorkflow() {
            // Given
            User user = new User("resetuser", "reset@example.com", "OldPass123!", Set.of(Role.USER));
            String resetToken = "reset-token-123";
            LocalDateTime resetExpiry = LocalDateTime.now().plusHours(1);

            // When - Initiate password reset
            user.setPasswordResetToken(resetToken);
            user.setPasswordResetTokenExpiresAt(resetExpiry);

            // Then - Reset token should be set
            assertThat(user.getPasswordResetToken()).isEqualTo(resetToken);
            assertThat(user.getPasswordResetTokenExpiresAt()).isEqualTo(resetExpiry);

            // When - Complete password reset
            user.setPassword("NewPass123!");
            user.setPasswordResetToken(null);
            user.setPasswordResetTokenExpiresAt(null);

            // Then - Password should be updated and reset token cleared
            assertThat(user.getPassword()).isEqualTo("NewPass123!");
            assertThat(user.getPasswordResetToken()).isNull();
            assertThat(user.getPasswordResetTokenExpiresAt()).isNull();
        }

        @Test
        @DisplayName("Should handle user profile completion workflow")
        void shouldHandleUserProfileCompletionWorkflow() {
            // Given
            User user = new User("profileuser", "profile@example.com", "SecurePass123!", Set.of(Role.AUTHOR));

            // When - Complete profile information
            user.setBio("Experienced software developer and technical writer");
            user.setWebsiteUrl("https://johndoe.dev");
            user.setTwitterUrl("https://twitter.com/johndoe");
            user.setLinkedinUrl("https://linkedin.com/in/johndoe");
            user.setGithubUrl("https://github.com/johndoe");

            // Then - Profile should be complete
            assertThat(user)
                .satisfies(u -> {
                    assertThat(u.getBio()).isNotBlank();
                    assertThat(u.getWebsiteUrl()).startsWith("https://");
                    assertThat(u.getTwitterUrl()).contains("twitter.com");
                    assertThat(u.getLinkedinUrl()).contains("linkedin.com");
                    assertThat(u.getGithubUrl()).contains("github.com");
                });
        }
    }

    // ==================== ToString Method Tests ====================

    @Nested
    @DisplayName("ToString Method Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should generate correct string representation")
        void shouldGenerateCorrectStringRepresentation() {
            // Given
            user.setRoles(Set.of(Role.USER, Role.AUTHOR));
            user.setEmailVerified(true);

            // When
            String userString = user.toString();

            // Then
            assertThat(userString)
                .contains("User{")
                .contains("username='testuser'")
                .contains("email='test@example.com'")
                .contains("enabled=true")
                .contains("emailVerified=true")
                .doesNotContain("password"); // Should not expose password
        }

        @Test
        @DisplayName("Should not expose sensitive information in toString")
        void shouldNotExposeSensitiveInformationInToString() {
            // Given
            user.setPassword("supersecretpassword");
            user.setEmailVerificationToken("secret-token");
            user.setPasswordResetToken("reset-token");

            // When
            String userString = user.toString();

            // Then
            assertThat(userString)
                .doesNotContain("supersecretpassword")
                .doesNotContain("secret-token")
                .doesNotContain("reset-token");
        }
    }

    // ==================== Object Methods Tests ====================

    @Nested
    @DisplayName("Object Methods Tests")
    class ObjectMethodsTests {

        @Nested
        @DisplayName("Equals Tests")
        class EqualsTests {

            @Test
            @DisplayName("Should return true for users with same id")
            void shouldReturnTrueForUsersWithSameId() throws Exception {
                // Given
                User user1 = new User("user1", "user1@example.com", "password123", Set.of(Role.USER));
                User user2 = new User("user2", "user2@example.com", "password456", Set.of(Role.ADMIN));
                
                // Set same ID using reflection
                java.lang.reflect.Field idField = BaseEntity.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(user1, 1L);
                idField.set(user2, 1L);

                // When & Then
                assertThat(user1).isEqualTo(user2);
                assertThat(user2).isEqualTo(user1);
            }

            @Test
            @DisplayName("Should return false for users with different ids")
            void shouldReturnFalseForUsersWithDifferentIds() throws Exception {
                // Given
                User user1 = new User("user1", "user1@example.com", "password123", Set.of(Role.USER));
                User user2 = new User("user1", "user1@example.com", "password123", Set.of(Role.USER));
                
                // Set different IDs using reflection
                java.lang.reflect.Field idField = BaseEntity.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(user1, 1L);
                idField.set(user2, 2L);

                // When & Then
                assertThat(user1).isNotEqualTo(user2);
            }

            @Test
            @DisplayName("Should return false when comparing with null")
            void shouldReturnFalseWhenComparingWithNull() {
                // Given
                User user = new User("testuser", "test@example.com", "password123", Set.of(Role.USER));

                // When & Then
                assertThat(user).isNotEqualTo(null);
            }

            @Test
            @DisplayName("Should return false when comparing with different class")
            void shouldReturnFalseWhenComparingWithDifferentClass() {
                // Given
                User user = new User("testuser", "test@example.com", "password123", Set.of(Role.USER));
                String notAUser = "not a user";

                // When & Then
                assertThat(user).isNotEqualTo(notAUser);
            }

            @Test
            @DisplayName("Should return false when id is null")
            void shouldReturnFalseWhenIdIsNull() {
                // Given
                User user1 = new User("user1", "user1@example.com", "password123", Set.of(Role.USER));
                User user2 = new User("user1", "user1@example.com", "password123", Set.of(Role.USER));
                // IDs are null by default

                // When & Then
                assertThat(user1).isNotEqualTo(user2);
            }
        }

        @Nested
        @DisplayName("HashCode Tests")
        class HashCodeTests {

            @Test
            @DisplayName("Should return same hash code for equal objects")
            void shouldReturnSameHashCodeForEqualObjects() throws Exception {
                // Given
                User user1 = new User("user1", "user1@example.com", "password123", Set.of(Role.USER));
                User user2 = new User("user2", "user2@example.com", "password456", Set.of(Role.ADMIN));
                
                // Set same ID using reflection
                java.lang.reflect.Field idField = BaseEntity.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(user1, 1L);
                idField.set(user2, 1L);

                // When & Then
                assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
            }

            @Test
            @DisplayName("Should handle null id in hashCode")
            void shouldHandleNullIdInHashCode() {
                // Given
                User user = new User("testuser", "test@example.com", "password123", Set.of(Role.USER));
                // ID is null by default

                // When & Then
                assertThat(user.hashCode()).isEqualTo(31);
            }

            @Test
            @DisplayName("Should return different hash codes for different ids")
            void shouldReturnDifferentHashCodesForDifferentIds() throws Exception {
                // Given
                User user1 = new User("user1", "user1@example.com", "password123", Set.of(Role.USER));
                User user2 = new User("user2", "user2@example.com", "password456", Set.of(Role.ADMIN));
                
                // Set different IDs using reflection
                java.lang.reflect.Field idField = BaseEntity.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(user1, 1L);
                idField.set(user2, 2L);

                // When & Then
                assertThat(user1.hashCode()).isNotEqualTo(user2.hashCode());
            }
        }
    }

    // ==================== Performance and Edge Cases Tests ====================

    @Nested
    @DisplayName("Performance and Edge Cases Tests")
    class PerformanceAndEdgeCasesTests {

        @Test
        @DisplayName("Should handle concurrent role modifications")
        void shouldHandleConcurrentRoleModifications() {
            // Given
            User user = new User("concurrentuser", "concurrent@example.com", "password123", new HashSet<>());
            Set<Role> rolesToAdd = Set.of(Role.USER, Role.AUTHOR, Role.ADMIN);

            // When - Simulate concurrent role additions
            rolesToAdd.forEach(user::addRole);

            // Then
            assertThat(user.getRoles())
                .hasSize(3)
                .containsExactlyInAnyOrderElementsOf(rolesToAdd);
        }

        @Test
        @DisplayName("Should maintain role collection integrity")
        void shouldMaintainRoleCollectionIntegrity() {
            // Given
            User user = new User("integrityuser", "integrity@example.com", "password123", Set.of(Role.USER));

            // When - Add and remove roles multiple times
            user.addRole(Role.AUTHOR);
            user.addRole(Role.ADMIN);
            user.removeRole(Role.USER);
            user.addRole(Role.USER);
            user.removeRole(Role.ADMIN);

            // Then
            assertThat(user.getRoles())
                .hasSize(2)
                .containsExactlyInAnyOrder(Role.USER, Role.AUTHOR);
        }
    }
}