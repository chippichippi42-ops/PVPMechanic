package org.twightlight.PVPMechanic.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.twightlight.PVPMechanic.api.PlayerStats;
import org.twightlight.PVPMechanic.api.SkillType;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CustomDamageEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player attacker;
    private final Entity defender;
    private final PlayerStats attackerStats;
    private final PlayerStats defenderStats; // May be null if defender is not a player

    private double baseDamage;
    private double finalDamage;
    private boolean critical;
    private SkillType attackType; // MELEE or RANGED

    private final List<SkillTriggerData> triggeredSkills = new ArrayList<>();
    private boolean cancelled;

    public CustomDamageEvent(Player attacker, Entity defender, PlayerStats attackerStats, PlayerStats defenderStats, double baseDamage, SkillType attackType) {
        this.attacker = attacker;
        this.defender = defender;
        this.attackerStats = attackerStats;
        this.defenderStats = defenderStats;
        this.baseDamage = baseDamage;
        this.finalDamage = baseDamage;
        this.attackType = attackType;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public void addTriggeredSkill(SkillTriggerData data) {
        triggeredSkills.add(data);
    }

    @Getter
    public static class SkillTriggerData {
        private final String skillName;
        private final SkillType type;
        private final double value;

        public SkillTriggerData(String skillName, SkillType type, double value) {
            this.skillName = skillName;
            this.type = type;
            this.value = value;
        }
    }
}
