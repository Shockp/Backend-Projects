package com.afb.expensetracker.command.handler;

import com.afb.expensetracker.command.ParsedCommand;
import com.afb.expensetracker.command.validation.*;

import org.apache.commons.cli.CommandLine;

/**
 * Handler for DELETE command operations.
 * <p>
 * Validates and processes arguments for deleting expenses,
 * ensuring the ID is present and valid.
 * </p>
 */
public class DeleteCommandHandler implements CommandHandler {
    /** Validator for ID arguments. */
    private final IdValidator idValidator = new IdValidator();

    @Override
    public void validateArguments(CommandLine cmd) throws ValidationException {
        if (!cmd.hasOption("id")) {
            throw new ValidationException("ID is required for delete command." +
                    "Use --id 123");
        }

        idValidator.validate(cmd.getOptionValue("id"));
    }

    @Override
    public void populateCommand(ParsedCommand parsedCommand, CommandLine cmd)
        throws ValidationException {

        parsedCommand.setArgument("id",
                idValidator.validate(cmd.getOptionValue("id")));
    }
}
