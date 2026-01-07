package org.twilight.PVPMechanic;

import org.bukkit.plugin.java.JavaPlugin;
import org.twilight.PVPMechanic.api.PVPMechanicAPI;
import org.twilight.PVPMechanic.api.PVPMechanicAPIImpl;
import org.twilight.PVPMechanic.config.ConfigManager;
import org.twilight.PVPMechanic.damage.DamageCalculator;
import org.twilight.PVPMechanic.event.DamageEventManager;
import org.twilight.PVPMechanic.skill.SkillManager;
import org.twilight.PVPMechanic.stats.StatManager;
import org.twilight.PVPMechanic.storage.StorageManager;

/**
 * Main plugin class for PVPMechanic
 */
public class PVPMechanic extends JavaPlugin {
    
    private static PVPMechanic instance;
    private PVPMechanicAPI api;
    
    private ConfigManager configManager;
    private StorageManager storageManager;
    private StatManager statManager;
    private SkillManager skillManager;
    private DamageCalculator damageCalculator;
    private DamageEventManager damageEventManager;
    private PlaceholderManager placeholderManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.storageManager = new StorageManager(this);
        this.statManager = new StatManager(this);
        this.skillManager = new SkillManager(this);
        this.damageCalculator = new DamageCalculator(this);
        this.damageEventManager = new DamageEventManager(this);
        this.placeholderManager = new PlaceholderManager(this);
        
        // Initialize API
        this.api = new PVPMechanicAPIImpl(this);
        
        // Load configuration
        configManager.loadAllConfigs();
        
        // Initialize storage
        storageManager.initialize();
        
        // Initialize PlaceholderAPI
        placeholderManager.initialize();
        
        // Register listeners
        registerListeners();
        
        getLogger().info("PVPMechanic has been enabled!");
    }
    
    @Override
    public void onDisable() {
        if (storageManager != null) {
            storageManager.shutdown();
        }
        getLogger().info("PVPMechanic has been disabled!");
    }
    
    private void registerListeners() {
        // Register damage event listeners
        getServer().getPluginManager().registerEvents(damageEventManager, this);
        
        // Register command
        getCommand("pvpmechanic").setExecutor(new PVPMechanicCommand(this));
    }
    
    /**
     * Get the API instance
     * @return PVPMechanicAPI instance
     */
    public static PVPMechanicAPI getAPI() {
        if (instance == null) {
            throw new IllegalStateException("Plugin not initialized");
        }
        return instance.api;
    }
    
    /**
     * Get the plugin instance
     * @return PVPMechanic instance
     */
    public static PVPMechanic getInstance() {
        return instance;
    }
    
    // Getter methods for managers
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public StorageManager getStorageManager() {
        return storageManager;
    }
    
    public StatManager getStatManager() {
        return statManager;
    }
    
    public SkillManager getSkillManager() {
        return skillManager;
    }
    
    public DamageCalculator getDamageCalculator() {
        return damageCalculator;
    }
    
    public DamageEventManager getDamageEventManager() {
        return damageEventManager;
    }
    
    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }
}