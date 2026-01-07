package org.twilight.PVPMechanic.api.event;

import org.bukkit.entity.Player;
import org.twilight.PVPMechanic.api.skill.SkillData;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of DamageEvent interface
 */
public class DamageEventImpl implements DamageEvent {
    
    private final Player attacker;
    private final Player defender;
    private final double baseDamage;
    private double finalDamage;
    private final String attackType;
    private boolean isCritical = false;
    private boolean isCancelled = false;
    private final List<String> skillsTriggered = new ArrayList<>();
    private final List<SkillData> skillTriggeredData = new ArrayList<>();
    
    public DamageEventImpl(Player attacker, Player defender, double baseDamage, String attackType) {
        this.attacker = attacker;
        this.defender = defender;
        this.baseDamage = baseDamage;
        this.finalDamage = baseDamage;
        this.attackType = attackType;
    }
    
    @Override
    public Player getAttacker() {
        return attacker;
    }
    
    @Override
    public Player getDefender() {
        return defender;
    }
    
    @Override
    public double getBaseDamage() {
        return baseDamage;
    }
    
    @Override
    public double getFinalDamage() {
        return finalDamage;
    }
    
    @Override
    public void setDamage(double damage) {
        this.finalDamage = damage;
    }
    
    @Override
    public boolean isCritical() {
        return isCritical;
    }
    
    @Override
    public String getAttackType() {
        return attackType;
    }
    
    @Override
    public List<String> getSkillsTriggered() {
        return new ArrayList<>(skillsTriggered);
    }
    
    @Override
    public List<SkillData> getSkillTriggeredData() {
        return new ArrayList<>(skillTriggeredData);
    }
    
    @Override
    public boolean isCancelled() {
        return isCancelled;
    }
    
    @Override
    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }
    
    // Internal methods for setting skills
    public void setSkillsTriggered(List<String> skillsTriggered) {
        this.skillsTriggered.clear();
        this.skillsTriggered.addAll(skillsTriggered);
    }
    
    public void setSkillTriggeredData(List<SkillData> skillTriggeredData) {
        this.skillTriggeredData.clear();
        this.skillTriggeredData.addAll(skillTriggeredData);
    }
    
    public void setCritical(boolean critical) {
        isCritical = critical;
    }
}