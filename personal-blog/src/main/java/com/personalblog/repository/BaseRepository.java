package com.personalblog.repository;

import com.personalblog.entity.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Base repository interface providing common CRUD operations and utilities
 * for all entities extending BaseEntity.
 * 
 * This interface includes:
 * - Standard CRUD operations with soft delete support
 * - Pagination and sorting utilities
 * - Specification support for dynamic queries
 * - Common query methods for audit fields
 * - Bulk operations for performance optimization
 * - Security-focused query methods
 * 
 * All entity repositories should extend this interface to inherit
 * these common operations and maintain consistency across the application.
 * 
 * @param <T> the entity type extending BaseEntity
 * @param <ID> the type of the entity identifier (typically Long)
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 */
@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity, ID> 
        extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    // ==================== Soft Delete Operations ====================

    /**
     * Find all non-deleted entities.
     * 
     * @return list of all non-deleted entities
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = false")
    List<T> findAllActive();

    /**
     * Find all non-deleted entities with sorting.
     * 
     * @param sort the sort specification
     * @return sorted list of all non-deleted entities
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = false")
    List<T> findAllActive(Sort sort);

    /**
     * Find all non-deleted entities with pagination.
     * 
     * @param pageable the pagination information
     * @return page of non-deleted entities
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = false")
    Page<T> findAllActive(Pageable pageable);

    /**
     * Find all deleted entities (for audit purposes).
     * 
     * @return list of all soft-deleted entities
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = true")
    List<T> findAllDeleted();

    /**
     * Find all deleted entities with pagination.
     * 
     * @param pageable the pagination information
     * @return page of soft-deleted entities
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = true")
    Page<T> findAllDeleted(Pageable pageable);

    /**
     * Find active entity by ID.
     * 
     * @param id the entity ID
     * @return optional containing the entity if found and not deleted
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.deleted = false")
    Optional<T> findActiveById(@Param("id") ID id);

    /**
     * Check if an active entity exists by ID.
     * 
     * @param id the entity ID
     * @return true if an active entity with the given ID exists
     */
    @Query("SELECT COUNT(e) > 0 FROM #{#entityName} e WHERE e.id = :id AND e.deleted = false")
    boolean existsActiveById(@Param("id") ID id);

    /**
     * Count all active entities.
     * 
     * @return number of active entities
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.deleted = false")
    long countActive();

    /**
     * Count all deleted entities.
     * 
     * @return number of soft-deleted entities
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.deleted = true")
    long countDeleted();

    // ==================== Soft Delete Operations ====================

    /**
     * Soft delete an entity by ID.
     * 
     * @param id the entity ID to soft delete
     * @return number of affected rows
     */
    @Modifying
    @Transactional
    @Query("UPDATE #{#entityName} e SET e.deleted = true, e.updatedAt = CURRENT_TIMESTAMP WHERE e.id = :id AND e.deleted = false")
    int softDeleteById(@Param("id") ID id);

    /**
     * Soft delete multiple entities by IDs.
     * 
     * @param ids the list of entity IDs to soft delete
     * @return number of affected rows
     */
    @Modifying
    @Transactional
    @Query("UPDATE #{#entityName} e SET e.deleted = true, e.updatedAt = CURRENT_TIMESTAMP WHERE e.id IN :ids AND e.deleted = false")
    int softDeleteByIds(@Param("ids") List<ID> ids);

    /**
     * Restore a soft-deleted entity by ID.
     * 
     * @param id the entity ID to restore
     * @return number of affected rows
     */
    @Modifying
    @Transactional
    @Query("UPDATE #{#entityName} e SET e.deleted = false, e.updatedAt = CURRENT_TIMESTAMP WHERE e.id = :id AND e.deleted = true")
    int restoreById(@Param("id") ID id);

    /**
     * Permanently delete soft-deleted entities older than the specified date.
     * Use with caution - this operation cannot be undone.
     * 
     * @param cutoffDate the date before which soft-deleted entities will be permanently removed
     * @return number of permanently deleted entities
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM #{#entityName} e WHERE e.deleted = true AND e.updatedAt < :cutoffDate")
    int permanentlyDeleteOldSoftDeleted(@Param("cutoffDate") LocalDateTime cutoffDate);

    // ==================== Audit Query Methods ====================

    /**
     * Find entities created within a date range.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of entities created within the date range
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = false AND e.createdAt BETWEEN :startDate AND :endDate")
    List<T> findActiveByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * Find entities updated within a date range.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of entities updated within the date range
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = false AND e.updatedAt BETWEEN :startDate AND :endDate")
    List<T> findActiveByUpdatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * Find entities created after a specific date.
     * 
     * @param date the date after which entities were created
     * @param pageable the pagination information
     * @return page of entities created after the specified date
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = false AND e.createdAt > :date")
    Page<T> findActiveByCreatedAtAfter(@Param("date") LocalDateTime date, Pageable pageable);

    /**
     * Find entities updated after a specific date.
     * 
     * @param date the date after which entities were updated
     * @param pageable the pagination information
     * @return page of entities updated after the specified date
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = false AND e.updatedAt > :date")
    Page<T> findActiveByUpdatedAtAfter(@Param("date") LocalDateTime date, Pageable pageable);

    // ==================== Specification Support ====================

    /**
     * Find all active entities matching the given specification.
     * 
     * @param spec the specification to match
     * @return list of entities matching the specification
     */
    default List<T> findAllActive(@Nullable Specification<T> spec) {
        Specification<T> activeSpec = (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("deleted"), false);
        
        Specification<T> combinedSpec = spec != null ? 
            Specification.allOf(activeSpec, spec) : activeSpec;
        
        return findAll(combinedSpec);
    }

    /**
     * Find all active entities matching the given specification with sorting.
     * 
     * @param spec the specification to match
     * @param sort the sort specification
     * @return sorted list of entities matching the specification
     */
    default List<T> findAllActive(@Nullable Specification<T> spec, @NonNull Sort sort) {
        Specification<T> activeSpec = (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("deleted"), false);
        
        Specification<T> combinedSpec = spec != null ? 
            Specification.allOf(activeSpec, spec) : activeSpec;
        
        return findAll(combinedSpec, sort);
    }

    /**
     * Find all active entities matching the given specification with pagination.
     * 
     * @param spec the specification to match
     * @param pageable the pagination information
     * @return page of entities matching the specification
     */
    default Page<T> findAllActive(@Nullable Specification<T> spec, @NonNull Pageable pageable) {
        Specification<T> activeSpec = (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("deleted"), false);
        
        Specification<T> combinedSpec = spec != null ? 
            Specification.allOf(activeSpec, spec) : activeSpec;
        
        return findAll(combinedSpec, pageable);
    }

    /**
     * Count active entities matching the given specification.
     * 
     * @param spec the specification to match
     * @return number of entities matching the specification
     */
    default long countActive(@Nullable Specification<T> spec) {
        Specification<T> activeSpec = (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("deleted"), false);
        
        Specification<T> combinedSpec = spec != null ? 
            Specification.allOf(activeSpec, spec) : activeSpec;
        
        return count(combinedSpec);
    }

    // ==================== Bulk Operations ====================

    /**
     * Update multiple entities with a custom specification.
     * This method should be overridden in specific repositories for type-safe updates.
     * 
     * @param spec the specification to match entities for update
     * @return number of updated entities
     */
    default int bulkUpdate(@NonNull Specification<T> spec) {
        // This is a placeholder - specific repositories should override with actual update logic
        throw new UnsupportedOperationException(
            "Bulk update operations must be implemented in specific repository interfaces");
    }

    /**
     * Batch save entities for improved performance.
     * 
     * @param entities the entities to save
     * @return list of saved entities
     */
    default List<T> saveAllInBatch(@NonNull Iterable<T> entities) {
        return saveAllAndFlush((Iterable<T>) entities);
    }

    // ==================== Utility Methods ====================

    /**
     * Check if any active entities exist.
     * 
     * @return true if at least one active entity exists
     */
    default boolean hasActiveEntities() {
        return countActive() > 0;
    }

    /**
     * Check if any deleted entities exist.
     * 
     * @return true if at least one deleted entity exists
     */
    default boolean hasDeletedEntities() {
        return countDeleted() > 0;
    }

    /**
     * Get the total count of all entities (active and deleted).
     * 
     * @return total number of entities
     */
    default long countAll() {
        return count();
    }

    /**
     * Find a random active entity.
     * Note: This method may not be performant for large datasets.
     * 
     * @return optional containing a random active entity
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = false ORDER BY FUNCTION('RANDOM')")
    Optional<T> findRandomActive();

    // ==================== Security & Validation ====================

    /**
     * Validate entity before save operations.
     * This method can be overridden in specific repositories for custom validation.
     * 
     * @param entity the entity to validate
     * @throws IllegalArgumentException if the entity is invalid
     */
    default void validateEntity(@NonNull T entity) {
        if (entity.getId() != null && entity.getId().equals(0L)) {
            throw new IllegalArgumentException("Entity ID cannot be zero");
        }
    }

    /**
     * Safe save operation with validation.
     * 
     * @param entity the entity to save
     * @return the saved entity
     * @throws IllegalArgumentException if the entity is invalid
     */
    default T safeSave(@NonNull T entity) {
        validateEntity(entity);
        return save(entity);
    }

    /**
     * Safe save all operation with validation.
     * 
     * @param entities the entities to save
     * @return list of saved entities
     * @throws IllegalArgumentException if any entity is invalid
     */
    default List<T> safeSaveAll(@NonNull Iterable<T> entities) {
        entities.forEach(this::validateEntity);
        return saveAll(entities);
    }
}
