package com.personalblog.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Category entity representing hierarchical blog post categories with
 * SEO metadata and UI display settings.
 * 
 * Features:
 * - Name and slug (URL-friendly)
 * - Description
 * - UI color code
 * - Self-referencing parent category for hierarchy
 * - Display order for custom sorting
 * - SEO metadata (meta title, description, keywords)
 * - Relationship to BlogPost (One-to-Many)
 * 
 * Inherits audit fields (id, createdAt, updatedAt, deleted) from BaseEntity.
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_category_name", columnList = "name"),
    @Index(name = "idx_category_slug", columnList = "slug"),
    @Index(name = "idx_category_parent", columnList = "parent_id"),
    @Index(name = "idx_category_display_order", columnList = "display_order")
})
public class Category extends BaseEntity {
    
    // ==================== Core Fields ====================

    @NotBlank
    @Size(max = 100, message = "Name must be at most 100 characters long")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank
    @Size(max = 200, message = "Slug must be at most 200 characters long")
    @Pattern(
        regexp = "^[a-z0-9-]+$",
        message = "Slug must contain only lowercase letters, numbers, and hyphens"
    )
    @Column(name = "slug", nullable = false, unique = true, length = 200)
    private String slug;

    @Size(max = 500, message = "Description must be at most 500 characters long")
    @Column(name = "description", length = 500)
    private String description;

    // ==================== UI Fields ====================

    @Pattern(
        regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$",
        message = "Color code must be a valid hex code (#RRGGBB or #RGB)"
    )
    @Column(name = "color_code", length = 7)
    private String colorCode = "#ffffff";

    @Min(0)
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    // ==================== SEO Metadata ====================

    @Size(max = 70, message = "Meta title must be at most 70 characters long")
    @Column(name = "meta_title", length = 70)
    private String metaTitle;

    @Size(max = 160, message = "Meta description must be at most 160 characters long")
    @Column(name = "meta_description", length = 160)
    private String metaDescription;

    @Size(max = 255, message = "Meta keywords must be at most 255 characters long")
    @Column(name = "meta_keywords", length = 255)
    private String metaKeywords;

    // ==================== Hierarchy ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Category> children = new HashSet<>();

    // ==================== Relationships ====================

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BlogPost> blogPosts = new HashSet<>();

    // ==================== Constructors ====================

    public Category() {
        super();
    }

    public Category(String name, String slug) {
        this();
        this.name = name;
        this.slug = slug;
    }

    public Category(String name, String slug, String description) {
        this(name, slug);
        this.description = description;
    }

    // ==================== Getters & Setters ====================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getMetaTitle() {
        return metaTitle;
    }

    public void setMetaTitle(String metaTitle) {
        this.metaTitle = metaTitle;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    public String getMetaKeywords() {
        return metaKeywords;
    }

    public void setMetaKeywords(String metaKeywords) {
        this.metaKeywords = metaKeywords;
    }

    public Category getParent() {
        return parent;
    }

    public void setParent(Category parent) {
        this.parent = parent;
    }

    public Set<Category> getChildren() {
        return children;
    }

    public void setChildren(Set<Category> children) {
        this.children = children;
    }

    public Set<BlogPost> getBlogPosts() {
        return blogPosts;
    }

    public void setBlogPosts(Set<BlogPost> blogPosts) {
        this.blogPosts = blogPosts;
    }

    // ==================== Utility Methods ====================

    /**
     * Adds a child category
     * @param child the category to add as a child
     */
    public void addChild(Category child) {
        if (child == null) {
            throw new IllegalArgumentException("Child category cannot be null");
        }
        
        if (this == child || this.isDescendantOf(child)) {
            throw new IllegalArgumentException("Child category cannot be a descendant of this category");
        }

        child.setParent(this);
        children.add(child);
    }

    /**
     * Checks if this category is a descendant of the given category by traversing up the category tree.
     * This method prevents circular references when setting parent-child relationships.
     *
     * @param potentialAncestor the category to check if it's an ancestor
     * @return true if this category is a descendant of the given category, false otherwise
     */
    private boolean isDescendantOf(Category potentialAncestor) {
        Category current = this.parent;
        while (current != null) {
            if (current.equals(potentialAncestor)) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    /**
     * Removes a child category.
     * @param child the category to remove as a child
     */
    public void removeChild(Category child) {
        if (child != null) {
            child.setParent(null);
            children.remove(child);
        }
    }

    /**
     * Adds a blog post to this category.
     * @param blogPost the blog post to add
     */
    public void addBlogPost(BlogPost blogPost) {
        if (blogPost != null) {
            blogPost.setCategory(this);
            blogPosts.add(blogPost);
        }
    }

    /**
     * Removes a blog post from this category.
     * @param post the blog post to remove
     */
    public void removeBlogPost(BlogPost post) {
        if (post != null) {
            post.setCategory(null);
            blogPosts.remove(post);
        }
    }

    /**
     * Checks if this category has any child categories.
     * 
     * @return true if this category has child categories, false otherwise
     */
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    /**
     * Checks if this category is a root category (has no parent).
     * 
     * @return true if this category has no parent, false otherwise
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * Calculates the depth of this category in the category hierarchy.
     * Root categories have a depth of 0, their direct children have a depth of 1, and so on.
     * 
     * @return the depth of this category in the hierarchy
     */
    public int getDepth() {
        int depth = 0;
        Category current = this.parent;
        while (current != null) {
            depth++;
            current = current.getParent();
        }
        return depth;
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Category category = (Category) obj;
        
        // If both have IDs, compare by ID
        if (getId() != null && category.getId() != null) {
            return Objects.equals(getId(), category.getId());
        }
        
        // If no IDs, compare by business key (name and slug)
        return Objects.equals(name, category.name) && 
               Objects.equals(slug, category.slug);
    }

    @Override
    public int hashCode() {
        // If ID exists, use it for hash
        if (getId() != null) {
            return Objects.hash(getId());
        }
        
        // Otherwise use business key
        return Objects.hash(name, slug);
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + getId() +
                ", name='" + (name != null ? name : "null") + '\'' +
                ", slug='" + (slug != null ? slug : "null") + '\'' +
                ", description='" + (description != null ? description : "null") + '\'' +
                ", colorCode='" + (colorCode != null ? colorCode : "null") + '\'' +
                ", displayOrder=" + displayOrder +
                ", metaTitle='" + (metaTitle != null ? metaTitle : "null") + '\'' +
                ", metaDescription='" + (metaDescription != null ? metaDescription : "null") + '\'' +
                ", metaKeywords='" + (metaKeywords != null ? metaKeywords : "null") + '\'' +
                ", parent=" + (parent != null ? parent.getId() : null) +
                ", childrenCount=" + (children != null ? children.size() : 0) +
                ", blogPostsCount=" + (blogPosts != null ? blogPosts.size() : 0) +
                '}';
    }
}