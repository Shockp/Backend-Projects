package com.personalblog.repository.integration;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Test suite runner for all repository integration tests.
 * 
 * <p>
 * This class runs all integration tests in the repository integration package,
 * providing a single entry point for executing the complete integration test suite.
 * </p>
 * 
 * <p>
 * Usage:
 * </p>
 * <pre>
 * mvn test -Dtest=IntegrationTestRunner
 * </pre>
 * 
 * @author Adrián Feito Blázquez (github.com/shockp)
 * @since 1.0.0
 */
@Suite
@SuiteDisplayName("Repository Integration Test Suite")
@SelectPackages("com.personalblog.repository.integration")
@IncludeClassNamePatterns(".*IntegrationTest.*")
public class IntegrationTestRunner {
    // This class serves as a test suite runner
    // No implementation needed - annotations handle the configuration
}