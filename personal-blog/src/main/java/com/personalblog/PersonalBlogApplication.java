package com.personalblog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * Main entry point for the Personal Blog Application.
 * 
 * This class bootstraps the Spring Boot Application and configures
 * auto-configuration, component scanning, and configuration properties.
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @version 1.0
 * @since 2025-08-02
 */
@SpringBootApplication
public class PersonalBlogApplication {
    /*
     * Main method that starts the Spring Boot application.
     * 
     * @param args Command line arguments passed to the application.
     */
    public static void main(String[] args) {
        SpringApplication.run(PersonalBlogApplication.class, args);
    }
}