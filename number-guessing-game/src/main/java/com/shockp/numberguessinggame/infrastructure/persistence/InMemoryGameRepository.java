package com.shockp.numberguessinggame.infrastructure.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.ArrayList;

import com.shockp.numberguessinggame.application.port.GameRepository;
import com.shockp.numberguessinggame.domain.model.Game;

/**
 * In-memory implementation of the {@link GameRepository} interface.
 * <p>
 * This class provides a simple in-memory storage solution for game instances
 * using a HashMap for storage and an AtomicLong for generating unique game IDs.
 * It implements the GameRepository port interface following hexagonal architecture principles.
 * </p>
 * <p>
 * This implementation is suitable for:
 * </p>
 * <ul>
 *   <li>Development and testing environments</li>
 *   <li>Simple applications that don't require persistent storage</li>
 *   <li>Prototyping and demonstration purposes</li>
 * </ul>
 * <p>
 * Thread Safety: This class is thread-safe for concurrent access to the repository.
 * The HashMap is not thread-safe by default, but this implementation uses
 * synchronized blocks to ensure thread safety for all operations.
 * </p>
 * 
 * @author shockp
 * @version 1.0
 * @since 1.0
 * @see GameRepository
 * @see Game
 */
public class InMemoryGameRepository implements GameRepository {
    
    /** In-memory storage for game instances, keyed by game ID */
    private final Map<String, Game> games;
    
    /** Atomic counter for generating unique game IDs */
    private final AtomicLong gameIdCounter;

    /**
     * Constructs a new InMemoryGameRepository with initialized storage.
     * <p>
     * This constructor initializes the in-memory storage map and the game ID counter.
     * The counter starts at 1 to ensure all game IDs are positive integers.
     * </p>
     * <p>
     * The repository is ready for immediate use after construction.
     * </p>
     */
    public InMemoryGameRepository() {
        this.games = new HashMap<>();
        this.gameIdCounter = new AtomicLong(1);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation generates a unique game ID if the game doesn't have one,
     * stores the game in the in-memory map, and handles null game validation.
     * </p>
     * <p>
     * The save operation is thread-safe and will overwrite any existing game
     * with the same ID.
     * </p>
     *
     * @param game the game to save, cannot be null
     * @throws IllegalArgumentException if game is null
     */
    @Override
    public void save(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null");
        }
        
        synchronized (games) {
            // Generate unique game ID if not exists
            String gameId = generateGameId();
            games.put(gameId, game);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation retrieves the game from the in-memory map using the
     * provided game ID. Returns null if no game is found with the given ID.
     * </p>
     * <p>
     * The load operation is thread-safe and validates the gameId parameter.
     * </p>
     *
     * @param gameId the unique identifier of the game to load, cannot be null or empty
     * @return the loaded game instance, or null if not found
     * @throws IllegalArgumentException if gameId is null or empty
     */
    @Override
    public Game load(String gameId) {
        if (gameId == null || gameId.trim().isEmpty()) {
            throw new IllegalArgumentException("Game ID cannot be null or empty");
        }
        
        synchronized (games) {
            return games.get(gameId.trim());
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation removes the game from the in-memory map using the
     * provided game ID. If no game exists with the given ID, the operation
     * completes silently without throwing an exception.
     * </p>
     * <p>
     * The delete operation is thread-safe and validates the gameId parameter.
     * </p>
     *
     * @param gameId the unique identifier of the game to delete, cannot be null or empty
     * @throws IllegalArgumentException if gameId is null or empty
     */
    @Override
    public void delete(String gameId) {
        if (gameId == null || gameId.trim().isEmpty()) {
            throw new IllegalArgumentException("Game ID cannot be null or empty");
        }
        
        synchronized (games) {
            games.remove(gameId.trim());
        }
    }

    /**
     * Returns all stored games in the repository.
     * <p>
     * This method provides access to all games currently stored in the repository.
     * The returned list is a copy of the current games to prevent external modification
     * of the internal storage.
     * </p>
     * <p>
     * This operation is thread-safe and returns an empty list if no games are stored.
     * </p>
     *
     * @return a list containing all stored games, never null
     */
    public List<Game> getAllGames() {
        synchronized (games) {
            return new ArrayList<>(games.values());
        }
    }

    /**
     * Generates a unique game identifier.
     * <p>
     * This method uses an atomic counter to generate unique, sequential game IDs.
     * The generated ID is a string representation of the counter value, ensuring
     * uniqueness across all game instances.
     * </p>
     * <p>
     * The generation is thread-safe and will never return the same ID twice
     * within the same repository instance.
     * </p>
     *
     * @return a unique game identifier as a string
     */
    public String generateGameId() {
        return String.valueOf(gameIdCounter.getAndIncrement());
    }

    /**
     * Removes all games from the repository.
     * <p>
     * This method is primarily intended for testing purposes to reset the
     * repository state. It clears all stored games and resets the game ID
     * counter to 1.
     * </p>
     * <p>
     * This operation is thread-safe and should be used with caution in
     * production environments.
     * </p>
     */
    public void clearAllGames() {
        synchronized (games) {
            games.clear();
            gameIdCounter.set(1);
        }
    }
}
