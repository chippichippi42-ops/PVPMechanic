package org.twightlight.PVPMechanic.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.twightlight.PVPMechanic.PVPMechanic;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private final PVPMechanic plugin;
    private FileConfiguration mainConfig;
    private FileConfiguration statsConfig;
    private FileConfiguration skillsConfig;
    private FileConfiguration formulaConfig;

    public ConfigManager(PVPMechanic plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    public void loadConfigs() {
        plugin.saveDefaultConfig();
        mainConfig = plugin.getConfig();

        statsConfig = loadConfig("stats.yml");
        skillsConfig = loadConfig("skills.yml");
        formulaConfig = loadConfig("formula.yml");
    }

    private FileConfiguration loadConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getMainConfig() {
        return mainConfig;
    }

    public FileConfiguration getStatsConfig() {
        return statsConfig;
    }

    public FileConfiguration getSkillsConfig() {
        return skillsConfig;
    }

    public FileConfiguration getFormulaConfig() {
        return formulaConfig;
    }
}
