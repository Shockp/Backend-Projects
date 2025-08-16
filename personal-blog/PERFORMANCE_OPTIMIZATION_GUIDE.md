# Performance Optimization Guide

## Overview

This guide provides comprehensive performance optimization strategies for the Personal Blog application, covering application-level optimizations, database tuning, caching strategies, and monitoring best practices.

## Application-Level Optimizations

### 1. JPA/Hibernate Query Optimization

#### Entity Graph Usage
```java
@Repository
public interface BlogPostRepository extends BaseRepository<BlogPost, Long> {
    
    // Optimized query with entity graph to avoid N+1 problems
    @EntityGraph(attributePaths = {"author", "category", "tags", "comments.authorUser"})
    @Query("SELECT bp FROM BlogPost bp WHERE bp.slug = :slug AND bp.status = 'PUBLISHED' AND bp.deleted = false")
    Optional<BlogPost> findBySlugWithDetails(@Param("slug") String slug);
    
    // Batch fetch for multiple posts
    @EntityGraph(attributePaths = {"author", "category"})
    @Query("SELECT bp FROM BlogPost bp WHERE bp.id IN :ids AND bp.deleted = false")
    List<BlogPost> findByIdsWithBasicDetails(@Param("ids") List<Long> ids);
}
```

#### Projection Usage for Read-Only Operations
```java
// Use projections for list views to reduce data transfer
public interface BlogPostSummaryProjection {
    Long getId();
    String getTitle();
    String getSlug();
    String getExcerpt();
    LocalDateTime getPublishedDate();
    String getAuthorName();
    String getCategoryName();
}

@Query("SELECT bp.id as id, bp.title as title, bp.slug as slug, " +
       "bp.excerpt as excerpt, bp.publishedDate as publishedDate, " +
       "bp.author.username as authorName, bp.category.name as categoryName " +
       "FROM BlogPost bp WHERE bp.status = 'PUBLISHED' AND bp.deleted = false")
Page<BlogPostSummaryProjection> findPublishedPostsSummary(Pageable pageable);
```

### 2. Service Layer Optimizations

#### Async Processing for Heavy Operations
```java
@Service
public class BlogPostService {
    
    @Async("taskExecutor")
    @Transactional
    public CompletableFuture<Void> updateSearchIndex(Long blogPostId) {
        BlogPost post = blogPostRepository.findById(blogPostId).orElse(null);
        if (post != null) {
            // Update search index asynchronously
            searchIndexService.indexBlogPost(post);
        }
        return CompletableFuture.completedFuture(null);
    }
    
    @Async("taskExecutor")
    public CompletableFuture<Void> sendNotifications(Long blogPostId) {
        // Send email notifications asynchronously
        notificationService.notifySubscribers(blogPostId);
        return CompletableFuture.completedFuture(null);
    }
}
```

#### Batch Operations
```java
@Service
public class TagService {
    
    @Transactional
    public void updateTagUsageCounts(List<Long> tagIds) {
        // Batch update instead of individual updates
        List<Tag> tags = tagRepository.findAllById(tagIds);
        
        Map<Long, Integer> usageCounts = blogPostRepository
            .countPostsByTagIds(tagIds)
            .stream()
            .collect(Collectors.toMap(
                result -> (Long) result[0],
                result -> ((Number) result[1]).intValue()
            ));
        
        tags.forEach(tag -> {
            Integer count = usageCounts.getOrDefault(tag.getId(), 0);
            tag.setUsageCount(count);
        });
        
        tagRepository.saveAll(tags);
    }
}
```

### 3. Controller Layer Optimizations

#### Response Compression
```java
@RestController
@RequestMapping("/api/blog-posts")
public class BlogPostController {
    
    @GetMapping
    @Compress // Custom annotation for response compression
    public ResponseEntity<Page<BlogPostSummaryProjection>> getPublishedPosts(
            @PageableDefault(size = 20, sort = "publishedDate", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<BlogPostSummaryProjection> posts = blogPostService.findPublishedPostsSummary(pageable);
        
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(Duration.ofMinutes(15)))
            .body(posts);
    }
}
```

