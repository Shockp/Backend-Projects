package com.shockp.numberguessinggame.infrastructure.cli;

import com.shockp.numberguessinggame.application.port.UserInterface;
import com.shockp.numberguessinggame.application.usecase.EndGameUseCase;
import com.shockp.numberguessinggame.application.usecase.MakeGuessUseCase;
import com.shockp.numberguessinggame.application.usecase.StartGameUseCase;
import com.shockp.numberguessinggame.domain.model.Game;

/**
 * Controller class that orchestrates the number guessing game flow.
 * <p>
 * This class serves as the main coordinator for the CLI-based number guessing game,
 * managing the interaction between use cases and the user interface. It implements
 * the Controller pattern from MVC architecture, handling user input, coordinating
 * game logic through use cases, and managing the overall game flow.
 * </p>
 * <p>
 * The controller is responsible for:
 * </p>
 * <ul>
 *   <li>Orchestrating the main game loop and flow</li>
 *   <li>Processing user commands and input validation</li>
 *   <li>Displaying game status and feedback</li>
 *   <li>Managing game state transitions</li>
 *   <li>Handling welcome/goodbye messages and rules display</li>
 * </ul>
 * <p>
 * The class follows dependency injection principles, receiving all dependencies
 * through its constructor. This makes it testable and loosely coupled with
 * its dependencies.
 * </p>
 * <p>
 * Thread Safety: This class is not thread-safe. It should be used in a
 * single-threaded context, typically the main application thread.
 * </p>
 * 
 * @author shockp
 * @version 1.0
 * @since 1.0
 * @see StartGameUseCase
 * @see MakeGuessUseCase
 * @see EndGameUseCase
 * @see UserInterface
 * @see Game
 */
public class GameController {
    
    /** Use case for starting new games */
    private final StartGameUseCase startGameUseCase;
    
    /** Use case for processing player guesses */
    private final MakeGuessUseCase makeGuessUseCase;
    
    /** Use case for ending games and managing game completion */
    private final EndGameUseCase endGameUseCase;
    
    /** Interface for user interaction and display */
    private final UserInterface userInterface;

    /**
     * Constructs a new GameController with the required dependencies.
     * <p>
     * All parameters are validated to ensure they are not null. The controller
     * will throw an exception if any dependency is missing, ensuring proper
     * initialization.
     * </p>
     *
     * @param startGameUseCase the use case for starting games, must not be null
     * @param makeGuessUseCase the use case for processing guesses, must not be null
     * @param endGameUseCase the use case for ending games, must not be null
     * @param userInterface the interface for user interaction, must not be null
     * @throws IllegalArgumentException if any parameter is null
     */
    public GameController(StartGameUseCase startGameUseCase, MakeGuessUseCase makeGuessUseCase,
                          EndGameUseCase endGameUseCase, UserInterface userInterface) {
        if (startGameUseCase == null || makeGuessUseCase == null || 
            endGameUseCase == null || userInterface == null) {
            throw new IllegalArgumentException("All parameters must be non-null");
        }

        this.startGameUseCase = startGameUseCase;
        this.makeGuessUseCase = makeGuessUseCase;
        this.endGameUseCase = endGameUseCase;
        this.userInterface = userInterface;
    }

    /**
     * Starts the number guessing game and manages the complete game flow.
     * <p>
     * This method initiates the game by displaying a welcome message,
     * running the main game loop, and concluding with a goodbye message.
     * It serves as the entry point for the entire game experience.
     * </p>
     * <p>
     * The method follows a simple flow:
     * 1. Display welcome message
     * 2. Execute the main game loop
     * 3. Display goodbye message
     * </p>
     */
    public void startGame() {
        displayWelcomeMessage();
        runGameLoop();
        displayGoodbyeMessage();
    }

    /**
     * Executes the main game loop that handles multiple game sessions.
     * <p>
     * This method manages the complete game lifecycle, including:
     * </p>
     * <ul>
     *   <li>Starting new games with player input</li>
     *   <li>Processing guesses until game completion</li>
     *   <li>Displaying feedback and game status</li>
     *   <li>Handling game completion and play again logic</li>
     * </ul>
     * <p>
     * The loop continues until the player chooses not to play again.
     * Each iteration represents a complete game session.
     * </p>
     */
    public void runGameLoop() {
        boolean playAgain;

        do {
            Game game = startGameUseCase.execute(
                startGameUseCase.getPlayerName(),
                startGameUseCase.selectDifficulty()
            );

            while (!game.isGameOver()) {
                int guess = makeGuessUseCase.getValidGuess(game);

                String feedback = makeGuessUseCase.execute(game, guess);

                userInterface.displayMessage(feedback);

                handleGameState(game);
            }

            endGameUseCase.execute(game);
            
            playAgain = endGameUseCase.askToPlayAgain();
            
        } while (playAgain);
    }

