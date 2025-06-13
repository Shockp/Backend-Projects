package model;

import java.time.LocalDateTime;
import java.util.Map;
/**
 * Represents a GitHub event as returned by the GitHub Events API.
 * This model maps to the JSON structure returned by the GitHub API endpoint:
 * https://api.github.com/users/{username}/events
 */
public class GitHubEvent {
    private String id;
    private String type;
    private Actor actor;
    private Repository repo;
    private Map<String, Object> payload;

    @SerializedName("public")
    private boolean isPublic;
}