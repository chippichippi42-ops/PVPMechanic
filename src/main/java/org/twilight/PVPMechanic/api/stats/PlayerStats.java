package org.twilight.PVPMechanic.api.stats;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a player's combat statistics
 */
public class PlayerStats {
    
    private final UUID playerId;
    private final Map<StatType, Double> stats = new EnumMap<>(StatType.class);
    
    public PlayerStats(UUID playerId) {
        this.playerId = playerId;
    }
    
    /**
     * Get the player UUID
     * @return Player UUID
     */
    public UUID getPlayerId() {
        return playerId;
    }
    
    /**
     * Get a stat value
     * @param statType The stat type
     * @return Stat value
     */
    public double getStat(StatType statType) {
        return stats.getOrDefault(statType, 0.0);
    }
    
    /**
     * Set a stat value
     * @param statType The stat type
     * @param value The value to set
     */
    public void setStat(StatType statType, double value) {
        stats.put(statType, value);
    }
    
    /**
     * Add to a stat value
     * @param statType The stat type
     * @param value The value to add
     */
    public void addStat(StatType statType, double value) {
        stats.merge(statType, value, Double::sum);
    }
    
    /**
     * Remove from a stat value
     * @param statType The stat type
     * @param value The value to remove
     */
    public void removeStat(StatType statType, double value) {
        stats.merge(statType, -value, Double::sum);
    }
    
    /**
     * Get all stats
     * @return Map of all stats
     */
    public Map<StatType, Double> getAllStats() {
        return new EnumMap<>(stats);
    }
    
    /**
     * Reset all stats to default values
     */
    public void resetToDefaults() {
        stats.clear();
    }
}