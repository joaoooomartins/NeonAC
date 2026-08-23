package com.neonac.core.prediction;

import org.bukkit.Material;

public final class UncertaintyHandler {

    private int lastTeleportTicks;
    private int lastVelocityTicks;
    private int lastFlyingTicks;
    private int lastOnGroundTicks;
    private int lastClimbingTicks;
    private int lastWaterTicks;
    private int lastLavaTicks;

    private boolean isSteppingOnIce;
    private boolean isSteppingOnSlime;
    private boolean isSteppingOnHoney;

    public void update(PlayerData data) {
        if (lastTeleportTicks < 100) lastTeleportTicks++;
        if (lastVelocityTicks < 100) lastVelocityTicks++;
        if (lastFlyingTicks < 100) lastFlyingTicks++;
        if (lastOnGroundTicks < 100) lastOnGroundTicks++;
        if (lastClimbingTicks < 100) lastClimbingTicks++;
        if (lastWaterTicks < 100) lastWaterTicks++;
        if (lastLavaTicks < 100) lastLavaTicks++;

        Material below = data.blockBelow;
        isSteppingOnIce = below == Material.ICE || below == Material.PACKED_ICE || below == Material.BLUE_ICE;
        isSteppingOnSlime = below == Material.SLIME_BLOCK;
        isSteppingOnHoney = below == Material.HONEY_BLOCK;

        if (data.onGround) lastOnGroundTicks = 0;
        if (data.wasTouchingWater) lastWaterTicks = 0;
        if (data.wasTouchingLava) lastLavaTicks = 0;
        if (data.isClimbing) lastClimbingTicks = 0;
        if (data.isFlying) lastFlyingTicks = 0;
    }

    public void onTeleport() {
        lastTeleportTicks = 0;
    }

    public void onVelocity() {
        lastVelocityTicks = 0;
    }

    public double getHorizontalUncertainty(PlayerData data) {
        double threshold = data.getMovementThreshold();
        double uncertainty = 0;

        if (lastTeleportTicks < 2) uncertainty += threshold * 4;
        if (lastVelocityTicks < 2) uncertainty += threshold * 2;
        if (lastFlyingTicks < 3) uncertainty += threshold;

        if (isSteppingOnIce) uncertainty += threshold * 2;
        if (isSteppingOnSlime) uncertainty += threshold * 3;
        if (isSteppingOnHoney) uncertainty += threshold * 2;

        if (data.onGround) uncertainty += threshold;
        if (data.isClimbing) uncertainty += threshold;

        return uncertainty;
    }

    public double getVerticalUncertainty(PlayerData data) {
        double threshold = data.getMovementThreshold();
        double uncertainty = 0;

        if (lastTeleportTicks < 2) uncertainty += threshold * 4;
        if (lastVelocityTicks < 2) uncertainty += threshold * 2;
        if (lastFlyingTicks < 3) uncertainty += threshold;

        if (isSteppingOnSlime) uncertainty += threshold * 4;
        if (isSteppingOnHoney) uncertainty += threshold * 3;

        if (!data.onGround && lastOnGroundTicks < 3) uncertainty += threshold;

        return uncertainty;
    }

    public double reduceOffset(double offset) {
        if (lastTeleportTicks < 3) offset -= 0.5;
        if (isSteppingOnSlime || isSteppingOnHoney) offset -= 0.1;
        return Math.max(0, offset);
    }
}
