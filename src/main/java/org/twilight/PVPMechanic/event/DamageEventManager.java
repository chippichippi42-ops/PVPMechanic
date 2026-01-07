package org.twilight.PVPMechanic.event;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.twilight.PVPMechanic.PVPMechanic;
import org.twilight.PVPMechanic.api.event.DamageEvent;

/**
 * Manages damage events and intercepts vanilla damage calculation
 */
public class DamageEventManager implements Listener {
    
    private final PVPMechanic plugin;
    
    public DamageEventManager(PVPMechanic plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Intercept vanilla damage events and replace with custom calculation
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Only handle player vs player damage
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player defender = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();
        
        // Determine attack type
        String attackType = determineAttackType(attacker, defender);
        
        // Calculate custom damage
        DamageEvent customEvent = plugin.getDamageCalculator().calculateDamage(
            attacker, defender, event.getDamage(), attackType
        );
        
        // Apply the custom damage
        event.setDamage(customEvent.getFinalDamage());
        
        // Cancel the event if needed
        if (customEvent.isCancelled()) {
            event.setCancelled(true);
        }
        
        // Fire the custom event to API listeners
        plugin.getAPI().fireDamageEvent(customEvent);
    }
    
    /**
     * Determine the attack type (MELEE or RANGED)
     */
    private String determineAttackType(Player attacker, Player defender) {
        // In a real implementation, this would check the weapon type, distance, etc.
        // For now, we'll use a simple heuristic
        
        double distance = attacker.getLocation().distance(defender.getLocation());
        
        if (distance > 4.0) {
            return "RANGED";
        } else {
            return "MELEE";
        }
    }
}