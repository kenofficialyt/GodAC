<div align="center">
  <h1>GodAC</h1>

  <div>
   <a href="https://github.com/kenofficialyt/GodAC/releases">
    <img alt="GitHub Release" src="https://img.shields.io/github/v/release/kenofficialyt/GodAC?style=flat&logo=github">
   </a>&nbsp;&nbsp;
   <a href="https://github.com/kenofficialyt/GodAC/releases">
    <img alt="Downloads" src="https://img.shields.io/github/downloads/kenofficialyt/GodAC/total?style=flat&logo=github&label=downloads">
   </a>&nbsp;&nbsp;
   <a href="https://discord.gg/">
    <img alt="Discord" src="https://img.shields.io/discord/811396969670901800?style=flat&label=discord&logo=discord">
   </a>
  </div>
  <br>
</div>

## What is GodAC?

GodAC is an advanced Minecraft anti-cheat plugin designed to detect and prevent cheating on your server. Built on the foundation of GrimAC, it provides comprehensive protection against various types of cheats and exploits.

## Features

### Movement Checks
- **Flight Detection** - Detects flying hacks and空中hack
- **Speed Detection** - Casts speed hacks and invalid movement
- **NoSlow Detection** - Prevents NoSlow cheating
- **Jesus/Water Walk** - Detects water walking hacks
- **Phase Detection** - Blocks players going through walls

### Combat Checks
- **Reach Detection** - Detects extended reach combat hacks
- **Hitbox Detection** - Casts hitbox manipulation
- **Multi-Action Detection** - Prevents fast clicking and combo exploits

### Scaffold/Scaffolding
- **Scaffold Detection** - Blocks scaffold walk/wallhack
- **Rotation Detection** - Detects invalid block placement rotations

### Packet & Timing Checks
- **Timer Detection** - Catches timer mods that speed up gameplay
- **Packet Order** - Validates packet sequence integrity
- **Bad Packets** - Filters malformed/malicious packets

### Anti-Spoof
- **Vanilla Spoof Detection** - Detects players claiming to be vanilla but using mods
- **Brand Spoofing** - Identifies clients trying to hide their true identity
- **Geyser Spoof Detection** - Prevents Java players from pretending to be Bedrock

### Additional Protection
- **Exploit Mitigations** - Protection against various server exploits
- **Crash Prevention** - Blocks crashers and malicious packets
- **Elytra Checks** - Detects elytra flight hacks
- **Vehicle Exploits** - Prevents vehicle-based cheats

## Supported Server Versions

- Minecraft 1.8 - 1.21+
- Spigot / Paper / Folia

## Requirements

- Java 17 or higher
- Spigot, Paper, or Folia server

## Installation

1. Download the latest JAR from [Releases](https://github.com/kenofficialyt/GodAC/releases)
2. Place the JAR in your server's `plugins` folder
3. Restart your server
4. Configure in `plugins/GodAC/config.yml`

## Configuration

The plugin automatically generates a configuration file. Key settings:

```yaml
# Enable/disable specific checks
checks:
  flight: true
  speed: true
  reach: true
  # ... more checks

# Setback actions
setback: true

# Alert settings
alerts:
  enabled: true
  staff-only: true
```

## Commands

| Command | Description |
|---------|-------------|
| `/godac` | Main plugin command |
| `/godac check <player>` | Check a player's violations |
| `/godac reload` | Reload configuration |
| `/godac alerts` | Toggle alerts |
| `/godac verbose <player>` | View detailed player data |

### AntiSpoof Commands

| Command | Description |
|---------|-------------|
| `/antispoof check [player]` | Check player spoof status |
| `/antispoof toggle` | Enable/disable AntiSpoof |
| `/antispoof debug` | Toggle debug mode |

## Permissions

| Permission | Description |
|------------|-------------|
| `godac.alerts` | Receive anti-cheat alerts |
| `godac.bypass` | Bypass all checks |
| `godac.check` | Use check commands |
| `godac.exempt` | Exempt from checks |
| `godac.antispoof` | Use AntiSpoof commands |

## Building

```bash
./gradlew build
```

The built JAR will be in `bukkit/build/libs/`

## License

This project is licensed under GNU-3.0. See [LICENSE](LICENSE) for details.

## Credits

- Based on [GrimAC](https://github.com/GrimAnticheat/Grim) by GrimAnticheat
- AntiSpoof module inspired by [AntiSpoof](https://github.com/GigaZelensky/AntiSpoof)

## Support

For issues and feature requests, please open an issue on GitHub.
