# Personal Blog - Testing Strategy

## Overview

This document outlines the comprehensive testing strategy for the Personal Blog application, ensuring high code quality, reliability, and maintainability through modern testing practices with Spring Boot 3.5.4 and Java 21.

## Testing Philosophy

### Core Principles
- **Test-Driven Development (TDD)**: Write tests before implementation
- **Test Pyramid**: Focus on unit tests, supported by integration and E2E tests
- **Fast Feedback**: Tests should run quickly and provide immediate feedback
- **Reliable Tests**: Tests should be deterministic and not flaky
- **Maintainable Tests**: Tests should be easy to read, understand, and modify
- **Real-World Testing**: Use TestContainers for realistic integration testing

### Quality Targets
- **Unit Test Coverage**: 95%+ for service layer
- **Integration Test Coverage**: 85%+ for repository layer
- **E2E Test Coverage**: 80%+ for critical user journeys
- **Mutation Test Score**: 85%+ (using PIT)
- **Performance**: All tests complete in under 2 minutes

## Testing Stack

### Core Testing Dependencies

```xml
<dependencies>
    <!-- Spring Boot Test Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- TestContainers -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Security Testing -->
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Web Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Mutation Testing -->
    <dependency>
        <groupId>org.pitest</groupId>
        <artifactId>pitest-maven</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Performance Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Testing Tools
- **JUnit 5**: Primary testing framework
- **Mockito**: Mocking framework
- **AssertJ**: Fluent assertions
- **TestContainers**: Integration testing with real databases
- **WireMock**: External service mocking
- **Spring Boot Test**: Spring-specific testing utilities
- **Spring Security Test**: Security testing utilities
- **WebTestClient**: Reactive web testing
- **PIT**: Mutation testing

## Test Categories

### 1. Unit Tests

#### Service Layer Testing

**Example: BlogPostService Test**

```java
@ExtendWith(MockitoExtension.class)
class BlogPostServiceTest {
    
    @Mock
    private BlogPostRepository blogPostRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private TagRepository tagRepository;
    
    @Mock
    private SlugGenerator slugGenerator;
    
    @InjectMocks
    private BlogPostService blogPostService;
    
    @Test
    @DisplayName("Should create blog post successfully")
    void shouldCreateBlogPostSuccessfully() {
        // Given
        CreateBlogPostRequest request = CreateBlogPostRequest.builder()
            .title("Test Post")
            .content("Test content")
            .categoryId(1L)
            .tagIds(List.of(1L, 2L))
            .build();
            
        Category category = Category.builder()
            .id(1L)
            .name("Technology")
            .slug("technology")
            .build();
            
        List<Tag> tags = List.of(
            Tag.builder().id(1L).name("Java").slug("java").build(),
            Tag.builder().id(2L).name("Spring").slug("spring").build()
        );
        
        BlogPost savedPost = BlogPost.builder()
            .id(1L)
            .title("Test Post")
            .slug("test-post")
            .content("Test content")
            .status(PostStatus.DRAFT)
            .category(category)
            .tags(new HashSet<>(tags))
            .build();
        
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(tagRepository.findAllById(List.of(1L, 2L))).thenReturn(tags);
        when(slugGenerator.generateSlug("Test Post")).thenReturn("test-post");
        when(blogPostRepository.save(any(BlogPost.class))).thenReturn(savedPost);
        
        // When
        BlogPostResponse response = blogPostService.createPost(request);
        
        // Then
        assertThat(response)
            .isNotNull()
            .satisfies(post -> {
                assertThat(post.getId()).isEqualTo(1L);
                assertThat(post.getTitle()).isEqualTo("Test Post");
                assertThat(post.getSlug()).isEqualTo("test-post");
                assertThat(post.getStatus()).isEqualTo(PostStatus.DRAFT);
                assertThat(post.getCategory().getName()).isEqualTo("Technology");
                assertThat(post.getTags()).hasSize(2);
            });
            
        verify(blogPostRepository).save(argThat(post -> 
            post.getTitle().equals("Test Post") &&
            post.getSlug().equals("test-post") &&
            post.getStatus() == PostStatus.DRAFT
        ));
    }
    
