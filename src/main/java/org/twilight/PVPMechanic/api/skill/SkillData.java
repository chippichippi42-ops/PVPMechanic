package org.twilight.PVPMechanic.api.skill;

/**
 * Data class representing skill information
 */
public class SkillData {
    private final String skillName;
    private final SkillType skillType;
    private final boolean triggered;
    private final Object config;
    
    public SkillData(String skillName, SkillType skillType, boolean triggered, Object config) {
        this.skillName = skillName;
        this.skillType = skillType;
        this.triggered = triggered;
        this.config = config;
    }
    
    public String getSkillName() {
        return skillName;
    }
    
    public SkillType getSkillType() {
        return skillType;
    }
    
    public boolean isTriggered() {
        return triggered;
    }
    
    public Object getConfig() {
        return config;
    }
}