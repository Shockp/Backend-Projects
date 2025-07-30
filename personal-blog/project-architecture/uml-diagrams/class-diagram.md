# Class Diagram - Personal Blog Application

This diagram shows the static structure of the Personal Blog application, including entities, DTOs, services, controllers, and their relationships.

## Domain Model & Architecture

```mermaid
classDiagram
    %% Base Entity
    class BaseEntity {
        <<abstract>>
        -Long id
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        -String createdBy
        -String updatedBy
        +getId() Long
        +getCreatedAt() LocalDateTime
        +getUpdatedAt() LocalDateTime
    }

    %% Core Entities
    class User {
        -String username
        -String email
        -String password
        -String firstName
        -String lastName
        -UserRole role
        -boolean enabled
        -LocalDateTime lastLoginAt
        +getFullName() String
        +isAdmin() boolean
        +updateLastLogin() void
    }

    class BlogPost {
        -String title
        -String slug
        -String content
        -String excerpt
        -String featuredImage
        -PostStatus status
        -LocalDateTime publishedAt
        -Integer viewCount
        -User author
        -Category category
        -Set~Tag~ tags
        -List~Comment~ comments
        +isPublished() boolean
        +incrementViewCount() void
        +addTag(Tag) void
        +removeTag(Tag) void
    }

    class Category {
        -String name
        -String slug
        -String description
        -String color
        -List~BlogPost~ posts
        +getPostCount() int
    }

    class Tag {
        -String name
        -String slug
        -String color
        -Set~BlogPost~ posts
        +getPostCount() int
    }

    class Comment {
        -String content
        -String authorName
        -String authorEmail
        -String authorWebsite
        -CommentStatus status
        -BlogPost post
        -Comment parentComment
        -List~Comment~ replies
        +isApproved() boolean
        +addReply(Comment) void
    }

    class RefreshToken {
        -String token
        -LocalDateTime expiryDate
        -User user
        +isExpired() boolean
    }

    %% Enums
    class UserRole {
        <<enumeration>>
        ADMIN
        USER
    }

    class PostStatus {
        <<enumeration>>
        DRAFT
        PUBLISHED
        ARCHIVED
    }

    class CommentStatus {
        <<enumeration>>
        PENDING
        APPROVED
        REJECTED
        SPAM
    }

    %% DTOs - Request
    class LoginRequest {
        -String email
        -String password
        +validate() void
    }

    class BlogPostRequest {
        -String title
        -String content
        -String excerpt
        -String featuredImage
        -PostStatus status
        -Long categoryId
        -Set~Long~ tagIds
        +validate() void
        +toEntity() BlogPost
    }

    class CategoryRequest {
        -String name
        -String description
        -String color
        +validate() void
        +toEntity() Category
    }

    class CommentRequest {
        -String content
        -String authorName
        -String authorEmail
        -String authorWebsite
        -Long postId
        -Long parentCommentId
        +validate() void
        +toEntity() Comment
    }

    %% DTOs - Response
    class AuthResponse {
        -String accessToken
        -String refreshToken
        -String tokenType
        -Long expiresIn
        -UserResponse user
    }

    class BlogPostResponse {
        -Long id
        -String title
        -String slug
        -String content
        -String excerpt
        -String featuredImage
        -PostStatus status
        -LocalDateTime publishedAt
        -Integer viewCount
        -UserResponse author
        -CategoryResponse category
        -Set~TagResponse~ tags
        -List~CommentResponse~ comments
        +fromEntity(BlogPost) BlogPostResponse
    }

    class CategoryResponse {
        -Long id
        -String name
        -String slug
        -String description
        -String color
        -Integer postCount
        +fromEntity(Category) CategoryResponse
    }

    class UserResponse {
        -Long id
        -String username
        -String email
        -String firstName
        -String lastName
        -UserRole role
        +fromEntity(User) UserResponse
    }

    class ApiResponse~T~ {
        -boolean success
        -String message
        -T data
        -LocalDateTime timestamp
        +success(T) ApiResponse~T~
        +error(String) ApiResponse~T~
    }

    %% Services
    class AuthService {
        -UserRepository userRepository
        -RefreshTokenRepository refreshTokenRepository
        -PasswordEncoder passwordEncoder
        -JwtTokenProvider jwtTokenProvider
        +login(LoginRequest) AuthResponse
        +refreshToken(String) AuthResponse
        +logout(String) void
        +validateToken(String) boolean
    }

    class BlogPostService {
        -BlogPostRepository blogPostRepository
        -CategoryRepository categoryRepository
        -TagRepository tagRepository
        -CacheService cacheService
        +findAll(Pageable) Page~BlogPostResponse~
        +findBySlug(String) BlogPostResponse
        +create(BlogPostRequest) BlogPostResponse
        +update(Long, BlogPostRequest) BlogPostResponse
        +delete(Long) void
        +incrementViewCount(Long) void
    }

    class CategoryService {
        -CategoryRepository categoryRepository
        +findAll() List~CategoryResponse~
        +findBySlug(String) CategoryResponse
        +create(CategoryRequest) CategoryResponse
        +update(Long, CategoryRequest) CategoryResponse
        +delete(Long) void
    }

    class SearchService {
        -BlogPostRepository blogPostRepository
        +searchPosts(String, Pageable) Page~BlogPostResponse~
        +searchByCategory(String, Pageable) Page~BlogPostResponse~
        +searchByTag(String, Pageable) Page~BlogPostResponse~
    }

    %% Controllers
    class AuthController {
        -AuthService authService
        +login(LoginRequest) ResponseEntity~AuthResponse~
        +refreshToken(RefreshTokenRequest) ResponseEntity~AuthResponse~
        +logout(HttpServletRequest) ResponseEntity~Void~
    }

    class BlogPostController {
        -BlogPostService blogPostService
        +getAllPosts(Pageable) ResponseEntity~Page~BlogPostResponse~~
        +getPostBySlug(String) ResponseEntity~BlogPostResponse~
        +createPost(BlogPostRequest) ResponseEntity~BlogPostResponse~
        +updatePost(Long, BlogPostRequest) ResponseEntity~BlogPostResponse~
        +deletePost(Long) ResponseEntity~Void~
    }

    class HomeController {
        -BlogPostService blogPostService
        -CategoryService categoryService
        +home(Model) String
        +post(String, Model) String
        +category(String, Model) String
        +search(String, Model) String
    }

    %% Security
    class JwtTokenProvider {
        -String jwtSecret
        -int jwtExpirationMs
        +generateToken(UserDetails) String
        +getUsernameFromToken(String) String
        +validateToken(String) boolean
        +getExpirationDateFromToken(String) Date
    }

    class CustomUserDetailsService {
        -UserRepository userRepository
        +loadUserByUsername(String) UserDetails
    }

    %% Repositories
    class UserRepository {
        <<interface>>
        +findByEmail(String) Optional~User~
        +findByUsername(String) Optional~User~
        +existsByEmail(String) boolean
        +existsByUsername(String) boolean
    }

    class BlogPostRepository {
        <<interface>>
        +findBySlug(String) Optional~BlogPost~
        +findByStatus(PostStatus, Pageable) Page~BlogPost~
        +findByCategory(Category, Pageable) Page~BlogPost~
        +findByTagsContaining(Tag, Pageable) Page~BlogPost~
        +searchByTitleOrContent(String, Pageable) Page~BlogPost~
    }

    class CategoryRepository {
        <<interface>>
        +findBySlug(String) Optional~Category~
        +findByName(String) Optional~Category~
    }

    %% Relationships
    BaseEntity <|-- User
    BaseEntity <|-- BlogPost
    BaseEntity <|-- Category
    BaseEntity <|-- Tag
    BaseEntity <|-- Comment
    BaseEntity <|-- RefreshToken

    User ||--o{ BlogPost : "authors"
    User ||--o{ RefreshToken : "owns"
    User ||--|| UserRole : "has"
    
    BlogPost ||--|| Category : "belongs to"
    BlogPost }o--o{ Tag : "tagged with"
    BlogPost ||--o{ Comment : "has"
    BlogPost ||--|| PostStatus : "has"
    
    Comment ||--o{ Comment : "replies"
    Comment ||--|| CommentStatus : "has"
    
    %% Service Dependencies
    AuthController --> AuthService
    BlogPostController --> BlogPostService
    HomeController --> BlogPostService
    HomeController --> CategoryService
    
    AuthService --> UserRepository
    AuthService --> RefreshTokenRepository
    AuthService --> JwtTokenProvider
    
    BlogPostService --> BlogPostRepository
    BlogPostService --> CategoryRepository
    BlogPostService --> TagRepository
    
    CategoryService --> CategoryRepository
    SearchService --> BlogPostRepository
    
    CustomUserDetailsService --> UserRepository
```

