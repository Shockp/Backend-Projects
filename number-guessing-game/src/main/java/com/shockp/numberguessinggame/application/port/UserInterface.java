package com.shockp.numberguessinggame.application.port;

/**
 * Port interface for user interface operations in the number guessing game.
 * <p>
 * This interface defines the contract for user interaction operations following the
 * hexagonal architecture pattern. It serves as a port that allows the application
 * layer to communicate with different UI implementations (CLI, GUI, web, etc.)
 * without being tightly coupled to any specific implementation.
 * </p>
 * <p>
 * Implementations of this interface should handle:
 * </p>
 * <ul>
 *   <li>Displaying messages to the user</li>
 *   <li>Capturing user input</li>
 *   <li>Presenting menu options</li>
 *   <li>Managing the user interaction flow</li>
 * </ul>
 * <p>
 * This interface is part of the application layer and follows the dependency
 * inversion principle by depending on abstractions rather than concrete implementations.
 * </p>
 */
public interface UserInterface {
    
    /**
     * Displays a message to the user.
     * <p>
     * This method should present the given message in a user-friendly format.
     * The implementation can choose how to display the message (console output,
     * GUI dialog, web page, etc.).
     * </p>
     *
     * @param message the message to display to the user, must not be null
     * @throws IllegalArgumentException if message is null
     */
    void displayMessage(String message);
    
    /**
     * Captures and returns user input.
     * <p>
     * This method should prompt the user for input and return the entered value.
     * The implementation should handle input validation and error cases appropriately.
     * </p>
     *
     * @return the user's input as a string, never null but may be empty
     * @throws RuntimeException if there's an error reading user input
     */
    String getUserInput();
    
    /**
     * Displays the main menu or navigation options to the user.
     * <p>
     * This method should present the available game options and actions
     * that the user can choose from. The menu should be clear and user-friendly.
     * </p>
     * <p>
     * Typical menu options might include:
     * </p>
     * <ul>
     *   <li>Start a new game</li>
     *   <li>Select difficulty level</li>
     *   <li>View game rules</li>
     *   <li>View high scores</li>
     *   <li>Exit the game</li>
     * </ul>
     */
    void displayMenu();
}
