# Development Guide

This guide provides comprehensive instructions for setting up, developing, and contributing to the Personal Blog application.

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Development Setup](#development-setup)
3. [IDE Configuration](#ide-configuration)
4. [Running the Application](#running-the-application)
5. [Development Workflow](#development-workflow)
6. [Code Standards](#code-standards)
7. [Testing Guidelines](#testing-guidelines)
8. [Debugging](#debugging)
9. [Database Management](#database-management)
10. [API Development](#api-development)
11. [Security Guidelines](#security-guidelines)
12. [Performance Optimization](#performance-optimization)
13. [Troubleshooting](#troubleshooting)

## 🔧 Prerequisites

### Required Software
- **Java 21+**: OpenJDK or Oracle JDK
- **Maven 3.6+**: Build automation (or use included wrapper)
- **Git**: Version control
- **Docker**: For containerized development (recommended)
- **PostgreSQL 13+**: Database (or use Docker)
- **Redis 6+**: Cache (or use Docker)

### Recommended Tools
- **IntelliJ IDEA**: IDE with Spring Boot support
- **Postman**: API testing
- **DBeaver**: Database management
- **Redis Desktop Manager**: Redis GUI

### Verify Installation
```bash
# Check Java version
java -version

# Check Maven version
mvn -version

# Check Docker version
docker --version
docker-compose --version

# Check Git version
git --version
```

## 🚀 Development Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd personal-blog
```

### 2. Environment Configuration
```bash
# Copy environment template
cp .env.example .env

# Edit environment variables
# Update database credentials, JWT secret, etc.
```

### 3. Database Setup

#### Option A: Docker (Recommended)
```bash
# Start PostgreSQL and Redis
docker-compose up -d database redis

# Verify services are running
docker-compose ps
```

#### Option B: Local Installation
```bash
# PostgreSQL setup
createdb personal_blog
psql personal_blog < docker/init.sql

# Redis setup
redis-server
```

### 4. Build the Application
```bash
# Using Maven wrapper (recommended)
./mvnw clean compile

# Or using Maven
mvn clean compile
```

### 5. Run Tests
```bash
# Run all tests
./mvnw test

# Run specific test categories
./mvnw test -Dtest="*ControllerTest"
```

## 🛠 IDE Configuration

### IntelliJ IDEA Setup

1. **Import Project**:
   - File → Open → Select `pom.xml`
   - Import as Maven project

2. **Configure JDK**:
   - File → Project Structure → Project
   - Set Project SDK to Java 21

3. **Enable Annotation Processing**:
   - Settings → Build → Compiler → Annotation Processors
   - Enable annotation processing

4. **Install Plugins**:
   - Spring Boot
   - Lombok
   - Docker
   - Database Navigator

5. **Code Style**:
   - Import code style from `ide-config/intellij-codestyle.xml`

### VS Code Setup

1. **Install Extensions**:
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - Docker
   - PostgreSQL

2. **Configure Settings**:
   ```json
   {
     "java.home": "/path/to/java21",
     "maven.executable.path": "./mvnw",
     "spring-boot.ls.java.home": "/path/to/java21"
   }
   ```

## 🏃 Running the Application

### Development Mode
```bash
# Start with development profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Or with environment variables
export SPRING_PROFILES_ACTIVE=dev
./mvnw spring-boot:run
```

### Docker Development
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down
```

### Hot Reload
Enable Spring Boot DevTools for automatic restart:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

## 🔄 Development Workflow

### Git Workflow
```bash
# Create feature branch
git checkout -b feature/new-feature

# Make changes and commit
git add .
git commit -m "feat: add new feature"

# Push and create PR
git push origin feature/new-feature
```

### Commit Message Convention
Follow [Conventional Commits](https://www.conventionalcommits.org/):
```
type(scope): description

feat: add user authentication
fix: resolve database connection issue
docs: update API documentation
test: add unit tests for user service
refactor: improve code structure
```

### Branch Strategy
- `main`: Production-ready code
- `develop`: Integration branch
- `feature/*`: New features
- `bugfix/*`: Bug fixes
- `hotfix/*`: Critical fixes

## 📝 Code Standards

### Java Coding Standards

1. **Naming Conventions**:
   ```java
   // Classes: PascalCase
   public class BlogPostService {}
   
   // Methods and variables: camelCase
   public BlogPost findById(UUID id) {}
   
   // Constants: UPPER_SNAKE_CASE
   private static final String DEFAULT_STATUS = "DRAFT";
   ```

2. **Package Structure**:
   ```
   com.personalblog
   ├── config          # Configuration classes
   ├── controller      # REST controllers
   ├── dto            # Data Transfer Objects
   ├── entity         # JPA entities
   ├── exception      # Custom exceptions
   ├── repository     # Data repositories
   ├── security       # Security configuration
   ├── service        # Business logic
   └── util           # Utility classes
   ```

3. **Documentation**:
   ```java
   /**
    * Service for managing blog posts.
    * 
    * @author Your Name
    * @since 1.0.0
    */
   @Service
   @Transactional
   public class BlogPostService {
       
       /**
        * Finds a blog post by its ID.
        * 
        * @param id the post ID
        * @return the blog post
        * @throws ResourceNotFoundException if post not found
        */
       public BlogPost findById(UUID id) {
           // Implementation
       }
   }
   ```

### Code Quality Tools

1. **Checkstyle**: Code style enforcement
2. **SpotBugs**: Bug detection
3. **PMD**: Code analysis
4. **SonarQube**: Code quality metrics

```bash
# Run code quality checks
./mvnw checkstyle:check
./mvnw spotbugs:check
./mvnw pmd:check
```

## 🧪 Testing Guidelines

### Test Structure
```
src/test/java/
├── controller/     # Controller tests
├── service/       # Service tests
├── repository/    # Repository tests
├── integration/   # Integration tests
└── util/         # Test utilities
```

### Unit Testing
```java
@ExtendWith(MockitoExtension.class)
class BlogPostServiceTest {
    
    @Mock
    private BlogPostRepository repository;
    
    @InjectMocks
    private BlogPostService service;
    
    @Test
    @DisplayName("Should find blog post by ID")
    void shouldFindBlogPostById() {
        // Given
        UUID id = UUID.randomUUID();
        BlogPost expected = BlogPost.builder()
            .id(id)
            .title("Test Post")
            .build();
        
        when(repository.findById(id)).thenReturn(Optional.of(expected));
        
        // When
        BlogPost actual = service.findById(id);
        
        // Then
        assertThat(actual).isEqualTo(expected);
    }
}
```

### Integration Testing
```java
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class BlogPostControllerIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldCreateBlogPost() {
        // Test implementation
    }
}
```

### Test Data Builders
```java
public class BlogPostTestDataBuilder {
    private String title = "Default Title";
    private String content = "Default Content";
    private User author = UserTestDataBuilder.aUser().build();
    
    public static BlogPostTestDataBuilder aBlogPost() {
        return new BlogPostTestDataBuilder();
    }
    
    public BlogPostTestDataBuilder withTitle(String title) {
        this.title = title;
        return this;
    }
    
    public BlogPost build() {
        return BlogPost.builder()
            .title(title)
            .content(content)
            .author(author)
            .build();
    }
}
```

## 🐛 Debugging

### Application Debugging

1. **Enable Debug Mode**:
   ```properties
   # application-dev.properties
   logging.level.com.personalblog=DEBUG
   logging.level.org.springframework.security=DEBUG
   ```

2. **Remote Debugging**:
   ```bash
   # Start with debug port
   ./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
   ```

3. **Actuator Endpoints**:
   ```bash
   # Health check
   curl http://localhost:8080/api/actuator/health
   
   # Application info
   curl http://localhost:8080/api/actuator/info
   
   # Metrics
   curl http://localhost:8080/api/actuator/metrics
   ```

### Database Debugging

1. **SQL Logging**:
   ```properties
   # Show SQL queries
   spring.jpa.show-sql=true
   spring.jpa.properties.hibernate.format_sql=true
   logging.level.org.hibernate.SQL=DEBUG
   logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
   ```

2. **Connection Pool Monitoring**:
   ```properties
   # HikariCP metrics
   spring.datasource.hikari.register-mbeans=true
   ```

## 🗄 Database Management

### Migrations

Using Flyway for database migrations:
```sql
-- V1__Create_users_table.sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Database Commands
```bash
# Run migrations
./mvnw flyway:migrate

# Check migration status
./mvnw flyway:info

# Clean database (development only)
./mvnw flyway:clean
```

### Backup and Restore
```bash
# Backup
pg_dump -h localhost -U postgres personal_blog > backup.sql

# Restore
psql -h localhost -U postgres personal_blog < backup.sql
```

## 🌐 API Development

### Controller Development
```java
@RestController
@RequestMapping("/api/v1/posts")
@Validated
@SecurityRequirement(name = "bearerAuth")
public class BlogPostController {
    
    @GetMapping
    @Operation(summary = "Get all blog posts")
    public ResponseEntity<PagedResponse<BlogPostDto>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Pageable pageable = PageRequest.of(page, size, 
            Sort.Direction.fromString(sortDir), sortBy);
        
        Page<BlogPost> posts = blogPostService.findAll(pageable);
        
        return ResponseEntity.ok(PagedResponse.of(posts, BlogPostDto::from));
    }
}
```

### DTO Development
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogPostDto {
    
    @Schema(description = "Post ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;
    
    @Schema(description = "Post title", example = "My First Blog Post")
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    @Schema(description = "Post content")
    @NotBlank(message = "Content is required")
    private String content;
    
    public static BlogPostDto from(BlogPost post) {
        return BlogPostDto.builder()
            .id(post.getId())
            .title(post.getTitle())
            .content(post.getContent())
            .build();
    }
}
```

### API Testing
```bash
# Using curl
curl -X GET "http://localhost:8080/api/v1/posts" \
     -H "Authorization: Bearer <token>" \
     -H "Content-Type: application/json"

# Using HTTPie
http GET localhost:8080/api/v1/posts \
     Authorization:"Bearer <token>"
```

## 🔒 Security Guidelines

### Authentication Implementation
```java
@Service
public class AuthService {
    
    public AuthResponse authenticate(LoginRequest request) {
        // Validate credentials
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(), 
                request.getPassword()
            )
        );
        
        // Generate JWT token
        String token = jwtTokenProvider.generateToken(auth);
        
        return AuthResponse.builder()
            .token(token)
            .type("Bearer")
            .expiresIn(jwtTokenProvider.getExpirationTime())
            .build();
    }
}
```

### Authorization Configuration
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

## ⚡ Performance Optimization

### Database Optimization
```java
// Use @Query for complex queries
@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, UUID> {
    
    @Query("SELECT p FROM BlogPost p " +
           "LEFT JOIN FETCH p.author " +
           "LEFT JOIN FETCH p.category " +
           "WHERE p.status = :status")
    List<BlogPost> findPublishedPostsWithDetails(@Param("status") PostStatus status);
}

// Use projections for read-only data
public interface BlogPostSummary {
    UUID getId();
    String getTitle();
    String getAuthorName();
    LocalDateTime getCreatedAt();
}
```

### Caching Implementation
```java
@Service
@CacheConfig(cacheNames = "posts")
public class BlogPostService {
    
    @Cacheable(key = "#id")
    public BlogPost findById(UUID id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
    }
    
    @CacheEvict(key = "#post.id")
    public BlogPost save(BlogPost post) {
        return repository.save(post);
    }
    
    @CacheEvict(allEntries = true)
    public void clearCache() {
        // Cache cleared
    }
}
```

## 🔧 Troubleshooting

### Common Issues

1. **Port Already in Use**:
   ```bash
   # Find process using port 8080
   netstat -tulpn | grep 8080
   
   # Kill process
   kill -9 <PID>
   ```

2. **Database Connection Issues**:
   ```bash
   # Check PostgreSQL status
   docker-compose ps database
   
   # View database logs
   docker-compose logs database
   
   # Test connection
   psql -h localhost -U postgres -d personal_blog
   ```

3. **Memory Issues**:
   ```bash
   # Increase JVM memory
   export MAVEN_OPTS="-Xmx2g -Xms1g"
   ./mvnw spring-boot:run
   ```

4. **Test Failures**:
   ```bash
   # Run tests with debug output
   ./mvnw test -X
   
   # Run specific test
   ./mvnw test -Dtest=BlogPostServiceTest
   ```

### Logging Configuration
```properties
# application-dev.properties
logging.level.com.personalblog=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
```

### Health Checks
```bash
# Application health
curl http://localhost:8080/api/actuator/health

# Database health
curl http://localhost:8080/api/actuator/health/db

# Redis health
curl http://localhost:8080/api/actuator/health/redis
```

---

## 📚 Additional Resources

- [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [JPA and Hibernate Guide](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Testing in Spring Boot](https://spring.io/guides/gs/testing-web/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)

---

*Happy coding! 🚀*