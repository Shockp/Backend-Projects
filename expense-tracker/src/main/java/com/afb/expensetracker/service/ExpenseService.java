package com.afb.expensetracker.service;

import com.afb.expensetracker.model.Expense;
import com.afb.expensetracker.model.ExpenseCategory;
import com.afb.expensetracker.persistence.StorageManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class that handles expense-related business operations.
 * <p>
 * This service provides methods for adding, updating, deleting, and retrieving
 * expenses. It acts as an intermediary between the CLI interface and the
 * persistence layer, implementing business logic and validations.
 * </p>
 */
public class ExpenseService {

    /** Storage manager for persisting and retrieving expense data. */
    private final StorageManager storageManager;
    /** In-memory cache of expenses to reduce file I/O operations. */
    private List<Expense> expenses;

    /**
     * Constructs a new ExpenseService with the specified storage manager.
     * <p>
     * Initializes the service and loads existing expenses from persistent storage.
     * </p>
     *
     * @param storageManager The storage manager to use for persistence operations
     */
    public ExpenseService(StorageManager storageManager) {
        this.storageManager = storageManager;
        this.expenses = storageManager.loadExpenses();
    }

    /**
     * Adds a new expense with the provided details.
     * <p>
     * Creates a new expense with the given description, amount, and category,
     * using the current date as the expense date.
     * </p>
     *
     * @param description A description of the expense
     * @param amount The monetary amount of the expense
     * @param category The category of the expense
     * @return The newly created expense
     * @throws IllegalArgumentException If description is empty or amount is negative
     */
    public Expense addExpense(String description, BigDecimal amount,
                              ExpenseCategory category) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }

        Expense expense = new Expense(LocalDate.now(), description,
                amount, category);
        expenses.add((expense));
        storageManager.saveExpenses(expenses);
        return expense;
    }

    /**
     * Updates an existing expense with the provided details.
     * <p>
     * Finds the expense with the given ID and updates its description, amount,
     * and category if provided. Fields with null values are not updated.
     * </p>
     *
     * @param id The ID of the expense to update
     * @param description The new description (or null to keep existing)
     * @param amount The new amount (or null to keep existing)
     * @param category The new category (or null to keep existing)
     * @return The updated expense, or empty if no expense with the given ID exists
     */
    public Optional<Expense> updateExpense(int id, String description,
                                           BigDecimal amount, ExpenseCategory category) {
        Optional<Expense> expenseOptional = findExpenseById(id);

        if (expenseOptional.isPresent()) {
            Expense expense = expenseOptional.get();

            if (description != null && !description.trim().isEmpty()) {
                expense.setDescription(description);
            }

            if (amount != null && amount.compareTo(BigDecimal.ZERO) >= 0) {
                expense.setAmount(amount);
            }

            if (category != null) {
                expense.setCategory(category);
            }

            storageManager.saveExpenses(expenses);
        }

        return expenseOptional;
    }

    /**
     * Deletes an expense with the specified ID.
     * <p>
     * Removes the expense with the given ID from the list and updates
     * the persistent storage.
     * </p>
     *
     * @param id The ID of the expense to delete
     * @return true if the expense was found and deleted, false otherwise
     */
    public boolean deleteExpense(int id) {
        Optional<Expense> expenseOptional = findExpenseById(id);
        int initialSize = expenses.size();

        if (expenseOptional.isPresent()) {
            expenses.remove(expenseOptional.get());
            storageManager.saveExpenses(expenses);
        }

        return expenses.size() < initialSize;
    }

    /**
     * Retrieves all expenses.
     *
     * @return A list of all expenses
     */
    public List<Expense> getAllExpenses() {
        return new ArrayList<>(expenses);
    }

    /**
     * Finds an expense by its ID.
     *
     * @param id The ID of the expense to find
     * @return An Optional containing the expense if found, or empty if not found
     */
    public Optional<Expense> findExpenseById(int id) {
        return expenses.stream()
                .filter(expense -> expense.getId() == id)
                .findFirst();
    }

    /**
     * Filters expenses by category.
     *
     * @param category The category to filter by
     * @return A list of expenses in the specified category
     */
    public List<Expense> getExpensesByCategory(ExpenseCategory category) {
        return expenses.stream()
                .filter(expense -> expense.getCategory() == category)
                .collect(Collectors.toList());
    }
}
