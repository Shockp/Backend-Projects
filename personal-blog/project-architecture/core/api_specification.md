# Personal Blog - API Specification

## API Overview

### Base Information
- **Base URL**: `https://api.personalblog.com/api/v1`
- **Authentication**: JWT Bearer Token
- **Content Type**: `application/json`
- **API Version**: v1
- **Rate Limiting**: 100 requests per minute per IP

### Response Format
All API responses follow a consistent structure:

```json
{
  "success": true,
  "data": {},
  "message": "Operation completed successfully",
  "timestamp": "2025-07-30T23:00:00Z",
  "errors": []
}
```

### Error Response Format
```json
{
  "success": false,
  "data": null,
  "message": "Validation failed",
  "timestamp": "2025-07-30T23:00:00Z",
  "errors": [
    {
      "field": "title",
      "code": "REQUIRED",
      "message": "Title is required"
    }
  ]
}
```

## Authentication Endpoints

### POST /auth/login
Authenticate admin user and receive JWT tokens.

**Request Body:**
```json
{
  "username": "admin",
  "password": "securepassword"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600,
    "tokenType": "Bearer"
  },
  "message": "Authentication successful",
  "timestamp": "2025-07-30T23:00:00Z"
}
```

**Error Responses:**
- `401 Unauthorized`: Invalid credentials
- `429 Too Many Requests`: Rate limit exceeded

### POST /auth/refresh
Refresh access token using refresh token.

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600,
    "tokenType": "Bearer"
  },
  "message": "Token refreshed successfully",
  "timestamp": "2025-07-30T23:00:00Z"
}
```

### POST /auth/logout
Invalidate current session and tokens.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": null,
  "message": "Logout successful",
  "timestamp": "2025-07-30T23:00:00Z"
}
```

## Blog Posts Endpoints

### GET /posts
Retrieve published blog posts with pagination and filtering.

**Query Parameters:**
- `page` (integer, default: 0): Page number
- `size` (integer, default: 10, max: 50): Page size
- `category` (string, optional): Filter by category slug
- `tag` (string, optional): Filter by tag name
- `search` (string, optional): Search in title and content
- `sort` (string, default: "publishedAt,desc"): Sort criteria

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Getting Started with Spring Boot 3.5",
        "slug": "getting-started-spring-boot-35",
        "excerpt": "Learn the basics of Spring Boot 3.5...",
        "content": "Full content here...",
        "publishedAt": "2025-07-30T10:00:00Z",
        "updatedAt": "2025-07-30T10:00:00Z",
        "readingTime": 5,
        "category": {
          "id": 1,
          "name": "Spring Boot",
          "slug": "spring-boot"
        },
        "tags": [
          {
            "id": 1,
            "name": "Java",
            "slug": "java"
          }
        ],
        "author": {
          "id": 1,
          "name": "Admin User",
          "email": "admin@example.com"
        }
      }
    ],
    "pageable": {
      "page": 0,
      "size": 10,
      "sort": "publishedAt,desc"
    },
    "totalElements": 25,
    "totalPages": 3,
    "first": true,
    "last": false
  },
  "message": "Posts retrieved successfully",
  "timestamp": "2025-07-30T23:00:00Z"
}
```

### GET /posts/{id}
Retrieve a specific blog post by ID.

**Path Parameters:**
- `id` (integer): Post ID

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Getting Started with Spring Boot 3.5",
    "slug": "getting-started-spring-boot-35",
    "content": "Full content here...",
    "publishedAt": "2025-07-30T10:00:00Z",
    "updatedAt": "2025-07-30T10:00:00Z",
    "readingTime": 5,
    "category": {
      "id": 1,
      "name": "Spring Boot",
      "slug": "spring-boot"
    },
    "tags": [
      {
        "id": 1,
        "name": "Java",
        "slug": "java"
      }
    ],
    "author": {
      "id": 1,
      "name": "Admin User",
      "email": "admin@example.com"
    },
    "metaTags": {
      "title": "Getting Started with Spring Boot 3.5 - Personal Blog",
      "description": "Learn the basics of Spring Boot 3.5...",
      "keywords": "spring boot, java, tutorial"
    }
  },
  "message": "Post retrieved successfully",
  "timestamp": "2025-07-30T23:00:00Z"
}
```

