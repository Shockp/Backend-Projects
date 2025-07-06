package com.shockp.numberguessinggame.domain.service;

import com.shockp.numberguessinggame.domain.model.Game;
import com.shockp.numberguessinggame.domain.model.difficulty.GameDifficulty;
import com.shockp.numberguessinggame.domain.model.GameState;
import com.shockp.numberguessinggame.domain.model.Player;

/**
 * Domain service responsible for managing number guessing game operations and business logic.
 * <p>
 * This service encapsulates all game-related business operations and provides a clean
 * interface for game management. It follows the Domain-Driven Design principle of
 * rich domain services that contain business logic that doesn't naturally belong to
 * any single entity or value object.
 * </p>
 * <p>
 * The service coordinates between different domain entities and ensures that all
 * business rules are properly enforced during game operations. It acts as the primary
 * orchestrator for game-related workflows.
 * </p>
 * <p>
 * Key responsibilities include:
 * </p>
 * <ul>
 *   <li>Game lifecycle management (creation, state transitions, completion)</li>
 *   <li>Guess processing with comprehensive validation and feedback</li>
 *   <li>Business rule enforcement and input validation</li>
 *   <li>Score management and game statistics</li>
 *   <li>Integration with external services (number generation)</li>
 * </ul>
 * <p>
 * This service is designed to be stateless and thread-safe, making it suitable for
 * use in concurrent environments. All methods perform proper validation and throw
 * appropriate exceptions for invalid states or inputs.
 * </p>
 * 
 * @author shockp
 * @version 1.0
 * @since 1.0
 * @see Game
 * @see Player
 * @see GameDifficulty
 * @see NumberGeneratorService
 */
public class GameService {
    
    /** Service for generating random target numbers for games */
    private final NumberGeneratorService numberGeneratorService;
    
    /**
     * Constructs a new GameService with the required dependencies.
     * <p>
     * This constructor follows the dependency injection pattern to ensure proper
     * initialization and testability. All dependencies are validated to prevent
     * null pointer exceptions during service operations.
     * </p>
     *
     * @param numberGeneratorService the service for generating random numbers, cannot be null
     * @throws IllegalArgumentException if numberGeneratorService is null
     */
    public GameService(NumberGeneratorService numberGeneratorService) {
        if (numberGeneratorService == null) {
            throw new IllegalArgumentException("NumberGeneratorService cannot be null");
        }
        this.numberGeneratorService = numberGeneratorService;
    }
    
    /**
     * Creates a new game instance with the specified difficulty and player.
     * <p>
     * This method orchestrates the complete game creation process by validating
     * inputs, generating a random target number, and creating a properly initialized
     * game entity. The created game is ready for immediate play.
     * </p>
     * <p>
     * The method ensures that:
     * </p>
     * <ul>
     *   <li>All input parameters are valid and non-null</li>
     *   <li>A cryptographically secure random number is generated</li>
     *   <li>The game entity is created with proper initial state</li>
     *   <li>All business invariants are satisfied</li>
     * </ul>
     *
     * @param difficulty the difficulty level for the new game, cannot be null
     * @param player the player who will participate in the game, cannot be null
     * @return a new Game instance ready to be played
     * @throws IllegalArgumentException if difficulty or player is null
     * @see Game#Game(GameDifficulty, Player, int)
     */
    public Game createGame(GameDifficulty difficulty, Player player) {
        if (difficulty == null) {
            throw new IllegalArgumentException("Game difficulty cannot be null");
        }
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        
        int targetNumber = numberGeneratorService.generateNumber();
        return new Game(difficulty, player, targetNumber);
    }

