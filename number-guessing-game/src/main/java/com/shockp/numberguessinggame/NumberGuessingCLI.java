package com.shockp.numberguessinggame;

import com.shockp.numberguessinggame.application.port.GameRepository;
import com.shockp.numberguessinggame.application.port.UserInterface;
import com.shockp.numberguessinggame.application.usecase.EndGameUseCase;
import com.shockp.numberguessinggame.application.usecase.MakeGuessUseCase;
import com.shockp.numberguessinggame.application.usecase.StartGameUseCase;
import com.shockp.numberguessinggame.domain.service.GameService;
import com.shockp.numberguessinggame.domain.service.NumberGeneratorService;
import com.shockp.numberguessinggame.infrastructure.cli.ConsoleView;
import com.shockp.numberguessinggame.infrastructure.cli.GameController;
import com.shockp.numberguessinggame.infrastructure.persistence.InMemoryGameRepository;

/**
 * Main entry point for the Number Guessing Game CLI application.
 * <p>
 * This class serves as the application bootstrap, responsible for:
 * </p>
 * <ul>
 *   <li>Initializing all dependencies and services</li>
 *   <li>Setting up the dependency injection container</li>
 *   <li>Creating and configuring the game controller</li>
 *   <li>Starting the main game flow</li>
 * </ul>
 * <p>
 * The class follows the dependency injection pattern and hexagonal architecture
 * principles, ensuring loose coupling between components and proper separation
 * of concerns. All dependencies are created and wired together in this class.
 * </p>
 * <p>
 * The application uses an in-memory repository for data persistence, making it
 * suitable for single-session gameplay. For production use, a persistent
 * repository implementation could be easily substituted.
 * </p>
 * 
 * @author shockp
 * @version 1.0
 * @since 1.0
 * @see GameController
 * @see GameService
 * @see GameRepository
 * @see UserInterface
 */
public class NumberGuessingCLI {
    
    /**
     * Main entry point for the Number Guessing Game application.
     * <p>
     * This method initializes all dependencies, creates the game controller,
     * and starts the game. It handles any initialization errors gracefully
     * and provides user-friendly error messages.
     * </p>
     * <p>
     * The method follows this initialization sequence:
     * </p>
     * <ol>
     *   <li>Set up domain services (NumberGeneratorService, GameService)</li>
     *   <li>Set up infrastructure components (Repository, UserInterface)</li>
     *   <li>Create use cases with proper dependencies</li>
     *   <li>Initialize the game controller</li>
     *   <li>Start the game</li>
     * </ol>
     *
     * @param args command line arguments (not used in this implementation)
     */
    public static void main(String[] args) {
        try {
            NumberGuessingCLI app = new NumberGuessingCLI();
            GameController gameController = app.initializeDependencies();
            
            System.out.println("Starting Number Guessing Game...");
            gameController.startGame();
            
        } catch (Exception e) {
            System.err.println("Error starting the game: " + e.getMessage());
            System.err.println("Please check your configuration and try again.");
            System.exit(1);
        }
    }

    /**
     * Initializes all dependencies and creates the game controller.
     * <p>
     * This method orchestrates the creation and wiring of all application
     * components following the dependency injection pattern. It ensures
     * that all dependencies are properly configured and validated before
     * creating the game controller.
     * </p>
     * <p>
     * The initialization follows the hexagonal architecture pattern:
     * </p>
     * <ul>
     *   <li>Domain services are created first</li>
     *   <li>Infrastructure adapters are initialized</li>
     *   <li>Use cases are created with proper dependencies</li>
     *   <li>Game controller is created with all use cases</li>
     * </ul>
     *
     * @return a fully configured GameController ready to start the game
     * @throws RuntimeException if any dependency fails to initialize
     * @see GameController
     */
    public GameController initializeDependencies() {
        // Set up domain services
        GameService gameService = setupGameService();
        
        // Set up infrastructure components
        GameRepository gameRepository = setupGameRepository();
        UserInterface userInterface = setupUserInterface();
        
        // Create use cases with dependencies
        StartGameUseCase startGameUseCase = new StartGameUseCase(
            gameService, gameRepository, userInterface);
        MakeGuessUseCase makeGuessUseCase = new MakeGuessUseCase(
            gameService, userInterface);
        EndGameUseCase endGameUseCase = new EndGameUseCase(
            gameRepository, userInterface);
        
        // Create and return the game controller
        return new GameController(
            startGameUseCase, makeGuessUseCase, endGameUseCase, userInterface);
    }

    /**
     * Sets up the game service with its dependencies.
     * <p>
     * This method creates the NumberGeneratorService and GameService,
     * establishing the core domain services needed for game operations.
     * The GameService is configured with the NumberGeneratorService
     * through constructor injection.
     * </p>
     *
     * @return a configured GameService instance
     * @see GameService
     * @see NumberGeneratorService
     */
    private GameService setupGameService() {
        NumberGeneratorService numberGeneratorService = new NumberGeneratorService();
        return new GameService(numberGeneratorService);
    }

    /**
     * Sets up the game repository for data persistence.
     * <p>
     * This method creates an InMemoryGameRepository instance for storing
     * game data. The in-memory implementation is suitable for single-session
     * gameplay and provides fast access to game data.
     * </p>
     * <p>
     * For production use, this method could be modified to return a
     * different repository implementation (e.g., database-based or file-based).
     * </p>
     *
     * @return a configured GameRepository instance
     * @see GameRepository
     * @see InMemoryGameRepository
     */
    private GameRepository setupGameRepository() {
        return new InMemoryGameRepository();
    }

    /**
     * Sets up the user interface for CLI interaction.
     * <p>
     * This method creates a ConsoleView instance that provides command-line
     * interface capabilities. The ConsoleView handles user input/output
     * and implements the UserInterface port contract.
     * </p>
     * <p>
     * The ConsoleView is configured to use standard input/output streams,
     * making it suitable for terminal-based interaction.
     * </p>
     *
     * @return a configured UserInterface instance
     * @see UserInterface
     * @see ConsoleView
     */
    private UserInterface setupUserInterface() {
        return new ConsoleView();
    }
}
