# UML Diagrams & Project Structure

This directory contains UML diagrams and the complete project structure for the Personal Blog application.

## 📁 Complete Project Structure

```
personal-blog/
├── .gitignore
├── README.md
├── pom.xml
├── docker-compose.yml
├── Dockerfile
├── .env.example
├── project-architecture/
│   ├── README.md
│   ├── core/
│   │   ├── README.md
│   │   ├── project_overview.md
│   │   ├── tech_stack.md
│   │   ├── architecture_decisions.md
│   │   └── api_specification.md
│   ├── development/
│   │   ├── README.md
│   │   ├── development_guide.md
│   │   └── testing_strategy.md
│   ├── operations/
│   │   ├── README.md
│   │   ├── deployment_guide.md
│   │   └── security_guidelines.md
│   ├── standards/
│   │   ├── README.md
│   │   ├── project_rules.md
│   │   └── user_rules.md
│   └── uml-diagrams/
│       ├── README.md
│       ├── class-diagram.md
│       ├── component-diagram.md
│       ├── sequence-diagram.md
│       ├── use-case-diagram.md
│       └── package-diagram.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── personalblog/
│   │   │           ├── PersonalBlogApplication.java
│   │   │           ├── config/
│   │   │           │   ├── SecurityConfig.java
│   │   │           │   ├── JwtConfig.java
│   │   │           │   ├── DatabaseConfig.java
│   │   │           │   ├── CacheConfig.java
│   │   │           │   ├── WebConfig.java
│   │   │           │   └── AsyncConfig.java
│   │   │           ├── controller/
│   │   │           │   ├── AuthController.java
│   │   │           │   ├── BlogPostController.java
│   │   │           │   ├── CategoryController.java
│   │   │           │   ├── TagController.java
│   │   │           │   ├── CommentController.java
│   │   │           │   ├── AdminController.java
│   │   │           │   ├── SearchController.java
│   │   │           │   └── HomeController.java
│   │   │           ├── dto/
│   │   │           │   ├── request/
│   │   │           │   │   ├── LoginRequest.java
│   │   │           │   │   ├── BlogPostRequest.java
│   │   │           │   │   ├── CategoryRequest.java
│   │   │           │   │   ├── TagRequest.java
│   │   │           │   │   ├── CommentRequest.java
│   │   │           │   │   └── SearchRequest.java
│   │   │           │   └── response/
│   │   │           │       ├── AuthResponse.java
│   │   │           │       ├── BlogPostResponse.java
│   │   │           │       ├── CategoryResponse.java
│   │   │           │       ├── TagResponse.java
│   │   │           │       ├── CommentResponse.java
│   │   │           │       ├── SearchResponse.java
│   │   │           │       ├── ApiResponse.java
│   │   │           │       └── ErrorResponse.java
│   │   │           ├── entity/
│   │   │           │   ├── User.java
│   │   │           │   ├── BlogPost.java
│   │   │           │   ├── Category.java
│   │   │           │   ├── Tag.java
│   │   │           │   ├── Comment.java
│   │   │           │   ├── BaseEntity.java
│   │   │           │   └── RefreshToken.java
│   │   │           ├── repository/
│   │   │           │   ├── UserRepository.java
│   │   │           │   ├── BlogPostRepository.java
│   │   │           │   ├── CategoryRepository.java
│   │   │           │   ├── TagRepository.java
│   │   │           │   ├── CommentRepository.java
│   │   │           │   └── RefreshTokenRepository.java
│   │   │           ├── service/
│   │   │           │   ├── AuthService.java
│   │   │           │   ├── BlogPostService.java
│   │   │           │   ├── CategoryService.java
│   │   │           │   ├── TagService.java
│   │   │           │   ├── CommentService.java
│   │   │           │   ├── SearchService.java
│   │   │           │   ├── EmailService.java
│   │   │           │   ├── CacheService.java
│   │   │           │   └── FileUploadService.java
│   │   │           ├── security/
│   │   │           │   ├── JwtAuthenticationFilter.java
│   │   │           │   ├── JwtTokenProvider.java
│   │   │           │   ├── CustomUserDetailsService.java
│   │   │           │   ├── SecurityUtils.java
│   │   │           │   └── RateLimitingFilter.java
│   │   │           ├── exception/
│   │   │           │   ├── GlobalExceptionHandler.java
│   │   │           │   ├── ResourceNotFoundException.java
│   │   │           │   ├── UnauthorizedException.java
│   │   │           │   ├── ValidationException.java
│   │   │           │   ├── DuplicateResourceException.java
│   │   │           │   └── ServiceException.java
│   │   │           ├── util/
│   │   │           │   ├── DateUtils.java
│   │   │           │   ├── StringUtils.java
│   │   │           │   ├── ValidationUtils.java
│   │   │           │   ├── SlugUtils.java
│   │   │           │   └── ImageUtils.java
│   │   │           └── validation/
│   │   │               ├── ValidEmail.java
│   │   │               ├── ValidPassword.java
│   │   │               ├── ValidSlug.java
│   │   │               └── UniqueEmail.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       ├── application-test.yml
│   │       ├── db/
│   │       │   └── migration/
│   │       │       ├── V1__Create_users_table.sql
│   │       │       ├── V2__Create_categories_table.sql
│   │       │       ├── V3__Create_tags_table.sql
│   │       │       ├── V4__Create_blog_posts_table.sql
│   │       │       ├── V5__Create_comments_table.sql
│   │       │       ├── V6__Create_refresh_tokens_table.sql
│   │       │       ├── V7__Create_blog_post_tags_table.sql
│   │       │       └── V8__Add_indexes.sql
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   ├── main.css
│   │       │   │   ├── blog.css
│   │       │   │   ├── admin.css
│   │       │   │   └── responsive.css
│   │       │   ├── js/
│   │       │   │   ├── main.js
│   │       │   │   ├── blog.js
│   │       │   │   ├── admin.js
│   │       │   │   └── search.js
│   │       │   ├── images/
│   │       │   │   ├── logo.svg
│   │       │   │   ├── favicon.ico
│   │       │   │   └── placeholder.svg
│   │       │   └── fonts/
│   │       │       └── (web fonts if needed)
│   │       ├── templates/
│   │       │   ├── layout/
│   │       │   │   ├── base.html
│   │       │   │   ├── header.html
│   │       │   │   ├── footer.html
│   │       │   │   └── sidebar.html
│   │       │   ├── blog/
│   │       │   │   ├── index.html
│   │       │   │   ├── post.html
│   │       │   │   ├── category.html
│   │       │   │   ├── tag.html
│   │       │   │   └── search.html
│   │       │   ├── auth/
│   │       │   │   ├── login.html
│   │       │   │   └── register.html
│   │       │   ├── admin/
│   │       │   │   ├── dashboard.html
│   │       │   │   ├── posts.html
│   │       │   │   ├── post-form.html
│   │       │   │   ├── categories.html
│   │       │   │   ├── tags.html
│   │       │   │   └── analytics.html
│   │       │   └── error/
│   │       │       ├── 404.html
│   │       │       ├── 500.html
│   │       │       └── access-denied.html
│   │       └── messages/
│   │           ├── messages.properties
│   │           ├── messages_en.properties
│   │           └── messages_es.properties
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── personalblog/
│       │           ├── PersonalBlogApplicationTests.java
│       │           ├── controller/
│       │           │   ├── AuthControllerTest.java
│       │           │   ├── BlogPostControllerTest.java
│       │           │   ├── CategoryControllerTest.java
│       │           │   ├── TagControllerTest.java
│       │           │   └── CommentControllerTest.java
│       │           ├── service/
│       │           │   ├── AuthServiceTest.java
│       │           │   ├── BlogPostServiceTest.java
│       │           │   ├── CategoryServiceTest.java
│       │           │   ├── TagServiceTest.java
│       │           │   └── CommentServiceTest.java
│       │           ├── repository/
│       │           │   ├── UserRepositoryTest.java
│       │           │   ├── BlogPostRepositoryTest.java
│       │           │   ├── CategoryRepositoryTest.java
│       │           │   ├── TagRepositoryTest.java
│       │           │   └── CommentRepositoryTest.java
│       │           ├── security/
│       │           │   ├── JwtTokenProviderTest.java
│       │           │   └── SecurityConfigTest.java
│       │           ├── integration/
│       │           │   ├── BlogPostIntegrationTest.java
│       │           │   ├── AuthIntegrationTest.java
│       │           │   └── SearchIntegrationTest.java
│       │           └── util/
│       │               ├── TestDataBuilder.java
│       │               ├── TestContainerConfig.java
│       │               └── MockMvcTestBase.java
│       └── resources/
│           ├── application-test.yml
│           ├── test-data.sql
│           └── logback-test.xml
└── scripts/
    ├── build.sh
    ├── deploy.sh
    ├── backup-db.sh
    └── restore-db.sh
```

