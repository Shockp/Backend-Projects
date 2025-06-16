package command;

/**
 * Base interface for all commands in the GitHub User Activity CLI.
 * Follows the Command Pattern by encapsulating requests as objects.
 * This interface defines the contract that all commands must implement.
 */
public interface Command {
    /**
     * Executes the command with provided arguments.
     *
     * @param args Command-line arguments
     * @throws Exception If there's an error executing the command
     */
    void execute(String[] args) throws Exception;

    /**
     * Returns the command name for identification and help purposes.
     * Used by the command invoker to match user input to commands.
     *
     * @return String representing the command name
     */
    String getCommandName();

    /**
     * Returns usage information for the command.
     * Provides help text how to use the command properly.
     *
     * @return String containing usage instructions
     */
    String getUsageInfo();
}