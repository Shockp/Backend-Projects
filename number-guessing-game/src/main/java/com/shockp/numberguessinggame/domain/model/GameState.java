package com.shockp.numberguessinggame.domain.model;

/**
 * Represents the different states that a number guessing game can be in.
 * <p>
 * This enum defines the four possible states of a game:
 * </p>
 * <ul>
 *   <li>{@link #NOT_STARTED} - The game has been created but not yet started</li>
 *   <li>{@link #IN_PROGRESS} - The game is currently being played</li>
 *   <li>{@link #WON} - The player has successfully guessed the number</li>
 *   <li>{@link #LOST} - The player has run out of attempts without guessing correctly</li>
 * </ul>
 * <p>
 * This enum is used throughout the game logic to track the current state
 * and determine appropriate actions and responses.
 * </p>
 */
public enum GameState {
    /**
     * The game has been created but not yet started.
     * <p>
     * This is the initial state when a new game instance is created.
     * </p>
     */
    NOT_STARTED,
    
    /**
     * The game is currently being played.
     * <p>
     * This state indicates that the game is active and the player
     * is making attempts to guess the number.
     * </p>
     */
    IN_PROGRESS,
    
    /**
     * The player has successfully guessed the number.
     * <p>
     * This state indicates that the game has ended with a win.
     * </p>
     */
    WON,
    
    /**
     * The player has run out of attempts without guessing correctly.
     * <p>
     * This state indicates that the game has ended with a loss.
     * </p>
     */
    LOST
}