## Key Design Patterns

### 1. **Repository Pattern**
- Clean separation between data access and business logic
- Spring Data JPA repositories with custom query methods
- Type-safe query methods with proper naming conventions

### 2. **DTO Pattern**
- Request DTOs for input validation and data transfer
- Response DTOs for controlled data exposure
- Separation of internal entities from API contracts

### 3. **Service Layer Pattern**
- Business logic encapsulation in service classes
- Transaction management at service level
- Clear separation of concerns

### 4. **Builder Pattern**
- Used in DTOs and entities for object construction
- Immutable response objects where appropriate
- Fluent API for complex object creation

## Architectural Layers

### **Controller Layer**
- REST API endpoints and web controllers
- Request validation and response formatting
- HTTP-specific concerns (status codes, headers)

### **Service Layer**
- Business logic implementation
- Transaction boundaries
- Cross-cutting concerns (caching, security)

### **Repository Layer**
- Data access abstraction
- Query optimization
- Database-specific operations

### **Entity Layer**
- Domain model representation
- JPA mappings and relationships
- Business rules and constraints

## Security Integration

### **Authentication Flow**
1. `AuthController` receives login request
2. `AuthService` validates credentials
3. `JwtTokenProvider` generates tokens
4. `RefreshToken` entity manages token lifecycle

### **Authorization**
- Role-based access control with `UserRole` enum
- Method-level security annotations
- JWT token validation in security filters

## Data Relationships

### **Core Relationships**
- **User → BlogPost**: One-to-Many (author relationship)
- **Category → BlogPost**: One-to-Many (categorization)
- **BlogPost ↔ Tag**: Many-to-Many (tagging system)
- **BlogPost → Comment**: One-to-Many (commenting)
- **Comment → Comment**: Self-referencing (reply system)

### **Audit Trail**
- `BaseEntity` provides audit fields for all entities
- Automatic timestamp management
- User tracking for create/update operations

---
*This class diagram represents the complete domain model and architecture for the Personal Blog application, following Spring Boot 3.5.4 and Java 21 best practices.*