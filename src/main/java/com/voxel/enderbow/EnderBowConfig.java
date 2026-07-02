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

    public EnderBowConfig(String displayName, List<String> lore, int slot, boolean giveOnJoin, double cooldownSeconds, boolean unbreakable, double velocityMultiplier, boolean actionbarCooldownMessage) {
        this.displayName = displayName;
        this.lore = lore;
        this.slot = slot;
        this.giveOnJoin = giveOnJoin;
        this.cooldownSeconds = cooldownSeconds;
        this.unbreakable = unbreakable;
        this.velocityMultiplier = velocityMultiplier;
        this.actionbarCooldownMessage = actionbarCooldownMessage;
    }

    public String getDisplayName() { return displayName; }
    public List<String> getLore() { return lore; }
    public int getSlot() { return slot; }
    public boolean isGiveOnJoin() { return giveOnJoin; }
    public double getCooldownSeconds() { return cooldownSeconds; }
    public boolean isUnbreakable() { return unbreakable; }
    public double getVelocityMultiplier() { return velocityMultiplier; }
    public boolean isActionbarCooldownMessage() { return actionbarCooldownMessage; }
}
