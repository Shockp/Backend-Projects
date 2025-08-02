package com.personalblog.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Base entity class providing common fields and functionality for all entities.
 * 
 * This class contains:
 * - Primary key (id)
 * - Audit fields (created and updated timestamps)
 * - Version for optimistic locking
 * - Common methods (equals, hashCode, toString)
 * 
 * All entities should extend this class to inherit these common properties.
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 2025-08-02
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {
    
    private static final long serialVersionID = 1L;

    /**
     * Primary key for all entities.
     * Uses IDENTITY strategy for auto-incremented database IDs.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    /**
     * Version field for optimistic locking.
     * Automatically managed by JPA to prevent concurrent modification issues.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    /**
     * Timestamp of when the entity was created.
     * Automatically set by Spring Data JPA auditing on entity creation.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of when the entity was last modified.
     * Automatically updated by Spring Data JPA auditing on entity updates.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Soft delete flag. When true, the entity is considered deleted
     * but remains in the database for audit purposes.
     */
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = Boolean.FALSE;
}