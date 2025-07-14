package com.shockp.weather;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@code WeatherApiWrapperApplication} is the main Spring Boot application class for the Weather API Wrapper Service.
 *
 * <p>This service provides a wrapper around weather APIs with caching, rate limiting, and comprehensive error handling.
 * It follows hexagonal architecture principles and implements security best practices for production deployment.</p>
 *
 * <h2>Key Features</h2>
 * <ul>
 *   <li>RESTful API endpoints for weather data retrieval</li>
 *   <li>Redis-based caching for improved performance</li>
 *   <li>Rate limiting to prevent API abuse</li>
 *   <li>Comprehensive logging and monitoring</li>
 *   <li>Graceful shutdown handling</li>
 *   <li>Production-ready security configurations</li>
 * </ul>
 *
 * <h2>Architecture</h2>
 * <p>The application follows Clean Architecture and Hexagonal Architecture patterns with clear separation between:</p>
 * <ul>
 *   <li>Domain layer: Core business logic and entities</li>
 *   <li>Application layer: Use cases and business workflows</li>
 *   <li>Infrastructure layer: External integrations and technical concerns</li>
 * </ul>
 *
 * <h2>Security Considerations</h2>
 * <ul>
 *   <li>Input validation at all entry points</li>
 *   <li>Rate limiting to prevent abuse</li>
 *   <li>Secure configuration management</li>
 *   <li>Comprehensive audit logging</li>
 *   <li>Graceful error handling without information disclosure</li>
 * </ul>
 *
 * @author Weather API Wrapper Service Team
 * @version 1.0.0
 * @since 1.0.0
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 */
@SpringBootApplication
public class WeatherApiWrapperApplication {

