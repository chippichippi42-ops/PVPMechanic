package org.twilight.PVPMechanic.skill;

import org.bukkit.entity.Player;
import org.twilight.PVPMechanic.PVPMechanic;
import org.twilight.PVPMechanic.api.event.DamageEvent;
import org.twilight.PVPMechanic.api.skill.SkillType;
import org.twilight.PVPMechanic.api.stats.StatType;

/**
 * Block skill - passive skill that can reduce incoming damage
 */
public class BlockSkill implements Skill {
    
    private final PVPMechanic plugin;
    
    public BlockSkill(PVPMechanic plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getName() {
        return "BLOCK";
    }
    
    @Override
    public SkillType getType() {
        return SkillType.PASSIVE;
    }
    
    @Override
    public boolean shouldTrigger(DamageEvent event) {
        // Block is a passive skill that triggers on damage received
        Player defender = event.getDefender();
        double blockChance = getTriggerChance(defender);
        return Math.random() * 100 < blockChance;
    }
    
    @Override
    public void execute(DamageEvent event, Player attacker, Player defender) {
        // Calculate block efficiency
        double blockEfficiency = plugin.getStatManager().getPlayerStats(defender).getStat(StatType.BLOCK_EFFICIENCY);
        double damageReduction = event.getFinalDamage() * (blockEfficiency / 100.0);
        
        // Apply damage reduction
        double newDamage = event.getFinalDamage() - damageReduction;
        event.setDamage(Math.max(0, newDamage));
        
        // Log the block
        plugin.getLogger().info(defender.getName() + " blocked " + String.format("%.1f", damageReduction) + " damage!");
    }
    
    @Override
    public double getTriggerChance(Player defender) {
        return plugin.getStatManager().getPlayerStats(defender).getStat(StatType.BLOCK_CHANCE);
    }
}