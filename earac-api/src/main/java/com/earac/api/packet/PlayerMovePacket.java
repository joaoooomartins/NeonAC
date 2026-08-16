package com.earac.api.packet;

/**
 * Abstract movement/update packet. Carries position and rotation deltas.
 */
public interface PlayerMovePacket extends EarACPacket {

    double getX();

    double getY();

    double getZ();

    float getYaw();

    float getPitch();

    boolean hasPosition();

    boolean hasRotation();

    boolean isOnGround();
}
