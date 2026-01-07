package org.twilight.PVPMechanic.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.twilight.PVPMechanic.PVPMechanic;
import org.twilight.PVPMechanic.api.stats.PlayerStats;
import org.twilight.PVPMechanic.api.stats.StatType;

/**
 * Main command handler for PVPMechanic
 */
public class PVPMechanicCommand implements CommandExecutor {
    
    private final PVPMechanic plugin;
    
    public PVPMechanicCommand(PVPMechanic plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            showHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "stats":
                showStats(player);
                break;
            case "reload":
                if (player.hasPermission("pvpmechanic.admin")) {
                    reloadConfig(player);
                } else {
                    player.sendMessage("§cYou don't have permission for this command.");
                }
                break;
            case "reset":
                if (player.hasPermission("pvpmechanic.admin")) {
                    resetStats(player);
                } else {
                    player.sendMessage("§cYou don't have permission for this command.");
                }
                break;
            default:
                showHelp(player);
        }
        
        return true;
    }
    
    private void showHelp(Player player) {
        player.sendMessage("§6§lPVPMechanic §7- §fComprehensive PVP System");
        player.sendMessage("§7/pvp stats §f- View your combat statistics");
        player.sendMessage("§7/pvp reload §f- Reload configuration (Admin)");
        player.sendMessage("§7/pvp reset §f- Reset your stats (Admin)");
    }
    
    private void showStats(Player player) {
        PlayerStats stats = plugin.getStatManager().getPlayerStats(player);
        
        player.sendMessage("§6§lYour Combat Statistics");
        player.sendMessage("§7Damage: §f" + String.format("%.1f", stats.getStat(StatType.DAMAGE)) + "%");
        player.sendMessage("§7Armor: §f" + String.format("%.1f", stats.getStat(StatType.ARMOR)));
        player.sendMessage("§7Crit Rate: §f" + String.format("%.1f", stats.getStat(StatType.CRIT_RATE)) + "%");
        player.sendMessage("§7Crit Damage: §f" + String.format("%.1f", stats.getStat(StatType.CRIT_DAMAGE)) + "%");
        player.sendMessage("§7Lifesteal: §f" + String.format("%.1f", stats.getStat(StatType.LIFESTEAL)) + "%");
        player.sendMessage("§7Penetration: §f" + String.format("%.1f", stats.getStat(StatType.PENETRATION)) + "%");
    }
    
    private void reloadConfig(Player player) {
        plugin.getConfigManager().loadAllConfigs();
        player.sendMessage("§aConfiguration reloaded successfully!");
    }
    
    private void resetStats(Player player) {
        PlayerStats stats = plugin.getStatManager().getPlayerStats(player);
        stats.resetToDefaults();
        plugin.getStatManager().savePlayerStats(player, stats);
        player.sendMessage("§aYour stats have been reset to default values!");
    }
}