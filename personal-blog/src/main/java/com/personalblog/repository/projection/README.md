# Repository Projections

This package contains projection interfaces designed to optimize read-only database operations in the personal blog application. Projections provide a lightweight alternative to loading full entities when only specific fields are needed.

## Overview

Repository projections are interfaces that define a subset of entity fields for optimized queries. They offer several benefits:

- **Performance**: Reduced memory footprint and faster query execution
- **Network Efficiency**: Less data transfer between database and application
- **Type Safety**: Compile-time checking of field access
- **Lazy Loading Prevention**: No risk of N+1 queries from unintended relationship loading

## Available Projections

### 1. BlogPostSummary

**Purpose**: Lightweight view of blog post data for listing pages, search results, and feeds.

**Fields**:
- Basic post information (id, title, slug, status)
- Timestamps (createdAt, publishedDate)
- Metrics (viewCount, readingTimeMinutes)
- Content preview (excerpt, featuredImageUrl)
- Author information (username, displayName)
- Category information (name, slug)

**Usage Examples**:

```java
// In service layer
@Service
public class BlogPostService {
    
    @Autowired
    private BlogPostRepository blogPostRepository;
    
    public Page<BlogPostSummary> getPublishedPosts(Pageable pageable) {
        return blogPostRepository.findPublishedPostSummaries(pageable);
    }
    
    public Page<BlogPostSummary> getPostsByCategory(String categorySlug, Pageable pageable) {
        return blogPostRepository.findPostSummariesByCategory(categorySlug, pageable);
    }
    
    public Page<BlogPostSummary> searchPosts(String searchTerm, Pageable pageable) {
        return blogPostRepository.searchPostSummaries(searchTerm, pageable);
    }
}
```

```java
// In controller layer
@RestController
@RequestMapping("/api/posts")
public class BlogPostController {
    
    @GetMapping
    public ResponseEntity<Page<BlogPostSummary>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BlogPostSummary> posts = blogPostService.getPublishedPosts(pageable);
        return ResponseEntity.ok(posts);
    }
}
```

### 2. CategoryWithCount

**Purpose**: Category information with post count statistics for navigation menus and admin interfaces.

**Fields**:
- Basic category information (id, name, slug, description)
- Hierarchy information (parentId, parentName)
- Display settings (displayOrder)
- SEO metadata (metaTitle, metaDescription)
- Statistics (totalPostCount, publishedPostCount)
- Timestamps (createdAt, updatedAt)

**Default Methods**:
- `hasChildren()`: Check if category has posts (based on post count)
- `hasPublishedPosts()`: Check if category has published posts
- `getDraftPostCount()`: Calculate draft posts (total - published)

**Usage Examples**:

```java
// For navigation menu
@Service
public class NavigationService {
    
    public List<CategoryWithCount> getNavigationCategories() {
        return categoryRepository.findRootCategoriesWithCounts();
    }
    
    public List<CategoryWithCount> getCategoryHierarchy(Long parentId) {
        return categoryRepository.findChildCategoriesWithCounts(parentId);
    }
}
```

```java
// For admin dashboard
@Service
public class AdminService {
    
    public Page<CategoryWithCount> getCategoriesWithStats(Pageable pageable) {
        return categoryRepository.findPopularCategoriesWithCounts(pageable);
    }
    
    public List<CategoryWithCount> searchCategories(String searchTerm) {
        Pageable pageable = PageRequest.of(0, 20);
        return categoryRepository.searchCategoriesWithCounts(searchTerm, pageable).getContent();
    }
}
```

### 3. TagCloudItem

**Purpose**: Tag information with usage statistics and popularity metrics for tag clouds and tag-based navigation.

**Fields**:
- Basic tag information (id, name, slug, description)
- Usage statistics (usageCount, actualPostCount)
- Visual settings (colorCode)
- Popularity metrics (relativePopularity)
- Timestamps (createdAt, updatedAt)

**Default Methods**:
- `calculateFontSize(minSize, maxSize)`: Calculate font size based on popularity
- `getCssClass()`: Get CSS class based on popularity level
- `isPopular(threshold)`: Check if tag exceeds popularity threshold
- `getDisplayWeight()`: Get weight for tag cloud algorithms (1-10 scale)
- `isUsageCountAccurate()`: Verify usage count matches actual post count

**Usage Examples**:

```java
// For tag cloud component
@Service
public class TagCloudService {
    
    public List<TagCloudItem> getTagCloudData(int maxTags) {
        return tagRepository.findTagCloudData(maxTags);
    }
    
    public List<TagCloudItem> getPopularTags(int minUsage) {
        Pageable pageable = PageRequest.of(0, 50);
        return tagRepository.findPopularTagCloudItems(minUsage, pageable).getContent();
    }
}
```

```java
// For tag cloud rendering
@Component
public class TagCloudRenderer {
    
    public String renderTagCloud(List<TagCloudItem> tags) {
        StringBuilder html = new StringBuilder("<div class='tag-cloud'>");
        
        for (TagCloudItem tag : tags) {
            double fontSize = tag.calculateFontSize(12.0, 24.0);
            String cssClass = tag.getCssClass();
            String color = tag.getColorCode() != null ? tag.getColorCode() : "#333";
            
            html.append(String.format(
                "<a href='/tags/%s' class='%s' style='font-size: %.1fpx; color: %s'>%s</a> ",
                tag.getSlug(), cssClass, fontSize, color, tag.getName()
            ));
        }
        
        html.append("</div>");
        return html.toString();
    }
}
```

