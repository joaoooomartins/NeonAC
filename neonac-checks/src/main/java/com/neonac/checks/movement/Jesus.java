package com.neonac.checks.movement;

import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckInfo;
import com.neonac.api.packet.PlayerMovePacket;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.check.AbstractCheck;
import com.neonac.core.check.CheckEngine;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
@CheckInfo(id = "jesus", name = "Jesus", category = CheckCategory.MOVEMENT,
        description = "Walking on liquid surfaces", since = 7)
public final class Jesus extends AbstractCheck {

    private final Map<UUID, Integer> ticks = new ConcurrentHashMap<>();

    public Jesus(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        java.util.UUID uuid = java.util.UUID.fromString(playerUuid);
        ticks.remove(uuid);
    }

    @Override
    public void onMove(NeonACPlayer player, PlayerMovePacket packet) {
        if (isExempt(player)) return;
        if (!player.isInWater() && !player.isInLava()) {
            ticks.put(player.getUniqueId(), 0);
            return;
        }

        double dy = packet.getY() - player.getLastPosition()[1];
        double[] delta = player.getDelta();
        double horiz = Math.hypot(delta[0], delta[2]);

        // Not sinking (dy >= ~0) while moving on the surface and not in a vehicle.
        if (dy > -0.05 && horiz > 0.05 && !player.isOnVehicle()) {
            int t = ticks.merge(player.getUniqueId(), 1, Integer::sum);
            if (t >= getConfig().getInt("min-ticks", 5)) {
                double confidence = Math.min(1.0, 0.3 + t * 0.04);
                flag(player, confidence, Map.of("ticks", t));
            }
        } else {
            ticks.put(player.getUniqueId(), 0);
        }
    }
}
