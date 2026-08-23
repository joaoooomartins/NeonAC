package com.neonac.api.packet;
public interface NeonACPacket {
    long getTimestamp();
    Object getPlatformPacket();
}
