package com.shockp.numberguessinggame.application.usecase;

import com.shockp.numberguessinggame.application.port.GameRepository;
import com.shockp.numberguessinggame.application.port.UserInterface;
import com.shockp.numberguessinggame.domain.model.Game;
import com.shockp.numberguessinggame.domain.model.Player;
import com.shockp.numberguessinggame.domain.model.difficulty.GameDifficulty;
import com.shockp.numberguessinggame.domain.service.GameService;

/**
 * Use case for starting a new number guessing game.
 * <p>
 * This use case orchestrates the complete game initialization process by coordinating
 * between the domain service, repository, and user interface. It follows the Single
 * Responsibility Principle by focusing solely on the game startup workflow.
 * </p>
 * <p>
 * The use case handles the following responsibilities:
 * </p>
 * <ul>
 *   <li>Input validation for player name and difficulty selection</li>
 *   <li>Coordination with domain services for game creation</li>
 *   <li>Game persistence through the repository</li>
 *   <li>User interaction and feedback provision</li>
 *   <li>Error handling and validation</li>
 * </ul>
 * <p>
 * This class is part of the application layer and should not contain business logic,
 * which is delegated to the domain services.
 * </p>
 */
public class StartGameUseCase {
    /** The domain service responsible for game creation and business logic */
    private final GameService gameService;
    
    /** The repository for persisting game state */
    private final GameRepository gameRepository;
    
    /** The interface for user interaction and feedback */
    private final UserInterface userInterface;

    /**
     * Constructs a new StartGameUseCase with required dependencies.
     * <p>
     * All dependencies are validated to ensure proper initialization. This constructor
     * follows the dependency injection pattern for better testability and loose coupling.
     * </p>
     *
     * @param gameService the domain service for game operations, cannot be null
     * @param gameRepository the repository for game persistence, cannot be null
     * @param userInterface the interface for user interaction, cannot be null
     * @throws IllegalArgumentException if any parameter is null
     */
    public StartGameUseCase(GameService gameService, GameRepository gameRepository,
                           UserInterface userInterface) {
        if (gameService == null || gameRepository == null || userInterface == null) {
            throw new IllegalArgumentException(
                "GameService, GameRepository and UserInterface cannot be null");
        }

        this.gameService = gameService;
        this.gameRepository = gameRepository;
        this.userInterface = userInterface;
    }

    /**
     * Executes the start game use case with the provided parameters.
     * <p>
     * This method orchestrates the complete game initialization workflow:
     * </p>
     * <ol>
     *   <li>Validates input parameters for correctness</li>
     *   <li>Creates a new player instance with the provided name</li>
     *   <li>Delegates game creation to the domain service</li>
     *   <li>Persists the created game through the repository</li>
     *   <li>Provides user feedback about successful game creation</li>
     *   <li>Returns the created game instance</li>
     * </ol>
     * <p>
     * The method ensures that all input is properly validated and trimmed before processing.
     * </p>
     *
     * @param playerName the name of the player, cannot be null or empty
     * @param gameDifficulty the difficulty level for the game, cannot be null
     * @return the created and persisted game instance
     * @throws IllegalArgumentException if playerName is null/empty or gameDifficulty is null
     */
    public Game execute(String playerName, GameDifficulty gameDifficulty) {
        if (playerName == null || playerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be null or empty");
        }

        if (gameDifficulty == null) {
            throw new IllegalArgumentException("Game difficulty cannot be null");
        }

        // Create player and game using domain service
        Game game = gameService.createGame(gameDifficulty, new Player(playerName.trim()));
        
        // Persist the game
        gameRepository.save(game);
        
        // Provide user feedback
        userInterface.displayMessage("Game started successfully! Good luck!");
        
        return game;
    }

    /**
     * Handles the difficulty selection process through user interaction.
     * <p>
     * This method presents the available difficulty options to the user and processes
     * their selection. It supports both numeric and text-based input for better user
     * experience.
     * </p>
     * <p>
     * The method displays:
     * </p>
     * <ul>
     *   <li>Easy difficulty with 10 attempts</li>
     *   <li>Medium difficulty with 5 attempts</li>
     *   <li>Hard difficulty with 3 attempts</li>
     * </ul>
     * <p>
     * Valid input options include both numbers (1, 2, 3) and text (easy, medium, hard).
     * </p>
     *
     * @return the selected difficulty level as a GameDifficulty instance
     * @throws IllegalArgumentException if an invalid difficulty is selected
     */
    public GameDifficulty selectDifficulty() {
        userInterface.displayMessage("Select difficulty level:");
        userInterface.displayMessage("1. Easy (10 attempts)");
        userInterface.displayMessage("2. Medium (5 attempts)");
        userInterface.displayMessage("3. Hard (3 attempts)");

        String difficultyInput = userInterface.getUserInput().toLowerCase().trim();
        
        switch (difficultyInput) {
            case "1":
            case "easy":
                return GameDifficulty.easy();
            case "2":
            case "medium":
                return GameDifficulty.medium();
            case "3":
            case "hard":
                return GameDifficulty.hard();
            default:
                throw new IllegalArgumentException("Invalid difficulty level. Please choose 1, 2, 3, or easy, medium, hard.");
        }
    }

    /**
     * Handles the player name input process through user interaction.
     * <p>
     * This method prompts the user for their name and validates the input to ensure
     * it meets the requirements for a valid player name.
     * </p>
     * <p>
     * The validation process:
     * </p>
     * <ul>
     *   <li>Checks that the input is not null</li>
     *   <li>Ensures the input is not empty after trimming whitespace</li>
     *   <li>Returns the trimmed name for consistency</li>
     * </ul>
     * <p>
     * The method provides clear error messages for invalid input to guide the user.
     * </p>
     *
     * @return the validated and trimmed player name
     * @throws IllegalArgumentException if the player name is null or empty after trimming
     */
    public String getPlayerName() {
        userInterface.displayMessage("Enter your name:");
        String playerName = userInterface.getUserInput();

        if (playerName == null || playerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be null or empty");
        }

        return playerName.trim();
    }
}