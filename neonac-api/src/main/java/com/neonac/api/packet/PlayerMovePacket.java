package com.neonac.api.packet;
public interface PlayerMovePacket extends NeonACPacket {

    double getX();

    double getY();

    double getZ();

    float getYaw();

    float getPitch();

    boolean hasPosition();

    boolean hasRotation();

    boolean isOnGround();
}
