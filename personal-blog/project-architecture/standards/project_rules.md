# Spring Boot Personal Blog - Project Rules

## Project Overview
Personal blog application built with Spring Boot 3.5.4, PostgreSQL, Thymeleaf, and modern Java development practices. This document defines the coding standards, architecture patterns, and development guidelines specific to this project.

## Technology Stack Requirements

### Core Technologies
- **Java**: 21 LTS with modern language features (virtual threads, pattern matching, records)
- **Spring Boot**: 3.5.4 with auto-configuration and embedded Tomcat
- **Database**: PostgreSQL 15+ for production, H2 for testing
- **ORM**: Spring Data JPA with Hibernate 6.6.22.Final
- **Security**: Spring Security 6.5.2 with JWT authentication
- **Frontend**: Thymeleaf 3.1+ with HTML5, CSS3, Vanilla JavaScript
- **Build**: Maven 3.9.x with Spring Boot Maven Plugin
- **Testing**: JUnit 5 + Mockito + Spring Boot Test + TestContainers
- **Deployment**: Docker with Railway/Render

## Code Organization & Architecture

### Package Structure
com.shockp.blogpersonal/
├── config/ # Configuration classes
├── controller/ # REST controllers and MVC controllers
├── dto/ # Data Transfer Objects
├── entity/ # JPA entities
├── repository/ # Spring Data repositories
├── service/ # Business logic services
├── security/ # Security configuration and filters
├── exception/ # Custom exceptions and handlers


### Layer Separation Rules
- **Controllers**: Handle HTTP requests, validation, and response formatting
- **Services**: Contain business logic, transaction management
- **Repositories**: Data access layer with custom queries
- **DTOs**: Never expose entities directly in APIs
- **Entities**: JPA entities with proper relationships and constraints

## Java Coding Standards

### Modern Java Features (Java 21)
- **USE** virtual threads for I/O operations: `@Async` with virtual thread executor
- **USE** records for immutable data structures: DTOs, value objects
- **USE** pattern matching where applicable: `switch` expressions, `instanceof`
- **USE** text blocks for multi-line strings (SQL queries, JSON templates)

### Code Style Requirements
- **Constructor Injection**: Always use constructor injection over `@Autowired` fields
- **Configuration Properties**: Use `@ConfigurationProperties` instead of `@Value`
- **Exception Handling**: Implement `@ControllerAdvice` for global exception handling
- **Validation**: Use Bean Validation API with custom validators
- **Logging**: Use SLF4J with structured logging patterns

### Naming Conventions
- **Classes**: PascalCase (e.g., `BlogPostService`, `UserController`)
- **Methods**: camelCase with descriptive names (e.g., `findPublishedPostsByCategory`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `DEFAULT_PAGE_SIZE`)
- **Package names**: lowercase, singular nouns (e.g., `controller`, `service`)

## Spring Boot Specific Rules

### Configuration
- Use `application.yml` for configuration (not .properties)
- Implement environment-specific profiles: `dev`, `test`, `prod`
- Configure health checks and actuator endpoints
- Use `@ConfigurationProperties` for grouped settings

### Data Access Layer
- Use Spring Data JPA repository interfaces
- Implement custom queries with `@Query` annotation
- Apply proper transaction boundaries with `@Transactional`
- Use database migrations with Flyway
- Implement audit fields: `createdAt`, `updatedAt`, `createdBy`

### Security Implementation
- Configure JWT authentication with refresh tokens
- Implement CSRF protection for form submissions
- Use method-level security with `@PreAuthorize`
- Validate all user inputs with Bean Validation
- Implement rate limiting for API endpoints

## Database Design Rules

### Entity Design
- Use `@Entity` with proper table mapping
- Implement bidirectional relationships carefully
- Use `@JsonIgnore` to prevent circular references
- Add unique constraints and indexes
- Implement soft delete where appropriate

### Query Optimization
- Use pagination for list endpoints
- Implement lazy loading for collections
- Add database indexes for frequently queried fields
- Use native queries for complex operations
- Monitor query performance with Hibernate statistics

## Frontend Guidelines (Thymeleaf)

