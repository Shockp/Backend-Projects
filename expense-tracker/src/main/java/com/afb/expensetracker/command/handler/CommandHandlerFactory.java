package com.afb.expensetracker.command.handler;

import com.afb.expensetracker.command.Command;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Factory for creating appropriate command handlers.
 * <p>
 * This factory uses the Factory pattern to create and return the correct
 * command handler implementation based on the command type. It maintains
 * a complete registry of handlers for all supported commands.
 * </p>
 */
public class CommandHandlerFactory {

    /** Registry of command handlers mapped by command type. */
    private final Map<Command, CommandHandler> handlers;

    /**
     * Constructs a new CommandHandlerFactory and initializes the handler registry.
     */
    public CommandHandlerFactory() {
        this.handlers = new HashMap<>();
        initializeHandlers();
    }

    /**
     * Initializes the handler registry with all supported command handlers.
     */
    private void initializeHandlers() {
        handlers.put(Command.ADD, new AddCommandHandler());
        handlers.put(Command.UPDATE, new UpdateCommandHandler());
        handlers.put(Command.DELETE, new DeleteCommandHandler());
        handlers.put(Command.LIST, new ListCommandHandler());
        handlers.put(Command.SUMMARY, new SummaryCommandHandler());
        handlers.put(Command.EXPORT, new ExportCommandHandler());
        handlers.put(Command.SET_BUDGET, new SetBudgetCommandHandler());
        handlers.put(Command.HELP, new HelpCommandHandler());
    }

    /**
     * Returns the appropriate command handler for the given command.
     *
     * @param command The command to get a handler for
     * @return The command handler implementation
     * @throws IllegalArgumentException If no handler is found for the command
     */
    public CommandHandler getHandler(Command command) {
        CommandHandler handler = handlers.get(command);
        if (handler == null) {
            throw new IllegalArgumentException("No handle found for command: "
                    + command);
        }
        return handler;
    }

    /**
     * Checks if a handler is available for the given command.
     *
     * @param command The command to check
     * @return true if a handler is available, false otherwise
     */
    public boolean hasHandler(Command command) {
        return handlers.containsKey(command);
    }

    /**
     * Returns all supported commands.
     *
     * @return Set of all commands that have handlers
     */
    public Set<Command> getSupportedCommands() {
        return handlers.keySet();
    }
}
