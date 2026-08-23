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

@CheckInfo(id = "positionplace", name = "PositionPlace", category = CheckCategory.PLAYER,
        description = "Block placed after large position shift", since = 7)
public final class PositionPlace extends AbstractCheck {

    private final Map<UUID, double[]> lastPos = new ConcurrentHashMap<>();

    public PositionPlace(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        UUID uuid = UUID.fromString(playerUuid);
        lastPos.remove(uuid);
    }

    @Override
    public void onPlace(NeonACPlayer player, PlayerPlacePacket packet) {
        if (isExempt(player)) return;

        UUID uuid = player.getUniqueId();
        double[] current = player.getPosition();
        double[] last = lastPos.get(uuid);

        if (last != null) {
            double dx = current[0] - last[0];
            double dy = current[1] - last[1];
            double dz = current[2] - last[2];
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (dist > 1.0) {
                double confidence = Math.min(1.0, (dist - 1.0) * 0.5);
                flag(player, confidence, Map.of("shift", String.format("%.2f", dist)));
            }
        }

        lastPos.put(uuid, current.clone());
    }
}