### Template Organization
- Create reusable fragments in `fragments/` directory
- Use layout templates with Thymeleaf Layout Dialect
- Implement responsive design with CSS Grid/Flexbox
- Follow semantic HTML5 structure
- Ensure WCAG 2.1 accessibility compliance

### Form Handling
- Use Spring MVC form binding with DTOs
- Implement proper form validation with error display
- Apply CSRF protection for all forms
- Use `th:field` for form binding
- Handle file uploads securely

## Testing Requirements

### Test Coverage Goals
- **Service Layer**: 95%+ coverage with unit tests
- **Controller Layer**: Integration tests with MockMvc
- **Repository Layer**: Use `@DataJpaTest` with TestContainers
- **Security**: Test authentication and authorization flows

### Testing Patterns
- Follow AAA pattern: Arrange, Act, Assert
- Use `@MockBean` for Spring context testing
- Implement test data builders for complex objects
- Create separate test profiles with H2 database
- Use TestContainers for integration tests

## Security & Production Rules

### Security Requirements
- Never log sensitive information (passwords, tokens)
- Validate all inputs at controller and service levels
- Use HTTPS in production with proper certificates
- Implement proper session management
- Follow OWASP security guidelines

### Performance Optimization
- Implement caching with Spring Cache abstraction
- Use virtual threads for concurrent operations
- Optimize database queries and use connection pooling
- Implement proper error handling and circuit breakers
- Monitor application metrics with Actuator

## API Design Standards

### REST API Guidelines
- Follow RESTful URL conventions: `/api/v1/posts`
- Use proper HTTP status codes
- Implement proper error response format
- Add API versioning strategy
- Document APIs with OpenAPI 3.0

### Response Format
{
"success": true,
"data": {...},
"message": "Operation completed successfully",
"timestamp": "2025-07-28T23:00:00Z"
}


## Documentation Requirements

### Code Documentation
- Write comprehensive Javadoc for public APIs
- Document complex business logic with inline comments
- Maintain up-to-date README.md with setup instructions
- Document API endpoints with examples
- Create architecture decision records (ADRs)

### Testing Documentation
- Document test scenarios and edge cases
- Maintain test data setup instructions
- Document mock strategies and test patterns

## Build & Deployment

### Maven Configuration
- Use Spring Boot parent POM 3.5.4
- Configure proper dependency management
- Implement code quality plugins: SpotBugs, PMD, Checkstyle
- Set up test execution with Surefire/Failsafe
- Configure Docker image building with Jib plugin

### Docker Best Practices
- Use multi-stage builds for optimization
- Run containers as non-root user
- Use Eclipse Temurin 21 JRE base image
- Implement proper health checks
- Configure resource limits and security contexts

## Error Handling Standards

### Exception Hierarchy
- Create custom exception classes extending `RuntimeException`
- Implement global exception handling with `@ControllerAdvice`
- Return consistent error responses with proper HTTP status codes
- Log exceptions with appropriate levels (ERROR, WARN, INFO)

### Validation Rules
- Use Bean Validation annotations on DTOs
- Implement custom validators for business rules
- Provide meaningful error messages for validation failures
- Handle validation exceptions globally

## Blog-Specific Business Rules

### Content Management
- Posts must have title, content, category, and publication status
- Support draft and published status for posts
- Implement post scheduling functionality
- Support multiple tags per post
- Enable rich text editing with sanitization

### User Management
- Admin-only access for post creation/editing
- Public read access for published posts
- Session-based authentication for admin users
- Implement user profile management

### SEO & Performance
- Generate SEO-friendly URLs (slugs)
- Implement meta tags and Open Graph properties
- Add sitemap generation
- Optimize images and implement lazy loading
- Configure proper caching headers

## Prohibited Practices

### Avoid These Patterns
- **DON'T** use `@Autowired` on fields (use constructor injection)
- **DON'T** expose entities directly in REST APIs
- **DON'T** ignore exception handling
- **DON'T** use `String` concatenation for SQL queries
- **DON'T** store sensitive data in version control
- **DON'T** skip input validation
- **DON'T** use default Spring Security configuration in production

### Performance Anti-Patterns
- **DON'T** use N+1 queries (use proper fetch strategies)
- **DON'T** load large datasets without pagination
- **DON'T** ignore database indexing
- **DON'T** block virtual threads with synchronous operations