package com.afb.expensetracker;

import com.afb.expensetracker.command.*;
import com.afb.expensetracker.command.validation.ValidationException;
import com.afb.expensetracker.model.Expense;
import com.afb.expensetracker.model.ExpenseCategory;
import com.afb.expensetracker.persistence.StorageManager;
import com.afb.expensetracker.service.*;

import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Main entry point for the Expense Tracker CLI application.
 * <p>
 * This class orchestrates the entire application flow by initializing services,
 * parsing command line arguments, and delegating operations to appropriate
 * service components. It serves as the controller in the MVC-like architecture.
 * </p>
 */
public class ExpenseTrackerCLI {
    /** Command parser for processing CLI arguments. */
    private final CommandParser commandParser;
    /** Storage manager for data persistence. */
    private final StorageManager storageManager;
    /** Service for expense CRUD operations. */
    private final ExpenseService expenseService;
    /** Service for expense summaries and calculations. */
    private final SummaryService summaryService;
    /** Service for budget management. */
    private final BudgetService budgetService;
    /** Service for category operations. */
    private final CategoryService categoryService;
    /** Service for export operations. */
    private final ExportService exportService;

    /**
     * Constructs a new ExpenseTrackerCLI and initializes all services.
     */
    public ExpenseTrackerCLI() {
        this.commandParser = new CommandParser();
        this.storageManager = new StorageManager();
        this.expenseService = new ExpenseService(storageManager);
        this.summaryService = new SummaryService(expenseService);
        this.budgetService = new BudgetService(summaryService);
        this.categoryService = new CategoryService();
        this.exportService = new ExportService(expenseService, summaryService);
    }

