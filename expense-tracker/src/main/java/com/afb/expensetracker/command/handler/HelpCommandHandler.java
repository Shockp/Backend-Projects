package com.afb.expensetracker.command.handler;

import com.afb.expensetracker.command.ParsedCommand;
import com.afb.expensetracker.command.validation.*;

import org.apache.commons.cli.CommandLine;

/**
 * Handler for HELP command operations.
 * <p>
 * Processes help command requests. This handler is simple as the help
 * command doesn't require any arguments or validation - it simply
 * triggers the display of help information.
 * </p>
 */
public class HelpCommandHandler implements CommandHandler {

    @Override
    public void validateArguments(CommandLine cmd) throws ValidationException {
        // HELP command has no arguments to validate
        // All arguments are ignored for help command
    }

    @Override
    public void populateCommand(ParsedCommand parsedCommand, CommandLine cmd)
        throws ValidationException {

        parsedCommand.setShowHelp(true);

        if (cmd.getArgs().length > 0) {
            String specificCommand = cmd.getArgs()[0];
            parsedCommand.setArgument("specificCommand", specificCommand);
        }
    }
}