#### HTTP Caching Headers
```java
@Component
public class CacheControlInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestURI = request.getRequestURI();
        
        if (requestURI.startsWith("/api/blog-posts/") && request.getMethod().equals("GET")) {
            // Cache blog post details for 30 minutes
            response.setHeader("Cache-Control", "public, max-age=1800");
            response.setHeader("Vary", "Accept-Encoding");
        } else if (requestURI.startsWith("/api/categories")) {
            // Cache categories for 1 hour
            response.setHeader("Cache-Control", "public, max-age=3600");
        }
        
        return true;
    }
}
```

## Database Performance Tuning

### 1. Connection Pool Optimization

```properties
# HikariCP Advanced Configuration
spring.datasource.hikari.maximum-pool-size=25
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.validation-timeout=5000
spring.datasource.hikari.leak-detection-threshold=60000

# Connection pool monitoring
spring.datasource.hikari.register-mbeans=true
```

### 2. JPA/Hibernate Performance Settings

```properties
# Batch processing optimization
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true

# Query optimization
spring.jpa.properties.hibernate.query.plan_cache_max_size=2048
spring.jpa.properties.hibernate.query.plan_parameter_metadata_max_size=128
spring.jpa.properties.hibernate.query.in_clause_parameter_padding=true

# Lazy loading optimization
spring.jpa.properties.hibernate.enable_lazy_load_no_trans=false
spring.jpa.properties.hibernate.bytecode.use_reflection_optimizer=true
```

### 3. Database-Specific Optimizations (PostgreSQL)

```sql
-- Connection and memory settings
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET work_mem = '4MB';
ALTER SYSTEM SET maintenance_work_mem = '64MB';

-- Query planner settings
ALTER SYSTEM SET random_page_cost = 1.1;
ALTER SYSTEM SET effective_io_concurrency = 200;

-- Checkpoint and WAL settings
ALTER SYSTEM SET checkpoint_completion_target = 0.9;
ALTER SYSTEM SET wal_buffers = '16MB';
ALTER SYSTEM SET max_wal_size = '1GB';

-- Reload configuration
SELECT pg_reload_conf();
```

## Advanced Caching Strategies

### 1. Multi-Level Caching Architecture

```java
@Configuration
@EnableCaching
public class AdvancedCacheConfig {
    
    @Bean
    @Primary
    public CacheManager compositeCacheManager() {
        CompositeCacheManager cacheManager = new CompositeCacheManager();
        
        // L1 Cache: Caffeine (in-memory)
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(5))
            .recordStats());
        
        // L2 Cache: Redis (distributed)
        RedisCacheManager redisCacheManager = RedisCacheManager.builder(redisConnectionFactory())
            .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .disableCachingNullValues())
            .build();
        
        cacheManager.setCacheManagers(Arrays.asList(caffeineCacheManager, redisCacheManager));
        cacheManager.setFallbackToNoOpCache(false);
        
        return cacheManager;
    }
}
```

### 2. Cache Warming and Preloading

```java
@Component
public class CacheWarmupService {
    
    private final BlogPostService blogPostService;
    private final CategoryService categoryService;
    private final TagService tagService;
    
    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void warmupCaches() {
        log.info("Starting cache warmup...");
        
        // Preload popular content
        CompletableFuture.allOf(
            CompletableFuture.runAsync(this::warmupBlogPosts),
            CompletableFuture.runAsync(this::warmupCategories),
            CompletableFuture.runAsync(this::warmupTags)
        ).join();
        
        log.info("Cache warmup completed");
    }
    
    private void warmupBlogPosts() {
        // Load recent posts
        blogPostService.findRecentPosts(PageRequest.of(0, 20));
        
        // Load popular posts
        blogPostService.findMostViewedPosts(PageRequest.of(0, 10));
    }
    
    private void warmupCategories() {
        categoryService.getCategoryHierarchy();
        categoryService.getCategoriesWithPostCount();
    }
    
    private void warmupTags() {
        tagService.getPopularTags(50);
        tagService.getTagCloud();
    }
}
```

### 3. Intelligent Cache Invalidation

