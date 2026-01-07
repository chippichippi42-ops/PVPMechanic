package org.twilight.PVPMechanic.api;

import org.bukkit.entity.Player;
import org.twilight.PVPMechanic.PVPMechanic;
import org.twilight.PVPMechanic.api.event.DamageEvent;
import org.twilight.PVPMechanic.api.skill.SkillType;
import org.twilight.PVPMechanic.api.stats.PlayerStats;
import org.twilight.PVPMechanic.api.stats.StatType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Implementation of the PVPMechanic API
 */
public class PVPMechanicAPIImpl implements PVPMechanicAPI {
    
    private final PVPMechanic plugin;
    private final List<Consumer<DamageEvent>> damageListeners = new ArrayList<>();
    
    public PVPMechanicAPIImpl(PVPMechanic plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public PlayerStats getPlayerStats(Player player) {
        return plugin.getStatManager().getPlayerStats(player);
    }
    
    @Override
    public void registerCustomStat(StatType statType, double defaultValue) {
        plugin.getStatManager().registerCustomStat(statType, defaultValue);
    }
    
    @Override
    public void registerSkill(String skillName, Object skillConfig, SkillType skillType) {
        plugin.getSkillManager().registerCustomSkill(skillName, skillConfig, skillType);
    }
    
    @Override
    public void addDamageModifier(String phase, Consumer<DamageEvent> modifier) {
        plugin.getDamageCalculator().addDamageModifier(phase, modifier);
    }
    
    @Override
    public void onDamage(Consumer<DamageEvent> listener) {
        damageListeners.add(listener);
    }
    
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    // Internal method to fire damage events to listeners
    public void fireDamageEvent(DamageEvent event) {
        for (Consumer<DamageEvent> listener : damageListeners) {
            listener.accept(event);
        }
    }
}