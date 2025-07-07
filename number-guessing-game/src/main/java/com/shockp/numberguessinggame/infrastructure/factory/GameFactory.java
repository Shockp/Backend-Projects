package com.shockp.numberguessinggame.infrastructure.factory;

import com.shockp.numberguessinggame.domain.model.Game;
import com.shockp.numberguessinggame.domain.model.Player;
import com.shockp.numberguessinggame.domain.model.difficulty.GameDifficulty;
import com.shockp.numberguessinggame.domain.service.NumberGeneratorService;

/**
 * Factory class for creating domain objects in the number guessing game.
 * <p>
 * This factory centralizes object creation logic and provides convenient methods
 * for creating games, players, and difficulty levels. It follows the Factory pattern
 * to encapsulate object creation and reduce coupling between components.
 * </p>
 * <p>
 * The factory is designed to be stateless and thread-safe, making it suitable for
 * use in concurrent environments. All methods perform proper validation and throw
 * appropriate exceptions for invalid inputs.
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
public class GameFactory {
    
    /**
     * Creates a new game with the specified difficulty and player.
     * <p>
     * This method generates a random target number using NumberGeneratorService
     * and creates a new Game instance with the provided parameters. The game
     * is ready to be played immediately after creation.
     * </p>
     * <p>
     * The method validates all input parameters to ensure the game is created
     * in a valid state. A new NumberGeneratorService instance is created for
     * each game to ensure proper randomization.
     * </p>
     *
     * @param difficulty the difficulty level for the game, cannot be null
     * @param player the player who will participate in the game, cannot be null
     * @return a new Game instance ready to be played
     * @throws IllegalArgumentException if difficulty or player is null
     * @see Game#Game(GameDifficulty, Player, int)
     * @see NumberGeneratorService#generateNumber()
     */
    public Game createGame(GameDifficulty difficulty, Player player) {
        if (difficulty == null || player == null) {
            throw new IllegalArgumentException("Difficulty and player cannot be null");
        }

        NumberGeneratorService numberGeneratorService = new NumberGeneratorService();
        int targetNumber = numberGeneratorService.generateNumber();

        return new Game(difficulty, player, targetNumber);
    }

    /**
     * Creates a new player with the specified name.
     * <p>
     * This method validates the player name to ensure it is not null or empty
     * after trimming whitespace. The returned player has an initial score of 0.
     * </p>
     *
     * @param name the name of the player, cannot be null or empty after trimming
     * @return a new Player instance with the specified name
     * @throws IllegalArgumentException if name is null or empty after trimming
     * @see Player#Player(String)
     */
    public Player createPlayer(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be null or empty");
        }

        return new Player(name.trim());
    }

    /**
     * Creates a difficulty level based on the provided string.
     * <p>
     * This method supports case-insensitive difficulty names and maps them
     * to the appropriate GameDifficulty instances. The supported difficulty
     * levels are "easy", "medium", and "hard".
     * </p>
     * <p>
     * The method validates the input string and provides a clear error message
     * for invalid difficulty levels.
     * </p>
     *
     * @param difficulty the difficulty name as a string, cannot be null or empty
     * @return a GameDifficulty instance corresponding to the input
     * @throws IllegalArgumentException if difficulty is null, empty, or invalid
     * @see GameDifficulty#easy()
     * @see GameDifficulty#medium()
     * @see GameDifficulty#hard()
     */
    public GameDifficulty createDifficulty(String difficulty) {
        if (difficulty == null || difficulty.trim().isEmpty()) {
            throw new IllegalArgumentException("Difficulty cannot be null or empty");
        }

        switch (difficulty.toLowerCase()) {
            case "easy":
                return GameDifficulty.easy();
            case "medium":
                return GameDifficulty.medium();
            case "hard":
                return GameDifficulty.hard();
            default:
                throw new IllegalArgumentException("Invalid difficulty: " + difficulty + 
                    ". Must be 'easy', 'medium', or 'hard'");
        }
    }

    /**
     * Creates a game with default settings for demo or test purposes.
     * <p>
     * This method creates a game using default values: a guest player and
     * medium difficulty. It is useful for demonstration, testing, or when
     * quick game setup is needed without user input.
     * </p>
     * <p>
     * The default configuration uses:
     * </p>
     * <ul>
     *   <li>Player name: "guest"</li>
     *   <li>Difficulty: medium (5 attempts)</li>
     *   <li>Target number: randomly generated</li>
     * </ul>
     *
     * @return a new Game instance with default settings
     * @see #createPlayer(String)
     * @see #createDifficulty(String)
     * @see #createGame(GameDifficulty, Player)
     */
    public Game createGameWithDefaults() {
        Player player = createPlayer("guest");
        GameDifficulty difficulty = createDifficulty("medium");

        return createGame(difficulty, player);
    }
}
