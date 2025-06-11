import java.util.List;

/**
 * Handles command-line interface and user interactions.
 * Parse arguments and delegate operations to TaskManager.
 */
public class TaskCLI {
    private final TaskManager manager = new TaskManager();

    public static void main(String[] args) {
        new TaskCLI().run(args);
    }

    /**
     * Executes the appropriate command based on arguments
     * @param args command-line arguments
     */
    public void run(String[] args) {
        if(args.length == 0) {
            showHelp();
            return;
        }

        String command = args[0].toLowerCase();
        switch(command) {
            case "add" -> handleAdd(args);
            case "update" -> handleUpdate(args);
            case "delete" -> handleDelete(args);
            case "mark-in-progress" -> handleStatusChange(args, "in-progress");
            case "mark-done" -> handleStatusChange(args, "done");
            case "list" -> handleList(args);
            case "help" -> showHelp();
            default -> {
                System.out.println("Invalid command: " + command);
                showHelp();
            }
        }
    }

    // Command handlers
    private void handleAdd(String[] args) {
        if(args.length < 2) {
            System.out.println("Usage: add \"task-description\"");
            return;
        }
        manager.addTask(args[1]);
    }

    private void handleUpdate(String[] args) {
        if(args.length < 3) {
            System.out.println("Usage: update <task-id> \"new-description\"");
            return;
        }
        try {
            int id = Integer.parseInt(args[1]);
            manager.updateTask(id, args[2]);
        } catch(NumberFormatException e) {
            System.out.println("Invalid task ID: " + args[1]);
        }
    }

    private void handleDelete(String[] args) {
        if(args.length < 2) {
            System.out.println("Usage: delete <task-id>");
            return;
        }
        try {
            int id = Integer.parseInt(args[1]);
            manager.deleteTask(id);
        } catch(NumberFormatException e) {
            System.out.println("Invalid task ID: " + args[1]);
        }
    }

    private void handleStatusChange(String[] args, String status) {
        if (args.length < 2) {
            System.out.printf("Usage: %s <task-id>%n", args[0]);
            return;
        }
        try {
            int id = Integer.parseInt(args[1]);
            if("in-progress".equals(status)) {
                manager.markInProgress(id);
            } else {
                manager.markDone(id);
            }
        } catch(NumberFormatException e) {
            System.out.println("Invalid task ID: " + args[1]);
        }
    }

    private void handleList(String[] args) {
        String filter = (args.length > 1) ? args[1] : null;
        if(filter != null && !List.of("todo", "in-progress", "done").contains(filter)) {
            System.out.println("Invalid filter. Valid options are: todo, in-progress, done");
            return;
        }
        manager.listTasks(filter);
    }

    /**
     * Displays available commands and their usage
     */
    private void showHelp() {
        System.out.println("Task Tracker CLI Commands:");
        System.out.println("  add \"description\"            Add new task");
        System.out.println("  update <id> \"description\"    Update task description");
        System.out.println("  delete <id>                  Remove task");
        System.out.println("  mark-in-progress <id>        Mark task as in-progress");
        System.out.println("  mark-done <id>               Mark task as completed");
        System.out.println("  list [status]                Show tasks (optional filter: todo, in-progress, done)");
        System.out.println("  help                         Show this help message");
    }
}