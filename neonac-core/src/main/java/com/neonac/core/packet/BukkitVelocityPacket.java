package com.neonac.core.packet;

import com.neonac.api.packet.PlayerVelocityPacket;
public final class BukkitVelocityPacket implements PlayerVelocityPacket {

    private final double vx, vy, vz;
    private final long timestamp;
    private final Object platform;

    public BukkitVelocityPacket(double vx, double vy, double vz, Object platform) {
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
        this.platform = platform;
        this.timestamp = System.nanoTime();
    }

    @Override
    public double getVelocityX() {
        return vx;
    }

    @Override
    public double getVelocityY() {
        return vy;
    }

    @Override
    public double getVelocityZ() {
        return vz;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public Object getPlatformPacket() {
        return platform;
    }
}
