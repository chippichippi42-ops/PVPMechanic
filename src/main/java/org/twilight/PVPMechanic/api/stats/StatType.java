package org.twilight.PVPMechanic.api.stats;

/**
 * Enum representing all available stat types
 */
public enum StatType {
    // Damage modifiers
    DAMAGE,
    MELEE_DAMAGE,
    RANGED_DAMAGE,
    ELEMENTAL_DAMAGE,
    TRUE_DAMAGE,
    INCREASE_DAMAGE,
    
    // Defense modifiers
    ARMOR,
    TRUE_DEFENSE,
    DAMAGE_REDUCTION,
    
    // Specialized protections
    REDUCE_FALL_DAMAGE,
    REDUCE_ELEMENTAL_DAMAGE,
    REDUCE_MELEE_DAMAGE,
    REDUCE_RANGED_DAMAGE,
    CRITICAL_DAMAGE_REDUCTION,
    
    // Combat effects
    PENETRATION,
    CRIT_RATE,
    CRIT_DAMAGE,
    DAMAGE_REBOUND,
    LIFESTEAL,
    LIFESTEAL_REDUCTION,
    
    // Effect modifiers
    REDUCE_NEGATIVE_EFFECT,
    
    // Skill-related stats
    BLOCK_CHANCE,
    BLOCK_EFFICIENCY,
    THUNDER_CHANCE,
    ICE_CHANCE,
    IGNITE_CHANCE,
    ADDITIONAL_ATTACK_DAMAGE,
    ADDITIONAL_ATTACK_CHANCE
}