**Error Responses:**
- `404 Not Found`: Post not found or not published

### GET /posts/slug/{slug}
Retrieve a blog post by its slug (SEO-friendly URL).

**Path Parameters:**
- `slug` (string): Post slug

**Response:** Same as GET /posts/{id}

## Admin Endpoints (Authentication Required)

### GET /admin/posts
Retrieve all posts (including drafts) for admin management.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Query Parameters:**
- `page` (integer, default: 0): Page number
- `size` (integer, default: 10): Page size
- `status` (string, optional): Filter by status (DRAFT, PUBLISHED, SCHEDULED)
- `sort` (string, default: "updatedAt,desc"): Sort criteria

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Getting Started with Spring Boot 3.5",
        "slug": "getting-started-spring-boot-35",
        "status": "PUBLISHED",
        "publishedAt": "2025-07-30T10:00:00Z",
        "createdAt": "2025-07-29T15:00:00Z",
        "updatedAt": "2025-07-30T10:00:00Z",
        "category": {
          "id": 1,
          "name": "Spring Boot",
          "slug": "spring-boot"
        },
        "tags": [
          {
            "id": 1,
            "name": "Java",
            "slug": "java"
          }
        ]
      }
    ],
    "pageable": {
      "page": 0,
      "size": 10,
      "sort": "updatedAt,desc"
    },
    "totalElements": 25,
    "totalPages": 3
  },
  "message": "Admin posts retrieved successfully",
  "timestamp": "2025-07-30T23:00:00Z"
}
```

### POST /admin/posts
Create a new blog post.

**Headers:**
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "title": "New Blog Post Title",
  "content": "Full content of the blog post...",
  "excerpt": "Short excerpt for the post...",
  "status": "DRAFT",
  "publishedAt": "2025-07-31T10:00:00Z",
  "categoryId": 1,
  "tagIds": [1, 2, 3],
  "metaTags": {
    "title": "Custom meta title",
    "description": "Custom meta description",
    "keywords": "keyword1, keyword2"
  }
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 26,
    "title": "New Blog Post Title",
    "slug": "new-blog-post-title",
    "content": "Full content of the blog post...",
    "excerpt": "Short excerpt for the post...",
    "status": "DRAFT",
    "publishedAt": "2025-07-31T10:00:00Z",
    "createdAt": "2025-07-30T23:00:00Z",
    "updatedAt": "2025-07-30T23:00:00Z",
    "category": {
      "id": 1,
      "name": "Spring Boot",
      "slug": "spring-boot"
    },
    "tags": [
      {
        "id": 1,
        "name": "Java",
        "slug": "java"
      }
    ]
  },
  "message": "Post created successfully",
  "timestamp": "2025-07-30T23:00:00Z"
}
```

**Validation Rules:**
- `title`: Required, 1-200 characters
- `content`: Required, minimum 10 characters
- `excerpt`: Optional, maximum 500 characters
- `status`: Required, one of [DRAFT, PUBLISHED, SCHEDULED]
- `publishedAt`: Required if status is PUBLISHED or SCHEDULED
- `categoryId`: Required, must exist
- `tagIds`: Optional, all IDs must exist

### PUT /admin/posts/{id}
Update an existing blog post.

**Path Parameters:**
- `id` (integer): Post ID

**Headers:**
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body:** Same as POST /admin/posts

**Response (200 OK):** Same structure as POST response

**Error Responses:**
- `404 Not Found`: Post not found
- `400 Bad Request`: Validation errors

### DELETE /admin/posts/{id}
Delete a blog post.

**Path Parameters:**
- `id` (integer): Post ID

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": null,
  "message": "Post deleted successfully",
  "timestamp": "2025-07-30T23:00:00Z"
}
```

## Categories Endpoints

### GET /categories
Retrieve all categories.

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Spring Boot",
      "slug": "spring-boot",
      "description": "Articles about Spring Boot framework",
      "postCount": 15,
      "createdAt": "2025-07-01T10:00:00Z"
    }
  ],
  "message": "Categories retrieved successfully",
  "timestamp": "2025-07-30T23:00:00Z"
}
```

