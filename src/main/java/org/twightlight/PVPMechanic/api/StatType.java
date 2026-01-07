package org.twightlight.PVPMechanic.api;

import java.util.HashMap;
import java.util.Map;

public enum StatType {
    // Damage Modifiers
    GENERIC_DAMAGE("generic_damage", 0.0),
    MELEE_DAMAGE("melee_damage", 0.0),
    RANGED_DAMAGE("ranged_damage", 0.0),
    ELEMENTAL_DAMAGE("elemental_damage", 0.0),
    TRUE_DAMAGE("true_damage", 0.0),
    INCREASE_DAMAGE("increase_damage", 0.0), // Percentage

    // Defense Modifiers
    GENERIC_ARMOR("generic_armor", 0.0),
    TRUE_DEFENSE("true_defense", 0.0),
    DAMAGE_REDUCTION("damage_reduction", 0.0), // Percentage

    // Specialized Protections
    REDUCE_FALL_DAMAGE("reduce_fall_damage", 0.0),
    REDUCE_ELEMENTAL_DAMAGE("reduce_elemental_damage", 0.0),
    REDUCE_MELEE_DAMAGE("reduce_melee_damage", 0.0),
    REDUCE_RANGED_DAMAGE("reduce_ranged_damage", 0.0),
    CRITICAL_DAMAGE_REDUCTION("critical_damage_reduction", 0.0),

    // Combat Effects
    PENETRATION("penetration", 0.0),
    CRIT_RATE("crit_rate", 0.0),
    CRIT_DAMAGE("crit_damage", 1.5), // Multiplier
    DAMAGE_REBOUND("damage_rebound", 0.0),
    LIFESTEAL("lifesteal", 0.0),
    LIFESTEAL_REDUCTION("lifesteal_reduction", 0.0),

    // Effect Modifiers
    REDUCE_NEGATIVE_EFFECT("reduce_negative_effect", 0.0), // 0-100%

    // Skill-related stats
    BLOCK_CHANCE("block_chance", 0.0),
    BLOCK_EFFICIENCY("block_efficiency", 0.0),
    THUNDER_CHANCE("thunder_chance", 0.0),
    ICE_CHANCE("ice_chance", 0.0),
    IGNITE_CHANCE("ignite_chance", 0.0),
    ADDITIONAL_ATTACK_CHANCE("additional_attack_chance", 0.0),
    ADDITIONAL_ATTACK_DAMAGE("additional_attack_damage", 0.0);

    private final String key;
    private final double defaultValue;
    private static final Map<String, StatType> BY_KEY = new HashMap<>();

    static {
        for (StatType type : values()) {
            BY_KEY.put(type.key.toLowerCase(), type);
        }
    }

    StatType(String key, double defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public double getDefaultValue() {
        return defaultValue;
    }

    public static StatType fromKey(String key) {
        return BY_KEY.get(key.toLowerCase());
    }
}
