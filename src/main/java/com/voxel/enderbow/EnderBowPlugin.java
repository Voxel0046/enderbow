package com.voxel.enderbow;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;

public final class EnderBowPlugin extends JavaPlugin {

    private EnderBowConfig ebConfig;
    private EnderBowListener listener;
    private Metrics metrics;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Initialize bStats metrics
        if (getConfig().getBoolean("metrics.enabled", true)) {
            metrics = new Metrics(this, 23078); // Use your actual bStats plugin ID
            setupMetrics();
        }

        // Load config first
        reloadEbConfig();

        // create listener after config is loaded
        listener = new EnderBowListener(this);
        Bukkit.getPluginManager().registerEvents(listener, this);

        EnderBowCommand commandExecutor = new EnderBowCommand(this);
        getCommand("enderbow").setExecutor(commandExecutor);

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

        // Parse effects configuration
        EnderBowConfig.EffectsConfig effectsConfig = parseEffectsConfig(cfg);

        ebConfig = new EnderBowConfig(
            ColorUtils.color(name),
            ColorUtils.colorList(lore),
            Math.max(0, Math.min(8, cfg.getInt("slot", 4))),
            cfg.getBoolean("give-on-join", true),
            Math.max(0.0, cfg.getDouble("cooldown-seconds", 2.0)),
            cfg.getBoolean("unbreakable", true),
            Math.max(0.0, cfg.getDouble("velocity-multiplier", 1.0)),
            ColorUtils.color(cfg.getString("cooldown-message", "&cEnderBow is on cooldown for %s seconds.")),
            cfg.getBoolean("actionbar-cooldown-message", true),
            Math.max(0.0, cfg.getDouble("forward-multiplier", 1.0)),
            Math.max(0.0, cfg.getDouble("vertical-multiplier", 1.0)),
            effectsConfig,
            cfg.getBoolean("metrics.enabled", true)
        );

