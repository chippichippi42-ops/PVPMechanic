package org.twilight.PVPMechanic.skill;

import org.bukkit.entity.Player;
import org.twilight.PVPMechanic.PVPMechanic;
import org.twilight.PVPMechanic.api.event.DamageEvent;
import org.twilight.PVPMechanic.api.skill.SkillType;
import org.twilight.PVPMechanic.api.stats.StatType;

/**
 * Thunder skill - melee skill that deals additional lightning damage
 */
public class ThunderSkill implements Skill {
    
    private final PVPMechanic plugin;
    
    public ThunderSkill(PVPMechanic plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getName() {
        return "THUNDER";
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
        // Get thunder damage multiplier from config
        double damageMultiplier = plugin.getConfigManager().getSkillsConfig().getDouble("thunder.damage_multiplier", 1.5);
        
        // Calculate additional damage based on elemental damage stat
        double elementalDamage = plugin.getStatManager().getPlayerStats(attacker).getStat(StatType.ELEMENTAL_DAMAGE);
        double additionalDamage = event.getBaseDamage() * damageMultiplier * (1 + elementalDamage / 100.0);
        
        // Add the additional damage
        event.setDamage(event.getFinalDamage() + additionalDamage);
        
        // Visual effect would be added here in a real implementation
        plugin.getLogger().info(attacker.getName() + " triggered THUNDER skill, dealing " + String.format("%.1f", additionalDamage) + " additional damage!");
    }
    
    @Override
    public double getTriggerChance(Player attacker) {
        return plugin.getStatManager().getPlayerStats(attacker).getStat(StatType.THUNDER_CHANCE);
    }
}