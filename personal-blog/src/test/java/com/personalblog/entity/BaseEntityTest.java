package com.personalblog.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for BaseEntity class.
 * 
 * Tests cover:
 * - JPA lifecycle callbacks (@PrePersist, @PreUpdate)
 * - Utility methods (isNew, isDeleted, markAsDeleted, restore)
 * - Object methods (equals, hashCode, toString)
 * - Getters and setters
 * - Edge cases and null handling
 * - Soft delete functionality
 * 
 * Following BDD patterns with given-when-then structure.
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("BaseEntity Tests")
class BaseEntityTest {

    /**
     * Concrete implementation of BaseEntity for testing purposes.
     */
    private static class TestEntity extends BaseEntity {
        private String name;
        
        public TestEntity() {
            super();
        }
        
        public TestEntity(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }

    @Nested
    @DisplayName("Constructor and Initialization Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create new entity with default values")
        void shouldCreateNewEntityWithDefaultValues() {
            // When
            TestEntity entity = new TestEntity();

            // Then
            assertThat(entity)
                .satisfies(e -> {
                    assertThat(e.getId()).isNull();
                    assertThat(e.getVersion()).isNull();
                    assertThat(e.getCreatedAt()).isNull();
                    assertThat(e.getUpdatedAt()).isNull();
                    assertThat(e.getDeleted()).isEqualTo(Boolean.FALSE);
                    assertThat(e.isNew()).isTrue();
                    assertThat(e.isDeleted()).isFalse();
                });
        }

        @Test
        @DisplayName("Should create entity with custom name")
        void shouldCreateEntityWithCustomName() {
            // Given
            String expectedName = "Test Entity";

            // When
            TestEntity entity = new TestEntity(expectedName);

            // Then
            assertThat(entity.getName()).isEqualTo(expectedName);
            assertThat(entity.getDeleted()).isEqualTo(Boolean.FALSE);
        }
    }

    @Nested
    @DisplayName("JPA Lifecycle Callback Tests")
    class LifecycleCallbackTests {

        @Test
        @DisplayName("Should set timestamps on @PrePersist")
        void shouldSetTimestampsOnPrePersist() {
            // Given
            TestEntity entity = new TestEntity("Test");
            LocalDateTime beforePersist = LocalDateTime.now();

            // When
            entity.onCreate(); // Simulate @PrePersist
            LocalDateTime afterPersist = LocalDateTime.now();

            // Then
            assertThat(entity.getCreatedAt())
                .isNotNull()
                .isBetween(beforePersist, afterPersist);
            assertThat(entity.getUpdatedAt())
                .isNotNull()
                .isBetween(beforePersist, afterPersist)
                .isEqualTo(entity.getCreatedAt());
            assertThat(entity.getDeleted()).isEqualTo(Boolean.FALSE);
        }

