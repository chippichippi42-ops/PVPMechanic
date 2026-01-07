package org.twightlight.PVPMechanic.api;

import java.util.UUID;

public interface PlayerStats {
    UUID getPlayerId();
    double getStat(StatType type);
    double getStat(String customStat);
    void setStat(StatType type, double value);
    void setStat(String customStat, double value);
    void addStat(StatType type, double value);
    void addStat(String customStat, double value);
}
