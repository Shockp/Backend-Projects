import java.time.LocalDateTime;

/**
 * Represents a task with core properties and automatic timestamp management.
 * Encapsulates task state and provides controlled modification methods.
 */
public class Task {
    private int id;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Constructs a new Task with initial state
     * @param id Unique identifier (should be sequentially generated)
     * @param description Task description (minimum 3 characters)
     */
    public Task(int id, String description) {
        this.id = id;
        this.description = description;
        this.status = "to-do";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters
    public int getId() { return id; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters

    /**
     * Updates task description and refreshes modification timestamp
     * @param description New task description (cannot be empty)
     */
    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Changes task state and updates modification timestamp
     * @param status New state (must be valid status)
     */
    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Provides formatted string representation for debugging
     * @return Detailed task information string
     */
    @Override
    public String toString() {
        return String.format("Task{id=%d, description='%s', status='%s', createdAt=%s, updatedAd=%s}",
                                id, description, status, createdAt, updatedAt);
    }
}
