package service;

import client.GitHubApiClient;
import client.GitHubApiException;

import model.GitHubEvent;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.Arrays;
import java.util.List;
import java.util.Collections;

/**
 * Service class that provides business logic for GitHub user
 * activity operations.
 */
public class GitHubApiService {
    private final GitHubApiClient apiClient;
    private final Gson gson;

    /**
     * Constructor that creates a GitHubApiService with default dependencies.
     * Uses default HTTP client and Gson configuration.
     */
    public GitHubApiService() {
        this.apiClient = new GitHubApiClient();
        this.gson = new Gson();
    }

    /**
     * Constructor for dependency injection, primarily used for testing.
     * Allow injection of custom HTTP client and Gson intances.
     *
     * @param apiClient Custom GitHubApiClient instance
     * @param gson Custom Gson instance for JSON processing
     */
    public GitHubApiService(GitHubApiClient apiClient, Gson gson) {
        this.apiClient = apiClient;
        this.gson = gson;
    }

    /**
     * Fetches and parses recent GitHub events for a specified user.
     * This method encapsulates the complete business logic for retrieving
     * user activity.
     *
     * @param username The GitHub username to fetch events for
     * @return List of GitHubEvent objects representing user's recent activity
     * @throws GitHubApiException if an error occurs during API request
     */
    public List<GitHubEvent> getUserActivity(String username) throws
            GitHubServiceException {
        if (username == null || username.trim().isEmpty()) {
            throw new GitHubServiceException("Username cannot be null or empty");
        }

        try {
            String jsonResponse = apiClient.getUserEvents(username);
            return parseEventsFromJson(jsonResponse);
        } catch (GitHubApiException e) {
            throw new GitHubServiceException("Failed to fetch user activity for: "
            + username, e);
        }
    }

    /**
     * Parses JSON response into a list of GitHubEvent objects.
     *
     * @param jsonResponse Raw JSON string from GitHub API
     * @return List of parsed GitHubEvent objects
     * @throws GitHubServiceException If JSON parsing fails
     */
    private List<GitHubEvent> parseEventsFromJson(String jsonResponse) throws
            GitHubServiceException {
        if (jsonResponse == null ||jsonResponse.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            GitHubEvent[] events = gson.fromJson(jsonResponse, GitHubEvent[].class);

            if (events == null || events.length == 0) {
                return Collections.emptyList();
            }

            return Arrays.asList(events);
        } catch (JsonSyntaxException e) {
            throw new GitHubServiceException("Failed to parse GitHub API" +
                    "response", e);
        }
    }

    /**
     * Filters events by type to show only specific kinds of GitHub activities.
     *
     * @param events List of all GitHub events.
     * @param eventType Specific type to filter by.
     * @return Filtered list containing only events of the specified type.
     */
    public List<GitHubEvent> filterEventsByType(List<GitHubEvent> events,
                                                String eventType) {
        if (events == null || eventType == null || eventType.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return events.stream()
                .filter(event -> eventType.equals(event.getType()))
                .toList();
    }

    public List<GitHubEvent> limitEvents(List<GitHubEvent> events, int limit) {
        if (events == null || limit <= 0) {
            return Collections.emptyList();
        }

        return events.stream()
                .limit(limit)
                .toList();
    }
}