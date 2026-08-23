package com.neonac.checks.player;

import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckInfo;
import com.neonac.api.packet.PlayerPlacePacket;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.check.AbstractCheck;
import com.neonac.core.check.CheckEngine;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@CheckInfo(id = "invalidplaceb", name = "InvalidPlaceB", category = CheckCategory.PLAYER,
        description = "Block placed through a wall", since = 7)
public final class InvalidPlaceB extends AbstractCheck {

    private final Map<UUID, Long> lastPlace = new ConcurrentHashMap<>();

    public InvalidPlaceB(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        UUID uuid = UUID.fromString(playerUuid);
        lastPlace.remove(uuid);
    }

    @Override
    public void onPlace(NeonACPlayer player, PlayerPlacePacket packet) {
        if (isExempt(player)) return;

        long pos = packet.getBlockPosition();
        int bx = (int) (pos >> 26);
        int by = (int) (pos >> 12) & 0xFFF;
        int bz = (int) pos;

        double[] pp = player.getPosition();
        double dx = bx + 0.5 - pp[0];
        double dy = by + 0.5 - (pp[1] + 1.62);
        double dz = bz + 0.5 - pp[2];
        double distSq = dx * dx + dy * dy + dz * dz;

        if (distSq > 25) {
            long now = System.currentTimeMillis();
            Long last = lastPlace.get(player.getUniqueId());
            if (last != null && now - last < 50) {
                flag(player, 0.8, Map.of("distance", String.format("%.2f", Math.sqrt(distSq))));
            }
            lastPlace.put(player.getUniqueId(), now);
        }
    }
}
