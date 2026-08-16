package com.earac.api.packet;

/**
 * Abstract block dig (break) packet.
 */
public interface PlayerDigPacket extends EarACPacket {

    int getAction(); // 0 = start, 1 = abort, 2 = finish

    long getBlockPosition();

    int getBlockFace();
}
