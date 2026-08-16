package com.earac.api.packet;

/**
 * Abstract transaction (confirmation) packet used to track client order/responsiveness.
 */
public interface PlayerTransactionPacket extends EarACPacket {

    int getWindowId();

    short getActionNumber();

    boolean isAccepted();
}
