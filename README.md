# EnderBow

EnderBow is a small Paper/Spigot plugin that gives players a configurable bow which shoots Ender Pearls instead of arrows.

Features
- Java 17+ and Paper 1.21.x compatible
- PersistentDataContainer tag to reliably identify the custom bow
- Configurable display name, lore, and hotbar slot
- Per-player cooldown to prevent spam
- Optionally makes the bow unbreakable and prevents durability loss
- /enderbow reload to reload config and re-give to online players

Build
- Requirements: Java 17+, Maven
- Build: mvn clean package
- Drop the generated JAR from target/ into your server's plugins/ folder

Config
Edit src/main/resources/config.yml (or plugins/EnderBow/config.yml after first run):
- name: display name (supports & color codes)
- lore: list of lore lines
- slot: hotbar slot (0-8)
- give-on-join: give the bow when players join
- cooldown-seconds: cooldown between uses
- unbreakable: make the bow unbreakable

Permissions
- enderbow.reload (default: op)

License
MIT (see LICENSE file)
