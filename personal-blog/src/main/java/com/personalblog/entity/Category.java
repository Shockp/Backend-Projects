package com.personalblog.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Category entity representing hierarchical blog post categories with
 * SEO metadata and UI dsplay settings.
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
public class Category {
    // Implementation needed
}