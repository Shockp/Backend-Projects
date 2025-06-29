package com.afb.expensetracker.command;

import com.afb.expensetracker.command.handler.CommandHandlerFactory;
import com.afb.expensetracker.command.handler.CommandHandler;
import com.afb.expensetracker.command.validation.ValidationException;

import org.apache.commons.cli.*;

import java.util.Arrays;

/**
 * Parses command line arguments and creates structured command objects.
 * <p>
 * This class uses Apache Commons CLI to parse command line arguments and
 * delegates command-specific validation and processing to appropriate
 * command handlers. It follows the Chain of Responsibility pattern for
 * command processing.
 * </p>
 */
public class CommandParser {

    /** Apache Commons CLI Options object for defining supported options. */
    private final Options options;
    /** Apache Commons CLI CommandLineParser for parsing arguments. */
    private final CommandLineParser parser;
    /** Factory for creating command handlers. */
    private final CommandHandlerFactory handlerFactory;

    /**
     * Constructs a new CommandParser and initializes supported CLI options.
     */
    public CommandParser() {
        this.options = new Options();
        this.parser = new DefaultParser();
        this.handlerFactory = new CommandHandlerFactory();
    }

    /**
     * Creates and configures the CLI options for all supported commands.
     *
     * @return Configured Options object
     */
    private Options createOptions() {
        Options opts = new Options();

        opts.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Show help information")
                .build());

        opts.addOption(Option.builder("d")
                .longOpt("description")
                .hasArg()
                .argName("description")
                .desc("Expense description")
                .build());

        opts.addOption(Option.builder("a")
                .longOpt("amount")
                .hasArg()
                .argName("amount")
                .desc("Expense amount")
                .build());

        opts.addOption(Option.builder("c")
                .longOpt("category")
                .hasArg()
                .argName("category")
                .desc("Expense category")
                .build());

        opts.addOption(Option.builder("i")
                .longOpt("id")
                .hasArg()
                .argName("id")
                .desc("Expense ID")
                .build());

        opts.addOption(Option.builder("m")
                .longOpt("month")
                .hasArg()
                .argName("month")
                .desc("Month (1-12)")
                .build());

        opts.addOption(Option.builder("f")
                .longOpt("file")
                .hasArg()
                .argName("filepath")
                .desc("Export file path")
                .build());

        opts.addOption(Option.builder("b")
                .longOpt("budget")
                .hasArg()
                .argName("amount")
                .desc("Budget amount")
                .build());

        return opts;
    }

    /**
     * Parses command line arguments and creates a ParsedCommand object.
     *
     * @param args The command line arguments
     * @return A ParsedCommand containing the parsed information
     * @throws ParseException If the arguments cannot be parsed
     * @throws ValidationException If required arguments are missing or invalid
     */
    public ParsedCommand parseCommand(String[] args)
        throws ParseException, ValidationException {

        if (args.length == 0) {
            throw new IllegalArgumentException("No command specified." +
                    "Use --help for usage information.");
        }

        // First argument should be the command
        String commandName = args[0];
        Command command = Command.fromString(commandName);

        if (command == null) {
            throw new IllegalArgumentException("Unknown command: " + commandName
                    + ". Use --help for available commands.");
        }

        // Parse the remaining arguments
        String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);
        CommandLine cmd = parser.parse(options, commandArgs);

        ParsedCommand parsedCommand = new ParsedCommand(command);

        // Handle help option
        if (cmd.hasOption("help")) {
            parsedCommand.setShowHelp(true);
            return parsedCommand;
        }

        // Handle HELP command
        if (command == Command.HELP) {
            parsedCommand.setShowHelp(true);
            return parsedCommand;
        }

        // Delegate to specific command handler
        if (handlerFactory.hasHandler(command)) {
            CommandHandler handler = handlerFactory.getHandler(command);
            handler.validateArguments(cmd);
            handler.populateCommand(parsedCommand, cmd);
        }

        return parsedCommand;
    }

    /**
     * Prints help information for all commands
     */
    public void printHelp() {
        System.out.println("Expense Tracker CLI - Personal Finance Management Tool");
        System.out.println();
        System.out.println("Usage: expense-tracker <command> [options]");
        System.out.println();
        System.out.println("Available Commands:");

        for (Command cmd : Command.values()) {
            System.out.printf("  %-12s %s%n", cmd.getCommandName(), cmd.getDescription());
        }

        System.out.println();
        System.out.println("Common Options:");
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("expense-tracker [options]", options);

        System.out.println();
        System.out.println("Examples:");
        System.out.println("  expense-tracker add --description \"Lunch\" --amount 12.50 --category FOOD");
        System.out.println("  expense-tracker list --month 6");
        System.out.println("  expense-tracker summary --month 6");
        System.out.println("  expense-tracker update --id 1 --amount 15.00");
        System.out.println("  expense-tracker delete --id 1");
        System.out.println("  expense-tracker export --file expenses.csv");
        System.out.println("  expense-tracker set-budget --month 6 --budget 1500.00");
    }
}
