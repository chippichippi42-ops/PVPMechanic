package org.twilight.PVPMechanic.skill;

import org.bukkit.entity.Player;
import org.twilight.PVPMechanic.api.event.DamageEvent;
import org.twilight.PVPMechanic.api.skill.SkillType;

/**
 * Base interface for all skills
 */
public interface Skill {
    
    /**
     * Get the skill name
     * @return Skill name
     */
    String getName();
    
    /**
     * Get the skill type
     * @return Skill type
     */
    SkillType getType();
    
    /**
     * Check if this skill should trigger for the given damage event
     * @param event The damage event
     * @return true if the skill should trigger
     */
    boolean shouldTrigger(DamageEvent event);
    
    /**
     * Execute the skill effect
     * @param event The damage event
     * @param attacker The attacker
     * @param defender The defender
     */
    void execute(DamageEvent event, Player attacker, Player defender);
    
    /**
     * Get the trigger chance for this skill
     * @param attacker The attacker
     * @return Trigger chance (0-100)
     */
    double getTriggerChance(Player attacker);
}