package com.afb.expensetracker.command;

/**
 * Enumeration of all supported commands in the expense tracker CLI application.
 * <p>
 * Each command represents a specific operation that users can perform through
 * the command line interface.
 * </p>
 */
public enum Command {
    /** Add a new expense to the tracker. */
    ADD("add", "Add a new expense"),
    /** Update an existing expense. */
    UPDATE("update", "Update an existing expense"),
    /** List all expense or filter by criteria. */
    LIST("list", "List expenses"),
    /** Show expense summary. */
    SUMMARY("summary", "Show expense summary"),
    /** Export expenses to CSV file. */
    EXPORT("export", "Export expenses to CSV"),
    /** Set monthly budget limits. */
    SET_BUGET("set-budget", "Set monthly budget limit"),
    /** Display help information */
    HELP("help", "Show help information");

    /** The command name as it appears in CLI arguments */
    private final String commandName;
    /** Human-readable description of the command */
    private final String description;

    /**
     * Constructs a Command with the specified name and description.
     *
     * @param commandName The name of the command as used in CLI
     * @param description A human-readable description of the command
     */
    Command(String commandName, String description) {
        this.commandName = commandName;
        this.description = description;
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
     * Finds a Command by its command name (case-insensitive).
     *
     * @param commandName The command name to search for
     * @return The matching Command, or null if not found
     */
    public static Command fromString(String commandName) {
        if (commandName == null) {
            return null;
        }

        for (Command command : Command.values()) {
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
