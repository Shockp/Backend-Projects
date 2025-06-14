package model;

import com.google.gson.annotations.SerializedName;

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

    @SerializedName("created_at")
    private String createdAt;

    // Default constructor for Gson
    public GitHubEvent() {}

    // Getters and setters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public Actor getActor() {
        return actor;
    }
    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public Repository getRepo() {
        return repo;
    }
    public void setRepo(Repository repo) {
        this.repo = repo;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }
    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public boolean isPublic() {
        return isPublic;
    }
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Nested class representing the actor (user) who triggered the event
     */
    public static class Actor {
        private long id;
        private String login;

        @SerializedName("display_login")
        private String displayLogin;

        @SerializedName("gravatar_id")
        private String gravatarId;

        private String url;

        @SerializedName("avatar_url")
        private String avatarUrl;

        // Default constructor
        public Actor() {}

        // Getters and setters
        public long getId() {
            return id;
        }
        public void setId(long id) {
            this.id = id;
        }

        public String getLogin() {
            return login;
        }
        public void setLogin(String login) {
            this.login = login;
        }

        public String getDisplayLogin() {
            return displayLogin;
        }
        public void setDisplayLogin(String displayLogin) {
            this.displayLogin = displayLogin;
        }

        public String getGravatarId() {
            return gravatarId;
        }
        public void setGravatarId(String gravatarId) {
            this.gravatarId = gravatarId;
        }

        public String getUrl() {
            return url;
        }
        public void setUrl(String url) {
            this.url = url;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }
        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
    }

    /**
     * Nested class representing the repository where the event occurred
     */
    public static class Repository {
        private long id;
        private String name;
        private String url;

        // Default constructor
        public Repository() {}

        // Getters and Setters
        public long getId() {
            return id;
        }
        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }
        public void setUrl(String url) {
            this.url = url;
        }
    }

    @Override
    public String toString() {
        return "GitHubEvent{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", actor=" + actor +
                ", repo=" + repo +
                ", payload=" + payload +
                ", isPublic=" + isPublic +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}