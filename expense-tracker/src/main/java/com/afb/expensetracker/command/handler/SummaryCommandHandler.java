package com.afb.expensetracker.command.handler;

import com.afb.expensetracker.command.ParsedCommand;
import com.afb.expensetracker.command.validation.*;

import org.apache.commons.cli.CommandLine;

/**
 * Handler for SUMMARY command operations.
 * <p>
 * Validates and processes arguments for generating expense summaries,
 * supporting optional month filtering for specific period summaries.
 * </p>
 */
public class SummaryCommandHandler implements CommandHandler {

    /** Validator for month arguments. */
    private final ArgumentValidator<Integer> monthValidator = new MonthValidator();

    @Override
    public void validateArguments(CommandLine cmd) throws ValidationException {
        if (cmd.hasOption("month")) {
            monthValidator.validate(cmd.getOptionValue("month"));
        }
    }

    @Override
    public void populateCommand(ParsedCommand parsedCommand, CommandLine cmd)
        throws ValidationException {

        if (cmd.hasOption("month")) {
            parsedCommand.setArgument("month", monthValidator.validate(cmd.getOptionValue("month")));
        }
    }
}
