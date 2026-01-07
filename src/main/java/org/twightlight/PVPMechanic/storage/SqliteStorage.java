package org.twightlight.PVPMechanic.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.twightlight.PVPMechanic.PVPMechanic;

import java.io.File;

public class SqliteStorage extends AbstractSqlStorage {

    public SqliteStorage(PVPMechanic plugin) {
        super(plugin);
        File dbFile = new File(plugin.getDataFolder(), plugin.getConfigManager().getMainConfig().getString("storage.sqlite.file", "pvp_data.db"));
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");
        config.setPoolName("PVPMechanic-SQLite");
        config.setMaximumPoolSize(1); // SQLite only supports one writer
        
        this.dataSource = new HikariDataSource(config);
        initTable();
    }
}
