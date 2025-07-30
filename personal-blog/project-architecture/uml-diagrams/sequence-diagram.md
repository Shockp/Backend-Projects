# Sequence Diagram - Personal Blog Application

This diagram shows the interaction flows for key use cases in the Personal Blog application, illustrating time-ordered interactions between system components.

## User Authentication Flow

```mermaid
sequenceDiagram
    participant U as User
    participant W as Web Browser
    participant AC as Auth Controller
    participant AS as Auth Service
    participant UR as User Repository
    participant JTP as JWT Token Provider
    participant RTR as Refresh Token Repository
    participant R as Redis Cache
    participant DB as PostgreSQL

    Note over U,DB: User Login Process
    
    U->>W: Enter credentials
    W->>AC: POST /api/auth/login {email, password}
    
    AC->>AC: Validate request format
    AC->>AS: login(LoginRequest)
    
    AS->>UR: findByEmail(email)
    UR->>DB: SELECT * FROM users WHERE email = ?
    DB-->>UR: User entity
    UR-->>AS: Optional<User>
    
    alt User not found
        AS-->>AC: throw UnauthorizedException
        AC-->>W: 401 Unauthorized
        W-->>U: Invalid credentials
    else User found
        AS->>AS: passwordEncoder.matches(password, user.password)
        
        alt Password invalid
            AS-->>AC: throw UnauthorizedException
            AC-->>W: 401 Unauthorized
            W-->>U: Invalid credentials
        else Password valid
            AS->>JTP: generateToken(userDetails)
            JTP->>JTP: Create JWT with claims
            JTP-->>AS: accessToken
            
            AS->>AS: generateRefreshToken()
            AS->>RTR: save(refreshToken)
            RTR->>DB: INSERT INTO refresh_tokens
            DB-->>RTR: Success
            RTR-->>AS: RefreshToken entity
            
            AS->>R: cache user session
            R-->>AS: Success
            
            AS->>AS: updateLastLogin(user)
            AS->>UR: save(user)
            UR->>DB: UPDATE users SET last_login_at = ?
            DB-->>UR: Success
            UR-->>AS: Updated user
            
            AS-->>AC: AuthResponse{accessToken, refreshToken, user}
            AC-->>W: 200 OK with tokens
            W->>W: Store tokens in localStorage
            W-->>U: Redirect to dashboard
        end
    end
```

## Blog Post Creation Flow

```mermaid
sequenceDiagram
    participant A as Admin User
    participant W as Web Browser
    participant JF as JWT Filter
    participant BPC as Blog Post Controller
    participant BPS as Blog Post Service
    participant CS as Cache Service
    participant BR as Blog Post Repository
    participant CR as Category Repository
    participant TR as Tag Repository
    participant R as Redis Cache
    participant DB as PostgreSQL

    Note over A,DB: Admin Creates New Blog Post
    
    A->>W: Fill post creation form
    W->>BPC: POST /api/admin/posts {title, content, categoryId, tagIds}
    Note right of W: Authorization: Bearer <JWT>
    
    BPC->>JF: Extract and validate JWT
    JF->>JF: validateToken(jwt)
    
    alt Invalid or expired token
        JF-->>BPC: 401 Unauthorized
        BPC-->>W: 401 Unauthorized
        W-->>A: Please login again
    else Valid token
        JF->>JF: Set SecurityContext
        JF-->>BPC: Continue with authenticated user
        
        BPC->>BPC: Validate request data
        BPC->>BPS: create(BlogPostRequest)
        
        BPS->>BPS: validatePostData(request)
        BPS->>BPS: generateSlug(title)
        BPS->>BPS: sanitizeContent(content)
        
        par Validate Category
            BPS->>CR: findById(categoryId)
            CR->>DB: SELECT * FROM categories WHERE id = ?
            DB-->>CR: Category entity
            CR-->>BPS: Optional<Category>
        and Validate Tags
            BPS->>TR: findAllById(tagIds)
            TR->>DB: SELECT * FROM tags WHERE id IN (?)
            DB-->>TR: List<Tag>
            TR-->>BPS: List<Tag>
        end
        
        alt Category or tags not found
            BPS-->>BPC: throw ValidationException
            BPC-->>W: 400 Bad Request
            W-->>A: Invalid category or tags
        else All valid
            BPS->>BPS: createBlogPost(data, category, tags)
            BPS->>BR: save(blogPost)
            
            BR->>DB: BEGIN TRANSACTION
            BR->>DB: INSERT INTO blog_posts (...)
            BR->>DB: INSERT INTO blog_post_tags (...)
            BR->>DB: COMMIT TRANSACTION
            DB-->>BR: BlogPost entity with ID
            BR-->>BPS: Saved BlogPost
            
            BPS->>CS: invalidateCache("blog_posts")
            CS->>R: DEL blog_posts:*
            R-->>CS: Success
            CS-->>BPS: Cache cleared
            
            BPS->>CS: invalidateCache("categories")
            CS->>R: DEL categories:*
            R-->>CS: Success
            CS-->>BPS: Cache cleared
            
            BPS->>BPS: convertToResponse(blogPost)
            BPS-->>BPC: BlogPostResponse
            BPC-->>W: 201 Created with post data
            W-->>A: Post created successfully
        end
    end
```

