package com.personalblog.repository;

import com.personalblog.entity.BlogPost;
import com.personalblog.entity.Category;
import com.personalblog.entity.Role;
import com.personalblog.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for CategoryRepository.
 * 
 * Tests cover:
 * - Basic CRUD operations with soft delete support
 * - Slug-based queries for SEO-friendly URLs
 * - Hierarchical category operations
 * - Category statistics and post counts
 * - Display order management
 * - SEO metadata queries
 * - Performance optimization with proper indexing
 * - Security validation and input sanitization
 * - Edge cases and error handling
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Category Repository Tests")
class CategoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category rootCategory;
    private Category childCategory;
    private Category grandChildCategory;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Create test user for blog posts
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setEmailVerified(true);
        testUser.setAccountEnabled(true);
        testUser.setAccountLocked(false);
        testUser.setRoles(Set.of(Role.AUTHOR));
        entityManager.persistAndFlush(testUser);

        // Create hierarchical category structure
        rootCategory = createCategory("Technology", "technology", "Tech-related posts", "#FF5733", 1);
        childCategory = createCategory("Programming", "programming", "Programming tutorials", "#33FF57", 2);
        grandChildCategory = createCategory("Java", "java", "Java programming", "#3357FF", 3);

        // Set up hierarchy
        childCategory.setParent(rootCategory);
        grandChildCategory.setParent(childCategory);

        entityManager.persistAndFlush(rootCategory);
        entityManager.persistAndFlush(childCategory);
        entityManager.persistAndFlush(grandChildCategory);
    }

    // ==================== Helper Methods ====================

    private Category createCategory(String name, String slug, String description, String colorCode, int displayOrder) {
        Category category = new Category();
        category.setName(name);
        category.setSlug(slug);
        category.setDescription(description);
        category.setColorCode(colorCode);
        category.setDisplayOrder(displayOrder);
        category.setMetaTitle(name + " - Meta Title");
        category.setMetaDescription(description + " - Meta Description");
        category.setMetaKeywords(name.toLowerCase() + ", " + slug);
        return category;
    }

    private BlogPost createBlogPost(String title, String slug, Category category) {
        BlogPost post = new BlogPost();
        post.setTitle(title);
        post.setSlug(slug);
        post.setContent("Test content for " + title);
        post.setExcerpt("Test excerpt");
        post.setStatus(BlogPost.Status.PUBLISHED);
        post.setAuthor(testUser);
        post.setCategory(category);
        post.setPublishedDate(LocalDateTime.now());
        return entityManager.persistAndFlush(post);
    }

    // ==================== Basic CRUD Tests ====================

    @Test
    @DisplayName("Should find category by slug")
    void shouldFindCategoryBySlug() {
        // When
        Optional<Category> found = categoryRepository.findBySlug("technology");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Technology");
        assertThat(found.get().getSlug()).isEqualTo("technology");
    }

    @Test
    @DisplayName("Should return empty when category slug not found")
    void shouldReturnEmptyWhenSlugNotFound() {
        // When
        Optional<Category> found = categoryRepository.findBySlug("nonexistent");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should check if slug exists")
    void shouldCheckIfSlugExists() {
        // When & Then
        assertThat(categoryRepository.existsBySlug("technology")).isTrue();
        assertThat(categoryRepository.existsBySlug("nonexistent")).isFalse();
    }

    @Test
    @DisplayName("Should check slug existence excluding specific category")
    void shouldCheckSlugExistenceExcludingCategory() {
        // When & Then
        assertThat(categoryRepository.existsBySlugAndIdNot("technology", rootCategory.getId())).isFalse();
        assertThat(categoryRepository.existsBySlugAndIdNot("technology", childCategory.getId())).isTrue();
    }

    // ==================== Hierarchical Tests ====================

    @Test
    @DisplayName("Should find root categories")
    void shouldFindRootCategories() {
        // When
        List<Category> rootCategories = categoryRepository.findRootCategories();

        // Then
        assertThat(rootCategories).hasSize(1);
        assertThat(rootCategories.get(0).getName()).isEqualTo("Technology");
        assertThat(rootCategories.get(0).getParent()).isNull();
    }

    @Test
    @DisplayName("Should find root categories with pagination")
    void shouldFindRootCategoriesWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("displayOrder"));

        // When
        Page<Category> rootCategories = categoryRepository.findRootCategories(pageable);

        // Then
        assertThat(rootCategories.getContent()).hasSize(1);
        assertThat(rootCategories.getContent().get(0).getName()).isEqualTo("Technology");
    }

    @Test
    @DisplayName("Should find children by parent ID")
    void shouldFindChildrenByParentId() {
        // When
        List<Category> children = categoryRepository.findByParentId(rootCategory.getId());

        // Then
        assertThat(children).hasSize(1);
        assertThat(children.get(0).getName()).isEqualTo("Programming");
        assertThat(children.get(0).getParent().getId()).isEqualTo(rootCategory.getId());
    }

    @Test
    @DisplayName("Should find children by parent ID with pagination")
    void shouldFindChildrenByParentIdWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("displayOrder"));

        // When
        Page<Category> children = categoryRepository.findByParentId(rootCategory.getId(), pageable);

        // Then
        assertThat(children.getContent()).hasSize(1);
        assertThat(children.getContent().get(0).getName()).isEqualTo("Programming");
    }

    @Test
    @DisplayName("Should find category hierarchy")
    void shouldFindCategoryHierarchy() {
        // When
        List<Category> hierarchy = categoryRepository.findCategoryHierarchy(grandChildCategory.getId());

        // Then
        assertThat(hierarchy).hasSize(3);
        // Should be ordered from root to leaf
        assertThat(hierarchy.get(0).getName()).isEqualTo("Technology");
        assertThat(hierarchy.get(1).getName()).isEqualTo("Programming");
        assertThat(hierarchy.get(2).getName()).isEqualTo("Java");
    }

    @Test
    @DisplayName("Should find all descendants of category")
    void shouldFindAllDescendants() {
        // When
        List<Category> descendants = categoryRepository.findAllDescendants(rootCategory.getId());

        // Then
        assertThat(descendants).hasSize(2);
        assertThat(descendants).extracting(Category::getName)
                .containsExactlyInAnyOrder("Programming", "Java");
    }

    @Test
    @DisplayName("Should check if category has children")
    void shouldCheckIfCategoryHasChildren() {
        // When & Then
        assertThat(categoryRepository.hasChildren(rootCategory.getId())).isTrue();
        assertThat(categoryRepository.hasChildren(grandChildCategory.getId())).isFalse();
    }

    @Test
    @DisplayName("Should count children of category")
    void shouldCountChildren() {
        // When & Then
        assertThat(categoryRepository.countChildren(rootCategory.getId())).isEqualTo(1);
        assertThat(categoryRepository.countChildren(grandChildCategory.getId())).isEqualTo(0);
    }

    // ==================== Display Order Tests ====================

    @Test
    @DisplayName("Should find categories ordered by display order")
    void shouldFindCategoriesOrderedByDisplayOrder() {
        // When
        List<Category> categories = categoryRepository.findAllByOrderByDisplayOrderAsc();

        // Then
        assertThat(categories).hasSize(3);
        assertThat(categories.get(0).getDisplayOrder()).isEqualTo(1);
        assertThat(categories.get(1).getDisplayOrder()).isEqualTo(2);
        assertThat(categories.get(2).getDisplayOrder()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should find categories by display order range")
    void shouldFindCategoriesByDisplayOrderRange() {
        // When
        List<Category> categories = categoryRepository.findByDisplayOrderBetween(1, 2);

        // Then
        assertThat(categories).hasSize(2);
        assertThat(categories).extracting(Category::getName)
                .containsExactlyInAnyOrder("Technology", "Programming");
    }

    @Test
    @DisplayName("Should find next display order")
    void shouldFindNextDisplayOrder() {
        // When
        Integer nextOrder = categoryRepository.findNextDisplayOrder();

        // Then
        assertThat(nextOrder).isEqualTo(4);
    }

    @Test
    @DisplayName("Should find max display order for parent")
    void shouldFindMaxDisplayOrderForParent() {
        // When
        Integer maxOrder = categoryRepository.findMaxDisplayOrderByParent(rootCategory.getId());

        // Then
        assertThat(maxOrder).isEqualTo(2);
    }

    // ==================== Blog Post Statistics Tests ====================

    @Test
    @DisplayName("Should find categories with post counts")
    void shouldFindCategoriesWithPostCounts() {
        // Given
        createBlogPost("Tech Post 1", "tech-post-1", rootCategory);
        createBlogPost("Tech Post 2", "tech-post-2", rootCategory);
        createBlogPost("Programming Post", "programming-post", childCategory);

        // When
        List<Object[]> categoriesWithCounts = categoryRepository.findCategoriesWithPostCounts();

        // Then
        assertThat(categoriesWithCounts).hasSize(3);
        
        // Find the root category result
        Object[] rootResult = categoriesWithCounts.stream()
                .filter(result -> ((Category) result[0]).getName().equals("Technology"))
                .findFirst()
                .orElseThrow();
        
        assertThat(rootResult[1]).isEqualTo(2L); // Post count
    }

    @Test
    @DisplayName("Should find categories with published post counts")
    void shouldFindCategoriesWithPublishedPostCounts() {
        // Given
        createBlogPost("Published Post", "published-post", rootCategory);
        
        BlogPost draftPost = new BlogPost();
        draftPost.setTitle("Draft Post");
        draftPost.setSlug("draft-post");
        draftPost.setContent("Draft content");
        draftPost.setStatus(BlogPost.Status.DRAFT);
        draftPost.setAuthor(testUser);
        draftPost.setCategory(rootCategory);
        entityManager.persistAndFlush(draftPost);

        // When
        List<Object[]> categoriesWithCounts = categoryRepository.findCategoriesWithPublishedPostCounts();

        // Then
        Object[] rootResult = categoriesWithCounts.stream()
                .filter(result -> ((Category) result[0]).getName().equals("Technology"))
                .findFirst()
                .orElseThrow();
        
        assertThat(rootResult[1]).isEqualTo(1L); // Only published posts counted
    }

    @Test
    @DisplayName("Should count posts in category")
    void shouldCountPostsInCategory() {
        // Given
        createBlogPost("Post 1", "post-1", rootCategory);
        createBlogPost("Post 2", "post-2", rootCategory);

        // When
        long postCount = categoryRepository.countPostsInCategory(rootCategory.getId());

        // Then
        assertThat(postCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Should count published posts in category")
    void shouldCountPublishedPostsInCategory() {
        // Given
        createBlogPost("Published Post", "published-post", rootCategory);
        
        BlogPost draftPost = new BlogPost();
        draftPost.setTitle("Draft Post");
        draftPost.setSlug("draft-post");
        draftPost.setContent("Draft content");
        draftPost.setStatus(BlogPost.Status.DRAFT);
        draftPost.setAuthor(testUser);
        draftPost.setCategory(rootCategory);
        entityManager.persistAndFlush(draftPost);

        // When
        long publishedCount = categoryRepository.countPublishedPostsInCategory(rootCategory.getId());

        // Then
        assertThat(publishedCount).isEqualTo(1);
    }

    // ==================== Popular Categories Tests ====================

    @Test
    @DisplayName("Should find popular categories by post count")
    void shouldFindPopularCategoriesByPostCount() {
        // Given
        createBlogPost("Tech Post 1", "tech-post-1", rootCategory);
        createBlogPost("Tech Post 2", "tech-post-2", rootCategory);
        createBlogPost("Programming Post", "programming-post", childCategory);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Category> popularCategories = categoryRepository.findPopularCategoriesByPostCount(pageable);

        // Then
        assertThat(popularCategories.getContent()).hasSize(2); // Only categories with posts
        assertThat(popularCategories.getContent().get(0).getName()).isEqualTo("Technology"); // Most posts first
    }

    @Test
    @DisplayName("Should find categories used in date range")
    void shouldFindCategoriesUsedInDateRange() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        
        createBlogPost("Recent Post", "recent-post", rootCategory);

        // When
        List<Category> categoriesInRange = categoryRepository.findCategoriesUsedInDateRange(startDate, endDate);

        // Then
        assertThat(categoriesInRange).hasSize(1);
        assertThat(categoriesInRange.get(0).getName()).isEqualTo("Technology");
    }

    // ==================== Search Tests ====================

    @Test
    @DisplayName("Should search categories by name")
    void shouldSearchCategoriesByName() {
        // When
        List<Category> results = categoryRepository.searchByName("tech");

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Technology");
    }

    @Test
    @DisplayName("Should search categories by name with pagination")
    void shouldSearchCategoriesByNameWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Category> results = categoryRepository.searchByName("prog", pageable);

        // Then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getName()).isEqualTo("Programming");
    }

    @Test
    @DisplayName("Should search categories by name or description")
    void shouldSearchCategoriesByNameOrDescription() {
        // When
        List<Category> results = categoryRepository.searchByNameOrDescription("tutorials");

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Programming");
    }

    // ==================== SEO Metadata Tests ====================

    @Test
    @DisplayName("Should find categories missing SEO metadata")
    void shouldFindCategoriesMissingSeoMetadata() {
        // Given
        Category categoryWithoutSeo = createCategory("No SEO", "no-seo", "Description", "#FFFFFF", 10);
        categoryWithoutSeo.setMetaTitle(null);
        categoryWithoutSeo.setMetaDescription(null);
        entityManager.persistAndFlush(categoryWithoutSeo);

        // When
        List<Category> categoriesNeedingSeo = categoryRepository.findCategoriesNeedingSeoOptimization();

        // Then
        assertThat(categoriesNeedingSeo).hasSize(1);
        assertThat(categoriesNeedingSeo.get(0).getName()).isEqualTo("No SEO");
    }

    // ==================== Bulk Operations Tests ====================

    @Test
    @DisplayName("Should update display order for categories")
    void shouldUpdateDisplayOrderForCategories() {
        // When
        int updatedCount = categoryRepository.updateDisplayOrder(rootCategory.getId(), 100);

        // Then
        assertThat(updatedCount).isEqualTo(1);
        
        entityManager.clear();
        Category updated = categoryRepository.findById(rootCategory.getId()).orElseThrow();
        assertThat(updated.getDisplayOrder()).isEqualTo(100);
    }

    @Test
    @DisplayName("Should reorder categories by parent")
    void shouldReorderCategoriesByParent() {
        // Given
        Category anotherChild = createCategory("Another Child", "another-child", "Description", "#FFFFFF", 5);
        anotherChild.setParent(rootCategory);
        entityManager.persistAndFlush(anotherChild);

        // When
        int updatedCount = categoryRepository.reorderCategoriesByParent(rootCategory.getId());

        // Then
        assertThat(updatedCount).isEqualTo(2); // Two children reordered
    }

    // ==================== Soft Delete Tests ====================

    @Test
    @DisplayName("Should not find soft deleted categories")
    void shouldNotFindSoftDeletedCategories() {
        // Given
        categoryRepository.softDeleteById(rootCategory.getId());
        entityManager.clear();

        // When
        Optional<Category> found = categoryRepository.findBySlug("technology");
        List<Category> allActive = categoryRepository.findAllActive();

        // Then
        assertThat(found).isEmpty();
        assertThat(allActive).doesNotContain(rootCategory);
    }

    @Test
    @DisplayName("Should cascade soft delete to children")
    void shouldCascadeSoftDeleteToChildren() {
        // When
        int deletedCount = categoryRepository.softDeleteCategoryAndChildren(rootCategory.getId());

        // Then
        assertThat(deletedCount).isEqualTo(3); // Root + child + grandchild
        
        entityManager.clear();
        assertThat(categoryRepository.findBySlug("technology")).isEmpty();
        assertThat(categoryRepository.findBySlug("programming")).isEmpty();
        assertThat(categoryRepository.findBySlug("java")).isEmpty();
    }

    // ==================== Validation Tests ====================

    @Test
    @DisplayName("Should validate slug format")
    void shouldValidateSlugFormat() {
        // Given
        Category invalidCategory = new Category();
        invalidCategory.setName("Invalid Category");
        invalidCategory.setSlug("Invalid Slug!"); // Contains invalid characters

        // When & Then
        assertThatThrownBy(() -> entityManager.persistAndFlush(invalidCategory))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should validate color code format")
    void shouldValidateColorCodeFormat() {
        // Given
        Category invalidCategory = new Category();
        invalidCategory.setName("Invalid Category");
        invalidCategory.setSlug("invalid-category");
        invalidCategory.setColorCode("invalid-color"); // Invalid hex format

        // When & Then
        assertThatThrownBy(() -> entityManager.persistAndFlush(invalidCategory))
                .isInstanceOf(Exception.class);
    }

    // ==================== Performance Tests ====================

    @Test
    @DisplayName("Should handle large number of categories efficiently")
    void shouldHandleLargeNumberOfCategoriesEfficiently() {
        // Given - Create many categories
        for (int i = 0; i < 100; i++) {
            Category category = createCategory("Category " + i, "category-" + i, "Description " + i, "#FFFFFF", i);
            entityManager.persist(category);
        }
        entityManager.flush();
        entityManager.clear();

        // When
        long startTime = System.currentTimeMillis();
        Page<Category> categories = categoryRepository.findAllActive(PageRequest.of(0, 20));
        long endTime = System.currentTimeMillis();

        // Then
        assertThat(categories.getContent()).hasSize(20);
        assertThat(endTime - startTime).isLessThan(1000); // Should complete within 1 second
    }

    // ==================== Edge Cases Tests ====================

    @Test
    @DisplayName("Should handle null and empty parameters gracefully")
    void shouldHandleNullAndEmptyParametersGracefully() {
        // When & Then
        assertThat(categoryRepository.findBySlug("")).isEmpty();
        assertThat(categoryRepository.searchByName("")).isEmpty();
        assertThat(categoryRepository.findByParentId(999L)).isEmpty();
    }

    @Test
    @DisplayName("Should handle circular reference prevention")
    void shouldHandleCircularReferencePrevention() {
        // Given
        Category parent = createCategory("Parent", "parent", "Parent category", "#FFFFFF", 1);
        Category child = createCategory("Child", "child", "Child category", "#FFFFFF", 2);
        
        entityManager.persistAndFlush(parent);
        entityManager.persistAndFlush(child);
        
        child.setParent(parent);
        entityManager.persistAndFlush(child);

        // When & Then - Attempting to make parent a child of child should fail
        assertThatThrownBy(() -> {
            parent.setParent(child);
            entityManager.persistAndFlush(parent);
        }).isInstanceOf(Exception.class);
    }
}