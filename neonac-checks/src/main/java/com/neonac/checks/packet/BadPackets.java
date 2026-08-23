package com.neonac.checks.packet;

import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckInfo;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.check.AbstractCheck;
import com.neonac.core.check.CheckEngine;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@CheckInfo(id = "badpackets", name = "BadPackets", category = CheckCategory.PACKET,
        description = "Invalid packet sequences or impossible states", since = 7)
public final class BadPackets extends AbstractCheck {

    private final Map<UUID, Integer> flags = new ConcurrentHashMap<>();

    public BadPackets(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        flags.remove(UUID.fromString(playerUuid));
    }

    @Override
    public void onTransaction(NeonACPlayer player, com.neonac.api.packet.PlayerTransactionPacket packet) {
        if (isExempt(player)) return;

        if (packet.getWindowId() < 0 && packet.getWindowId() != -1) {
            UUID uuid = player.getUniqueId();
            int f = flags.merge(uuid, 1, Integer::sum);
            if (f > 3) {
                flag(player, 0.7, "windowId", packet.getWindowId());
            }
        }
    }

    @Override
    public void onMove(NeonACPlayer player, com.neonac.api.packet.PlayerMovePacket packet) {
        if (isExempt(player)) return;

        double x = packet.getX();
        double y = packet.getY();
        double z = packet.getZ();

        if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)
                || Double.isInfinite(x) || Double.isInfinite(y) || Double.isInfinite(z)) {
            flag(player, 0.9, "nan", true);
        }

        if (y < -64 || y > 320) {
            flag(player, 0.8, "y", String.format("%.1f", y));
        }
    }
}
