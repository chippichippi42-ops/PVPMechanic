package org.twightlight.PVPMechanic.storage;

import org.bukkit.configuration.file.YamlConfiguration;
import org.twightlight.PVPMechanic.PVPMechanic;
import org.twightlight.PVPMechanic.api.PlayerStats;
import org.twightlight.PVPMechanic.api.StatType;
import org.twightlight.PVPMechanic.stats.PlayerStatsImpl;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class YamlStorage implements StorageProvider {

    private final PVPMechanic plugin;
    private final File dataFolder;

    public YamlStorage(PVPMechanic plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "players");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    @Override
    public CompletableFuture<PlayerStats> loadPlayerStats(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            File file = new File(dataFolder, uuid.toString() + ".yml");
            if (!file.exists()) return null;

            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            PlayerStatsImpl stats = new PlayerStatsImpl(uuid, plugin.getStatsManager());

            for (StatType type : StatType.values()) {
                if (config.contains("stats." + type.getKey())) {
                    stats.setStat(type, config.getDouble("stats." + type.getKey()));
                }
            }

            if (config.contains("custom_stats")) {
                for (String key : config.getConfigurationSection("custom_stats").getKeys(false)) {
                    stats.setStat(key, config.getDouble("custom_stats." + key));
                }
            }

            return stats;
        });
    }

    @Override
    public CompletableFuture<Void> savePlayerStats(PlayerStats stats) {
        return CompletableFuture.runAsync(() -> {
            File file = new File(dataFolder, stats.getPlayerId().toString() + ".yml");
            YamlConfiguration config = new YamlConfiguration();

            PlayerStatsImpl impl = (PlayerStatsImpl) stats;
            for (Map.Entry<StatType, Double> entry : impl.getAllStats().entrySet()) {
                config.set("stats." + entry.getKey().getKey(), entry.getValue());
            }

            for (Map.Entry<String, Double> entry : impl.getAllCustomStats().entrySet()) {
                config.set("custom_stats." + entry.getKey(), entry.getValue());
            }

            try {
                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save player stats for " + stats.getPlayerId());
                e.printStackTrace();
            }
        });
    }

    @Override
    public void shutdown() {
        // Nothing special for YAML
    }
}
