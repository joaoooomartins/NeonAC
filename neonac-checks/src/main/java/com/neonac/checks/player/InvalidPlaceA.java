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

@CheckInfo(id = "invalidplacea", name = "InvalidPlaceA", category = CheckCategory.PLAYER,
        description = "Block placed inside own bounding box", since = 7)
public final class InvalidPlaceA extends AbstractCheck {

    private final Map<UUID, Integer> violations = new ConcurrentHashMap<>();

    public InvalidPlaceA(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        UUID uuid = UUID.fromString(playerUuid);
        violations.remove(uuid);
    }

    @Override
    public void onPlace(NeonACPlayer player, PlayerPlacePacket packet) {
        if (isExempt(player)) return;

        long pos = packet.getBlockPosition();
        int bx = (int) (pos >> 26);
        int by = (int) (pos >> 12) & 0xFFF;
        int bz = (int) pos;

        double[] pp = player.getPosition();
        double px = pp[0], py = pp[1], pz = pp[2];

        boolean insideX = bx + 1 > px - 0.3 && bx < px + 0.3;
        boolean insideY = by + 1 > py && by < py + 1.8;
        boolean insideZ = bz + 1 > pz - 0.3 && bz < pz + 0.3;

        if (insideX && insideY && insideZ) {
            int v = violations.merge(player.getUniqueId(), 1, Integer::sum);
            if (v >= getConfig().getInt("min-violations", 3)) {
                double confidence = Math.min(1.0, 0.5 + v * 0.1);
                flag(player, confidence, Map.of("violations", v));
            }
        } else {
            violations.put(player.getUniqueId(), 0);
        }
    }
}
