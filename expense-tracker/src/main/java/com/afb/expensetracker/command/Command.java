package com.afb.expensetracker.command;

/**
 * Enumeration of all supported commands in the expense tracker CLI application.
 * <p>
 * Each command represents a specific operation that users can perform through
 * the command line interface. The enum includes command names, descriptions,
 * and required argument specifications.
 * </p>
 */
public enum Command {

    /** Add a new expense to the tracker. */
    ADD("add", "Add a new expense", new String[]{"description", "amount"},
            new String[]{"category"}),

    /** Update an existing expense. */
    UPDATE("update", "Update an existing expense", new String[]{"id"},
            new String[]{"description", "amount", "category"}),

    /** Delete an expense from the tracker. */
    DELETE("delete", "Delete an expense", new String[]{"id"},
            new String[]{}),

    /** List all expenses or filter by criteria. */
    LIST("list", "List expenses", new String[]{},
            new String[]{"category", "month"}),

    /** Show expense summaries. */
    SUMMARY("summary", "Show expense summary", new String[]{},
            new String[]{"month"}),

    /** Export expenses to CSV file. */
    EXPORT("export", "Export expenses to CSV", new String[]{"file"},
            new String[]{"category", "month"}),

    /** Set monthly budget limits. */
    SET_BUDGET("set-budget", "Set monthly budget limit",
            new String[]{"month", "budget"}, new String[]{}),

    /** Display help information. */
    HELP("help", "Show help information", new String[]{},
            new String[]{});

    /** The command name as it appears in CLI arguments. */
    private final String commandName;

    /** Human-readable description of the command. */
    private final String description;

    /** Required arguments for this command. */
    private final String[] requiredArguments;

    /** Optional arguments for this command. */
    private final String[] optionalArguments;

    /**
     * Constructs a Command with the specified name, description, and argument specifications.
     *
     * @param commandName The name of the command as used in CLI
     * @param description A human-readable description of the command
     * @param requiredArguments Array of required argument names
     * @param optionalArguments Array of optional argument names
     */
    Command(String commandName, String description, String[] requiredArguments, String[] optionalArguments) {
        this.commandName = commandName;
        this.description = description;
        this.requiredArguments = requiredArguments.clone();
        this.optionalArguments = optionalArguments.clone();
    }

    /**
     * Returns the command name as used in CLI arguments.
     *
     * @return The command name string
     */
    public String getCommandName() {
        return commandName;
    }

    /**
     * Returns the human-readable description of the command.
     *
     * @return The command description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the required arguments for this command.
     *
     * @return Array of required argument names
     */
    public String[] getRequiredArguments() {
        return requiredArguments.clone();
    }

    /**
     * Returns the optional arguments for this command.
     *
     * @return Array of optional argument names
     */
    public String[] getOptionalArguments() {
        return optionalArguments.clone();
    }

    /**
     * Finds a Command by its command name (case-insensitive).
     *
     * @param commandName The command name to search for
     * @return The matching Command, or null if not found
     */
    public static Command fromString(String commandName) {
        if (commandName == null) {
            return null;
        }

        for (Command command : values()) {
            if (command.commandName.equalsIgnoreCase(commandName)) {
                return command;
            }
        }
        return null;
    }

    /**
     * Returns the command name when converting to string.
     *
     * @return The command name
     */
    @Override
    public String toString() {
        return commandName;
    }
}