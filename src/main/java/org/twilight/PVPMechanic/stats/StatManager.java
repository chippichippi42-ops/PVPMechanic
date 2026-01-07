package org.twilight.PVPMechanic.stats;

import org.bukkit.entity.Player;
import org.twilight.PVPMechanic.PVPMechanic;
import org.twilight.PVPMechanic.api.stats.PlayerStats;
import org.twilight.PVPMechanic.api.stats.StatType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Manages player statistics
 */
public class StatManager {
    
    private final PVPMechanic plugin;
    private final Map<StatType, Double> defaultStats = new EnumMap<>(StatType.class);
    
    public StatManager(PVPMechanic plugin) {
        this.plugin = plugin;
        loadDefaultStats();
    }
    
    /**
     * Load default stat values from configuration
     */
    private void loadDefaultStats() {
        for (StatType statType : StatType.values()) {
            String configPath = "default." + statType.name();
            if (plugin.getConfigManager().getStatsConfig().contains(configPath)) {
                double defaultValue = plugin.getConfigManager().getStatsConfig().getDouble(configPath);
                defaultStats.put(statType, defaultValue);
            }
        }
    }
    
    /**
     * Get player stats (will load from storage if not cached)
     * @param player The player
     * @return PlayerStats instance
     */
    public PlayerStats getPlayerStats(Player player) {
        return plugin.getStorageManager().getPlayerStats(player);
    }
    
    /**
     * Save player stats
     * @param player The player
     * @param stats The stats to save
     */
    public void savePlayerStats(Player player, PlayerStats stats) {
        plugin.getStorageManager().savePlayerStats(player, stats);
    }
    
    /**
     * Register a custom stat type
     * @param statType The stat type to register
     * @param defaultValue Default value for the stat
     */
    public void registerCustomStat(StatType statType, double defaultValue) {
        defaultStats.put(statType, defaultValue);
        plugin.getLogger().info("Registered custom stat: " + statType.name() + " with default value: " + defaultValue);
    }
    
    /**
     * Get default value for a stat
     * @param statType The stat type
     * @return Default value
     */
    public double getDefaultStat(StatType statType) {
        return defaultStats.getOrDefault(statType, 0.0);
    }
}