# Personal Blog - Development Guide

## Quick Start

### Prerequisites
- **Java 21** (OpenJDK or Eclipse Temurin)
- **Maven 3.9+** (or use included Maven Wrapper)
- **PostgreSQL 15+** (for local development)
- **Docker** (optional, for containerized development)
- **IDE** (IntelliJ IDEA recommended, VS Code with Java extensions)

### Initial Setup

1. **Clone and Navigate**
   ```bash
   cd c:/Users/adria/Desktop/Backend-Projects/personal-blog
   ```

2. **Verify Java Version**
   ```bash
   java -version  # Should show Java 21
   ```

3. **Build Project**
   ```bash
   ./mvnw clean compile  # Use mvnw.cmd on Windows
   ```

4. **Run Tests**
   ```bash
   ./mvnw test
   ```

5. **Start Application**
   ```bash
   ./mvnw spring-boot:run
   ```

## Project Structure

### Source Code Organization
```
src/
├── main/
│   ├── java/com/shockp/blogpersonal/
│   │   ├── config/              # Configuration classes
│   │   ├── controller/          # REST and MVC controllers
│   │   ├── dto/                # Data Transfer Objects
│   │   ├── entity/             # JPA entities
│   │   ├── repository/         # Spring Data repositories
│   │   ├── service/            # Business logic services
│   │   ├── security/           # Security configuration
│   │   ├── exception/          # Custom exceptions
│   │   └── BlogPersonalApplication.java
│   ├── resources/
│   │   ├── static/             # CSS, JS, images
│   │   ├── templates/          # Thymeleaf templates
│   │   ├── db/migration/       # Flyway migrations
│   │   └── application.yml     # Configuration
│   └── webapp/                 # Additional web resources
└── test/
    ├── java/                   # Test classes
    └── resources/              # Test resources
```

### Configuration Files
- `application.yml` - Main configuration
- `application-dev.yml` - Development profile
- `application-test.yml` - Test profile
- `application-prod.yml` - Production profile

## Development Workflow

### Feature Development Process

1. **Create Feature Branch**
   ```bash
   git checkout -b feature/post-management
   ```

2. **Write Tests First (TDD)**
   ```java
   @Test
   void shouldCreateBlogPost() {
       // Arrange
       CreatePostRequest request = new CreatePostRequest("Title", "Content");
       
       // Act
       BlogPost result = blogPostService.createPost(request);
       
       // Assert
       assertThat(result.getTitle()).isEqualTo("Title");
   }
   ```

3. **Implement Feature**
   - Create/update entities
   - Implement service logic
   - Add controller endpoints
   - Create/update templates

4. **Run Tests Continuously**
   ```bash
   ./mvnw test-compile failsafe:integration-test
   ```

5. **Verify Code Quality**
   ```bash
   ./mvnw spotbugs:check pmd:check checkstyle:check
   ```

### Database Development

#### Local Database Setup
```bash
# Start PostgreSQL with Docker
docker run --name blog-postgres \
  -e POSTGRES_DB=blogpersonal \
  -e POSTGRES_USER=bloguser \
  -e POSTGRES_PASSWORD=blogpass \
  -p 5432:5432 \
  -d postgres:15
```

#### Migration Management
```bash
# Create new migration
./mvnw flyway:migrate

# Check migration status
./mvnw flyway:info

# Clean database (development only)
./mvnw flyway:clean
```

#### Migration File Naming
```
V1__Create_initial_schema.sql
V2__Add_blog_posts_table.sql
V3__Add_categories_and_tags.sql
```

### Testing Guidelines

#### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class BlogPostServiceTest {
    
    @Mock
    private BlogPostRepository repository;
    
    @InjectMocks
    private BlogPostService service;
    
    @Test
    void shouldFindPublishedPosts() {
        // Test implementation
    }
}
```

#### Integration Tests
```java
@SpringBootTest
@Testcontainers
class BlogPostIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @Test
    void shouldPersistBlogPost() {
        // Integration test implementation
    }
}
```

#### Web Layer Tests
```java
@WebMvcTest(BlogPostController.class)
class BlogPostControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private BlogPostService service;
    
    @Test
    void shouldReturnBlogPosts() throws Exception {
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
```

### Security Development

#### JWT Configuration
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .requestMatchers("/api/public/**").permitAll()
                    .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();
    }
}
```

#### Input Validation
```java
public record CreatePostRequest(
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    String title,
    
    @NotBlank(message = "Content is required")
    @Size(min = 10, message = "Content must be at least 10 characters")
    String content
) {}
```

### Frontend Development

#### Thymeleaf Templates
```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title th:text="${post.title} + ' - Personal Blog'">Blog Post</title>
</head>
<body>
    <article>
        <h1 th:text="${post.title}">Post Title</h1>
        <div th:utext="${post.content}">Post content...</div>
    </article>
</body>
</html>
```

#### CSS Organization
```
static/css/
├── base.css          # Reset, typography, base styles
├── layout.css        # Grid, flexbox layouts
├── components.css    # Reusable components
├── pages.css         # Page-specific styles
└── utilities.css     # Utility classes
```

## Environment Configuration

### Development Profile (`application-dev.yml`)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/blogpersonal
    username: bloguser
    password: blogpass
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: validate
  devtools:
    restart:
      enabled: true
    livereload:
      enabled: true

logging:
  level:
    com.shockp.blogpersonal: DEBUG
    org.springframework.security: DEBUG
```

### Test Profile (`application-test.yml`)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
  flyway:
    enabled: false

logging:
  level:
    org.springframework.web: DEBUG
```

## Build and Deployment

### Local Build
```bash
# Full build with tests
./mvnw clean package

# Skip tests (for quick builds)
./mvnw clean package -DskipTests

# Build Docker image
./mvnw spring-boot:build-image
```

### Docker Development
```dockerfile
# Dockerfile.dev
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
# Build and run with Docker
docker build -f Dockerfile.dev -t personal-blog:dev .
docker run -p 8080:8080 personal-blog:dev
```

### Production Deployment
```bash
# Build production image
./mvnw clean package -Pprod
./mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=personal-blog:latest

# Deploy to Railway/Render
git push origin main  # Triggers automatic deployment
```

## Debugging and Troubleshooting

### Common Issues

1. **Database Connection Issues**
   ```bash
   # Check PostgreSQL status
   docker ps | grep postgres
   
   # View logs
   docker logs blog-postgres
   ```

2. **Port Conflicts**
   ```bash
   # Find process using port 8080
   netstat -ano | findstr :8080
   
   # Kill process (Windows)
   taskkill /PID <process_id> /F
   ```

3. **Maven Issues**
   ```bash
   # Clear Maven cache
   ./mvnw dependency:purge-local-repository
   
   # Refresh dependencies
   ./mvnw clean compile -U
   ```

### Debug Configuration
```bash
# Run with debug mode
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

### Logging Configuration
```yaml
logging:
  level:
    com.shockp.blogpersonal: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

## Performance Optimization

### Database Optimization
```sql
-- Add indexes for frequently queried columns
CREATE INDEX idx_blog_posts_published_at ON blog_posts(published_at);
CREATE INDEX idx_blog_posts_category_id ON blog_posts(category_id);
CREATE INDEX idx_blog_posts_status ON blog_posts(status);
```

### JVM Tuning
```bash
# Production JVM options
java -XX:+UseZGC \
     -XX:+UnlockExperimentalVMOptions \
     -XX:+UseTransparentHugePages \
     -Xms512m -Xmx1g \
     -jar app.jar
```

### Caching Strategy
```java
@Service
public class BlogPostService {
    
    @Cacheable(value = "posts", key = "#id")
    public BlogPost findById(Long id) {
        return repository.findById(id).orElseThrow();
    }
    
    @CacheEvict(value = "posts", key = "#post.id")
    public BlogPost updatePost(BlogPost post) {
        return repository.save(post);
    }
}
```

This development guide provides comprehensive instructions for working with the personal blog project, from initial setup to advanced optimization techniques.