    /**
     * Main entry point of the application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        ExpenseTrackerCLI app = new ExpenseTrackerCLI();
        app.run(args);
    }

    /**
     * Runs the application with the provided command line arguments.
     *
     * @param args Command line arguments
     */
    public void run(String[] args) {
        try {
            ParsedCommand parsedCommand = commandParser.parseCommand(args);

            if (parsedCommand.isShowHelp()) {
                commandParser.printHelp();
                return;
            }

            executeCommand(parsedCommand);
        } catch (ParseException e) {
            System.err.println("Error parsing command: " + e.getMessage());
            System.err.println("Use --help for usage information.");
            System.exit(1);
        } catch (ValidationException e) {
            System.err.println("Validation error: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Executes the parsed command by delegating to appropriate service methods.
     *
     * @param parsedCommand The parsed and validated command
     */
    private void executeCommand(ParsedCommand parsedCommand) {
        Command command = parsedCommand.getCommand();

        switch (command) {
            case ADD:
                executeAddCommand(parsedCommand);
                break;
            case UPDATE:
                executeUpdateCommand(parsedCommand);
                break;
            case DELETE:
                executeDeleteCommand(parsedCommand);
                break;
            case LIST:
                executeListCommand(parsedCommand);
                break;
            case SUMMARY:
                executeSummaryCommand(parsedCommand);
                break;
            case EXPORT:
                executeExportCommand(parsedCommand);
                break;
            case SET_BUDGET:
                executeSetBudgetCommand(parsedCommand);
                break;
            case HELP:
                commandParser.printHelp();
                break;
            default:
                throw new IllegalArgumentException("Unsupported command: "
                        + command);
        }
    }

    /**
     * Executes the ADD command to create a new expense.
     */
    private void executeAddCommand(ParsedCommand parsedCommand) {
        String description = parsedCommand.getArgument("description",
                String.class);
        BigDecimal amount = parsedCommand.getArgument("amount",
                BigDecimal.class);
        ExpenseCategory category = parsedCommand.getArgument("category",
                ExpenseCategory.class);

        Expense expense = expenseService.addExpense(description, amount,
                category);
        System.out.printf("Expense added successfully (ID: %d)%n",
                expense.getId());

        checkBudgetWarning(LocalDate.now().getMonthValue());
    }

    /**
     * Executes the UPDATE command to modify an existing expense.
     */
    private void executeUpdateCommand(ParsedCommand parsedCommand) {
        Integer id = parsedCommand.getArgument("id", Integer.class);
        String description = parsedCommand.getArgument("description",
                String.class);
        BigDecimal amount = parsedCommand.getArgument("amount",
                BigDecimal.class);
        ExpenseCategory category = parsedCommand.getArgument("category",
                ExpenseCategory.class);

        Optional<Expense> updatedExpense = expenseService.updateExpense(
                id, description, amount, category);

        if (updatedExpense.isPresent()) {
            System.out.printf("Expense updated successfully (ID: %d)%n", id);

            if (amount != null) {
                checkBudgetWarning(LocalDate.now().getMonthValue());
            }
        } else {
            System.err.printf("Expense with ID %d not found%n", id);
        }
    }

    /**
     * Executes the DELETE command to remove an expense.
     */
    private void executeDeleteCommand(ParsedCommand parsedCommand) {
        Integer id = parsedCommand.getArgument("id", Integer.class);

        boolean deleted = expenseService.deleteExpense(id);

        if (deleted) {
            System.out.printf("Expense deleted successfully (ID: %d)%n", id);
        } else {
            System.err.printf("Expense with ID %d not found%n", id);
        }
    }

    /**
     * Executes the LIST command to display expenses.
     */
    private void executeListCommand(ParsedCommand parsedCommand) {
        ExpenseCategory category = parsedCommand.getArgument("category",
                ExpenseCategory.class);
        Integer month = parsedCommand.getArgument("month", Integer.class);

        List<Expense> expenses;

        if (category != null) {
            expenses = expenseService.getExpensesByCategory(category);
        } else {
            expenses = expenseService.getAllExpenses();
        }

        if (month != null) {
            int currentYear = LocalDate.now().getYear();
            Month targetMonth = Month.of(month);
            expenses = expenses.stream()
                    .filter(expense -> expense.getDate().getMonth() ==
                            targetMonth && expense.getDate().getYear() ==
                            currentYear)
                    .toList();
        }

        displayExpensesList(expenses);
    }

    /**
     * Executes the SUMMARY command to display expense summaries.
     */
    private void executeSummaryCommand(ParsedCommand parsedCommand) {
        Integer month = parsedCommand.getArgument("month", Integer.class);

        if (month != null) {
            BigDecimal monthlyTotal = summaryService.calculateMonthlyTotal(month);
            System.out.printf("Total expenses for %s: $%.2f%n",
                    Month.of(month).name(), monthlyTotal);

            displayBudgetInfo(month);

            Map<ExpenseCategory, BigDecimal> categoryTotals =
                    summaryService.calculateMonthlyTotalByCategory(month);

            if (!categoryTotals.isEmpty()) {
                System.out.println("\nCategory breakdown:");
                categoryTotals.entrySet().stream()
                        .sorted(Map.Entry.<ExpenseCategory,
                                BigDecimal>comparingByValue().reversed())
                        .forEach(entry ->
                                System.out.printf("  %s: $%.2f%n",
                                entry.getKey().getCategoryName(), entry.getValue()));
            }
        } else {
            BigDecimal total = summaryService.calculateTotal();
            System.out.printf("Total expenses: $%.2f%n", total);

            Map<ExpenseCategory, BigDecimal> categoryTotals =
                    summaryService.calculateTotalByCategory();

            if (!categoryTotals.isEmpty()) {
                System.out.println("\nCategory breakdown:");
                categoryTotals.entrySet().stream()
                        .sorted(Map.Entry.<ExpenseCategory,
                                BigDecimal>comparingByValue().reversed())
                        .forEach(entry ->
                                System.out.printf("  %s: $%.2f%n",
                                entry.getKey().getCategoryName(), entry.getValue()));
            }
        }
    }

    /**
     * Executes the EXPORT command to export expenses to CSV.
     */
    private void executeExportCommand(ParsedCommand parsedCommand) {
        String filePath = parsedCommand.getArgument("file", String.class);
        ExpenseCategory category = parsedCommand.getArgument("category",
                ExpenseCategory.class);
        Integer month = parsedCommand.getArgument("month", Integer.class);

        try {
            boolean success;

            if (category != null) {
                success = exportService.exportCategoryExpenses(category, filePath);
            } else if (month != null) {
                success = exportService.exportMonthlyExpenses(month, filePath);
            } else {
                success = exportService.exportAllExpenses(filePath);
            }

            if (success) {
                System.out.printf("Expenses exported successfully to %s%n",
                        filePath);
            } else {
                System.err.println("Failed to export expenses");
            }
        } catch (IOException e) {
            System.err.println("Failed to export expenses: " + e.getMessage());
        }
    }

    /**
     * Executes the SET_BUDGET command to set monthly budget limits
     */
    private void executeSetBudgetCommand(ParsedCommand parsedCommand) {
        Integer month = parsedCommand.getArgument("month", Integer.class);
        BigDecimal budget = parsedCommand.getArgument("budget", BigDecimal.class);

        budgetService.setBudget(month, budget);
        System.out.printf("Budget for %s set to $%.2f%n",
                Month.of(month).name(), budget);

        displayBudgetInfo(month);
    }

    /**
     * Displays a formatted list of expenses
     */
    private void displayExpensesList(List<Expense> expenses) {
        if (expenses.isEmpty()) {
            System.out.println("No expenses found");
            return;
        }

        System.out.printf("%-4s | %-12s | %-30s | %-10s | %-15s%n",
                "ID", "Date", "Description", "Amount", "Category");
        System.out.println("-------|--------------|--------------------------------|" +
                "------------|----------------");

        for (Expense expense : expenses) {
            System.out.printf("%-4d | %-12s | %-30s | $%-9.2f | %-15s%n",
                    expense.getId(),
                    expense.getDate().toString(),
                    truncateString(expense.getDescription(), 30),
                    expense.getAmount(),
                    expense.getCategory().getCategoryName());
        }

        BigDecimal total = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        System.out.println("-------|--------------|--------------------------------|" +
                "------------|----------------");
        System.out.printf("Total: $%.2f%n", total);
    }

    /**
     * Displays budget information for a specific month.
     */
    private void displayBudgetInfo(Integer month) {
        BigDecimal budget = budgetService.getBudget(month);
        BigDecimal spent = summaryService.calculateMonthlyTotal(month);
        BigDecimal remaining = budgetService.getRemainingBudget(month);
        BigDecimal utilization =
                budgetService.getBudgetUtilizationPercentage(month);

        System.out.printf("Budget for %s: $%.2f%n", Month.of(month).name(), budget);
        System.out.printf("Spent: $%.2f (%.1f%%)%n", spent, utilization);
        System.out.printf("Remaining: $%.2f%n", remaining);

        if (budgetService.isBudgetExceeded(month)) {
            System.out.println("WARNING: Budget exceeded!");
        }
    }

    /**
     * Checks and displays budget warning if current month budget is exceeded.
     */
    private void checkBudgetWarning(Integer month) {
        if (budgetService.isBudgetExceeded(month)) {
            BigDecimal overspent = budgetService.getRemainingBudget(month).abs();
            System.err.printf("WARNING: You have exceeded your %s budget by" +
                    " $%.2f%n", Month.of(month).name(), overspent);
        }
    }

    /**
     * Truncates a string to the specified length with ellipsis if necessary.
     */
    private String truncateString(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
}