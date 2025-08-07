package com.personalblog.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Test configuration class for Category entity testing.
 * Provides necessary beans and configuration for comprehensive testing.
 * 
 * @author Test Configuration
 * @version 1.0
 */
@TestConfiguration
@EnableJpaRepositories(basePackages = "com.personalblog.repository")
@EnableTransactionManagement
@Profile("test")
public class TestConfig {

    /**
     * Provides a validator bean for constraint validation testing.
     * 
     * @return Validator instance for testing
     */
    @Bean
    @Primary
    public Validator validator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        return factory.getValidator();
    }
}