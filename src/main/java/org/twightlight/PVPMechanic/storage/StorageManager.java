package org.twightlight.PVPMechanic.storage;

import org.twightlight.PVPMechanic.PVPMechanic;
import org.twightlight.PVPMechanic.api.PlayerStats;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class StorageManager {

    private final PVPMechanic plugin;
    private StorageProvider provider;

    public StorageManager(PVPMechanic plugin) {
        this.plugin = plugin;
        initProvider();
    }

    private void initProvider() {
        String type = plugin.getConfigManager().getMainConfig().getString("storage.type", "yaml").toLowerCase();
        switch (type) {
            case "sqlite":
                provider = new SqliteStorage(plugin);
                break;
            case "mysql":
                provider = new MysqlStorage(plugin);
                break;
            case "yaml":
            default:
                provider = new YamlStorage(plugin);
                break;
        }
    }

    public CompletableFuture<PlayerStats> loadPlayerStats(UUID uuid) {
        return provider.loadPlayerStats(uuid);
    }

    public CompletableFuture<Void> savePlayerStats(PlayerStats stats) {
        return provider.savePlayerStats(stats);
    }

    public void shutdown() {
        if (provider != null) {
            provider.shutdown();
        }
    }
}
