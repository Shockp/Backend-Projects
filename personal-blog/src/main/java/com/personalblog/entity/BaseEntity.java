package com.personalblog.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
    
    private static final long serialVersionUID = 1L;

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
    @NotNull
    private Boolean deleted = Boolean.FALSE;

    // ==================== Constructors ====================

    /**
     * Default constructor required by JPA.
     */
    protected BaseEntity() {
        // Protected to prevent direct instantiation while allowing subclass access
    }

    // ==================== Getters and Setters ====================

    /**
     * Gets the entity ID.
     * 
     * @return the entity ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the entity ID.
     * Should typically only be used by JPA or in test scenarios.
     * 
     * @param id the entity ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the version for optimistic locking.
     * 
     * @return the version number
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Sets the version for optimistic locking.
     * Should typically only be managed by JPA.
     * 
     * @param version the version number
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * Gets the creation timestamp.
     * 
     * @return when the entity was created
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     * Should typically only be managed by Spring Data JPA auditing.
     * 
     * @param createdAt when the entity was created
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the last modification timestamp.
     * 
     * @return when the entity was last updated
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the last modification timestamp.
     * Should typically only be managed by Spring Data JPA auditing.
     * 
     * @param updatedAt when the entity was last updated
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Gets the soft delete flag.
     * 
     * @return true if the entity is soft deleted, false otherwise
     */
    public Boolean getDeleted() {
        return deleted;
    }

    /**
     * Sets the soft delete flag.
     * 
     * @param deleted true to soft delete the entity, false otherwise
     */
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted != null ? deleted : Boolean.FALSE;
    }

    // ==================== Utility Methods ====================

    /**
     * Check if this entity is new (not yet persisted).
     * 
     * @return true if the entity is new (ID is null), false otherwise
     */
    public boolean isNew() {
        return this.id == null;
    }

    /**
     * Checks if this entity is soft deleted.
     * 
     * @return true if the entity is soft deleted, false otherwise
     */
    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.deleted);
    }

    /**
     * Marks this entity as soft deleted
     */
    public void markAsDeleted() {
        this.deleted = Boolean.TRUE;
    }

    /**
     * Restore this entity from soft deleted state
     */
    public void restore() {
        this.deleted = Boolean.FALSE;
    }

    // ==================== Object Methods ====================

    /**
     * Equals method based on ID for entity comparison.
     * Two entities are equal if they have the same ID and are not null.
     * 
     * @param obj the object to compare
     * @return true if the entities are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BaseEntity other = (BaseEntity) obj;
        return Objects.equals(this.id, other.id) && id != null;
    }

    /**
     * HashCode method based on ID.
     * Uses a constant for new entities to maintain consistency.
     * 
     * @return hash code of the entity
     */
    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : 31;
    }

    /**
     * String representation of the entity.
     * 
     * @return string representation including class name and ID
     */
    @Override
    public String toString() {
        return String.format("%s{id=%s, version=%s, createdAt=%s, updatedAt=%s, deleted=%s}",
            this.getClass().getSimpleName(),
            id,
            version,
            createdAt,
            updatedAt,
            deleted
        );
    }

    // ==================== JPA Lifecycle Callbacks ====================

    /**
     * Pre-persist callback to set default values before entity creation.
     * Called automatically by JPA before the entity is persisted.
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.deleted == null) {
            this.deleted = Boolean.FALSE;
        }
    }

    /**
     * Pre-update callback to update the timestamp before entity update.
     * Called automatically by JPA before the entity is updated.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}