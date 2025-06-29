package com.afb.expensetracker.command.handler;

import com.afb.expensetracker.command.ParsedCommand;
import com.afb.expensetracker.command.validation.*;

import org.apache.commons.cli.CommandLine;

/**
 * Handler for EXPORT command operations.
 * <p>
 * Validates and processes arguments for exporting expenses to CSV,
 * ensuring a file path is provided and supporting optional filtering
 * by category and month.
 * </p>
 */
public class ExportCommandHandler implements CommandHandler {

    /** Validator for category arguments. */
    private final ArgumentValidator<com.afb.expensetracker.model.ExpenseCategory> categoryValidator = new CategoryValidator();
    /** Validator for month arguments. */
    private final ArgumentValidator<Integer> monthValidator = new MonthValidator();

    @Override
    public void validateArguments(CommandLine cmd) throws ValidationException {
        if (!cmd.hasOption("file")) {
            throw new ValidationException("File path is required for export command." +
                    "Use --file \"expenses.csv\"");
        }

        String filePath = cmd.getOptionValue("file");
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new ValidationException("File path cannot be empty");
        }

        if (cmd.hasOption("category")) {
            categoryValidator.validate(cmd.getOptionValue("category"));
        }
        if (cmd.hasOption("month")) {
            monthValidator.validate(cmd.getOptionValue("month"));
        }
    }

    @Override
    public void populateCommand(ParsedCommand parsedCommand, CommandLine cmd)
        throws  ValidationException {

        parsedCommand.setArgument("file",
                cmd.getOptionValue("file").trim());

        if (cmd.hasOption("category")) {
            parsedCommand.setArgument("category", categoryValidator.validate(cmd.getOptionValue("category")));
        }
        if (cmd.hasOption("month")) {
            parsedCommand.setArgument("month", monthValidator.validate(cmd.getOptionValue("month")));
        }
    }
}
