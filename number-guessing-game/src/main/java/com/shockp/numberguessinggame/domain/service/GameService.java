package com.shockp.numberguessinggame.domain.service;

import com.shockp.numberguessinggame.domain.model.Game;
import com.shockp.numberguessinggame.domain.model.difficulty.GameDifficulty;
import com.shockp.numberguessinggame.domain.model.GameState;
import com.shockp.numberguessinggame.domain.model.Player;

/**
 * Service class responsible for managing game operations and business logic.
 * <p>
 * This service provides a high-level interface for game-related operations,
 * acting as a facade for the game domain model. It handles:
 * </p>
 * <ul>
 *   <li>Game creation with specified difficulty and player</li>
 *   <li>Guess processing with automatic game state management</li>
 *   <li>Coordination between game state transitions</li>
 * </ul>
 * <p>
 * The service ensures proper game flow by automatically starting games
 * when the first guess is made, and delegates core game logic to the
 * Game domain model.
 * </p>
 */
public class GameService {
    
    /**
     * Creates a new game instance with the specified difficulty and player.
     * <p>
     * This method initializes a new Game object with the given parameters.
     * The game will be created in the NOT_STARTED state and will generate
     * a random target number for the player to guess.
     * </p>
     *
     * @param difficulty the difficulty level for the new game, cannot be null
     * @param player the player who will participate in the game, cannot be null
     * @return a new Game instance ready to be played
     * @throws IllegalArgumentException if difficulty or player is null
     */
    public Game createGame(GameDifficulty difficulty, Player player) {
        return new Game(difficulty, player);
    }

    /**
     * Processes a player's guess and returns the appropriate feedback.
     * <p>
     * This method handles the complete guess processing workflow:
     * </p>
     * <ul>
     *   <li>Automatically starts the game if it's in NOT_STARTED state</li>
     *   <li>Delegates the actual guess processing to the Game domain model</li>
     *   <li>Returns feedback message from the game</li>
     * </ul>
     * <p>
     * The method ensures proper game state management by automatically
     * transitioning from NOT_STARTED to IN_PROGRESS when the first
     * guess is made.
     * </p>
     *
     * @param game the game instance to process the guess for, cannot be null
     * @param guess the player's guess (must be between 1 and 100)
     * @return a feedback message describing the result of the guess
     * @throws IllegalArgumentException if game is null or guess is invalid
     * @throws IllegalStateException if the game is not in a valid state for guessing
     */
    public String processGuess(Game game, int guess) {
        if (game.getState() == GameState.NOT_STARTED) {
            game.startGame();
        }
        return game.makeGuess(guess);
    }
}
