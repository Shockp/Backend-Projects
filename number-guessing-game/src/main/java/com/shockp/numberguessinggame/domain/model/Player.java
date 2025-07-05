package com.shockp.numberguessinggame.domain.model;

/**
 * Represents a player in the number guessing game.
 * This class encapsulates player information including their name and current score.
 */
public class Player {
    /** The player's name, which cannot be changed after creation */
    private final String name;
    
    /** The player's current score in the game */
    private int score;

    /**
     * Constructs a new Player with the specified name.
     * The initial score is set to 0.
     * 
     * @param name the name of the player, cannot be null or empty
     * @throws IllegalArgumentException if name is null or empty
     */
    public Player(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be null or empty");
        }
        this.name = name.trim();
        this.score = 0;
    }

    /**
     * Gets the player's name.
     * 
     * @return the player's name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the player's current score.
     * 
     * @return the current score
     */
    public int getScore() {
        return score;
    }

    /**
     * Increments the player's score by 1.
     * This method is typically called when the player correctly guesses a number.
     */
    public void incrementScore() {
        this.score++;
    }
}