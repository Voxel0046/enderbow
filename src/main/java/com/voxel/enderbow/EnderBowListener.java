package com.voxel.enderbow;

import org.bukkit.NamespacedKey;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;

public class EnderBowListener implements Listener {

    private final EnderBowPlugin plugin;
    private ItemStack configuredBow;
    private final NamespacedKey key;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

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
                    String msg = ChatColor.RED + "EnderBow is on cooldown for " + remaining + "s.";
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

        // cancel original shoot event to avoid arrow consumption visuals
        event.setCancelled(true);
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
