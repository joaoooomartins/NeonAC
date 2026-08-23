package com.neonac.api.packet;

public interface PlayerAttackPacket extends NeonACPacket {

    int getTargetEntityId();
    Object getTargetEntity();
    boolean isAttack();
    default boolean isCritical() { return false; }
    default boolean isBow() { return false; }
    default boolean isEating() { return false; }
    default boolean isConsuming() { return false; }
}
