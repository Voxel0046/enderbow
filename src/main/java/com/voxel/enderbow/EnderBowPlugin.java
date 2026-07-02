package com.voxel.enderbow;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class EnderBowPlugin extends JavaPlugin {

    private EnderBowConfig ebConfig;
    private EnderBowListener listener;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // create listener early so reloadEbConfig can re-give items safely
        listener = new EnderBowListener(this);
        Bukkit.getPluginManager().registerEvents(listener, this);

        getCommand("enderbow").setExecutor(new EnderBowCommand(this));

        reloadEbConfig();

        getLogger().info("EnderBow enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("EnderBow disabled");
    }

    public void reloadEbConfig() {
        reloadConfig();
        FileConfiguration cfg = getConfig();
        String name = cfg.getString("name", "&5Ender Bow");
        java.util.List<String> lore = cfg.getStringList("lore");

        ebConfig = new EnderBowConfig(
            ColorUtils.color(name),
            ColorUtils.colorList(lore),
            Math.max(0, Math.min(8, cfg.getInt("slot", 4))),
            cfg.getBoolean("give-on-join", true),
            Math.max(0.0, cfg.getDouble("cooldown-seconds", 2.0)),
            cfg.getBoolean("unbreakable", true),
            Math.max(0.0, cfg.getDouble("velocity-multiplier", 1.0)),
            cfg.getBoolean("actionbar-cooldown-message", true)
        );

        // refresh listener's internal bow and re-give to online players if configured
        if (listener != null) {
            listener.refreshConfiguredBow();
            if (ebConfig.isGiveOnJoin()) {
                Bukkit.getOnlinePlayers().forEach(p -> listener.giveBowTo(p));
            }
        }
    }

    public EnderBowConfig getEbConfig() {
        return ebConfig;
    }
}
