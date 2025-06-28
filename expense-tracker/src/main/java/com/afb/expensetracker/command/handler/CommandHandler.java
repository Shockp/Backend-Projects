package com.afb.expensetracker.command.handler;

import com.afb.expensetracker.command.ParsedCommand;
import com.afb.expensetracker.command.validation.ValidationException;

import org.apache.commons.cli.*;

/**
 * Interface for handling specific command types.
 * <p>
 * Each command type (ADD, UPDATE, DELETE, etc.) has its own handler
 * implementation that knows how to validate and populate command data.
 * </p>
 */
public interface CommandHandler {

    /**
     * Validates the arguments for this command type.
     *
     * @param cmd The parsed command line arguments
     * @throws ValidationException If required arguments are missing or invalid
     */
    void validateArguments(CommandLine cmd) throws ValidationException;

    /**
     * Populates the ParsedCommand with validated argument values.
     *
     * @param parsedCommand The command to populate
     * @param cmd The parsed command line arguments
     * @throws ValidationException If argument parsing fails
     */
    void populateCommand(ParsedCommand parsedCommand, CommandLine cmd)
        throws ValidationException;
}