```java
@Component
public class SmartCacheEvictionService {
    
    private final CacheManager cacheManager;
    
    @EventListener
    public void handleBlogPostUpdate(BlogPostUpdatedEvent event) {
        BlogPost post = event.getBlogPost();
        
        // Evict specific post cache
        evictCache("blogPosts", post.getSlug());
        
        // Evict related caches
        evictCache("publishedPosts", "*"); // All pages
        evictCache("categoryPosts", post.getCategory().getSlug());
        
        // Evict tag-related caches
        post.getTags().forEach(tag -> 
            evictCache("tagPosts", tag.getSlug()));
    }
    
    @EventListener
    public void handleCategoryUpdate(CategoryUpdatedEvent event) {
        // Evict hierarchy cache
        evictCache("categories", "hierarchy");
        evictCache("categoryPaths", "*");
    }
    
    private void evictCache(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            if ("*".equals(key)) {
                cache.clear();
            } else {
                cache.evict(key);
            }
        }
    }
}
```

## Search Performance Optimization

### 1. Full-Text Search Implementation

```java
@Service
public class SearchService {
    
    @Cacheable(value = "searchResults", key = "#query + '_' + #pageable.pageNumber")
    public Page<BlogPostSummaryProjection> searchBlogPosts(String query, Pageable pageable) {
        // Use database full-text search for better performance
        return blogPostRepository.fullTextSearch(query, pageable);
    }
    
    @Async
    public CompletableFuture<Void> updateSearchIndex(BlogPost post) {
        // Update search index asynchronously
        searchIndexRepository.save(createSearchIndex(post));
        return CompletableFuture.completedFuture(null);
    }
}
```

### 2. Search Index Optimization

```sql
-- PostgreSQL full-text search optimization
CREATE INDEX idx_blog_posts_search_vector 
ON blog_posts USING gin(to_tsvector('english', title || ' ' || content || ' ' || excerpt));

-- Create materialized view for search
CREATE MATERIALIZED VIEW blog_post_search AS
SELECT 
    id,
    title,
    slug,
    excerpt,
    published_date,
    to_tsvector('english', title || ' ' || content || ' ' || excerpt) as search_vector
FROM blog_posts 
WHERE status = 'PUBLISHED' AND deleted = false;

CREATE INDEX idx_blog_post_search_vector ON blog_post_search USING gin(search_vector);
```

## Monitoring and Performance Metrics

### 1. Application Performance Monitoring

```java
@Component
public class PerformanceMonitor {
    
    private final MeterRegistry meterRegistry;
    private final Timer.Sample sample;
    
    @EventListener
    public void handleQueryExecution(QueryExecutionEvent event) {
        Timer.builder("database.query.duration")
            .tag("query.type", event.getQueryType())
            .tag("entity", event.getEntityName())
            .register(meterRegistry)
            .record(event.getDuration(), TimeUnit.MILLISECONDS);
    }
    
    @EventListener
    public void handleCacheAccess(CacheAccessEvent event) {
        Counter.builder("cache.access")
            .tag("cache.name", event.getCacheName())
            .tag("result", event.isHit() ? "hit" : "miss")
            .register(meterRegistry)
            .increment();
    }
}
```

### 2. Custom Health Indicators

```java
@Component
public class DatabasePerformanceHealthIndicator implements HealthIndicator {
    
    private final DataSource dataSource;
    
    @Override
    public Health health() {
        try {
            long startTime = System.currentTimeMillis();
            
            // Test query performance
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT 1")) {
                statement.executeQuery();
            }
            
            long duration = System.currentTimeMillis() - startTime;
            
            Health.Builder builder = duration < 100 ? Health.up() : Health.down();
            
            return builder
                .withDetail("query_time_ms", duration)
                .withDetail("threshold_ms", 100)
                .build();
                
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
```

### 3. Performance Testing

