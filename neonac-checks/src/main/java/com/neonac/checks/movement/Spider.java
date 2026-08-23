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
@CheckInfo(id = "spider", name = "Spider", category = CheckCategory.MOVEMENT,
        description = "Climbing walls without ladder/vine", since = 7)
public final class Spider extends AbstractCheck {

    private final Map<UUID, Integer> climbTicks = new ConcurrentHashMap<>();

    public Spider(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        java.util.UUID uuid = java.util.UUID.fromString(playerUuid);
        climbTicks.remove(uuid);
    }

    @Override
    public void onMove(NeonACPlayer player, PlayerMovePacket packet) {
        if (isExempt(player)) return;
        if (player.isOnLadder() || player.isOnGround() || player.isInWater()) {
            climbTicks.put(player.getUniqueId(), 0);
            return;
        }

        double dy = packet.getY() - player.getLastPosition()[1];
        double[] delta = player.getDelta();
        double horiz = Math.hypot(delta[0], delta[2]);

        if (dy > 0.05 && horiz < 0.15) {
            int t = climbTicks.merge(player.getUniqueId(), 1, Integer::sum);
            if (t >= getConfig().getInt("min-ticks", 6)) {
                double confidence = Math.min(1.0, 0.3 + t * 0.04);
                flag(player, confidence, Map.of("climbTicks", t));
            }
        } else {
            climbTicks.put(player.getUniqueId(), 0);
        }
    }
}