        // refresh listener's internal bow and re-give to online players if configured
        if (listener != null) {
            listener.refreshConfiguredBow();
            if (ebConfig.isGiveOnJoin()) {
                Bukkit.getOnlinePlayers().forEach(p -> listener.giveBowTo(p));
            }
        }
    }

    private EnderBowConfig.EffectsConfig parseEffectsConfig(FileConfiguration cfg) {
        try {
            // Throw sound
            boolean throwSoundEnabled = cfg.getBoolean("effects.throw-sound.enabled", true);
            String throwSound = cfg.getString("effects.throw-sound.sound", "ENTITY_ENDER_PEARL_THROW");
            float throwSoundVolume = (float) cfg.getDouble("effects.throw-sound.volume", 1.0);
            float throwSoundPitch = (float) cfg.getDouble("effects.throw-sound.pitch", 1.0);
            EnderBowConfig.SoundConfig throwSoundConfig = new EnderBowConfig.SoundConfig(
                throwSoundEnabled, throwSound, throwSoundVolume, throwSoundPitch
            );

            // Throw particles
            boolean throwParticlesEnabled = cfg.getBoolean("effects.throw-particles.enabled", true);
            String throwParticle = cfg.getString("effects.throw-particles.particle", "PORTAL");
            int throwParticleCount = cfg.getInt("effects.throw-particles.count", 8);
            double throwOffsetX = cfg.getDouble("effects.throw-particles.offset-x", 0.3);
            double throwOffsetY = cfg.getDouble("effects.throw-particles.offset-y", 0.3);
            double throwOffsetZ = cfg.getDouble("effects.throw-particles.offset-z", 0.3);
            EnderBowConfig.ParticleConfig throwParticleConfig = new EnderBowConfig.ParticleConfig(
                throwParticlesEnabled, throwParticle, throwParticleCount, throwOffsetX, throwOffsetY, throwOffsetZ
            );

            // Trail particles
            boolean trailParticlesEnabled = cfg.getBoolean("effects.trail-particles.enabled", true);
            String trailParticle = cfg.getString("effects.trail-particles.particle", "PURPLE_FLAME");
            int trailParticleCount = cfg.getInt("effects.trail-particles.count", 2);
            double trailOffsetX = cfg.getDouble("effects.trail-particles.offset-x", 0.1);
            double trailOffsetY = cfg.getDouble("effects.trail-particles.offset-y", 0.1);
            double trailOffsetZ = cfg.getDouble("effects.trail-particles.offset-z", 0.1);
            double trailSpeed = cfg.getDouble("effects.trail-particles.speed", 0.0);
            EnderBowConfig.ParticleConfig trailParticleConfig = new EnderBowConfig.ParticleConfig(
                trailParticlesEnabled, trailParticle, trailParticleCount, trailOffsetX, trailOffsetY, trailOffsetZ, trailSpeed
            );

            // Teleport sound
            boolean teleportSoundEnabled = cfg.getBoolean("effects.teleport-sound.enabled", true);
            String teleportSound = cfg.getString("effects.teleport-sound.sound", "ENTITY_ENDERMAN_TELEPORT");
            float teleportSoundVolume = (float) cfg.getDouble("effects.teleport-sound.volume", 1.0);
            float teleportSoundPitch = (float) cfg.getDouble("effects.teleport-sound.pitch", 1.0);
            EnderBowConfig.SoundConfig teleportSoundConfig = new EnderBowConfig.SoundConfig(
                teleportSoundEnabled, teleportSound, teleportSoundVolume, teleportSoundPitch
            );

            // Teleport particles
            boolean teleportParticlesEnabled = cfg.getBoolean("effects.teleport-particles.enabled", true);
            String teleportParticle = cfg.getString("effects.teleport-particles.particle", "PORTAL");
            int teleportParticleCount = cfg.getInt("effects.teleport-particles.count", 16);
            double teleportOffsetX = cfg.getDouble("effects.teleport-particles.offset-x", 0.5);
            double teleportOffsetY = cfg.getDouble("effects.teleport-particles.offset-y", 0.5);
            double teleportOffsetZ = cfg.getDouble("effects.teleport-particles.offset-z", 0.5);
            EnderBowConfig.ParticleConfig teleportParticleConfig = new EnderBowConfig.ParticleConfig(
                teleportParticlesEnabled, teleportParticle, teleportParticleCount, teleportOffsetX, teleportOffsetY, teleportOffsetZ
            );

            return new EnderBowConfig.EffectsConfig(throwSoundConfig, throwParticleConfig, trailParticleConfig, teleportSoundConfig, teleportParticleConfig);
        } catch (Exception e) {
            getLogger().warning("Failed to parse effects configuration, using defaults");
            return createDefaultEffectsConfig();
        }
    }

    private EnderBowConfig.EffectsConfig createDefaultEffectsConfig() {
        EnderBowConfig.SoundConfig throwSound = new EnderBowConfig.SoundConfig(true, "ENTITY_ENDER_PEARL_THROW", 1.0f, 1.0f);
        EnderBowConfig.ParticleConfig throwParticles = new EnderBowConfig.ParticleConfig(true, "PORTAL", 8, 0.3, 0.3, 0.3);
        EnderBowConfig.ParticleConfig trailParticles = new EnderBowConfig.ParticleConfig(true, "PURPLE_FLAME", 2, 0.1, 0.1, 0.1, 0.0);
        EnderBowConfig.SoundConfig teleportSound = new EnderBowConfig.SoundConfig(true, "ENTITY_ENDERMAN_TELEPORT", 1.0f, 1.0f);
        EnderBowConfig.ParticleConfig teleportParticles = new EnderBowConfig.ParticleConfig(true, "PORTAL", 16, 0.5, 0.5, 0.5);
        return new EnderBowConfig.EffectsConfig(throwSound, throwParticles, trailParticles, teleportSound, teleportParticles);
    }

    private void setupMetrics() {
        metrics.addCustomChart(new org.bstats.bukkit.Metrics.SimplePie("cooldown_enabled",
            () -> ebConfig.getCooldownSeconds() > 0 ? "yes" : "no"));
        metrics.addCustomChart(new org.bstats.bukkit.Metrics.SimplePie("effects_enabled",
            () -> (ebConfig.getEffectsConfig().getThrowSound().isEnabled() || 
                   ebConfig.getEffectsConfig().getThrowParticles().isEnabled()) ? "yes" : "no"));
        metrics.addCustomChart(new org.bstats.bukkit.Metrics.SimplePie("give_on_join",
            () -> ebConfig.isGiveOnJoin() ? "yes" : "no"));
        metrics.addCustomChart(new org.bstats.bukkit.Metrics.AdvancedPie("player_count",
            () -> {
                java.util.Map<String, Integer> map = new java.util.HashMap<>();
                int count = Bukkit.getOnlinePlayers().size();
                map.put(count + " players", 1);
                return map;
            }));
    }

    public EnderBowConfig getEbConfig() {
        return ebConfig;
    }

    public void giveBowTo(org.bukkit.entity.Player player) {
        if (listener != null) listener.giveBowTo(player);
    }
}
