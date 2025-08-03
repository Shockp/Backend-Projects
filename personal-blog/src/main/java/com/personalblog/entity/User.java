package com.personalblog.entity;

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

import javax.management.relation.Role;

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
public class User {
    
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
    // ==================== Profile Information ====================
    // ==================== Social Media Links ====================
    // ==================== Email Verification ====================
    // ==================== Login Tracking ====================
    // ==================== Password Reset ====================
    // ==================== JPA Relationships ====================
    // ==================== Constructors ====================
    // ==================== UserDetails Implementation ====================
    // ==================== Utility Methods ====================
    // ==================== Getters and Setters ====================
    // ==================== Object Methods ====================

}