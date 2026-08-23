package com.neonac.api.packet;
public interface PlayerPlacePacket extends NeonACPacket {

    long getBlockPosition();

    int getBlockFace();

    float getCursorX();

    float getCursorY();

    float getCursorZ();
}
