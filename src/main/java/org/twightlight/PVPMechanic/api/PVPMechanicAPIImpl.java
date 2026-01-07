package org.twightlight.PVPMechanic.api;

import org.bukkit.entity.Player;
import org.twightlight.PVPMechanic.PVPMechanic;
import org.twightlight.PVPMechanic.event.CustomDamageEvent;
import java.util.function.Consumer;

public class PVPMechanicAPIImpl implements PVPMechanicAPI {

    private final PVPMechanic plugin;

    public PVPMechanicAPIImpl(PVPMechanic plugin) {
        this.plugin = plugin;
    }

    @Override
    public PlayerStats getPlayerStats(Player player) {
        return plugin.getStatsManager().getPlayerStats(player);
    }

    @Override
    public void onDamage(Consumer<CustomDamageEvent> listener) {
        // This is a bit simplified, usually we'd use Bukkit Events for this
        // but the requirement asked for this in the API
        plugin.getDamageManager().registerDamageListener(listener);
    }

    @Override
    public void registerCustomStat(String key, double defaultValue) {
        plugin.getStatsManager().registerCustomStat(key, defaultValue);
    }

    @Override
    public void registerSkill(String name, SkillType type) {
        plugin.getSkillManager().registerSkill(name, type);
    }
}
