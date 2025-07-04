package com.shockp.numberguessinggame.domain.model.difficulty;

/**
 * Concrete implementation of the {@link DifficultyStrategy} interface representing
 * the "Easy" difficulty level in the number guessing game.
 * <p>
 * This difficulty level allows the player a higher number of attempts to guess the number.
 * </p>
 */
public class DifficultyEasy implements DifficultyStrategy {
    
    /**
     * The maximum number of attempts allowed for the "Easy" difficulty.
     */
    private static final int MAX_ATTEMPTS = 10;

    /**
     * The name of this difficulty level.
     */
    private static final String DIFFICULTY_NAME = "Easy";

    /**
     * {@inheritDoc}
     * <p>
     * For "Easy" difficulty, this returns 10.
     * </p>
     */
    @Override
    public int getMaxAttempts() {
        return MAX_ATTEMPTS;
    }

    /**
     * {@inheritDoc}
     * <p>
     * For this implementation, returns "Easy".
     * </p>
     */
    @Override
    public String getDifficultyName() {
        return DIFFICULTY_NAME;
    }
}