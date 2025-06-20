package com.afb.expensetracker.persistence;

import com.afb.expensetracker.model.Expense;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the persistence of expense data using JSON serialization.
 * <p>
 * This class handles reading from and writing to the JSON data file,
 * using Gson for serialization and deserialization of expense objects.
 * It includes custom type adapters for handling Java 8 date types.
 * </p>
 */
public class StorageManager {
    /** Path to the JSON data file. */
    private final String dataFilePath;
    /** Gson instance configured with type adapters. */
    private final Gson gson;

    /**
     * Constructs a new StorageManager with the default data file path.
     * <p>
     * Initializes the Gson instance with custom type adapters for LocalDate.
     * </p>
     */
    public StorageManager() {
        this.dataFilePath = FileLocator.getDataFilePath();
        this.gson = buildGson();
    }

    /**
     * Configures and builds a Gson instance with custom type adapters.
     * <p>
     * The custom adapters handle serialization and deserialization of
     * LocalDate objects, which are not natively supported by Gson.
     * </p>
     *
     * @return A configured Gson instance
     */
    private Gson buildGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {
                    @Override
                    public JsonElement serialize(LocalDate src, Type typeOfSrc,
                                                 JsonSerializationContext context) {
                        return new JsonPrimitive(src.toString());
                    }
                })
                .registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
                    @Override
                    public LocalDate deserialize(JsonElement json, Type typeOfT,
                                                 JsonDeserializationContext context)
                            throws JsonParseException {
                        return LocalDate.parse(json.getAsString());
                    }
                })
                .create();
    }

    /**
     * Loads the list of expenses from the JSON data file.
     * <p>
     * If the file does not exist or cannot be read, an empty list is returned.
     * </p>
     *
     * @return List of Expense objects, or an empty list if the file doesn't exist
     */
    public List<Expense> loadExpenses() {
        File file = new File(dataFilePath);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (Reader reader = Files.newBufferedReader(Paths.get(dataFilePath))) {
            Type listType = new TypeToken<ArrayList<Expense>>(){}.getType();
            List<Expense> expenses = gson.fromJson(reader, listType);
            return expenses != null ? expenses : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Error reading expenses file: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Saves the list of expenses to the JSON data file.
     * <p>
     * Creates parent directories if they don't exist. If an error occurs
     * during writing, an error message is printed to standard error.
     * </p>
     *
     * @param expenses List of Expense objects to save
     */
    public void saveExpenses(List<Expense> expenses) {
        File file = new File(dataFilePath);
        // Ensure parent directories exist
        file.getParentFile().mkdirs();

        try (Writer writer = Files.newBufferedWriter(Paths.get(dataFilePath))) {
            gson.toJson(expenses, writer);
        } catch (IOException e) {
            System.err.println("Error writing expenses file: " + e.getMessage());
        }
    }
}
