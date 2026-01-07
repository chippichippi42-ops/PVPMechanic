package org.twightlight.PVPMechanic;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.twightlight.PVPMechanic.api.PVPMechanicAPI;
import org.twightlight.PVPMechanic.api.PVPMechanicAPIImpl;
import org.twightlight.PVPMechanic.config.ConfigManager;
import org.twightlight.PVPMechanic.damage.DamageManager;
import org.twightlight.PVPMechanic.stats.StatsManager;
import org.twightlight.PVPMechanic.storage.StorageManager;
import org.twightlight.PVPMechanic.skill.SkillManager;
import org.twightlight.PVPMechanic.placeholder.PVPPlaceholderExpansion;
import org.bukkit.Bukkit;

public class PVPMechanic extends JavaPlugin {

    @Getter
    private static PVPMechanic instance;
    @Getter
    private static PVPMechanicAPI API;

    @Getter
    private ConfigManager configManager;
    @Getter
    private StatsManager statsManager;
    @Getter
    private DamageManager damageManager;
    @Getter
    private StorageManager storageManager;
    @Getter
    private SkillManager skillManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize Managers
        this.configManager = new ConfigManager(this);
        this.storageManager = new StorageManager(this);
        this.statsManager = new StatsManager(this);
        this.skillManager = new SkillManager(this);
        this.damageManager = new DamageManager(this);

        API = new PVPMechanicAPIImpl(this);

        // Register Command
        getCommand("pvp").setExecutor((sender, command, label, args) -> {
            if (args.length > 0 && args[0].equalsIgnoreCase("stats")) {
                sender.sendMessage("§aPVPMechanic Performance Stats:");
                sender.sendMessage("§7Avg Damage Calc Time: §f" + String.format("%.4f", damageManager.getAverageCalculationTimeMs()) + "ms");
                return true;
            }
            return false;
        });

        // PlaceholderAPI integration
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PVPPlaceholderExpansion(this).register();
        }

        getLogger().info("PVPMechanic has been enabled!");
    }

    @Override
    public void onDisable() {
        if (storageManager != null) {
            storageManager.shutdown();
        }
        getLogger().info("PVPMechanic has been disabled!");
    }
}
