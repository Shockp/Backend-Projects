package com.afb.expensetracker.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Resolves file paths for the expense tracker application.
 * <p>
 * This utility class handles the resolution of file paths based on configuration
 * properties or system defaults. It primarily locates the JSON data file used for
 * storing expense records.
 * </p>
 */
public class FileLocator {

    /** Name of the configuration properties file on the classpath. */
    private static final String CONFIG_FILE = "config.properties";
    /** Property key for the data file path in the configuration. */
    private static final String DATA_FILE_KEY = "data.file";
    /** Default data file path if the configuration property is not found. */
    private static final String DEFAULT_DATA_FILE =
            System.getProperty("user.home") + "/.expense-tracker/expenses.json";

    /**
     * Returns the absolute path to the data file for storing expenses.
     * <p>
     * The method first attempts to read the path from the configuration file.
     * If the configuration file is missing, cannot be read, or does not contain
     * the data file path property, it falls back to the default location in the
     * user's home directory.
     * </p>
     *
     * @return String containing the absolute path to the data file
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
