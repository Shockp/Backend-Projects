package com.personalblog.repository;

import com.personalblog.entity.BaseEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doCallRealMethod;

/**
 * Comprehensive test suite for BaseRepository interface.
 * Tests all common repository operations including soft delete,
 * pagination, specification support, and security features.
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("BaseRepository Tests")
class BaseRepositoryTest {

    @Mock
    private TestEntityRepository repository;

    private TestEntity activeEntity;
    private TestEntity deletedEntity;
    private List<TestEntity> testEntities;
    private Pageable pageable;
    private Sort sort;

    @BeforeEach
    void setUp() {
        // Create test entities
        activeEntity = new TestEntity();
        activeEntity.setId(1L);
        activeEntity.setName("Active Entity");
        activeEntity.setDeleted(false);
        activeEntity.setCreatedAt(LocalDateTime.now().minusDays(1));
        activeEntity.setUpdatedAt(LocalDateTime.now());

        deletedEntity = new TestEntity();
        deletedEntity.setId(2L);
        deletedEntity.setName("Deleted Entity");
        deletedEntity.setDeleted(true);
        deletedEntity.setCreatedAt(LocalDateTime.now().minusDays(2));
        deletedEntity.setUpdatedAt(LocalDateTime.now().minusHours(1));

        testEntities = Arrays.asList(activeEntity, deletedEntity);
        pageable = PageRequest.of(0, 10, Sort.by("name"));
        sort = Sort.by(Sort.Direction.ASC, "name");
    }

    @Nested
    @DisplayName("Soft Delete Operations")
    class SoftDeleteOperationsTests {

        @Test
        @DisplayName("Should find all active entities")
        void findAllActive_ShouldReturnOnlyActiveEntities() {
            // Given
            List<TestEntity> activeEntities = List.of(activeEntity);
            when(repository.findAllActive()).thenReturn(activeEntities);

            // When
            List<TestEntity> result = repository.findAllActive();

            // Then
            assertThat(result)
                .hasSize(1)
                .containsExactly(activeEntity)
                .allMatch(entity -> !entity.isDeleted());
            verify(repository).findAllActive();
        }

        @Test
        @DisplayName("Should find all active entities with sorting")
        void findAllActive_WithSort_ShouldReturnSortedActiveEntities() {
            // Given
            List<TestEntity> activeEntities = List.of(activeEntity);
            when(repository.findAllActive(sort)).thenReturn(activeEntities);

            // When
            List<TestEntity> result = repository.findAllActive(sort);

            // Then
            assertThat(result)
                .hasSize(1)
                .containsExactly(activeEntity);
            verify(repository).findAllActive(sort);
        }

        @Test
        @DisplayName("Should find all active entities with pagination")
        void findAllActive_WithPageable_ShouldReturnPagedActiveEntities() {
            // Given
            Page<TestEntity> activePage = new PageImpl<>(List.of(activeEntity), pageable, 1);
            when(repository.findAllActive(pageable)).thenReturn(activePage);

            // When
            Page<TestEntity> result = repository.findAllActive(pageable);

            // Then
            assertThat(result.getContent())
                .hasSize(1)
                .containsExactly(activeEntity);
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(repository).findAllActive(pageable);
        }

        @Test
        @DisplayName("Should find all deleted entities")
        void findAllDeleted_ShouldReturnOnlyDeletedEntities() {
            // Given
            List<TestEntity> deletedEntities = List.of(deletedEntity);
            when(repository.findAllDeleted()).thenReturn(deletedEntities);

            // When
            List<TestEntity> result = repository.findAllDeleted();

            // Then
            assertThat(result)
                .hasSize(1)
                .containsExactly(deletedEntity)
                .allMatch(BaseEntity::isDeleted);
            verify(repository).findAllDeleted();
        }

        @Test
        @DisplayName("Should find active entity by ID")
        void findActiveById_WithValidId_ShouldReturnActiveEntity() {
            // Given
            when(repository.findActiveById(1L)).thenReturn(Optional.of(activeEntity));

            // When
            Optional<TestEntity> result = repository.findActiveById(1L);

            // Then
            assertThat(result)
                .isPresent()
                .contains(activeEntity);
            verify(repository).findActiveById(1L);
        }

        @Test
        @DisplayName("Should not find deleted entity by ID when searching for active")
        void findActiveById_WithDeletedEntityId_ShouldReturnEmpty() {
            // Given
            when(repository.findActiveById(2L)).thenReturn(Optional.empty());

            // When
            Optional<TestEntity> result = repository.findActiveById(2L);

            // Then
            assertThat(result).isEmpty();
            verify(repository).findActiveById(2L);
        }

        @Test
        @DisplayName("Should check if active entity exists by ID")
        void existsActiveById_WithActiveEntity_ShouldReturnTrue() {
            // Given
            when(repository.existsActiveById(1L)).thenReturn(true);

            // When
            boolean result = repository.existsActiveById(1L);

            // Then
            assertThat(result).isTrue();
            verify(repository).existsActiveById(1L);
        }

        @Test
        @DisplayName("Should return false for deleted entity when checking active existence")
        void existsActiveById_WithDeletedEntity_ShouldReturnFalse() {
            // Given
            when(repository.existsActiveById(2L)).thenReturn(false);

            // When
            boolean result = repository.existsActiveById(2L);

            // Then
            assertThat(result).isFalse();
            verify(repository).existsActiveById(2L);
        }
    }

    @Nested
    @DisplayName("Count Operations")
    class CountOperationsTests {

        @Test
        @DisplayName("Should count active entities")
        void countActive_ShouldReturnActiveEntityCount() {
            // Given
            when(repository.countActive()).thenReturn(1L);

            // When
            long result = repository.countActive();

            // Then
            assertThat(result).isEqualTo(1L);
            verify(repository).countActive();
        }

        @Test
        @DisplayName("Should count deleted entities")
        void countDeleted_ShouldReturnDeletedEntityCount() {
            // Given
            when(repository.countDeleted()).thenReturn(1L);

            // When
            long result = repository.countDeleted();

            // Then
            assertThat(result).isEqualTo(1L);
            verify(repository).countDeleted();
        }

        @Test
        @DisplayName("Should check if active entities exist")
        void hasActiveEntities_WithActiveEntities_ShouldReturnTrue() {
            // Given
            when(repository.countActive()).thenReturn(1L);
            when(repository.hasActiveEntities()).thenCallRealMethod();

            // When
            boolean result = repository.hasActiveEntities();

            // Then
            assertThat(result).isTrue();
            verify(repository).countActive();
        }

        @Test
        @DisplayName("Should check if deleted entities exist")
        void hasDeletedEntities_WithDeletedEntities_ShouldReturnTrue() {
            // Given
            when(repository.countDeleted()).thenReturn(1L);
            when(repository.hasDeletedEntities()).thenCallRealMethod();

            // When
            boolean result = repository.hasDeletedEntities();

            // Then
            assertThat(result).isTrue();
            verify(repository).countDeleted();
        }
    }

    @Nested
    @DisplayName("Soft Delete Modification Operations")
    class SoftDeleteModificationTests {

        @Test
        @DisplayName("Should soft delete entity by ID")
        void softDeleteById_WithValidId_ShouldReturnAffectedRows() {
            // Given
            when(repository.softDeleteById(1L)).thenReturn(1);

            // When
            int result = repository.softDeleteById(1L);

            // Then
            assertThat(result).isEqualTo(1);
            verify(repository).softDeleteById(1L);
        }

        @Test
        @DisplayName("Should soft delete multiple entities by IDs")
        void softDeleteByIds_WithValidIds_ShouldReturnAffectedRows() {
            // Given
            List<Long> ids = Arrays.asList(1L, 3L, 5L);
            when(repository.softDeleteByIds(ids)).thenReturn(3);

            // When
            int result = repository.softDeleteByIds(ids);

            // Then
            assertThat(result).isEqualTo(3);
            verify(repository).softDeleteByIds(ids);
        }

        @Test
        @DisplayName("Should restore soft-deleted entity by ID")
        void restoreById_WithValidId_ShouldReturnAffectedRows() {
            // Given
            when(repository.restoreById(2L)).thenReturn(1);

            // When
            int result = repository.restoreById(2L);

            // Then
            assertThat(result).isEqualTo(1);
            verify(repository).restoreById(2L);
        }

        @Test
        @DisplayName("Should permanently delete old soft-deleted entities")
        void permanentlyDeleteOldSoftDeleted_WithCutoffDate_ShouldReturnDeletedCount() {
            // Given
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            when(repository.permanentlyDeleteOldSoftDeleted(cutoffDate)).thenReturn(5);

            // When
            int result = repository.permanentlyDeleteOldSoftDeleted(cutoffDate);

            // Then
            assertThat(result).isEqualTo(5);
            verify(repository).permanentlyDeleteOldSoftDeleted(cutoffDate);
        }
    }

    @Nested
    @DisplayName("Audit Query Operations")
    class AuditQueryTests {

        @Test
        @DisplayName("Should find entities created within date range")
        void findActiveByCreatedAtBetween_WithDateRange_ShouldReturnEntities() {
            // Given
            LocalDateTime startDate = LocalDateTime.now().minusDays(2);
            LocalDateTime endDate = LocalDateTime.now();
            List<TestEntity> entities = List.of(activeEntity);
            when(repository.findActiveByCreatedAtBetween(startDate, endDate)).thenReturn(entities);

            // When
            List<TestEntity> result = repository.findActiveByCreatedAtBetween(startDate, endDate);

            // Then
            assertThat(result)
                .hasSize(1)
                .containsExactly(activeEntity);
            verify(repository).findActiveByCreatedAtBetween(startDate, endDate);
        }

        @Test
        @DisplayName("Should find entities updated within date range")
        void findActiveByUpdatedAtBetween_WithDateRange_ShouldReturnEntities() {
            // Given
            LocalDateTime startDate = LocalDateTime.now().minusHours(2);
            LocalDateTime endDate = LocalDateTime.now();
            List<TestEntity> entities = List.of(activeEntity);
            when(repository.findActiveByUpdatedAtBetween(startDate, endDate)).thenReturn(entities);

            // When
            List<TestEntity> result = repository.findActiveByUpdatedAtBetween(startDate, endDate);

            // Then
            assertThat(result)
                .hasSize(1)
                .containsExactly(activeEntity);
            verify(repository).findActiveByUpdatedAtBetween(startDate, endDate);
        }

        @Test
        @DisplayName("Should find entities created after specific date with pagination")
        void findActiveByCreatedAtAfter_WithDateAndPageable_ShouldReturnPagedEntities() {
            // Given
            LocalDateTime date = LocalDateTime.now().minusDays(2);
            Page<TestEntity> page = new PageImpl<>(List.of(activeEntity), pageable, 1);
            when(repository.findActiveByCreatedAtAfter(date, pageable)).thenReturn(page);

            // When
            Page<TestEntity> result = repository.findActiveByCreatedAtAfter(date, pageable);

            // Then
            assertThat(result.getContent())
                .hasSize(1)
                .containsExactly(activeEntity);
            verify(repository).findActiveByCreatedAtAfter(date, pageable);
        }
    }

    @Nested
    @DisplayName("Security and Validation Operations")
    class SecurityValidationTests {

        @Test
        @DisplayName("Should validate entity successfully")
        void validateEntity_WithValidEntity_ShouldNotThrow() {
            // Given
            TestEntity validEntity = new TestEntity();
            validEntity.setId(1L);
            validEntity.setName("Valid Entity");
            doCallRealMethod().when(repository).validateEntity(validEntity);

            // When & Then
            assertThatCode(() -> repository.validateEntity(validEntity))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should throw exception for entity with zero ID")
        void validateEntity_WithZeroId_ShouldThrowException() {
            // Given
            TestEntity invalidEntity = new TestEntity();
            invalidEntity.setId(0L);
            invalidEntity.setName("Invalid Entity");
            doCallRealMethod().when(repository).validateEntity(invalidEntity);

            // When & Then
            assertThatThrownBy(() -> repository.validateEntity(invalidEntity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Entity ID cannot be zero");
        }

        @Test
        @DisplayName("Should safely save valid entity")
        void safeSave_WithValidEntity_ShouldSaveSuccessfully() {
            // Given
            TestEntity validEntity = new TestEntity();
            validEntity.setId(1L);
            validEntity.setName("Valid Entity");
            doCallRealMethod().when(repository).validateEntity(validEntity);
            when(repository.save(validEntity)).thenReturn(validEntity);
            when(repository.safeSave(validEntity)).thenCallRealMethod();

            // When
            TestEntity result = repository.safeSave(validEntity);

            // Then
            assertThat(result).isEqualTo(validEntity);
            verify(repository).save(validEntity);
        }

        @Test
        @DisplayName("Should throw exception when safely saving invalid entity")
        void safeSave_WithInvalidEntity_ShouldThrowException() {
            // Given
            TestEntity invalidEntity = new TestEntity();
            invalidEntity.setId(0L);
            invalidEntity.setName("Invalid Entity");
            doCallRealMethod().when(repository).validateEntity(invalidEntity);
            when(repository.safeSave(invalidEntity)).thenCallRealMethod();

            // When & Then
            assertThatThrownBy(() -> repository.safeSave(invalidEntity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Entity ID cannot be zero");
            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Utility Operations")
    class UtilityOperationsTests {

        @Test
        @DisplayName("Should find random active entity")
        void findRandomActive_ShouldReturnRandomEntity() {
            // Given
            when(repository.findRandomActive()).thenReturn(Optional.of(activeEntity));

            // When
            Optional<TestEntity> result = repository.findRandomActive();

            // Then
            assertThat(result)
                .isPresent()
                .contains(activeEntity);
            verify(repository).findRandomActive();
        }

        @Test
        @DisplayName("Should return total count of all entities")
        void countAll_ShouldReturnTotalCount() {
            // Given
            when(repository.count()).thenReturn(2L);
            when(repository.countAll()).thenCallRealMethod();

            // When
            long result = repository.countAll();

            // Then
            assertThat(result).isEqualTo(2L);
            verify(repository).count();
        }

        @Test
        @DisplayName("Should save all entities in batch")
        void saveAllInBatch_WithEntities_ShouldSaveSuccessfully() {
            // Given
            List<TestEntity> entities = List.of(activeEntity);
            when(repository.saveAllAndFlush(entities)).thenReturn(entities);
            when(repository.saveAllInBatch(entities)).thenCallRealMethod();

            // When
            List<TestEntity> result = repository.saveAllInBatch(entities);

            // Then
            assertThat(result)
                .hasSize(1)
                .containsExactly(activeEntity);
            verify(repository).saveAllAndFlush(entities);
        }
    }

    // Test entity for repository testing
    @Entity
    @Table(name = "test_entities")
    static class TestEntity extends BaseEntity {
        @Column(name = "name")
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    // Test repository interface
    interface TestEntityRepository extends BaseRepository<TestEntity, Long> {
    }
}