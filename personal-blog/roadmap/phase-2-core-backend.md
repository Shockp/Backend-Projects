# Phase 2: Core Backend Development

**Status**: 🔄 In Progress  
**Duration**: 1-2 weeks  
**Completion**: 0%

## 🎯 Objectives

Implement the core backend functionality including entities, repositories, services, and basic REST API endpoints for the Personal Blog application.

## 📋 Tasks

### 1. Entity Implementation ⏳

#### 1.1 Base Entity ⏳
- [ ] Implement `BaseEntity` with:
  - UUID primary key
  - Created/updated timestamps
  - Version field for optimistic locking
  - Soft delete support
  - JPA annotations
  - Audit fields

#### 1.2 User Entity ⏳
- [ ] Implement `User` entity with:
  - Basic user information (username, email, firstName, lastName)
  - Password field with validation
  - Role enumeration (ADMIN, AUTHOR, USER)
  - Account status fields (enabled, locked, expired)
  - Profile information (bio, avatar, website)
  - Social media links
  - Email verification status
  - Last login timestamp
  - JPA relationships
  - Bean validation annotations

#### 1.3 BlogPost Entity ⏳
- [ ] Implement `BlogPost` entity with:
  - Title and slug (URL-friendly)
  - Content (rich text support)
  - Excerpt/summary
  - Publication status (DRAFT, PUBLISHED, ARCHIVED)
  - Publication date
  - Featured image URL
  - SEO metadata (meta title, description, keywords)
  - View count
  - Reading time estimation
  - Author relationship (Many-to-One with User)
  - Category relationship (Many-to-One with Category)
  - Tags relationship (Many-to-Many with Tag)
  - Comments relationship (One-to-Many with Comment)
  - JPA annotations and indexes

#### 1.4 Category Entity ⏳
- [ ] Implement `Category` entity with:
  - Name and slug
  - Description
  - Color code for UI
  - Parent category (self-referencing for hierarchy)
  - Display order
  - SEO metadata
  - Blog posts relationship (One-to-Many)
  - JPA annotations

#### 1.5 Tag Entity ⏳
- [ ] Implement `Tag` entity with:
  - Name and slug
  - Description
  - Color code
  - Usage count
  - Blog posts relationship (Many-to-Many)
  - JPA annotations

#### 1.6 Comment Entity ⏳
- [ ] Implement `Comment` entity with:
  - Content
  - Author information (User or guest)
  - Email for guest comments
  - Status (PENDING, APPROVED, REJECTED, SPAM)
  - Parent comment (for replies)
  - Blog post relationship
  - IP address tracking
  - User agent information
  - JPA annotations

#### 1.7 RefreshToken Entity ⏳
- [ ] Implement `RefreshToken` entity with:
  - Token value (UUID)
  - Expiry date
  - User relationship
  - Device information
  - JPA annotations

### 2. Repository Layer ⏳

#### 2.1 Base Repository ⏳
- [ ] Create `BaseRepository` interface with:
  - Common query methods
  - Soft delete support
  - Pagination utilities
  - Specification support

#### 2.2 User Repository ⏳
- [ ] Implement `UserRepository` with:
  - Find by username/email
  - Find by role
  - Search users by name
  - Account status queries
  - Custom queries for user statistics
  - Native queries for complex operations

#### 2.3 BlogPost Repository ⏳
- [ ] Implement `BlogPostRepository` with:
  - Find published posts
  - Find by author
  - Find by category/tags
  - Search by title/content
  - Find featured posts
  - Date range queries
  - Popular posts (by views)
  - Related posts queries
  - Full-text search support

#### 2.4 Category Repository ⏳
- [ ] Implement `CategoryRepository` with:
  - Find by slug
  - Hierarchical queries
  - Find with post counts
  - Order by display order

#### 2.5 Tag Repository ⏳
- [ ] Implement `TagRepository` with:
  - Find by slug
  - Popular tags (by usage)
  - Search by name
  - Tag cloud queries

#### 2.6 Comment Repository ⏳
- [ ] Implement `CommentRepository` with:
  - Find by blog post
  - Find by status
  - Find by author
  - Hierarchical comment queries
  - Moderation queries

#### 2.7 RefreshToken Repository ⏳
- [ ] Implement `RefreshTokenRepository` with:
  - Find by token
  - Find by user
  - Delete expired tokens
  - Find by device

