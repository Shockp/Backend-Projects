package com.afb.expensetracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a financial expense record with a unique auto-generated ID, date,
 * description, amount, and category.
 * <p>
 * IDs are generated in a thread-safe manner using AtomicInteger to ensure
 * uniqueness across concurrent operations.
 * </p>
 */
public class Expense {
    /** Thread-safe generator for sequentially generated IDs. */
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);

    /** Unique identifier for the expense. */
    private final int id;
    /** Date when the expense occurred. */
    private LocalDate date;
    /** Description of the expense. */
    private String description;
    /** Amount of the expense. */
    private BigDecimal amount;
    /** Category of the expense. */
    private ExpenseCategory category;

    /**
     * Constructs a new Expense with an auto-generated unique ID.
     *
     * @param date        Date when the expense occurred.
     * @param description Description of the expense.
     * @param amount      Amount of the expense.
     * @param category    Category of the expense.
     */
    public Expense(LocalDate date, String description, BigDecimal amount,
                   ExpenseCategory category) {
        this.id = ID_GENERATOR.incrementAndGet();
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.category = category;
    }

    /**
     * Constructs an Expense with a specified ID, used during
     * JSON deserialization.
     * The ID generator is updated to prevent future conflicts.
     *
     * @param id Unique identifier for the expense.
     * @param date Date when the expense occurred.
     * @param description Description of the expense.
     * @param amount Amount of the expense.
     * @param category Category of the expense.
     */
    public Expense(int id, LocalDate date, String description, BigDecimal amount,
                   ExpenseCategory category) {
        this.id = id;
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.category = category;
        ID_GENERATOR.updateAndGet(current -> Math.max(current, id));
    }

    /** Default constructor for JSON deserialization frameworks. */
    public Expense() {
        this.id = ID_GENERATOR.incrementAndGet();
    }

    /** @return The unique ID of this expense. */
    public int getId() {
        return id;
    }

    /** @return The date when the expense occurred. */
    public LocalDate getDate() {
        return date;
    }
    /** Set the date when the expense occurred.
     *
     * @param date The new date.
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /** @return The description of the expense. */
    public String getDescription() {
        return description;
    }
    /** Set the description of the expense.
     *
     * @param description The new description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /** @return The amount of the expense. */
    public BigDecimal getAmount() {
        return amount;
    }
    /** Set the amount of the expense.
     *
     * @param amount The new amount.
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /** @return The category of the expense. */
    public ExpenseCategory getCategory() {
        return category;
    }
    /** Set the category of the expense.
     *
     * @param category The new category.
     */
    public void setCategory(ExpenseCategory category) {
        this.category = category;
    }

    /**
     * Equality is based solely on the unique ID.
     *
     * @param o The object to compare.
     * @return True if IDs match; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Expense)) return false;

        Expense expense = (Expense) o;
        return id == expense.id;
    }

    /** @return Hash code based on the unique ID. */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /** @return Readable string representation of the expense. */
    @Override
    public String toString() {
        return String.format(
                "Expense{id=%d, date=%s, description='%s', amount=%s, category=%s}",
                id, date, description, amount, category
        );
    }
}