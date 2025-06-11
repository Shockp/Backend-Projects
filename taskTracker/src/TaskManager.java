import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages task lifecycle operations and JSON persistence.
 * Handles all CRUD operations and maintans data integrity.
 */
public class TaskManager {
    private List<Task> tasks = new ArrayList<>();
    private final String DATA_FILE = "tasks.json";
    private int nextId = 1;
    private final Gson gson;

    /**
     * Initializes taskManager with JSON configuration.
     * Automatically loads tasks from JSON file if it exists.
     */
    public TaskManager() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
        loadTasks();
    }

    /**
     * Adds new task to the system.
     * @param description Task description (minimum 3 characters)
     */
    public void addTask(String description) {
        Task newTask = new Task(nextId++, description);
        tasks.add(newTask);
        saveTasks();
        System.out.println("Added Task ID %d%n", newTask.getId());
    }

    /**
     * Updates existing task description.
     * @param id Target task ID.
     * @param newDescription Updated task description (minimum 3 characters)
     */
    public void updateTask(int id, String newDescription) {
        findTaskById(id).ifPresentOrElse(
                task -> {
                    task.setDescription(newDescription);
                    saveTasks();
                    System.out.println("Updated Task ID %d%n", id);
                },
                () -> System.out.println("Task not found with ID %d%n", id)
        );
    }

    /**
     * Removes task from the system.
     * @param id Target task ID.
     */
    public void deleteTask(int id) {
        if(task.removeIf(task -> task.getId() == id)) {
            saveTasks();
            System.out.println("Deleted Task ID %d%n", id);
        } else {
            System.out.println("Task not found with ID %d%n", id);
        }
    }

    /**
     * Changes task status to in-progress.
     * @param id Target task ID
     */
    public void markInProgress(int id) {
        updateStatus(id, "in-progress");
    }

    /**
     * Changes task status to done.
     * @param id Target task ID
     */
    public void markDone(int id) {
        updateStatus(id, "done");
    }

    /**
     * Displays tasks with optional status filtering
     * @param statusFilter Optional status filter (null for all)
     */
    public listTasks(String statusFiler) {
        List<Task> filtered = tasks.stream()
                .filter(t -> statusFilter == null || t.getStatus().equals(statusFilter))
                .collect(Collectors.toList());

        if(filtered.isEmpty()) {
            System.out.println("No tasks found%n" + (statusFilter == null ? " with status: " + statusFiler : ""));
        } else {
            filtered.forEach(System.out::println);
        }
    }

    // Internal helper methods
    private Optional<Task> findTaskById(int id) {
        return tasks.stream()
                .filter(t -> t.getId() == id)
                .findFirst();
    }

    private void UpdateStatus(int id, String status) {
        findTaskById(id).ifPresentOrElse(
                task -> {
                    task.setStatus(status);
                    saveTasks();
                    System.out.println("Updated Task ID %d status to %s%n", id, status);
                },
                () -> System.out.println("Task not found with ID %d%n", id)
        );
    }

    /**
     * Loads tasks from JSON storage.
     * Handles file errors gracefully.
     */
    private void loadTasks() {
        try (Reader reader = new FileReader(DATA_FILE)) {
            Type taskListType = new TypeToken<List<Task>>(){}.getType();
            List<Task> loaded = gson.fromJson(reader, taskListType);
            if (loaded != null) {
                tasks = loaded;
                nexId = tasks.stream()
                        .mapToInt(Task::getId)
                        .max()
                        .orElse(0) + 1;
            }
        } catch (IOException e) {
            System.err.println("Error loading tasks from JSON file: " + e.getMessage());
        }
    }

    /**
     * Saves current task state to JSON.
     * Maintains data persistence
     */
    private void saveTasks() {
        try (Writer writer = new FileWriter(DATA_FILE)) {
            gson.toJson(tasks, writer);
        } catch (IOException e) {
            System.err.println("Error saving tasks to JSON file: " + e.getMessage());
        })
    }
}