### 3. Service Layer ⏳

#### 3.1 User Service ⏳
- [ ] Implement `UserService` with:
  - User registration
  - User profile management
  - Password change/reset
  - Email verification
  - User search and filtering
  - Account activation/deactivation
  - Role management
  - User statistics
  - Avatar upload handling

#### 3.2 BlogPost Service ⏳
- [ ] Implement `BlogPostService` with:
  - Create/update/delete posts
  - Publish/unpublish posts
  - Draft management
  - Slug generation
  - SEO optimization
  - Reading time calculation
  - View count tracking
  - Featured image handling
  - Related posts suggestion
  - Post scheduling
  - Bulk operations

#### 3.3 Category Service ⏳
- [ ] Implement `CategoryService` with:
  - CRUD operations
  - Hierarchy management
  - Slug generation
  - Category statistics
  - Reordering categories

#### 3.4 Tag Service ⏳
- [ ] Implement `TagService` with:
  - CRUD operations
  - Tag suggestion
  - Usage statistics
  - Tag merging
  - Popular tags

#### 3.5 Comment Service ⏳
- [ ] Implement `CommentService` with:
  - Comment submission
  - Comment moderation
  - Reply handling
  - Spam detection
  - Comment statistics
  - Notification handling

### 4. DTO Implementation ⏳

#### 4.1 Request DTOs ⏳
- [ ] `UserRegistrationRequest`
- [ ] `UserUpdateRequest`
- [ ] `LoginRequest`
- [ ] `PasswordChangeRequest`
- [ ] `BlogPostCreateRequest`
- [ ] `BlogPostUpdateRequest`
- [ ] `CategoryCreateRequest`
- [ ] `TagCreateRequest`
- [ ] `CommentCreateRequest`

#### 4.2 Response DTOs ⏳
- [ ] `UserResponse`
- [ ] `UserProfileResponse`
- [ ] `BlogPostResponse`
- [ ] `BlogPostSummaryResponse`
- [ ] `CategoryResponse`
- [ ] `TagResponse`
- [ ] `CommentResponse`
- [ ] `AuthResponse`

#### 4.3 DTO Mappers ⏳
- [ ] Implement MapStruct mappers for:
  - Entity to DTO conversion
  - DTO to Entity conversion
  - Custom mapping logic
  - Nested object mapping

### 5. Basic REST Controllers ⏳

#### 5.1 User Controller ⏳
- [ ] Implement `UserController` with endpoints:
  - `GET /api/users/profile` - Get current user profile
  - `PUT /api/users/profile` - Update user profile
  - `POST /api/users/change-password` - Change password
  - `GET /api/users/{id}` - Get user by ID
  - `GET /api/users` - List users (admin)

#### 5.2 BlogPost Controller ⏳
- [ ] Implement `BlogPostController` with endpoints:
  - `GET /api/posts` - List published posts
  - `GET /api/posts/{slug}` - Get post by slug
  - `POST /api/posts` - Create new post
  - `PUT /api/posts/{id}` - Update post
  - `DELETE /api/posts/{id}` - Delete post
  - `POST /api/posts/{id}/publish` - Publish post
  - `GET /api/posts/drafts` - List drafts
  - `GET /api/posts/search` - Search posts

#### 5.3 Category Controller ⏳
- [ ] Implement `CategoryController` with endpoints:
  - `GET /api/categories` - List categories
  - `GET /api/categories/{slug}` - Get category by slug
  - `POST /api/categories` - Create category
  - `PUT /api/categories/{id}` - Update category
  - `DELETE /api/categories/{id}` - Delete category

#### 5.4 Tag Controller ⏳
- [ ] Implement `TagController` with endpoints:
  - `GET /api/tags` - List tags
  - `GET /api/tags/popular` - Get popular tags
  - `POST /api/tags` - Create tag
  - `PUT /api/tags/{id}` - Update tag
  - `DELETE /api/tags/{id}` - Delete tag

#### 5.5 Comment Controller ⏳
- [ ] Implement `CommentController` with endpoints:
  - `GET /api/posts/{postId}/comments` - Get post comments
  - `POST /api/posts/{postId}/comments` - Add comment
  - `PUT /api/comments/{id}` - Update comment
  - `DELETE /api/comments/{id}` - Delete comment
  - `POST /api/comments/{id}/approve` - Approve comment (admin)

### 6. Configuration Classes ⏳

