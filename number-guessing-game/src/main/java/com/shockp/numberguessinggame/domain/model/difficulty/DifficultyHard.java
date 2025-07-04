package com.shockp.numberguessinggame.domain.model.difficulty;

/**
 * Concrete implementation of the {@link DifficultyStrategy} interface representing
 * the "Hard" difficulty level in the number guessing game.
 * <p>
 * This difficulty level allows the player the fewest number of attempts to guess the number,
 * making the game most challenging.
 * </p>
 */
public class DifficultyHard implements DifficultyStrategy {
    
    /**
     * The maximum number of attempts allowed for the "Hard" difficulty.
     */
    private static final int MAX_ATTEMPTS = 3;

    /**
     * The name of this difficulty level.
     */
    private static final String DIFFICULTY_NAME = "Hard";

    /**
     * {@inheritDoc}
     * <p>
     * For "Hard" difficulty, this returns 3.
     * </p>
     */
    @Override
    public int getMaxAttempts() {
        return MAX_ATTEMPTS;
    }

    /**
     * {@inheritDoc}
     * <p>
     * For this implementation, returns "Hard".
     * </p>
     */
    @Override
    public String getDifficultyName() {
        return DIFFICULTY_NAME;
    }
}
