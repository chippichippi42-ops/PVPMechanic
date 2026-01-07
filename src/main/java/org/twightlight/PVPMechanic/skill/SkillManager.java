package org.twightlight.PVPMechanic.skill;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.twightlight.PVPMechanic.PVPMechanic;
import org.twightlight.PVPMechanic.api.PlayerStats;
import org.twightlight.PVPMechanic.api.SkillType;
import org.twightlight.PVPMechanic.api.StatType;
import org.twightlight.PVPMechanic.event.CustomDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SkillManager {

    private final PVPMechanic plugin;
    private final Random random = new Random();
    private final Map<String, SkillType> customSkills = new HashMap<>();
    
    // To prevent infinite loops in additional attacks
    private final ThreadLocal<Boolean> isProcessingAdditionalAttack = ThreadLocal.withInitial(() -> false);

    public SkillManager(PVPMechanic plugin) {
        this.plugin = plugin;
    }

    public void registerSkill(String name, SkillType type) {
        customSkills.put(name, type);
    }

    public void processSkills(CustomDamageEvent event) {
        Player attacker = event.getAttacker();
        Entity defender = event.getDefender();
        PlayerStats attackerStats = event.getAttackerStats();
        PlayerStats defenderStats = event.getDefenderStats();
        SkillType attackType = event.getAttackType();

        // 1. Block Mechanic (Defender)
        if (defenderStats != null) {
            double blockChance = defenderStats.getStat(StatType.BLOCK_CHANCE);
            if (random.nextDouble() * 100 < blockChance) {
                double blockEfficiency = defenderStats.getStat(StatType.BLOCK_EFFICIENCY);
                event.setFinalDamage(event.getFinalDamage() * (1.0 - blockEfficiency / 100.0));
                event.addTriggeredSkill(new CustomDamageEvent.SkillTriggerData("Block", SkillType.BOTH, blockEfficiency));
                if (defender instanceof Player) {
                    ((Player) defender).playSound(defender.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 1.0f);
                }
            }
        }

        // Only melee skills from here on
        if (attackType != SkillType.MELEE) return;

        // 2. Thunder Mechanic
        double thunderChance = attackerStats.getStat(StatType.THUNDER_CHANCE);
        if (random.nextDouble() * 100 < thunderChance) {
            double extraDamage = attackerStats.getStat(StatType.ELEMENTAL_DAMAGE);
            event.setFinalDamage(event.getFinalDamage() + extraDamage);
            event.addTriggeredSkill(new CustomDamageEvent.SkillTriggerData("Thunder", SkillType.MELEE, extraDamage));
            defender.getWorld().strikeLightningEffect(defender.getLocation());
            defender.getWorld().playSound(defender.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.5f);
        }

        // 3. Ice Mechanic
        double iceChance = attackerStats.getStat(StatType.ICE_CHANCE);
        if (random.nextDouble() * 100 < iceChance) {
            if (defender instanceof LivingEntity) {
                double duration = 20; // 1 second = 20 ticks
                if (defenderStats != null) {
                    double reduction = defenderStats.getStat(StatType.REDUCE_NEGATIVE_EFFECT);
                    duration *= (1.0 - reduction / 100.0);
                }
                if (duration > 0) {
                    ((LivingEntity) defender).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) duration, 1));
                    event.addTriggeredSkill(new CustomDamageEvent.SkillTriggerData("Ice", SkillType.MELEE, duration));
                }
            }
        }

        // 4. Ignite Mechanic
        double igniteChance = attackerStats.getStat(StatType.IGNITE_CHANCE);
        if (random.nextDouble() * 100 < igniteChance) {
            if (defender instanceof LivingEntity) {
                double duration = 100; // 5 seconds = 100 ticks
                if (defenderStats != null) {
                    double reduction = defenderStats.getStat(StatType.REDUCE_NEGATIVE_EFFECT);
                    duration *= (1.0 - reduction / 100.0);
                }
                if (duration > 0) {
                    LivingEntity livingDefender = (LivingEntity) defender;
                    livingDefender.setFireTicks((int) duration);
                    event.addTriggeredSkill(new CustomDamageEvent.SkillTriggerData("Ignite", SkillType.MELEE, duration));
                    
                    // Fire damage task
                    new FireDamageTask(attacker, livingDefender, (int) duration, attackerStats.getStat(StatType.ELEMENTAL_DAMAGE), 
                                      defenderStats != null ? defenderStats.getStat(StatType.REDUCE_ELEMENTAL_DAMAGE) : 0).runTaskTimer(plugin, 10, 20);
                }
            }
        }

        // 5. Additional Attack
        if (!isProcessingAdditionalAttack.get()) {
            double addChance = attackerStats.getStat(StatType.ADDITIONAL_ATTACK_CHANCE);
            if (random.nextDouble() * 100 < addChance) {
                double damageScaling = attackerStats.getStat(StatType.ADDITIONAL_ATTACK_DAMAGE);
                event.addTriggeredSkill(new CustomDamageEvent.SkillTriggerData("Additional Attack", SkillType.MELEE, damageScaling));
                
                Bukkit.getScheduler().runTask(plugin, () -> {
                    isProcessingAdditionalAttack.set(true);
                    try {
                        plugin.getDamageManager().processCustomDamage(attacker, defender, event.getBaseDamage() * (damageScaling / 100.0), SkillType.MELEE, null);
                    } finally {
                        isProcessingAdditionalAttack.set(false);
                    }
                });
            }
        }
    }

    private class FireDamageTask extends org.bukkit.scheduler.BukkitRunnable {
        private final Player attacker;
        private final LivingEntity defender;
        private int ticksRemaining;
        private final double elementalDamage;
        private final double elementalRed;

        public FireDamageTask(Player attacker, LivingEntity defender, int ticks, double elementalDamage, double elementalRed) {
            this.attacker = attacker;
            this.defender = defender;
            this.ticksRemaining = ticks;
            this.elementalDamage = elementalDamage;
            this.elementalRed = elementalRed;
        }

        @Override
        public void run() {
            if (ticksRemaining <= 0 || !defender.isValid()) {
                cancel();
                return;
            }
            ticksRemaining -= 20;
            
            double damage = (1.0 + elementalDamage) * (1.0 - elementalRed / 100.0);
            
            // Remove noDamageTicks to allow consecutive hits
            defender.setNoDamageTicks(0);
            defender.damage(damage, attacker);
        }
    }
}
