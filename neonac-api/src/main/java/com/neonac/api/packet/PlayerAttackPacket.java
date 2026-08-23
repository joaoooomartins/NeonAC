package com.neonac.api.packet;
public interface PlayerAttackPacket extends NeonACPacket {

    int getTargetEntityId();
    Object getTargetEntity();
    boolean isAttack();
}
