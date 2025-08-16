package com.personalblog.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;
import java.util.Arrays;

/**
 * Cache configuration for the Personal Blog application.
 * Provides Caffeine-based in-memory caching.
 * 
 * Features:
 * - L1 Cache: Caffeine for fast in-memory access
 * - Cache-specific TTL configurations
 * - Performance monitoring support
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Primary cache manager using Caffeine for in-memory caching.
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        return caffeineCacheManager();
    }

    /**
     * Caffeine cache manager configuration.
     * Optimized for different cache types with specific settings.
     */
    @Bean
    public CaffeineCacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Default Caffeine configuration
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(10))
            .expireAfterAccess(Duration.ofMinutes(5))
            .recordStats()
            .weakKeys()
            .weakValues());
        
        // Pre-create caches with specific configurations
        cacheManager.setCacheNames(Arrays.asList(
            "blogPosts",
            "publishedPosts", 
            "categories",
            "categoryHierarchy",
            "tags",
            "tagCloud",
            "users",
            "searchResults",
            "comments",
            "statistics"
        ));
        
        return cacheManager;
    }



    /**
     * Cache configuration properties for external configuration.
     */
    public static class CacheProperties {
        
        public static final String BLOG_POSTS_CACHE = "blogPosts";
        public static final String PUBLISHED_POSTS_CACHE = "publishedPosts";
        public static final String CATEGORIES_CACHE = "categories";
        public static final String CATEGORY_HIERARCHY_CACHE = "categoryHierarchy";
        public static final String TAGS_CACHE = "tags";
        public static final String TAG_CLOUD_CACHE = "tagCloud";
        public static final String USERS_CACHE = "users";
        public static final String SEARCH_RESULTS_CACHE = "searchResults";
        public static final String COMMENTS_CACHE = "comments";
        public static final String STATISTICS_CACHE = "statistics";
        
        // Cache key patterns
        public static final String BLOG_POST_BY_SLUG_KEY = "slug:%s";
        public static final String BLOG_POST_BY_ID_KEY = "id:%d";
        public static final String PUBLISHED_POSTS_PAGE_KEY = "page:%d:size:%d:sort:%s";
        public static final String CATEGORY_BY_SLUG_KEY = "slug:%s";
        public static final String CATEGORY_HIERARCHY_KEY = "hierarchy";
        public static final String TAG_CLOUD_KEY = "cloud:limit:%d";
        public static final String USER_BY_USERNAME_KEY = "username:%s";
        public static final String USER_BY_EMAIL_KEY = "email:%s";
        public static final String SEARCH_RESULTS_KEY = "query:%s:page:%d";
        public static final String COMMENT_COUNT_KEY = "post:%d:count";
        public static final String DAILY_STATS_KEY = "daily:%s";
    }

    /**
     * Cache key generator for consistent key generation across the application.
     */
    public static class CacheKeyGenerator {
        
        public static String blogPostBySlug(String slug) {
            return String.format(CacheProperties.BLOG_POST_BY_SLUG_KEY, slug);
        }
        
        public static String blogPostById(Long id) {
            return String.format(CacheProperties.BLOG_POST_BY_ID_KEY, id);
        }
        
        public static String publishedPostsPage(int page, int size, String sort) {
            return String.format(CacheProperties.PUBLISHED_POSTS_PAGE_KEY, page, size, sort);
        }
        
        public static String categoryBySlug(String slug) {
            return String.format(CacheProperties.CATEGORY_BY_SLUG_KEY, slug);
        }
        
        public static String tagCloud(int limit) {
            return String.format(CacheProperties.TAG_CLOUD_KEY, limit);
        }
        
        public static String userByUsername(String username) {
            return String.format(CacheProperties.USER_BY_USERNAME_KEY, username.toLowerCase());
        }
        
        public static String userByEmail(String email) {
            return String.format(CacheProperties.USER_BY_EMAIL_KEY, email.toLowerCase());
        }
        
        public static String searchResults(String query, int page) {
            return String.format(CacheProperties.SEARCH_RESULTS_KEY, 
                query.toLowerCase().trim(), page);
        }
        
        public static String commentCount(Long postId) {
            return String.format(CacheProperties.COMMENT_COUNT_KEY, postId);
        }
        
        public static String dailyStats(String date) {
            return String.format(CacheProperties.DAILY_STATS_KEY, date);
        }
    }
}