## Performance Considerations

### Query Optimization

1. **Selective Field Loading**: Projections only load required fields, reducing memory usage
2. **Join Optimization**: Queries are optimized to fetch related data in single queries
3. **Index Usage**: Projection queries are designed to leverage database indexes effectively

### Best Practices

1. **Use for Read-Only Operations**: Projections are ideal for display and reporting scenarios
2. **Avoid in Write Operations**: Use full entities when modifications are needed
3. **Consider Caching**: Projection results are excellent candidates for caching
4. **Monitor Performance**: Use database query analysis tools to verify optimization

### Example Performance Comparison

```java
// Full entity loading (higher memory usage)
Page<BlogPost> posts = blogPostRepository.findByStatusAndDeletedFalse(Status.PUBLISHED, pageable);

// Projection loading (optimized memory usage)
Page<BlogPostSummary> summaries = blogPostRepository.findPublishedPostSummaries(pageable);
```

## Testing

Each projection interface has comprehensive unit tests that verify:

- Correct field mapping from entities to projections
- Proper handling of null values and edge cases
- Accurate calculation of derived fields (counts, percentages)
- Correct behavior of default methods
- Pagination and sorting functionality
- Query performance and optimization

### Running Tests

```bash
# Run all projection tests
./mvnw test -Dtest="**/projection/*Test"

# Run specific projection tests
./mvnw test -Dtest="BlogPostSummaryTest"
./mvnw test -Dtest="CategoryWithCountTest"
./mvnw test -Dtest="TagCloudItemTest"
```

## Integration with Frontend

### JSON Serialization

Projections work seamlessly with JSON serialization for REST APIs:

```java
@GetMapping("/api/posts/summaries")
public ResponseEntity<Page<BlogPostSummary>> getPostSummaries(Pageable pageable) {
    Page<BlogPostSummary> summaries = blogPostService.getPublishedPostSummaries(pageable);
    return ResponseEntity.ok(summaries);
}
```

### Frontend Usage

```javascript
// Fetch blog post summaries
fetch('/api/posts/summaries?page=0&size=10')
  .then(response => response.json())
  .then(data => {
    data.content.forEach(post => {
      console.log(`${post.title} by ${post.authorDisplayName}`);
      console.log(`Category: ${post.categoryName}`);
      console.log(`Reading time: ${post.readingTimeMinutes} minutes`);
    });
  });

// Render tag cloud
fetch('/api/tags/cloud?limit=50')
  .then(response => response.json())
  .then(tags => {
    const tagCloudHtml = tags.map(tag => {
      const fontSize = 12 + (tag.relativePopularity / 100) * 12; // 12-24px range
      return `<a href="/tags/${tag.slug}" 
                 style="font-size: ${fontSize}px; color: ${tag.colorCode || '#333'}"
                 class="${tag.cssClass}">
                ${tag.name}
              </a>`;
    }).join(' ');
    
    document.getElementById('tag-cloud').innerHTML = tagCloudHtml;
  });
```

## Migration from Full Entities

When migrating existing code to use projections:

1. **Identify Read-Only Scenarios**: Look for places where entities are loaded but not modified
2. **Update Repository Methods**: Add projection-based query methods
3. **Update Service Layer**: Change return types to use projections
4. **Update Controllers**: Ensure JSON serialization works correctly
5. **Update Tests**: Add tests for new projection methods

### Migration Example

```java
// Before: Using full entity
public Page<BlogPost> getPublishedPosts(Pageable pageable) {
    return blogPostRepository.findByStatusAndDeletedFalse(Status.PUBLISHED, pageable);
}

// After: Using projection
public Page<BlogPostSummary> getPublishedPostSummaries(Pageable pageable) {
    return blogPostRepository.findPublishedPostSummaries(pageable);
}
```

## Troubleshooting

### Common Issues

1. **Null Pointer Exceptions**: Ensure projection queries handle null values properly
2. **Performance Issues**: Verify that queries use appropriate indexes
3. **Mapping Errors**: Check that projection field names match query aliases exactly
4. **Pagination Problems**: Ensure count queries are optimized for large datasets

### Debugging Tips

1. **Enable SQL Logging**: Add `spring.jpa.show-sql=true` to see generated queries
2. **Use Query Analysis**: Analyze query execution plans in your database
3. **Monitor Memory Usage**: Compare memory usage before and after projection implementation
4. **Test with Large Datasets**: Verify performance improvements with realistic data volumes

## Future Enhancements

Potential improvements to the projection system:

1. **Dynamic Projections**: Support for runtime field selection
2. **Nested Projections**: More complex projection hierarchies
3. **Caching Integration**: Automatic caching of projection results
4. **GraphQL Integration**: Use projections with GraphQL queries
5. **Audit Projections**: Specialized projections for audit and reporting needs