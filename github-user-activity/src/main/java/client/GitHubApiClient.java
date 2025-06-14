package client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * HTTP client for communicating with the GitHub API.
 * Handles making requests to GitHub's REST API endpoints.
 */
public class GitHubApiClient {
    private static final String GITHUB_API_BASE_URL = "https://api.github.com";
    private static final String USER_EVENTS_ENDPOINT = "/users/%s/events";
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;

    private final HttpClient httpClient;

    /**
     * Default constructor that creates a GitHubApiClient with
     * optimal configuration.
     * Uses HTTP/2 for better performance and set reasonable timeout values.
     */
    public GitHubApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
                .build();
    }

    /**
     * Alternative constructor accepting custom HttpClient for
     * dependency injection.
     *
     * @param httpClient Custom HttpClient instance to use for requests.
     */
    public GitHubApiClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Fetches recent activity events for a specified GitHub username.
     * Makes HTTP GET request to GitHub's events API endpoint.
     *
     * @param username GitHub username to fetch events for.
     * @return JSON response as String containing the user's recent event.
     * @throws GitHubApiException if an error occurs during the request.
      */
    public String getUserEvents(String username) throws GitHubApiException{
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty.");
        }

        String endpoint = String.format(USER_EVENTS_ENDPOINT, username);
        String fullUrl = GITHUB_API_BASE_URL + endpoint;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "GitHub-User-Activity-CLI/1.0")
                .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            return handleResponse(response, username);
        } catch (IOException e) {
            throw new GitHubApiException("Network error while fetching user " +
                    "events for: " + username, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GitHubApiException("Request was interrupted while " +
                    "fetching user events for: " + username, e);
        }
    }

    /**
     * Processes HTTP response and handles different status codes appropriately.
     *
     * @param response The HTTP response from GitHub API.
     * @param username The username being queried.
     * @return The JSON response body as a String for successful requests.
     * @throws GitHubApiException if the response indicates an error condition.
     */
    private String handleResponse(HttpResponse<String> response, String username)
        throws GitHubApiException {
        int statusCode = response.statusCode();

        switch (statusCode) {
            case 200:
                return response.body();
            case 404:
                throw new GitHubApiException("User not found: " + username);
            case 403:
                throw new GitHubApiException("Access forbidden");
            case 500:
            case 502:
            case 503:
            case 504:
                throw new GitHubApiException("GitHub API server error (status code: " +
                        statusCode + ")");
            default:
                throw new GitHubApiException("Unexpected HTTP status code: " +
                        statusCode);
        }
    }
}