### POST /admin/categories
Create a new category (Admin only).

**Headers:**
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "New Category",
  "description": "Description of the new category"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 5,
    "name": "New Category",
    "slug": "new-category",
    "description": "Description of the new category",
    "postCount": 0,
    "createdAt": "2025-07-30T23:00:00Z"
  },
  "message": "Category created successfully",
  "timestamp": "2025-07-30T23:00:00Z"
}
```

## Tags Endpoints

### GET /tags
Retrieve all tags.

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Java",
      "slug": "java",
      "postCount": 20,
      "createdAt": "2025-07-01T10:00:00Z"
    }
  ],
  "message": "Tags retrieved successfully",
  "timestamp": "2025-07-30T23:00:00Z"
}
```

### POST /admin/tags
Create a new tag (Admin only).

**Headers:**
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "New Tag"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 15,
    "name": "New Tag",
    "slug": "new-tag",
    "postCount": 0,
    "createdAt": "2025-07-30T23:00:00Z"
  },
  "message": "Tag created successfully",
  "timestamp": "2025-07-30T23:00:00Z"
}
```

## Search Endpoints

### GET /search
Search across blog posts.

**Query Parameters:**
- `q` (string, required): Search query
- `page` (integer, default: 0): Page number
- `size` (integer, default: 10): Page size
- `category` (string, optional): Filter by category
- `tag` (string, optional): Filter by tag

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "query": "spring boot",
    "results": [
      {
        "id": 1,
        "title": "Getting Started with Spring Boot 3.5",
        "slug": "getting-started-spring-boot-35",
        "excerpt": "Learn the basics of Spring Boot 3.5...",
        "publishedAt": "2025-07-30T10:00:00Z",
        "category": {
          "name": "Spring Boot",
          "slug": "spring-boot"
        },
        "relevanceScore": 0.95
      }
    ],
    "totalResults": 5,
    "totalPages": 1,
    "page": 0,
    "size": 10
  },
  "message": "Search completed successfully",
  "timestamp": "2025-07-30T23:00:00Z"
}
```

## Analytics Endpoints (Admin Only)

### GET /admin/analytics/overview
Get blog analytics overview.

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "totalPosts": 25,
    "publishedPosts": 20,
    "draftPosts": 5,
    "totalCategories": 8,
    "totalTags": 15,
    "postsThisMonth": 3,
    "popularPosts": [
      {
        "id": 1,
        "title": "Getting Started with Spring Boot 3.5",
        "views": 1250
      }
    ]
  },
  "message": "Analytics retrieved successfully",
  "timestamp": "2025-07-30T23:00:00Z"
}
```

## Error Codes

### HTTP Status Codes
- `200 OK`: Request successful
- `201 Created`: Resource created successfully
- `400 Bad Request`: Invalid request data
- `401 Unauthorized`: Authentication required
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `409 Conflict`: Resource conflict (e.g., duplicate slug)
- `422 Unprocessable Entity`: Validation errors
- `429 Too Many Requests`: Rate limit exceeded
- `500 Internal Server Error`: Server error

### Custom Error Codes
- `VALIDATION_ERROR`: Input validation failed
- `AUTHENTICATION_FAILED`: Invalid credentials
- `TOKEN_EXPIRED`: JWT token expired
- `RESOURCE_NOT_FOUND`: Requested resource not found
- `DUPLICATE_RESOURCE`: Resource already exists
- `RATE_LIMIT_EXCEEDED`: Too many requests

## Rate Limiting

### Limits
- **Public endpoints**: 100 requests per minute per IP
- **Admin endpoints**: 200 requests per minute per user
- **Search endpoints**: 50 requests per minute per IP

### Headers
Rate limit information is included in response headers:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1627689600
```

This API specification provides a comprehensive guide for integrating with the personal blog application, ensuring consistent and predictable behavior across all endpoints.