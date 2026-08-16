package com.earac.core.packet;

import com.earac.api.packet.PlayerPlacePacket;
import org.bukkit.block.Block;

/**
 * Translates block placement into the abstract place packet.
 */
public final class BukkitPlacePacket implements PlayerPlacePacket {

    private final long blockPosition;
    private final int blockFace;
    private final long timestamp;
    private final Object platform;

    public BukkitPlacePacket(Block block, int blockFace, Object platform) {
        this.blockPosition = pack(block.getX(), block.getY(), block.getZ());
        this.blockFace = blockFace;
        this.platform = platform;
        this.timestamp = System.nanoTime();
    }

    private static long pack(int x, int y, int z) {
        return ((long) x & 0x3FFFFFFL) << 38 | ((long) y & 0xFFFL) << 26 | ((long) z & 0x3FFFFFFL);
    }

    @Override
    public long getBlockPosition() {
        return blockPosition;
    }

    @Override
    public int getBlockFace() {
        return blockFace;
    }

    @Override
    public float getCursorX() {
        return 0.5f;
    }

    @Override
    public float getCursorY() {
        return 0.5f;
    }

    @Override
    public float getCursorZ() {
        return 0.5f;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public Object getPlatformPacket() {
        return platform;
    }
}
