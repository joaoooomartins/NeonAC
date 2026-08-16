package com.earac.api.packet;

/**
 * Base abstraction for every packet translated by the protocol layer.
 * Checks consume only these types — never raw NMS/protocol packets.
 */
public interface EarACPacket {

    /**
     * @return the server-time nanoseconds at which the packet was received.
     */
    long getTimestamp();

    /**
     * @return the platform packet object (for adapters only; never inspect in checks).
     */
    Object getPlatformPacket();
}
