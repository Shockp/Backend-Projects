package com.afb.expensetracker.service;

import com.afb.expensetracker.model.Expense;
import com.afb.expensetracker.model.ExpenseCategory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service class that provides functionality to export expense data to CSV format.
 * <p>
 * This service allows exporting all expenses or filtered expenses to a CSV file.
 * </p>
 */
public class ExportService {
    /** The expense service used to retrieve expense data. */
    private final ExpenseService expenseService;
    /** The summary service used for expense sumaries. */
    private final SummaryService summaryService;
    /** Date formatter for formatting expense dates in the export */
    private final DateTimeFormatter dateFormatter;

    /**
     * Constructs a new ExportService with the specified services.
     *
     * @param expenseService The expense service to use for retrieving expense data
     * @param summaryService The summary service to use for expense summaries
     */
    public ExportService(ExpenseService expenseService, SummaryService summaryService) {
        this.expenseService = expenseService;
        this.summaryService = summaryService;
        this.dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    }

    /**
     * Exports all expenses to a CSV file.
     * <p>
     * Creates a CSV file with all expenses, including ID, date, description,
     * amount, and category.
     * </p>
     *
     * @param filePath The path where the CSV file should be saved
     * @return true if the export was successful, false otherwise
     * @throws IOException if an I/O error occurs
     */
    public boolean exportAllExpenses(String filePath) throws IOException {
        List<Expense> expenses = expenseService.getAllExpenses();
        return exportExpensesToCSV(expenses, filePath);
    }

    /**
     * Exports expenses for a specific month to a CSV file.
     * <p>
     * Creates a CSV file with expenses from the specified month of the current year.
     * </p>
     *
     * @param month The month (1-12) to export expenses for
     * @param filePath The path where the CSV file should be saved
     * @return true if the export was successful, false otherwise
     * @throws IllegalArgumentException if the month is not between 1 and 12
     * @throws IOException if an I/O error occurs
     */
    public boolean exportMonthlyExpenses(int month, String filePath)
        throws  IOException {
        if (month < 1 ||month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }

        List<Expense> allExpenses = expenseService.getAllExpenses();
        List<Expense> monthlyExpenses = allExpenses.stream()
                .filter(expense -> expense.getDate().getMonthValue() == month
                    && expense.getDate().getYear() == LocalDate.now().getYear())
                .toList();

        return exportExpensesToCSV(monthlyExpenses, filePath);
    }

    /**
     * Exports expenses for a specific category to a CSV file.
     * <p>
     * Creates a CSV file with expenses from the specified category.
     * </p>
     *
     * @param category The category to export expenses for
     * @param filePath The path where the CSV file should be saved
     * @return true if the export was successful, false otherwise
     * @throws IOException if an I/O error occurs
     */
    public boolean exportCategoryExpenses(ExpenseCategory category,
                                          String filePath) throws  IOException {
        List<Expense> categoryExpenses = expenseService.getExpensesByCategory(category);
        return exportExpensesToCSV(categoryExpenses, filePath);
    }

    /**
     * Exports a summary of expenses by category to a CSV file.
     * <p>
     * Creates a CSV file with the total amount for each category.
     * </p>
     *
     * @param filePath The path where the CSV file should be saved
     * @return true if the export was successful, false otherwise
     * @throws IOException if an I/O error occurs
     */
    public boolean exportCategorySummary(String filePath) throws IOException {
        Map<ExpenseCategory, BigDecimal> categorySummary =
                summaryService.calculateTotalByCategory();

        File file = new File(filePath);
        file.getParentFile().mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("Category,Total Amount");
            writer.newLine();

            for (Map.Entry<ExpenseCategory, BigDecimal>
                    entry : categorySummary.entrySet()) {
                writer.write(String.format("%s,%s",
                        entry.getKey().getCategoryName(),
                        entry.getValue().toString()));
                writer.newLine();
            }

            return true;
        }
    }

    /**
     * Helper method to export a list of expenses to a CSV file.
     *
     * @param expenses The list of expenses to export
     * @param filePath The path where the CSV file should be saved
     * @return true if the export was successful, false otherwise
     * @throws IOException if an I/O error occurs
     */
    private boolean exportExpensesToCSV(List<Expense> expenses, String filePath)
        throws IOException {
        File file = new File(filePath);
        file.getParentFile().mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("ID,Date,Description,Amount,Category");
            writer.newLine();

            for (Expense expense : expenses) {
                writer.write(String.format("%d,%s,%s,%s,%s",
                        expense.getId(),
                        expense.getDate().format(dateFormatter),
                        escapeCSV(expense.getDescription()),
                        expense.getAmount().toString(),
                        expense.getCategory().getCategoryName()));
                writer.newLine();
            }

            return true;
        }
    }

    /**
     * Escapes special characters in a string for CSV format.
     *
     * @param input The string to escape
     * @return The escaped string
     */
    private String escapeCSV(String input) {
        if (input == null) {
            return "";
        }

        if (input.contains(",") || input.contains("\"") ||
            input.contains("\n")) {
            return "\"" + input.replace("\"", "\"\"" + "\"");
        }

        return input;
    }
}
