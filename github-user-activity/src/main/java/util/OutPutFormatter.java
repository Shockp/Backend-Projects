package util;

import model.GitHubEvent;

import java.util.List;
import java.util.Map;

/**
 * Utility class for formatting and displaying GitHub events.
 * Provides consistent output formatting across the application.
 * Handles different events types with appropriate display formats.
 */
public class OutPutFormatter {

    public void displayEvents(List<GitHubEvent> events) {
        if (events == null || events.isEmpty()) {
            System.out.println("No events to display.");
            return;
        }

        for (int i = 0; i < events.size(); i++) {
            GitHubEvent event = events.get(i);

            System.out.println((i + 1) + ". " + formatEventDescription(event));
            System.out.println("    Time: " + formatEventTimestamp(event.getCreatedAt()));

            if (i < events.size() - 1) {
                System.out.println();
            }
        }
    }

    /**
     * Formats a timestamp in ISO 8601 format.
     *
     * @param timestamp ISO 8601 formatted timestamp
     * @return Formatted timestamp for display.
     */
    private String formatEventTimestamp(String timestamp) {
        return (timestamp != null) ? timestamp : "Unknown time";
    }

    /**
     * Formats a single GitHub event into a human-readable description.
     * Handles different event types by calling the appropriate formatting method.
     *
     * @param event GitHubEvent to format
     * @return Formatted string description of the event
     */
    private String formatEventDescription(GitHubEvent event) {
        String eventType = event.getType();
        String actorName = (event.getActor() != null) ?
                event.getActor().getLogin() : "Unknown";
        String repoName = (event.getRepo() != null) ?
                event.getRepo().getName() : "Unknown";

        // Format description based on an event type
        return switch (eventType) {
            case "PushEvent" -> formatPushEvent(actorName, repoName,
                    event.getPayload());
            case "CreateEvent" -> formatCreateEvent(actorName, repoName,
                    event.getPayload());
            case "WatchEvent" -> formatWatchEvent(actorName, repoName);
            case "ForkEvent" -> formatForkEvent(actorName, repoName);
            case "IssuesEvent" -> formatIssuesEvent(actorName, repoName,
                    event.getPayload());
            case "PullRequestEvent" -> formatPullRequestEvent(actorName, repoName,
                    event.getPayload());
            default -> formatGenericEvent(actorName, repoName, eventType);
        };
    }

    /**
     * Formats a push event with commit information.
     */
    private String formatPushEvent(String actorName, String repoName,
                                   Map<String, Object> payload) {
        Object sizeObj = payload.get("size");
        String commitCount = (sizeObj != null) ? sizeObj.toString() : "some";

        return String.format("%s pushed %s commit(s) to %s", actorName,
                commitCount, repoName);
    }

    /**
     * Formats a create event (repository, branch or tag creation).
     */
    private String formatCreateEvent(String actorName, String repoName,
                                     Map<String, Object> payload) {
        Object refTypeObj = payload.get("ref_type");
        String refType = (refTypeObj != null) ? refTypeObj.toString() :
                "repository";

        return String.format("%s created %s %s", actorName, refType, repoName);
    }

    /**
     * Formats a watch event (starring a repository).
     */
    private String formatWatchEvent(String actorName, String repoName) {
        return String.format("%s starred %s", actorName, repoName);
    }

    /**
     * Formats a fork event.
     */
    private String formatForkEvent(String actorName, String repoName) {
        return String.format("%s forked %s", actorName, repoName);
    }

    /**
     * Formats an issues event (opened, closed, etc.)
     */
    private String formatIssuesEvent(String actorName, String repoName,
                                     Map<String, Object> payload) {
        Object actionObj = payload.get("action");
        String action = (actionObj != null) ? actionObj.toString() : "modified";

        return String.format("%s %s an issue in %s", actorName, action,
                repoName);
    }

    /**
     * Formats a pull request event
     */
    private String formatPullRequestEvent(String actorName, String repoName,
                                          Map<String, Object> payload) {
        Object actionObj = payload.get("action");
        String action = (actionObj != null) ? actionObj.toString() : "modified";

        return String.format("%s %s a pull request in %s", actorName, action,
                repoName);
    }

    /**
     * Formats generic events that don't have specific formatting logic.
     */
    private String formatGenericEvent(String actorName, String repoName,
                                      String eventType) {
        String action = eventType.replace("Event", "").toLowerCase();
        return String.format("%s performed %s action on %s", actorName,
                action, repoName);
    }
}