    @Test
    @DisplayName("Should throw exception when category not found")
    void shouldThrowExceptionWhenCategoryNotFound() {
        // Given
        CreateBlogPostRequest request = CreateBlogPostRequest.builder()
            .title("Test Post")
            .content("Test content")
            .categoryId(999L)
            .build();
            
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> blogPostService.createPost(request))
            .isInstanceOf(CategoryNotFoundException.class)
            .hasMessage("Category not found with id: 999");
            
        verify(blogPostRepository, never()).save(any());
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t", "\n"})
    @DisplayName("Should throw exception for invalid title")
    void shouldThrowExceptionForInvalidTitle(String invalidTitle) {
        // Given
        CreateBlogPostRequest request = CreateBlogPostRequest.builder()
            .title(invalidTitle)
            .content("Test content")
            .categoryId(1L)
            .build();
        
        // When & Then
        assertThatThrownBy(() -> blogPostService.createPost(request))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Title cannot be blank");
    }
}
```

#### Repository Layer Testing

**Example: BlogPostRepository Test**

```java
@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb"
})
class BlogPostRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private BlogPostRepository blogPostRepository;
    
    @Test
    @DisplayName("Should find published posts by category")
    void shouldFindPublishedPostsByCategory() {
        // Given
        Category category = Category.builder()
            .name("Technology")
            .slug("technology")
            .build();
        entityManager.persistAndFlush(category);
        
        BlogPost publishedPost = BlogPost.builder()
            .title("Published Post")
            .slug("published-post")
            .content("Content")
            .status(PostStatus.PUBLISHED)
            .publishedAt(LocalDateTime.now())
            .category(category)
            .build();
            
        BlogPost draftPost = BlogPost.builder()
            .title("Draft Post")
            .slug("draft-post")
            .content("Content")
            .status(PostStatus.DRAFT)
            .category(category)
            .build();
            
        entityManager.persist(publishedPost);
        entityManager.persist(draftPost);
        entityManager.flush();
        
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<BlogPost> result = blogPostRepository.findPublishedPostsByCategory(
            category.getSlug(), pageable);
        
        // Then
        assertThat(result.getContent())
            .hasSize(1)
            .extracting(BlogPost::getTitle)
            .containsExactly("Published Post");
    }
    
    @Test
    @DisplayName("Should find posts by tag")
    void shouldFindPostsByTag() {
        // Given
        Tag javaTag = Tag.builder().name("Java").slug("java").build();
        entityManager.persistAndFlush(javaTag);
        
        BlogPost post = BlogPost.builder()
            .title("Java Post")
            .slug("java-post")
            .content("Content")
            .status(PostStatus.PUBLISHED)
            .publishedAt(LocalDateTime.now())
            .tags(Set.of(javaTag))
            .build();
            
        entityManager.persistAndFlush(post);
        
        // When
        List<BlogPost> result = blogPostRepository.findByTagsSlug("java");
        
        // Then
        assertThat(result)
            .hasSize(1)
            .extracting(BlogPost::getTitle)
            .containsExactly("Java Post");
    }
}
```

### 2. Integration Tests

#### Web Layer Integration Tests

**Example: BlogPostController Integration Test**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(OrderAnnotation.class)
class BlogPostControllerIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @Autowired
    private WebTestClient webTestClient;
    
    @Autowired
    private BlogPostRepository blogPostRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    private String adminToken;
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @BeforeEach
    void setUp() {
        adminToken = jwtTokenProvider.generateToken("admin", List.of("ROLE_ADMIN"));
    }
    
    @Test
    @Order(1)
    @DisplayName("Should create blog post successfully")
    void shouldCreateBlogPostSuccessfully() {
        // Given
        Category category = categoryRepository.save(
            Category.builder().name("Technology").slug("technology").build());
            
        CreateBlogPostRequest request = CreateBlogPostRequest.builder()
            .title("Integration Test Post")
            .content("This is a test post content")
            .excerpt("Test excerpt")
            .categoryId(category.getId())
            .status(PostStatus.PUBLISHED)
            .build();
        
        // When & Then
        webTestClient.post()
            .uri("/api/v1/admin/posts")
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.success").isEqualTo(true)
            .jsonPath("$.data.title").isEqualTo("Integration Test Post")
            .jsonPath("$.data.slug").isEqualTo("integration-test-post")
            .jsonPath("$.data.status").isEqualTo("PUBLISHED")
            .jsonPath("$.data.category.name").isEqualTo("Technology");
    }
    
    @Test
    @Order(2)
    @DisplayName("Should get published posts")
    void shouldGetPublishedPosts() {
        // When & Then
        webTestClient.get()
            .uri("/api/v1/posts?page=0&size=10")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.success").isEqualTo(true)
            .jsonPath("$.data.content").isArray()
            .jsonPath("$.data.content[0].title").isEqualTo("Integration Test Post")
            .jsonPath("$.data.totalElements").isNumber()
            .jsonPath("$.data.totalPages").isNumber();
    }
    
    @Test
    @DisplayName("Should return 401 for unauthorized admin requests")
    void shouldReturn401ForUnauthorizedAdminRequests() {
        // Given
        CreateBlogPostRequest request = CreateBlogPostRequest.builder()
            .title("Unauthorized Post")
            .content("Content")
            .categoryId(1L)
            .build();
        
        // When & Then
        webTestClient.post()
            .uri("/api/v1/admin/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isUnauthorized();
    }
}
```

