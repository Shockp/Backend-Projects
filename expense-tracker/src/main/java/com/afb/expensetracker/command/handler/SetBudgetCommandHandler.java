package com.afb.expensetracker.command.handler;

import com.afb.expensetracker.command.ParsedCommand;
import com.afb.expensetracker.command.validation.*;

import org.apache.commons.cli.CommandLine;

/**
 * Handler for SET_BUDGET command operations.
 * <p>
 * Validates and processes arguments for setting monthly budget limits,
 * ensuring both month and budget amount are provided and valid.
 * </p>
 */
public class SetBudgetCommandHandler implements CommandHandler {

    /** Validator for month arguments. */
    private final ArgumentValidator<Integer> monthValidator = new MonthValidator();
    /** Validator for budget amount arguments. */
    private final ArgumentValidator<java.math.BigDecimal> amountValidator = new AmountValidator();

    @Override
    public void validateArguments(CommandLine cmd) throws ValidationException {
        if (!cmd.hasOption("month")) {
            throw new ValidationException("Month is required for set-budget command. " +
                    "Use --month 1");
        }
        if (!cmd.hasOption("budget")) {
            throw new ValidationException("Budget amount is required for set-budget command." +
                    " Use --budget 1000.00");
        }

        monthValidator.validate(cmd.getOptionValue("month"));
        amountValidator.validate(cmd.getOptionValue("budget"));
    }

    @Override
    public void populateCommand(ParsedCommand parsedCommand, CommandLine cmd)
        throws ValidationException {

        parsedCommand.setArgument("month",
                monthValidator.validate(cmd.getOptionValue("month")));
        parsedCommand.setArgument("budgetAmount",
                amountValidator.validate(cmd.getOptionValue("budget")));
    }
}
