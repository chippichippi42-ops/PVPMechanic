package org.twilight.PVPMechanic.skill;

import org.bukkit.entity.Player;
import org.twilight.PVPMechanic.PVPMechanic;
import org.twilight.PVPMechanic.api.event.DamageEvent;
import org.twilight.PVPMechanic.api.skill.SkillType;
import org.twilight.PVPMechanic.api.stats.StatType;

/**
 * Additional Attack skill - melee skill that triggers follow-up attacks
 */
public class AdditionalAttackSkill implements Skill {
    
    private final PVPMechanic plugin;
    
    public AdditionalAttackSkill(PVPMechanic plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getName() {
        return "ADDITIONAL_ATTACK";
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
        // Get damage percentage from config
        double damagePercentage = plugin.getConfigManager().getSkillsConfig().getDouble("additional_attack.damage_percentage", 30.0);
        
        // Calculate additional attack damage
        double baseDamage = event.getBaseDamage();
        double additionalDamage = baseDamage * (damagePercentage / 100.0);
        
        // In a real implementation, we would create a new damage event for the additional attack
        // For now, just add the damage to the current event and log
        event.setDamage(event.getFinalDamage() + additionalDamage);
        
        plugin.getLogger().info(attacker.getName() + " triggered ADDITIONAL_ATTACK skill, dealing " + String.format("%.1f", additionalDamage) + " additional damage!");
        
        // Note: In a full implementation, this would create a separate damage event
        // to prevent infinite loops and allow each additional attack to be processed independently
    }
    
    @Override
    public double getTriggerChance(Player attacker) {
        return plugin.getStatManager().getPlayerStats(attacker).getStat(StatType.ADDITIONAL_ATTACK_CHANCE);
    }
}