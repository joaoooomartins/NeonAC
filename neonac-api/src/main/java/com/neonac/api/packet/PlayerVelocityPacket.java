package com.neonac.api.packet;
public interface PlayerVelocityPacket extends NeonACPacket {

    double getVelocityX();

    double getVelocityY();

    double getVelocityZ();
}