## Search Functionality Flow

```mermaid
sequenceDiagram
    participant U as User
    participant W as Web Browser
    participant SC as Search Controller
    participant SS as Search Service
    participant CS as Cache Service
    participant BR as Blog Post Repository
    participant R as Redis Cache
    participant DB as PostgreSQL

    Note over U,DB: User Searches for Blog Posts
    
    U->>W: Enter search query
    W->>SC: GET /api/search?q=spring boot&page=0&size=10
    
    SC->>SC: Validate search parameters
    SC->>SS: searchPosts(query, pageable)
    
    SS->>SS: sanitizeQuery(query)
    SS->>SS: generateCacheKey(query, pageable)
    
    SS->>CS: get(cacheKey)
    CS->>R: GET search:spring_boot:page_0:size_10
    
    alt Cache hit
        R-->>CS: Cached search results
        CS-->>SS: Page<BlogPostResponse>
        SS-->>SC: Cached results
        SC-->>W: 200 OK with results
        W-->>U: Display search results
    else Cache miss
        R-->>CS: null
        CS-->>SS: null
        
        SS->>BR: searchByTitleOrContent(query, pageable)
        BR->>DB: Full-text search query
        Note right of DB: SELECT * FROM blog_posts<br/>WHERE to_tsvector('english', title || ' ' || content)<br/>@@ plainto_tsquery('english', ?)<br/>AND status = 'PUBLISHED'<br/>ORDER BY ts_rank(...) DESC<br/>LIMIT ? OFFSET ?
        
        DB-->>BR: List<BlogPost> with total count
        BR-->>SS: Page<BlogPost>
        
        SS->>SS: convertToResponsePage(posts)
        
        par Cache results
            SS->>CS: put(cacheKey, results, TTL=300s)
            CS->>R: SETEX search:spring_boot:page_0:size_10 300 <results>
            R-->>CS: Success
            CS-->>SS: Cached
        and Log search
            SS->>SS: logSearchQuery(query, resultCount)
        end
        
        SS-->>SC: Page<BlogPostResponse>
        SC-->>W: 200 OK with results
        W-->>U: Display search results
    end
```

## Blog Post Viewing Flow

```mermaid
sequenceDiagram
    participant U as User
    participant W as Web Browser
    participant HC as Home Controller
    participant BPS as Blog Post Service
    participant CS as Cache Service
    participant BR as Blog Post Repository
    participant R as Redis Cache
    participant DB as PostgreSQL

    Note over U,DB: User Views Blog Post
    
    U->>W: Click on blog post link
    W->>HC: GET /post/spring-boot-tutorial
    
    HC->>BPS: findBySlug("spring-boot-tutorial")
    
    BPS->>CS: get("post:spring-boot-tutorial")
    CS->>R: GET post:spring-boot-tutorial
    
    alt Cache hit
        R-->>CS: Cached blog post
        CS-->>BPS: BlogPostResponse
        
        par Increment view count (async)
            BPS->>BPS: incrementViewCountAsync(postId)
            BPS->>BR: incrementViewCount(postId)
            BR->>DB: UPDATE blog_posts SET view_count = view_count + 1
            DB-->>BR: Success
            BR-->>BPS: Updated
        end
        
        BPS-->>HC: BlogPostResponse
        HC->>HC: addToModel(post)
        HC-->>W: Render post.html template
        W-->>U: Display blog post
        
    else Cache miss
        R-->>CS: null
        CS-->>BPS: null
        
        BPS->>BR: findBySlugAndStatus(slug, PUBLISHED)
        BR->>DB: SELECT * FROM blog_posts WHERE slug = ? AND status = 'PUBLISHED'
        DB-->>BR: BlogPost entity
        BR-->>BPS: Optional<BlogPost>
        
        alt Post not found
            BPS-->>HC: throw ResourceNotFoundException
            HC-->>W: 404 Not Found
            W-->>U: Post not found page
        else Post found
            BPS->>BPS: convertToResponse(blogPost)
            
            par Cache post
                BPS->>CS: put("post:" + slug, response, TTL=3600s)
                CS->>R: SETEX post:spring-boot-tutorial 3600 <post_data>
                R-->>CS: Success
                CS-->>BPS: Cached
            and Increment view count
                BPS->>BR: incrementViewCount(postId)
                BR->>DB: UPDATE blog_posts SET view_count = view_count + 1
                DB-->>BR: Success
                BR-->>BPS: Updated
            end
            
            BPS-->>HC: BlogPostResponse
            HC->>HC: addToModel(post)
            HC-->>W: Render post.html template
            W-->>U: Display blog post
        end
    end
```

## Comment Submission Flow

