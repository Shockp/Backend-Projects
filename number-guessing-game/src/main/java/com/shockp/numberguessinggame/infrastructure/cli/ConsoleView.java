package com.shockp.numberguessinggame.infrastructure.cli;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import com.shockp.numberguessinggame.application.port.UserInterface;
import com.shockp.numberguessinggame.domain.model.Game;

/**
 * ConsoleView provides a command-line interface for interacting with the Number Guessing Game.
 * <p>
 * It handles user input and output, displays menus, game state, and messages, and delegates
 * validation and business logic to the appropriate services and domain classes.
 * </p>
 */
public class ConsoleView implements UserInterface {
    private final Scanner scanner;
    private final PrintStream output;

    /**
     * Constructs a ConsoleView using standard input and output streams.
     */
    public ConsoleView() {
        this.scanner = new Scanner(System.in);
        this.output = System.out;
    }

    /**
     * Constructs a ConsoleView with custom input and output streams (for testing or redirection).
     * @param input the input stream to read user input from
     * @param output the output stream to write messages to
     */
    public ConsoleView(InputStream input, PrintStream output) {
        this.scanner = new Scanner(input);
        this.output = new PrintStream(output);
    }

    /**
     * Displays a message to the user.
     * @param message the message to display (must not be null or empty)
     * @throws IllegalArgumentException if the message is null or empty
     */
    @Override
    public void displayMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
        output.println(message);
    }

    /**
     * Prompts the user for input and returns the trimmed input string.
     * @return the user's input (never null or empty)
     * @throws RuntimeException if the input is null or empty
     */
    @Override
    public String getUserInput() {
        output.print("> ");
        output.flush();
        String input = scanner.nextLine();
        if (input == null || input.trim().isEmpty()) {
            throw new RuntimeException("Invalid input");
        }
        return input.trim();
    }

    /**
     * Displays the main menu options to the user.
     */
    @Override
    public void displayMenu() {
        displayMessage("------------------------------------");
        displayMessage("Welcome to the Number Guessing Game!");
        displayMessage("------------------------------------");
        displayMessage("Please select an option:");
        displayMessage("1. Start a new game");
        displayMessage("2. Load a saved game");
        displayMessage("3. View game rules");
        displayMessage("4. View high scores");
        displayMessage("5. Exit");
    }

    /**
     * Clears the console screen (works in most terminals supporting ANSI escape codes).
     */
    public void clearScreen() {
        output.print("\033[H\033[2J");
        output.flush();
    }

    /**
     * Displays the current state of the game, including player, difficulty, attempts, and status.
     * @param game the game instance whose state is to be displayed
     */
    public void displayGameState(Game game) {
        displayMessage("------------------------------------");
        displayMessage("Player: " + game.getPlayer().getName());
        displayMessage("Difficulty: " + game.getDifficulty().getDifficultyName());
        displayMessage("Current Attempts: " + game.getCurrentAttempts());
        displayMessage("Remaining Attempts: " + game.getRemainingAttempts());
        displayMessage("Status: " + game.getState());
        displayMessage("------------------------------------");
    }

    /**
     * Displays an error message to the user.
     * @param error the error message to display
     */
    public void displayError(String error) {
        displayMessage(error);
    }

    /**
     * Displays a success message to the user.
     * @param message the success message to display
     */
    public void displaySuccess(String message) {
        displayMessage(message);
    }
}
