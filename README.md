# EnderBow

EnderBow is a Paper/Spigot plugin that gives players a configurable bow which shoots Ender Pearls instead of arrows.

Features
- Java 17+ and Paper 1.21.x compatible
- PersistentDataContainer tag to reliably identify the custom bow
- Configurable display name, lore, and hotbar slot
- Per-player cooldown to prevent spam
- Optionally makes the bow unbreakable and prevents durability loss
- Configurable velocity multiplier for ender pearls
- Separate forward and vertical multipliers for velocity
- Action bar cooldown message (toggleable)
- Hex color support (#rrggbb) and & color codes
- /enderbow reload to reload config and re-give to online players
- /enderbow give <player> to give the bow to a player
- Permission enderbow.bypass to skip cooldowns

Build
- Requirements: Java 17+, Maven
- Build: mvn clean package
- Drop the generated JAR from target/ into your server's plugins/ folder

Permissions
- enderbow.reload (default: op)
- enderbow.give (default: op)
- enderbow.bypass (default: false)

License
MIT (see LICENSE file)
