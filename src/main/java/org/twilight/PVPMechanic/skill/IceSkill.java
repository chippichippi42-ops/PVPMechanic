package org.twilight.PVPMechanic.skill;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.twilight.PVPMechanic.PVPMechanic;
import org.twilight.PVPMechanic.api.event.DamageEvent;
import org.twilight.PVPMechanic.api.skill.SkillType;
import org.twilight.PVPMechanic.api.stats.StatType;

/**
 * Ice skill - melee skill that applies slow effect
 */
public class IceSkill implements Skill {
    
    private final PVPMechanic plugin;
    
    public IceSkill(PVPMechanic plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getName() {
        return "ICE";
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
        // Get duration from config
        double baseDuration = plugin.getConfigManager().getSkillsConfig().getDouble("ice.duration", 1.0);
        
        // Apply REDUCE_NEGATIVE_EFFECT scaling
        double reduceNegativeEffect = plugin.getStatManager().getPlayerStats(defender).getStat(StatType.REDUCE_NEGATIVE_EFFECT);
        int durationTicks = (int) (baseDuration * 20 * (1 - reduceNegativeEffect / 100.0));
        
        // Apply slow effect
        defender.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, durationTicks, 1));
        
        plugin.getLogger().info(attacker.getName() + " triggered ICE skill, applying Slow II for " + (durationTicks / 20.0) + " seconds!");
    }
    
    @Override
    public double getTriggerChance(Player attacker) {
        return plugin.getStatManager().getPlayerStats(attacker).getStat(StatType.ICE_CHANCE);
    }
}