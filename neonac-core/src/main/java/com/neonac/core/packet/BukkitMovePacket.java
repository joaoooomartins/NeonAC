package com.neonac.core.packet;

import com.neonac.api.packet.PlayerMovePacket;
public final class BukkitMovePacket implements PlayerMovePacket {

    private final double x, y, z;
    private final float yaw, pitch;
    private final boolean onGround;
    private final boolean hasPos, hasRot;
    private final long timestamp;
    private final Object platform;

    public BukkitMovePacket(double x, double y, double z, float yaw, float pitch,
                            boolean onGround, boolean hasPos, boolean hasRot, Object platform) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
        this.hasPos = hasPos;
        this.hasRot = hasRot;
        this.timestamp = System.nanoTime();
        this.platform = platform;
    }

    @Override
    public double getX() { return x; }

    @Override
    public double getY() { return y; }

    @Override
    public double getZ() { return z; }

    @Override
    public float getYaw() { return yaw; }

    @Override
    public float getPitch() { return pitch; }

    @Override
    public boolean hasPosition() { return hasPos; }

    @Override
    public boolean hasRotation() { return hasRot; }

    @Override
    public boolean isOnGround() { return onGround; }

    @Override
    public long getTimestamp() { return timestamp; }

    @Override
    public Object getPlatformPacket() { return platform; }
}
