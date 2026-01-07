package org.twightlight.PVPMechanic.stats;

import org.twightlight.PVPMechanic.api.PlayerStats;
import org.twightlight.PVPMechanic.api.StatType;
import org.twightlight.PVPMechanic.util.FastStatMap;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStatsImpl implements PlayerStats {

    private final UUID playerId;
    private final FastStatMap stats = new FastStatMap();
    private final Map<String, Double> customStats = new ConcurrentHashMap<>();
    private final StatsManager statsManager;

    public PlayerStatsImpl(UUID playerId, StatsManager statsManager) {
        this.playerId = playerId;
        this.statsManager = statsManager;
    }

    @Override
    public UUID getPlayerId() {
        return playerId;
    }

    @Override
    public double getStat(StatType type) {
        return stats.get(type);
    }

    @Override
    public double getStat(String customStat) {
        return customStats.getOrDefault(customStat.toLowerCase(), statsManager.getCustomStatDefault(customStat));
    }

    @Override
    public void setStat(StatType type, double value) {
        stats.set(type, value);
    }

    @Override
    public void setStat(String customStat, double value) {
        customStats.put(customStat.toLowerCase(), value);
    }

    @Override
    public void addStat(StatType type, double value) {
        stats.add(type, value);
    }

    @Override
    public void addStat(String customStat, double value) {
        customStats.put(customStat.toLowerCase(), getStat(customStat) + value);
    }

    public Map<StatType, Double> getAllStats() {
        Map<StatType, Double> all = new EnumMap<>(StatType.class);
        for (StatType type : StatType.values()) {
            all.put(type, stats.get(type));
        }
        return all;
    }

    public Map<String, Double> getAllCustomStats() {
        return new HashMap<>(customStats);
    }
}