```mermaid
sequenceDiagram
    participant U as User
    participant W as Web Browser
    participant CC as Comment Controller
    participant ComS as Comment Service
    participant ES as Email Service
    participant ComR as Comment Repository
    participant BR as Blog Post Repository
    participant DB as PostgreSQL
    participant SMTP as SMTP Server

    Note over U,SMTP: User Submits Comment
    
    U->>W: Fill comment form
    W->>CC: POST /api/comments {content, authorName, authorEmail, postId}
    
    CC->>CC: Validate request data
    CC->>ComS: create(CommentRequest)
    
    ComS->>ComS: validateCommentData(request)
    ComS->>ComS: sanitizeContent(content)
    ComS->>ComS: validateEmail(authorEmail)
    
    ComS->>BR: findById(postId)
    BR->>DB: SELECT * FROM blog_posts WHERE id = ?
    DB-->>BR: BlogPost entity
    BR-->>ComS: Optional<BlogPost>
    
    alt Post not found
        ComS-->>CC: throw ResourceNotFoundException
        CC-->>W: 404 Not Found
        W-->>U: Post not found
    else Post found
        ComS->>ComS: createComment(request, post)
        ComS->>ComS: setStatus(PENDING) // Moderation required
        
        ComS->>ComR: save(comment)
        ComR->>DB: INSERT INTO comments (...)
        DB-->>ComR: Comment entity with ID
        ComR-->>ComS: Saved Comment
        
        par Notify admin (async)
            ComS->>ES: sendCommentNotification(comment, post)
            ES->>ES: prepareNotificationEmail()
            ES->>SMTP: Send email to admin
            SMTP-->>ES: Email sent
            ES-->>ComS: Notification sent
        end
        
        ComS->>ComS: convertToResponse(comment)
        ComS-->>CC: CommentResponse
        CC-->>W: 201 Created
        W-->>U: Comment submitted for moderation
    end
```

## Token Refresh Flow

```mermaid
sequenceDiagram
    participant W as Web Browser
    participant AC as Auth Controller
    participant AS as Auth Service
    participant RTR as Refresh Token Repository
    participant JTP as JWT Token Provider
    participant R as Redis Cache
    participant DB as PostgreSQL

    Note over W,DB: Automatic Token Refresh
    
    W->>W: Detect token expiration
    W->>AC: POST /api/auth/refresh {refreshToken}
    
    AC->>AS: refreshToken(refreshTokenString)
    
    AS->>RTR: findByToken(refreshTokenString)
    RTR->>DB: SELECT * FROM refresh_tokens WHERE token = ?
    DB-->>RTR: RefreshToken entity
    RTR-->>AS: Optional<RefreshToken>
    
    alt Refresh token not found
        AS-->>AC: throw UnauthorizedException
        AC-->>W: 401 Unauthorized
        W->>W: Redirect to login
    else Refresh token found
        AS->>AS: validateRefreshToken(refreshToken)
        
        alt Token expired
            AS->>RTR: delete(refreshToken)
            RTR->>DB: DELETE FROM refresh_tokens WHERE id = ?
            DB-->>RTR: Success
            RTR-->>AS: Deleted
            
            AS-->>AC: throw UnauthorizedException
            AC-->>W: 401 Unauthorized
            W->>W: Redirect to login
        else Token valid
            AS->>JTP: generateToken(user)
            JTP->>JTP: Create new JWT
            JTP-->>AS: newAccessToken
            
            AS->>AS: generateNewRefreshToken()
            AS->>RTR: save(newRefreshToken)
            RTR->>DB: INSERT INTO refresh_tokens
            DB-->>RTR: Success
            RTR-->>AS: New RefreshToken
            
            AS->>RTR: delete(oldRefreshToken)
            RTR->>DB: DELETE FROM refresh_tokens WHERE id = ?
            DB-->>RTR: Success
            RTR-->>AS: Deleted
            
            AS->>R: Update user session cache
            R-->>AS: Success
            
            AS-->>AC: AuthResponse{newAccessToken, newRefreshToken}
            AC-->>W: 200 OK with new tokens
            W->>W: Update stored tokens
            W->>W: Continue with original request
        end
    end
```

## Key Interaction Patterns

### **Security-First Approach**
- Every API request goes through JWT validation
- Token refresh mechanism prevents session hijacking
- Input validation and sanitization at multiple layers

### **Performance Optimization**
- Multi-level caching strategy (Redis + application cache)
- Async operations for non-critical tasks
- Database query optimization with proper indexing

### **Error Handling**
- Graceful degradation for cache misses
- Comprehensive exception handling
- User-friendly error messages

### **Audit and Monitoring**
- Search query logging for analytics
- User activity tracking
- Performance metrics collection

### **Scalability Considerations**
- Stateless authentication with JWT
- Cache invalidation strategies
- Async processing for heavy operations

---
*These sequence diagrams represent the critical user flows in the Personal Blog application, designed for security, performance, and maintainability using Spring Boot 3.5.4 and modern Java practices.*