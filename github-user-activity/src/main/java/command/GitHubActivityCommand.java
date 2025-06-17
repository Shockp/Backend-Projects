package command;

import service.GitHubApiService;
import service.GitHubServiceException;

import model.GitHubEvent;

import util.OutPutFormatter;

import java.util.List;

/**
 * Concrete command implementation for fetching GitHub user activity.
 * Implements the Command interface to handle GitHub user activity requests.
 * This class coordinates between the CLI interface and service layer.
 */
public class GitHubActivityCommand implements Command {
    private final GitHubApiService apiService;
    private final OutPutFormatter outputFormatter;

    /**
     * Constructor that creates GitHubActivityCommand with default dependencies.
     * Uses default service and formatter implementations.
     */
    public GitHubActivityCommand() {
        this.apiService = new GitHubApiService();
        this.outputFormatter = new OutPutFormatter();
    }

    /**
     * Constructor for dependency injection, primarily used for testing.
     * Allows injection of custom service and formatter implementations.
     *
     * @param apiService Custom GitHubApiService instance
     * @param outputFormatter Custom OutPutFormatter instance
     */
    public GitHubActivityCommand(GitHubApiService apiService, OutPutFormatter outputFormatter) {
        this.apiService = apiService;
        this.outputFormatter = outputFormatter;
    }

    /**
     * Executes the GitHub user activity command with provided arguments.
     * Validates input, fetches user activity and displays formatted results.
     *
     * @param args Command-line arguments where the first argument should be username
     * @throws Exception If there's an error during command execution
     */
    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            System.err.println("Error: username is required");
            System.err.println(getUsageInfo());
            return;
        }

        String username = args[0];

        if (username.trim().isEmpty()) {
            System.err.println("Error: Username cannot be empty");
            System.err.println(getUsageInfo());
            return;
        }

        try {
            System.out.println("Fetching GitHub activity for user: "
                    + username);

            List<GitHubEvent> events = apiService.getUserActivity(username);

            if (events.isEmpty()) {
                System.out.println("No recent activity found for user: "
                        + username);
                return;
            }

            System.out.println("Found " + events.size() + " recent events:");
            System.out.println();

            outputFormatter.displayEvents(events);
        } catch (GitHubServiceException e) {
            System.err.println("Error fetching GitHub activity: "
                    + e.getMessage());

            if (e.getMessage().contains("User not found")) {
                System.err.println("Please verify the username is correct and" +
                        " the user exists on GitHub");
            } else if (e.getMessage().contains("rate limit")) {
                System.err.println("GitHub API rate limit exceeded. Please try" +
                        " again later.");
            }
        }
    }

    /**
     * Returns the command name for identification purposes.
     *
     * @return String Representing the command name
     */
    @Override
    public String getCommandName() {
        return "github-activity";
    }

    /**
     * Returns usage information for the command.
     *
     * @return String Containing detailed usage instructions
     */
    @Override
    public String getUsageInfo() {
        return """
                Usage: github-activity <username>
                Fetches and displays recent GitHub activity for the\
                 specified user.
                
                Arguments:
                  username    GitHub username to fetch activity for
                
                Example:
                  github-activity octocat""";
    }
}