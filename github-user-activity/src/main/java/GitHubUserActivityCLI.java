import command.Command;
import command.GitHubActivityCommand;

import java.util.HashMap;
import java.util.Map;

/**
 * Main CLI class that serves as the entry point for the GitHub User
 * Activity CLI.
 * Implements command pattern by coordinating between user input and
 * command execution.
 */
public class GitHubUserActivityCLI {
    private final Map<String, Command> commands;

    /**
     * Constructor that initializes the CLI with available commands.
     * Sets up the command registry for dynamic command execution.
     */
    public GitHubUserActivityCLI() {
        this.commands = new HashMap<>();
        registerCommand(new GitHubActivityCommand());
    }

    /**
     * Registers a command in the command registry.
     * Enables dynamic command lookup and execution.
     *
     * @param command Command instance to register
     */
    private void registerCommand(Command command) {
        commands.put(command.getCommandName(), command);
    }

    /**
     * Main entry point for the GitHub User Activity CLI application.
     * Parses command-line arguments and executes appropriate commands.
     *
     * @param args Command-line arguments passed to the application
     */
    public static void main(String[] args) {
        GitHubUserActivityCLI cli = new GitHubUserActivityCLI();
        cli.run(args);
    }

    /**
     * Runs the CLI application with provided arguments.
     * Handles argument parsing, command lookup and execution coordination.
     *
     * @param args Command-line arguments to process
     */
    public void run(String[] args) {
        if (args.length == 0) {
            displayUsage();
            return;
        }

        String commandName = args[0];

        if ("help".equals(commandName) || "--help".equals(commandName) ||
        "-h".equals(commandName)) {
            displayHelp();
            return;
        }

        try {
            String[] commandArgs = new String[args.length];
            System.arraycopy(args, 0, commandArgs, 0, args.length);

            GitHubActivityCommand command = new GitHubActivityCommand();
            command.execute(commandArgs);
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: "
                    + e.getMessage());
            System.err.println("Use 'help' or '--help' for usage information.");
        }
    }

    /**
     * Displays basic usage information for the CLI.
     * Shows how to invoke the application with proper arguments.
     */
    private void displayUsage() {
        System.out.println("GitHub User Activity CLI");
        System.out.println("Usage: github-activity <username>");
        System.out.println("       github-activity help");
        System.out.println();
        System.out.println("For detailed help, use: github-activity help");
    }

    /**
     * Displays comprehensive help information.
     * Provides detailed usage instructions and examples.
     */
    private void displayHelp() {
        System.out.println("GitHub User Activity CLI");
        System.out.println("========================");
        System.out.println();
        System.out.println("This tool fetches and displays recent activity for a GitHub user.");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  github-activity <username>    Fetch activity for specified user");
        System.out.println("  github-activity help          Show this help message");
        System.out.println("  github-activity --help        Show this help message");
        System.out.println("  github-activity -h            Show this help message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  github-activity octocat       Fetch activity for user 'octocat'");
        System.out.println("  github-activity defunkt       Fetch activity for user 'defunkt'");
        System.out.println();
        System.out.println("The tool will display recent GitHub events including:");
        System.out.println("  - Push events (commits)");
        System.out.println("  - Repository creation");
        System.out.println("  - Stars and forks");
        System.out.println("  - Issues and pull requests");
        System.out.println("  - And other GitHub activities");
    }
}