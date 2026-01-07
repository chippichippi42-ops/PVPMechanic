package org.twightlight.PVPMechanic.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;
import org.twightlight.PVPMechanic.PVPMechanic;

public class MysqlStorage extends AbstractSqlStorage {

    public MysqlStorage(PVPMechanic plugin) {
        super(plugin);
        ConfigurationSection configSection = plugin.getConfigManager().getMainConfig().getConfigurationSection("storage.mysql");
        
        HikariConfig config = new HikariConfig();
        String host = configSection.getString("host", "localhost");
        int port = configSection.getInt("port", 3306);
        String database = configSection.getString("database", "pvp_mechanic");
        String username = configSection.getString("username", "root");
        String password = configSection.getString("password", "password");
        
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        config.setUsername(username);
        config.setPassword(password);
        config.addProviderProperty("cachePrepStmts", "true");
        config.addProviderProperty("prepStmtCacheSize", "250");
        config.addProviderProperty("prepStmtCacheSqlLimit", "2048");
        config.setMaximumPoolSize(configSection.getInt("pool-size", 10));
        config.setMaxLifetime(configSection.getLong("max-lifetime", 1800000));
        config.setPoolName("PVPMechanic-MySQL");
        
        this.dataSource = new HikariDataSource(config);
        initTable();
    }
}
