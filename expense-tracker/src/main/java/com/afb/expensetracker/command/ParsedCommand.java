package com.afb.expensetracker.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a parsed command with all its arguments and options.
 * <p>
 * This class holds the structured representation of a command line input
 * after it has been parsed and validated by the CommandParser. It uses
 * a generic approach to store arguments of different types.
 * </p>
 */
public class ParsedCommand {

    /** The command to execute */
    private final Command command;
    /** Map storing all parsed arguments by name. */
    private final Map<String, Object> arguments;
    /** Whether to show help information. */
    private boolean showHelp = false;

    /**
     * Constructs a ParsedCommand with the specified command.
     *
     * @param command The command to execute
     */
    public ParsedCommand(Command command) {
        this.command = command;
        this.arguments = new HashMap<>();
    }

    /**
     * Returns the command to execute.
     *
     * @return The command
     */
    public Command getCommand() {
        return command;
    }

    /**
     * Returns whether help should be shown.
     *
     * @return true if help should be shown
     */
    public boolean isShowHelp() {
        return showHelp;
    }

    /**
     * Sets whether help should be shown.
     *
     * @param showHelp true to show help
     */
    public void setShowHelp(boolean showHelp) {
        this.showHelp = showHelp;
    }

    /**
     * Gets an argument value with type safety.
     *
     * @param key The argument name
     * @param type The expected type of the argument
     * @param <T> The type parameter
     * @return The argument value cast to the specified type
     * @throws ClassCastException If the argument cannot be cast to the specified type
     */
    @SuppressWarnings("unchecked")
    public <T> T getArgument(String key, Class<T> type) {
        Object value = arguments.get(key);
        if (value == null) {
            return null;
        }
        return (T) value;
    }

    /**
     * Sets an argument value.
     *
     * @param key The argument name
     * @param value the argument value
     */
    public void setArgument(String key, Object value) {
        arguments.put(key, value);
    }

    /**
     * Checks if an argument is present.
     *
     * @param key The argument name
     * @return true if the argument is present
     */
    public boolean hasArgument(String key) {
        return arguments.containsKey(key);
    }

    /**
     * Returns all argument names.
     *
     * @return Set of argument names
     */
    public java.util.Set<String> getArgumentNames() {
        return arguments.keySet();
    }
}
