import java.time.LocalDateTime;

public class Task {
    private int id;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor for new tasks
    public Task(int id, String description) {
        this.id = id;
        this.description = description;
        this.status = "to-do";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and setters
    public int getId() { return id; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("Task{id=%d, description='%s', status='%s', createdAt=%s, updatedAd=%s}",
                                    id, description, status, createdAt, updatedAt);
    }
}