## 📊 UML Diagrams Overview

This directory contains comprehensive UML diagrams for the Personal Blog application, providing visual documentation of the system architecture, design patterns, and component relationships.

### 🎯 Available Diagrams

#### **1. Class Diagram** ✅
- **File**: [`class-diagram.md`](./class-diagram.md)
- **Purpose**: Show the static structure of the system
- **Content**: Entities, DTOs, Services, Controllers, and their relationships
- **Focus**: Object-oriented design and data model relationships
- **Key Features**: JPA entities, service layer design, DTO patterns, repository interfaces

#### **2. Component Diagram** ✅
- **File**: [`component-diagram.md`](./component-diagram.md)
- **Purpose**: Illustrate high-level system architecture
- **Content**: Application layers, external systems, and interfaces
- **Focus**: System decomposition and component interactions
- **Key Features**: Layered architecture, external integrations, deployment view

#### **3. Sequence Diagram** ✅
- **File**: [`sequence-diagram.md`](./sequence-diagram.md)
- **Purpose**: Show interaction flows for key use cases
- **Content**: Authentication, blog post creation, search functionality, comment submission
- **Focus**: Time-ordered interactions between system components
- **Key Features**: JWT authentication flow, caching strategies, async operations

#### **4. Use Case Diagram** ✅
- **File**: [`use-case-diagram.md`](./use-case-diagram.md)
- **Purpose**: Define functional requirements and user interactions
- **Content**: User roles, system boundaries, and use case relationships
- **Focus**: System functionality from user perspective
- **Key Features**: Role-based access control, inheritance relationships, business rules

