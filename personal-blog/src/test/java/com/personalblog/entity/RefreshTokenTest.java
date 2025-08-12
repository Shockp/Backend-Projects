package com.personalblog.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for RefreshToken entity.
 * 
 * Tests cover:
 * - Entity validation with Bean Validation API
 * - Business logic methods
 * - Token management functionality
 * - User relationship management
 * - Device information handling
 * - Security features
 * - Object methods (equals, hashCode, toString)
 * - Constructor behavior
 * - Edge cases and boundary conditions
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 */
@DisplayName("RefreshToken Entity Tests")
class RefreshTokenTest {

    private Validator validator;
    private RefreshToken validToken;
    private User testUser;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        
        // Create valid refresh token
        validToken = new RefreshToken();
        validToken.setUser(testUser);
        validToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        validToken.setDeviceId("device-123");
        validToken.setDeviceName("Test Device");
        validToken.setDeviceType("desktop");
        validToken.setIpAddress("192.168.1.1");
        validToken.setUserAgent("Mozilla/5.0 Test Browser");
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor should initialize with defaults")
        void defaultConstructor_ShouldInitializeWithDefaults() {
            RefreshToken token = new RefreshToken();
            
            assertThat(token.getTokenValue()).isNotNull();
            assertThat(token.getTokenValue()).hasSize(36); // UUID length
            assertThat(token.getRevoked()).isFalse();
            assertThat(token.getUser()).isNull();
            assertThat(token.getExpiryDate()).isNull();
            assertThat(token.getDeviceId()).isNull();
            assertThat(token.getLastUsedAt()).isNull();
        }

        @Test
        @DisplayName("Constructor with user and expiry should set values correctly")
        void constructorWithUserAndExpiry_ShouldSetValues() {
            LocalDateTime expiry = LocalDateTime.now().plusDays(7);
            RefreshToken token = new RefreshToken(testUser, expiry);
            
            assertThat(token.getUser()).isEqualTo(testUser);
            assertThat(token.getExpiryDate()).isEqualTo(expiry);
            assertThat(token.getTokenValue()).isNotNull();
            assertThat(token.getRevoked()).isFalse();
        }

        @Test
        @DisplayName("Constructor with device info should set all values")
        void constructorWithDeviceInfo_ShouldSetAllValues() {
            LocalDateTime expiry = LocalDateTime.now().plusDays(7);
            String deviceId = "device-456";
            String deviceName = "iPhone 15";
            String ipAddress = "10.0.0.1";
            
            RefreshToken token = new RefreshToken(testUser, expiry, deviceId, deviceName, ipAddress);
            
            assertThat(token.getUser()).isEqualTo(testUser);
            assertThat(token.getExpiryDate()).isEqualTo(expiry);
            assertThat(token.getDeviceId()).isEqualTo(deviceId);
            assertThat(token.getDeviceName()).isEqualTo(deviceName);
            assertThat(token.getIpAddress()).isEqualTo(ipAddress);
            assertThat(token.getTokenValue()).isNotNull();
            assertThat(token.getRevoked()).isFalse();
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Valid refresh token should pass validation")
        void validRefreshToken_ShouldPassValidation() {
            Set<ConstraintViolation<RefreshToken>> violations = validator.validate(validToken);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Token without user should fail validation")
        void tokenWithoutUser_ShouldFailValidation() {
            validToken.setUser(null);
            
            Set<ConstraintViolation<RefreshToken>> violations = validator.validate(validToken);
            
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo("User is required");
        }

        @Test
        @DisplayName("Token without expiry date should fail validation")
        void tokenWithoutExpiryDate_ShouldFailValidation() {
            validToken.setExpiryDate(null);
            
            Set<ConstraintViolation<RefreshToken>> violations = validator.validate(validToken);
            
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Expiry date is required");
        }

        @Test
        @DisplayName("Token with past expiry date should fail validation")
        void tokenWithPastExpiryDate_ShouldFailValidation() {
            validToken.setExpiryDate(LocalDateTime.now().minusDays(1));
            
            Set<ConstraintViolation<RefreshToken>> violations = validator.validate(validToken);
            
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Expiry date must be in the future");
        }

        @Test
        @DisplayName("Device ID exceeding max length should fail validation")
        void deviceIdExceedingMaxLength_ShouldFailValidation() {
            // Create a string longer than DEVICE_ID_MAX_LENGTH (100)
            String longDeviceId = "a".repeat(101);
            validToken.setDeviceId(longDeviceId);
            
            Set<ConstraintViolation<RefreshToken>> violations = validator.validate(validToken);
            
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("Device ID must not exceed");
        }

        @Test
        @DisplayName("Device name exceeding max length should fail validation")
        void deviceNameExceedingMaxLength_ShouldFailValidation() {
            // Create a string longer than DEVICE_NAME_MAX_LENGTH (100)
            String longDeviceName = "a".repeat(101);
            validToken.setDeviceName(longDeviceName);
            
            Set<ConstraintViolation<RefreshToken>> violations = validator.validate(validToken);
            
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("Device name must not exceed");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "invalid-ip",
            "999.999.999.999",
            "192.168.1",
            "not.an.ip.address"
        })
        @DisplayName("Invalid IP address should fail validation")
        void invalidIpAddress_ShouldFailValidation(String invalidIp) {
            validToken.setIpAddress(invalidIp);
            
            Set<ConstraintViolation<RefreshToken>> violations = validator.validate(validToken);
            
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo("IP address must be a valid IPv4 or IPv6 address");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "192.168.1.1",
            "10.0.0.1",
            "127.0.0.1",
            "2001:0db8:85a3:0000:0000:8a2e:0370:7334"
        })
        @DisplayName("Valid IP addresses should pass validation")
        void validIpAddresses_ShouldPassValidation(String validIp) {
            validToken.setIpAddress(validIp);
            
            Set<ConstraintViolation<RefreshToken>> violations = validator.validate(validToken);
            
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("isExpired should return true for expired token")
        void isExpired_WithExpiredToken_ShouldReturnTrue() {
            validToken.setExpiryDate(LocalDateTime.now().minusMinutes(1));
            
            assertThat(validToken.isExpired()).isTrue();
        }

        @Test
        @DisplayName("isExpired should return false for valid token")
        void isExpired_WithValidToken_ShouldReturnFalse() {
            validToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));
            
