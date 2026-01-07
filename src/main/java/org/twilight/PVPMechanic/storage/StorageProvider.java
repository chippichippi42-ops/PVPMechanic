package org.twilight.PVPMechanic.storage;

import org.bukkit.entity.Player;
import org.twilight.PVPMechanic.api.stats.PlayerStats;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for storage providers
 */
public interface StorageProvider {
    
    /**
     * Initialize the storage provider
     */
    void initialize();
    
    /**
     * Shutdown the storage provider
     */
    void shutdown();
    
    /**
     * Load player stats asynchronously
     * @param uuid Player UUID
     * @return CompletableFuture with PlayerStats
     */
    CompletableFuture<PlayerStats> loadPlayerStats(UUID uuid);
    
    /**
     * Save player stats asynchronously
     * @param uuid Player UUID
     * @param stats PlayerStats to save
     * @return CompletableFuture that completes when saved
     */
    CompletableFuture<Void> savePlayerStats(UUID uuid, PlayerStats stats);
    
    /**
     * Get the storage type name
     * @return Storage type name
     */
    String getType();
}