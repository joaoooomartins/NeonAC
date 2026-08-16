package com.earac.api.packet;

/**
 * Abstract velocity (entity velocity / knockback) packet applied to the player.
 */
public interface PlayerVelocityPacket extends EarACPacket {

    double getVelocityX();

    double getVelocityY();

    double getVelocityZ();
}