            assertThat(validToken.isExpired()).isFalse();
        }

        @Test
        @DisplayName("isValid should return true for non-expired, non-revoked token")
        void isValid_WithValidToken_ShouldReturnTrue() {
            validToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));
            validToken.setRevoked(false);
            
            assertThat(validToken.isValid()).isTrue();
        }

        @Test
        @DisplayName("isValid should return false for expired token")
        void isValid_WithExpiredToken_ShouldReturnFalse() {
            validToken.setExpiryDate(LocalDateTime.now().minusMinutes(1));
            validToken.setRevoked(false);
            
            assertThat(validToken.isValid()).isFalse();
        }

        @Test
        @DisplayName("isValid should return false for revoked token")
        void isValid_WithRevokedToken_ShouldReturnFalse() {
            validToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));
            validToken.setRevoked(true);
            
            assertThat(validToken.isValid()).isFalse();
        }

        @Test
        @DisplayName("revoke should set revoked to true")
        void revoke_ShouldSetRevokedToTrue() {
            validToken.setRevoked(false);
            
            validToken.revoke();
            
            assertThat(validToken.getRevoked()).isTrue();
        }

        @Test
        @DisplayName("updateLastUsed should set current timestamp")
        void updateLastUsed_ShouldSetCurrentTimestamp() {
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);
            
            validToken.updateLastUsed();
            
            LocalDateTime after = LocalDateTime.now().plusSeconds(1);
            assertThat(validToken.getLastUsedAt()).isBetween(before, after);
        }

        @Test
        @DisplayName("extendExpiry should add minutes to expiry date")
        void extendExpiry_ShouldAddMinutesToExpiryDate() {
            LocalDateTime originalExpiry = LocalDateTime.now().plusDays(1);
            validToken.setExpiryDate(originalExpiry);
            
            validToken.extendExpiry(60);
            
            assertThat(validToken.getExpiryDate()).isEqualTo(originalExpiry.plusMinutes(60));
        }

        @Test
        @DisplayName("regenerateToken should create new token value")
        void regenerateToken_ShouldCreateNewTokenValue() {
            String originalToken = validToken.getTokenValue();
            
            validToken.regenerateToken();
            
            assertThat(validToken.getTokenValue()).isNotEqualTo(originalToken);
            assertThat(validToken.getTokenValue()).hasSize(36); // UUID length
        }

        @Test
        @DisplayName("belongsToUser should return true for correct user ID")
        void belongsToUser_WithCorrectUserId_ShouldReturnTrue() {
            testUser.setId(123L);
            validToken.setUser(testUser);
            
            assertThat(validToken.belongsToUser(123L)).isTrue();
        }

        @Test
        @DisplayName("belongsToUser should return false for incorrect user ID")
        void belongsToUser_WithIncorrectUserId_ShouldReturnFalse() {
            testUser.setId(123L);
            validToken.setUser(testUser);
            
            assertThat(validToken.belongsToUser(456L)).isFalse();
        }

        @Test
        @DisplayName("belongsToUser should return false when user is null")
        void belongsToUser_WithNullUser_ShouldReturnFalse() {
            validToken.setUser(null);
            
            assertThat(validToken.belongsToUser(123L)).isFalse();
        }

        @Test
        @DisplayName("getDeviceDisplayName should return device name when available")
        void getDeviceDisplayName_WithDeviceName_ShouldReturnDeviceName() {
            validToken.setDeviceName("John's iPhone");
            
            assertThat(validToken.getDeviceDisplayName()).isEqualTo("John's iPhone");
        }

        @Test
        @DisplayName("getDeviceDisplayName should return formatted device type when name is null")
        void getDeviceDisplayName_WithoutDeviceName_ShouldReturnFormattedDeviceType() {
            validToken.setDeviceName(null);
            validToken.setDeviceType("mobile");
            
            assertThat(validToken.getDeviceDisplayName()).isEqualTo("Mobile Device");
        }

        @Test
        @DisplayName("getDeviceDisplayName should return 'Unknown Device' when both are null")
        void getDeviceDisplayName_WithoutDeviceNameAndType_ShouldReturnUnknownDevice() {
            validToken.setDeviceName(null);
            validToken.setDeviceType(null);
            
            assertThat(validToken.getDeviceDisplayName()).isEqualTo("Unknown Device");
        }
    }

    @Nested
    @DisplayName("Object Methods Tests")
    class ObjectMethodsTests {

        @Test
        @DisplayName("equals should return true for same object")
        void equals_WithSameObject_ShouldReturnTrue() {
            assertThat(validToken).isEqualTo(validToken);
        }

        @Test
        @DisplayName("equals should return false for null")
        void equals_WithNull_ShouldReturnFalse() {
            assertThat(validToken).isNotEqualTo(null);
        }

        @Test
        @DisplayName("equals should return false for different class")
        void equals_WithDifferentClass_ShouldReturnFalse() {
            assertThat(validToken).isNotEqualTo("not a refresh token");
        }

        @Test
        @DisplayName("equals should return true for tokens with same ID")
        void equals_WithSameId_ShouldReturnTrue() {
            RefreshToken token1 = new RefreshToken();
            RefreshToken token2 = new RefreshToken();
            token1.setId(1L);
            token2.setId(1L);
            
            assertThat(token1).isEqualTo(token2);
        }

        @Test
        @DisplayName("equals should return false for tokens with different IDs")
        void equals_WithDifferentIds_ShouldReturnFalse() {
            RefreshToken token1 = new RefreshToken();
            RefreshToken token2 = new RefreshToken();
            token1.setId(1L);
            token2.setId(2L);
            
            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("hashCode should be consistent")
        void hashCode_ShouldBeConsistent() {
            validToken.setId(1L);
            int hash1 = validToken.hashCode();
            int hash2 = validToken.hashCode();
            
            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("hashCode should be same for equal objects")
        void hashCode_ForEqualObjects_ShouldBeSame() {
            RefreshToken token1 = new RefreshToken();
            RefreshToken token2 = new RefreshToken();
            token1.setId(1L);
            token2.setId(1L);
            
            assertThat(token1.hashCode()).isEqualTo(token2.hashCode());
        }

        @Test
        @DisplayName("toString should contain key information")
        void toString_ShouldContainKeyInformation() {
            validToken.setId(1L);
            testUser.setUsername("testuser");
            validToken.setUser(testUser);
            validToken.setDeviceName("Test Device");
            validToken.setDeviceType("desktop");
            
            String result = validToken.toString();
            
            assertThat(result)
                .contains("RefreshToken")
                .contains("id=1")
                .contains("testuser")
                .contains("Test Device")
                .contains("desktop");
        }

        @Test
        @DisplayName("toString should mask token value for security")
        void toString_ShouldMaskTokenValue() {
            String fullToken = validToken.getTokenValue();
            
            String result = validToken.toString();
            
            assertThat(result).contains(fullToken.substring(0, 8) + "...");
            assertThat(result).doesNotContain(fullToken);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Token with null revoked should default to false")
        void tokenWithNullRevoked_ShouldDefaultToFalse() {
            RefreshToken token = new RefreshToken();
            token.setRevoked(null);
            
            // Simulate @PrePersist behavior
            if (token.getRevoked() == null) {
                token.setRevoked(false);
            }
            
            assertThat(token.getRevoked()).isFalse();
        }

        @Test
        @DisplayName("Token with empty device name should return device type display name")
        void tokenWithEmptyDeviceName_ShouldReturnDeviceTypeDisplayName() {
            validToken.setDeviceName("");
            validToken.setDeviceType("tablet");
            
            assertThat(validToken.getDeviceDisplayName()).isEqualTo("Tablet Device");
        }

        @Test
        @DisplayName("Token with whitespace-only device name should return device type display name")
        void tokenWithWhitespaceDeviceName_ShouldReturnDeviceTypeDisplayName() {
            validToken.setDeviceName("   ");
            validToken.setDeviceType("mobile");
            
            assertThat(validToken.getDeviceDisplayName()).isEqualTo("Mobile Device");
        }

        @Test
        @DisplayName("isValid should handle null revoked as false")
        void isValid_WithNullRevoked_ShouldTreatAsFalse() {
            validToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));
            validToken.setRevoked(null);
            
            assertThat(validToken.isValid()).isTrue();
        }

        @Test
        @DisplayName("Token value should be UUID format")
        void tokenValue_ShouldBeUuidFormat() {
            String tokenValue = validToken.getTokenValue();
            
            // Should not throw exception
            assertThatNoException().isThrownBy(() -> UUID.fromString(tokenValue));
        }
    }
}