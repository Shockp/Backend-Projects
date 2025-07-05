package com.shockp.numberguessinggame.domain.model;

import com.shockp.numberguessinggame.domain.model.difficulty.GameDifficulty;

import java.util.Random;

/**
 * Represents a number guessing game instance.
 * <p>
 * This class manages the core game logic including:
 * </p>
 * <ul>
 *   <li>Game state management (not started, in progress, won, lost)</li>
 *   <li>Player guess processing and feedback</li>
 *   <li>Attempt tracking and validation</li>
 *   <li>Score management</li>
 *   <li>Game completion detection</li>
 * </ul>
 * <p>
 * The game generates a random target number between 1 and 100 that the player
 * must guess within the allowed number of attempts defined by the game difficulty.
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
     * Constructs a new Game with the specified difficulty and player.
     * <p>
     * The game is initialized in the NOT_STARTED state with a randomly
     * generated target number between 1 and 100.
     * </p>
     *
     * @param difficulty the difficulty level of the game, cannot be null
     * @param player the player participating in the game, cannot be null
     * @throws IllegalArgumentException if difficulty or player is null
     */
    public Game(GameDifficulty difficulty, Player player) {
        if (difficulty == null) {
            throw new IllegalArgumentException("Game difficulty cannot be null");
        }
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        
        this.difficulty = difficulty;
        this.player = player;
        this.state = GameState.NOT_STARTED;
        this.targetNumber = generateRandomNumber();
        this.currentAttempts = 0;
    }

    /**
     * Generates a random number between 1 and 100 for the player to guess.
     * <p>
     * This method uses Java's Random class to generate a uniformly distributed
     * random integer in the specified range.
     * </p>
     *
     * @return a random number between 1 and 100 (inclusive)
     */
    private int generateRandomNumber() {
        Random random = new Random();
        return random.nextInt(100) + 1;
    }

    /**
     * Starts the game by changing its state from NOT_STARTED to IN_PROGRESS.
     * <p>
     * This method can only be called when the game is in the NOT_STARTED state.
     * If the game is already in progress or has ended, this method has no effect.
     * </p>
     */
    public void startGame() {
        if (state == GameState.NOT_STARTED) {
            state = GameState.IN_PROGRESS;
        }
    }

    /**
     * Processes a player's guess and returns appropriate feedback.
     * <p>
     * This method:
     * </p>
     * <ul>
     *   <li>Validates that the game is in progress</li>
     *   <li>Increments the attempt counter</li>
     *   <li>Compares the guess with the target number</li>
     *   <li>Updates the game state if the game ends (win or loss)</li>
     *   <li>Increments the player's score if they win</li>
     *   <li>Returns appropriate feedback message</li>
     * </ul>
     *
     * @param guess the player's guess (must be between 1 and 100)
     * @return a feedback message describing the result of the guess
     * @throws IllegalStateException if the game is not in progress
     * @throws IllegalArgumentException if guess is outside valid range (1-100)
     */
    public String makeGuess(int guess) {
        if (state != GameState.IN_PROGRESS) {
            throw new IllegalStateException("Game is not in progress");
        }
        
        if (guess < 1 || guess > 100) {
            throw new IllegalArgumentException("Guess must be between 1 and 100");
        }

        currentAttempts++;

        if (guess == targetNumber) {
            state = GameState.WON;
            player.incrementScore();
            return "Congratulations! You've guessed the number in " + 
                   currentAttempts + " attempts.";
        } else if (currentAttempts >= difficulty.getMaxAttempts()) {
            state = GameState.LOST;
            return "Sorry, you've run out of attempts. The number was " + 
                   targetNumber + ".";
        } else {
            int remainingAttempts = difficulty.getMaxAttempts() - currentAttempts;

            if (guess < targetNumber) {
                return "Too low! Try again. You have " + remainingAttempts +
                       " attempts left.";
            } else {
                return "Too high! Try again. You have " + remainingAttempts +
                       " attempts left.";
            }
        }
    }

    /**
     * Gets the difficulty level of this game.
     *
     * @return the game difficulty
     */
    public GameDifficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Gets the player participating in this game.
     *
     * @return the game player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the current state of the game.
     *
     * @return the current game state
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
     *
     * @return the target number (1-100)
     */
    public int getTargetNumber() {
        return targetNumber;
    }

    /**
     * Gets the number of attempts the player has made so far.
     *
     * @return the current number of attempts
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
     *
     * @return the number of remaining attempts
     */
    public int getRemainingAttempts() {
        return difficulty.getMaxAttempts() - currentAttempts;
    }

    /**
     * Checks if the game has ended (either won or lost).
     *
     * @return true if the game is over, false otherwise
     */
    public boolean isGameOver() {
        return state == GameState.WON || state == GameState.LOST;
    }
}