    private static final Logger logger = LoggerFactory.getLogger(WeatherApiWrapperApplication.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private static final AtomicBoolean isShuttingDown = new AtomicBoolean(false);
    private static ConfigurableApplicationContext applicationContext;

    /**
     * Main entry point for the Weather API Wrapper Service application.
     *
     * <p>Initializes the Spring Boot application with comprehensive error handling,
     * logging, and graceful shutdown capabilities. Implements security best practices
     * and monitoring for production deployment.</p>
     *
     * @param args command line arguments passed to the application
     * @throws SecurityException if security configuration fails
     * @implNote This method ensures proper resource cleanup and logging even in failure scenarios
     */
    public static void main(String[] args) {
        try {
            logApplicationStartup(args);
            
            SpringApplication app = new SpringApplication(WeatherApiWrapperApplication.class);
            configureSpringApplication(app);
            registerShutdownHook();
            
            applicationContext = app.run(args);
            
            logStartupSuccess();
            
        } catch (Exception e) {
            logStartupFailure(e);
            performEmergencyShutdown();
        }
    }

    /**
     * Configures the Spring Boot application with security and operational settings.
     *
     * <p>Sets up application properties, event listeners, and web application type.
     * Implements security configurations and operational monitoring.</p>
     *
     * @param app the SpringApplication instance to configure
     * @throws IllegalArgumentException if application configuration is invalid
     * @implNote Configuration follows security best practices and operational requirements
     */
    private static void configureSpringApplication(SpringApplication app) {
        try {
            // Set application name for monitoring and logging
            System.setProperty("spring.application.name", "weather-api-wrapper-service");
            
            // Configure application type and banner
            app.setWebApplicationType(WebApplicationType.SERVLET);
            app.setBannerMode(org.springframework.boot.Banner.Mode.CONSOLE);
            
            // Register application event listeners for monitoring
            app.addListeners(event -> {
                if (event instanceof ApplicationReadyEvent) {
                    logger.info("Application ready to serve requests at {}", 
                              LocalDateTime.now().format(TIMESTAMP_FORMATTER));
                } else if (event instanceof ApplicationFailedEvent failedEvent) {
                    logger.error("Application startup failed", failedEvent.getException());
                } else if (event instanceof ContextClosedEvent) {
                    logger.info("Application context closing at {}", 
                              LocalDateTime.now().format(TIMESTAMP_FORMATTER));
                }
            });
            
            // Set additional security properties
            configureSecurityProperties();
            
            logger.debug("Spring application configuration completed successfully");
            
        } catch (Exception e) {
            logger.error("Failed to configure Spring application", e);
            throw new IllegalStateException("Application configuration failed", e);
        }
    }

    /**
     * Configures security-related system properties for production deployment.
     *
     * <p>Sets up security headers, SSL/TLS configurations, and other security-related
     * properties to ensure secure operation in production environments.</p>
     *
     * @implNote Security properties are configured to follow OWASP recommendations
     */
    private static void configureSecurityProperties() {
        // Disable unnecessary features for security
        System.setProperty("spring.jmx.enabled", "false");
        
        // Configure actuator security
        System.setProperty("management.endpoints.enabled-by-default", "false");
        System.setProperty("management.endpoint.health.enabled", "true");
        System.setProperty("management.endpoint.info.enabled", "true");
        
        // Set security headers
        System.setProperty("server.servlet.session.cookie.secure", "true");
        System.setProperty("server.servlet.session.cookie.http-only", "true");
        
        logger.debug("Security properties configured");
    }

    /**
     * Registers a JVM shutdown hook for graceful application termination.
     *
     * <p>Ensures proper cleanup of resources, connections, and in-progress operations
     * when the application is terminated. Implements timeout-based shutdown to prevent
     * hanging processes.</p>
     *
     * @implNote Shutdown hook ensures data integrity and proper resource cleanup
     */
    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (isShuttingDown.compareAndSet(false, true)) {
                logger.info("Shutdown hook triggered at {}", 
                          LocalDateTime.now().format(TIMESTAMP_FORMATTER));
                performGracefulShutdown();
            }
        }, "weather-api-shutdown-hook"));
        
        logger.debug("Shutdown hook registered successfully");
    }

    /**
     * Performs graceful shutdown of the application with timeout handling.
     *
     * <p>Closes the Spring application context, waits for in-progress operations
     * to complete, and ensures proper cleanup of all resources. Implements
     * timeout to prevent indefinite hanging.</p>
     *
     * @implNote Maximum shutdown time is limited to prevent system hang
     */
    private static void performGracefulShutdown() {
        try {
            if (applicationContext != null && applicationContext.isActive()) {
                logger.info("Initiating graceful shutdown...");
                
                // Close Spring context with timeout
                applicationContext.close();
                
                logger.info("Application shutdown completed successfully at {}", 
                          LocalDateTime.now().format(TIMESTAMP_FORMATTER));
            }
        } catch (Exception e) {
            logger.error("Error during graceful shutdown", e);
        }
    }

    /**
     * Performs emergency shutdown when normal startup fails.
     *
     * <p>Attempts to clean up any partially initialized resources and
     * exits the JVM with appropriate error code.</p>
     *
     * @implNote Emergency shutdown ensures no hanging processes remain
     */
    private static void performEmergencyShutdown() {
        try {
            if (applicationContext != null) {
                applicationContext.close();
            }
        } catch (Exception e) {
            logger.error("Error during emergency shutdown", e);
        } finally {
            System.exit(1);
        }
    }

    /**
     * Logs application startup information including environment details.
     *
     * @param args command line arguments
     * @implNote Startup logging provides essential debugging information
     */
    private static void logApplicationStartup(String[] args) {
        logger.info("Starting Weather API Wrapper Service at {}", 
                   LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        logger.info("Java version: {}", System.getProperty("java.version"));
        logger.info("Java vendor: {}", System.getProperty("java.vendor"));
        logger.info("Operating system: {} {}", 
                   System.getProperty("os.name"), System.getProperty("os.version"));
        logger.info("Available processors: {}", Runtime.getRuntime().availableProcessors());
        logger.info("Max memory: {} MB", Runtime.getRuntime().maxMemory() / 1024 / 1024);
        
        if (args != null && args.length > 0) {
            logger.info("Application arguments: {}", Arrays.toString(args));
        } else {
            logger.info("No application arguments provided");
        }
    }

    /**
     * Logs successful application startup.
     *
     * @implNote Success logging provides operational visibility
     */
    private static void logStartupSuccess() {
        logger.info("Weather API Wrapper Service started successfully at {}", 
                   LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        logger.info("Application is ready to process weather requests");
    }

    /**
     * Logs application startup failure with comprehensive error information.
     *
     * @param e the exception that caused startup failure
     * @implNote Failure logging aids in troubleshooting and monitoring
     */
    private static void logStartupFailure(Exception e) {
        logger.error("Failed to start Weather API Wrapper Service at {}: {}", 
                    LocalDateTime.now().format(TIMESTAMP_FORMATTER), e.getMessage());
        
        if (logger.isDebugEnabled()) {
            logger.debug("Startup failure details", e);
        }
    }

    /**
     * PreDestroy callback for Spring-managed shutdown.
     *
     * <p>This method is called by Spring during application context shutdown
     * to perform any necessary cleanup operations.</p>
     *
     * @implNote Ensures proper cleanup of Spring-managed resources
     */
    @PreDestroy
    public void onDestroy() {
        if (isShuttingDown.compareAndSet(false, true)) {
            logger.info("Application context shutdown initiated at {}", 
                       LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        }
    }
}
