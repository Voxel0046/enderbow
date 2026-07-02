package com.voxel.enderbow;

import java.util.List;

public class EnderBowConfig {
    private final String displayName;
    private final List<String> lore;
    private final int slot;
    private final boolean giveOnJoin;
    private final double cooldownSeconds;
    private final boolean unbreakable;
    private final double velocityMultiplier;
    private final boolean actionbarCooldownMessage;
    private final double forwardMultiplier;
    private final double verticalMultiplier;
    private final EffectsConfig effectsConfig;
    private final boolean metricsEnabled;

    public EnderBowConfig(String displayName, List<String> lore, int slot, boolean giveOnJoin, double cooldownSeconds, boolean unbreakable, double velocityMultiplier, boolean actionbarCooldownMessage, double forwardMultiplier, double verticalMultiplier, EffectsConfig effectsConfig, boolean metricsEnabled) {
        this.displayName = displayName;
        this.lore = lore;
        this.slot = slot;
        this.giveOnJoin = giveOnJoin;
        this.cooldownSeconds = cooldownSeconds;
        this.unbreakable = unbreakable;
        this.velocityMultiplier = velocityMultiplier;
        this.actionbarCooldownMessage = actionbarCooldownMessage;
        this.forwardMultiplier = forwardMultiplier;
        this.verticalMultiplier = verticalMultiplier;
        this.effectsConfig = effectsConfig;
        this.metricsEnabled = metricsEnabled;
    }

    public String getDisplayName() { return displayName; }
    public List<String> getLore() { return lore; }
    public int getSlot() { return slot; }
    public boolean isGiveOnJoin() { return giveOnJoin; }
    public double getCooldownSeconds() { return cooldownSeconds; }
    public boolean isUnbreakable() { return unbreakable; }
    public double getVelocityMultiplier() { return velocityMultiplier; }
    public boolean isActionbarCooldownMessage() { return actionbarCooldownMessage; }
    public double getForwardMultiplier() { return forwardMultiplier; }
    public double getVerticalMultiplier() { return verticalMultiplier; }
    public EffectsConfig getEffectsConfig() { return effectsConfig; }
    public boolean isMetricsEnabled() { return metricsEnabled; }

    public static class EffectsConfig {
        private final SoundConfig throwSound;
        private final ParticleConfig throwParticles;
        private final SoundConfig teleportSound;
        private final ParticleConfig teleportParticles;

        public EffectsConfig(SoundConfig throwSound, ParticleConfig throwParticles, SoundConfig teleportSound, ParticleConfig teleportParticles) {
            this.throwSound = throwSound;
            this.throwParticles = throwParticles;
            this.teleportSound = teleportSound;
            this.teleportParticles = teleportParticles;
        }

        public SoundConfig getThrowSound() { return throwSound; }
        public ParticleConfig getThrowParticles() { return throwParticles; }
        public SoundConfig getTeleportSound() { return teleportSound; }
        public ParticleConfig getTeleportParticles() { return teleportParticles; }
    }

    public static class SoundConfig {
        private final boolean enabled;
        private final String sound;
        private final float volume;
        private final float pitch;

        public SoundConfig(boolean enabled, String sound, float volume, float pitch) {
            this.enabled = enabled;
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
        }

        public boolean isEnabled() { return enabled; }
        public String getSound() { return sound; }
        public float getVolume() { return volume; }
        public float getPitch() { return pitch; }
    }

    public static class ParticleConfig {
        private final boolean enabled;
        private final String particle;
        private final int count;
        private final double offsetX;
        private final double offsetY;
        private final double offsetZ;

        public ParticleConfig(boolean enabled, String particle, int count, double offsetX, double offsetY, double offsetZ) {
            this.enabled = enabled;
            this.particle = particle;
            this.count = count;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
        }

        public boolean isEnabled() { return enabled; }
        public String getParticle() { return particle; }
        public int getCount() { return count; }
        public double getOffsetX() { return offsetX; }
        public double getOffsetY() { return offsetY; }
        public double getOffsetZ() { return offsetZ; }
    }
}
