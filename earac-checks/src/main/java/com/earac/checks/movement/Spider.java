package com.earac.checks.movement;

import com.earac.api.check.CheckCategory;
import com.earac.api.check.CheckInfo;
import com.earac.api.packet.PlayerMovePacket;
import com.earac.api.player.EarACPlayer;
import com.earac.core.check.AbstractCheck;
import com.earac.core.check.CheckEngine;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spider: detects climbing vertical surfaces without a ladder/vine. A legitimate player
 * cannot ascend a flat wall; repeated upward motion while airborne and not on a climbable
 * block is anomalous.
 */
@CheckInfo(id = "spider", name = "Spider", category = CheckCategory.MOVEMENT,
        description = "Climbing walls without ladder/vine", since = 7)
public final class Spider extends AbstractCheck {

    private final Map<UUID, Integer> climbTicks = new ConcurrentHashMap<>();

    public Spider(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void onMove(EarACPlayer player, PlayerMovePacket packet) {
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