#### **5. Package Diagram** ✅
- **File**: [`package-diagram.md`](./package-diagram.md)
- **Purpose**: Show the organization of system modules
- **Content**: Package structure, dependencies, and architectural layers
- **Focus**: Code organization and module relationships
- **Key Features**: Clean architecture layers, dependency flow, Spring Boot patterns

## 🎯 Key Features Represented

### Core Functionality
- **Blog Management**: Create, read, update, delete blog posts
- **Category & Tag System**: Organize content with categories and tags
- **Comment System**: User engagement through comments
- **Search Functionality**: Full-text search across blog content
- **User Authentication**: JWT-based authentication and authorization

### Technical Features
- **Security**: Spring Security 6.5.2 with JWT tokens
- **Database**: PostgreSQL with Flyway migrations
- **Caching**: Redis for performance optimization
- **Testing**: Comprehensive test suite with TestContainers
- **API**: RESTful API with proper error handling
- **Frontend**: Thymeleaf templates with responsive design

### Operational Features
- **Monitoring**: Spring Boot Actuator endpoints
- **Logging**: Structured logging with SLF4J
- **Deployment**: Docker containerization
- **Database Migration**: Flyway for version control
- **Environment Configuration**: Profile-based configuration

## 🔧 Technology Stack Integration

- **Java 21**: Virtual threads, pattern matching, records
- **Spring Boot 3.5.4**: Latest features and security updates
- **Spring Security 6.5.2**: Modern authentication patterns
- **Spring Data JPA**: Database abstraction and repositories
- **PostgreSQL 15+**: Advanced database features
- **Thymeleaf**: Server-side template engine
- **Maven**: Dependency management and build tool
- **Docker**: Containerization and deployment

## 📝 Diagram Creation Guidelines

1. **Mermaid Syntax**: All diagrams use Mermaid.js syntax for consistency
2. **Clarity**: Focus on essential elements, avoid overcomplification
3. **Consistency**: Use consistent naming and styling across diagrams
4. **Documentation**: Each diagram includes explanatory text
5. **Maintenance**: Update diagrams when architecture changes

---
*This structure represents a production-ready Spring Boot blog application following 2025 best practices and modern Java development standards.*