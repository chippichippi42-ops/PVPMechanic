package org.twilight.PVPMechanic.skill;

import org.bukkit.entity.Player;
import org.twilight.PVPMechanic.api.event.DamageEvent;
import org.twilight.PVPMechanic.api.skill.SkillType;

/**
 * Generic skill implementation for custom skills
 */
public class GenericSkill implements Skill {
    
    private final String name;
    private final SkillType type;
    private final Object config;
    
    public GenericSkill(String name, SkillType type, Object config) {
        this.name = name;
        this.type = type;
        this.config = config;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public SkillType getType() {
        return type;
    }
    
    @Override
    public boolean shouldTrigger(DamageEvent event) {
        // Check if attack type matches skill type
        if (type == SkillType.MELEE && !"MELEE".equals(event.getAttackType())) {
            return false;
        }
        if (type == SkillType.RANGED && !"RANGED".equals(event.getAttackType())) {
            return false;
        }
        
        // Check trigger chance
        Player attacker = event.getAttacker();
        double chance = getTriggerChance(attacker);
        return Math.random() * 100 < chance;
    }
    
    @Override
    public void execute(DamageEvent event, Player attacker, Player defender) {
        // Generic skill execution - can be overridden by custom implementations
        // For now, just log that the skill triggered
        System.out.println("Generic skill " + name + " triggered!");
    }
    
    @Override
    public double getTriggerChance(Player attacker) {
        // Default 10% chance for generic skills
        return 10.0;
    }
}