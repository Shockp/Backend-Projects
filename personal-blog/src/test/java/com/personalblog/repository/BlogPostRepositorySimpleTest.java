package com.personalblog.repository;

import com.personalblog.entity.BlogPost;
import com.personalblog.entity.Category;
import com.personalblog.entity.Tag;
import com.personalblog.entity.User;
import com.personalblog.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Simple test suite for BlogPostRepository focusing on core functionality.
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @since 1.0.0
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@DisplayName("BlogPostRepository Simple Tests")
class BlogPostRepositorySimpleTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BlogPostRepository blogPostRepository;

    private User testAuthor;
    private Category testCategory;
    private Tag testTag;
    private BlogPost publishedPost;
    private BlogPost draftPost;

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

        // Create test tag
        testTag = new Tag();
        testTag.setName("Java");
        testTag.setSlug("java");
        testTag.setUsageCount(5);
        testTag = entityManager.persistAndFlush(testTag);

        // Create test blog posts
        publishedPost = createBlogPost("Published Post", "published-post", 
            BlogPost.Status.PUBLISHED, LocalDateTime.now().minusDays(1));
        publishedPost.setViewCount(100L);
        publishedPost = entityManager.persistAndFlush(publishedPost);

        draftPost = createBlogPost("Draft Post", "draft-post", 
            BlogPost.Status.DRAFT, null);
        draftPost = entityManager.persistAndFlush(draftPost);

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
        post.setTags(new HashSet<>(Arrays.asList(testTag)));
        post.setPublishedDate(publishedDate);
        post.setMetaTitle("Meta " + title);
        post.setMetaDescription("Meta description for " + title);
        post.setReadingTimeMinutes(5);
        return post;
    }

    @Test
    @DisplayName("Should find blog posts by status")
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
    @DisplayName("Should search by title or content")
    void shouldSearchByTitleOrContent() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<BlogPost> titleResults = blogPostRepository.searchByTitleOrContent("Published", pageable);

        // Then
        assertThat(titleResults.getContent()).hasSize(1);
        assertThat(titleResults.getContent().get(0).getTitle()).contains("Published");
    }

    @Test
    @DisplayName("Should count posts by status")
    void shouldCountPostsByStatus() {
        // When
        long publishedCount = blogPostRepository.countByStatus(BlogPost.Status.PUBLISHED);
        long draftCount = blogPostRepository.countByStatus(BlogPost.Status.DRAFT);

        // Then
        assertThat(publishedCount).isEqualTo(1);
        assertThat(draftCount).isEqualTo(1);
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
    @DisplayName("Should find posts by author and status")
    void shouldFindPostsByAuthorAndStatus() {
        // When
        List<BlogPost> authorDrafts = blogPostRepository.findByStatusAndAuthorIdAndDeletedFalse(
            BlogPost.Status.DRAFT, testAuthor.getId());

        // Then
        assertThat(authorDrafts).hasSize(1);
        assertThat(authorDrafts.get(0).getTitle()).isEqualTo("Draft Post");
        assertThat(authorDrafts.get(0).getAuthor().getId()).isEqualTo(testAuthor.getId());
    }
}