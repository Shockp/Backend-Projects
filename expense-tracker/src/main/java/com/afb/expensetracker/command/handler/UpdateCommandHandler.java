package com.afb.expensetracker.command.handler;

import com.afb.expensetracker.command.ParsedCommand;
import com.afb.expensetracker.command.validation.*;
import com.afb.expensetracker.model.ExpenseCategory;

import java.math.BigDecimal;

import org.apache.commons.cli.CommandLine;

/**
 * Handler for UPDATE command operations.
 * <p>
 * Validates and processes arguments for updating existing expenses,
 * ensuring the ID is present and at least one field to update is provided.
 * </p>
 */
public class UpdateCommandHandler implements CommandHandler {
    /** Validator for ID arguments. */
    private final IdValidator idValidator = new IdValidator();
    /** Validator for amount arguments. */
    private final AmountValidator amountValidator = new AmountValidator();
    /** Validator for description arguments. */
    private final DescriptionValidator descriptionValidator = new DescriptionValidator();
    /** Validator for category arguments. */
    private final CategoryValidator categoryValidator = new CategoryValidator();

    @Override
    public void validateArguments(CommandLine cmd) throws ValidationException {
        if (!cmd.hasOption("id")) {
            throw new ValidationException("ID is required for update command." +
                    "Use --id 123");
        }

        idValidator.validate(cmd.getOptionValue("id"));

        if (!cmd.hasOption("description") && !cmd.hasOption("amount") && !cmd.hasOption("category")) {
            throw new ValidationException("At least one field to update must be provided " +
                    "(description, amount or category)");
        }

        if (cmd.hasOption("description")) {
            descriptionValidator.validate(cmd.getOptionValue("description"));
        }
        if (cmd.hasOption("amount")) {
            amountValidator.validate(cmd.getOptionValue("amount"));
        }
        if (cmd.hasOption("category")) {
            categoryValidator.validate(cmd.getOptionValue("category"));
        }
    }

    @Override
    public void populateCommand(ParsedCommand parsedCommand, CommandLine cmd)
        throws ValidationException {
        parsedCommand.setArgument("id",
                idValidator.validate(cmd.getOptionValue("id")));

        if (cmd.hasOption("description")) {
            parsedCommand.setArgument("description",
                    descriptionValidator.validate(cmd.getOptionValue("description")));
        }
        if (cmd.hasOption("amount")) {
            parsedCommand.setArgument("amount",
                    amountValidator.validate(cmd.getOptionValue("amount")));
        }
        if (cmd.hasOption("category")) {
            parsedCommand.setArgument("category",
                    categoryValidator.validate(cmd.getOptionValue("category")));
        }
    }
}
