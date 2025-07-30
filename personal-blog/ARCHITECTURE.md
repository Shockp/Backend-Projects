# Personal Blog - Architecture Documentation

This document provides a comprehensive overview of the Personal Blog application architecture, design patterns, and implementation details.

## 📋 Table of Contents

1. [System Overview](#system-overview)
2. [Architecture Patterns](#architecture-patterns)
3. [Technology Stack](#technology-stack)
4. [Project Structure](#project-structure)
5. [Database Design](#database-design)
6. [Security Architecture](#security-architecture)
7. [API Design](#api-design)
8. [Caching Strategy](#caching-strategy)
9. [Error Handling](#error-handling)
10. [Testing Strategy](#testing-strategy)
11. [Deployment Architecture](#deployment-architecture)
12. [Performance Considerations](#performance-considerations)

## 🏗 System Overview

The Personal Blog application is a modern, scalable web application built using Spring Boot 3.5.4 with Java 21. It follows a layered architecture pattern with clear separation of concerns.

### Key Features
- User authentication and authorization
- Blog post management (CRUD operations)
- Category and tag system
- Comment system with moderation
- Full-text search capabilities
- File upload and management
- Email notifications
- Admin dashboard
- RESTful API with OpenAPI documentation

## 🏛 Architecture Patterns

### 1. Layered Architecture
```
┌─────────────────────────────────────┐
│           Presentation Layer        │
│         (Controllers, DTOs)         │
├─────────────────────────────────────┤
│            Service Layer            │
│        (Business Logic)             │
├─────────────────────────────────────┤
│          Repository Layer           │
│        (Data Access)                │
├─────────────────────────────────────┤
│            Entity Layer             │
│        (Domain Models)              │
└─────────────────────────────────────┘
```

### 2. Design Patterns Used
- **Repository Pattern**: Data access abstraction
- **Service Layer Pattern**: Business logic encapsulation
- **DTO Pattern**: Data transfer between layers
- **Builder Pattern**: Complex object construction
- **Strategy Pattern**: Multiple authentication strategies
- **Observer Pattern**: Event-driven notifications
- **Singleton Pattern**: Configuration management

## 🛠 Technology Stack

### Core Framework
- **Spring Boot 3.5.4**: Main application framework
- **Spring Security 6.5.2**: Authentication and authorization
- **Spring Data JPA**: Data persistence
- **Spring Cache**: Caching abstraction
- **Spring Mail**: Email functionality
- **Spring Validation**: Input validation

### Database & Storage
- **PostgreSQL 15+**: Primary database
- **Redis 7+**: Caching and session storage
- **H2**: In-memory database for testing

### Build & Deployment
- **Maven 3.9.6**: Build automation
- **Docker**: Containerization
- **Docker Compose**: Multi-container orchestration
- **Nginx**: Reverse proxy and load balancing

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/personalblog/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── entity/         # JPA entities
│   │   ├── exception/      # Custom exceptions
│   │   ├── repository/     # Data repositories
│   │   ├── security/       # Security configuration
│   │   ├── service/        # Business logic
│   │   └── util/          # Utility classes
│   └── resources/
│       ├── application*.properties
│       ├── static/         # Static resources
│       └── templates/      # Email templates
└── test/
    ├── java/com/personalblog/
    │   ├── controller/     # Controller tests
    │   ├── service/       # Service tests
    │   └── repository/    # Repository tests
    └── resources/
        └── application-test.properties
```

### Layer Responsibilities

#### Controllers (`controller/`)
- Handle HTTP requests and responses
- Input validation and sanitization
- Request/response mapping
- Exception handling delegation

#### Services (`service/`)
- Business logic implementation
- Transaction management
- Cross-cutting concerns (logging, caching)
- Integration with external services

#### Repositories (`repository/`)
- Data access abstraction
- Custom query implementations
- Database-specific optimizations

#### Entities (`entity/`)
- Domain model representation
- JPA mappings and relationships
- Business rules and constraints

## 🗄 Database Design

### Entity Relationship Diagram
```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│    User     │    │  BlogPost   │    │  Category   │
├─────────────┤    ├─────────────┤    ├─────────────┤
│ id (PK)     │    │ id (PK)     │    │ id (PK)     │
│ username    │    │ title       │    │ name        │
│ email       │    │ content     │    │ description │
│ password    │    │ author_id   │────┤ slug        │
│ role        │    │ category_id │────┤ parent_id   │
│ created_at  │    │ status      │    │ created_at  │
│ updated_at  │    │ created_at  │    │ updated_at  │
└─────────────┘    │ updated_at  │    └─────────────┘
       │           └─────────────┘           │
       │                  │                 │
       │                  │                 │
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Comment   │    │ PostTag     │    │     Tag     │
├─────────────┤    ├─────────────┤    ├─────────────┤
│ id (PK)     │    │ post_id     │    │ id (PK)     │
│ content     │    │ tag_id      │    │ name        │
│ author_id   │────┤             │────┤ slug        │
│ post_id     │────┤             │    │ description │
│ parent_id   │    │             │    │ created_at  │
│ status      │    │             │    │ updated_at  │
│ created_at  │    │             │    └─────────────┘
│ updated_at  │    └─────────────┘
└─────────────┘
```

### Key Design Decisions

1. **UUID Primary Keys**: For better security and distributed systems support
2. **Soft Deletes**: Maintain data integrity and audit trails
3. **Optimistic Locking**: Prevent concurrent modification conflicts
4. **Indexing Strategy**: Optimized for common query patterns
5. **Normalization**: 3NF compliance with performance considerations

## 🔒 Security Architecture

### Authentication Flow
```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Client    │    │   Gateway   │    │   Service   │
└─────────────┘    └─────────────┘    └─────────────┘
       │                  │                  │
       │ 1. Login Request │                  │
       ├─────────────────→│                  │
       │                  │ 2. Authenticate  │
       │                  ├─────────────────→│
       │                  │ 3. JWT Token     │
       │                  │←─────────────────┤
       │ 4. JWT Response  │                  │
       │←─────────────────┤                  │
       │                  │                  │
       │ 5. API Request   │                  │
       │   + JWT Token    │                  │
       ├─────────────────→│ 6. Validate JWT  │
       │                  ├─────────────────→│
       │                  │ 7. Process       │
       │                  │←─────────────────┤
       │ 8. Response      │                  │
       │←─────────────────┤                  │
```

### Security Features

1. **JWT Authentication**: Stateless token-based authentication
2. **Role-based Authorization**: ADMIN, USER, MODERATOR roles
3. **Password Encryption**: BCrypt with configurable strength
4. **CORS Configuration**: Configurable cross-origin policies
5. **Rate Limiting**: Request throttling to prevent abuse
6. **Input Validation**: Comprehensive validation at all layers
7. **SQL Injection Prevention**: Parameterized queries
8. **XSS Protection**: Input sanitization and output encoding

## 🌐 API Design

### RESTful Principles

- **Resource-based URLs**: `/api/posts`, `/api/users`
- **HTTP Methods**: GET, POST, PUT, DELETE
- **Status Codes**: Proper HTTP status code usage
- **Content Negotiation**: JSON primary, XML support
- **Versioning**: URL-based versioning (`/api/v1/`)

### API Structure
```
/api/v1/
├── /auth
│   ├── POST /login
│   ├── POST /register
│   ├── POST /refresh
│   └── POST /logout
├── /posts
│   ├── GET    /posts
│   ├── POST   /posts
│   ├── GET    /posts/{id}
│   ├── PUT    /posts/{id}
│   └── DELETE /posts/{id}
├── /categories
│   ├── GET    /categories
│   ├── POST   /categories
│   ├── GET    /categories/{id}
│   ├── PUT    /categories/{id}
│   └── DELETE /categories/{id}
└── /users
    ├── GET    /users/profile
    ├── PUT    /users/profile
    └── POST   /users/change-password
```

### Response Format
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "title": "Blog Post Title",
    "content": "Post content...",
    "author": {
      "id": "uuid",
      "username": "author"
    },
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z"
  },
  "message": "Operation successful",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## 🚀 Caching Strategy

### Multi-level Caching

1. **Application Level**: Spring Cache with Redis
2. **Database Level**: PostgreSQL query cache
3. **HTTP Level**: Nginx caching for static content

### Cache Configuration
```java
@Cacheable(value = "posts", key = "#id")
public BlogPost findById(UUID id) {
    return blogPostRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
}

@CacheEvict(value = "posts", key = "#post.id")
public BlogPost updatePost(BlogPost post) {
    return blogPostRepository.save(post);
}
```

### Cache Invalidation Strategy
- **Time-based**: TTL for different data types
- **Event-based**: Cache eviction on data modifications
- **Manual**: Admin interface for cache management

## ⚠️ Error Handling

### Exception Hierarchy
```
RuntimeException
├── BusinessException
│   ├── ResourceNotFoundException
│   ├── DuplicateResourceException
│   └── InvalidOperationException
├── SecurityException
│   ├── UnauthorizedException
│   └── ForbiddenException
└── ValidationException
    ├── InvalidInputException
    └── ConstraintViolationException
```

### Global Exception Handler
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .message(ex.getMessage())
            .timestamp(Instant.now())
            .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
```

## 🧪 Testing Strategy

### Testing Pyramid
```
        ┌─────────────────┐
        │   E2E Tests     │  ← Few, High-level
        │   (Selenium)    │
        ├─────────────────┤
        │ Integration     │  ← Some, API-level
        │ Tests (Spring)  │
        ├─────────────────┤
        │   Unit Tests    │  ← Many, Fast
        │   (JUnit 5)     │
        └─────────────────┘
```

### Test Categories

1. **Unit Tests**: Individual component testing
2. **Integration Tests**: Component interaction testing
3. **Repository Tests**: Database interaction testing
4. **Security Tests**: Authentication and authorization testing
5. **Performance Tests**: Load and stress testing

### Test Configuration
- **Test Containers**: Real database testing
- **Test Profiles**: Isolated test environments
- **Mock Objects**: External service mocking
- **Test Data Builders**: Consistent test data creation

## 🚀 Deployment Architecture

### Container Architecture
```
┌─────────────────────────────────────────────────────────┐
│                    Docker Host                          │
├─────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │
│  │    Nginx    │  │   Spring    │  │ PostgreSQL  │     │
│  │   (Proxy)   │  │    Boot     │  │ (Database)  │     │
│  │             │  │             │  │             │     │
│  └─────────────┘  └─────────────┘  └─────────────┘     │
│         │                 │                 │          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │
│  │   Redis     │  │   Volumes   │  │   Networks  │     │
│  │  (Cache)    │  │  (Storage)  │  │ (Internal)  │     │
│  │             │  │             │  │             │     │
│  └─────────────┘  └─────────────┘  └─────────────┘     │
└─────────────────────────────────────────────────────────┘
```

### Environment Configuration

- **Development**: Local Docker Compose
- **Staging**: Cloud-based containers
- **Production**: Orchestrated deployment (Kubernetes/Docker Swarm)

## ⚡ Performance Considerations

### Database Optimization
1. **Indexing Strategy**: Composite indexes for common queries
2. **Connection Pooling**: HikariCP configuration
3. **Query Optimization**: N+1 problem prevention
4. **Pagination**: Efficient large dataset handling

### Application Optimization
1. **Virtual Threads**: Java 21 virtual threads for concurrency
2. **Lazy Loading**: JPA lazy loading configuration
3. **Caching**: Multi-level caching strategy
4. **Compression**: Response compression

### Monitoring and Metrics
1. **Spring Boot Actuator**: Health checks and metrics
2. **Application Metrics**: Custom business metrics
3. **Database Metrics**: Query performance monitoring
4. **Cache Metrics**: Hit/miss ratios and performance

---

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Reference](https://spring.io/projects/spring-security)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Redis Documentation](https://redis.io/documentation)
- [Docker Documentation](https://docs.docker.com/)

---

*This architecture document is a living document and should be updated as the system evolves.*