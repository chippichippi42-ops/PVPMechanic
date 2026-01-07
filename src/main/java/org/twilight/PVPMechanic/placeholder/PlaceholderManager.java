package org.twilight.PVPMechanic.placeholder;

import org.bukkit.entity.Player;
import org.twilight.PVPMechanic.PVPMechanic;
import org.twilight.PVPMechanic.api.stats.PlayerStats;
import org.twilight.PVPMechanic.api.stats.StatType;

/**
 * Manages PlaceholderAPI integration
 */
public class PlaceholderManager {
    
    private final PVPMechanic plugin;
    
    public PlaceholderManager(PVPMechanic plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Initialize PlaceholderAPI integration
     */
    public void initialize() {
        // Check if PlaceholderAPI is available
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PVPMechanicPlaceholderExpansion(plugin).register();
            plugin.getLogger().info("PlaceholderAPI integration enabled!");
        } else {
            plugin.getLogger().info("PlaceholderAPI not found, placeholders will not be available.");
        }
    }
    
    /**
     * Get a stat value as a string for placeholders
     * @param player The player
     * @param statType The stat type
     * @return Formatted stat value
     */
    public String getStatPlaceholder(Player player, StatType statType) {
        PlayerStats stats = plugin.getStatManager().getPlayerStats(player);
        return String.format("%.1f", stats.getStat(statType));
    }
    
    /**
     * Get a stat value as a string for placeholders (with custom stat name)
     * @param player The player
     * @param statName The stat name
     * @return Formatted stat value or "0.0" if stat not found
     */
    public String getStatPlaceholder(Player player, String statName) {
        try {
            StatType statType = StatType.valueOf(statName.toUpperCase());
            return getStatPlaceholder(player, statType);
        } catch (IllegalArgumentException e) {
            return "0.0";
        }
    }
}