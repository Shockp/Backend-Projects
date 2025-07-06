package com.shockp.numberguessinggame.application.port;

import com.shockp.numberguessinggame.domain.model.Game;

/**
 * Repository interface for managing game persistence operations.
 * <p>
 * This interface defines the contract for saving, loading, and deleting game instances.
 * Implementations should handle the actual persistence mechanism (in-memory, file-based, database, etc.).
 * </p>
 */
public interface GameRepository {
    
    /**
     * Saves a game instance to persistent storage.
     *
     * @param game the game to save, must not be null
     * @throws IllegalArgumentException if game is null
     */
    void save(Game game);
    
    /**
     * Loads a game instance from persistent storage by its ID.
     *
     * @param gameId the unique identifier of the game to load
     * @return the loaded game instance, or null if not found
     * @throws IllegalArgumentException if gameId is null or empty
     */
    Game load(String gameId);
    
    /**
     * Deletes a game instance from persistent storage by its ID.
     *
     * @param gameId the unique identifier of the game to delete
     * @throws IllegalArgumentException if gameId is null or empty
     */
    void delete(String gameId);
}
