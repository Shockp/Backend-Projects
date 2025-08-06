package com.personalblog;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the Personal Blog Application.
 * 
 * This class bootstraps the Spring Boot Application and configures
 * auto-configuration, component scanning, and configuration properties.
 * 
 * This Spring Boot application provides modern, secure personal blog platform
 * with features including user authentication, blog post management,
 * categorization, and responsive web interface.
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 1.0
 */
@SpringBootApplication
@EnableJpaAuditing
public class PersonalBlogApplication {

    private static final Logger logger = LoggerFactory.getLogger(PersonalBlogApplication.class);

    /**
     * Main method that starts the Spring Boot application.
     * 
     * @param args Command line arguments passed to the application.
     */
    public static void main(String[] args) {
        logger.info("Starting Personal Blog Application...");
        SpringApplication.run(PersonalBlogApplication.class, args);
        logger.info("Personal Blog Application started successfully!");
    }

    /**
     * CommandLineRunner bean that executes after the application starts.
     * Useful for initialization tasks and debugging.
     * 
     * @param ctx The spring application context
     * @return CommandLineRunner instance
     */
    @Bean
    public CommandLineRunner commandLineRunner (ApplicationContext ctx) {
        return args -> {
            logger.info("Personal Blog Application is ready!");
            logger.info("Available at: http://localhost:8080");

            // Print loaded beans count for debugging
            int beanCount = ctx.getBeanDefinitionNames().length;
            logger.info("Loaded beans count: {}", beanCount);
        };
    }
}