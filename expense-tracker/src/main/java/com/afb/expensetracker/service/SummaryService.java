package com.afb.expensetracker.service;

import com.afb.expensetracker.model.Expense;
import com.afb.expensetracker.model.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class that provides expense summary and analysis functionality.
 * <p>
 * This service calculates various summaries of expenses, such as total expenses,
 * monthly expenses, and category-based summaries.
 * </p>
 */
public class SummaryService {
    /** The expense service used to retrieve expense data. */
    private ExpenseService expenseService;

    /**
     * Constructs a new SummaryService with the specified expense service.
     *
     * @param expenseService The expense service to use for retrieving expense data
     */
    public SummaryService(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    /**
     * Calculates the total sum of all expenses.
     *
     * @return The total amount of all expenses
     */
    public BigDecimal calculateTotal() {
        return expenseService.getAllExpenses().stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates the total expenses for a specific month of the current year.
     * <p>
     * Filters expenses by the specified month in the current year and
     * sums their amounts.
     * </p>
     *
     * @param month The month (1-12) to calculate expenses for
     * @return The total amount of expenses for the specified month
     * @throws IllegalArgumentException if the month is not between 1 and 12
     */
    public BigDecimal calculateMonthlyTotal(int month) {
        if (month < 1 ||month > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12");
        }

        int currentYear = LocalDate.now().getYear();
        Month targetMonth = Month.of(month);

        return expenseService.getAllExpenses().stream()
                .filter(expense -> expense.getDate().getMonth().equals(targetMonth) &&
                        expense.getDate().getYear() == currentYear)
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates the total expenses for each category.
     * <p>
     * Groups expenses by category and sums the amounts within each group.
     * </p>
     *
     * @return A map of categories to their total expense amounts
     */
    public Map<ExpenseCategory, BigDecimal> calculateTotalByCategory() {
        return expenseService.getAllExpenses().stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.mapping(
                                Expense::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));
    }

    /**
     * Calculates the total expenses for each category in a specific month.
     * <p>
     * Filters expenses by the specified month in the current year,
     * then groups them by category and sums the amounts within each group.
     * </p>
     *
     * @param month The month (1-12) to calculate expenses for
     * @return A map of categories to their total expense amounts for the month
     * @throws IllegalArgumentException if the month is not between 1 and 12
     */
    public Map<ExpenseCategory, BigDecimal> calculateMonthlyTotalByCategory(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }

        int currentYear = LocalDate.now().getYear();
        Month targetMonth = Month.of(month);

        return expenseService.getAllExpenses().stream()
                .filter(expense -> expense.getDate().getMonth().equals(targetMonth) &&
                        expense.getDate().getYear() == currentYear)
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.mapping(
                                Expense::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));
    }
}