#### Security Integration Tests

**Example: JWT Security Test**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityIntegrationTest {
    
    @Autowired
    private WebTestClient webTestClient;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Test
    @DisplayName("Should authenticate admin successfully")
    void shouldAuthenticateAdminSuccessfully() {
        // Given
        LoginRequest request = new LoginRequest("admin", "admin123");
        
        // When & Then
        webTestClient.post()
            .uri("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.success").isEqualTo(true)
            .jsonPath("$.data.accessToken").exists()
            .jsonPath("$.data.refreshToken").exists()
            .jsonPath("$.data.expiresIn").isNumber();
    }
    
    @Test
    @DisplayName("Should reject invalid credentials")
    void shouldRejectInvalidCredentials() {
        // Given
        LoginRequest request = new LoginRequest("admin", "wrongpassword");
        
        // When & Then
        webTestClient.post()
            .uri("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isUnauthorized()
            .expectBody()
            .jsonPath("$.success").isEqualTo(false)
            .jsonPath("$.message").value(containsString("Invalid credentials"));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should access admin endpoints with valid role")
    void shouldAccessAdminEndpointsWithValidRole() {
        webTestClient.get()
            .uri("/api/v1/admin/posts")
            .exchange()
            .expectStatus().isOk();
    }
    
    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should deny access to admin endpoints with invalid role")
    void shouldDenyAccessToAdminEndpointsWithInvalidRole() {
        webTestClient.get()
            .uri("/api/v1/admin/posts")
            .exchange()
            .expectStatus().isForbidden();
    }
}
```

### 3. End-to-End Tests

#### Complete User Journey Tests

**Example: Blog Management E2E Test**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(OrderAnnotation.class)
class BlogManagementE2ETest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("e2etest")
            .withUsername("test")
            .withPassword("test");
    
    @Autowired
    private WebTestClient webTestClient;
    
    private String adminToken;
    private Long createdPostId;
    private Long categoryId;
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Test
    @Order(1)
    @DisplayName("E2E: Admin login")
    void adminLogin() {
        LoginRequest request = new LoginRequest("admin", "admin123");
        
        EntityExchangeResult<String> result = webTestClient.post()
            .uri("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .returnResult();
            
        // Extract token from response
        String responseBody = result.getResponseBody();
        adminToken = JsonPath.read(responseBody, "$.data.accessToken");
        
        assertThat(adminToken).isNotBlank();
    }
    
    @Test
    @Order(2)
    @DisplayName("E2E: Create category")
    void createCategory() {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
            .name("E2E Testing")
            .description("Category for E2E testing")
            .build();
            
        EntityExchangeResult<String> result = webTestClient.post()
            .uri("/api/v1/admin/categories")
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(String.class)
            .returnResult();
            
        String responseBody = result.getResponseBody();
        categoryId = JsonPath.read(responseBody, "$.data.id");
        
        assertThat(categoryId).isNotNull();
    }
    
    @Test
    @Order(3)
    @DisplayName("E2E: Create blog post")
    void createBlogPost() {
        CreateBlogPostRequest request = CreateBlogPostRequest.builder()
            .title("E2E Test Post")
            .content("This is a comprehensive E2E test post with detailed content.")
            .excerpt("E2E test excerpt")
            .categoryId(categoryId)
            .status(PostStatus.PUBLISHED)
            .build();
            
        EntityExchangeResult<String> result = webTestClient.post()
            .uri("/api/v1/admin/posts")
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(String.class)
            .returnResult();
            
        String responseBody = result.getResponseBody();
        createdPostId = JsonPath.read(responseBody, "$.data.id");
        
        assertThat(createdPostId).isNotNull();
    }
    
    @Test
    @Order(4)
    @DisplayName("E2E: Public can view published post")
    void publicCanViewPublishedPost() {
        webTestClient.get()
            .uri("/api/v1/posts/{id}", createdPostId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.success").isEqualTo(true)
            .jsonPath("$.data.title").isEqualTo("E2E Test Post")
            .jsonPath("$.data.status").isEqualTo("PUBLISHED")
            .jsonPath("$.data.category.name").isEqualTo("E2E Testing");
    }
    
    @Test
    @Order(5)
    @DisplayName("E2E: Update blog post")
    void updateBlogPost() {
        UpdateBlogPostRequest request = UpdateBlogPostRequest.builder()
            .title("Updated E2E Test Post")
            .content("Updated content for E2E testing.")
            .excerpt("Updated excerpt")
            .categoryId(categoryId)
            .status(PostStatus.PUBLISHED)
            .build();
            
        webTestClient.put()
            .uri("/api/v1/admin/posts/{id}", createdPostId)
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.success").isEqualTo(true)
            .jsonPath("$.data.title").isEqualTo("Updated E2E Test Post");
    }
    
    @Test
    @Order(6)
    @DisplayName("E2E: Search functionality")
    void searchFunctionality() {
        webTestClient.get()
            .uri("/api/v1/search?q=E2E&page=0&size=10")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.success").isEqualTo(true)
            .jsonPath("$.data.results").isArray()
            .jsonPath("$.data.results[0].title").value(containsString("E2E"));
    }
    
    @Test
    @Order(7)
    @DisplayName("E2E: Delete blog post")
    void deleteBlogPost() {
        webTestClient.delete()
            .uri("/api/v1/admin/posts/{id}", createdPostId)
            .header("Authorization", "Bearer " + adminToken)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.success").isEqualTo(true);
            
        // Verify post is deleted
        webTestClient.get()
            .uri("/api/v1/posts/{id}", createdPostId)
            .exchange()
            .expectStatus().isNotFound();
    }
}
```

### 4. Performance Tests

#### Load Testing

**Example: Performance Test**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class PerformanceTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("perftest")
            .withUsername("test")
            .withPassword("test");
    
    @Autowired
    private WebTestClient webTestClient;
    
    @Autowired
    private BlogPostRepository blogPostRepository;
    
    @Test
    @DisplayName("Should handle concurrent requests efficiently")
    void shouldHandleConcurrentRequestsEfficiently() {
        // Given - Create test data
        createTestPosts(100);
        
        // When - Execute concurrent requests
        List<CompletableFuture<Void>> futures = IntStream.range(0, 50)
            .mapToObj(i -> CompletableFuture.runAsync(() -> {
                webTestClient.get()
                    .uri("/api/v1/posts?page=0&size=10")
                    .exchange()
                    .expectStatus().isOk();
            }))
            .toList();
            
        // Then - All requests should complete within reasonable time
        assertThatCode(() -> {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .get(5, TimeUnit.SECONDS);
        }).doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should respond within acceptable time limits")
    void shouldRespondWithinAcceptableTimeLimits() {
        // Given
        createTestPosts(10);
        
        // When & Then
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        webTestClient.get()
            .uri("/api/v1/posts?page=0&size=10")
            .exchange()
            .expectStatus().isOk();
            
        stopWatch.stop();
        
        // Response should be under 500ms
        assertThat(stopWatch.getTotalTimeMillis()).isLessThan(500);
    }
    
    private void createTestPosts(int count) {
        List<BlogPost> posts = IntStream.range(0, count)
            .mapToObj(i -> BlogPost.builder()
                .title("Performance Test Post " + i)
                .slug("performance-test-post-" + i)
                .content("Content for performance test post " + i)
                .status(PostStatus.PUBLISHED)
                .publishedAt(LocalDateTime.now())
                .build())
            .toList();
            
        blogPostRepository.saveAll(posts);
    }
}
```

## Test Configuration

### Base Test Configuration

**TestConfiguration Class**

```java
@TestConfiguration
public class TestConfig {
    
    @Bean
    @Primary
    public Clock testClock() {
        return Clock.fixed(Instant.parse("2025-07-30T10:00:00Z"), ZoneOffset.UTC);
    }
    
    @Bean
    @Primary
    public SlugGenerator testSlugGenerator() {
        return Mockito.mock(SlugGenerator.class);
    }
    
    @Bean
    @Primary
    public EmailService testEmailService() {
        return Mockito.mock(EmailService.class);
    }
}
```

### Test Profiles

**application-test.yml**

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: false
  
  cache:
    type: none
  
  security:
    user:
      name: admin
      password: admin123
      roles: ADMIN

jwt:
  secret: test-jwt-secret-key-for-testing-purposes-only
  expiration: 3600000
  refresh-expiration: 86400000

logging:
  level:
    org.springframework.security: DEBUG
    org.springframework.web: DEBUG
    com.personalblog: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

## Test Data Management

### Test Data Builders

**BlogPostTestDataBuilder**

```java
public class BlogPostTestDataBuilder {
    
    private String title = "Default Test Post";
    private String content = "Default test content";
    private PostStatus status = PostStatus.DRAFT;
    private Category category;
    private Set<Tag> tags = new HashSet<>();
    private LocalDateTime publishedAt;
    
    public static BlogPostTestDataBuilder aBlogPost() {
        return new BlogPostTestDataBuilder();
    }
    
    public BlogPostTestDataBuilder withTitle(String title) {
        this.title = title;
        return this;
    }
    
    public BlogPostTestDataBuilder withContent(String content) {
        this.content = content;
        return this;
    }
    
    public BlogPostTestDataBuilder withStatus(PostStatus status) {
        this.status = status;
        return this;
    }
    
    public BlogPostTestDataBuilder published() {
        this.status = PostStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
        return this;
    }
    
    public BlogPostTestDataBuilder withCategory(Category category) {
        this.category = category;
        return this;
    }
    
    public BlogPostTestDataBuilder withTags(Tag... tags) {
        this.tags = Set.of(tags);
        return this;
    }
    
    public BlogPost build() {
        return BlogPost.builder()
            .title(title)
            .slug(title.toLowerCase().replace(" ", "-"))
            .content(content)
            .status(status)
            .publishedAt(publishedAt)
            .category(category)
            .tags(tags)
            .build();
    }
}
```

### Database Test Utilities

**DatabaseTestUtils**

```java
@Component
public class DatabaseTestUtils {
    
    @Autowired
    private TestEntityManager entityManager;
    
    public Category createAndPersistCategory(String name) {
        Category category = Category.builder()
            .name(name)
            .slug(name.toLowerCase().replace(" ", "-"))
            .build();
        return entityManager.persistAndFlush(category);
    }
    
    public Tag createAndPersistTag(String name) {
        Tag tag = Tag.builder()
            .name(name)
            .slug(name.toLowerCase().replace(" ", "-"))
            .build();
        return entityManager.persistAndFlush(tag);
    }
    
    public BlogPost createAndPersistBlogPost(String title, Category category, PostStatus status) {
        BlogPost post = BlogPost.builder()
            .title(title)
            .slug(title.toLowerCase().replace(" ", "-"))
            .content("Test content for " + title)
            .status(status)
            .category(category)
            .publishedAt(status == PostStatus.PUBLISHED ? LocalDateTime.now() : null)
            .build();
        return entityManager.persistAndFlush(post);
    }
    
    public void cleanDatabase() {
        entityManager.getEntityManager()
            .createNativeQuery("TRUNCATE TABLE blog_posts, categories, tags, post_tags RESTART IDENTITY")
            .executeUpdate();
    }
}
```

## Test Execution

### Maven Configuration

**pom.xml Test Configuration**

```xml
<build>
    <plugins>
        <!-- Surefire Plugin for Unit Tests -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.1.2</version>
            <configuration>
                <includes>
                    <include>**/*Test.java</include>
                    <include>**/*Tests.java</include>
                </includes>
                <excludes>
                    <exclude>**/*IntegrationTest.java</exclude>
                    <exclude>**/*E2ETest.java</exclude>
                </excludes>
                <systemPropertyVariables>
                    <spring.profiles.active>test</spring.profiles.active>
                </systemPropertyVariables>
            </configuration>
        </plugin>
        
        <!-- Failsafe Plugin for Integration Tests -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-failsafe-plugin</artifactId>
            <version>3.1.2</version>
            <configuration>
                <includes>
                    <include>**/*IntegrationTest.java</include>
                    <include>**/*E2ETest.java</include>
                </includes>
                <systemPropertyVariables>
                    <spring.profiles.active>test</spring.profiles.active>
                </systemPropertyVariables>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>integration-test</goal>
                        <goal>verify</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        
        <!-- JaCoCo for Code Coverage -->
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.10</version>
            <executions>
                <execution>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
                <execution>
                    <id>check</id>
                    <goals>
                        <goal>check</goal>
                    </goals>
                    <configuration>
                        <rules>
                            <rule>
                                <element>CLASS</element>
                                <limits>
                                    <limit>
                                        <counter>LINE</counter>
                                        <value>COVEREDRATIO</value>
                                        <minimum>0.85</minimum>
                                    </limit>
                                </limits>
                            </rule>
                        </rules>
                    </configuration>
                </execution>
            </executions>
        </plugin>
        
        <!-- PIT Mutation Testing -->
        <plugin>
            <groupId>org.pitest</groupId>
            <artifactId>pitest-maven</artifactId>
            <version>1.15.0</version>
            <dependencies>
                <dependency>
                    <groupId>org.pitest</groupId>
                    <artifactId>pitest-junit5-plugin</artifactId>
                    <version>1.2.0</version>
                </dependency>
            </dependencies>
            <configuration>
                <targetClasses>
                    <param>com.personalblog.service.*</param>
                    <param>com.personalblog.controller.*</param>
                </targetClasses>
                <targetTests>
                    <param>com.personalblog.*Test</param>
                </targetTests>
                <mutationThreshold>85</mutationThreshold>
                <coverageThreshold>90</coverageThreshold>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Test Execution Commands

```bash
# Run unit tests only
mvn test

# Run integration tests only
mvn failsafe:integration-test

# Run all tests
mvn verify

# Run tests with coverage
mvn clean verify jacoco:report

# Run mutation tests
mvn org.pitest:pitest-maven:mutationCoverage

# Run specific test class
mvn test -Dtest=BlogPostServiceTest

# Run tests with specific profile
mvn test -Dspring.profiles.active=test

# Run tests in parallel
mvn test -T 4
```

## Continuous Integration

### GitHub Actions Workflow

**.github/workflows/test.yml**

```yaml
name: Test Suite

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_PASSWORD: postgres
          POSTGRES_DB: testdb
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 5432:5432
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'
    
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
    
    - name: Run unit tests
      run: mvn test
    
    - name: Run integration tests
      run: mvn failsafe:integration-test failsafe:verify
    
    - name: Generate test report
      run: mvn jacoco:report
    
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
      with:
        file: ./target/site/jacoco/jacoco.xml
    
    - name: Publish test results
      uses: dorny/test-reporter@v1
      if: success() || failure()
      with:
        name: Maven Tests
        path: target/surefire-reports/*.xml
        reporter: java-junit
```

## Best Practices

### Test Organization
1. **Follow AAA Pattern**: Arrange, Act, Assert
2. **Use descriptive test names**: Should describe what is being tested
3. **One assertion per test**: Focus on single behavior
4. **Use test data builders**: For complex object creation
5. **Clean up after tests**: Ensure test isolation

### Performance Considerations
1. **Use @DirtiesContext sparingly**: It slows down test execution
2. **Prefer @MockBean over @Autowired**: For faster unit tests
3. **Use TestContainers for integration tests**: Real database behavior
4. **Parallel test execution**: When tests are independent
5. **Test slicing**: Use @WebMvcTest, @DataJpaTest for focused testing

### Maintenance
1. **Regular test review**: Remove obsolete tests
2. **Update test data**: Keep test scenarios relevant
3. **Monitor test execution time**: Optimize slow tests
4. **Test coverage analysis**: Identify untested code paths
5. **Mutation testing**: Verify test quality

This comprehensive testing strategy ensures the Personal Blog application maintains high quality, reliability, and performance while supporting continuous development and deployment practices.