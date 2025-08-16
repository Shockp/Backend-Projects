package com.personalblog.repository.integration;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests using TestContainers with PostgreSQL.
 * 
 * <p>
 * This class provides a shared PostgreSQL container for all integration tests,
 * ensuring consistent database behavior and proper isolation between test runs.
 * </p>
 * 
 * <p>
 * Features:
 * </p>
 * <ul>
 * <li>PostgreSQL container with proper configuration</li>
 * <li>Dynamic property configuration for Spring Boot</li>
 * <li>Shared container instance for performance</li>
 * <li>Proper cleanup and resource management</li>
 * </ul>
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @since 1.0.0
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("integration-test")
public abstract class BaseIntegrationTest {

    /**
     * Shared PostgreSQL container for all integration tests.
     * Using a static container to improve test performance by reusing the same instance.
     */
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("personal_blog_test")
            .withUsername("test_user")
            .withPassword("test_password")
            .withReuse(true);

    /**
     * Configure Spring Boot properties dynamically based on the container configuration.
     * 
     * @param registry the dynamic property registry
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        
        // JPA Configuration for integration tests
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
        
        // Disable Redis for integration tests
        registry.add("spring.data.redis.repositories.enabled", () -> "false");
        registry.add("spring.cache.type", () -> "simple");
        
        // Disable mail for integration tests
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> "1025");
        
        // JWT Configuration for tests
        registry.add("jwt.secret", () -> "integrationTestSecretKeyForTestingPurposesOnly");
        registry.add("jwt.expiration", () -> "3600000");
        registry.add("jwt.refresh-expiration", () -> "86400000");
    }

    /**
     * Ensure the container is started before any tests run.
     */
    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }
}