package com.personalblog.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * JPA Integration tests for BaseEntity class.
 * 
 * Tests the actual JPA lifecycle callbacks, auditing functionality,
 * and database interactions using TestEntityManager.
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("BaseEntity JPA Integration Tests")
class BaseEntityJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    /**
     * Concrete JPA entity for testing BaseEntity functionality.
     */
    @Entity
    @Table(name = "test_entities")
    public static class TestJpaEntity extends BaseEntity {
        
        @Column(name = "name")
        private String name;
        
        @Column(name = "description")
        private String description;
        
        // Default constructor for JPA
        public TestJpaEntity() {
            super();
        }
        
        public TestJpaEntity(String name) {
            this.name = name;
        }
        
        public TestJpaEntity(String name, String description) {
            this.name = name;
            this.description = description;
        }
        
        // Getters and setters
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
    }

    @Test
    @DisplayName("Should auto-generate ID on persist")
    void shouldAutoGenerateIdOnPersist() {
        // Given
        TestJpaEntity entity = new TestJpaEntity("Test Entity");
        assertThat(entity.getId()).isNull();
        assertThat(entity.isNew()).isTrue();

        // When
        TestJpaEntity savedEntity = entityManager.persistAndFlush(entity);

        // Then
        assertThat(savedEntity.getId()).isNotNull().isPositive();
        assertThat(savedEntity.isNew()).isFalse();
    }

    @Test
    @DisplayName("Should set audit timestamps on persist")
    void shouldSetAuditTimestampsOnPersist() {
        // Given
        TestJpaEntity entity = new TestJpaEntity("Audit Test");
        LocalDateTime beforePersist = LocalDateTime.now();

        // When
        TestJpaEntity savedEntity = entityManager.persistAndFlush(entity);
        LocalDateTime afterPersist = LocalDateTime.now();

        // Then
        assertThat(savedEntity.getCreatedAt())
            .isNotNull()
            .isBetween(beforePersist, afterPersist);
        assertThat(savedEntity.getUpdatedAt())
            .isNotNull()
            .isBetween(beforePersist, afterPersist)
            .isEqualTo(savedEntity.getCreatedAt());
    }

    @Test
    @DisplayName("Should initialize version on persist")
    void shouldInitializeVersionOnPersist() {
        // Given
        TestJpaEntity entity = new TestJpaEntity("Version Test");
        assertThat(entity.getVersion()).isNull();

        // When
        TestJpaEntity savedEntity = entityManager.persistAndFlush(entity);

        // Then
        assertThat(savedEntity.getVersion()).isNotNull().isEqualTo(0L);
    }

    @Test
    @DisplayName("Should set deleted flag to false on persist")
    void shouldSetDeletedFlagToFalseOnPersist() {
        // Given
        TestJpaEntity entity = new TestJpaEntity("Delete Test");

        // When
        TestJpaEntity savedEntity = entityManager.persistAndFlush(entity);

        // Then
        assertThat(savedEntity.getDeleted()).isEqualTo(Boolean.FALSE);
        assertThat(savedEntity.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("Should update version and maintain audit fields on entity update")
    void shouldUpdateVersionAndMaintainAuditFieldsOnEntityUpdate() {
        // Given - Persist initial entity
        TestJpaEntity entity = new TestJpaEntity("Update Test");
        TestJpaEntity savedEntity = entityManager.persistAndFlush(entity);
        Long initialVersion = savedEntity.getVersion();

        // Clear the persistence context to ensure fresh load
        entityManager.clear();

        // When - Update the entity
        TestJpaEntity foundEntity = entityManager.find(TestJpaEntity.class, savedEntity.getId());
        foundEntity.setName("Updated Name");
        foundEntity.setDescription("Updated Description");
        
        // Force update
        TestJpaEntity updatedEntity = entityManager.persistAndFlush(foundEntity);

        // Then
        assertThat(updatedEntity.getCreatedAt())
            .isNotNull(); // Should be set and not change
        assertThat(updatedEntity.getUpdatedAt())
            .isNotNull(); // Should be set
        assertThat(updatedEntity.getVersion())
            .isEqualTo(initialVersion + 1); // Version should increment
        assertThat(updatedEntity.getName()).isEqualTo("Updated Name");
        assertThat(updatedEntity.getDescription()).isEqualTo("Updated Description");
        
        // Verify audit fields are properly set (basic validation)
        assertThat(updatedEntity.getUpdatedAt())
            .isNotNull()
            .isNotEqualTo(LocalDateTime.MIN)
            .isNotEqualTo(LocalDateTime.MAX);
    }

    @Test
    @DisplayName("Should increment version on entity updates")
    void shouldIncrementVersionOnEntityUpdates() {
        // Given - Persist entity
        TestJpaEntity entity = new TestJpaEntity("Version Test");
        TestJpaEntity savedEntity = entityManager.persistAndFlush(entity);
        Long entityId = savedEntity.getId();
        Long initialVersion = savedEntity.getVersion();
        
        assertThat(initialVersion).isEqualTo(0L);

        // Clear context and load fresh instance
        entityManager.clear();
        TestJpaEntity foundEntity = entityManager.find(TestJpaEntity.class, entityId);

        // When - Update the entity
        foundEntity.setName("Updated Name");
        entityManager.flush();

        // Then - Version should be incremented
        assertThat(foundEntity.getVersion()).isEqualTo(initialVersion + 1);
        
        // When - Update again
        foundEntity.setDescription("Updated Description");
        entityManager.flush();
        
        // Then - Version should be incremented again
        assertThat(foundEntity.getVersion()).isEqualTo(initialVersion + 2);
    }

    @Test
    @DisplayName("Should persist and retrieve soft deleted entities")
    void shouldPersistAndRetrieveSoftDeletedEntities() {
        // Given
        TestJpaEntity entity = new TestJpaEntity("Soft Delete Test");
        TestJpaEntity savedEntity = entityManager.persistAndFlush(entity);
        Long entityId = savedEntity.getId();

        // When - Mark as deleted and update
        savedEntity.markAsDeleted();
        entityManager.persistAndFlush(savedEntity);
        entityManager.clear();

        // Then - Entity should still exist in database but marked as deleted
        TestJpaEntity foundEntity = entityManager.find(TestJpaEntity.class, entityId);
        assertThat(foundEntity).isNotNull();
        assertThat(foundEntity.isDeleted()).isTrue();
        assertThat(foundEntity.getDeleted()).isEqualTo(Boolean.TRUE);

        // When - Restore entity
        foundEntity.restore();
        entityManager.persistAndFlush(foundEntity);
        entityManager.clear();

        // Then - Entity should be restored
        TestJpaEntity restoredEntity = entityManager.find(TestJpaEntity.class, entityId);
        assertThat(restoredEntity).isNotNull();
        assertThat(restoredEntity.isDeleted()).isFalse();
        assertThat(restoredEntity.getDeleted()).isEqualTo(Boolean.FALSE);
    }

    @Test
    @DisplayName("Should maintain entity equality across persistence operations")
    void shouldMaintainEntityEqualityAcrossPersistenceOperations() {
        // Given
        TestJpaEntity entity1 = new TestJpaEntity("Equality Test 1");
        TestJpaEntity entity2 = new TestJpaEntity("Equality Test 2");

        // When - Persist entities
        TestJpaEntity savedEntity1 = entityManager.persistAndFlush(entity1);
        TestJpaEntity savedEntity2 = entityManager.persistAndFlush(entity2);
        entityManager.clear();

        // Load fresh instances
        TestJpaEntity loadedEntity1a = entityManager.find(TestJpaEntity.class, savedEntity1.getId());
        TestJpaEntity loadedEntity1b = entityManager.find(TestJpaEntity.class, savedEntity1.getId());
        TestJpaEntity loadedEntity2 = entityManager.find(TestJpaEntity.class, savedEntity2.getId());

        // Then - Entities with same ID should be equal
        assertThat(loadedEntity1a).isEqualTo(loadedEntity1b);
        assertThat(loadedEntity1a).isNotEqualTo(loadedEntity2);
        assertThat(loadedEntity1a.hashCode()).isEqualTo(loadedEntity1b.hashCode());
        assertThat(loadedEntity1a.hashCode()).isNotEqualTo(loadedEntity2.hashCode());
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void shouldHandleNullValuesGracefully() {
        // Given
        TestJpaEntity entity = new TestJpaEntity();
        entity.setName(null);
        entity.setDescription(null);

        // When
        TestJpaEntity savedEntity = entityManager.persistAndFlush(entity);

        // Then
        assertThat(savedEntity.getId()).isNotNull();
        assertThat(savedEntity.getName()).isNull();
        assertThat(savedEntity.getDescription()).isNull();
        assertThat(savedEntity.getCreatedAt()).isNotNull();
        assertThat(savedEntity.getUpdatedAt()).isNotNull();
        assertThat(savedEntity.getDeleted()).isEqualTo(Boolean.FALSE);
    }

    @Test
    @DisplayName("Should generate meaningful toString representation")
    void shouldGenerateMeaningfulToStringRepresentation() {
        // Given
        TestJpaEntity entity = new TestJpaEntity("ToString Test");
        TestJpaEntity savedEntity = entityManager.persistAndFlush(entity);

        // When
        String toStringResult = savedEntity.toString();

        // Then
        assertThat(toStringResult)
            .contains("TestJpaEntity")
            .contains("id=" + savedEntity.getId())
            .contains("version=" + savedEntity.getVersion())
            .contains("deleted=false")
            .contains(savedEntity.getCreatedAt().toString())
            .contains(savedEntity.getUpdatedAt().toString());
    }

    @Test
    @DisplayName("Should support batch operations efficiently")
    void shouldSupportBatchOperationsEfficiently() {
        // Given
        int batchSize = 100;
        java.util.List<TestJpaEntity> entities = new java.util.ArrayList<>();

        // When - Create and persist multiple entities
        for (int i = 0; i < batchSize; i++) {
            TestJpaEntity entity = new TestJpaEntity("Batch Entity " + i, "Description " + i);
            entities.add(entity);
            entityManager.persist(entity);
            
            // Flush and clear every 20 entities to avoid memory issues
            if (i % 20 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
        entityManager.clear();

        // Then - All entities should be persisted with proper audit fields
        for (int i = 0; i < batchSize; i++) {
            TestJpaEntity foundEntity = entityManager.find(TestJpaEntity.class, entities.get(i).getId());
            assertThat(foundEntity).isNotNull();
            assertThat(foundEntity.getName()).isEqualTo("Batch Entity " + i);
            assertThat(foundEntity.getCreatedAt()).isNotNull();
            assertThat(foundEntity.getUpdatedAt()).isNotNull();
            assertThat(foundEntity.getDeleted()).isEqualTo(Boolean.FALSE);
        }
    }

    @Test
    @DisplayName("Should handle entity lifecycle state transitions")
    void shouldHandleEntityLifecycleStateTransitions() {
        // Given - Transient state
        TestJpaEntity entity = new TestJpaEntity("Lifecycle Test");
        assertThat(entity.isNew()).isTrue();
        assertThat(entity.getId()).isNull();

        // When - Persist (Transient -> Managed)
        TestJpaEntity managedEntity = entityManager.persistAndFlush(entity);
        assertThat(managedEntity.isNew()).isFalse();
        assertThat(managedEntity.getId()).isNotNull();

        // When - Detach (Managed -> Detached)
        entityManager.detach(managedEntity);
        managedEntity.setName("Updated while detached");

        // When - Merge (Detached -> Managed)
        TestJpaEntity mergedEntity = entityManager.merge(managedEntity);
        entityManager.flush();

        // Then - Changes should be persisted
        entityManager.clear();
        TestJpaEntity foundEntity = entityManager.find(TestJpaEntity.class, mergedEntity.getId());
        assertThat(foundEntity.getName()).isEqualTo("Updated while detached");
        assertThat(foundEntity.getUpdatedAt()).isAfter(foundEntity.getCreatedAt());
    }
}