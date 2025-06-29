package com.afb.expensetracker.command.handler;

import com.afb.expensetracker.command.ParsedCommand;
import com.afb.expensetracker.command.validation.*;

import org.apache.commons.cli.CommandLine;

/**
 * Handler for LIST command operations.
 * <p>
 * Validates and processes arguments for listing expenses,
 * supporting optional filtering by category and month.
 * </p>
 */
public class ListCommandHandler implements CommandHandler {
    /** Validator for category arguments */
    private final CategoryValidator categoryValidator = new CategoryValidator();
    /** Validator for month arguments */
    private final MonthValidator  monthValidator = new MonthValidator();

    @Override
    public void validateArguments(CommandLine cmd) throws ValidationException {
        if (cmd.hasOption("category")) {
            categoryValidator.validate(cmd.getOptionValue("category"));
        }
        if (cmd.hasOption("month")) {
            monthValidator.validate(cmd.getOptionValue("month"));
        }
    }

    @Override
    public void populateCommand(ParsedCommand parsedCommand, CommandLine cmd)
        throws ValidationException {

        if (cmd.hasOption("category")) {
            parsedCommand.setArgument("category",
                    categoryValidator.validate(cmd.getOptionValue("category")));
        }
        if (cmd.hasOption("month")) {
            parsedCommand.setArgument("month",
                    monthValidator.validate(cmd.getOptionValue("month")));
        }
    }
}
