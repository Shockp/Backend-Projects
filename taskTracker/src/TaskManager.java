import com.google.gson.Gson;
import java.util.List;

public class TaskManager {
    private List<Task> tasks;
    private final String fileName = "tasks.json";
    private final Gson gson;
    private int nextId = 1;

    public TaskManager() {

    }
}