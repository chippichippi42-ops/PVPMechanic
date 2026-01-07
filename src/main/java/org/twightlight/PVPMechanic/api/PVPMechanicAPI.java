package org.twightlight.PVPMechanic.api;

import org.bukkit.entity.Player;
import org.twightlight.PVPMechanic.event.CustomDamageEvent;
import java.util.function.Consumer;

public interface PVPMechanicAPI {
    PlayerStats getPlayerStats(Player player);
    void onDamage(Consumer<CustomDamageEvent> listener);
    void registerCustomStat(String key, double defaultValue);
    void registerSkill(String name, SkillType type);
    // Add more as needed
}
