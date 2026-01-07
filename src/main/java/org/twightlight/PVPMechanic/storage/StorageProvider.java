package org.twightlight.PVPMechanic.storage;

import org.twightlight.PVPMechanic.api.PlayerStats;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface StorageProvider {
    CompletableFuture<PlayerStats> loadPlayerStats(UUID uuid);
    CompletableFuture<Void> savePlayerStats(PlayerStats stats);
    void shutdown();
}
