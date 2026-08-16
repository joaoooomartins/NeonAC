package com.earac.api.packet;

/**
 * Abstract block place packet.
 */
public interface PlayerPlacePacket extends EarACPacket {

    long getBlockPosition();

    int getBlockFace();

    float getCursorX();

    float getCursorY();

    float getCursorZ();
}