#### 6.1 Database Configuration ⏳
- [ ] Implement `DatabaseConfig` with:
  - JPA configuration
  - Connection pool settings
  - Transaction management
  - Audit configuration

#### 6.2 Cache Configuration ⏳
- [ ] Implement `CacheConfig` with:
  - Redis configuration
  - Cache managers
  - TTL settings
  - Cache key generation

#### 6.3 Web Configuration ⏳
- [ ] Implement `WebConfig` with:
  - CORS configuration
  - Message converters
  - Interceptors
  - Static resource handling

### 7. Validation & Error Handling ⏳

#### 7.1 Custom Validators ⏳
- [ ] `UniqueUsername` validator
- [ ] `UniqueEmail` validator
- [ ] `ValidSlug` validator
- [ ] `ValidPassword` validator
- [ ] `ValidImageUrl` validator

#### 7.2 Exception Classes ⏳
- [ ] `DuplicateResourceException`
- [ ] `InvalidOperationException`
- [ ] `BusinessLogicException`
- [ ] `DataIntegrityException`

#### 7.3 Global Exception Handler ⏳
- [ ] Enhance `GlobalExceptionHandler` with:
  - Validation error handling
  - Database constraint violations
  - Business logic exceptions
  - Proper HTTP status codes
  - Error response formatting

### 8. Database Migrations ⏳

#### 8.1 Flyway Setup ⏳
- [ ] Configure Flyway for database migrations
- [ ] Create initial migration scripts:
  - `V1__Create_users_table.sql`
  - `V2__Create_categories_table.sql`
  - `V3__Create_tags_table.sql`
  - `V4__Create_blog_posts_table.sql`
  - `V5__Create_comments_table.sql`
  - `V6__Create_refresh_tokens_table.sql`
  - `V7__Create_indexes.sql`
  - `V8__Insert_default_data.sql`

#### 8.2 Test Data ⏳
- [ ] Create sample data for development:
  - Admin user
  - Sample categories
  - Sample tags
  - Sample blog posts
  - Sample comments

## 🧪 Testing Tasks ⏳

### 1. Unit Tests ⏳
- [ ] Entity tests with validation
- [ ] Repository tests with test data
- [ ] Service layer tests with mocking
- [ ] DTO mapper tests
- [ ] Validator tests

### 2. Integration Tests ⏳
- [ ] Controller integration tests
- [ ] Database integration tests
- [ ] Cache integration tests
- [ ] End-to-end API tests

### 3. Test Data Management ⏳
- [ ] Test data builders
- [ ] Database test containers
- [ ] Test profiles configuration

## 📊 Success Criteria

- [ ] All entities properly implemented with relationships
- [ ] Repository layer with custom queries working
- [ ] Service layer with business logic implemented
- [ ] Basic CRUD operations via REST APIs
- [ ] Proper validation and error handling
- [ ] Database migrations working
- [ ] Unit tests with >80% coverage
- [ ] Integration tests passing
- [ ] API documentation generated
- [ ] Performance benchmarks established

## 🔍 Quality Checks

- [ ] Code review completed
- [ ] All tests passing
- [ ] Code coverage meets requirements
- [ ] Performance tests completed
- [ ] Security review completed
- [ ] Documentation updated
- [ ] API endpoints tested with Postman/Swagger

## 📈 Deliverables

1. **Complete Entity Layer**: All JPA entities with relationships
2. **Repository Layer**: Custom queries and specifications
3. **Service Layer**: Business logic implementation
4. **DTO Layer**: Request/response objects with mappers
5. **REST Controllers**: Basic CRUD endpoints
6. **Database Schema**: Flyway migrations
7. **Validation Framework**: Custom validators
8. **Error Handling**: Comprehensive exception handling
9. **Test Suite**: Unit and integration tests
10. **API Documentation**: OpenAPI/Swagger documentation

## 🚀 Next Steps

Upon completion of Phase 2, the application will have:
- Complete data model
- Working REST APIs
- Database persistence
- Basic validation
- Test coverage

**Next Phase**: [Phase 3 - Security & Authentication](./phase-3-security.md)

---

**Phase 2 Status**: 🔄 **IN PROGRESS**  
**Previous Phase**: [Phase 1 - Foundation & Setup](./phase-1-foundation.md)  
**Next Phase**: [Phase 3 - Security & Authentication](./phase-3-security.md)