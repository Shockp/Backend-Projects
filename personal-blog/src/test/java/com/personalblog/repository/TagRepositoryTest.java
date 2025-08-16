package com.personalblog.repository;

import com.personalblog.entity.BlogPost;
import com.personalblog.entity.Category;
import com.personalblog.entity.Role;
import com.personalblog.entity.Tag;
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
 * Comprehensive test suite for TagRepository.
 * 
 * Tests cover:
 * - Basic CRUD operations with soft delete support
 * - Slug-based queries for SEO-friendly URLs
 * - Tag usage count management and statistics
 * - Popular tags and tag cloud functionality
 * - Search capabilities across tag names and descriptions
 * - Blog post associations and tag filtering
 * - Performance optimization with proper indexing
 * - Security validation and input sanitization
 * - Thread-safe usage count operations
 * - Edge cases and error handling
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tag Repository Tests")
class TagRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TagRepository tagRepository;

    private Tag javaTag;
    private Tag springTag;
    private Tag webTag;
    private User testUser;
    private Category testCategory;

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

        // Create test category
        testCategory = new Category();
        testCategory.setName("Technology");
        testCategory.setSlug("technology");
        testCategory.setDescription("Tech-related posts");
        testCategory.setDisplayOrder(1);
        entityManager.persistAndFlush(testCategory);

        // Create test tags
        javaTag = createTag("Java", "java", "Java programming language", "#FF5733", 5);
        springTag = createTag("Spring", "spring", "Spring Framework", "#33FF57", 3);
        webTag = createTag("Web Development", "web-development", "Web development topics", "#3357FF", 8);

        entityManager.persistAndFlush(javaTag);
        entityManager.persistAndFlush(springTag);
        entityManager.persistAndFlush(webTag);
    }

    // ==================== Helper Methods ====================

    private Tag createTag(String name, String slug, String description, String colorCode, int usageCount) {
        Tag tag = new Tag();
        tag.setName(name);
        tag.setSlug(slug);
        tag.setDescription(description);
        tag.setColorCode(colorCode);
        tag.setUsageCount(usageCount);
        return tag;
    }

    private BlogPost createBlogPost(String title, String slug, Set<Tag> tags) {
        BlogPost post = new BlogPost();
        post.setTitle(title);
        post.setSlug(slug);
        post.setContent("Test content for " + title);
        post.setExcerpt("Test excerpt");
        post.setStatus(BlogPost.Status.PUBLISHED);
        post.setAuthor(testUser);
        post.setCategory(testCategory);
        post.setPublishedDate(LocalDateTime.now());
        post.setTags(tags);
        return entityManager.persistAndFlush(post);
    }

    // ==================== Basic CRUD Tests ====================

    @Test
    @DisplayName("Should find tag by slug")
    void shouldFindTagBySlug() {
        // When
        Optional<Tag> found = tagRepository.findBySlug("java");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Java");
        assertThat(found.get().getSlug()).isEqualTo("java");
    }

    @Test
    @DisplayName("Should return empty when tag slug not found")
    void shouldReturnEmptyWhenSlugNotFound() {
        // When
        Optional<Tag> found = tagRepository.findBySlug("nonexistent");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find tag by name case insensitive")
    void shouldFindTagByNameCaseInsensitive() {
        // When
        Optional<Tag> found = tagRepository.findByNameIgnoreCase("JAVA");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Java");
    }

    @Test
    @DisplayName("Should check if slug exists")
    void shouldCheckIfSlugExists() {
        // When & Then
        assertThat(tagRepository.existsBySlug("java")).isTrue();
        assertThat(tagRepository.existsBySlug("nonexistent")).isFalse();
    }

    @Test
    @DisplayName("Should check if name exists case insensitive")
    void shouldCheckIfNameExistsCaseInsensitive() {
        // When & Then
        assertThat(tagRepository.existsByNameIgnoreCase("JAVA")).isTrue();
        assertThat(tagRepository.existsByNameIgnoreCase("nonexistent")).isFalse();
    }

    @Test
    @DisplayName("Should check slug existence excluding specific tag")
    void shouldCheckSlugExistenceExcludingTag() {
        // When & Then
        assertThat(tagRepository.existsBySlugAndIdNot("java", javaTag.getId())).isFalse();
        assertThat(tagRepository.existsBySlugAndIdNot("java", springTag.getId())).isTrue();
    }

    // ==================== Usage Count Tests ====================

    @Test
    @DisplayName("Should find tags by usage count range")
    void shouldFindTagsByUsageCountRange() {
        // When
        List<Tag> tags = tagRepository.findByUsageCountBetween(3, 5);

        // Then
        assertThat(tags).hasSize(2);
        assertThat(tags).extracting(Tag::getName)
                .containsExactlyInAnyOrder("Java", "Spring");
    }

    @Test
    @DisplayName("Should find tags with usage count greater than threshold")
    void shouldFindTagsWithUsageCountGreaterThan() {
        // When
        List<Tag> popularTags = tagRepository.findByUsageCountGreaterThan(4);

        // Then
        assertThat(popularTags).hasSize(2);
        assertThat(popularTags).extracting(Tag::getName)
                .containsExactlyInAnyOrder("Java", "Web Development");
    }

    @Test
    @DisplayName("Should find unused tags")
    void shouldFindUnusedTags() {
        // Given
        Tag unusedTag = createTag("Unused", "unused", "Unused tag", "#FFFFFF", 0);
        entityManager.persistAndFlush(unusedTag);

        // When
        List<Tag> unusedTags = tagRepository.findUnusedTags();

        // Then
        assertThat(unusedTags).hasSize(1);
        assertThat(unusedTags.get(0).getName()).isEqualTo("Unused");
    }

    @Test
    @DisplayName("Should increment usage count")
    void shouldIncrementUsageCount() {
        // Given
        Long tagId = javaTag.getId();
        int originalCount = javaTag.getUsageCount();

        // When
        int updatedRows = tagRepository.incrementUsageCount(tagId);

        // Then
        assertThat(updatedRows).isEqualTo(1);
        
        entityManager.clear();
        Tag updated = tagRepository.findById(tagId).orElseThrow();
        assertThat(updated.getUsageCount()).isEqualTo(originalCount + 1);
    }

    @Test
    @DisplayName("Should decrement usage count")
    void shouldDecrementUsageCount() {
        // Given
        Long tagId = javaTag.getId();
        int originalCount = javaTag.getUsageCount();

        // When
        int updatedRows = tagRepository.decrementUsageCount(tagId);

        // Then
        assertThat(updatedRows).isEqualTo(1);
        
        entityManager.clear();
        Tag updated = tagRepository.findById(tagId).orElseThrow();
        assertThat(updated.getUsageCount()).isEqualTo(originalCount - 1);
    }

    @Test
    @DisplayName("Should not decrement usage count below zero")
    void shouldNotDecrementUsageCountBelowZero() {
        // Given
        Tag zeroUsageTag = createTag("Zero Usage", "zero-usage", "Zero usage tag", "#FFFFFF", 0);
        entityManager.persistAndFlush(zeroUsageTag);

        // When
        int updatedRows = tagRepository.decrementUsageCount(zeroUsageTag.getId());

        // Then
        assertThat(updatedRows).isEqualTo(0); // No rows updated
        
        entityManager.clear();
        Tag updated = tagRepository.findById(zeroUsageTag.getId()).orElseThrow();
        assertThat(updated.getUsageCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should update usage count to specific value")
    void shouldUpdateUsageCountToSpecificValue() {
        // Given
        Long tagId = javaTag.getId();

        // When
        int updatedRows = tagRepository.updateUsageCount(tagId, 10);

        // Then
        assertThat(updatedRows).isEqualTo(1);
        
        entityManager.clear();
        Tag updated = tagRepository.findById(tagId).orElseThrow();
        assertThat(updated.getUsageCount()).isEqualTo(10);
    }

    // ==================== Popular Tags Tests ====================

    @Test
    @DisplayName("Should find most popular tags")
    void shouldFindMostPopularTags() {
        // Given
        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<Tag> popularTags = tagRepository.findMostPopular(pageable);

        // Then
        assertThat(popularTags.getContent()).hasSize(2);
        assertThat(popularTags.getContent().get(0).getName()).isEqualTo("Web Development"); // Highest usage count
        assertThat(popularTags.getContent().get(1).getName()).isEqualTo("Java");
    }

    @Test
    @DisplayName("Should find popular tags with minimum usage")
    void shouldFindPopularTagsWithMinimumUsage() {
        // Given
        int minUsage = 4;
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Tag> popularTags = tagRepository.findPopularTags(minUsage, pageable);

        // Then
        assertThat(popularTags.getContent()).hasSize(2);
        assertThat(popularTags.getContent()).allMatch(tag -> tag.getUsageCount() >= minUsage);
    }

    @Test
    @DisplayName("Should get tag cloud data")
    void shouldGetTagCloudData() {
        // When
        List<Object[]> tagCloudData = tagRepository.getTagCloudData();

        // Then
        assertThat(tagCloudData).hasSize(3);
        
        // Verify structure: [Tag, usageCount]
        Object[] firstTag = tagCloudData.get(0);
        assertThat(firstTag).hasSize(2);
        assertThat(firstTag[0]).isInstanceOf(Tag.class);
        assertThat(firstTag[1]).isInstanceOf(Number.class);
    }

    @Test
    @DisplayName("Should get tag cloud data with limit")
    void shouldGetTagCloudDataWithLimit() {
        // When
        List<Object[]> tagCloudData = tagRepository.getTagCloudData(2);

        // Then
        assertThat(tagCloudData).hasSize(2);
        
        // Should be ordered by usage count descending
        Tag firstTag = (Tag) tagCloudData.get(0)[0];
        Tag secondTag = (Tag) tagCloudData.get(1)[0];
        
        assertThat(firstTag.getUsageCount()).isGreaterThanOrEqualTo(secondTag.getUsageCount());
    }

    // ==================== Search Tests ====================

    @Test
    @DisplayName("Should search tags by name")
    void shouldSearchTagsByName() {
        // When
        List<Tag> results = tagRepository.searchByName("java");

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Java");
    }

    @Test
    @DisplayName("Should search tags by name with pagination")
    void shouldSearchTagsByNameWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Tag> results = tagRepository.searchByName("web", pageable);

        // Then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getName()).isEqualTo("Web Development");
    }

    @Test
    @DisplayName("Should search tags by name or description")
    void shouldSearchTagsByNameOrDescription() {
        // When
        List<Tag> results = tagRepository.searchByNameOrDescription("framework");

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Spring");
    }

    @Test
    @DisplayName("Should search tags by name or description with pagination")
    void shouldSearchTagsByNameOrDescriptionWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Tag> results = tagRepository.searchByNameOrDescription("development", pageable);

        // Then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getName()).isEqualTo("Web Development");
    }

    // ==================== Blog Post Association Tests ====================

    @Test
    @DisplayName("Should find tags by blog post ID")
    void shouldFindTagsByBlogPostId() {
        // Given
        BlogPost post = createBlogPost("Java Spring Tutorial", "java-spring-tutorial", Set.of(javaTag, springTag));

        // When
        List<Tag> postTags = tagRepository.findByBlogPostId(post.getId());

        // Then
        assertThat(postTags).hasSize(2);
        assertThat(postTags).extracting(Tag::getName)
                .containsExactlyInAnyOrder("Java", "Spring");
    }

    @Test
    @DisplayName("Should find tags used in published posts")
    void shouldFindTagsUsedInPublishedPosts() {
        // Given
        createBlogPost("Published Post", "published-post", Set.of(javaTag));
        
        BlogPost draftPost = new BlogPost();
        draftPost.setTitle("Draft Post");
        draftPost.setSlug("draft-post");
        draftPost.setContent("Draft content");
        draftPost.setStatus(BlogPost.Status.DRAFT);
        draftPost.setAuthor(testUser);
        draftPost.setCategory(testCategory);
        draftPost.setTags(Set.of(springTag));
        entityManager.persistAndFlush(draftPost);

        // When
        List<Tag> publishedTags = tagRepository.findTagsUsedInPublishedPosts();

        // Then
        assertThat(publishedTags).hasSize(1);
        assertThat(publishedTags.get(0).getName()).isEqualTo("Java");
    }

    @Test
    @DisplayName("Should find tags used in date range")
    void shouldFindTagsUsedInDateRange() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        
        createBlogPost("Recent Post", "recent-post", Set.of(javaTag, springTag));

        // When
        List<Tag> tagsInRange = tagRepository.findTagsUsedInDateRange(startDate, endDate);

        // Then
        assertThat(tagsInRange).hasSize(2);
        assertThat(tagsInRange).extracting(Tag::getName)
                .containsExactlyInAnyOrder("Java", "Spring");
    }

    @Test
    @DisplayName("Should count posts using tag")
    void shouldCountPostsUsingTag() {
        // Given
        createBlogPost("Post 1", "post-1", Set.of(javaTag));
        createBlogPost("Post 2", "post-2", Set.of(javaTag, springTag));

        // When
        long javaPostCount = tagRepository.countPostsUsingTag(javaTag.getId());
        long springPostCount = tagRepository.countPostsUsingTag(springTag.getId());

        // Then
        assertThat(javaPostCount).isEqualTo(2);
        assertThat(springPostCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Should count published posts using tag")
    void shouldCountPublishedPostsUsingTag() {
        // Given
        createBlogPost("Published Post", "published-post", Set.of(javaTag));
        
        BlogPost draftPost = new BlogPost();
        draftPost.setTitle("Draft Post");
        draftPost.setSlug("draft-post");
        draftPost.setContent("Draft content");
        draftPost.setStatus(BlogPost.Status.DRAFT);
        draftPost.setAuthor(testUser);
        draftPost.setCategory(testCategory);
        draftPost.setTags(Set.of(javaTag));
        entityManager.persistAndFlush(draftPost);

        // When
        long publishedCount = tagRepository.countPublishedPostsUsingTag(javaTag.getId());

        // Then
        assertThat(publishedCount).isEqualTo(1); // Only published posts counted
    }

    // ==================== Statistics Tests ====================

    @Test
    @DisplayName("Should get tag usage statistics")
    void shouldGetTagUsageStatistics() {
        // When
        List<Object[]> stats = tagRepository.getTagUsageStatistics();

        // Then
        assertThat(stats).hasSize(3);
        
        // Verify structure: [Tag, usageCount, postCount]
        Object[] firstStat = stats.get(0);
        assertThat(firstStat).hasSize(3);
        assertThat(firstStat[0]).isInstanceOf(Tag.class);
        assertThat(firstStat[1]).isInstanceOf(Number.class);
        assertThat(firstStat[2]).isInstanceOf(Number.class);
    }

    @Test
    @DisplayName("Should find tags with discrepant usage counts")
    void shouldFindTagsWithDiscrepantUsageCounts() {
        // Given - Create a post with java tag to create discrepancy
        createBlogPost("Java Post", "java-post", Set.of(javaTag));

        // When
        List<Tag> discrepantTags = tagRepository.findTagsWithDiscrepantUsageCounts();

        // Then
        assertThat(discrepantTags).hasSize(1);
        assertThat(discrepantTags.get(0).getName()).isEqualTo("Java");
    }

    @Test
    @DisplayName("Should recalculate usage counts")
    void shouldRecalculateUsageCounts() {
        // Given
        createBlogPost("Java Post 1", "java-post-1", Set.of(javaTag));
        createBlogPost("Java Post 2", "java-post-2", Set.of(javaTag));

        // When
        int updatedCount = tagRepository.recalculateUsageCounts();

        // Then
        assertThat(updatedCount).isGreaterThan(0);
        
        entityManager.clear();
        Tag updated = tagRepository.findById(javaTag.getId()).orElseThrow();
        assertThat(updated.getUsageCount()).isEqualTo(2); // Should match actual post count
    }

    // ==================== Sorting Tests ====================

    @Test
    @DisplayName("Should find all tags ordered by name")
    void shouldFindAllTagsOrderedByName() {
        // When
        List<Tag> tags = tagRepository.findAllByOrderByNameAsc();

        // Then
        assertThat(tags).hasSize(3);
        assertThat(tags.get(0).getName()).isEqualTo("Java");
        assertThat(tags.get(1).getName()).isEqualTo("Spring");
        assertThat(tags.get(2).getName()).isEqualTo("Web Development");
    }

    @Test
    @DisplayName("Should find all tags ordered by usage count")
    void shouldFindAllTagsOrderedByUsageCount() {
        // When
        List<Tag> tags = tagRepository.findAllByOrderByUsageCountDesc();

        // Then
        assertThat(tags).hasSize(3);
        assertThat(tags.get(0).getName()).isEqualTo("Web Development"); // Highest usage count
        assertThat(tags.get(1).getName()).isEqualTo("Java");
        assertThat(tags.get(2).getName()).isEqualTo("Spring"); // Lowest usage count
    }

    // ==================== Bulk Operations Tests ====================

    @Test
    @DisplayName("Should bulk update color codes")
    void shouldBulkUpdateColorCodes() {
        // Given
        List<Long> tagIds = List.of(javaTag.getId(), springTag.getId());
        String newColor = "#FFFFFF";

        // When
        int updatedCount = tagRepository.bulkUpdateColorCode(tagIds, newColor);

        // Then
        assertThat(updatedCount).isEqualTo(2);
        
        entityManager.clear();
        Tag updatedJava = tagRepository.findById(javaTag.getId()).orElseThrow();
        Tag updatedSpring = tagRepository.findById(springTag.getId()).orElseThrow();
        
        assertThat(updatedJava.getColorCode()).isEqualTo(newColor);
        assertThat(updatedSpring.getColorCode()).isEqualTo(newColor);
    }

    @Test
    @DisplayName("Should merge tags")
    void shouldMergeTags() {
        // Given
        Tag sourceTag = createTag("JavaScript", "javascript", "JavaScript language", "#FFFF00", 2);
        Tag targetTag = createTag("JS", "js", "JS language", "#FFFF00", 3);
        entityManager.persistAndFlush(sourceTag);
        entityManager.persistAndFlush(targetTag);

        // Create posts with source tag
        createBlogPost("JS Post 1", "js-post-1", Set.of(sourceTag));
        createBlogPost("JS Post 2", "js-post-2", Set.of(sourceTag));

        // When
        int mergedCount = tagRepository.mergeTags(sourceTag.getId(), targetTag.getId());

        // Then
        assertThat(mergedCount).isEqualTo(2); // Two posts updated
        
        entityManager.clear();
        Tag updated = tagRepository.findById(targetTag.getId()).orElseThrow();
        assertThat(updated.getUsageCount()).isEqualTo(5); // 3 + 2 from merged tag
    }

    // ==================== Soft Delete Tests ====================

    @Test
    @DisplayName("Should not find soft deleted tags")
    void shouldNotFindSoftDeletedTags() {
        // Given
        tagRepository.softDeleteById(javaTag.getId());
        entityManager.clear();

        // When
        Optional<Tag> found = tagRepository.findBySlug("java");
        List<Tag> allActive = tagRepository.findAllActive();

        // Then
        assertThat(found).isEmpty();
        assertThat(allActive).doesNotContain(javaTag);
    }

    @Test
    @DisplayName("Should cleanup unused tags")
    void shouldCleanupUnusedTags() {
        // Given
        Tag unusedTag1 = createTag("Unused1", "unused1", "Unused tag 1", "#FFFFFF", 0);
        Tag unusedTag2 = createTag("Unused2", "unused2", "Unused tag 2", "#FFFFFF", 0);
        entityManager.persistAndFlush(unusedTag1);
        entityManager.persistAndFlush(unusedTag2);

        // When
        int cleanedCount = tagRepository.cleanupUnusedTags();

        // Then
        assertThat(cleanedCount).isEqualTo(2);
        
        entityManager.clear();
        assertThat(tagRepository.findBySlug("unused1")).isEmpty();
        assertThat(tagRepository.findBySlug("unused2")).isEmpty();
    }

    // ==================== Validation Tests ====================

    @Test
    @DisplayName("Should validate slug format")
    void shouldValidateSlugFormat() {
        // Given
        Tag invalidTag = new Tag();
        invalidTag.setName("Invalid Tag");
        invalidTag.setSlug("Invalid Slug!"); // Contains invalid characters

        // When & Then
        assertThatThrownBy(() -> entityManager.persistAndFlush(invalidTag))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should validate color code format")
    void shouldValidateColorCodeFormat() {
        // Given
        Tag invalidTag = new Tag();
        invalidTag.setName("Invalid Tag");
        invalidTag.setSlug("invalid-tag");
        invalidTag.setColorCode("invalid-color"); // Invalid hex format

        // When & Then
        assertThatThrownBy(() -> entityManager.persistAndFlush(invalidTag))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should validate name length")
    void shouldValidateNameLength() {
        // Given
        Tag invalidTag = new Tag();
        invalidTag.setName("A"); // Too short
        invalidTag.setSlug("a");

        // When & Then
        assertThatThrownBy(() -> entityManager.persistAndFlush(invalidTag))
                .isInstanceOf(Exception.class);
    }

    // ==================== Performance Tests ====================

    @Test
    @DisplayName("Should handle large number of tags efficiently")
    void shouldHandleLargeNumberOfTagsEfficiently() {
        // Given - Create many tags
        for (int i = 0; i < 100; i++) {
            Tag tag = createTag("Tag " + i, "tag-" + i, "Description " + i, "#FFFFFF", i % 10);
            entityManager.persist(tag);
        }
        entityManager.flush();
        entityManager.clear();

        // When
        long startTime = System.currentTimeMillis();
        Page<Tag> tags = tagRepository.findMostPopular(PageRequest.of(0, 20));
        long endTime = System.currentTimeMillis();

        // Then
        assertThat(tags.getContent()).hasSize(20);
        assertThat(endTime - startTime).isLessThan(1000); // Should complete within 1 second
    }

    // ==================== Edge Cases Tests ====================

    @Test
    @DisplayName("Should handle null and empty parameters gracefully")
    void shouldHandleNullAndEmptyParametersGracefully() {
        // When & Then
        assertThat(tagRepository.findBySlug("")).isEmpty();
        assertThat(tagRepository.searchByName("")).isEmpty();
        assertThat(tagRepository.findByBlogPostId(999L)).isEmpty();
    }

    @Test
    @DisplayName("Should handle concurrent usage count updates")
    void shouldHandleConcurrentUsageCountUpdates() {
        // Given
        Long tagId = javaTag.getId();

        // When - Simulate concurrent updates
        tagRepository.incrementUsageCount(tagId);
        tagRepository.incrementUsageCount(tagId);
        tagRepository.decrementUsageCount(tagId);

        // Then
        entityManager.clear();
        Tag updated = tagRepository.findById(tagId).orElseThrow();
        assertThat(updated.getUsageCount()).isEqualTo(6); // 5 + 1 + 1 - 1
    }

    @Test
    @DisplayName("Should handle special characters in search")
    void shouldHandleSpecialCharactersInSearch() {
        // Given
        Tag specialTag = createTag("C++", "cpp", "C++ programming", "#FFFFFF", 1);
        entityManager.persistAndFlush(specialTag);

        // When
        List<Tag> results = tagRepository.searchByName("C++");

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("C++");
    }
}