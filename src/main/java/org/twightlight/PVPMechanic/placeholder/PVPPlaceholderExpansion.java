package org.twightlight.PVPMechanic.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.twightlight.PVPMechanic.PVPMechanic;
import org.twightlight.PVPMechanic.api.PlayerStats;
import org.twightlight.PVPMechanic.api.StatType;

public class PVPPlaceholderExpansion extends PlaceholderExpansion {

    private final PVPMechanic plugin;

    public PVPPlaceholderExpansion(PVPMechanic plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "pvp";
    }

    @Override
    public @NotNull String getAuthor() {
        return "twightlight";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null || !player.isOnline()) {
            return "";
        }

        Player onlinePlayer = player.getPlayer();
        PlayerStats stats = plugin.getStatsManager().getPlayerStats(onlinePlayer);

        StatType type = StatType.fromKey(params);
        if (type != null) {
            return String.valueOf(stats.getStat(type));
        }

        // Check custom stats
        return String.valueOf(stats.getStat(params));
    }
}
