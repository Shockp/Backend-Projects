package com.afb.expensetracker.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Resolves the path to the data file for storing expenses.
 */
public class FileLocator {

    private static final String CONFIG_FILE = "config.properties";
    private static final String DATA_FILE_KEY = "data.file";
    private static final String DEFAULT_DATA_FILE =
            System.getProperty("user.home") + "/.expense-tracker/expenses.json";

    /**
     * Returns the absolute path to the data file, as configured or default.
     *
     * @return String path to the data file.
     */
    public static String getDataFilePath() {
        Properties properties = new Properties();
        try (InputStream in =
                FileLocator.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in != null) {
                properties.load(in);
                String filePath = properties.getProperty(DATA_FILE_KEY);
                if (filePath != null && !filePath.trim().isEmpty()) {
                    return Paths.get(filePath).toAbsolutePath().toString();
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading config file: " + e.getMessage());
        }
        return DEFAULT_DATA_FILE;
    }
}
