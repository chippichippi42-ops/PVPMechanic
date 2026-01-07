package org.twilight.PVPMechanic.skill;

import org.bukkit.entity.Player;
import org.twilight.PVPMechanic.PVPMechanic;
import org.twilight.PVPMechanic.api.event.DamageEvent;
import org.twilight.PVPMechanic.api.skill.SkillType;
import org.twilight.PVPMechanic.api.stats.StatType;

/**
 * Ignite skill - melee skill that applies fire damage over time
 */
public class IgniteSkill implements Skill {
    
    private final PVPMechanic plugin;
    
    public IgniteSkill(PVPMechanic plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getName() {
        return "IGNITE";
    }
    
    @Override
    public SkillType getType() {
        return SkillType.MELEE;
    }
    
    @Override
    public boolean shouldTrigger(DamageEvent event) {
        // Only trigger on melee attacks
        if (!"MELEE".equals(event.getAttackType())) {
            return false;
        }
        
        Player attacker = event.getAttacker();
        double chance = getTriggerChance(attacker);
        return Math.random() * 100 < chance;
    }
    
    @Override
    public void execute(DamageEvent event, Player attacker, Player defender) {
        // Get config values
        double baseDuration = plugin.getConfigManager().getSkillsConfig().getDouble("ignite.duration", 5.0);
        double damagePerTick = plugin.getConfigManager().getSkillsConfig().getDouble("ignite.damage_per_tick", 1.0);
        
        // Apply REDUCE_NEGATIVE_EFFECT scaling to duration
        double reduceNegativeEffect = plugin.getStatManager().getPlayerStats(defender).getStat(StatType.REDUCE_NEGATIVE_EFFECT);
        int durationTicks = (int) (baseDuration * 20 * (1 - reduceNegativeEffect / 100.0));
        
        // Calculate fire damage with elemental damage scaling
        double elementalDamage = plugin.getStatManager().getPlayerStats(attacker).getStat(StatType.ELEMENTAL_DAMAGE);
        double fireDamagePerTick = damagePerTick * (1 + elementalDamage / 100.0);
        
        // Apply REDUCE_ELEMENTAL_DAMAGE
        double reduceElementalDamage = plugin.getStatManager().getPlayerStats(defender).getStat(StatType.REDUCE_ELEMENTAL_DAMAGE);
        fireDamagePerTick *= (1 - reduceElementalDamage / 100.0);
        
        // In a real implementation, we would schedule the fire damage ticks
        // For now, just log the effect
        plugin.getLogger().info(attacker.getName() + " triggered IGNITE skill! " + defender.getName() + 
                              " will take " + fireDamagePerTick + " fire damage every second for " + (durationTicks / 20.0) + " seconds");
        
        // Note: The special noDamageTicks removal would be handled in the fire damage tick logic
    }
    
    @Override
    public double getTriggerChance(Player attacker) {
        return plugin.getStatManager().getPlayerStats(attacker).getStat(StatType.IGNITE_CHANCE);
    }
}