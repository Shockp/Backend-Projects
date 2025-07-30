## 📂 Folder Structure

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