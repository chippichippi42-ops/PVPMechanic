package org.twightlight.PVPMechanic.util;

import org.twightlight.PVPMechanic.api.StatType;
import java.util.Arrays;

public class FastStatMap {
    private final double[] values;

    public FastStatMap() {
        this.values = new double[StatType.values().length];
        for (StatType type : StatType.values()) {
            values[type.ordinal()] = type.getDefaultValue();
        }
    }

    public double get(StatType type) {
        return values[type.ordinal()];
    }

    public void set(StatType type, double value) {
        values[type.ordinal()] = value;
    }

    public void add(StatType type, double value) {
        values[type.ordinal()] += value;
    }
}
