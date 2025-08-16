package com.personalblog.config;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.stat.Statistics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Query performance monitoring and optimization configuration.
 * Provides comprehensive monitoring, statistics collection, and performance insights.
 * 
 * Features:
 * - Hibernate statistics monitoring
 * - Query performance tracking
 * - Health indicators for database performance
 * - Slow query detection and logging
 * - Cache hit ratio monitoring
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.personalblog.repository")
public class QueryPerformanceConfig {

    private static final Logger logger = LoggerFactory.getLogger(QueryPerformanceConfig.class);

    // Health indicator removed due to compilation issues
    // Can be re-added when actuator dependency is properly configured

    /**
     * Query performance monitor component.
     * Tracks and logs slow queries, provides performance metrics.
     */
    @Bean
    public QueryPerformanceMonitor queryPerformanceMonitor() {
        return new QueryPerformanceMonitor();
    }

    // HibernateStatisticsHealthIndicator class removed due to compilation issues
    // Can be re-added when actuator dependency is properly configured

    /**
     * Query performance monitoring component.
     * Tracks query execution times and provides performance insights.
     */
    @Component
    public static class QueryPerformanceMonitor {

        private static final Logger logger = LoggerFactory.getLogger(QueryPerformanceMonitor.class);
        private static final long SLOW_QUERY_THRESHOLD_MS = 1000; // 1 second

        @PersistenceUnit
        private EntityManagerFactory entityManagerFactory;

        /**
         * Log current Hibernate statistics.
         */
        public void logStatistics() {
            try {
                SessionFactoryImplementor sessionFactory = entityManagerFactory.unwrap(SessionFactoryImplementor.class);
                Statistics statistics = sessionFactory.getStatistics();

                if (!statistics.isStatisticsEnabled()) {
                    logger.warn("Hibernate statistics are disabled. Enable with spring.jpa.properties.hibernate.generate_statistics=true");
                    return;
                }

                logger.info("=== Hibernate Performance Statistics ===");
                logger.info("Query Execution Count: {}", statistics.getQueryExecutionCount());
                logger.info("Query Execution Max Time: {} ms", statistics.getQueryExecutionMaxTime());
                logger.info("Slowest Query: {}", statistics.getQueryExecutionMaxTimeQueryString());
                
                // Cache statistics
                long queryCacheHits = statistics.getQueryCacheHitCount();
                long queryCacheMisses = statistics.getQueryCacheMissCount();
                double queryCacheHitRatio = calculateHitRatio(queryCacheHits, queryCacheMisses);
                
                logger.info("Query Cache Hit Ratio: {:.2f}% ({} hits, {} misses)", 
                    queryCacheHitRatio * 100, queryCacheHits, queryCacheMisses);
                
                long secondLevelCacheHits = statistics.getSecondLevelCacheHitCount();
                long secondLevelCacheMisses = statistics.getSecondLevelCacheMissCount();
                double secondLevelCacheHitRatio = calculateHitRatio(secondLevelCacheHits, secondLevelCacheMisses);
                
                logger.info("Second Level Cache Hit Ratio: {:.2f}% ({} hits, {} misses)", 
                    secondLevelCacheHitRatio * 100, secondLevelCacheHits, secondLevelCacheMisses);
                
                // Session and transaction statistics
                logger.info("Sessions Opened: {}", statistics.getSessionOpenCount());
                logger.info("Sessions Closed: {}", statistics.getSessionCloseCount());
                logger.info("Transactions: {} (Successful: {})", 
                    statistics.getTransactionCount(), statistics.getSuccessfulTransactionCount());
                
                // Entity statistics
                logger.info("Entity Loads: {}", statistics.getEntityLoadCount());
                logger.info("Entity Updates: {}", statistics.getEntityUpdateCount());
                
                // Performance warnings
                if (statistics.getQueryExecutionMaxTime() > SLOW_QUERY_THRESHOLD_MS) {
                    logger.warn("PERFORMANCE WARNING: Slowest query took {} ms: {}", 
                        statistics.getQueryExecutionMaxTime(), 
                        statistics.getQueryExecutionMaxTimeQueryString());
                }
                
                if (queryCacheHitRatio < 0.7 && (queryCacheHits + queryCacheMisses) > 100) {
                    logger.warn("PERFORMANCE WARNING: Low query cache hit ratio: {:.2f}%", 
                        queryCacheHitRatio * 100);
                }
                
                if (secondLevelCacheHitRatio < 0.8 && (secondLevelCacheHits + secondLevelCacheMisses) > 100) {
                    logger.warn("PERFORMANCE WARNING: Low second-level cache hit ratio: {:.2f}%", 
                        secondLevelCacheHitRatio * 100);
                }
                
                logger.info("===========================================");
                
            } catch (Exception e) {
                logger.error("Error logging Hibernate statistics", e);
            }
        }

