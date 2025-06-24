package com.afb.expensetracker.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Service class that manages monthly budget limits and provides budget-related operations.
 * <p>
 * This service allows setting and retrieving monthly budget limits, and checking
 * whether expenses exceed the budget.
 * </p>
 */
public class BudgetService {
    /** The summary service used to calculate expense totals. */
    private final SummaryService summaryService;
    /** Map storing the budget limits for each month */
    private final Map<Integer, BigDecimal> monthlyBudgets;
    /** Default budget amount from configuration */
    private final BigDecimal defaultBudget;

    /**
     * Constructs a new BudgetService with the specified summary service.
     * <p>
     * Initializes monthly budgets and loads the default budget amount from configuration.
     * </p>
     *
     * @param summaryService The summary service to use for expense calculations
     */
    public BudgetService(SummaryService summaryService) {
        this.summaryService = summaryService;
        this.monthlyBudgets = new HashMap<>();
        this.defaultBudget = loadDefaultBudget();
    }

    /**
     * Loads the default budget amount from the configuration file.
     *
     * @return The default budget amount, or 1000 if not specified
     */
    private BigDecimal loadDefaultBudget() {
        Properties properties = new Properties();
        try (InputStream in =
                getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                properties.load(in);
                String budgetStr = properties.getProperty("budget.default");
                if (budgetStr != null && !budgetStr.trim().isEmpty()) {
                    return new BigDecimal(budgetStr);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Warning: Could not load default budget: " +
                    e.getMessage());
        }
        return new BigDecimal("1000.00"); // Default fallback
    }

    /**
     * Sets the budget limit for a specific month.
     *
     * @param month The month (1-12) to set the budget for
     * @param limit The budget limit amount
     * @throws IllegalArgumentException if the month is not between 1 and 12 or the limit is negative
     */
    public void setBudget(int month, BigDecimal limit) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }

        if (limit == null || limit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Budget limit cannot be negative");
        }
        
        monthlyBudgets.put(month, limit);
    }

    /**
     * Gets the budget limit for a specific month.
     * <p>
     * Returns the configured budget for the specified month, or the default
     * budget if no specific budget has been set.
     * </p>
     *
     * @param month The month (1-12) to get the budget for
     * @return The budget limit for the specified month
     * @throws IllegalArgumentException if the month is not between 1 and 12
     */
    public BigDecimal getBudget(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }

        return monthlyBudgets.getOrDefault(month, defaultBudget);
    }

    /**
     * Checks if expenses for a specific month exceed the budget.
     *
     * @param month The month (1-12) to check
     * @return true if expenses exceed the budget, false otherwise
     * @throws IllegalArgumentException if the month is not between 1 and 12
     */
    public boolean isBudgetExceeded(int month) {
        if (month < 1 ||month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }

        BigDecimal budget = getBudget(month);
        BigDecimal expenses = summaryService.calculateMonthlyTotal(month);

        return expenses.compareTo(budget) > 0;
    }

    /**
     * Calculates the remaining budget for a specific month.
     * <p>
     * Subtracts the total expenses for the month from the budget limit.
     * </p>
     *
     * @param month The month (1-12) to calculate the remaining budget for
     * @return The remaining budget amount (negative if exceeded)
     * @throws IllegalArgumentException if the month is not between 1 and 12
     */
    public BigDecimal getRemainingBudget(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }

        BigDecimal budget = getBudget(month);
        BigDecimal expenses = summaryService.calculateMonthlyTotal(month);
        
        return budget.subtract(expenses);
    }

    /**
     * Gets the budget utilization percentage for a specific month.
     * <p>
     * Calculates what percentage of the budget has been used.
     * </p>
     *
     * @param month The month (1-12) to calculate the utilization for
     * @return The budget utilization as a percentage
     * @throws IllegalArgumentException if the month is not between 1 and 12
     */
    public BigDecimal getBudgetUtilizationPercentage(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }

        BigDecimal budget = getBudget(month);
        BigDecimal expenses = summaryService.calculateMonthlyTotal(month);

        if (budget.compareTo(BigDecimal.ZERO) == 0) {
            return expenses.compareTo(BigDecimal.ZERO) > 0 ?
                    new BigDecimal("100") : BigDecimal.ZERO;
        }

        return expenses.multiply(new BigDecimal("100")).divide(
                budget, 2, RoundingMode.HALF_UP);
    }
}
