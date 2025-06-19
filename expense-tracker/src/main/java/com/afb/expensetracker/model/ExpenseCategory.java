package com.afb.expensetracker.model;

/**
 * Enumerates predefined categories for classifying expenses.
 */
public enum ExpenseCategory {

    FOOD("Food & Drinks"),
    TRANSPORTATION("Transportation"),
    ENTERTAINMENT("Entertainment"),
    UTILITIES("Utilities"),
    HEALTHCARE("Healthcare"),
    SHOPPING("Shopping"),
    EDUCATION("Education"),
    TRAVEL("Travel"),
    HOUSING("Housing"),
    INSURANCE("Insurance"),
    PERSONAL_CARE("Personal Care"),
    BUSINESS("Business"),
    MISCELLANEOUS("Miscellaneous");

    /**
     * Category name shown to the user.
     */
    private final String categoryName;

    /**
     * Constructs an ExpenseCategory with the given category name.
     *
     * @param categoryName The category name for the expense.
     */
    ExpenseCategory(String categoryName) {
        this.categoryName = categoryName;
    }

    /**
     * @return The category name shown to the user.
     */
    public String getCategoryName() {
        return categoryName;
    }

    /**
     * Converts the category name to the corresponding enum constant.
     *
     * @param categoryName The category name to convert.
     * @return The corresponding enum constant.
     * @throws IllegalArgumentException If the category name is not recognized.
     */
    public static ExpenseCategory fromCategoryName(String categoryName) {
        for (ExpenseCategory expenseCategory : values()) {
            if (expenseCategory.getCategoryName().equalsIgnoreCase(categoryName)) {
                return expenseCategory;
            }
        }
        throw new IllegalArgumentException("Unknown category: " + categoryName);
    }

    /**
     * Converts an enum name (case-insensitive) to the corresponding constant.
     *
     * @param categoryName The name of the enum constant.
     * @return The matching ExpenseCategory.
     * @throws IllegalArgumentException If the category name is not recognized.
     */
    public static ExpenseCategory fromString(String categoryName) {
        try {
            return valueOf(categoryName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("unknown category: "
                    + categoryName);
        }
    }

    /**
     * @return The category name when this enum is printed.
     */
    @Override
    public String toString() {
        return categoryName;
    }
}
