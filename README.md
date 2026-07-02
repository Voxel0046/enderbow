# EnderBow

EnderBow is a small Paper/Spigot plugin that gives players a configurable bow which shoots Ender Pearls instead of arrows.

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

Config
Edit src/main/resources/config.yml (or plugins/EnderBow/config.yml after first run):
- name: display name (supports & color codes and #rrggbb hex codes)
- lore: list of lore lines
- slot: hotbar slot (0-8)
- give-on-join: give the bow when players join
- cooldown-seconds: cooldown between uses
- unbreakable: make the bow unbreakable
- velocity-multiplier: multiply pearl speed
- forward-multiplier: multiplier for forward (XZ) components
- vertical-multiplier: multiplier for vertical (Y) component
- actionbar-cooldown-message: toggle actionbar cooldown message

Permissions
- enderbow.reload (default: op)
- enderbow.give (default: op)
- enderbow.bypass (default: false)

License
MIT (see LICENSE file)
