package com.neonac.api.packet;
public interface PlayerDigPacket extends NeonACPacket {

    int getAction();

    long getBlockPosition();

    int getBlockFace();
}
