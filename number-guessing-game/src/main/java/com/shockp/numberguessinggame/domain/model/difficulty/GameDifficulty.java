package com.shockp.numberguessinggame.domain.model.difficulty;

import java.util.Objects;

/**
 * Represents a game difficulty level using the Strategy pattern.
 * <p>
 * This class encapsulates a difficulty strategy and provides convenient factory methods
 * for creating different difficulty levels. It acts as a wrapper around the
 * {@link DifficultyStrategy} interface, allowing for easy creation and management
 * of game difficulty levels.
 * </p>
 * <p>
 * The class provides static factory methods for creating predefined difficulty levels:
 * <ul>
 *   <li>{@link #easy()} - Creates an easy difficulty level (10 attempts)</li>
 *   <li>{@link #medium()} - Creates a medium difficulty level (5 attempts)</li>
 *   <li>{@link #hard()} - Creates a hard difficulty level (3 attempts)</li>
 * </ul>
 * </p>
 * <p>
 * Two {@code GameDifficulty} instances are considered equal if they use the same
 * strategy class, regardless of the specific strategy instance.
 * </p>
 */
public class GameDifficulty {

    /**
     * The difficulty strategy that defines the behavior of this difficulty level.
     */
    private DifficultyStrategy strategy;

    /**
     * Constructs a new {@code GameDifficulty} with the specified strategy.
     *
     * @param strategy the difficulty strategy to use, must not be null
     * @throws NullPointerException if the strategy is null
     */
    public GameDifficulty(DifficultyStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy, "Strategy cannot be null");
    }

    /**
     * Creates a new {@code GameDifficulty} with easy difficulty level.
     * <p>
     * Easy difficulty allows 10 attempts to guess the number.
     * </p>
     *
     * @return a new {@code GameDifficulty} instance with easy difficulty
     */
    public static GameDifficulty easy() {
        return new GameDifficulty(new DifficultyEasy());
    }

    /**
     * Creates a new {@code GameDifficulty} with medium difficulty level.
     * <p>
     * Medium difficulty allows 5 attempts to guess the number.
     * </p>
     *
     * @return a new {@code GameDifficulty} instance with medium difficulty
     */
    public static GameDifficulty medium() {
        return new GameDifficulty(new DifficultyMedium());
    }

    /**
     * Creates a new {@code GameDifficulty} with hard difficulty level.
     * <p>
     * Hard difficulty allows 3 attempts to guess the number.
     * </p>
     *
     * @return a new {@code GameDifficulty} instance with hard difficulty
     */
    public static GameDifficulty hard() {
        return new GameDifficulty(new DifficultyHard());
    }

    /**
     * Returns the maximum number of attempts allowed for this difficulty level.
     *
     * @return the maximum number of attempts
     */
    public int getMaxAttempts() {
        return strategy.getMaxAttempts();
    }

    /**
     * Returns the name of this difficulty level.
     *
     * @return the difficulty name
     */
    public String getDifficultyName() {
        return strategy.getDifficultyName();
    }

    /**
     * Sets a new difficulty strategy for this game difficulty.
     *
     * @param strategy the new strategy to use, must not be null
     * @throws NullPointerException if the strategy is null
     */
    public void setStrategy(DifficultyStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy, "Strategy cannot be null");
    }

    /**
     * Returns the current difficulty strategy.
     *
     * @return the current difficulty strategy
     */
    public DifficultyStrategy getStrategy() {
        return strategy;
    }

    /**
     * Compares this {@code GameDifficulty} with another object for equality.
     * <p>
     * Two {@code GameDifficulty} instances are considered equal if they use
     * the same strategy class.
     * </p>
     *
     * @param obj the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GameDifficulty)) return false;

        GameDifficulty other = (GameDifficulty) obj;
        return strategy.getClass().equals(other.strategy.getClass());
    }

    /**
     * Returns a hash code for this {@code GameDifficulty}.
     * <p>
     * The hash code is based on the strategy class to maintain consistency
     * with the {@link #equals(Object)} method.
     * </p>
     *
     * @return a hash code for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(strategy.getClass());
    }

    /**
     * Returns a string representation of this {@code GameDifficulty}.
     * <p>
     * The string includes the difficulty name and maximum attempts.
     * </p>
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return String.format("GameDifficulty{name=%s, maxAttempts=%d}", 
        strategy.getDifficultyName(), strategy.getMaxAttempts());
    }
}