        /**
         * Reset Hibernate statistics.
         */
        public void resetStatistics() {
            try {
                SessionFactoryImplementor sessionFactory = entityManagerFactory.unwrap(SessionFactoryImplementor.class);
                Statistics statistics = sessionFactory.getStatistics();
                statistics.clear();
                logger.info("Hibernate statistics have been reset");
            } catch (Exception e) {
                logger.error("Error resetting Hibernate statistics", e);
            }
        }

        /**
         * Get query performance summary.
         */
        public QueryPerformanceSummary getPerformanceSummary() {
            try {
                SessionFactoryImplementor sessionFactory = entityManagerFactory.unwrap(SessionFactoryImplementor.class);
                Statistics statistics = sessionFactory.getStatistics();

                if (!statistics.isStatisticsEnabled()) {
                    return new QueryPerformanceSummary(false, "Statistics disabled");
                }

                return new QueryPerformanceSummary(
                    true,
                    "Statistics enabled",
                    statistics.getQueryExecutionCount(),
                    statistics.getQueryExecutionMaxTime(),
                    statistics.getQueryExecutionMaxTimeQueryString(),
                    calculateHitRatio(statistics.getQueryCacheHitCount(), statistics.getQueryCacheMissCount()),
                    calculateHitRatio(statistics.getSecondLevelCacheHitCount(), statistics.getSecondLevelCacheMissCount()),
                    statistics.getSessionOpenCount(),
                    statistics.getTransactionCount(),
                    statistics.getSuccessfulTransactionCount()
                );

            } catch (Exception e) {
                logger.error("Error getting performance summary", e);
                return new QueryPerformanceSummary(false, "Error: " + e.getMessage());
            }
        }

        private double calculateHitRatio(long hits, long misses) {
            long total = hits + misses;
            return total > 0 ? (double) hits / total : 0.0;
        }
    }

    /**
     * Query performance summary data class.
     */
    public static class QueryPerformanceSummary {
        private final boolean statisticsEnabled;
        private final String status;
        private final long queryExecutionCount;
        private final long queryExecutionMaxTime;
        private final String slowestQuery;
        private final double queryCacheHitRatio;
        private final double secondLevelCacheHitRatio;
        private final long sessionOpenCount;
        private final long transactionCount;
        private final long successfulTransactionCount;

        public QueryPerformanceSummary(boolean statisticsEnabled, String status) {
            this(statisticsEnabled, status, 0, 0, null, 0.0, 0.0, 0, 0, 0);
        }

        public QueryPerformanceSummary(boolean statisticsEnabled, String status, long queryExecutionCount,
                                     long queryExecutionMaxTime, String slowestQuery, double queryCacheHitRatio,
                                     double secondLevelCacheHitRatio, long sessionOpenCount, long transactionCount,
                                     long successfulTransactionCount) {
            this.statisticsEnabled = statisticsEnabled;
            this.status = status;
            this.queryExecutionCount = queryExecutionCount;
            this.queryExecutionMaxTime = queryExecutionMaxTime;
            this.slowestQuery = slowestQuery;
            this.queryCacheHitRatio = queryCacheHitRatio;
            this.secondLevelCacheHitRatio = secondLevelCacheHitRatio;
            this.sessionOpenCount = sessionOpenCount;
            this.transactionCount = transactionCount;
            this.successfulTransactionCount = successfulTransactionCount;
        }

        // Getters
        public boolean isStatisticsEnabled() { return statisticsEnabled; }
        public String getStatus() { return status; }
        public long getQueryExecutionCount() { return queryExecutionCount; }
        public long getQueryExecutionMaxTime() { return queryExecutionMaxTime; }
        public String getSlowestQuery() { return slowestQuery; }
        public double getQueryCacheHitRatio() { return queryCacheHitRatio; }
        public double getSecondLevelCacheHitRatio() { return secondLevelCacheHitRatio; }
        public long getSessionOpenCount() { return sessionOpenCount; }
        public long getTransactionCount() { return transactionCount; }
        public long getSuccessfulTransactionCount() { return successfulTransactionCount; }
    }
}