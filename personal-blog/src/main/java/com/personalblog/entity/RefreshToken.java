package com.personalblog.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Formula;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

/**
 * RefreshToken entity for JWT token management.
 * 
 * Features:
 * - Secure token storage with UUID values
 * - Expiry date management
 * - User relationship for token ownership
 * - Device information tracking
 * - Automatic token cleanup support
 * - Security audit fields
 * 
 * Inherits audit fields (id, createdAt, updatedAt, version) from BaseEntity.
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.1
 * @since 1.0
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_refresh_token_value", columnList = "token_value", unique = true),
    @Index(name = "idx_refresh_token_user_id", columnList = "user_id"),
    @Index(name = "idx_refresh_token_expiry", columnList = "expiry_date"),
    @Index(name = "idx_refresh_token_device", columnList = "device_id")
})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class RefreshToken extends BaseEntity {
    
    // ==================== Constants ====================
    
    private static final int DEVICE_NAME_MAX_LENGTH = 100;
    private static final int DEVICE_TYPE_MAX_LENGTH = 50;
    private static final int IP_ADDRESS_MAX_LENGTH = 45;
    private static final int USER_AGENT_MAX_LENGTH = 500;
    private static final int DEVICE_ID_MAX_LENGTH = 100;
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String ENCRYPTION_KEY = "NoKeyNoWin";
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int CLEANUP_BATCH_SIZE = 1000;
    
    // ==================== Core Fields ====================

    /**
     * Encrypted token value (UUID).
     * Used for refresh token validation.
     */
    @NotNull(message = "Token value is required")
    @Column(name = "token_value", nullable = false, unique = true, length = 500)
    private String encryptedTokenValue;
    
    /**
     * Transient field for the decrypted token value.
     * Not persisted to database.
     */
    @Transient
    private String tokenValue;

    /**
     * Token expiry date.
     * Tokens are invalid after this date.
     */
    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    /**
     * Whether the token has been revoked.
     * Revoked tokens cannot be used for refresh.
     */
    @NotNull(message = "Revoked status is required")
    @Column(name = "is_revoked", nullable = false)
    private Boolean revoked = false;

    // ==================== User Relationship ====================

    /**
     * User who owns this refresh token.
     * Required relationship with optimized lazy loading.
     */
    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_refresh_token_user"))
    private User user;
    
    /**
     * User ID for efficient queries without loading the full User entity.
     */
    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    // ==================== Device Information ====================

    /**
     * Device identifier for token tracking.
     * Used to identify unique devices/sessions.
     */
    @Size(max = DEVICE_ID_MAX_LENGTH, 
          message = "Device ID must not exceed {max} characters")
    @Column(name = "device_id", length = DEVICE_ID_MAX_LENGTH)
    private String deviceId;

    /**
     * Human-readable device name.
     * E.g., "John's iPhone", "Chrome on Windows"
     */
    @Size(max = DEVICE_NAME_MAX_LENGTH, 
          message = "Device name must not exceed {max} characters")
    @Column(name = "device_name", length = DEVICE_NAME_MAX_LENGTH)
    private String deviceName;

    /**
     * Device type classification.
     * E.g., "mobile", "desktop", "tablet"
     */
    @Size(max = DEVICE_TYPE_MAX_LENGTH, 
          message = "Device type must not exceed {max} characters")
    @Column(name = "device_type", length = DEVICE_TYPE_MAX_LENGTH)
    private String deviceType;

    // ==================== Security & Tracking ====================

    /**
     * IP address when token was created.
     * Used for security monitoring.
     */
    @Size(max = IP_ADDRESS_MAX_LENGTH, 
          message = "IP address must not exceed {max} characters")
    @Pattern(regexp = "^$|^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$",
             message = "IP address must be a valid IPv4 or IPv6 address")
    @Column(name = "ip_address", length = IP_ADDRESS_MAX_LENGTH)
    private String ipAddress;

    /**
     * User agent string when token was created.
     * Used for device identification and security.
     */
    @Size(max = USER_AGENT_MAX_LENGTH, 
          message = "User agent must not exceed {max} characters")
    @Column(name = "user_agent", length = USER_AGENT_MAX_LENGTH)
    private String userAgent;

    /**
     * Last time this token was used.
     * Updated on each refresh operation.
     */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    // ==================== Rate Limiting Fields ====================
    
    /**
     * Number of failed authentication attempts with this token.
     * Used for rate limiting and security monitoring.
     */
    @Min(value = 0, message = "Failed attempts cannot be negative")
    @Column(name = "failed_attempts", nullable = false)
    private Integer failedAttempts = 0;
    
    /**
     * Timestamp when the token was last blocked due to rate limiting.
     * Null if never blocked.
     */
    @Column(name = "blocked_until")
    private LocalDateTime blockedUntil;
    
    /**
     * Number of times this token has been used successfully.
     * Used for analytics and security monitoring.
     */
    @Min(value = 0, message = "Usage count cannot be negative")
    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;
    
    /**
     * Whether this token is marked for cleanup.
     * Used by background cleanup processes.
     */
    @Column(name = "marked_for_cleanup", nullable = false)
    private Boolean markedForCleanup = false;
    
    /**
     * Computed field to check if token is expired (for queries).
     */
    @Formula("(expiry_date < CURRENT_TIMESTAMP)")
    private Boolean expired;

    // ==================== Constructors ====================

    /**
     * Default constructor for JPA.
     */
    public RefreshToken() {
        super();
        this.revoked = false;
        this.failedAttempts = 0;
        this.usageCount = 0;
        this.markedForCleanup = false;
    }

    /**
     * Constructor for creating a new refresh token.
     * 
     * @param user The user who owns this token
     * @param expiryDate When the token expires
     */
    public RefreshToken(User user, LocalDateTime expiryDate) {
        this();
        this.user = user;
        this.userId = user != null ? user.getId() : null;
        this.expiryDate = expiryDate;
    }

    /**
     * Constructor with device information.
     * 
     * @param user The user who owns this token
     * @param expiryDate When the token expires
     * @param deviceId Device identifier
     * @param deviceName Human-readable device name
     * @param ipAddress IP address of the device
     */
    public RefreshToken(User user, LocalDateTime expiryDate, String deviceId, 
                       String deviceName, String ipAddress) {
        this(user, expiryDate);
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.ipAddress = ipAddress;
    }

    // ==================== Getters and Setters ====================

    public String getTokenValue() {
        // Decrypt on-demand if not already decrypted
        if (tokenValue == null && encryptedTokenValue != null) {
            this.tokenValue = decryptToken(encryptedTokenValue);
        }
        return tokenValue;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
        // Encrypt immediately when setting, or clear encrypted value if null
        if (tokenValue != null) {
            this.encryptedTokenValue = encryptToken(tokenValue);
        } else {
            this.encryptedTokenValue = null;
        }
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Boolean getRevoked() {
        return revoked;
    }

    public void setRevoked(Boolean revoked) {
        this.revoked = revoked;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public Integer getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(Integer failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public LocalDateTime getBlockedUntil() {
        return blockedUntil;
    }

    public void setBlockedUntil(LocalDateTime blockedUntil) {
        this.blockedUntil = blockedUntil;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }

    public Boolean getMarkedForCleanup() {
        return markedForCleanup;
    }

    public void setMarkedForCleanup(Boolean markedForCleanup) {
        this.markedForCleanup = markedForCleanup;
    }

    public Boolean getExpired() {
        return expired;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    public String getEncryptedTokenValue() {
        return encryptedTokenValue;
    }

    public void setEncryptedTokenValue(String encryptedTokenValue) {
        this.encryptedTokenValue = encryptedTokenValue;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    // ==================== Business Methods ====================

    /**
     * Checks if the token is expired.
     * 
     * @return true if the token is past its expiry date
     */
    public boolean isExpired() {
        return expiryDate != null && LocalDateTime.now().isAfter(this.expiryDate);
    }
    
    /**
     * Checks if the token is currently blocked due to rate limiting.
     * 
     * @return true if the token is blocked, false otherwise
     */
    public boolean isBlocked() {
        return blockedUntil != null && LocalDateTime.now().isBefore(blockedUntil);
    }
    
    /**
     * Checks if the token has exceeded the maximum failed attempts.
     * 
     * @return true if max attempts exceeded, false otherwise
     */
    public boolean hasExceededMaxAttempts() {
        return failedAttempts != null && failedAttempts >= MAX_FAILED_ATTEMPTS;
    }

    /**
     * Checks if the token is valid (not expired, not revoked, and not blocked).
     * 
     * @return true if the token can be used for refresh
     */
    public boolean isValid() {
        return !isExpired() && !Boolean.TRUE.equals(this.revoked) && !isBlocked() && !hasExceededMaxAttempts();
    }

    /**
     * Revokes the token, making it unusable.
     */
    public void revoke() {
        this.revoked = true;
    }
    
    /**
     * Records a successful token usage.
     * Resets failed attempts and increments usage count.
     */
    public void recordSuccessfulUsage() {
        this.failedAttempts = 0;
        this.usageCount = (this.usageCount != null ? this.usageCount : 0) + 1;
        this.lastUsedAt = LocalDateTime.now();
        this.blockedUntil = null; // Clear any existing block
    }
    
    /**
     * Records a failed authentication attempt.
     * Increments failed attempts and blocks token if max attempts exceeded.
     */
    public void recordFailedAttempt() {
        this.failedAttempts = (this.failedAttempts != null ? this.failedAttempts : 0) + 1;
        
        if (hasExceededMaxAttempts()) {
            // Block token for 1 hour after max attempts
            this.blockedUntil = LocalDateTime.now().plusHours(1);
        }
    }
    
    /**
     * Marks the token for cleanup by background processes.
     */
    public void markForCleanup() {
        this.markedForCleanup = true;
    }
    
    /**
     * Checks if the token should be cleaned up.
     * 
     * @return true if token is expired, revoked, or marked for cleanup
     */
    public boolean shouldBeCleanedUp() {
        return isExpired() || Boolean.TRUE.equals(this.revoked) || Boolean.TRUE.equals(markedForCleanup);
    }

    /**
     * Updates the last used timestamp to current time.
     * @deprecated Use recordSuccessfulUsage() instead for better security tracking
     */
    @Deprecated
    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }

    /**
     * Extends the token expiry by the specified duration.
     * 
     * @param additionalMinutes Minutes to add to current expiry
     */
    public void extendExpiry(long additionalMinutes) {
        this.expiryDate = this.expiryDate.plusMinutes(additionalMinutes);
    }

    /**
     * Generates a new token value.
     * Used for token rotation.
     */
    public void regenerateToken() {
        this.tokenValue = UUID.randomUUID().toString();
    }

    /**
     * Checks if this token belongs to the specified user.
     * 
     * @param userId The user ID to check
     * @return true if the token belongs to the user
     */
    public boolean belongsToUser(Long userId) {
        return this.user != null && Objects.equals(this.user.getId(), userId);
    }

    /**
     * Gets a display name for the device.
     * Returns device name if available, otherwise device type or "Unknown Device".
     * 
     * @return Display name for the device
     */
    public String getDeviceDisplayName() {
        if (deviceName != null && !deviceName.trim().isEmpty()) {
            return deviceName;
        }
        if (deviceType != null && !deviceType.trim().isEmpty()) {
            return deviceType.substring(0, 1).toUpperCase() + deviceType.substring(1) + " Device";
        }
        return "Unknown Device";
    }

    // ==================== Encryption Methods ====================

    /**
     * Encrypts a token value using AES encryption.
     * 
     * @param tokenValue The plain text token value
     * @return The encrypted token value
     */
    private String encryptToken(String tokenValue) {
        if (tokenValue == null || tokenValue.isEmpty()) {
            return null;
        }
        
        try {
            SecretKeySpec secretKey = new SecretKeySpec(ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8), ENCRYPTION_ALGORITHM);
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(tokenValue.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt token", e);
        }
    }

    /**
     * Decrypts an encrypted token value.
     * 
     * @param encryptedTokenValue The encrypted token value
     * @return The decrypted token value
     */
    private String decryptToken(String encryptedTokenValue) {
        if (encryptedTokenValue == null || encryptedTokenValue.isEmpty()) {
            return null;
        }
        
        try {
            SecretKeySpec secretKey = new SecretKeySpec(ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8), ENCRYPTION_ALGORITHM);
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedTokenValue));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt token", e);
        }
    }

    /**
      * JPA callback to decrypt token after loading from database.
      */
     @PostLoad
     private void decryptTokenValue() {
         if (encryptedTokenValue != null && tokenValue == null) {
             this.tokenValue = decryptToken(encryptedTokenValue);
         }
     }
 
     /**
      * JPA callback to process token before persisting/updating to database.
      * Combines validation, encryption, and default value setting.
      */
     @PrePersist
     @PreUpdate
     private void processToken() {
         setDefaults();
         validateToken();
         encryptTokenValue();
     }
     
     /**
      * Encrypts token value if needed.
      */
     private void encryptTokenValue() {
         if (tokenValue != null && (encryptedTokenValue == null || encryptedTokenValue.isEmpty())) {
             this.encryptedTokenValue = encryptToken(tokenValue);
         }
     }
     
     // ==================== Static Cleanup Methods ====================
     
     /**
      * Static method to determine if a batch of tokens should be cleaned up.
      * Used by repository cleanup queries.
      * 
      * @return SQL condition for cleanup eligibility
      */
     public static String getCleanupCondition() {
         return "(expiry_date < CURRENT_TIMESTAMP OR revoked = true OR marked_for_cleanup = true)";
     }
     
     /**
      * Gets the batch size for cleanup operations.
      * 
      * @return The recommended batch size for cleanup
      */
     public static int getCleanupBatchSize() {
         return CLEANUP_BATCH_SIZE;
     }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        RefreshToken that = (RefreshToken) obj;
        return Objects.equals(getId(), that.getId()) && getId() != null;
    }

    @Override
    public int hashCode() {
        return getId() != null ? Objects.hash(getId()) : 31;
    }

    @Override
    public String toString() {
        return String.format(
            "RefreshToken{id=%s, tokenValue='%s', expiryDate=%s, revoked=%s, " +
            "user=%s, deviceName='%s', deviceType='%s', lastUsedAt=%s, createdAt=%s}",
            getId(),
            tokenValue != null ? tokenValue.substring(0, 8) + "..." : null,
            expiryDate,
            revoked,
            user != null ? user.getUsername() : null,
            deviceName,
            deviceType,
            lastUsedAt,
            getCreatedAt()
        );
    }

    // ==================== Validation Methods ====================

    /**
     * Validates token data.
     */
    private void validateToken() {
        if (tokenValue == null || tokenValue.trim().isEmpty()) {
            throw new IllegalStateException("Token value cannot be null or empty");
        }
        
        if (expiryDate != null && expiryDate.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot create token with past expiry date");
        }
        
        if (user == null) {
            throw new IllegalStateException("Refresh token must be associated with a user");
        }
    }

    /**
     * Sets default values before persisting.
     */
    private void setDefaults() {
        if (revoked == null) {
            revoked = false;
        }
        
        if (tokenValue == null) {
            tokenValue = UUID.randomUUID().toString();
        }
    }
}