package com.earac.core.packet;

import com.earac.api.packet.PlayerAttackPacket;

/**
 * Translates a Bukkit {@code EntityDamageByEntityEvent} (player -> entity) into an attack packet.
 */
public final class BukkitAttackPacket implements PlayerAttackPacket {

    private final int targetEntityId;
    private final Object targetEntity;
    private final long timestamp;
    private final Object platform;

    public BukkitAttackPacket(int targetEntityId, Object targetEntity, Object platform) {
        this.targetEntityId = targetEntityId;
        this.targetEntity = targetEntity;
        this.platform = platform;
        this.timestamp = System.nanoTime();
    }

    @Override
    public int getTargetEntityId() {
        return targetEntityId;
    }

    @Override
    public Object getTargetEntity() {
        return targetEntity;
    }

    @Override
    public boolean isAttack() {
        return true;
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
