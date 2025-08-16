package com.personalblog.repository;

import com.personalblog.entity.BlogPost;
import com.personalblog.entity.Category;
import com.personalblog.entity.Tag;
import com.personalblog.entity.User;
import com.personalblog.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for BlogPostRepository.
 * 
 * <p>
 * This test class covers all query methods in BlogPostRepository including:
 * </p>
 * <ul>
 * <li>Status-based queries</li>
 * <li>Content search functionality</li>
 * <li>SEO and routing queries</li>
 * <li>Category and tag-based filtering</li>
 * <li>Author-based queries</li>
 * <li>Analytics and statistics</li>
 * <li>Bulk operations</li>
 * <li>Validation and edge cases</li>
 * </ul>
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @since 1.0.0
 */
@DataJpaTest(excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration.class,
    org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration.class
})
@ActiveProfiles("test")
@DisplayName("BlogPostRepository Tests")
class BlogPostRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BlogPostRepository blogPostRepository;

    private User testAuthor;
    private Category testCategory;
    private Tag testTag1;
    private Tag testTag2;
    private BlogPost publishedPost;
    private BlogPost draftPost;
    private BlogPost archivedPost;

    @BeforeEach
    void setUp() {
        // Create test author
        testAuthor = new User();
        testAuthor.setUsername("testauthor");
        testAuthor.setEmail("test@example.com");
        testAuthor.setPassword("hashedpassword");
        testAuthor.setAccountEnabled(true);
        testAuthor.setEmailVerified(true);
        testAuthor.setRoles(Set.of(Role.AUTHOR));
        testAuthor = entityManager.persistAndFlush(testAuthor);

        // Create test category
        testCategory = new Category();
        testCategory.setName("Technology");
        testCategory.setSlug("technology");
        testCategory.setDescription("Technology related posts");
        testCategory.setDisplayOrder(1);
        testCategory = entityManager.persistAndFlush(testCategory);

        // Create test tags
        testTag1 = new Tag();
        testTag1.setName("Java");
        testTag1.setSlug("java");
        testTag1.setUsageCount(5);
        testTag1 = entityManager.persistAndFlush(testTag1);

        testTag2 = new Tag();
        testTag2.setName("Spring");
        testTag2.setSlug("spring");
        testTag2.setUsageCount(3);
        testTag2 = entityManager.persistAndFlush(testTag2);

        // Create test blog posts
        publishedPost = createBlogPost("Published Post", "published-post",
                BlogPost.Status.PUBLISHED, LocalDateTime.now().minusDays(1));
        publishedPost.setViewCount(100L);
        publishedPost.setFeaturedImageUrl("https://example.com/image.jpg");
        publishedPost = entityManager.persistAndFlush(publishedPost);

        draftPost = createBlogPost("Draft Post", "draft-post",
                BlogPost.Status.DRAFT, null);
        draftPost = entityManager.persistAndFlush(draftPost);

        archivedPost = createBlogPost("Archived Post", "archived-post",
                BlogPost.Status.ARCHIVED, LocalDateTime.now().minusDays(30));
        archivedPost = entityManager.persistAndFlush(archivedPost);

        entityManager.clear();
    }

    private BlogPost createBlogPost(String title, String slug, BlogPost.Status status, LocalDateTime publishedDate) {
        BlogPost post = new BlogPost();
        post.setTitle(title);
        post.setSlug(slug);
        post.setContent("This is the content for " + title);
        post.setExcerpt("This is the excerpt for " + title);
        post.setStatus(status);
        post.setAuthor(testAuthor);
        post.setCategory(testCategory);
        post.setTags(new HashSet<>(Arrays.asList(testTag1, testTag2)));
        post.setPublishedDate(publishedDate);
        post.setMetaTitle("Meta " + title);
        post.setMetaDescription("Meta description for " + title);
        post.setReadingTimeMinutes(5);
        return post;
    }

    @Nested
    @DisplayName("Status-Based Queries")
    class StatusBasedQueries {

        @Test
        @DisplayName("Should find blog posts by status with pagination")
        void shouldFindBlogPostsByStatus() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<BlogPost> publishedPosts = blogPostRepository.findByStatusAndDeletedFalse(
                    BlogPost.Status.PUBLISHED, pageable);
            Page<BlogPost> draftPosts = blogPostRepository.findByStatusAndDeletedFalse(
                    BlogPost.Status.DRAFT, pageable);

            // Then
            assertThat(publishedPosts.getContent()).hasSize(1);
            assertThat(publishedPosts.getContent().get(0).getTitle()).isEqualTo("Published Post");

            assertThat(draftPosts.getContent()).hasSize(1);
            assertThat(draftPosts.getContent().get(0).getTitle()).isEqualTo("Draft Post");
        }

        @Test
        @DisplayName("Should find blog posts by status and author")
        void shouldFindBlogPostsByStatusAndAuthor() {
            // When
            List<BlogPost> authorDrafts = blogPostRepository.findByStatusAndAuthorIdAndDeletedFalse(
                    BlogPost.Status.DRAFT, testAuthor.getId());

            // Then
            assertThat(authorDrafts).hasSize(1);
            assertThat(authorDrafts.get(0).getTitle()).isEqualTo("Draft Post");
            assertThat(authorDrafts.get(0).getAuthor().getId()).isEqualTo(testAuthor.getId());
        }

        @Test
        @DisplayName("Should find published posts with details using entity graph")
        void shouldFindPublishedPostsWithDetails() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<BlogPost> publishedPosts = blogPostRepository.findPublishedPostsWithDetails(pageable);

            // Then
            assertThat(publishedPosts.getContent()).hasSize(1);
            BlogPost post = publishedPosts.getContent().get(0);
            assertThat(post.getTitle()).isEqualTo("Published Post");
            assertThat(post.getStatus()).isEqualTo(BlogPost.Status.PUBLISHED);
            // Entity graph should load relationships
            assertThat(post.getAuthor()).isNotNull();
            assertThat(post.getCategory()).isNotNull();
        }

        @Test
        @DisplayName("Should find drafts by author")
        void shouldFindDraftsByAuthor() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<BlogPost> drafts = blogPostRepository.findDraftsByAuthor(testAuthor.getId(), pageable);

            // Then
            assertThat(drafts.getContent()).hasSize(1);
            assertThat(drafts.getContent().get(0).getStatus()).isEqualTo(BlogPost.Status.DRAFT);
            assertThat(drafts.getContent().get(0).getAuthor().getId()).isEqualTo(testAuthor.getId());
        }

        @Test
        @DisplayName("Should not find deleted posts")
        void shouldNotFindDeletedPosts() {
            // Given
            publishedPost.markAsDeleted();
            entityManager.persistAndFlush(publishedPost);
            entityManager.clear();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<BlogPost> publishedPosts = blogPostRepository.findByStatusAndDeletedFalse(
                    BlogPost.Status.PUBLISHED, pageable);

            // Then
            assertThat(publishedPosts.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Content Search Queries")
    class ContentSearchQueries {

        @Test
        @DisplayName("Should search by title or content")
        void shouldSearchByTitleOrContent() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<BlogPost> titleResults = blogPostRepository.searchByTitleOrContent("Published", pageable);
            Page<BlogPost> contentResults = blogPostRepository.searchByTitleOrContent("content", pageable);

            // Then
            assertThat(titleResults.getContent()).hasSize(1);
            assertThat(titleResults.getContent().get(0).getTitle()).contains("Published");

            assertThat(contentResults.getContent()).hasSize(1);
            assertThat(contentResults.getContent().get(0).getContent()).contains("content");
        }

        @Test
        @DisplayName("Should perform advanced search across multiple fields")
        void shouldPerformAdvancedSearch() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<BlogPost> metaTitleResults = blogPostRepository.advancedSearch(
                    "Meta", BlogPost.Status.PUBLISHED, pageable);
            Page<BlogPost> allStatusResults = blogPostRepository.advancedSearch(
                    "Post", null, pageable);

            // Then
            assertThat(metaTitleResults.getContent()).hasSize(1);
            assertThat(metaTitleResults.getContent().get(0).getMetaTitle()).contains("Meta");

            assertThat(allStatusResults.getContent()).hasSize(3); // All posts contain "Post"
        }

        @Test
        @DisplayName("Should handle case-insensitive search")
        void shouldHandleCaseInsensitiveSearch() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<BlogPost> upperCaseResults = blogPostRepository.searchByTitleOrContent("PUBLISHED", pageable);
            Page<BlogPost> lowerCaseResults = blogPostRepository.searchByTitleOrContent("published", pageable);

            // Then
            assertThat(upperCaseResults.getContent()).hasSize(1);
            assertThat(lowerCaseResults.getContent()).hasSize(1);
            assertThat(upperCaseResults.getContent().get(0).getId())
                    .isEqualTo(lowerCaseResults.getContent().get(0).getId());
        }

        @Test
        @DisplayName("Should validate search term length")
        void shouldValidateSearchTermLength() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When & Then
            assertThatThrownBy(() -> blogPostRepository.searchByTitleOrContent("a", pageable))
                    .isInstanceOf(ConstraintViolationException.class);
        }
    }

    @Nested
    @DisplayName("SEO and Routing Queries")
    class SeoAndRoutingQueries {

        @Test
        @DisplayName("Should find published post by slug")
        void shouldFindPublishedPostBySlug() {
            // When
            Optional<BlogPost> result = blogPostRepository.findBySlugAndPublished("published-post");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getTitle()).isEqualTo("Published Post");
            assertThat(result.get().getStatus()).isEqualTo(BlogPost.Status.PUBLISHED);
        }

        @Test
        @DisplayName("Should not find draft post by slug in published query")
        void shouldNotFindDraftPostBySlugInPublishedQuery() {
            // When
            Optional<BlogPost> result = blogPostRepository.findBySlugAndPublished("draft-post");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find any post by slug regardless of status")
        void shouldFindAnyPostBySlugRegardlessOfStatus() {
            // When
            Optional<BlogPost> publishedResult = blogPostRepository.findBySlugAndDeletedFalse("published-post");
            Optional<BlogPost> draftResult = blogPostRepository.findBySlugAndDeletedFalse("draft-post");

            // Then
            assertThat(publishedResult).isPresent();
            assertThat(draftResult).isPresent();
        }

        @Test
        @DisplayName("Should check if slug exists")
        void shouldCheckIfSlugExists() {
            // When
            boolean existingSlug = blogPostRepository.existsBySlugAndDeletedFalse("published-post");
            boolean nonExistingSlug = blogPostRepository.existsBySlugAndDeletedFalse("non-existing");

            // Then
            assertThat(existingSlug).isTrue();
            assertThat(nonExistingSlug).isFalse();
        }

        @Test
        @DisplayName("Should check slug existence excluding specific post")
        void shouldCheckSlugExistenceExcludingSpecificPost() {
            // When
            boolean existsForOtherPost = blogPostRepository.existsBySlugAndIdNotAndDeletedFalse(
                    "published-post", draftPost.getId());
            boolean existsForSamePost = blogPostRepository.existsBySlugAndIdNotAndDeletedFalse(
                    "published-post", publishedPost.getId());

            // Then
            assertThat(existsForOtherPost).isTrue();
            assertThat(existsForSamePost).isFalse();
        }
    }

    @Nested
    @DisplayName("Category-Based Queries")
    class CategoryBasedQueries {

        @Test
        @DisplayName("Should find posts by category ID and status")
        void shouldFindPostsByCategoryIdAndStatus() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<BlogPost> publishedInCategory = blogPostRepository.findByCategoryIdAndStatus(
                    testCategory.getId(), BlogPost.Status.PUBLISHED, pageable);

            // Then
            assertThat(publishedInCategory.getContent()).hasSize(1);
            assertThat(publishedInCategory.getContent().get(0).getCategory().getId())
                    .isEqualTo(testCategory.getId());
        }

        @Test
        @DisplayName("Should find published posts by category slug")
        void shouldFindPublishedPostsByCategorySlug() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<BlogPost> postsInCategory = blogPostRepository.findPublishedByCategorySlug(
                    "technology", pageable);

            // Then
            assertThat(postsInCategory.getContent()).hasSize(1);
            assertThat(postsInCategory.getContent().get(0).getCategory().getSlug())
                    .isEqualTo("technology");
        }

        @Test
        @DisplayName("Should not find posts in deleted category")
        void shouldNotFindPostsInDeletedCategory() {
            // Given
            testCategory.markAsDeleted();
            entityManager.persistAndFlush(testCategory);
            entityManager.clear();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<BlogPost> postsInCategory = blogPostRepository.findPublishedByCategorySlug(
                    "technology", pageable);

            // Then
            assertThat(postsInCategory.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tag-Based Queries")
    class TagBasedQueries {

        @Test
        @DisplayName("Should find posts by tag IDs (OR operation)")
        void shouldFindPostsByTagIds() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            List<Long> tagIds = Arrays.asList(testTag1.getId());

            // When
            Page<BlogPost> postsWithTags = blogPostRepository.findByTagIdsAndStatus(
                    tagIds, BlogPost.Status.PUBLISHED, pageable);

            // Then
            assertThat(postsWithTags.getContent()).hasSize(1);
            assertThat(postsWithTags.getContent().get(0).getTags())
                    .extracting(Tag::getId)
                    .contains(testTag1.getId());
        }

        @Test
        @DisplayName("Should find published posts by tag slug")
        void shouldFindPublishedPostsByTagSlug() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<BlogPost> postsWithTag = blogPostRepository.findPublishedByTagSlug("java", pageable);

            // Then
            assertThat(postsWithTag.getContent()).hasSize(1);
            assertThat(postsWithTag.getContent().get(0).getTags())
                    .extracting(Tag::getSlug)
                    .contains("java");
        }

        @Test
        @DisplayName("Should find posts with all specified tags (AND operation)")
        void shouldFindPostsWithAllSpecifiedTags() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            List<Long> tagIds = Arrays.asList(testTag1.getId(), testTag2.getId());

            // When
            Page<BlogPost> postsWithAllTags = blogPostRepository.findByAllTagIdsAndStatus(
                    tagIds, tagIds.size(), BlogPost.Status.PUBLISHED, pageable);

            // Then
            assertThat(postsWithAllTags.getContent()).hasSize(1);
            assertThat(postsWithAllTags.getContent().get(0).getTags()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Author-Based Queries")
    class AuthorBasedQueries {

        @Test
        @DisplayName("Should find posts by author ID and status")
        void shouldFindPostsByAuthorIdAndStatus() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<BlogPost> authorPosts = blogPostRepository.findByAuthorIdAndStatus(
                    testAuthor.getId(), BlogPost.Status.PUBLISHED, pageable);
            Page<BlogPost> allAuthorPosts = blogPostRepository.findByAuthorIdAndStatus(
                    testAuthor.getId(), null, pageable);

            // Then
            assertThat(authorPosts.getContent()).hasSize(1);
            assertThat(allAuthorPosts.getContent()).hasSize(3); // All statuses
        }

        @Test
        @DisplayName("Should find published posts by author username")
        void shouldFindPublishedPostsByAuthorUsername() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<BlogPost> authorPosts = blogPostRepository.findPublishedByAuthorUsername(
                    "testauthor", pageable);

            // Then
            assertThat(authorPosts.getContent()).hasSize(1);
            assertThat(authorPosts.getContent().get(0).getAuthor().getUsername())
                    .isEqualTo("testauthor");
        }
    }

    @Nested
    @DisplayName("Analytics and Statistics")
    class AnalyticsAndStatistics {

        @Test
        @DisplayName("Should count posts by status")
        void shouldCountPostsByStatus() {
            // When
            long publishedCount = blogPostRepository.countByStatus(BlogPost.Status.PUBLISHED);
            long draftCount = blogPostRepository.countByStatus(BlogPost.Status.DRAFT);
            long archivedCount = blogPostRepository.countByStatus(BlogPost.Status.ARCHIVED);

            // Then
            assertThat(publishedCount).isEqualTo(1);
            assertThat(draftCount).isEqualTo(1);
            assertThat(archivedCount).isEqualTo(1);
        }

        @Test
        @DisplayName("Should count posts by author and date range")
        void shouldCountPostsByAuthorAndDateRange() {
            // Given
            LocalDateTime startDate = LocalDateTime.now().minusDays(2);
            LocalDateTime endDate = LocalDateTime.now().plusDays(1);

            // When
            long count = blogPostRepository.countByAuthorAndDateRange(
                    testAuthor.getId(), startDate, endDate);

            // Then
            assertThat(count).isEqualTo(3); // All posts created in this range
        }

        @Test
        @DisplayName("Should count published posts by category")
        void shouldCountPublishedPostsByCategory() {
            // When
            long count = blogPostRepository.countPublishedByCategory(testCategory.getId());

            // Then
            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("Should find most viewed posts")
        void shouldFindMostViewedPosts() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<BlogPost> mostViewed = blogPostRepository.findMostViewedPosts(pageable);

            // Then
            assertThat(mostViewed.getContent()).hasSize(1);
            assertThat(mostViewed.getContent().get(0).getViewCount()).isEqualTo(100L);
        }

        @Test
        @DisplayName("Should find recently published posts")
        void shouldFindRecentlyPublishedPosts() {
            // Given
            LocalDateTime since = LocalDateTime.now().minusDays(2);
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<BlogPost> recentPosts = blogPostRepository.findRecentlyPublished(since, pageable);

            // Then
            assertThat(recentPosts.getContent()).hasSize(1);
            assertThat(recentPosts.getContent().get(0).getTitle()).isEqualTo("Published Post");
        }

        @Test
        @DisplayName("Should find featured posts")
        void shouldFindFeaturedPosts() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<BlogPost> featuredPosts = blogPostRepository.findFeaturedPosts(pageable);

            // Then
            assertThat(featuredPosts.getContent()).hasSize(1);
            assertThat(featuredPosts.getContent().get(0).getFeaturedImageUrl()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Bulk Operations")
    class BulkOperations {

        @Test
        @DisplayName("Should increment view count")
        void shouldIncrementViewCount() {
            // Given
            Long initialViewCount = publishedPost.getViewCount();

            // When
            int updatedRows = blogPostRepository.incrementViewCount(publishedPost.getId());
            entityManager.clear();
            BlogPost updatedPost = entityManager.find(BlogPost.class, publishedPost.getId());

            // Then
            assertThat(updatedRows).isEqualTo(1);
            assertThat(updatedPost.getViewCount()).isEqualTo(initialViewCount + 1);
        }

        @Test
        @DisplayName("Should bulk update post status")
        void shouldBulkUpdatePostStatus() {
            // Given
            List<Long> postIds = Arrays.asList(draftPost.getId());

            // When
            int updatedRows = blogPostRepository.bulkUpdateStatus(postIds, BlogPost.Status.PUBLISHED);
            entityManager.clear();
            BlogPost updatedPost = entityManager.find(BlogPost.class, draftPost.getId());

            // Then
            assertThat(updatedRows).isEqualTo(1);
            assertThat(updatedPost.getStatus()).isEqualTo(BlogPost.Status.PUBLISHED);
        }

        @Test
        @DisplayName("Should publish posts")
        void shouldPublishPosts() {
            // Given
            List<Long> postIds = Arrays.asList(draftPost.getId());

            // When
            int updatedRows = blogPostRepository.publishPosts(postIds);
            entityManager.clear();
            BlogPost updatedPost = entityManager.find(BlogPost.class, draftPost.getId());

            // Then
            assertThat(updatedRows).isEqualTo(1);
            assertThat(updatedPost.getStatus()).isEqualTo(BlogPost.Status.PUBLISHED);
            assertThat(updatedPost.getPublishedDate()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Utility and Validation Methods")
    class UtilityAndValidationMethods {

        @Test
        @DisplayName("Should check if author has posts")
        void shouldCheckIfAuthorHasPosts() {
            // When
            boolean hasPostsExisting = blogPostRepository.existsByAuthorId(testAuthor.getId());
            boolean hasPostsNonExisting = blogPostRepository.existsByAuthorId(999L);

            // Then
            assertThat(hasPostsExisting).isTrue();
            assertThat(hasPostsNonExisting).isFalse();
        }

        @Test
        @DisplayName("Should find scheduled posts")
        void shouldFindScheduledPosts() {
            // Given
            BlogPost scheduledPost = createBlogPost("Scheduled Post", "scheduled-post",
                    BlogPost.Status.PUBLISHED, LocalDateTime.now().plusDays(1));
            entityManager.persistAndFlush(scheduledPost);
            entityManager.clear();
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<BlogPost> scheduledPosts = blogPostRepository.findScheduledPosts(pageable);

            // Then
            assertThat(scheduledPosts.getContent()).hasSize(1);
            assertThat(scheduledPosts.getContent().get(0).getTitle()).isEqualTo("Scheduled Post");
        }

        @Test
        @DisplayName("Should find posts without reading time")
        void shouldFindPostsWithoutReadingTime() {
            // Given
            BlogPost postWithoutReadingTime = createBlogPost("No Reading Time", "no-reading-time",
                    BlogPost.Status.DRAFT, null);
            postWithoutReadingTime.setReadingTimeMinutes(null);
            entityManager.persistAndFlush(postWithoutReadingTime);
            entityManager.clear();

            // When
            List<BlogPost> postsWithoutReadingTime = blogPostRepository.findPostsWithoutReadingTime();

            // Then
            assertThat(postsWithoutReadingTime).hasSize(1);
            assertThat(postsWithoutReadingTime.get(0).getReadingTimeMinutes()).isNull();
        }
    }

    @Nested
    @DisplayName("Validation and Error Handling")
    class ValidationAndErrorHandling {

        @Test
        @DisplayName("Should validate null parameters")
        void shouldValidateNullParameters() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When & Then
            assertThatThrownBy(() -> blogPostRepository.findByStatusAndDeletedFalse(null, pageable))
                    .isInstanceOf(ConstraintViolationException.class);

            assertThatThrownBy(() -> blogPostRepository.findBySlugAndPublished(null))
                    .isInstanceOf(ConstraintViolationException.class);

            assertThatThrownBy(() -> blogPostRepository.countByStatus(null))
                    .isInstanceOf(ConstraintViolationException.class);
        }

        @Test
        @DisplayName("Should validate blank strings")
        void shouldValidateBlankStrings() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When & Then
            assertThatThrownBy(() -> blogPostRepository.findBySlugAndPublished(""))
                    .isInstanceOf(ConstraintViolationException.class);

            assertThatThrownBy(() -> blogPostRepository.searchByTitleOrContent("", pageable))
                    .isInstanceOf(ConstraintViolationException.class);
        }

        @Test
        @DisplayName("Should validate string size constraints")
        void shouldValidateStringSizeConstraints() {
            // Given
            String tooLongSlug = "a".repeat(256); // Max is 255
            String tooLongSearchTerm = "a".repeat(101); // Max is 100
            Pageable pageable = PageRequest.of(0, 10);

            // When & Then
            assertThatThrownBy(() -> blogPostRepository.findBySlugAndPublished(tooLongSlug))
                    .isInstanceOf(ConstraintViolationException.class);

            assertThatThrownBy(() -> blogPostRepository.searchByTitleOrContent(tooLongSearchTerm, pageable))
                    .isInstanceOf(ConstraintViolationException.class);
        }

        @Test
        @DisplayName("Should handle empty results gracefully")
        void shouldHandleEmptyResultsGracefully() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<BlogPost> emptyResults = blogPostRepository.findByStatusAndDeletedFalse(
                    BlogPost.Status.PUBLISHED, PageRequest.of(10, 10)); // Page beyond results
            Optional<BlogPost> notFound = blogPostRepository.findBySlugAndPublished("non-existing");

            // Then
            assertThat(emptyResults.getContent()).isEmpty();
            assertThat(emptyResults.getTotalElements()).isEqualTo(1); // Still shows total
            assertThat(notFound).isEmpty();
        }
    }

    @Nested
    @DisplayName("Performance and Optimization")
    class PerformanceAndOptimization {

        @Test
        @DisplayName("Should use entity graphs for optimized loading")
        void shouldUseEntityGraphsForOptimizedLoading() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<BlogPost> postsWithDetails = blogPostRepository.findPublishedPostsWithDetails(pageable);

            // Then
            assertThat(postsWithDetails.getContent()).hasSize(1);
            BlogPost post = postsWithDetails.getContent().get(0);

            // Verify relationships are loaded (no lazy loading exceptions)
            assertThat(post.getAuthor().getUsername()).isEqualTo("testauthor");
            assertThat(post.getCategory().getName()).isEqualTo("Technology");
            assertThat(post.getTags()).isNotEmpty();
        }

        @Test
        @DisplayName("Should handle large result sets with pagination")
        void shouldHandleLargeResultSetsWithPagination() {
            // Given - Create multiple posts
            for (int i = 0; i < 25; i++) {
                BlogPost post = createBlogPost("Post " + i, "post-" + i,
                        BlogPost.Status.PUBLISHED, LocalDateTime.now().minusDays(i));
                entityManager.persist(post);
            }
            entityManager.flush();
            entityManager.clear();

            // When
            Page<BlogPost> firstPage = blogPostRepository.findByStatusAndDeletedFalse(
                    BlogPost.Status.PUBLISHED, PageRequest.of(0, 10));
            Page<BlogPost> secondPage = blogPostRepository.findByStatusAndDeletedFalse(
                    BlogPost.Status.PUBLISHED, PageRequest.of(1, 10));

            // Then
            assertThat(firstPage.getContent()).hasSize(10);
            assertThat(secondPage.getContent()).hasSize(10);
            assertThat(firstPage.getTotalElements()).isEqualTo(26); // 25 + original published post
            assertThat(firstPage.getTotalPages()).isEqualTo(3);
        }
    }
}