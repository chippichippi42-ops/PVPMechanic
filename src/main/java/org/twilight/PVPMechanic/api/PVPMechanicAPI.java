package org.twilight.PVPMechanic.api;

import org.bukkit.entity.Player;
import org.twilight.PVPMechanic.api.event.DamageEvent;
import org.twilight.PVPMechanic.api.skill.SkillType;
import org.twilight.PVPMechanic.api.stats.PlayerStats;
import org.twilight.PVPMechanic.api.stats.StatType;

import java.util.function.Consumer;

/**
 * Main API interface for PVPMechanic plugin
 */
public interface PVPMechanicAPI {
    
    /**
     * Get player stats
     * @param player The player
     * @return PlayerStats instance
     */
    PlayerStats getPlayerStats(Player player);
    
    /**
     * Register a custom stat type
     * @param statType The stat type to register
     * @param defaultValue Default value for the stat
     */
    void registerCustomStat(StatType statType, double defaultValue);
    
    /**
     * Register a custom skill
     * @param skillName Name of the skill
     * @param skillConfig Configuration for the skill
     * @param skillType Type of skill (MELEE, RANGED, PASSIVE)
     */
    void registerSkill(String skillName, Object skillConfig, SkillType skillType);
    
    /**
     * Add a damage modifier hook
     * @param phase The calculation phase to hook into
     * @param modifier The damage modifier function
     */
    void addDamageModifier(String phase, Consumer<DamageEvent> modifier);
    
    /**
     * Listen to damage events
     * @param listener The event listener
     */
    void onDamage(Consumer<DamageEvent> listener);
    
    /**
     * Get the current version of the plugin
     * @return Version string
     */
    String getVersion();
}