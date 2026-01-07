package org.twilight.PVPMechanic.skill;

import org.twilight.PVPMechanic.PVPMechanic;
import org.twilight.PVPMechanic.api.skill.SkillType;

import java.util.*;

/**
 * Manages all skills in the system
 */
public class SkillManager {
    
    private final PVPMechanic plugin;
    private final Map<String, Skill> skills = new HashMap<>();
    private final Map<SkillType, List<Skill>> skillsByType = new EnumMap<>(SkillType.class);
    
    public SkillManager(PVPMechanic plugin) {
        this.plugin = plugin;
        registerDefaultSkills();
    }
    
    /**
     * Register default skills
     */
    private void registerDefaultSkills() {
        // Register Block skill (PASSIVE type)
        if (plugin.getConfigManager().getSkillsConfig().getBoolean("enabled.BLOCK", true)) {
            registerSkill(new BlockSkill(plugin));
        }
        
        // Register Thunder skill (MELEE type)
        if (plugin.getConfigManager().getSkillsConfig().getBoolean("enabled.THUNDER", true)) {
            registerSkill(new ThunderSkill(plugin));
        }
        
        // Register Ice skill (MELEE type)
        if (plugin.getConfigManager().getSkillsConfig().getBoolean("enabled.ICE", true)) {
            registerSkill(new IceSkill(plugin));
        }
        
        // Register Ignite skill (MELEE type)
        if (plugin.getConfigManager().getSkillsConfig().getBoolean("enabled.IGNITE", true)) {
            registerSkill(new IgniteSkill(plugin));
        }
        
        // Register Additional Attack skill (MELEE type)
        if (plugin.getConfigManager().getSkillsConfig().getBoolean("enabled.ADDITIONAL_ATTACK", true)) {
            registerSkill(new AdditionalAttackSkill(plugin));
        }
    }
    
    /**
     * Register a skill
     * @param skill The skill to register
     */
    public void registerSkill(Skill skill) {
        skills.put(skill.getName(), skill);
        skillsByType.computeIfAbsent(skill.getType(), k -> new ArrayList<>()).add(skill);
        plugin.getLogger().info("Registered skill: " + skill.getName() + " (" + skill.getType() + ")");
    }
    
    /**
     * Register a custom skill
     * @param skillName Name of the skill
     * @param skillConfig Configuration for the skill
     * @param skillType Type of skill
     */
    public void registerCustomSkill(String skillName, Object skillConfig, SkillType skillType) {
        // For now, we'll create a generic skill
        // In a full implementation, this would use a skill factory
        registerSkill(new GenericSkill(skillName, skillType, skillConfig));
    }
    
    /**
     * Get a skill by name
     * @param skillName The skill name
     * @return Skill instance or null if not found
     */
    public Skill getSkill(String skillName) {
        return skills.get(skillName);
    }
    
    /**
     * Get all skills of a specific type
     * @param skillType The skill type
     * @return List of skills of that type
     */
    public List<Skill> getSkillsByType(SkillType skillType) {
        return skillsByType.getOrDefault(skillType, Collections.emptyList());
    }
    
    /**
     * Get all registered skills
     * @return Map of all skills
     */
    public Map<String, Skill> getAllSkills() {
        return Collections.unmodifiableMap(skills);
    }
}