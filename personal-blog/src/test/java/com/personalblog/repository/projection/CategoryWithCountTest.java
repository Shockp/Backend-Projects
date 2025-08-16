package com.personalblog.repository.projection;

import com.personalblog.entity.BlogPost;
import com.personalblog.entity.Category;
import com.personalblog.entity.User;
import com.personalblog.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CategoryWithCount projection interface.
 * Tests the projection methods in CategoryRepository to ensure
 * proper data mapping and count calculations.
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CategoryWithCount Projection Tests")
class CategoryWithCountTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CategoryRepository categoryRepository;

    private User testAuthor;
    private Category rootCategory;
    private Category childCategory;
    private Category emptyCategory;

    @BeforeEach
    void setUp() {
        // Create test author
        testAuthor = new User();
        testAuthor.setUsername("testauthor");
        // User entity doesn't have displayName field, using username for display
        testAuthor.setEmail("test@example.com");
        testAuthor.setPassword("password");
        testAuthor = entityManager.persistAndFlush(testAuthor);

        // Create root category
        rootCategory = new Category();
        rootCategory.setName("Root Category");
        rootCategory.setSlug("root-category");
        rootCategory.setDescription("Root category description");
        rootCategory.setDisplayOrder(1);
        rootCategory.setMetaTitle("Root Category Meta Title");
        rootCategory.setMetaDescription("Root category meta description");
        rootCategory = entityManager.persistAndFlush(rootCategory);

        // Create child category
        childCategory = new Category();
        childCategory.setName("Child Category");
        childCategory.setSlug("child-category");
        childCategory.setDescription("Child category description");
        childCategory.setDisplayOrder(1);
        childCategory.setParent(rootCategory);
        childCategory = entityManager.persistAndFlush(childCategory);

        // Create empty category
        emptyCategory = new Category();
        emptyCategory.setName("Empty Category");
        emptyCategory.setSlug("empty-category");
        emptyCategory.setDescription("Empty category description");
        emptyCategory.setDisplayOrder(2);
        emptyCategory = entityManager.persistAndFlush(emptyCategory);

        // Create published posts in root category
        for (int i = 1; i <= 3; i++) {
            BlogPost post = new BlogPost();
            post.setTitle("Root Post " + i);
            post.setSlug("root-post-" + i);
            post.setContent("Content " + i);
            post.setExcerpt("Excerpt " + i);
            post.setStatus(BlogPost.Status.PUBLISHED);
            post.setAuthor(testAuthor);
            post.setCategory(rootCategory);
            post.setPublishedDate(LocalDateTime.now().minusDays(i));
            entityManager.persistAndFlush(post);
        }

        // Create draft posts in root category
        for (int i = 1; i <= 2; i++) {
            BlogPost post = new BlogPost();
            post.setTitle("Root Draft " + i);
            post.setSlug("root-draft-" + i);
            post.setContent("Draft Content " + i);
            post.setExcerpt("Draft Excerpt " + i);
            post.setStatus(BlogPost.Status.DRAFT);
            post.setAuthor(testAuthor);
            post.setCategory(rootCategory);
            entityManager.persistAndFlush(post);
        }

        // Create published post in child category
        BlogPost childPost = new BlogPost();
        childPost.setTitle("Child Post");
        childPost.setSlug("child-post");
        childPost.setContent("Child Content");
        childPost.setExcerpt("Child Excerpt");
        childPost.setStatus(BlogPost.Status.PUBLISHED);
        childPost.setAuthor(testAuthor);
        childPost.setCategory(childCategory);
        childPost.setPublishedDate(LocalDateTime.now().minusDays(1));
        entityManager.persistAndFlush(childPost);

        entityManager.clear();
    }

    @Test
    @DisplayName("Should find all categories with correct post counts")
    void shouldFindAllCategoriesWithCorrectPostCounts() {
        // When
        List<CategoryWithCount> categories = categoryRepository.findCategoriesWithCounts();

        // Then
        assertThat(categories).isNotNull();
        assertThat(categories).hasSize(3);

        // Find root category
        CategoryWithCount rootCategoryWithCount = categories.stream()
                .filter(c -> "Root Category".equals(c.getName()))
                .findFirst()
                .orElseThrow();

        assertThat(rootCategoryWithCount.getId()).isEqualTo(rootCategory.getId());
        assertThat(rootCategoryWithCount.getName()).isEqualTo("Root Category");
        assertThat(rootCategoryWithCount.getSlug()).isEqualTo("root-category");
        assertThat(rootCategoryWithCount.getDescription()).isEqualTo("Root category description");
        assertThat(rootCategoryWithCount.getDisplayOrder()).isEqualTo(1);
        assertThat(rootCategoryWithCount.getParentId()).isNull();
        assertThat(rootCategoryWithCount.getParentName()).isNull();
        assertThat(rootCategoryWithCount.getTotalPostCount()).isEqualTo(5L); // 3 published + 2 draft
        assertThat(rootCategoryWithCount.getPublishedPostCount()).isEqualTo(3L);
        assertThat(rootCategoryWithCount.getMetaTitle()).isEqualTo("Root Category Meta Title");
        assertThat(rootCategoryWithCount.getMetaDescription()).isEqualTo("Root category meta description");
        assertThat(rootCategoryWithCount.getCreatedAt()).isNotNull();
        assertThat(rootCategoryWithCount.getUpdatedAt()).isNotNull();

        // Find child category
        CategoryWithCount childCategoryWithCount = categories.stream()
                .filter(c -> "Child Category".equals(c.getName()))
                .findFirst()
                .orElseThrow();

        assertThat(childCategoryWithCount.getId()).isEqualTo(childCategory.getId());
        assertThat(childCategoryWithCount.getName()).isEqualTo("Child Category");
        assertThat(childCategoryWithCount.getParentId()).isEqualTo(rootCategory.getId());
        assertThat(childCategoryWithCount.getParentName()).isEqualTo("Root Category");
        assertThat(childCategoryWithCount.getTotalPostCount()).isEqualTo(1L);
        assertThat(childCategoryWithCount.getPublishedPostCount()).isEqualTo(1L);

        // Find empty category
        CategoryWithCount emptyCategoryWithCount = categories.stream()
                .filter(c -> "Empty Category".equals(c.getName()))
                .findFirst()
                .orElseThrow();

        assertThat(emptyCategoryWithCount.getId()).isEqualTo(emptyCategory.getId());
        assertThat(emptyCategoryWithCount.getName()).isEqualTo("Empty Category");
        assertThat(emptyCategoryWithCount.getTotalPostCount()).isEqualTo(0L);
        assertThat(emptyCategoryWithCount.getPublishedPostCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should find root categories with counts")
    void shouldFindRootCategoriesWithCounts() {
        // When
        List<CategoryWithCount> rootCategories = categoryRepository.findRootCategoriesWithCounts();

        // Then
        assertThat(rootCategories).isNotNull();
        assertThat(rootCategories).hasSize(2); // root category and empty category

        CategoryWithCount rootCategoryWithCount = rootCategories.stream()
                .filter(c -> "Root Category".equals(c.getName()))
                .findFirst()
                .orElseThrow();

        assertThat(rootCategoryWithCount.getParentId()).isNull();
        assertThat(rootCategoryWithCount.getTotalPostCount()).isEqualTo(5L);
        assertThat(rootCategoryWithCount.getPublishedPostCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("Should find child categories with counts by parent ID")
    void shouldFindChildCategoriesWithCountsByParentId() {
        // When
        List<CategoryWithCount> childCategories = categoryRepository.findChildCategoriesWithCounts(rootCategory.getId());

        // Then
        assertThat(childCategories).isNotNull();
        assertThat(childCategories).hasSize(1);

        CategoryWithCount childCategoryWithCount = childCategories.get(0);
        assertThat(childCategoryWithCount.getName()).isEqualTo("Child Category");
        assertThat(childCategoryWithCount.getParentId()).isEqualTo(rootCategory.getId());
        assertThat(childCategoryWithCount.getParentName()).isEqualTo("Root Category");
        assertThat(childCategoryWithCount.getTotalPostCount()).isEqualTo(1L);
        assertThat(childCategoryWithCount.getPublishedPostCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should find categories with counts using pagination")
    void shouldFindCategoriesWithCountsUsingPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<CategoryWithCount> categoriesPage = categoryRepository.findCategoriesWithCounts(pageable);

        // Then
        assertThat(categoriesPage).isNotNull();
        assertThat(categoriesPage.getContent()).hasSize(2);
        assertThat(categoriesPage.getTotalElements()).isEqualTo(3);
        assertThat(categoriesPage.getTotalPages()).isEqualTo(2);
        assertThat(categoriesPage.hasNext()).isTrue();
    }

    @Test
    @DisplayName("Should find popular categories ordered by published post count")
    void shouldFindPopularCategoriesOrderedByPublishedPostCount() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<CategoryWithCount> popularCategories = categoryRepository.findPopularCategoriesWithCounts(pageable);

        // Then
        assertThat(popularCategories).isNotNull();
        assertThat(popularCategories.getContent()).hasSize(3);

        // Should be ordered by published post count descending
        List<CategoryWithCount> categories = popularCategories.getContent();
        assertThat(categories.get(0).getName()).isEqualTo("Root Category");
        assertThat(categories.get(0).getPublishedPostCount()).isEqualTo(3L);
        assertThat(categories.get(1).getName()).isEqualTo("Child Category");
        assertThat(categories.get(1).getPublishedPostCount()).isEqualTo(1L);
        assertThat(categories.get(2).getName()).isEqualTo("Empty Category");
        assertThat(categories.get(2).getPublishedPostCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should search categories with counts by name")
    void shouldSearchCategoriesWithCountsByName() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<CategoryWithCount> searchResults = categoryRepository.searchCategoriesWithCounts("Root", pageable);

        // Then
        assertThat(searchResults).isNotNull();
        assertThat(searchResults.getContent()).hasSize(1);

        CategoryWithCount foundCategory = searchResults.getContent().get(0);
        assertThat(foundCategory.getName()).isEqualTo("Root Category");
        assertThat(foundCategory.getTotalPostCount()).isEqualTo(5L);
        assertThat(foundCategory.getPublishedPostCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("Should test default methods in CategoryWithCount interface")
    void shouldTestDefaultMethodsInCategoryWithCountInterface() {
        // Given
        List<CategoryWithCount> categories = categoryRepository.findCategoriesWithCounts();
        CategoryWithCount rootCategoryWithCount = categories.stream()
                .filter(c -> "Root Category".equals(c.getName()))
                .findFirst()
                .orElseThrow();
        CategoryWithCount emptyCategoryWithCount = categories.stream()
                .filter(c -> "Empty Category".equals(c.getName()))
                .findFirst()
                .orElseThrow();

        // Test hasChildren method (based on post count, not actual children)
        assertThat(rootCategoryWithCount.hasChildren()).isTrue();
        assertThat(emptyCategoryWithCount.hasChildren()).isFalse();

        // Test hasPublishedPosts method
        assertThat(rootCategoryWithCount.hasPublishedPosts()).isTrue();
        assertThat(emptyCategoryWithCount.hasPublishedPosts()).isFalse();

        // Test getDraftPostCount method
        assertThat(rootCategoryWithCount.getDraftPostCount()).isEqualTo(2L); // 5 total - 3 published
        assertThat(emptyCategoryWithCount.getDraftPostCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should handle categories with null counts gracefully")
    void shouldHandleCategoriesWithNullCountsGracefully() {
        // This test ensures that the projection handles null values properly
        // The LEFT JOIN should return 0 for categories with no posts
        List<CategoryWithCount> categories = categoryRepository.findCategoriesWithCounts();
        CategoryWithCount emptyCategoryWithCount = categories.stream()
                .filter(c -> "Empty Category".equals(c.getName()))
                .findFirst()
                .orElseThrow();

        assertThat(emptyCategoryWithCount.getTotalPostCount()).isNotNull();
        assertThat(emptyCategoryWithCount.getPublishedPostCount()).isNotNull();
        assertThat(emptyCategoryWithCount.getTotalPostCount()).isEqualTo(0L);
        assertThat(emptyCategoryWithCount.getPublishedPostCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should return empty list when searching for non-existent category")
    void shouldReturnEmptyListWhenSearchingForNonExistentCategory() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<CategoryWithCount> searchResults = categoryRepository.searchCategoriesWithCounts("NonExistent", pageable);

        // Then
        assertThat(searchResults).isNotNull();
        assertThat(searchResults.getContent()).isEmpty();
        assertThat(searchResults.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("Should maintain display order in results")
    void shouldMaintainDisplayOrderInResults() {
        // When
        List<CategoryWithCount> categories = categoryRepository.findCategoriesWithCounts();

        // Then
        assertThat(categories).isNotNull();
        assertThat(categories).hasSize(3);

        // Should be ordered by display order
        assertThat(categories.get(0).getDisplayOrder()).isEqualTo(1);
        assertThat(categories.get(1).getDisplayOrder()).isEqualTo(1);
        assertThat(categories.get(2).getDisplayOrder()).isEqualTo(2);
    }
}