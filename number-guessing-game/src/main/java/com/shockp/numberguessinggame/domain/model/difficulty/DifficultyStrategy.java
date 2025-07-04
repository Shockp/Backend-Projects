package com.shockp.numberguessinggame.domain.model.difficulty;

/**
 * Strategy interface for defining difficulty levels in the number guessing game.
 * Implementations specify the maximum number of attempts allowed and the name of the difficulty.
 */
public interface DifficultyStrategy {
    
    /**
     * Returns the maximum number of attempts allowed for this difficulty level.
     *
     * @return the maximum number of attempts
     */
    int getMaxAttempts();

    /**
     * Returns the name of the difficulty level.
     *
     * @return the difficulty name
     */
    String getDifficultyName();
}
