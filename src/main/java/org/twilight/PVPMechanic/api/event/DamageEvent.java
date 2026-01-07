package org.twilight.PVPMechanic.api.event;

import org.bukkit.entity.Player;
import org.twilight.PVPMechanic.api.skill.SkillData;

import java.util.List;

/**
 * Custom damage event with full context
 */
public interface DamageEvent {
    
    /**
     * Get the attacker
     * @return Attacker player
     */
    Player getAttacker();
    
    /**
     * Get the defender
     * @return Defender player
     */
    Player getDefender();
    
    /**
     * Get the base damage before calculation
     * @return Base damage value
     */
    double getBaseDamage();
    
    /**
     * Get the final calculated damage
     * @return Final damage value
     */
    double getFinalDamage();
    
    /**
     * Set the final damage value
     * @param damage New damage value
     */
    void setDamage(double damage);
    
    /**
     * Check if this is a critical hit
     * @return true if critical hit
     */
    boolean isCritical();
    
    /**
     * Get the attack type
     * @return Attack type (MELEE, RANGED)
     */
    String getAttackType();
    
    /**
     * Get all skills that were triggered by this hit
     * @return List of skill names
     */
    List<String> getSkillsTriggered();
    
    /**
     * Get detailed data about triggered skills
     * @return List of SkillData objects
     */
    List<SkillData> getSkillTriggeredData();
    
    /**
     * Check if the event is cancelled
     * @return true if cancelled
     */
    boolean isCancelled();
    
    /**
     * Cancel the damage event
     * @param cancelled true to cancel
     */
    void setCancelled(boolean cancelled);
}