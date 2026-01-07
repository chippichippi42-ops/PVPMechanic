package org.twightlight.PVPMechanic.storage;

import com.zaxxer.hikari.HikariDataSource;
import org.twightlight.PVPMechanic.PVPMechanic;
import org.twightlight.PVPMechanic.api.PlayerStats;
import org.twightlight.PVPMechanic.api.StatType;
import org.twightlight.PVPMechanic.stats.PlayerStatsImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractSqlStorage implements StorageProvider {

    protected final PVPMechanic plugin;
    protected HikariDataSource dataSource;

    public AbstractSqlStorage(PVPMechanic plugin) {
        this.plugin = plugin;
    }

    protected void initTable() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS pvp_stats (" +
                             "uuid VARCHAR(36) PRIMARY KEY, " +
                             "stat_key VARCHAR(64), " +
                             "stat_value DOUBLE, " +
                             "UNIQUE(uuid, stat_key)" +
                             ")")) {
            // This schema is a bit simplistic, maybe one row per player with many columns?
            // But one row per stat is more extensible for custom stats.
            // Let's go with:
            // uuid VARCHAR(36), stat_key VARCHAR(64), stat_value DOUBLE, PRIMARY KEY (uuid, stat_key)
            ps.executeUpdate();
            
            // Re-create if I want to change it
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS pvp_stats_new (" +
                    "uuid VARCHAR(36), " +
                    "stat_key VARCHAR(64), " +
                    "stat_value DOUBLE, " +
                    "PRIMARY KEY (uuid, stat_key)" +
                    ")").executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CompletableFuture<PlayerStats> loadPlayerStats(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            PlayerStatsImpl stats = new PlayerStatsImpl(uuid, plugin.getStatsManager());
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT stat_key, stat_value FROM pvp_stats_new WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    String key = rs.getString("stat_key");
                    double value = rs.getDouble("stat_value");
                    StatType type = StatType.fromKey(key);
                    if (type != null) {
                        stats.setStat(type, value);
                    } else {
                        stats.setStat(key, value);
                    }
                }
                return found ? stats : null;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Void> savePlayerStats(PlayerStats stats) {
        return CompletableFuture.runAsync(() -> {
            PlayerStatsImpl impl = (PlayerStatsImpl) stats;
            try (Connection conn = dataSource.getConnection()) {
                conn.setAutoCommit(false);
                try (PreparedStatement ps = conn.prepareStatement(
                        "REPLACE INTO pvp_stats_new (uuid, stat_key, stat_value) VALUES (?, ?, ?)")) {
                    String uuidStr = stats.getPlayerId().toString();
                    
                    for (Map.Entry<StatType, Double> entry : impl.getAllStats().entrySet()) {
                        ps.setString(1, uuidStr);
                        ps.setString(2, entry.getKey().getKey());
                        ps.setDouble(3, entry.getValue());
                        ps.addBatch();
                    }
                    
                    for (Map.Entry<String, Double> entry : impl.getAllCustomStats().entrySet()) {
                        ps.setString(1, uuidStr);
                        ps.setString(2, entry.getKey());
                        ps.setDouble(3, entry.getValue());
                        ps.addBatch();
                    }
                    
                    ps.executeBatch();
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
