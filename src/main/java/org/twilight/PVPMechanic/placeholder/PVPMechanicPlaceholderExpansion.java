package org.twilight.PVPMechanic.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.twilight.PVPMechanic.PVPMechanic;
import org.twilight.PVPMechanic.api.stats.StatType;

/**
 * PlaceholderAPI expansion for PVPMechanic
 */
public class PVPMechanicPlaceholderExpansion extends PlaceholderExpansion {
    
    private final PVPMechanic plugin;
    
    public PVPMechanicPlaceholderExpansion(PVPMechanic plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getIdentifier() {
        return "pvp";
    }
    
    @Override
    public String getAuthor() {
        return "Twilight";
    }
    
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }
        
        // Handle specific stat placeholders
        try {
            StatType statType = StatType.valueOf(identifier.toUpperCase());
            return plugin.getPlaceholderManager().getStatPlaceholder(player, statType);
        } catch (IllegalArgumentException e) {
            // Not a standard stat, check for custom stats
            return plugin.getPlaceholderManager().getStatPlaceholder(player, identifier);
        }
    }
}