        @Test
        @DisplayName("Should not override existing createdAt on @PrePersist")
        void shouldNotOverrideExistingCreatedAtOnPrePersist() {
            // Given
            TestEntity entity = new TestEntity("Test");
            LocalDateTime existingCreatedAt = LocalDateTime.now().minusDays(1);
            entity.setCreatedAt(existingCreatedAt);

            // When
            entity.onCreate(); // Simulate @PrePersist

            // Then
            assertThat(entity.getCreatedAt()).isEqualTo(existingCreatedAt);
            assertThat(entity.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should update timestamp on @PreUpdate")
        void shouldUpdateTimestampOnPreUpdate() {
            // Given
            TestEntity entity = new TestEntity("Test");
            entity.onCreate(); // Simulate initial persist
            LocalDateTime initialUpdatedAt = entity.getUpdatedAt();
            
            // Wait a bit to ensure different timestamp
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // When
            entity.onUpdate(); // Simulate @PreUpdate

            // Then
            assertThat(entity.getUpdatedAt())
                .isNotNull()
                .isAfter(initialUpdatedAt);
        }

        @Test
        @DisplayName("Should not override existing deleted flag on @PrePersist")
        void shouldNotOverrideExistingDeletedFlagOnPrePersist() {
            // Given
            TestEntity entity = new TestEntity("Test");
            entity.setDeleted(Boolean.TRUE);

            // When
            entity.onCreate(); // Simulate @PrePersist

            // Then
            assertThat(entity.getDeleted()).isEqualTo(Boolean.TRUE);
        }
    }

    @Nested
    @DisplayName("Utility Method Tests")
    class UtilityMethodTests {

        @Test
        @DisplayName("Should return true for new entity (null ID)")
        void shouldReturnTrueForNewEntity() {
            // Given
            TestEntity entity = new TestEntity("Test");

            // When & Then
            assertThat(entity.isNew()).isTrue();
        }

        @Test
        @DisplayName("Should return false for persisted entity (non-null ID)")
        void shouldReturnFalseForPersistedEntity() {
            // Given
            TestEntity entity = new TestEntity("Test");
            entity.setId(1L);

            // When & Then
            assertThat(entity.isNew()).isFalse();
        }

        @Test
        @DisplayName("Should return false for non-deleted entity")
        void shouldReturnFalseForNonDeletedEntity() {
            // Given
            TestEntity entity = new TestEntity("Test");

            // When & Then
            assertThat(entity.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("Should return true for deleted entity")
        void shouldReturnTrueForDeletedEntity() {
            // Given
            TestEntity entity = new TestEntity("Test");
            entity.setDeleted(Boolean.TRUE);

            // When & Then
            assertThat(entity.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("Should mark entity as deleted")
        void shouldMarkEntityAsDeleted() {
            // Given
            TestEntity entity = new TestEntity("Test");
            assertThat(entity.isDeleted()).isFalse();

            // When
            entity.markAsDeleted();

            // Then
            assertThat(entity.isDeleted()).isTrue();
            assertThat(entity.getDeleted()).isEqualTo(Boolean.TRUE);
        }

        @Test
        @DisplayName("Should restore entity from deleted state")
        void shouldRestoreEntityFromDeletedState() {
            // Given
            TestEntity entity = new TestEntity("Test");
            entity.markAsDeleted();
            assertThat(entity.isDeleted()).isTrue();

            // When
            entity.restore();

            // Then
            assertThat(entity.isDeleted()).isFalse();
            assertThat(entity.getDeleted()).isEqualTo(Boolean.FALSE);
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get ID correctly")
        void shouldSetAndGetIdCorrectly() {
            // Given
            TestEntity entity = new TestEntity("Test");
            Long expectedId = 42L;

            // When
            entity.setId(expectedId);

            // Then
            assertThat(entity.getId()).isEqualTo(expectedId);
        }

        @Test
        @DisplayName("Should set and get version correctly")
        void shouldSetAndGetVersionCorrectly() {
            // Given
            TestEntity entity = new TestEntity("Test");
            Long expectedVersion = 5L;

            // When
            entity.setVersion(expectedVersion);

            // Then
            assertThat(entity.getVersion()).isEqualTo(expectedVersion);
        }

        @Test
        @DisplayName("Should set and get timestamps correctly")
        void shouldSetAndGetTimestampsCorrectly() {
            // Given
            TestEntity entity = new TestEntity("Test");
            LocalDateTime expectedCreatedAt = LocalDateTime.now().minusDays(1);
            LocalDateTime expectedUpdatedAt = LocalDateTime.now();

            // When
            entity.setCreatedAt(expectedCreatedAt);
            entity.setUpdatedAt(expectedUpdatedAt);

            // Then
            assertThat(entity.getCreatedAt()).isEqualTo(expectedCreatedAt);
            assertThat(entity.getUpdatedAt()).isEqualTo(expectedUpdatedAt);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        @DisplayName("Should set and get deleted flag correctly")
        void shouldSetAndGetDeletedFlagCorrectly(Boolean deletedValue) {
            // Given
            TestEntity entity = new TestEntity("Test");

            // When
            entity.setDeleted(deletedValue);

            // Then
            assertThat(entity.getDeleted()).isEqualTo(deletedValue);
        }

        @Test
        @DisplayName("Should handle null deleted value by setting to false")
        void shouldHandleNullDeletedValueBySettingToFalse() {
            // Given
            TestEntity entity = new TestEntity("Test");

            // When
            entity.setDeleted(null);

            // Then
            assertThat(entity.getDeleted()).isEqualTo(Boolean.FALSE);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Given
            TestEntity entity = new TestEntity("Test");
            entity.setId(1L);

            // When & Then
            assertThat(entity).isEqualTo(entity);
            assertThat(entity.hashCode()).isEqualTo(entity.hashCode());
        }

        @Test
        @DisplayName("Should be equal when IDs are same and not null")
        void shouldBeEqualWhenIdsAreSameAndNotNull() {
            // Given
            TestEntity entity1 = new TestEntity("Test1");
            TestEntity entity2 = new TestEntity("Test2");
            entity1.setId(1L);
            entity2.setId(1L);

            // When & Then
            assertThat(entity1).isEqualTo(entity2);
            assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when IDs are different")
        void shouldNotBeEqualWhenIdsAreDifferent() {
            // Given
            TestEntity entity1 = new TestEntity("Test");
            TestEntity entity2 = new TestEntity("Test");
            entity1.setId(1L);
            entity2.setId(2L);

            // When & Then
            assertThat(entity1).isNotEqualTo(entity2);
        }

        @Test
        @DisplayName("Should not be equal when one ID is null")
        void shouldNotBeEqualWhenOneIdIsNull() {
            // Given
            TestEntity entity1 = new TestEntity("Test");
            TestEntity entity2 = new TestEntity("Test");
            entity1.setId(1L);
            // entity2.setId(null); // ID remains null

            // When & Then
            assertThat(entity1).isNotEqualTo(entity2);
        }

        @Test
        @DisplayName("Should not be equal when both IDs are null")
        void shouldNotBeEqualWhenBothIdsAreNull() {
            // Given
            TestEntity entity1 = new TestEntity("Test1");
            TestEntity entity2 = new TestEntity("Test2");
            // Both IDs remain null

            // When & Then
            assertThat(entity1).isNotEqualTo(entity2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Given
            TestEntity entity = new TestEntity("Test");
            entity.setId(1L);

            // When & Then
            assertThat(entity).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different class")
        void shouldNotBeEqualToDifferentClass() {
            // Given
            TestEntity entity = new TestEntity("Test");
            entity.setId(1L);
            String differentObject = "Not an entity";

            // When & Then
            assertThat(entity).isNotEqualTo(differentObject);
        }

        @Test
        @DisplayName("Should return constant hash code for null ID")
        void shouldReturnConstantHashCodeForNullId() {
            // Given
            TestEntity entity1 = new TestEntity("Test1");
            TestEntity entity2 = new TestEntity("Test2");
            // Both IDs remain null

            // When & Then
            assertThat(entity1.hashCode()).isEqualTo(31);
            assertThat(entity2.hashCode()).isEqualTo(31);
            assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
        }

        @Test
        @DisplayName("Should return ID-based hash code for non-null ID")
        void shouldReturnIdBasedHashCodeForNonNullId() {
            // Given
            TestEntity entity = new TestEntity("Test");
            entity.setId(42L);

            // When & Then
            assertThat(entity.hashCode()).isNotEqualTo(31);
            assertThat(entity.hashCode()).isEqualTo(entity.hashCode()); // Consistent
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should generate correct toString for new entity")
        void shouldGenerateCorrectToStringForNewEntity() {
            // Given
            TestEntity entity = new TestEntity("Test");

            // When
            String result = entity.toString();

            // Then
            assertThat(result)
                .contains("TestEntity")
                .contains("id=null")
                .contains("version=null")
                .contains("createdAt=null")
                .contains("updatedAt=null")
                .contains("deleted=false");
        }

        @Test
        @DisplayName("Should generate correct toString for persisted entity")
        void shouldGenerateCorrectToStringForPersistedEntity() {
            // Given
            TestEntity entity = new TestEntity("Test");
            entity.setId(1L);
            entity.setVersion(2L);
            LocalDateTime now = LocalDateTime.now();
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            entity.setDeleted(Boolean.TRUE);

            // When
            String result = entity.toString();

            // Then
            assertThat(result)
                .contains("TestEntity")
                .contains("id=1")
                .contains("version=2")
                .contains("deleted=true")
                .contains(now.toString());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Null Handling Tests")
    class EdgeCaseTests {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should handle null and empty name gracefully")
        void shouldHandleNullAndEmptyNameGracefully(String name) {
            // When
            TestEntity entity = new TestEntity(name);

            // Then
            assertThat(entity.getName()).isEqualTo(name);
            assertThat(entity.getDeleted()).isEqualTo(Boolean.FALSE);
        }

        @Test
        @DisplayName("Should handle multiple soft delete operations")
        void shouldHandleMultipleSoftDeleteOperations() {
            // Given
            TestEntity entity = new TestEntity("Test");

            // When & Then - Multiple delete operations
            entity.markAsDeleted();
            assertThat(entity.isDeleted()).isTrue();
            
            entity.markAsDeleted(); // Should remain deleted
            assertThat(entity.isDeleted()).isTrue();
            
            entity.restore();
            assertThat(entity.isDeleted()).isFalse();
            
            entity.restore(); // Should remain not deleted
            assertThat(entity.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("Should handle version changes correctly")
        void shouldHandleVersionChangesCorrectly() {
            // Given
            TestEntity entity = new TestEntity("Test");
            
            // When & Then
            entity.setVersion(0L);
            assertThat(entity.getVersion()).isEqualTo(0L);
            
            entity.setVersion(Long.MAX_VALUE);
            assertThat(entity.getVersion()).isEqualTo(Long.MAX_VALUE);
            
            entity.setVersion(null);
            assertThat(entity.getVersion()).isNull();
        }

        @Test
        @DisplayName("Should handle extreme timestamp values")
        void shouldHandleExtremeTimestampValues() {
            // Given
            TestEntity entity = new TestEntity("Test");
            LocalDateTime minDateTime = LocalDateTime.MIN;
            LocalDateTime maxDateTime = LocalDateTime.MAX;

            // When
            entity.setCreatedAt(minDateTime);
            entity.setUpdatedAt(maxDateTime);

            // Then
            assertThat(entity.getCreatedAt()).isEqualTo(minDateTime);
            assertThat(entity.getUpdatedAt()).isEqualTo(maxDateTime);
        }
    }

    @Nested
    @DisplayName("Business Logic Integration Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should maintain audit trail through entity lifecycle")
        void shouldMaintainAuditTrailThroughEntityLifecycle() {
            // Given - New entity
            TestEntity entity = new TestEntity("Lifecycle Test");
            assertThat(entity.isNew()).isTrue();

            // When - Simulate persistence
            entity.onCreate();
            entity.setId(1L);
            entity.setVersion(0L);
            LocalDateTime createdAt = entity.getCreatedAt();
            LocalDateTime updatedAt = entity.getUpdatedAt();

            // Then - Entity is persisted
            assertThat(entity.isNew()).isFalse();
            assertThat(createdAt).isNotNull();
            assertThat(updatedAt).isNotNull().isEqualTo(createdAt);

            // When - Simulate update
            try {
                Thread.sleep(1); // Ensure different timestamp
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            entity.onUpdate();
            entity.setVersion(1L);

            // Then - Update timestamp changed, created timestamp unchanged
            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entity.getUpdatedAt()).isAfter(updatedAt);
            assertThat(entity.getVersion()).isEqualTo(1L);

            // When - Soft delete
            entity.markAsDeleted();

            // Then - Entity is soft deleted but audit trail preserved
            assertThat(entity.isDeleted()).isTrue();
            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entity.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should support entity comparison in collections")
        void shouldSupportEntityComparisonInCollections() {
            // Given
            TestEntity entity1 = new TestEntity("Entity 1");
            TestEntity entity2 = new TestEntity("Entity 2");
            TestEntity entity3 = new TestEntity("Entity 3");
            
            entity1.setId(1L);
            entity2.setId(2L);
            entity3.setId(1L); // Same ID as entity1

            // When & Then - Set operations
            java.util.Set<TestEntity> entitySet = new java.util.HashSet<>();
            entitySet.add(entity1);
            entitySet.add(entity2);
            entitySet.add(entity3); // Should not be added (same ID as entity1)

            assertThat(entitySet)
                .hasSize(2)
                .contains(entity1, entity2)
                .contains(entity3); // entity3 equals entity1

            // When & Then - List operations
            java.util.List<TestEntity> entityList = java.util.Arrays.asList(entity1, entity2, entity3);
            assertThat(entityList.indexOf(entity1)).isEqualTo(0);
            assertThat(entityList.indexOf(entity3)).isEqualTo(0); // Same as entity1
        }
    }

    @Nested
    @DisplayName("Performance and Memory Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle large number of entities efficiently")
        void shouldHandleLargeNumberOfEntitiesEfficiently() {
            // Given
            int entityCount = 1000;
            java.util.List<TestEntity> entities = new java.util.ArrayList<>();

            // When - Create many entities
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < entityCount; i++) {
                TestEntity entity = new TestEntity("Entity " + i);
                entity.setId((long) i);
                entity.onCreate();
                entities.add(entity);
            }
            long endTime = System.currentTimeMillis();

            // Then - Should complete quickly
            assertThat(entities).hasSize(entityCount);
            assertThat(endTime - startTime).isLessThan(1000); // Less than 1 second
            
            // Verify all entities are properly initialized
            assertThat(entities)
                .allSatisfy(entity -> {
                    assertThat(entity.getId()).isNotNull();
                    assertThat(entity.getCreatedAt()).isNotNull();
                    assertThat(entity.getUpdatedAt()).isNotNull();
                    assertThat(entity.getDeleted()).isEqualTo(Boolean.FALSE);
                });
        }

        @Test
        @DisplayName("Should maintain consistent hashCode across operations")
        void shouldMaintainConsistentHashCodeAcrossOperations() {
            // Given
            TestEntity entity = new TestEntity("Consistency Test");
            entity.setId(42L);
            int initialHashCode = entity.hashCode();

            // When - Perform various operations
            entity.onCreate();
            entity.onUpdate();
            entity.markAsDeleted();
            entity.restore();
            entity.setVersion(5L);
            entity.setName("Updated Name");

            // Then - HashCode should remain consistent (based on ID)
            assertThat(entity.hashCode()).isEqualTo(initialHashCode);
        }
    }

    @Nested
    @DisplayName("Serialization Tests")
    class SerializationTests {

        @Test
        @DisplayName("Should be serializable")
        void shouldBeSerializable() {
            // Given
            TestEntity entity = new TestEntity("Serialization Test");
            entity.setId(1L);
            entity.setVersion(1L);
            entity.onCreate();

            // When & Then - Should implement Serializable
            assertThat(entity).isInstanceOf(java.io.Serializable.class);
            
            // Verify serialVersionUID is defined
            try {
                java.lang.reflect.Field serialVersionUIDField = 
                    BaseEntity.class.getDeclaredField("serialVersionUID");
                assertThat(serialVersionUIDField).isNotNull();
                assertThat(java.lang.reflect.Modifier.isStatic(serialVersionUIDField.getModifiers())).isTrue();
                assertThat(java.lang.reflect.Modifier.isFinal(serialVersionUIDField.getModifiers())).isTrue();
            } catch (NoSuchFieldException e) {
                fail("serialVersionUID field should be defined");
            }
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Should handle concurrent access safely")
        void shouldHandleConcurrentAccessSafely() throws InterruptedException {
            // Given
            TestEntity entity = new TestEntity("Thread Safety Test");
            entity.setId(1L);
            int threadCount = 10;
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(threadCount);
            java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger(0);

            // When - Multiple threads access entity concurrently
            for (int i = 0; i < threadCount; i++) {
                new Thread(() -> {
                    try {
                        // Perform various operations
                        entity.onUpdate();
                        entity.markAsDeleted();
                        entity.restore();
                        entity.toString();
                        entity.hashCode();
                        successCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                }).start();
            }

            // Then - All threads should complete successfully
            latch.await(5, java.util.concurrent.TimeUnit.SECONDS);
            assertThat(successCount.get()).isEqualTo(threadCount);
            assertThat(entity.getId()).isEqualTo(1L); // ID should remain unchanged
        }
    }
}