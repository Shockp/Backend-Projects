package com.shockp.numberguessinggame.application.usecase;

import com.shockp.numberguessinggame.application.port.GameRepository;
import com.shockp.numberguessinggame.application.port.UserInterface;
import com.shockp.numberguessinggame.domain.model.Game;
import com.shockp.numberguessinggame.domain.model.GameState;

/**
 * Use case for handling game completion and finalization in the number guessing game.
 * <p>
 * This use case manages the end-of-game workflow, including statistics persistence,
 * final result display, and play-again functionality. It follows the Single
 * Responsibility Principle by focusing solely on game completion operations.
 * </p>
 * 
 * @author shockp
 * @version 1.0
 * @since 1.0
 * @see GameRepository
 * @see UserInterface
 * @see Game
 */
public class EndGameUseCase {

    /** The repository for persisting game data */
    private final GameRepository gameRepository;
    
    /** The interface for user interaction and feedback */
    private final UserInterface userInterface;

    /**
     * Constructs a new EndGameUseCase with required dependencies.
     * <p>
     * All dependencies are validated to ensure proper initialization. This constructor
     * follows the dependency injection pattern for better testability and loose coupling.
     * </p>
     *
     * @param gameRepository the repository for game persistence, cannot be null
     * @param userInterface the interface for user interaction, cannot be null
     * @throws IllegalArgumentException if any parameter is null
     */
    public EndGameUseCase(GameRepository gameRepository, UserInterface userInterface) {
        if (gameRepository == null || userInterface == null) {
            throw new IllegalArgumentException(
                "GameRepository and UserInterface cannot be null"
                );
        }

        this.gameRepository = gameRepository;
        this.userInterface = userInterface;
    }

    /**
     * Executes the end game use case with the provided game instance.
     * <p>
     * This method orchestrates the complete end-of-game workflow by saving
     * game statistics and displaying the final result to the player.
     * </p>
     * <p>
     * The execution workflow includes:
     * </p>
     * <ul>
     *   <li>Input validation for the game instance</li>
     *   <li>Game statistics persistence</li>
     *   <li>Final result display with game outcome</li>
     * </ul>
     *
     * @param game the game instance to finalize, cannot be null
     * @throws IllegalArgumentException if game is null
     * @see #saveGameStatistics(Game)
     * @see #displayFinalResult(Game)
     */
    public void execute(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null");
        }
        
        saveGameStatistics(game);
        displayFinalResult(game);
    }

    /**
     * Persists the game statistics to the repository.
     * <p>
     * This method saves the current game state and all associated data
     * to the game repository for future reference or analysis.
     * </p>
     *
     * @param game the game instance to save, cannot be null
     * @throws IllegalArgumentException if game is null
     * @see GameRepository#save(Game)
     */
    public void saveGameStatistics(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null");
        }
        
        gameRepository.save(game);
    }

    /**
     * Displays the final game result and statistics to the player.
     * <p>
     * This method provides comprehensive feedback about the game outcome,
     * including win/loss status, attempts used, and the target number.
     * </p>
     * <p>
     * The displayed information includes:
     * </p>
     * <ul>
     *   <li>Win or loss message based on game state</li>
     *   <li>Attempts used compared to maximum attempts</li>
     *   <li>Revelation of the target number</li>
     * </ul>
     *
     * @param game the game instance to display results for, cannot be null
     * @throws IllegalArgumentException if game is null
     * @see GameState
     */
    public void displayFinalResult(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null");
        }
        
        if (game.getState() == GameState.WON) {
            userInterface.displayMessage("Congratulations! You won!");
        } else {
            userInterface.displayMessage("Sorry! You lost!");
        }
        
        userInterface.displayMessage("Attempts used: " + game.getCurrentAttempts() +
         "/" + game.getDifficulty().getMaxAttempts());
        
        userInterface.displayMessage("The target number was: " + game.getTargetNumber());
    }

    /**
     * Prompts the player to decide whether to play another game.
     * <p>
     * This method handles the play-again decision process by prompting the user
     * and processing their response. It supports both "y"/"yes" and "n"/"no"
     * responses for better user experience.
     * </p>
     * <p>
     * The method accepts various input formats:
     * </p>
     * <ul>
     *   <li>"y" or "yes" (case-insensitive) returns true</li>
     *   <li>Any other input returns false</li>
     * </ul>
     *
     * @return true if the player wants to play again, false otherwise
     */
    public boolean askToPlayAgain() {
        userInterface.displayMessage("Do you want to play again? (y/n):");
        String input = userInterface.getUserInput().toLowerCase().trim();
        
        return input.equals("y") || input.equals("yes");
    }
}
