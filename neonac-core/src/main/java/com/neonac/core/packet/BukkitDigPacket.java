package com.neonac.core.packet;

import com.neonac.api.packet.PlayerDigPacket;
import org.bukkit.block.Block;
public final class BukkitDigPacket implements PlayerDigPacket {

    private final int action;
    private final long blockPosition;
    private final int blockFace;
    private final long timestamp;
    private final Object platform;

    public BukkitDigPacket(int action, Block block, int blockFace, Object platform) {
        this.action = action;
        this.blockPosition = pack(block.getX(), block.getY(), block.getZ());
        this.blockFace = blockFace;
        this.platform = platform;
        this.timestamp = System.nanoTime();
    }

    private static long pack(int x, int y, int z) {
        return ((long) x & 0x3FFFFFFL) << 38 | ((long) y & 0xFFFL) << 26 | ((long) z & 0x3FFFFFFL);
    }

    @Override
    public int getAction() {
        return action;
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
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public Object getPlatformPacket() {
        return platform;
    }
}
