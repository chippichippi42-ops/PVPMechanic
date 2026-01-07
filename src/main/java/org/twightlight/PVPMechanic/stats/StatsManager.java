package org.twightlight.PVPMechanic.stats;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.twightlight.PVPMechanic.PVPMechanic;
import org.twightlight.PVPMechanic.api.PlayerStats;
import org.twightlight.PVPMechanic.api.StatType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StatsManager implements Listener {

    private final PVPMechanic plugin;
    private final Map<UUID, PlayerStats> playerStatsMap = new ConcurrentHashMap<>();
    private final Map<String, Double> customStatsDefaults = new HashMap<>();

    public StatsManager(PVPMechanic plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public PlayerStats getPlayerStats(Player player) {
        return playerStatsMap.computeIfAbsent(player.getUniqueId(), id -> new PlayerStatsImpl(id, this));
    }

    public void registerCustomStat(String key, double defaultValue) {
        customStatsDefaults.put(key.toLowerCase(), defaultValue);
    }

    public double getCustomStatDefault(String key) {
        return customStatsDefaults.getOrDefault(key.toLowerCase(), 0.0);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        plugin.getStorageManager().loadPlayerStats(uuid).thenAccept(stats -> {
            if (stats != null) {
                playerStatsMap.put(uuid, stats);
            } else {
                playerStatsMap.put(uuid, new PlayerStatsImpl(uuid, this));
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        PlayerStats stats = playerStatsMap.remove(uuid);
        if (stats != null) {
            plugin.getStorageManager().savePlayerStats(stats);
        }
    }

    public Map<String, Double> getCustomStatsDefaults() {
        return customStatsDefaults;
    }
}
