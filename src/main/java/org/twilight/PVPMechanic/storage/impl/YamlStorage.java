package org.twilight.PVPMechanic.storage.impl;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.twilight.PVPMechanic.PVPMechanic;
import org.twilight.PVPMechanic.api.stats.PlayerStats;
import org.twilight.PVPMechanic.api.stats.StatType;
import org.twilight.PVPMechanic.storage.StorageProvider;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * YAML-based storage provider
 */
public class YamlStorage implements StorageProvider {
    
    private final PVPMechanic plugin;
    private File dataFolder;
    
    public YamlStorage(PVPMechanic plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void initialize() {
        dataFolder = new File(plugin.getDataFolder(), "players");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }
    
    @Override
    public void shutdown() {
        // Nothing to do for YAML storage
    }
    
    @Override
    public CompletableFuture<PlayerStats> loadPlayerStats(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            File playerFile = new File(dataFolder, uuid.toString() + ".yml");
            PlayerStats stats = new PlayerStats(uuid);
            
            if (playerFile.exists()) {
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(playerFile);
                
                // Load all stats
                for (StatType statType : StatType.values()) {
                    if (yaml.contains(statType.name())) {
                        stats.setStat(statType, yaml.getDouble(statType.name()));
                    }
                }
            } else {
                // Load default stats from config
                for (StatType statType : StatType.values()) {
                    String configPath = "default." + statType.name();
                    if (plugin.getConfigManager().getStatsConfig().contains(configPath)) {
                        double defaultValue = plugin.getConfigManager().getStatsConfig().getDouble(configPath);
                        stats.setStat(statType, defaultValue);
                    }
                }
            }
            
            return stats;
        });
    }
    
    @Override
    public CompletableFuture<Void> savePlayerStats(UUID uuid, PlayerStats stats) {
        return CompletableFuture.runAsync(() -> {
            File playerFile = new File(dataFolder, uuid.toString() + ".yml");
            YamlConfiguration yaml = new YamlConfiguration();
            
            // Save all stats
            for (StatType statType : StatType.values()) {
                yaml.set(statType.name(), stats.getStat(statType));
            }
            
            try {
                yaml.save(playerFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save player stats for " + uuid, e);
            }
        });
    }
    
    @Override
    public String getType() {
        return "yaml";
    }
}