package com.shockp.numberguessinggame.domain.model;

import com.shockp.numberguessinggame.domain.model.difficulty.GameDifficulty;

/**
 * Represents a number guessing game instance.
 * <p>
 * This class is a pure domain entity that encapsulates game state and data.
 * It follows the principle of anemic domain model where business logic is
 * handled by domain services, not the entity itself.
 * </p>
 * <p>
 * The game contains:
 * </p>
 * <ul>
 *   <li>Game state (not started, in progress, won, lost)</li>
 *   <li>Game data (target number, attempts, difficulty, player)</li>
 *   <li>Basic state transitions</li>
 * </ul>
 * <p>
 * Business logic such as guess processing, validation, and feedback generation
 * is handled by the GameService class.
 * </p>
 * 
 * <p>
 * Thread Safety: This class is not thread-safe. External synchronization
 * should be used if multiple threads access the same game instance.
 * </p>
 * 
 * <p>
 * Immutability: The difficulty, player, and target number are immutable.
 * The state and current attempts can be modified through public methods.
 * </p>
 */
public class Game {
    /** The difficulty level of the game, determines max attempts */
    private final GameDifficulty difficulty;
    
    /** The player participating in the game */
    private final Player player;
    
    /** The current state of the game */
    private GameState state;
    
    /** The target number that the player must guess */
    private final int targetNumber;
    
    /** The number of attempts the player has made so far */
    private int currentAttempts;

    /**
     * Constructs a new Game with the specified difficulty, player, and target number.
     * <p>
     * The game is initialized in the NOT_STARTED state. The target number
     * should be provided by the service layer, not generated within the entity.
     * </p>
     * <p>
     * The constructor performs validation on all parameters to ensure
     * the game is created in a valid state.
     * </p>
     *
     * @param difficulty the difficulty level of the game, cannot be null
     * @param player the player participating in the game, cannot be null
     * @param targetNumber the target number to guess (1-100 inclusive)
     * @throws IllegalArgumentException if difficulty or player is null, or targetNumber is invalid
     */
    public Game(GameDifficulty difficulty, Player player, int targetNumber) {
        if (difficulty == null) {
            throw new IllegalArgumentException("Game difficulty cannot be null");
        }
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        if (targetNumber < 1 || targetNumber > 100) {
            throw new IllegalArgumentException("Target number must be between 1 and 100");
        }
        
        this.difficulty = difficulty;
        this.player = player;
        this.targetNumber = targetNumber;
        this.state = GameState.NOT_STARTED;
        this.currentAttempts = 0;
    }

    /**
     * Starts the game by changing its state from NOT_STARTED to IN_PROGRESS.
     * <p>
     * This method can only be called when the game is in the NOT_STARTED state.
     * If the game is already in progress or has ended, this method has no effect.
     * </p>
     * <p>
     * This method is idempotent - calling it multiple times when the game
     * is already started will not change the state.
     * </p>
     */
    public void startGame() {
        if (state == GameState.NOT_STARTED) {
            state = GameState.IN_PROGRESS;
        }
    }

    /**
     * Records a guess attempt and updates the game state accordingly.
     * <p>
     * This method only updates the internal state. Business logic for
     * determining win/loss conditions and generating feedback messages
     * should be handled by the GameService.
     * </p>
     * <p>
     * The method increments the attempt counter and checks if the guess
     * is correct or if the maximum attempts have been reached.
     * </p>
     *
     * @param guess the player's guess (should be between 1-100)
     * @return the result of the guess (correct, too high, too low, or game over)
     * @throws IllegalStateException if the game is not in progress
     */
    public GuessResult recordGuess(int guess) {
        if (state != GameState.IN_PROGRESS) {
            throw new IllegalStateException("Game is not in progress");
        }

        currentAttempts++;

        if (guess == targetNumber) {
            state = GameState.WON;
            return GuessResult.CORRECT;
        } else if (currentAttempts >= difficulty.getMaxAttempts()) {
            state = GameState.LOST;
            return GuessResult.GAME_OVER;
        } else {
            return guess < targetNumber ? GuessResult.TOO_LOW : GuessResult.TOO_HIGH;
        }
    }

    /**
     * Gets the difficulty level of this game.
     * <p>
     * The difficulty determines the maximum number of attempts allowed
     * and other game parameters.
     * </p>
     *
     * @return the game difficulty, never null
     */
    public GameDifficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Gets the player participating in this game.
     * <p>
     * The player object contains information about the participant
     * including their name and score.
     * </p>
     *
     * @return the game player, never null
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the current state of the game.
     * <p>
     * The game can be in one of four states: NOT_STARTED, IN_PROGRESS,
     * WON, or LOST.
     * </p>
     *
     * @return the current game state, never null
     */
    public GameState getState() {
        return state;
    }

    /**
     * Gets the target number that the player must guess.
     * <p>
     * Note: This method is typically used for testing or debugging purposes.
     * In a production game, the target number should remain hidden from the player.
     * </p>
     * <p>
     * The target number is set during construction and cannot be changed.
     * </p>
     *
     * @return the target number (1-100 inclusive)
     */
    public int getTargetNumber() {
        return targetNumber;
    }

    /**
     * Gets the number of attempts the player has made so far.
     * <p>
     * This counter starts at 0 and is incremented with each guess.
     * </p>
     *
     * @return the current number of attempts (0 or greater)
     */
    public int getCurrentAttempts() {
        return currentAttempts;
    }

    /**
     * Gets the number of attempts remaining for the player.
     * <p>
     * This is calculated as the difference between the maximum allowed
     * attempts (based on difficulty) and the current number of attempts.
     * </p>
     * <p>
     * The result can be negative if the player has exceeded the maximum
     * attempts, though this should not occur in normal gameplay.
     * </p>
     *
     * @return the number of remaining attempts (can be negative)
     */
    public int getRemainingAttempts() {
        return difficulty.getMaxAttempts() - currentAttempts;
    }

    /**
     * Checks if the game has ended (either won or lost).
     * <p>
     * A game is considered over when the player has either correctly
     * guessed the number or exhausted all available attempts.
     * </p>
     *
     * @return true if the game is over (WON or LOST), false otherwise
     */
    public boolean isGameOver() {
        return state == GameState.WON || state == GameState.LOST;
    }

    /**
     * Increments the player's score.
     * <p>
     * This method should be called when the player wins the game.
     * It delegates the score increment to the player object.
     * </p>
     * <p>
     * The score increment is typically handled by the GameService
     * after processing a winning guess.
     * </p>
     */
    public void incrementPlayerScore() {
        player.incrementScore();
    }

    /**
     * Enum representing the result of a guess.
     * <p>
     * This enum provides the possible outcomes when a player makes a guess:
     * </p>
     * <ul>
     *   <li>CORRECT - The player guessed the number correctly</li>
     *   <li>TOO_HIGH - The guess is higher than the target number</li>
     *   <li>TOO_LOW - The guess is lower than the target number</li>
     *   <li>GAME_OVER - The player has exhausted all attempts</li>
     * </ul>
     */
    public enum GuessResult {
        /** Player guessed the number correctly */
        CORRECT,
        
        /** Guess is higher than the target number */
        TOO_HIGH,
        
        /** Guess is lower than the target number */
        TOO_LOW,
        
        /** Game is over (max attempts reached) */
        GAME_OVER
    }
}
