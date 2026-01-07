package org.twilight.PVPMechanic.damage;

import org.bukkit.entity.Player;
import org.twilight.PVPMechanic.PVPMechanic;
import org.twilight.PVPMechanic.api.event.DamageEvent;
import org.twilight.PVPMechanic.api.event.DamageEventImpl;
import org.twilight.PVPMechanic.api.skill.SkillData;
import org.twilight.PVPMechanic.api.stats.StatType;
import org.twilight.PVPMechanic.skill.Skill;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Handles custom damage calculation
 */
public class DamageCalculator {
    
    private final PVPMechanic plugin;
    private final List<Consumer<DamageEvent>> damageModifiers = new ArrayList<>();
    
    public DamageCalculator(PVPMechanic plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Calculate custom damage for a combat event
     * @param attacker The attacker
     * @param defender The defender
     * @param baseDamage The base damage from vanilla
     * @param attackType The attack type (MELEE, RANGED)
     * @return The calculated damage event
     */
    public DamageEvent calculateDamage(Player attacker, Player defender, double baseDamage, String attackType) {
        // Create damage event
        DamageEventImpl event = new DamageEventImpl(attacker, defender, baseDamage, attackType);
        
        // Get player stats
        var attackerStats = plugin.getStatManager().getPlayerStats(attacker);
        var defenderStats = plugin.getStatManager().getPlayerStats(defender);
        
        // Apply base damage modifiers
        applyBaseDamageModifiers(event, attackerStats, defenderStats);
        
        // Check for critical hit
        checkCriticalHit(event, attackerStats);
        
        // Apply armor and defense calculations
        applyDefenseCalculations(event, attackerStats, defenderStats);
        
        // Check and trigger skills based on attack type
        triggerSkills(event, attacker, defender, attackType);
        
        // Apply post-calculation effects (lifesteal, rebound, etc.)
        applyPostCalculationEffects(event, attackerStats, defenderStats);
        
        // Apply custom damage modifiers
        applyCustomModifiers(event);
        
        return event;
    }
    
    /**
     * Apply base damage modifiers
     */
    private void applyBaseDamageModifiers(DamageEventImpl event, PlayerStats attackerStats, PlayerStats defenderStats) {
        double damage = event.getBaseDamage();
        
        // Apply damage modifiers based on attack type
        if ("MELEE".equals(event.getAttackType())) {
            damage += damage * (attackerStats.getStat(StatType.MELEE_DAMAGE) / 100.0);
            damage -= damage * (defenderStats.getStat(StatType.REDUCE_MELEE_DAMAGE) / 100.0);
        } else if ("RANGED".equals(event.getAttackType())) {
            damage += damage * (attackerStats.getStat(StatType.RANGED_DAMAGE) / 100.0);
            damage -= damage * (defenderStats.getStat(StatType.REDUCE_RANGED_DAMAGE) / 100.0);
        }
        
        // Apply general damage modifiers
        damage += damage * (attackerStats.getStat(StatType.DAMAGE) / 100.0);
        damage += damage * (attackerStats.getStat(StatType.INCREASE_DAMAGE) / 100.0);
        
        event.setDamage(damage);
    }
    
    /**
     * Check for critical hit
     */
    private void checkCriticalHit(DamageEventImpl event, PlayerStats attackerStats) {
        double critRate = attackerStats.getStat(StatType.CRIT_RATE);
        boolean isCritical = Math.random() * 100 < critRate;
        
        if (isCritical) {
            double critDamage = attackerStats.getStat(StatType.CRIT_DAMAGE);
            double damageMultiplier = 1 + (critDamage / 100.0);
            
            // Apply critical damage
            event.setDamage(event.getFinalDamage() * damageMultiplier);
            event.setCritical(true);
            
            // Apply critical damage reduction from defender
            double critReduction = defenderStats.getStat(StatType.CRITICAL_DAMAGE_REDUCTION);
            event.setDamage(event.getFinalDamage() * (1 - critReduction / 100.0));
        }
    }
    
    /**
     * Apply armor and defense calculations
     */
    private void applyDefenseCalculations(DamageEventImpl event, PlayerStats attackerStats, PlayerStats defenderStats) {
        double damage = event.getFinalDamage();
        
        // Apply penetration
        double penetration = attackerStats.getStat(StatType.PENETRATION);
        double effectiveArmor = defenderStats.getStat(StatType.ARMOR) * (1 - penetration / 100.0);
        
        // Apply armor reduction (vanilla-like formula)
        double armorReductionFactor = plugin.getConfigManager().getFormulaConfig().getDouble("damage.armor_reduction_factor", 0.04);
        double armorReduction = effectiveArmor * armorReductionFactor * damage;
        damage = Math.max(damage - armorReduction, damage * 0.2); // Minimum 20% of original damage
        
        // Apply true defense (flat reduction)
        damage = Math.max(0, damage - defenderStats.getStat(StatType.TRUE_DEFENSE));
        
        // Apply damage reduction percentage
        damage *= (1 - defenderStats.getStat(StatType.DAMAGE_REDUCTION) / 100.0);
        
        event.setDamage(damage);
    }
    
    /**
     * Trigger skills based on attack type
     */
    private void triggerSkills(DamageEventImpl event, Player attacker, Player defender, String attackType) {
        List<String> triggeredSkills = new ArrayList<>();
        List<SkillData> skillDataList = new ArrayList<>();
        
        // Get skills of appropriate type
        var skills = plugin.getSkillManager().getSkillsByType(getSkillTypeForAttack(attackType));
        
        for (Skill skill : skills) {
            if (skill.shouldTrigger(event)) {
                skill.execute(event, attacker, defender);
                triggeredSkills.add(skill.getName());
                skillDataList.add(new SkillData(skill.getName(), skill.getType(), true, null));
            }
        }
        
        // Also check passive skills
        var passiveSkills = plugin.getSkillManager().getSkillsByType(SkillType.PASSIVE);
        for (Skill skill : passiveSkills) {
            if (skill.shouldTrigger(event)) {
                skill.execute(event, attacker, defender);
                triggeredSkills.add(skill.getName());
                skillDataList.add(new SkillData(skill.getName(), skill.getType(), true, null));
            }
        }
        
        event.setSkillsTriggered(triggeredSkills);
        event.setSkillTriggeredData(skillDataList);
    }
    
    /**
     * Apply post-calculation effects
     */
    private void applyPostCalculationEffects(DamageEventImpl event, PlayerStats attackerStats, PlayerStats defenderStats) {
        // Apply lifesteal
        double lifesteal = attackerStats.getStat(StatType.LIFESTEAL);
        if (lifesteal > 0 && event.getFinalDamage() > 0) {
            double healAmount = event.getFinalDamage() * (lifesteal / 100.0);
            // In a real implementation, we would heal the attacker here
            plugin.getLogger().info("Lifesteal: " + event.getAttacker().getName() + " heals for " + String.format("%.1f", healAmount));
        }
        
        // Apply lifesteal reduction
        double lifestealReduction = defenderStats.getStat(StatType.LIFESTEAL_REDUCTION);
        if (lifestealReduction > 0) {
            // Reduce the lifesteal effect
            // Implementation would depend on how lifesteal is applied
        }
        
        // Apply damage rebound
        double damageRebound = defenderStats.getStat(StatType.DAMAGE_REBOUND);
        if (damageRebound > 0 && event.getFinalDamage() > 0) {
            double reboundDamage = event.getFinalDamage() * (damageRebound / 100.0);
            // In a real implementation, we would damage the attacker here
            plugin.getLogger().info("Damage Rebound: " + event.getDefender().getName() + " reflects " + String.format("%.1f", reboundDamage) + " damage back to " + event.getAttacker().getName());
        }
    }
    
    /**
     * Apply custom damage modifiers
     */
    private void applyCustomModifiers(DamageEvent event) {
        for (Consumer<DamageEvent> modifier : damageModifiers) {
            modifier.accept(event);
        }
    }
    
    /**
     * Convert attack type to skill type
     */
    private SkillType getSkillTypeForAttack(String attackType) {
        if ("MELEE".equals(attackType)) {
            return SkillType.MELEE;
        } else if ("RANGED".equals(attackType)) {
            return SkillType.RANGED;
        }
        return SkillType.PASSIVE; // Default to passive for unknown types
    }
    
    /**
     * Add a custom damage modifier
     * @param phase The calculation phase to hook into
     * @param modifier The damage modifier function
     */
    public void addDamageModifier(String phase, Consumer<DamageEvent> modifier) {
        damageModifiers.add(modifier);
    }
}