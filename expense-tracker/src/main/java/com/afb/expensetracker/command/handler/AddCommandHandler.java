package com.afb.expensetracker.command.handler;

import com.afb.expensetracker.command.ParsedCommand;
import com.afb.expensetracker.command.validation.*;
import com.afb.expensetracker.model.ExpenseCategory;

import java.math.BigDecimal;

import org.apache.commons.cli.CommandLine;

/**
 * Handler for ADD command operations.
 * <p>
 * Validates and processes arguments for adding new expenses,
 * ensuring required fields are present and valid.
 * </p>
 */
public class AddCommandHandler implements CommandHandler {

    /** Validator for amount arguments */
    private final AmountValidator amountValidator = new AmountValidator();
    /** Validator for description arguments */
    private final DescriptionValidator descriptionValidator = new DescriptionValidator();
    /** Validator for category arguments */
    private final CategoryValidator categoryValidator = new CategoryValidator();

    @Override
    public void validateArguments(CommandLine cmd) throws ValidationException {
        if (!cmd.hasOption("description")) {
            throw new ValidationException("Description is required for add command." +
                    "Use --description \"your description\"");
        }
        if (!cmd.hasOption("amount")) {
            throw new ValidationException("Amount is required for add command." +
                    "Use --amount \"123.45\"");
        }

        descriptionValidator.validate(cmd.getOptionValue("description"));
        amountValidator.validate(cmd.getOptionValue("amount"));

        if (cmd.hasOption("category")) {
            categoryValidator.validate(cmd.getOptionValue("category"));
        }
    }

    @Override
    public void populateCommand(ParsedCommand parsedCommand, CommandLine cmd)
        throws ValidationException {
        parsedCommand.setArgument("description",
                descriptionValidator.validate(cmd.getOptionValue("description")));
        parsedCommand.setArgument("amount",
                amountValidator.validate(cmd.getOptionValue("amount")));

        ExpenseCategory category = ExpenseCategory.MISCELLANEOUS;
        if (cmd.hasOption("category")) {
            category = categoryValidator.validate(cmd.getOptionValue("category"));
        }
        parsedCommand.setArgument("category", category);
    }
}