```java
@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
public class PerformanceIntegrationTest {
    
    @Autowired
    private BlogPostService blogPostService;
    
    @Test
    @Order(1)
    public void testBlogPostListPerformance() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        Page<BlogPostSummaryProjection> posts = blogPostService
            .findPublishedPostsSummary(PageRequest.of(0, 20));
        
        stopWatch.stop();
        
        assertThat(posts).isNotNull();
        assertThat(stopWatch.getTotalTimeMillis()).isLessThan(200);
    }
    
    @Test
    @Order(2)
    public void testSearchPerformance() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        Page<BlogPostSummaryProjection> results = blogPostService
            .searchBlogPosts("spring boot", PageRequest.of(0, 10));
        
        stopWatch.stop();
        
        assertThat(results).isNotNull();
        assertThat(stopWatch.getTotalTimeMillis()).isLessThan(300);
    }
}
```

## Load Testing and Capacity Planning

### 1. JMeter Test Plan Configuration

```xml
<!-- Blog Post List Load Test -->
<HTTPSamplerProxy>
    <elementProp name="HTTPsampler.Arguments" elementType="Arguments">
        <collectionProp name="Arguments.arguments">
            <elementProp name="page" elementType="HTTPArgument">
                <boolProp name="HTTPArgument.always_encode">false</boolProp>
                <stringProp name="Argument.value">${__Random(0,10)}</stringProp>
                <stringProp name="Argument.metadata">=</stringProp>
                <boolProp name="HTTPArgument.use_equals">true</boolProp>
                <stringProp name="Argument.name">page</stringProp>
            </elementProp>
        </collectionProp>
    </elementProp>
    <stringProp name="HTTPSampler.domain">localhost</stringProp>
    <stringProp name="HTTPSampler.port">8080</stringProp>
    <stringProp name="HTTPSampler.path">/api/blog-posts</stringProp>
    <stringProp name="HTTPSampler.method">GET</stringProp>
</HTTPSamplerProxy>
```

### 2. Performance Benchmarks

```java
@Component
public class PerformanceBenchmark {
    
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void runPerformanceBenchmark() {
        Map<String, Long> benchmarks = new HashMap<>();
        
        // Benchmark blog post listing
        long startTime = System.currentTimeMillis();
        blogPostService.findPublishedPosts(PageRequest.of(0, 20));
        benchmarks.put("blog_post_list", System.currentTimeMillis() - startTime);
        
        // Benchmark search
        startTime = System.currentTimeMillis();
        blogPostService.searchBlogPosts("test", PageRequest.of(0, 10));
        benchmarks.put("search", System.currentTimeMillis() - startTime);
        
        // Log results
        benchmarks.forEach((operation, duration) -> {
            log.info("Performance benchmark - {}: {}ms", operation, duration);
            
            // Send metrics to monitoring system
            meterRegistry.timer("benchmark.duration", "operation", operation)
                .record(duration, TimeUnit.MILLISECONDS);
        });
    }
}
```

## Production Deployment Optimizations

### 1. JVM Tuning

```bash
# JVM arguments for production
-Xms2g -Xmx4g
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+UseStringDeduplication
-XX:+OptimizeStringConcat
-XX:+UseCompressedOops
-XX:+UseCompressedClassPointers
-Djava.awt.headless=true
-Dspring.profiles.active=production
```

### 2. Application Properties for Production

```properties
# Production optimizations
server.compression.enabled=true
server.compression.mime-types=application/json,application/xml,text/html,text/xml,text/plain,text/css,application/javascript
server.compression.min-response-size=1024

# Connection pool for production
spring.datasource.hikari.maximum-pool-size=50
spring.datasource.hikari.minimum-idle=10

# Logging optimization
logging.level.org.hibernate.SQL=WARN
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=WARN

# Actuator security
management.endpoints.web.exposure.include=health,metrics,prometheus
management.endpoint.health.show-details=when-authorized
```

### 3. Docker Optimization

```dockerfile
# Multi-stage build for smaller image
FROM openjdk:21-jdk-slim as builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM openjdk:21-jre-slim
WORKDIR /app

# Add performance monitoring agent
COPY --from=builder /app/target/*.jar app.jar

# Optimize container for performance
ENV JAVA_OPTS="-Xms1g -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

This comprehensive performance optimization guide provides actionable strategies to significantly improve the application's performance, scalability, and user experience.