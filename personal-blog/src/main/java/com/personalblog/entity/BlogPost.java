package com.personalblog.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * BlogPost entity representing a blog post with rich content,
 * metadata, and relationships to author, category, tags and comments.
 * 
 * Features:
 * - Title and slug (URL-friendly)
 * - Rich text content
 * - Excerpt/summary
 * - Publication status (DRAFT, PUBLISHED, ARCHIVED)
 * - Publication date
 * - Featured image URL
 * - SEO metadata (meta title, description, keywords)
 * - View count
 * - Estimated reading time
 * - Relationships: author, category, tags, comments
 * - Database indexes for performance
 * 
 * Inherits audit fields (id, createdAt, updatedAt, deleted) from BaseEntity.
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 */
public class BlogPost {
    // Implementation needed
}