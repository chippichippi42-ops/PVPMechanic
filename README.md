# PVPMechanic - Comprehensive Minecraft PVP Mechanics System

A powerful, extensible Paper plugin that replaces Minecraft's vanilla damage system with a highly customizable PVP mechanics framework.

## Features

### Characteristic Stats System
- **Damage Modifiers**: generic damage, melee damage, ranged damage, elemental damage, true damage
- **Defense Modifiers**: generic armor, true defense, damage reduction
- **Specialized Protections**: fall damage reduction, elemental damage reduction, etc.
- **Combat Effects**: penetration, crit rate, crit damage, damage rebound, lifesteal
- **Effect Modifiers**: reduce negative effect duration

### Skills & Passive Abilities
- **Block Mechanics**: Chance-based damage absorption
- **Thunder Mechanics**: Additional lightning damage on melee hits
- **Ice Mechanics**: Apply slow effect on melee hits
- **Ignite Mechanics**: Fire damage over time on melee hits
- **Additional Attack**: Follow-up attack chains
- **Modular Skill System**: Easy to add custom skills with attack type filtering

### Custom Damage Formula
- Completely replaces vanilla damage calculation
- Modular calculation pipeline
- Support for multiple damage types
- Critical hit system
- Skill integration with attack type filtering

### Developer API
```java
// Get API instance
PVPMechanicAPI api = PVPMechanic.getAPI();

// Query player stats
PlayerStats stats = api.getPlayerStats(player);
stats.addStat(StatType.CRIT_RATE, 10);

// Listen to damage events
api.onDamage(event -> {
  if (event.isCritical()) {
    event.setDamage(event.getFinalDamage() * 1.1);
  }
});

// Register custom skills
api.registerSkill("MySkill", skillConfig, SkillType.MELEE);
```

### Storage System
- **YAML Storage**: Simple file-based persistence
- **SQLite Support**: Embedded database option
- **MySQL Support**: Remote database for large networks
- Async operations to prevent blocking
- Automatic data loading/saving

### Configuration
- YAML-based configuration files
- Easy stat balancing
- Skill enable/disable toggles
- Formula coefficients

### PlaceholderAPI Integration
- `%pvp_damage%`, `%pvp_armor%`, `%pvp_crit_rate%`, etc.
- Support for custom stats
- Dynamic placeholder registration

## Installation

1. Download the latest plugin jar
2. Place it in your server's `plugins/` directory
3. Restart your server
4. Configure the plugin via `plugins/PVPMechanic/config.yml`

## Commands

- `/pvp stats` - View your combat statistics
- `/pvp reload` - Reload configuration (Admin)
- `/pvp reset` - Reset your stats (Admin)

## Configuration

The plugin creates several configuration files on first run:

- `config.yml` - Main plugin settings and storage configuration
- `stats.yml` - Default stat values and balancing
- `skills.yml` - Skill configuration and enable/disable toggles
- `formula.yml` - Damage formula coefficients

## Development

### Building
```bash
gradle build
```

### Adding Custom Skills
```java
// Register a custom skill
api.registerSkill("Fireball", fireballConfig, SkillType.RANGED);
```

### Adding Custom Stats
```java
// Register a custom stat type
api.registerCustomStat(StatType.CUSTOM_STAT, 0.0);
```

## Compatibility

- **Minecraft Versions**: 1.8.9 ~ 1.21.10
- **Java Version**: 11+
- **Server Type**: Paper
- **Dependencies**: None required (PlaceholderAPI optional)

## Performance

- Object pooling for damage events
- Intelligent stat caching
- Async database operations
- Lock-free patterns for high concurrency
- Performance metrics and debugging tools

## License

MIT License - See LICENSE file for details.

## Support

For issues, feature requests, or questions, please open an issue on the GitHub repository.

## Contributing

Contributions are welcome! Please follow the existing code style and submit pull requests.

## Roadmap

- [x] Core damage calculation system
- [x] Basic skill implementations
- [x] Stat system with persistence
- [x] Developer API
- [x] PlaceholderAPI integration
- [ ] Advanced performance optimizations
- [ ] SQLite storage implementation
- [ ] MySQL storage implementation
- [ ] Web-based admin interface
- [ ] Advanced skill chaining system
- [ ] GUI configuration tools