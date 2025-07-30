# Personal Blog - Technology Stack Specification

## Core Framework & Runtime

### Java Platform
- **Java Version**: 21 LTS (Eclipse Temurin)
- **Key Features Used**:
  - Virtual Threads (Project Loom) for improved concurrency
  - Pattern Matching with `instanceof` and `switch` expressions
  - Records for immutable data structures (DTOs)
  - Text Blocks for multi-line strings (SQL, JSON templates)
  - Sealed Classes for domain modeling

### Spring Boot Ecosystem
- **Spring Boot**: 3.5.4
- **Spring Framework**: 6.2.x
- **Spring Security**: 6.5.2
- **Spring Data JPA**: 3.2.x
- **Spring Web MVC**: 6.2.x
- **Spring Boot Actuator**: 3.5.4
- **Spring Boot DevTools**: 3.5.4 (development only)

## Database & Persistence

### Primary Database
- **PostgreSQL**: 15.x or higher
- **JDBC Driver**: PostgreSQL JDBC 42.7.x
- **Connection Pool**: HikariCP 5.1.x (default with Spring Boot)
- **Migration Tool**: Flyway 10.x

### ORM & Data Access
- **Hibernate**: 6.6.22.Final
- **Spring Data JPA**: 3.2.x
- **Bean Validation**: 3.0.x (Jakarta Validation)
- **Jackson**: 2.16.x for JSON processing

### Testing Database
- **H2 Database**: 2.2.x (in-memory for tests)
- **TestContainers**: 1.19.x for integration testing

## Security Framework

### Authentication & Authorization
- **Spring Security**: 6.5.2
- **JWT Library**: `io.jsonwebtoken:jjwt-api:0.12.x`
- **Password Encoding**: BCrypt (Spring Security default)
- **Session Management**: Stateless with JWT tokens

### Security Features
- CSRF Protection for form submissions
- CORS Configuration for API endpoints
- Method-level security with `@PreAuthorize`
- Rate limiting with custom interceptors
- Input validation with Bean Validation API

## Frontend Technologies

### Template Engine
- **Thymeleaf**: 3.1.x
- **Thymeleaf Layout Dialect**: 3.3.x
- **Thymeleaf Spring Security**: 3.1.x

### Web Technologies
- **HTML5**: Semantic markup with accessibility features
- **CSS3**: Modern features (Grid, Flexbox, Custom Properties)
- **JavaScript**: ES6+ vanilla JavaScript (no frameworks)
- **Icons**: SVG-based icon system
- **Fonts**: System fonts with web font fallbacks

## Build & Development Tools

### Build System
- **Maven**: 3.9.x
- **Maven Wrapper**: Included for consistent builds
- **Spring Boot Maven Plugin**: 3.5.4
- **Maven Surefire Plugin**: 3.2.x (unit tests)
- **Maven Failsafe Plugin**: 3.2.x (integration tests)

### Code Quality Tools
- **SpotBugs**: 4.8.x for static analysis
- **PMD**: 7.0.x for code quality checks
- **Checkstyle**: 10.12.x for code style enforcement
- **JaCoCo**: 0.8.x for code coverage reporting

### Development Tools
- **Lombok**: 1.18.x for boilerplate reduction
- **MapStruct**: 1.5.x for bean mapping
- **Spring Boot DevTools**: Hot reloading and live reload

## Testing Framework

### Unit Testing
- **JUnit**: 5.10.x (Jupiter)
- **Mockito**: 5.7.x for mocking
- **AssertJ**: 3.24.x for fluent assertions
- **Spring Boot Test**: 3.5.4

### Integration Testing
- **TestContainers**: 1.19.x for database testing
- **Spring Boot Test**: `@SpringBootTest` for full context
- **MockMvc**: For web layer testing
- **WebMvcTest**: For controller testing
- **DataJpaTest**: For repository testing

## Logging & Monitoring

### Logging Framework
- **SLF4J**: 2.0.x (API)
- **Logback**: 1.4.x (implementation)
- **Spring Boot Logging**: Auto-configuration

### Monitoring & Metrics
- **Spring Boot Actuator**: 3.5.4
- **Micrometer**: 1.12.x for metrics collection
- **Prometheus**: Metrics format support

## Configuration Management

### Configuration Format
- **YAML**: Primary configuration format (`application.yml`)
- **Properties**: Fallback for simple configurations
- **Environment Variables**: For deployment-specific settings

### Configuration Features
- **Spring Profiles**: `dev`, `test`, `prod`
- **@ConfigurationProperties**: Type-safe configuration binding
- **Validation**: Bean Validation on configuration classes

## Containerization & Deployment

### Container Technology
- **Docker**: Latest stable version
- **Base Image**: Eclipse Temurin 21 JRE Alpine
- **Multi-stage Build**: Optimization for production images

### Container Tools
- **Jib Maven Plugin**: 3.4.x for container building
- **Docker Compose**: For local development environment

### Cloud Deployment
- **Railway**: Primary deployment platform
- **Render**: Alternative deployment platform
- **Environment**: 12-factor app compliance

## Performance & Caching

### Caching
- **Spring Cache**: Abstraction layer
- **Caffeine**: 3.1.x (local caching)
- **Redis**: Optional distributed caching

### Performance Features
- **Virtual Threads**: Java 21 for improved concurrency
- **Connection Pooling**: HikariCP optimization
- **Query Optimization**: JPA query hints and indexing
- **Compression**: Gzip compression for responses

## API Documentation

### Documentation Tools
- **OpenAPI 3**: API specification
- **SpringDoc OpenAPI**: 2.2.x for Spring Boot integration
- **Swagger UI**: Interactive API documentation

## Development Dependencies

### Maven Dependencies (Key Versions)
```xml
<properties>
    <java.version>21</java.version>
    <spring-boot.version>3.5.4</spring-boot.version>
    <postgresql.version>42.7.3</postgresql.version>
    <testcontainers.version>1.19.3</testcontainers.version>
    <lombok.version>1.18.30</lombok.version>
    <mapstruct.version>1.5.5.Final</mapstruct.version>
</properties>
```

### Production Dependencies
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Security
- Spring Boot Starter Thymeleaf
- Spring Boot Starter Actuator
- Spring Boot Starter Validation
- PostgreSQL Driver
- Flyway Core
- Lombok
- MapStruct

### Development Dependencies
- Spring Boot Starter Test
- Spring Security Test
- TestContainers PostgreSQL
- H2 Database
- Spring Boot DevTools

## Environment Specifications

### Development Environment
- **Java**: OpenJDK 21 or Eclipse Temurin 21
- **IDE**: IntelliJ IDEA or VS Code with Java extensions
- **Database**: PostgreSQL 15+ or H2 (embedded)
- **Build Tool**: Maven 3.9+

### Production Environment
- **Runtime**: Java 21 JRE (containerized)
- **Database**: PostgreSQL 15+ (managed service)
- **Memory**: Minimum 512MB, recommended 1GB+
- **Storage**: SSD with sufficient space for logs and data

## Security Considerations

### Runtime Security
- Non-root container execution
- Minimal base image (Alpine Linux)
- Regular security updates
- Environment variable secrets management

### Application Security
- Input validation and sanitization
- SQL injection prevention (parameterized queries)
- XSS protection with content security policy
- HTTPS enforcement in production
- Secure session management

This technology stack ensures a modern, secure, and maintainable blog application that follows current industry best practices and is ready for production deployment.