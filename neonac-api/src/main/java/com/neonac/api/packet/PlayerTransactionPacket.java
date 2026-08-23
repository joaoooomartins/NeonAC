package com.neonac.api.packet;
public interface PlayerTransactionPacket extends NeonACPacket {

    int getWindowId();

    short getActionNumber();

    boolean isAccepted();
}
