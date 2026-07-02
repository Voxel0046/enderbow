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

    public EnderBowConfig(String displayName, List<String> lore, int slot, boolean giveOnJoin, double cooldownSeconds, boolean unbreakable, double velocityMultiplier, boolean actionbarCooldownMessage, double forwardMultiplier, double verticalMultiplier) {
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
}
