package com.personalblog.repository.projection;

import com.personalblog.entity.BlogPost;
import com.personalblog.entity.Category;
import com.personalblog.entity.Tag;
import com.personalblog.entity.User;
import com.personalblog.repository.TagRepository;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TagCloudItem projection interface.
 * Tests the projection methods in TagRepository to ensure
 * proper data mapping and popularity calculations.
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TagCloudItem Projection Tests")
class TagCloudItemTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TagRepository tagRepository;

    private User testAuthor;
    private Category testCategory;
    private Tag popularTag;
    private Tag moderateTag;
    private Tag unpopularTag;
    private Tag unusedTag;

    @BeforeEach
    void setUp() {
        // Create test author
        testAuthor = new User();
        testAuthor.setUsername("testauthor");
        // User entity doesn't have displayName field, using username for display
        testAuthor.setEmail("test@example.com");
        testAuthor.setPassword("password");
        testAuthor = entityManager.persistAndFlush(testAuthor);

        // Create test category
        testCategory = new Category();
        testCategory.setName("Test Category");
        testCategory.setSlug("test-category");
        testCategory.setDescription("Test category description");
        testCategory.setDisplayOrder(1);
        testCategory = entityManager.persistAndFlush(testCategory);

        // Create tags with different usage counts
        popularTag = new Tag();
        popularTag.setName("Popular Tag");
        popularTag.setSlug("popular-tag");
        popularTag.setDescription("Most popular tag");
        popularTag.setUsageCount(10);
        popularTag.setColorCode("#FF0000");
        popularTag = entityManager.persistAndFlush(popularTag);

        moderateTag = new Tag();
        moderateTag.setName("Moderate Tag");
        moderateTag.setSlug("moderate-tag");
        moderateTag.setDescription("Moderately popular tag");
        moderateTag.setUsageCount(5);
        moderateTag.setColorCode("#00FF00");
        moderateTag = entityManager.persistAndFlush(moderateTag);

        unpopularTag = new Tag();
        unpopularTag.setName("Unpopular Tag");
        unpopularTag.setSlug("unpopular-tag");
        unpopularTag.setDescription("Least popular tag");
        unpopularTag.setUsageCount(1);
        unpopularTag.setColorCode("#0000FF");
        unpopularTag = entityManager.persistAndFlush(unpopularTag);

        unusedTag = new Tag();
        unusedTag.setName("Unused Tag");
        unusedTag.setSlug("unused-tag");
        unusedTag.setDescription("Tag with no usage");
        unusedTag.setUsageCount(0);
        unusedTag = entityManager.persistAndFlush(unusedTag);

        // Create blog posts with tags
        // Popular tag: 10 posts
        for (int i = 1; i <= 10; i++) {
            BlogPost post = new BlogPost();
            post.setTitle("Popular Post " + i);
            post.setSlug("popular-post-" + i);
            post.setContent("Content " + i);
            post.setExcerpt("Excerpt " + i);
            post.setStatus(BlogPost.Status.PUBLISHED);
            post.setAuthor(testAuthor);
            post.setCategory(testCategory);
            post.setPublishedDate(LocalDateTime.now().minusDays(i));
            post.setTags(Set.of(popularTag));
            entityManager.persistAndFlush(post);
        }

        // Moderate tag: 5 posts
        for (int i = 1; i <= 5; i++) {
            BlogPost post = new BlogPost();
            post.setTitle("Moderate Post " + i);
            post.setSlug("moderate-post-" + i);
            post.setContent("Content " + i);
            post.setExcerpt("Excerpt " + i);
            post.setStatus(BlogPost.Status.PUBLISHED);
            post.setAuthor(testAuthor);
            post.setCategory(testCategory);
            post.setPublishedDate(LocalDateTime.now().minusDays(i));
            post.setTags(Set.of(moderateTag));
            entityManager.persistAndFlush(post);
        }

        // Unpopular tag: 1 post
        BlogPost unpopularPost = new BlogPost();
        unpopularPost.setTitle("Unpopular Post");
        unpopularPost.setSlug("unpopular-post");
        unpopularPost.setContent("Unpopular Content");
        unpopularPost.setExcerpt("Unpopular Excerpt");
        unpopularPost.setStatus(BlogPost.Status.PUBLISHED);
        unpopularPost.setAuthor(testAuthor);
        unpopularPost.setCategory(testCategory);
        unpopularPost.setPublishedDate(LocalDateTime.now().minusDays(1));
        unpopularPost.setTags(Set.of(unpopularTag));
        entityManager.persistAndFlush(unpopularPost);

        entityManager.clear();
    }

    @Test
    @DisplayName("Should find all tag cloud data with correct popularity calculations")
    void shouldFindAllTagCloudDataWithCorrectPopularityCalculations() {
        // When
        List<TagCloudItem> tagCloudItems = tagRepository.findAllTagCloudData();

        // Then
        assertThat(tagCloudItems).isNotNull();
        assertThat(tagCloudItems).hasSize(3); // Only tags with usage count > 0

        // Find popular tag
        TagCloudItem popularTagItem = tagCloudItems.stream()
                .filter(t -> "Popular Tag".equals(t.getName()))
                .findFirst()
                .orElseThrow();

        assertThat(popularTagItem.getId()).isEqualTo(popularTag.getId());
        assertThat(popularTagItem.getName()).isEqualTo("Popular Tag");
        assertThat(popularTagItem.getSlug()).isEqualTo("popular-tag");
        assertThat(popularTagItem.getDescription()).isEqualTo("Most popular tag");
        assertThat(popularTagItem.getUsageCount()).isEqualTo(10);
        assertThat(popularTagItem.getColorCode()).isEqualTo("#FF0000");
        assertThat(popularTagItem.getActualPostCount()).isEqualTo(10L);
        assertThat(popularTagItem.getRelativePopularity()).isEqualTo(100.0); // Most popular = 100%
        assertThat(popularTagItem.getCreatedAt()).isNotNull();
        assertThat(popularTagItem.getUpdatedAt()).isNotNull();

        // Find moderate tag
        TagCloudItem moderateTagItem = tagCloudItems.stream()
                .filter(t -> "Moderate Tag".equals(t.getName()))
                .findFirst()
                .orElseThrow();

        assertThat(moderateTagItem.getUsageCount()).isEqualTo(5);
        assertThat(moderateTagItem.getActualPostCount()).isEqualTo(5L);
        assertThat(moderateTagItem.getRelativePopularity()).isEqualTo(50.0); // 5/10 * 100 = 50%

        // Find unpopular tag
        TagCloudItem unpopularTagItem = tagCloudItems.stream()
                .filter(t -> "Unpopular Tag".equals(t.getName()))
                .findFirst()
                .orElseThrow();

        assertThat(unpopularTagItem.getUsageCount()).isEqualTo(1);
        assertThat(unpopularTagItem.getActualPostCount()).isEqualTo(1L);
        assertThat(unpopularTagItem.getRelativePopularity()).isEqualTo(10.0); // 1/10 * 100 = 10%
    }

    @Test
    @DisplayName("Should find tag cloud data with limit")
    void shouldFindTagCloudDataWithLimit() {
        // When
        List<TagCloudItem> tagCloudItems = tagRepository.findTagCloudData(2);

        // Then
        assertThat(tagCloudItems).isNotNull();
        assertThat(tagCloudItems).hasSize(2); // Limited to 2 items

        // Should be ordered by usage count descending
        assertThat(tagCloudItems.get(0).getName()).isEqualTo("Popular Tag");
        assertThat(tagCloudItems.get(0).getUsageCount()).isEqualTo(10);
        assertThat(tagCloudItems.get(1).getName()).isEqualTo("Moderate Tag");
        assertThat(tagCloudItems.get(1).getUsageCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should find popular tag cloud items with minimum usage")
    void shouldFindPopularTagCloudItemsWithMinimumUsage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<TagCloudItem> popularTags = tagRepository.findPopularTagCloudItems(5, pageable);

        // Then
        assertThat(popularTags).isNotNull();
        assertThat(popularTags.getContent()).hasSize(2); // Only tags with usage >= 5

        assertThat(popularTags.getContent().get(0).getName()).isEqualTo("Popular Tag");
        assertThat(popularTags.getContent().get(0).getUsageCount()).isEqualTo(10);
        assertThat(popularTags.getContent().get(1).getName()).isEqualTo("Moderate Tag");
        assertThat(popularTags.getContent().get(1).getUsageCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should search tag cloud items by name")
    void shouldSearchTagCloudItemsByName() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<TagCloudItem> searchResults = tagRepository.searchTagCloudItems("Most popular", pageable);

        // Then
        assertThat(searchResults).isNotNull();
        assertThat(searchResults.getContent()).hasSize(1);

        TagCloudItem foundTag = searchResults.getContent().get(0);
        assertThat(foundTag.getName()).isEqualTo("Popular Tag");
        assertThat(foundTag.getUsageCount()).isEqualTo(10);
        assertThat(foundTag.getRelativePopularity()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("Should find tag cloud items with discrepant counts")
    void shouldFindTagCloudItemsWithDiscrepantCounts() {
        // Given - Manually update usage count to create discrepancy
        entityManager.getEntityManager().createQuery("UPDATE Tag t SET t.usageCount = 15 WHERE t.id = :id")
                .setParameter("id", popularTag.getId())
                .executeUpdate();
        entityManager.clear();

        // When
        List<TagCloudItem> discrepantTags = tagRepository.findTagCloudItemsWithDiscrepantCounts();

        // Then
        assertThat(discrepantTags).isNotNull();
        assertThat(discrepantTags).hasSize(1);

        TagCloudItem discrepantTag = discrepantTags.get(0);
        assertThat(discrepantTag.getName()).isEqualTo("Popular Tag");
        assertThat(discrepantTag.getUsageCount()).isEqualTo(15); // Updated usage count
        assertThat(discrepantTag.getActualPostCount()).isEqualTo(10L); // Actual post count
    }

    @Test
    @DisplayName("Should find trending tag cloud items")
    void shouldFindTrendingTagCloudItems() {
        // Given
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<TagCloudItem> trendingTags = tagRepository.findTrendingTagCloudItems(since, pageable);

        // Then
        assertThat(trendingTags).isNotNull();
        assertThat(trendingTags.getContent()).hasSize(3);

        // Should be ordered by recent post count
        assertThat(trendingTags.getContent().get(0).getName()).isEqualTo("Popular Tag");
        assertThat(trendingTags.getContent().get(1).getName()).isEqualTo("Moderate Tag");
        assertThat(trendingTags.getContent().get(2).getName()).isEqualTo("Unpopular Tag");
    }

    @Test
    @DisplayName("Should test default methods in TagCloudItem interface")
    void shouldTestDefaultMethodsInTagCloudItemInterface() {
        // Given
        List<TagCloudItem> tagCloudItems = tagRepository.findAllTagCloudData();
        TagCloudItem popularTagItem = tagCloudItems.stream()
                .filter(t -> "Popular Tag".equals(t.getName()))
                .findFirst()
                .orElseThrow();
        TagCloudItem unpopularTagItem = tagCloudItems.stream()
                .filter(t -> "Unpopular Tag".equals(t.getName()))
                .findFirst()
                .orElseThrow();

        // Test calculateFontSize method
        double popularFontSize = popularTagItem.calculateFontSize(12.0, 24.0);
        double unpopularFontSize = unpopularTagItem.calculateFontSize(12.0, 24.0);

        assertThat(popularFontSize).isEqualTo(24.0); // 100% popularity = max size
        assertThat(unpopularFontSize).isEqualTo(13.2); // 10% popularity = 12 + (24-12) * 0.1

        // Test getCssClass method
        assertThat(popularTagItem.getCssClass()).isEqualTo("tag-xl"); // >= 80%
        assertThat(unpopularTagItem.getCssClass()).isEqualTo("tag-small"); // < 30%

        // Test isPopular methods
        assertThat(popularTagItem.isPopular()).isTrue(); // >= 50%
        assertThat(popularTagItem.isPopular(80.0)).isTrue(); // >= 80%
        assertThat(unpopularTagItem.isPopular()).isFalse(); // < 50%
        assertThat(unpopularTagItem.isPopular(5.0)).isTrue(); // >= 5%

        // Test getDisplayWeight method
        assertThat(popularTagItem.getDisplayWeight()).isEqualTo(10); // 100% = weight 10
        assertThat(unpopularTagItem.getDisplayWeight()).isEqualTo(1); // 10% = weight 1

        // Test isUsageCountAccurate method
        assertThat(popularTagItem.isUsageCountAccurate()).isTrue(); // 10 usage = 10 posts
        assertThat(unpopularTagItem.isUsageCountAccurate()).isTrue(); // 1 usage = 1 post
    }

    @Test
    @DisplayName("Should handle tags with zero usage count")
    void shouldHandleTagsWithZeroUsageCount() {
        // When - findAllTagCloudData should exclude tags with usage count 0
        List<TagCloudItem> tagCloudItems = tagRepository.findAllTagCloudData();

        // Then
        assertThat(tagCloudItems).isNotNull();
        assertThat(tagCloudItems).hasSize(3); // Should not include unused tag

        boolean hasUnusedTag = tagCloudItems.stream()
                .anyMatch(t -> "Unused Tag".equals(t.getName()));
        assertThat(hasUnusedTag).isFalse();
    }

    @Test
    @DisplayName("Should return empty results when no tags match criteria")
    void shouldReturnEmptyResultsWhenNoTagsMatchCriteria() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<TagCloudItem> searchResults = tagRepository.searchTagCloudItems("NonExistent", pageable);

        // Then
        assertThat(searchResults).isNotNull();
        assertThat(searchResults.getContent()).isEmpty();
        assertThat(searchResults.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void shouldHandlePaginationCorrectly() {
        // Given
        Pageable firstPage = PageRequest.of(0, 2);
        Pageable secondPage = PageRequest.of(1, 2);

        // When
        Page<TagCloudItem> firstPageResults = tagRepository.findPopularTagCloudItems(0, firstPage);
        Page<TagCloudItem> secondPageResults = tagRepository.findPopularTagCloudItems(0, secondPage);

        // Then
        assertThat(firstPageResults.getContent()).hasSize(2);
        assertThat(firstPageResults.getTotalElements()).isEqualTo(3);
        assertThat(firstPageResults.getTotalPages()).isEqualTo(2);
        assertThat(firstPageResults.hasNext()).isTrue();

        assertThat(secondPageResults.getContent()).hasSize(1);
        assertThat(secondPageResults.getTotalElements()).isEqualTo(3);
        assertThat(secondPageResults.isLast()).isTrue();
        assertThat(secondPageResults.hasPrevious()).isTrue();
    }

    @Test
    @DisplayName("Should maintain correct ordering by usage count")
    void shouldMaintainCorrectOrderingByUsageCount() {
        // When
        List<TagCloudItem> tagCloudItems = tagRepository.findAllTagCloudData();

        // Then
        assertThat(tagCloudItems).isNotNull();
        assertThat(tagCloudItems).hasSize(3);

        // Should be ordered by usage count descending
        assertThat(tagCloudItems.get(0).getUsageCount()).isEqualTo(10);
        assertThat(tagCloudItems.get(1).getUsageCount()).isEqualTo(5);
        assertThat(tagCloudItems.get(2).getUsageCount()).isEqualTo(1);

        // Verify ordering is maintained
        for (int i = 0; i < tagCloudItems.size() - 1; i++) {
            assertThat(tagCloudItems.get(i).getUsageCount())
                    .isGreaterThanOrEqualTo(tagCloudItems.get(i + 1).getUsageCount());
        }
    }
}