package org.twightlight.PVPMechanic.damage;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.twightlight.PVPMechanic.PVPMechanic;
import org.twightlight.PVPMechanic.api.PlayerStats;
import org.twightlight.PVPMechanic.api.SkillType;
import org.twightlight.PVPMechanic.api.StatType;
import org.twightlight.PVPMechanic.event.CustomDamageEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class DamageManager implements Listener {

    private final PVPMechanic plugin;
    private final List<Consumer<CustomDamageEvent>> externalListeners = new ArrayList<>();
    private final Random random = new Random();
    
    private final AtomicLong totalCalculations = new AtomicLong(0);
    private final AtomicLong totalTimeNanos = new AtomicLong(0);

    public DamageManager(PVPMechanic plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void registerDamageListener(Consumer<CustomDamageEvent> listener) {
        externalListeners.add(listener);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVanillaDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player || (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player)) {
            Player attacker;
            SkillType attackType;
            if (event.getDamager() instanceof Player) {
                attacker = (Player) event.getDamager();
                attackType = SkillType.MELEE;
            } else {
                attacker = (Player) ((Projectile) event.getDamager()).getShooter();
                attackType = SkillType.RANGED;
            }

            Entity defender = event.getEntity();
            processCustomDamage(attacker, defender, event.getDamage(), attackType, event);
        }
    }

    public void processCustomDamage(Player attacker, Entity defender, double baseDamage, SkillType attackType, EntityDamageByEntityEvent vanillaEvent) {
        long start = System.nanoTime();
        try {
            PlayerStats attackerStats = plugin.getStatsManager().getPlayerStats(attacker);
            PlayerStats defenderStats = null;
            if (defender instanceof Player) {
                defenderStats = plugin.getStatsManager().getPlayerStats((Player) defender);
            }

            CustomDamageEvent customEvent = new CustomDamageEvent(attacker, defender, attackerStats, defenderStats, baseDamage, attackType);
            
            // 1. Base Damage Calculation from Stats
            double damage = baseDamage;
            if (attackType == SkillType.MELEE) {
                damage += attackerStats.getStat(StatType.MELEE_DAMAGE);
            } else {
                damage += attackerStats.getStat(StatType.RANGED_DAMAGE);
            }
            damage += attackerStats.getStat(StatType.GENERIC_DAMAGE);
            
            // 2. Crit Check
            double critRate = attackerStats.getStat(StatType.CRIT_RATE);
            if (random.nextDouble() * 100 < critRate) {
                customEvent.setCritical(true);
                double critDamageMult = attackerStats.getStat(StatType.CRIT_DAMAGE);
                double critRed = (defenderStats != null) ? defenderStats.getStat(StatType.CRITICAL_DAMAGE_REDUCTION) : 0;
                damage *= Math.max(1.0, critDamageMult - critRed / 100.0);
            }

            // 3. Skills Integration
            plugin.getSkillManager().processSkills(customEvent);

            // 4. Defense Calculation
            damage = calculateDefense(damage, attackType, attackerStats, defenderStats);

            // 5. Increase Damage Modifier
            damage *= (1.0 + attackerStats.getStat(StatType.INCREASE_DAMAGE) / 100.0);

            customEvent.setFinalDamage(damage);

            // 6. External Listeners & Bukkit Event
            Bukkit.getPluginManager().callEvent(customEvent);
            for (Consumer<CustomDamageEvent> listener : externalListeners) {
                listener.accept(customEvent);
            }

            if (customEvent.isCancelled()) {
                if (vanillaEvent != null) vanillaEvent.setCancelled(true);
                return;
            }

            // Apply final damage
            double finalDamage = customEvent.getFinalDamage();
            
            if (vanillaEvent != null) {
                vanillaEvent.setDamage(finalDamage);
            } else {
                // This is for additional attacks where we don't have a vanilla event
                if (defender instanceof LivingEntity) {
                    ((LivingEntity) defender).damage(finalDamage, attacker);
                }
            }

            // 7. Post-calculation Effects (Lifesteal, Rebound)
            applyPostEffects(finalDamage, attacker, defender, attackerStats, defenderStats);

        } finally {
            totalCalculations.incrementAndGet();
            totalTimeNanos.addAndGet(System.nanoTime() - start);
        }
    }

    private double calculateDefense(double damage, SkillType attackType, PlayerStats attackerStats, PlayerStats defenderStats) {
        if (defenderStats == null) return damage;
        
        double armor = defenderStats.getStat(StatType.GENERIC_ARMOR);
        double penetration = attackerStats.getStat(StatType.PENETRATION);
        double effectiveArmor = Math.max(0, armor - penetration);
        
        // Formula: damage = damage * (100 / (100 + effectiveArmor))
        damage *= (100.0 / (100.0 + effectiveArmor));
        
        double reduction = defenderStats.getStat(StatType.DAMAGE_REDUCTION);
        if (attackType == SkillType.MELEE) reduction += defenderStats.getStat(StatType.REDUCE_MELEE_DAMAGE);
        else if (attackType == SkillType.RANGED) reduction += defenderStats.getStat(StatType.REDUCE_RANGED_DAMAGE);
        
        return damage * (1.0 - Math.min(0.99, reduction / 100.0));
    }

    private void applyPostEffects(double finalDamage, Player attacker, Entity defender, PlayerStats attackerStats, PlayerStats defenderStats) {
        if (finalDamage <= 0) return;
        
        // Lifesteal
        double lifesteal = attackerStats.getStat(StatType.LIFESTEAL);
        double lifestealRed = (defenderStats != null) ? defenderStats.getStat(StatType.LIFESTEAL_REDUCTION) : 0;
        double actualLifesteal = lifesteal * (1.0 - lifestealRed / 100.0);
        if (actualLifesteal > 0) {
            double heal = finalDamage * (actualLifesteal / 100.0);
            attacker.setHealth(Math.min(attacker.getMaxHealth(), attacker.getHealth() + heal));
        }

        // Rebound
        if (defenderStats != null) {
            double rebound = defenderStats.getStat(StatType.DAMAGE_REBOUND);
            if (rebound > 0) {
                double reboundDamage = finalDamage * (rebound / 100.0);
                attacker.damage(reboundDamage, defender);
            }
        }
    }

    public double getAverageCalculationTimeMs() {
        long total = totalCalculations.get();
        if (total == 0) return 0;
        return (totalTimeNanos.get() / (double) total) / 1_000_000.0;
    }
}
