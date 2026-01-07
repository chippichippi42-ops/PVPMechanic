package org.twilight.PVPMechanic.storage;

import org.bukkit.entity.Player;
import org.twilight.PVPMechanic.PVPMechanic;
import org.twilight.PVPMechanic.api.stats.PlayerStats;
import org.twilight.PVPMechanic.storage.impl.YamlStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Manages storage providers and player data
 */
public class StorageManager {
    
    private final PVPMechanic plugin;
    private StorageProvider storageProvider;
    private final Map<UUID, PlayerStats> playerStatsCache = new HashMap<>();
    
    public StorageManager(PVPMechanic plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Initialize the storage manager
     */
    public void initialize() {
        String storageType = plugin.getConfigManager().getConfig().getString("storage.type", "yaml");
        
        switch (storageType.toLowerCase()) {
            case "yaml":
                storageProvider = new YamlStorage(plugin);
                break;
            case "sqlite":
                // storageProvider = new SqliteStorage(plugin);
                plugin.getLogger().warning("SQLite storage not yet implemented, falling back to YAML");
                storageProvider = new YamlStorage(plugin);
                break;
            case "mysql":
                // storageProvider = new MysqlStorage(plugin);
                plugin.getLogger().warning("MySQL storage not yet implemented, falling back to YAML");
                storageProvider = new YamlStorage(plugin);
                break;
            default:
                plugin.getLogger().warning("Unknown storage type: " + storageType + ", using YAML");
                storageProvider = new YamlStorage(plugin);
        }
        
        storageProvider.initialize();
        plugin.getLogger().info("Using " + storageProvider.getType() + " storage provider");
    }
    
    /**
     * Shutdown the storage manager
     */
    public void shutdown() {
        if (storageProvider != null) {
            storageProvider.shutdown();
        }
        
        // Save all cached player stats
        saveAllCachedStats();
    }
    
    /**
     * Get player stats (cached or loaded)
     * @param player The player
     * @return PlayerStats instance
     */
    public PlayerStats getPlayerStats(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Return cached stats if available
        if (playerStatsCache.containsKey(uuid)) {
            return playerStatsCache.get(uuid);
        }
        
        // Load stats asynchronously and cache them
        try {
            CompletableFuture<PlayerStats> future = storageProvider.loadPlayerStats(uuid);
            PlayerStats stats = future.get(); // This blocks, but we'll optimize later
            playerStatsCache.put(uuid, stats);
            return stats;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load player stats for " + player.getName(), e);
            // Return default stats
            PlayerStats defaultStats = new PlayerStats(player.getUniqueId());
            playerStatsCache.put(uuid, defaultStats);
            return defaultStats;
        }
    }
    
    /**
     * Save player stats
     * @param player The player
     * @param stats The stats to save
     */
    public void savePlayerStats(Player player, PlayerStats stats) {
        UUID uuid = player.getUniqueId();
        playerStatsCache.put(uuid, stats);
        
        // Save asynchronously
        storageProvider.savePlayerStats(uuid, stats).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player stats for " + player.getName(), ex);
            return null;
        });
    }
    
    /**
     * Remove player stats from cache (on logout)
     * @param player The player
     */
    public void removePlayerStats(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Save before removing
        if (playerStatsCache.containsKey(uuid)) {
            savePlayerStats(player, playerStatsCache.get(uuid));
        }
        
        playerStatsCache.remove(uuid);
    }
    
    /**
     * Save all cached player stats
     */
    private void saveAllCachedStats() {
        for (Map.Entry<UUID, PlayerStats> entry : playerStatsCache.entrySet()) {
            storageProvider.savePlayerStats(entry.getKey(), entry.getValue()).exceptionally(ex -> {
                plugin.getLogger().log(Level.SEVERE, "Failed to save player stats for " + entry.getKey(), ex);
                return null;
            });
        }
    }
    
    /**
     * Get the current storage provider
     * @return StorageProvider instance
     */
    public StorageProvider getStorageProvider() {
        return storageProvider;
    }
}