    /**
     * Processes user input commands and executes appropriate actions.
     * <p>
     * This method handles various user commands including help, quit, and rules.
     * Input is normalized (trimmed and converted to lowercase) for case-insensitive
     * command matching. Invalid or empty input results in an error message.
     * </p>
     * <p>
     * Supported commands:
     * </p>
     * <ul>
     *   <li>help, h - Display the game menu</li>
     *   <li>quit, exit, q - Exit the game</li>
     *   <li>rules, r - Display game rules</li>
     * </ul>
     *
     * @param input the user input to process, can be null or empty
     */
    public void processUserInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            userInterface.displayMessage("Invalid input. Please try again.");
            return;
        }
        
        String trimmedInput = input.trim().toLowerCase();
        
        switch (trimmedInput) {
            case "help":
            case "h":
                userInterface.displayMenu();
                break;
            case "quit":
            case "exit":
            case "q":
                userInterface.displayMessage("Goodbye!");
                System.exit(0);
                break;
            case "rules":
            case "r":
                displayRules();
                break;
            default:
                userInterface.displayMessage("Unknown command. Type 'help' for available commands.");
                break;
        }
    }

    /**
     * Displays the current game status and state information.
     * <p>
     * This method shows comprehensive game information including player details,
     * difficulty level, attempt counts, and current game state. The information
     * is formatted for easy reading with clear section boundaries.
     * </p>
     * <p>
     * If the game parameter is null, the method returns without displaying
     * any information to prevent null pointer exceptions.
     * </p>
     *
     * @param game the game instance to display status for, can be null
     */
    public void handleGameState(Game game) {
        if (game == null) {
            return;
        }
        
        userInterface.displayMessage("--- Game Status ---");
        userInterface.displayMessage("Player: " + game.getPlayer().getName());
        userInterface.displayMessage("Difficulty: " + game.getDifficulty().getDifficultyName());
        userInterface.displayMessage("Attempts: " + game.getCurrentAttempts() + "/" + game.getDifficulty().getMaxAttempts());
        userInterface.displayMessage("Remaining: " + game.getRemainingAttempts());
        userInterface.displayMessage("Status: " + game.getState());
        userInterface.displayMessage("------------------");
    }

    /**
     * Displays the welcome message when the game starts.
     * <p>
     * This method presents a formatted welcome message that introduces
     * the game to the player. The message includes basic game information
     * and sets expectations for the gameplay experience.
     * </p>
     */
    public void displayWelcomeMessage() {
        userInterface.displayMessage("==========================================");
        userInterface.displayMessage("    Welcome to the Number Guessing Game!");
        userInterface.displayMessage("==========================================");
        userInterface.displayMessage("");
        userInterface.displayMessage("Try to guess the number between 1 and 100");
        userInterface.displayMessage("Choose your difficulty level");
        userInterface.displayMessage("Beat your high score!");
        userInterface.displayMessage("");
    }

    /**
     * Displays the goodbye message when the game ends.
     * <p>
     * This method presents a formatted farewell message that thanks
     * the player for participating and invites them to return.
     * </p>
     */
    public void displayGoodbyeMessage() {
        userInterface.displayMessage("");
        userInterface.displayMessage("==========================================");
        userInterface.displayMessage("    Thanks for playing! Come back soon!");
        userInterface.displayMessage("==========================================");
    }
    
    private void displayRules() {
        userInterface.displayMessage("==========================================");
        userInterface.displayMessage("                    GAME RULES");
        userInterface.displayMessage("==========================================");
        userInterface.displayMessage("1. A random number between 1 and 100 is generated");
        userInterface.displayMessage("2. You must guess the number within the allowed attempts");
        userInterface.displayMessage("3. After each guess, you'll get a hint (too high/too low)");
        userInterface.displayMessage("4. Difficulty levels:");
        userInterface.displayMessage("   - Easy: 10 attempts");
        userInterface.displayMessage("   - Medium: 5 attempts");
        userInterface.displayMessage("   - Hard: 3 attempts");
        userInterface.displayMessage("5. Commands: help(h), rules(r), quit(q)");
        userInterface.displayMessage("==========================================");
    }
}
