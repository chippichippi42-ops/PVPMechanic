package org.twilight.PVPMechanic.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.twilight.PVPMechanic.PVPMechanic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;

/**
 * Manages plugin configuration files
 */
public class ConfigManager {
    
    private final PVPMechanic plugin;
    private YamlConfiguration config;
    private YamlConfiguration statsConfig;
    private YamlConfiguration skillsConfig;
    private YamlConfiguration formulaConfig;
    
    public ConfigManager(PVPMechanic plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Load all configuration files
     */
    public void loadAllConfigs() {
        loadConfig("config.yml");
        loadConfig("stats.yml");
        loadConfig("skills.yml");
        loadConfig("formula.yml");
    }
    
    /**
     * Load a specific configuration file
     * @param fileName Configuration file name
     */
    private void loadConfig(String fileName) {
        File configFile = new File(plugin.getDataFolder(), fileName);
        
        // Create config file if it doesn't exist
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            try (InputStream in = plugin.getResource(fileName)) {
                if (in != null) {
                    Files.copy(in, configFile.toPath());
                } else {
                    // Create empty file with default content
                    YamlConfiguration yaml = new YamlConfiguration();
                    configureDefaults(yaml, fileName);
                    yaml.save(configFile);
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create " + fileName, e);
            }
        }
        
        // Load the configuration
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(configFile);
        
        // Set which config this is
        switch (fileName) {
            case "config.yml":
                config = yaml;
                break;
            case "stats.yml":
                statsConfig = yaml;
                break;
            case "skills.yml":
                skillsConfig = yaml;
                break;
            case "formula.yml":
                formulaConfig = yaml;
                break;
        }
    }
    
    /**
     * Configure default values for a configuration
     * @param yaml YAML configuration
     * @param fileName Configuration file name
     */
    private void configureDefaults(YamlConfiguration yaml, String fileName) {
        switch (fileName) {
            case "config.yml":
                yaml.set("storage.type", "yaml");
                yaml.set("storage.yaml.auto-save-interval", 300);
                yaml.set("storage.sqlite.file", "pvp_data.db");
                yaml.set("storage.mysql.host", "localhost");
                yaml.set("storage.mysql.port", 3306);
                yaml.set("storage.mysql.database", "pvp_mechanic");
                yaml.set("storage.mysql.username", "root");
                yaml.set("storage.mysql.password", "password");
                yaml.set("storage.mysql.pool-size", 10);
                yaml.set("storage.mysql.max-lifetime", 1800000);
                break;
            
            case "stats.yml":
                // Default stat values
                yaml.set("default.DAMAGE", 10.0);
                yaml.set("default.MELEE_DAMAGE", 0.0);
                yaml.set("default.RANGED_DAMAGE", 0.0);
                yaml.set("default.ELEMENTAL_DAMAGE", 0.0);
                yaml.set("default.TRUE_DAMAGE", 0.0);
                yaml.set("default.INCREASE_DAMAGE", 0.0);
                yaml.set("default.ARMOR", 5.0);
                yaml.set("default.TRUE_DEFENSE", 0.0);
                yaml.set("default.DAMAGE_REDUCTION", 0.0);
                yaml.set("default.REDUCE_FALL_DAMAGE", 0.0);
                yaml.set("default.REDUCE_ELEMENTAL_DAMAGE", 0.0);
                yaml.set("default.REDUCE_MELEE_DAMAGE", 0.0);
                yaml.set("default.REDUCE_RANGED_DAMAGE", 0.0);
                yaml.set("default.CRITICAL_DAMAGE_REDUCTION", 0.0);
                yaml.set("default.PENETRATION", 0.0);
                yaml.set("default.CRIT_RATE", 5.0);
                yaml.set("default.CRIT_DAMAGE", 50.0);
                yaml.set("default.DAMAGE_REBOUND", 0.0);
                yaml.set("default.LIFESTEAL", 0.0);
                yaml.set("default.LIFESTEAL_REDUCTION", 0.0);
                yaml.set("default.REDUCE_NEGATIVE_EFFECT", 0.0);
                yaml.set("default.BLOCK_CHANCE", 10.0);
                yaml.set("default.BLOCK_EFFICIENCY", 50.0);
                yaml.set("default.THUNDER_CHANCE", 5.0);
                yaml.set("default.ICE_CHANCE", 5.0);
                yaml.set("default.IGNITE_CHANCE", 5.0);
                yaml.set("default.ADDITIONAL_ATTACK_DAMAGE", 30.0);
                yaml.set("default.ADDITIONAL_ATTACK_CHANCE", 10.0);
                break;
            
            case "skills.yml":
                yaml.set("enabled.BLOCK", true);
                yaml.set("enabled.THUNDER", true);
                yaml.set("enabled.ICE", true);
                yaml.set("enabled.IGNITE", true);
                yaml.set("enabled.ADDITIONAL_ATTACK", true);
                
                yaml.set("block.chance", 10.0);
                yaml.set("block.efficiency", 50.0);
                
                yaml.set("thunder.chance", 5.0);
                yaml.set("thunder.damage_multiplier", 1.5);
                
                yaml.set("ice.chance", 5.0);
                yaml.set("ice.duration", 1.0);
                
                yaml.set("ignite.chance", 5.0);
                yaml.set("ignite.duration", 5.0);
                yaml.set("ignite.damage_per_tick", 1.0);
                
                yaml.set("additional_attack.chance", 10.0);
                yaml.set("additional_attack.damage_percentage", 30.0);
                break;
            
            case "formula.yml":
                yaml.set("damage.base_multiplier", 1.0);
                yaml.set("damage.armor_reduction_factor", 0.04);
                yaml.set("damage.crit_multiplier", 1.5);
                yaml.set("damage.penetration_factor", 0.01);
                break;
        }
    }
    
    // Getter methods for configurations
    public YamlConfiguration getConfig() {
        return config;
    }
    
    public YamlConfiguration getStatsConfig() {
        return statsConfig;
    }
    
    public YamlConfiguration getSkillsConfig() {
        return skillsConfig;
    }
    
    public YamlConfiguration getFormulaConfig() {
        return formulaConfig;
    }
    
    /**
     * Save all configurations
     */
    public void saveAllConfigs() {
        saveConfig(config, "config.yml");
        saveConfig(statsConfig, "stats.yml");
        saveConfig(skillsConfig, "skills.yml");
        saveConfig(formulaConfig, "formula.yml");
    }
    
    /**
     * Save a specific configuration
     * @param yaml YAML configuration
     * @param fileName File name
     */
    private void saveConfig(YamlConfiguration yaml, String fileName) {
        if (yaml == null) return;
        
        try {
            yaml.save(new File(plugin.getDataFolder(), fileName));
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save " + fileName, e);
        }
    }
}