    /**
     * Processes a player's guess and returns comprehensive feedback.
     * <p>
     * This method handles the complete guess processing workflow, including validation,
     * state management, and feedback generation. It automatically manages game state
     * transitions and ensures all business rules are followed.
     * </p>
     * <p>
     * The processing workflow includes:
     * </p>
     * <ul>
     *   <li>Input validation for game instance and guess value</li>
     *   <li>Automatic game start if the game is in NOT_STARTED state</li>
     *   <li>Guess range validation (1-100 inclusive)</li>
     *   <li>Game state validation to ensure guessing is allowed</li>
     *   <li>Guess recording and result processing</li>
     *   <li>Score management for successful guesses</li>
     *   <li>Comprehensive feedback message generation</li>
     * </ul>
     * <p>
     * The method handles all possible game outcomes and provides appropriate
     * feedback for each scenario, including win conditions, loss conditions,
     * and continuation scenarios.
     * </p>
     *
     * @param game the game instance to process the guess for, cannot be null
     * @param guess the player's guess (must be between 1 and 100 inclusive)
     * @return a descriptive feedback message indicating the result of the guess
     * @throws IllegalArgumentException if game is null or guess is outside valid range
     * @throws IllegalStateException if the game is not in a valid state for guessing
     * @see Game#recordGuess(int)
     * @see Game#startGame()
     */
    public String processGuess(Game game, int guess) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null");
        }
        
        // Validate guess range
        if (guess < 1 || guess > 100) {
            throw new IllegalArgumentException("Guess must be between 1 and 100");
        }
        
        // Auto-start game if not started
        if (game.getState() == GameState.NOT_STARTED) {
            game.startGame();
        }
        
        // Validate game state
        if (game.getState() != GameState.IN_PROGRESS) {
            throw new IllegalStateException("Game is not in progress");
        }
        
        // Record the guess and get result
        Game.GuessResult result = game.recordGuess(guess);
        
        // Handle the result and generate feedback
        return generateFeedback(game, result, guess);
    }
    
    /**
     * Generates appropriate feedback message based on the guess result.
     * <p>
     * This private method encapsulates the feedback generation logic and ensures
     * consistent messaging across all game outcomes. It handles score management
     * for successful guesses and provides informative feedback for all scenarios.
     * </p>
     * <p>
     * The method processes all possible guess results:
     * </p>
     * <ul>
     *   <li>CORRECT: Increments player score and provides congratulations message</li>
     *   <li>GAME_OVER: Reveals the target number and provides loss message</li>
     *   <li>TOO_LOW/TOO_HIGH: Provides guidance and remaining attempts count</li>
     * </ul>
     *
     * @param game the game instance containing current state and statistics
     * @param result the result of the guess operation
     * @param guess the player's guess value (for context)
     * @return a formatted feedback message appropriate for the result
     * @throws IllegalStateException if an unknown guess result is encountered
     * @see Game.GuessResult
     */
    private String generateFeedback(Game game, Game.GuessResult result, int guess) {
        switch (result) {
            case CORRECT:
                game.incrementPlayerScore();
                return String.format("Congratulations! You've guessed the number in %d attempts.", 
                                   game.getCurrentAttempts());
                
            case GAME_OVER:
                return String.format("Sorry, you've run out of attempts. The number was %d.", 
                                   game.getTargetNumber());
                
            case TOO_LOW:
                return String.format("Too low! Try again. You have %d attempts left.", 
                                   game.getRemainingAttempts());
                
            case TOO_HIGH:
                return String.format("Too high! Try again. You have %d attempts left.", 
                                   game.getRemainingAttempts());
                
            default:
                throw new IllegalStateException("Unknown guess result: " + result);
        }
    }
    
    /**
     * Validates if a game can accept guesses in its current state.
     * <p>
     * This method provides a safe way to check if a game is in a valid state
     * for processing guesses. It handles null game instances gracefully and
     * checks the game state against valid guessing states.
     * </p>
     * <p>
     * A game can accept guesses when it is in either NOT_STARTED or IN_PROGRESS
     * state. Games that are WON or LOST cannot accept further guesses.
     * </p>
     *
     * @param game the game to validate, can be null
     * @return true if the game can accept guesses, false otherwise
     * @see GameState
     */
    public boolean canAcceptGuess(Game game) {
        if (game == null) {
            return false;
        }
        
        GameState state = game.getState();
        return state == GameState.NOT_STARTED || state == GameState.IN_PROGRESS;
    }
    
    /**
     * Checks if a guess is within the valid range.
     *
     * @param guess the guess to validate
     * @return true if the guess is valid, false otherwise
     */
    public boolean isValidGuess(int guess) {
        return guess >= 1 && guess <= 100;
    }
    
    /**
     * Gets the game status information.
     *
     * @param game the game to get status for
     * @return a formatted status message
     */
    public String getGameStatus(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null");
        }
        
        return String.format("Game State: %s, Attempts: %d/%d, Difficulty: %s", 
                           game.getState(), 
                           game.getCurrentAttempts(), 
                           game.getDifficulty().getMaxAttempts(),
                           game.getDifficulty().getDifficultyName());
    }
}
