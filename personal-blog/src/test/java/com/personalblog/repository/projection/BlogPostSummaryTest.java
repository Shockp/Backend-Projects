package com.personalblog.repository.projection;

import com.personalblog.entity.BlogPost;
import com.personalblog.entity.Category;
import com.personalblog.entity.User;
import com.personalblog.repository.BlogPostRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for BlogPostSummary projection interface.
 * Tests the projection methods in BlogPostRepository to ensure
 * proper data mapping and query performance.
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("BlogPostSummary Projection Tests")
class BlogPostSummaryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BlogPostRepository blogPostRepository;

    private User testAuthor;
    private Category testCategory;
    private BlogPost publishedPost;
    private BlogPost draftPost;

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

        // Create published blog post
        publishedPost = new BlogPost();
        publishedPost.setTitle("Published Test Post");
        publishedPost.setSlug("published-test-post");
        publishedPost.setContent("This is the content of the published test post.");
        publishedPost.setExcerpt("This is the excerpt of the published test post.");
        publishedPost.setStatus(BlogPost.Status.PUBLISHED);
        publishedPost.setAuthor(testAuthor);
        publishedPost.setCategory(testCategory);
        publishedPost.setPublishedDate(LocalDateTime.now().minusDays(1));
        publishedPost.setViewCount(100L);
        publishedPost.setReadingTimeMinutes(5);
        publishedPost.setFeaturedImageUrl("https://example.com/image.jpg");
        publishedPost = entityManager.persistAndFlush(publishedPost);

        // Create draft blog post
        draftPost = new BlogPost();
        draftPost.setTitle("Draft Test Post");
        draftPost.setSlug("draft-test-post");
        draftPost.setContent("This is the content of the draft test post.");
        draftPost.setExcerpt("This is the excerpt of the draft test post.");
        draftPost.setStatus(BlogPost.Status.DRAFT);
        draftPost.setAuthor(testAuthor);
        draftPost.setCategory(testCategory);
        draftPost.setViewCount(0L);
        draftPost.setReadingTimeMinutes(3);
        draftPost = entityManager.persistAndFlush(draftPost);

        entityManager.clear();
    }

    @Test
    @DisplayName("Should find published post summaries with correct projection data")
    void shouldFindPublishedPostSummariesWithCorrectData() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<BlogPostSummary> summaries = blogPostRepository.findPublishedPostSummaries(pageable);

        // Then
        assertThat(summaries).isNotNull();
        assertThat(summaries.getContent()).hasSize(1);
        
        BlogPostSummary summary = summaries.getContent().get(0);
        assertThat(summary.getId()).isEqualTo(publishedPost.getId());
        assertThat(summary.getTitle()).isEqualTo("Published Test Post");
        assertThat(summary.getSlug()).isEqualTo("published-test-post");
        assertThat(summary.getStatus()).isEqualTo(BlogPost.Status.PUBLISHED);
        assertThat(summary.getViewCount()).isEqualTo(100);
        assertThat(summary.getReadingTimeMinutes()).isEqualTo(5);
        assertThat(summary.getExcerpt()).isEqualTo("This is the excerpt of the published test post.");
        assertThat(summary.getFeaturedImageUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(summary.getAuthorUsername()).isEqualTo("testauthor");
        assertThat(summary.getAuthorDisplayName()).isEqualTo("testauthor"); // Using username as display name
        assertThat(summary.getCategoryName()).isEqualTo("Test Category");
        assertThat(summary.getCategorySlug()).isEqualTo("test-category");
        assertThat(summary.getCreatedAt()).isNotNull();
        assertThat(summary.getPublishedDate()).isNotNull();
    }

    @Test
    @DisplayName("Should find post summaries by category slug")
    void shouldFindPostSummariesByCategorySlug() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<BlogPostSummary> summaries = blogPostRepository.findPostSummariesByCategory("test-category", pageable);

        // Then
        assertThat(summaries).isNotNull();
        assertThat(summaries.getContent()).hasSize(1);
        
        BlogPostSummary summary = summaries.getContent().get(0);
        assertThat(summary.getCategorySlug()).isEqualTo("test-category");
        assertThat(summary.getStatus()).isEqualTo(BlogPost.Status.PUBLISHED);
    }

    @Test
    @DisplayName("Should find post summaries by author username")
    void shouldFindPostSummariesByAuthorUsername() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<BlogPostSummary> summaries = blogPostRepository.findPostSummariesByAuthor("testauthor", pageable);

        // Then
        assertThat(summaries).isNotNull();
        assertThat(summaries.getContent()).hasSize(1);
        
        BlogPostSummary summary = summaries.getContent().get(0);
        assertThat(summary.getAuthorUsername()).isEqualTo("testauthor");
        assertThat(summary.getStatus()).isEqualTo(BlogPost.Status.PUBLISHED);
    }

    @Test
    @DisplayName("Should search post summaries by title")
    void shouldSearchPostSummariesByTitle() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<BlogPostSummary> summaries = blogPostRepository.searchPostSummaries("Published", pageable);

        // Then
        assertThat(summaries).isNotNull();
        assertThat(summaries.getContent()).hasSize(1);
        
        BlogPostSummary summary = summaries.getContent().get(0);
        assertThat(summary.getTitle()).contains("Published");
        assertThat(summary.getStatus()).isEqualTo(BlogPost.Status.PUBLISHED);
    }

    @Test
    @DisplayName("Should search post summaries by excerpt")
    void shouldSearchPostSummariesByExcerpt() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<BlogPostSummary> summaries = blogPostRepository.searchPostSummaries("excerpt", pageable);

        // Then
        assertThat(summaries).isNotNull();
        assertThat(summaries.getContent()).hasSize(1);
        
        BlogPostSummary summary = summaries.getContent().get(0);
        assertThat(summary.getExcerpt()).contains("excerpt");
    }

    @Test
    @DisplayName("Should find recent post summaries")
    void shouldFindRecentPostSummaries() {
        // Given
        LocalDateTime since = LocalDateTime.now().minusDays(2);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<BlogPostSummary> summaries = blogPostRepository.findRecentPostSummaries(since, pageable);

        // Then
        assertThat(summaries).isNotNull();
        assertThat(summaries.getContent()).hasSize(1);
        
        BlogPostSummary summary = summaries.getContent().get(0);
        assertThat(summary.getPublishedDate()).isAfter(since);
        assertThat(summary.getStatus()).isEqualTo(BlogPost.Status.PUBLISHED);
    }

    @Test
    @DisplayName("Should not include draft posts in published summaries")
    void shouldNotIncludeDraftPostsInPublishedSummaries() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<BlogPostSummary> summaries = blogPostRepository.findPublishedPostSummaries(pageable);

        // Then
        assertThat(summaries).isNotNull();
        assertThat(summaries.getContent()).hasSize(1);
        assertThat(summaries.getContent().get(0).getStatus()).isEqualTo(BlogPost.Status.PUBLISHED);
    }

    @Test
    @DisplayName("Should return empty page when no posts match criteria")
    void shouldReturnEmptyPageWhenNoPostsMatchCriteria() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<BlogPostSummary> summaries = blogPostRepository.findPostSummariesByCategory("nonexistent-category", pageable);

        // Then
        assertThat(summaries).isNotNull();
        assertThat(summaries.getContent()).isEmpty();
        assertThat(summaries.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void shouldHandlePaginationCorrectly() {
        // Given - Create additional published posts
        for (int i = 1; i <= 5; i++) {
            BlogPost post = new BlogPost();
            post.setTitle("Additional Post " + i);
            post.setSlug("additional-post-" + i);
            post.setContent("Content " + i);
            post.setExcerpt("Excerpt " + i);
            post.setStatus(BlogPost.Status.PUBLISHED);
            post.setAuthor(testAuthor);
            post.setCategory(testCategory);
            post.setPublishedDate(LocalDateTime.now().minusDays(i));
            post.setViewCount((long) (i * 10));
            post.setReadingTimeMinutes(i);
            entityManager.persistAndFlush(post);
        }
        entityManager.clear();

        Pageable firstPage = PageRequest.of(0, 3);
        Pageable secondPage = PageRequest.of(1, 3);

        // When
        Page<BlogPostSummary> firstPageSummaries = blogPostRepository.findPublishedPostSummaries(firstPage);
        Page<BlogPostSummary> secondPageSummaries = blogPostRepository.findPublishedPostSummaries(secondPage);

        // Then
        assertThat(firstPageSummaries.getContent()).hasSize(3);
        assertThat(firstPageSummaries.getTotalElements()).isEqualTo(6);
        assertThat(firstPageSummaries.getTotalPages()).isEqualTo(2);
        assertThat(firstPageSummaries.isFirst()).isTrue();
        assertThat(firstPageSummaries.hasNext()).isTrue();

        assertThat(secondPageSummaries.getContent()).hasSize(3);
        assertThat(secondPageSummaries.getTotalElements()).isEqualTo(6);
        assertThat(secondPageSummaries.isLast()).isTrue();
        assertThat(secondPageSummaries.hasPrevious()).isTrue();
    }
}