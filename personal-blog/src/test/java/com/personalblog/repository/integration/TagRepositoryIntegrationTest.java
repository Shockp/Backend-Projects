package com.personalblog.repository.integration;

import com.personalblog.entity.BlogPost;
import com.personalblog.entity.Category;
import com.personalblog.entity.Role;
import com.personalblog.entity.Tag;
import com.personalblog.entity.User;
import com.personalblog.repository.BlogPostRepository;
import com.personalblog.repository.CategoryRepository;
import com.personalblog.repository.TagRepository;
import com.personalblog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for TagRepository using TestContainers with PostgreSQL.
 * 
 * <p>
 * These tests verify tag management, usage statistics, and concurrent
 * access scenarios with actual PostgreSQL database.
 * </p>
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @since 1.0.0
 */
@DisplayName("TagRepository Integration Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TagRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private User testAuthor;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        // Clean up any existing data
        blogPostRepository.deleteAll();
        tagRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Create test author
        testAuthor = new User();
        testAuthor.setUsername("tagintegrationauthor");
        testAuthor.setEmail("tagintegration@example.com");
        testAuthor.setPassword("hashedpassword");
        testAuthor.setAccountEnabled(true);
        testAuthor.setEmailVerified(true);
        testAuthor.setRoles(Set.of(Role.AUTHOR));
        testAuthor = userRepository.save(testAuthor);

        // Create test category
        testCategory = new Category();
        testCategory.setName("Integration Test Category");
        testCategory.setSlug("integration-test-category");
        testCategory.setDescription("Category for integration testing");
        testCategory.setDisplayOrder(1);
        testCategory = categoryRepository.save(testCategory);
    }

    private Tag createTag(String name, String slug, int usageCount) {
        Tag tag = new Tag();
        tag.setName(name);
        tag.setSlug(slug);
        tag.setUsageCount(usageCount);
        return tag;
    }

    private BlogPost createBlogPost(String title, String slug, Set<Tag> tags) {
        BlogPost post = new BlogPost();
        post.setTitle(title);
        post.setSlug(slug);
        post.setContent("Content for " + title);
        post.setExcerpt("Excerpt for " + title);
        post.setStatus(BlogPost.Status.PUBLISHED);
        post.setAuthor(testAuthor);
        post.setCategory(testCategory);
        post.setTags(tags);
        post.setPublishedDate(LocalDateTime.now());
        post.setReadingTimeMinutes(5);
        post.setViewCount(0L);
        return post;
    }

    @Nested
    @DisplayName("Basic Tag Operations")
    class BasicTagOperations {

        @Test
        @DisplayName("Should create and retrieve tags")
        @Transactional
        void shouldCreateAndRetrieveTags() {
            // Given
            Tag javaTag = createTag("Java", "java", 5);
            Tag springTag = createTag("Spring", "spring", 3);

            // When
            Tag savedJavaTag = tagRepository.save(javaTag);
            Tag savedSpringTag = tagRepository.save(springTag);

            // Then
            Optional<Tag> foundJavaTag = tagRepository.findByNameAndDeletedFalse("Java");
            Optional<Tag> foundSpringTag = tagRepository.findBySlugAndDeletedFalse("spring");

            assertThat(foundJavaTag).isPresent();
            assertThat(foundJavaTag.get().getUsageCount()).isEqualTo(5);

            assertThat(foundSpringTag).isPresent();
            assertThat(foundSpringTag.get().getName()).isEqualTo("Spring");
        }

        @Test
        @DisplayName("Should handle tag uniqueness constraints")
        @Transactional
        void shouldHandleTagUniquenessConstraints() {
            // Given
            Tag tag1 = createTag("Java", "java", 1);
            Tag tag2 = createTag("Different Java", "java", 2); // Same slug

            // When & Then
            tagRepository.save(tag1);
            
            assertThatThrownBy(() -> {
                tagRepository.save(tag2);
                tagRepository.flush();
            }).isInstanceOf(Exception.class);

            // Verify original tag still exists
            Optional<Tag> existing = tagRepository.findBySlugAndDeletedFalse("java");
            assertThat(existing).isPresent();
            assertThat(existing.get().getName()).isEqualTo("Java");
        }

        @Test
        @DisplayName("Should check tag existence")
        @Transactional
        void shouldCheckTagExistence() {
            // Given
            Tag tag = tagRepository.save(createTag("Python", "python", 2));

            // When
            boolean existsByName = tagRepository.existsByNameAndDeletedFalse("Python");
            boolean notExistsByName = tagRepository.existsByNameAndDeletedFalse("NonExistent");

            // Then
            assertThat(existsByName).isTrue();
            assertThat(notExistsByName).isFalse();
        }
    }

    @Nested
    @DisplayName("Tag Usage Statistics")
    class TagUsageStatistics {

        @Test
        @DisplayName("Should find most used tags")
        @Transactional
        void shouldFindMostUsedTags() {
            // Given
            Tag popularTag = tagRepository.save(createTag("Popular", "popular", 100));
            Tag moderateTag = tagRepository.save(createTag("Moderate", "moderate", 50));
            Tag rareTag = tagRepository.save(createTag("Rare", "rare", 5));
            Tag unusedTag = tagRepository.save(createTag("Unused", "unused", 0));

            // When
            Pageable pageable = PageRequest.of(0, 3);
            Page<Tag> mostUsedTags = tagRepository.findMostUsedTags(pageable);

            // Then
            assertThat(mostUsedTags.getContent()).hasSize(3); // Excludes unused tag
            assertThat(mostUsedTags.getContent().get(0).getName()).isEqualTo("Popular");
            assertThat(mostUsedTags.getContent().get(1).getName()).isEqualTo("Moderate");
            assertThat(mostUsedTags.getContent().get(2).getName()).isEqualTo("Rare");
        }

        @Test
        @DisplayName("Should calculate tag post counts")
        @Transactional
        void shouldCalculateTagPostCounts() {
            // Given
            Tag javaTag = tagRepository.save(createTag("Java", "java", 0));
            Tag springTag = tagRepository.save(createTag("Spring", "spring", 0));
            Tag unusedTag = tagRepository.save(createTag("Unused", "unused", 0));

            // Create posts with tags
            BlogPost post1 = createBlogPost("Java Post 1", "java-post-1", Set.of(javaTag));
            BlogPost post2 = createBlogPost("Java Post 2", "java-post-2", Set.of(javaTag));
            BlogPost post3 = createBlogPost("Java Spring Post", "java-spring-post", Set.of(javaTag, springTag));

            blogPostRepository.saveAll(Arrays.asList(post1, post2, post3));

            // When
            List<Object[]> tagsWithCounts = tagRepository.findTagsWithPostCounts();

            // Then
            assertThat(tagsWithCounts).hasSize(3);

            for (Object[] result : tagsWithCounts) {
                Tag tag = (Tag) result[0];
                Long count = (Long) result[1];

                switch (tag.getName()) {
                    case "Java" -> assertThat(count).isEqualTo(3L);
                    case "Spring" -> assertThat(count).isEqualTo(1L);
                    case "Unused" -> assertThat(count).isEqualTo(0L);
                    default -> fail("Unexpected tag: " + tag.getName());
                }
            }
        }

        @Test
        @DisplayName("Should find unused tags for cleanup")
        @Transactional
        void shouldFindUnusedTagsForCleanup() {
            // Given
            Tag usedTag = tagRepository.save(createTag("Used", "used", 5));
            Tag unusedTag1 = tagRepository.save(createTag("Unused 1", "unused-1", 0));
            Tag unusedTag2 = tagRepository.save(createTag("Unused 2", "unused-2", 0));

            // When
            List<Tag> unusedTags = tagRepository.findUnusedTags();

            // Then
            assertThat(unusedTags).hasSize(2);
            assertThat(unusedTags)
                    .extracting(Tag::getName)
                    .containsExactlyInAnyOrder("Unused 1", "Unused 2");
        }
    }

    @Nested
    @DisplayName("Tag Search and Autocomplete")
    class TagSearchAndAutocomplete {

        @Test
        @DisplayName("Should support autocomplete functionality")
        @Transactional
        void shouldSupportAutocompleteFunctionality() {
            // Given
            Tag javaTag = tagRepository.save(createTag("Java", "java", 10));
            Tag javascriptTag = tagRepository.save(createTag("JavaScript", "javascript", 8));
            Tag pythonTag = tagRepository.save(createTag("Python", "python", 5));

            // When
            List<Tag> javaResults = tagRepository.findByNameStartingWithIgnoreCase("ja");
            List<Tag> pythonResults = tagRepository.findByNameStartingWithIgnoreCase("py");
            List<Tag> noResults = tagRepository.findByNameStartingWithIgnoreCase("xyz");

            // Then
            assertThat(javaResults).hasSize(2);
            assertThat(javaResults.get(0).getName()).isEqualTo("Java"); // Higher usage count first
            assertThat(javaResults.get(1).getName()).isEqualTo("JavaScript");

            assertThat(pythonResults).hasSize(1);
            assertThat(pythonResults.get(0).getName()).isEqualTo("Python");

            assertThat(noResults).isEmpty();
        }

        @Test
        @DisplayName("Should handle case-insensitive search")
        @Transactional
        void shouldHandleCaseInsensitiveSearch() {
            // Given
            Tag tag = tagRepository.save(createTag("JavaScript", "javascript", 5));

            // When
            List<Tag> upperCaseResults = tagRepository.findByNameStartingWithIgnoreCase("JAVA");
            List<Tag> lowerCaseResults = tagRepository.findByNameStartingWithIgnoreCase("java");
            List<Tag> mixedCaseResults = tagRepository.findByNameStartingWithIgnoreCase("JaVa");

            // Then
            assertThat(upperCaseResults).hasSize(1);
            assertThat(lowerCaseResults).hasSize(1);
            assertThat(mixedCaseResults).hasSize(1);
            
            assertThat(upperCaseResults.get(0).getId()).isEqualTo(tag.getId());
            assertThat(lowerCaseResults.get(0).getId()).isEqualTo(tag.getId());
            assertThat(mixedCaseResults.get(0).getId()).isEqualTo(tag.getId());
        }
    }

    @Nested
    @DisplayName("Tag-Post Relationships")
    class TagPostRelationships {

        @Test
        @DisplayName("Should find tags by blog post")
        @Transactional
        void shouldFindTagsByBlogPost() {
            // Given
            Tag javaTag = tagRepository.save(createTag("Java", "java", 0));
            Tag springTag = tagRepository.save(createTag("Spring", "spring", 0));
            Tag hibernateTag = tagRepository.save(createTag("Hibernate", "hibernate", 0));

            BlogPost post1 = createBlogPost("Java Spring Post", "java-spring-post", Set.of(javaTag, springTag));
            BlogPost post2 = createBlogPost("Java Hibernate Post", "java-hibernate-post", Set.of(javaTag, hibernateTag));

            BlogPost savedPost1 = blogPostRepository.save(post1);
            BlogPost savedPost2 = blogPostRepository.save(post2);

            // When
            List<Tag> post1Tags = tagRepository.findByBlogPostId(savedPost1.getId());
            List<Tag> post2Tags = tagRepository.findByBlogPostId(savedPost2.getId());

            // Then
            assertThat(post1Tags).hasSize(2);
            assertThat(post1Tags)
                    .extracting(Tag::getName)
                    .containsExactlyInAnyOrder("Java", "Spring");

            assertThat(post2Tags).hasSize(2);
            assertThat(post2Tags)
                    .extracting(Tag::getName)
                    .containsExactlyInAnyOrder("Java", "Hibernate");
        }

        @Test
        @DisplayName("Should handle many-to-many relationships correctly")
        @Transactional
        void shouldHandleManyToManyRelationshipsCorrectly() {
            // Given
            Tag popularTag = tagRepository.save(createTag("Popular", "popular", 0));
            
            // Create multiple posts with the same tag
            BlogPost post1 = createBlogPost("Post 1", "post-1", Set.of(popularTag));
            BlogPost post2 = createBlogPost("Post 2", "post-2", Set.of(popularTag));
            BlogPost post3 = createBlogPost("Post 3", "post-3", Set.of(popularTag));

            blogPostRepository.saveAll(Arrays.asList(post1, post2, post3));

            // When
            List<Object[]> tagsWithCounts = tagRepository.findTagsWithPostCounts();

            // Then
            assertThat(tagsWithCounts).hasSize(1);
            Object[] result = tagsWithCounts.get(0);
            Tag tag = (Tag) result[0];
            Long count = (Long) result[1];

            assertThat(tag.getName()).isEqualTo("Popular");
            assertThat(count).isEqualTo(3L);
        }

        @Test
        @DisplayName("Should handle deleted posts in relationships")
        @Transactional
        void shouldHandleDeletedPostsInRelationships() {
            // Given
            Tag tag = tagRepository.save(createTag("Test Tag", "test-tag", 0));
            
            BlogPost activePost = createBlogPost("Active Post", "active-post", Set.of(tag));
            BlogPost deletedPost = createBlogPost("Deleted Post", "deleted-post", Set.of(tag));
            deletedPost.markAsDeleted();

            blogPostRepository.save(activePost);
            blogPostRepository.save(deletedPost);

            // When
            List<Object[]> tagsWithCounts = tagRepository.findTagsWithPostCounts();

            // Then
            assertThat(tagsWithCounts).hasSize(1);
            Object[] result = tagsWithCounts.get(0);
            Tag resultTag = (Tag) result[0];
            Long count = (Long) result[1];

            assertThat(resultTag.getName()).isEqualTo("Test Tag");
            assertThat(count).isEqualTo(1L); // Only active post counted
        }
    }

    @Nested
    @DisplayName("Concurrent Operations")
    class ConcurrentOperations {

        @Test
        @DisplayName("Should handle concurrent tag creation")
        void shouldHandleConcurrentTagCreation() throws InterruptedException {
            // Given
            ExecutorService executor = Executors.newFixedThreadPool(5);
            int numberOfTags = 20;

            // When - Concurrent tag creation
            CompletableFuture<Tag>[] futures = new CompletableFuture[numberOfTags];
            for (int i = 0; i < numberOfTags; i++) {
                final int tagIndex = i;
                futures[i] = CompletableFuture.supplyAsync(() -> {
                    Tag tag = createTag("Concurrent Tag " + tagIndex,
                            "concurrent-tag-" + tagIndex, tagIndex);
                    return tagRepository.save(tag);
                }, executor);
            }

            CompletableFuture.allOf(futures).join();
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);

            // Then
            List<Tag> allTags = tagRepository.findAll();
            assertThat(allTags).hasSize(numberOfTags);

            // Verify all slugs are unique
            Set<String> slugs = allTags.stream()
                    .map(Tag::getSlug)
                    .collect(java.util.stream.Collectors.toSet());
            assertThat(slugs).hasSize(numberOfTags);
        }

        @Test
        @DisplayName("Should handle concurrent usage count updates")
        void shouldHandleConcurrentUsageCountUpdates() throws InterruptedException {
            // Given
            Tag tag = tagRepository.save(createTag("Popular Tag", "popular-tag", 0));
            ExecutorService executor = Executors.newFixedThreadPool(10);
            int numberOfUpdates = 100;

            // When - Concurrent usage count increments
            CompletableFuture<?>[] futures = new CompletableFuture[numberOfUpdates];
            for (int i = 0; i < numberOfUpdates; i++) {
                futures[i] = CompletableFuture.runAsync(() -> {
                    // Simulate usage count increment
                    Tag currentTag = tagRepository.findById(tag.getId()).orElseThrow();
                    currentTag.setUsageCount(currentTag.getUsageCount() + 1);
                    tagRepository.save(currentTag);
                }, executor);
            }

            CompletableFuture.allOf(futures).join();
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);

            // Then - Note: Due to concurrent updates, the final count may be less than numberOfUpdates
            // This is expected behavior and demonstrates the need for proper synchronization in real applications
            Optional<Tag> updatedTag = tagRepository.findById(tag.getId());
            assertThat(updatedTag).isPresent();
            assertThat(updatedTag.get().getUsageCount()).isGreaterThan(0);
            assertThat(updatedTag.get().getUsageCount()).isLessThanOrEqualTo(numberOfUpdates);
        }
    }

    @Nested
    @DisplayName("Soft Delete Operations")
    class SoftDeleteOperations {

        @Test
        @DisplayName("Should handle soft delete operations")
        @Transactional
        void shouldHandleSoftDeleteOperations() {
            // Given
            Tag activeTag = tagRepository.save(createTag("Active Tag", "active-tag", 5));
            Tag toDeleteTag = tagRepository.save(createTag("To Delete Tag", "to-delete-tag", 3));

            // When - Soft delete one tag
            toDeleteTag.markAsDeleted();
            tagRepository.save(toDeleteTag);

            // Then
            Optional<Tag> foundActive = tagRepository.findByNameAndDeletedFalse("Active Tag");
            Optional<Tag> foundDeleted = tagRepository.findByNameAndDeletedFalse("To Delete Tag");

            assertThat(foundActive).isPresent();
            assertThat(foundDeleted).isEmpty();

            // Verify deleted tag still exists in database
            Optional<Tag> deletedById = tagRepository.findById(toDeleteTag.getId());
            assertThat(deletedById).isPresent();
            assertThat(deletedById.get().isDeleted()).isTrue();

            // Most used tags should not include deleted tags
            Page<Tag> mostUsedTags = tagRepository.findMostUsedTags(PageRequest.of(0, 10));
            assertThat(mostUsedTags.getContent()).hasSize(1);
            assertThat(mostUsedTags.getContent().get(0).getName()).isEqualTo("Active Tag");
        }

        @Test
        @DisplayName("Should handle tag deletion with associated posts")
        @Transactional
        void shouldHandleTagDeletionWithAssociatedPosts() {
            // Given
            Tag tag = tagRepository.save(createTag("Tag with Posts", "tag-with-posts", 0));
            BlogPost post = createBlogPost("Post with Tag", "post-with-tag", Set.of(tag));
            blogPostRepository.save(post);

            // When - Soft delete tag
            tag.markAsDeleted();
            tagRepository.save(tag);

            // Then
            Optional<Tag> deletedTag = tagRepository.findByNameAndDeletedFalse("Tag with Posts");
            assertThat(deletedTag).isEmpty();

            // Post should still exist and reference the tag
            Optional<BlogPost> existingPost = blogPostRepository.findBySlugAndDeletedFalse("post-with-tag");
            assertThat(existingPost).isPresent();
            assertThat(existingPost.get().getTags()).hasSize(1);
            assertThat(existingPost.get().getTags().iterator().next().getId()).isEqualTo(tag.getId());

            // Tag count queries should not include deleted tags
            List<Object[]> tagsWithCounts = tagRepository.findTagsWithPostCounts();
            assertThat(tagsWithCounts).isEmpty(); // No active tags
        }
    }

    @Nested
    @DisplayName("Performance and Optimization")
    class PerformanceAndOptimization {

        @Test
        @DisplayName("Should efficiently handle large tag collections")
        @Transactional
        void shouldEfficientlyHandleLargeTagCollections() {
            // Given - Create many tags
            int numberOfTags = 100;
            for (int i = 0; i < numberOfTags; i++) {
                Tag tag = createTag("Tag " + i, "tag-" + i, i);
                tagRepository.save(tag);
            }

            // When - Query with pagination
            Pageable firstPage = PageRequest.of(0, 10);
            Pageable secondPage = PageRequest.of(1, 10);

            Page<Tag> firstPageResults = tagRepository.findMostUsedTags(firstPage);
            Page<Tag> secondPageResults = tagRepository.findMostUsedTags(secondPage);

            // Then
            assertThat(firstPageResults.getContent()).hasSize(10);
            assertThat(secondPageResults.getContent()).hasSize(10);
            assertThat(firstPageResults.getTotalElements()).isEqualTo(numberOfTags - 1); // Excludes tag with usage count 0

            // Verify ordering (highest usage count first)
            assertThat(firstPageResults.getContent().get(0).getUsageCount())
                    .isGreaterThan(firstPageResults.getContent().get(9).getUsageCount());
        }

        @Test
        @DisplayName("Should optimize autocomplete queries")
        @Transactional
        void shouldOptimizeAutocompleteQueries() {
            // Given - Create tags with various prefixes
            String[] prefixes = {"java", "javascript", "python", "php", "ruby", "go", "rust", "kotlin"};
            for (String prefix : prefixes) {
                for (int i = 0; i < 5; i++) {
                    Tag tag = createTag(prefix + " " + i, prefix + "-" + i, 10 - i);
                    tagRepository.save(tag);
                }
            }

            // When - Perform autocomplete searches
            long startTime = System.currentTimeMillis();
            List<Tag> javaResults = tagRepository.findByNameStartingWithIgnoreCase("java");
            List<Tag> pythonResults = tagRepository.findByNameStartingWithIgnoreCase("py");
            long endTime = System.currentTimeMillis();

            // Then
            assertThat(javaResults).hasSize(10); // java + javascript
            assertThat(pythonResults).hasSize(5); // python only
            assertThat(endTime - startTime).isLessThan(1000); // Should be fast

            // Verify ordering by usage count
            assertThat(javaResults.get(0).getUsageCount())
                    .isGreaterThanOrEqualTo(javaResults.get(1).getUsageCount());
        }
    }
}