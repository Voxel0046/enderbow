package com.voxel.enderbow;

import org.bukkit.NamespacedKey;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EnderBowListener implements Listener {

    private final EnderBowPlugin plugin;
    private ItemStack configuredBow;
    private final NamespacedKey key;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<Integer, UUID> pearls = new ConcurrentHashMap<>(); // Track pearl entity IDs to their shooter
    private final Map<Integer, BukkitTask> pearlTrails = new ConcurrentHashMap<>(); // Track particle trail tasks

    public EnderBowListener(EnderBowPlugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "enderbow");
        this.configuredBow = createConfiguredBow();
    }

    private ItemStack createConfiguredBow() {
        EnderBowConfig cfg = plugin.getEbConfig();
        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta meta = bow.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(cfg.getDisplayName());
            meta.setLore(new ArrayList<>(cfg.getLore()));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            // mark via PersistentDataContainer so identification is robust
            meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte)1);
            // optional: make unbreakable if requested
            meta.setUnbreakable(cfg.isUnbreakable());
            if (cfg.isUnbreakable()) {
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            }
            bow.setItemMeta(meta);
        }
        return bow;
    }

    public void refreshConfiguredBow() {
        this.configuredBow = createConfiguredBow();
    }

    public void giveBowTo(Player player) {
        // Recreate in case config changed
        configuredBow = createConfiguredBow();
        int slot = plugin.getEbConfig().getSlot();
        player.getInventory().setItem(slot, configuredBow);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.getEbConfig().isGiveOnJoin()) {
            giveBowTo(event.getPlayer());
        }
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        ItemStack bow = event.getBow();
        if (bow == null || bow.getType() != Material.BOW) return;
        if (!bow.hasItemMeta()) return;

        ItemMeta meta = bow.getItemMeta();
        if (meta == null) return;
        Byte tag = meta.getPersistentDataContainer().get(key, PersistentDataType.BYTE);
        if (tag == null || tag != (byte)1) return;

        // bypass permission check
        if (player.hasPermission("enderbow.bypass")) {
            // allow firing without cooldown
        } else {
            // cooldown check
            double cooldownSeconds = plugin.getEbConfig().getCooldownSeconds();
            if (cooldownSeconds > 0) {
                UUID id = player.getUniqueId();
                long now = System.currentTimeMillis();
                long last = cooldowns.getOrDefault(id, 0L);
                long cooldownMillis = (long)(cooldownSeconds * 1000.0);
                if (now - last < cooldownMillis) {
                    // still on cooldown: cancel the shot and optionally notify
                    event.setCancelled(true);
                    long remaining = (cooldownMillis - (now - last) + 999) / 1000;
                    String msg = plugin.getEbConfig().getCooldownMessage()
                        .replace("%s", String.valueOf(remaining));
                    if (plugin.getEbConfig().isActionbarCooldownMessage()) {
                        // send as action bar
                        try {
                            player.sendActionBar(msg);
                        } catch (NoSuchMethodError | NoClassDefFoundError ex) {
                            // fallback
                            player.sendMessage(msg);
                        }
                    } else {
                        player.sendMessage(msg);
                    }
                    return;
                }
                // record use
                cooldowns.put(id, now);
            }
        }

        // Remove the arrow projectile (if any) and spawn ender pearl with similar velocity
        if (event.getProjectile() != null) {
            event.getProjectile().remove();
        }
        // calculate velocity from force and player's eye direction as fallback
        float force = event.getForce(); // 0..1
        double base = Math.max(1.0, force * 2.0) * plugin.getEbConfig().getVelocityMultiplier();
        double forward = plugin.getEbConfig().getForwardMultiplier();
        double vertical = plugin.getEbConfig().getVerticalMultiplier();

        Vector dir = player.getEyeLocation().getDirection();
        double vx = dir.getX() * base * forward;
        double vz = dir.getZ() * base * forward;
        double vy = dir.getY() * base * vertical;

        Vector velocity = new Vector(vx, vy, vz);

        EnderPearl pearl = (EnderPearl) player.getWorld().spawnEntity(player.getEyeLocation(), EntityType.ENDER_PEARL);
        pearl.setShooter(player);
        pearl.setVelocity(velocity);

        // Track pearl for effects on teleport
        pearls.put(pearl.getEntityId(), player.getUniqueId());

        // Play throw effects
        playThrowEffects(player);
        
        // Start particle trail for the pearl
        startPearlTrail(pearl);

        // cancel original shoot event to avoid arrow consumption visuals
        event.setCancelled(true);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof EnderPearl)) return;

        EnderPearl pearl = (EnderPearl) event.getEntity();
        int entityId = pearl.getEntityId();
        
        UUID shooterUUID = pearls.remove(entityId);
        
        // Cancel the particle trail task
        BukkitTask task = pearlTrails.remove(entityId);
        if (task != null) {
            task.cancel();
        }

        if (shooterUUID != null) {
            Player shooter = org.bukkit.Bukkit.getPlayer(shooterUUID);
            if (shooter != null) {
                playTeleportEffects(pearl.getLocation());
            }
        }
    }

    private void startPearlTrail(EnderPearl pearl) {
        EnderBowConfig cfg = plugin.getEbConfig();
        EnderBowConfig.EffectsConfig effects = cfg.getEffectsConfig();
        
        // Check if trail particles are enabled
        if (!effects.getTrailParticles().isEnabled()) {
            return;
        }
        
        int entityId = pearl.getEntityId();
        
        try {
            Particle particle = Particle.valueOf(effects.getTrailParticles().getParticle());
            
            BukkitTask task = org.bukkit.Bukkit.getScheduler().scheduleSyncRepeatingTask(
                plugin,
                () -> {
                    if (pearl.isDead()) {
                        // Pearl is gone, cancel this task
                        BukkitTask t = pearlTrails.remove(entityId);
                        if (t != null) {
                            t.cancel();
                        }
                    } else {
                        // Spawn particles at pearl location
                        pearl.getWorld().spawnParticle(
                            particle,
                            pearl.getLocation(),
                            effects.getTrailParticles().getCount(),
                            effects.getTrailParticles().getOffsetX(),
                            effects.getTrailParticles().getOffsetY(),
                            effects.getTrailParticles().getOffsetZ(),
                            effects.getTrailParticles().getSpeed()
                        );
                    }
                },
                0,
                1 // Run every tick
            );
            
            pearlTrails.put(entityId, task);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid trail particle: " + effects.getTrailParticles().getParticle());
        }
    }

    private void playThrowEffects(Player player) {
        EnderBowConfig cfg = plugin.getEbConfig();
        EnderBowConfig.EffectsConfig effects = cfg.getEffectsConfig();

        // Play throw sound
        if (effects.getThrowSound().isEnabled()) {
            try {
                Sound sound = Sound.valueOf(effects.getThrowSound().getSound());
                player.getWorld().playSound(player.getEyeLocation(), sound, effects.getThrowSound().getVolume(), effects.getThrowSound().getPitch());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid throw sound: " + effects.getThrowSound().getSound());
            }
        }

        // Play throw particles
        if (effects.getThrowParticles().isEnabled()) {
            try {
                Particle particle = Particle.valueOf(effects.getThrowParticles().getParticle());
                player.getWorld().spawnParticle(
                    particle,
                    player.getEyeLocation(),
                    effects.getThrowParticles().getCount(),
                    effects.getThrowParticles().getOffsetX(),
                    effects.getThrowParticles().getOffsetY(),
                    effects.getThrowParticles().getOffsetZ()
                );
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid throw particle: " + effects.getThrowParticles().getParticle());
            }
        }
    }

    private void playTeleportEffects(org.bukkit.Location location) {
        EnderBowConfig cfg = plugin.getEbConfig();
        EnderBowConfig.EffectsConfig effects = cfg.getEffectsConfig();

        // Play teleport sound
        if (effects.getTeleportSound().isEnabled()) {
            try {
                Sound sound = Sound.valueOf(effects.getTeleportSound().getSound());
                location.getWorld().playSound(location, sound, effects.getTeleportSound().getVolume(), effects.getTeleportSound().getPitch());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid teleport sound: " + effects.getTeleportSound().getSound());
            }
        }

        // Play teleport particles
        if (effects.getTeleportParticles().isEnabled()) {
            try {
                Particle particle = Particle.valueOf(effects.getTeleportParticles().getParticle());
                location.getWorld().spawnParticle(
                    particle,
                    location,
                    effects.getTeleportParticles().getCount(),
                    effects.getTeleportParticles().getOffsetX(),
                    effects.getTeleportParticles().getOffsetY(),
                    effects.getTeleportParticles().getOffsetZ()
                );
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid teleport particle: " + effects.getTeleportParticles().getParticle());
            }
        }
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.BOW) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        Byte tag = meta.getPersistentDataContainer().get(key, PersistentDataType.BYTE);
        if (tag != null && tag == (byte)1) {
            // Prevent durability loss for our special bow
            event.setCancelled(true);
        }
    }
}
