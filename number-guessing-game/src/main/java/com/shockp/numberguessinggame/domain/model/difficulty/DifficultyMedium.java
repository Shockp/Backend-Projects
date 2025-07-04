package com.shockp.numberguessinggame.domain.model.difficulty;

/**
 * Concrete implementation of the {@link DifficultyStrategy} interface representing
 * the "Medium" difficulty level in the number guessing game.
 * <p>
 * This difficulty level allows the player a moderate number of attempts to guess the number.
 * </p>
 */
public class DifficultyMedium implements DifficultyStrategy {
    
    /**
     * The maximum number of attempts allowed for the "Medium" difficulty.
     */
    private static final int MAX_ATTEMPTS = 5;

    /**
     * The name of this difficulty level.
     */
    private static final String DIFFICULTY_NAME = "Medium";

    /**
     * {@inheritDoc}
     * <p>
     * For "Medium" difficulty, this returns 5.
     * </p>
     */
    @Override
    public int getMaxAttempts() {
        return MAX_ATTEMPTS;
    }

    /**
     * {@inheritDoc}
     * <p>
     * For this implementation, returns "Medium".
     * </p>
     */
    @Override
    public String getDifficultyName() {
        return DIFFICULTY_NAME;
    }
}
