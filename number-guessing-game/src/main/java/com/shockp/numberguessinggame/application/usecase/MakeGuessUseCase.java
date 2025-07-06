package com.shockp.numberguessinggame.application.usecase;

import com.shockp.numberguessinggame.application.port.UserInterface;
import com.shockp.numberguessinggame.domain.model.Game;
import com.shockp.numberguessinggame.domain.service.GameService;

/**
 * Use case for processing player guesses in the number guessing game.
 * <p>
 * This use case handles the complete guess processing workflow by coordinating
 * between the domain service and user interface. It follows the Single
 * Responsibility Principle by focusing solely on guess processing operations.
 * </p>
 * 
 * @author shockp
 * @version 1.0
 * @since 1.0
 * @see GameService
 * @see UserInterface
 * @see Game
 */
public class MakeGuessUseCase {
    
    /** The domain service responsible for game operations and business logic */
    private final GameService gameService;
    
    /** The interface for user interaction and feedback */
    private final UserInterface userInterface;

    /**
     * Constructs a new MakeGuessUseCase with required dependencies.
     * <p>
     * All dependencies are validated to ensure proper initialization. This constructor
     * follows the dependency injection pattern for better testability and loose coupling.
     * </p>
     *
     * @param gameService the domain service for game operations, cannot be null
     * @param userInterface the interface for user interaction, cannot be null
     * @throws IllegalArgumentException if any parameter is null
     */
    public MakeGuessUseCase(GameService gameService, UserInterface userInterface) {
        if (gameService == null || userInterface == null) {
            throw new IllegalArgumentException(
                "GameService and UserInterface cannot be null");
        }

        this.gameService = gameService;
        this.userInterface = userInterface;
    }

    /**
     * Executes the make guess use case with the provided parameters.
     * <p>
     * This method validates the game state using GameService and processes
     * the guess using GameService, returning the feedback message.
     * </p>
     *
     * @param game the game instance to process the guess for, cannot be null
     * @param guess the player's guess (must be between 1 and 100)
     * @return the feedback message from processing the guess
     * @throws IllegalArgumentException if game is null or guess is invalid
     * @throws IllegalStateException if the game is not in a valid state for guessing
     */
    public String execute(Game game, int guess) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null");
        }
        
        // Validate game state using GameService
        if (!gameService.canAcceptGuess(game)) {
            throw new IllegalStateException("Game is not in a valid state for guessing");
        }
        
        // Process guess using GameService
        return gameService.processGuess(game, guess);
    }

    /**
     * Handles the user input process for getting a valid guess.
     * <p>
     * This method prompts for guess input and validates it using GameService
     * to ensure it meets the requirements for a valid guess (1-100).
     * </p>
     *
     * @param game the game instance for context, cannot be null
     * @return the validated guess as an integer
     * @throws IllegalArgumentException if game is null
     */
    public int getValidGuess(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null");
        }
        
        while (true) {
            userInterface.displayMessage("Enter your guess (1-100):");
            String input = userInterface.getUserInput();
            
            try {
                int guess = Integer.parseInt(input.trim());
                
                // Validate input (1-100) using GameService
                if (gameService.isValidGuess(guess)) {
                    return guess;
                } else {
                    userInterface.displayMessage("Guess must be between 1 and 100. Please try again.");
                }
            } catch (NumberFormatException e) {
                userInterface.displayMessage("Invalid input. Please enter a valid number between 1 and 100.");
            }
        }
    }

    /**
     * Validates if a guess is within the acceptable range.
     * <p>
     * This method uses GameService.isValidGuess() method to check if the
     * guess is valid and returns the validation result.
     * </p>
     *
     * @param guess the guess to validate
     * @return true if the guess is valid (between 1 and 100), false otherwise
     */
    public boolean validateGuess(int guess) {
        return gameService.isValidGuess(guess);
    }
}
