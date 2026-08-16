package com.earac.checks.movement;

import com.earac.api.check.CheckCategory;
import com.earac.api.check.CheckInfo;
import com.earac.api.packet.PlayerMovePacket;
import com.earac.api.player.EarACPlayer;
import com.earac.core.check.AbstractCheck;
import com.earac.core.check.CheckEngine;
import com.earac.core.movement.MovementEngine;
import com.earac.core.version.VersionAdapterRegistry;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fly: detects sustained vertical suspension — the player remains airborne without
 * descending at the gravity-predicted rate. Uses the version adapter's gravity and
 * friction so the same logic works across versions; contextual exemptions (liquid,
 * ladder, velocity, teleport) suppress it.
 */
@CheckInfo(id = "fly", name = "Fly", category = CheckCategory.MOVEMENT,
        description = "Invalid vertical suspension / flight", since = 7)
public final class Fly extends AbstractCheck {

    private final Map<UUID, Integer> airTicks = new ConcurrentHashMap<>();

    public Fly(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void onMove(EarACPlayer player, PlayerMovePacket packet) {
        if (isExempt(player)) return;
        if (player.isOnGround() || player.isInWater() || player.isInLava()
                || player.isOnLadder() || player.isGliding()) {
            airTicks.put(player.getUniqueId(), 0);
            return;
        }

        MovementEngine.MovementSample s = engine.getMovementEngine()
                .analyse(player, packet.getX(), packet.getY(), packet.getZ(),
                        packet.isOnGround(), VersionAdapterRegistry.get(player.getVersion()));

        UUID uuid = player.getUniqueId();
        // Player should be falling (predictedDy negative) but is not descending enough.
        if (!packet.isOnGround() && s.predictedDy < -0.05 && packet.getY() - player.getLastPosition()[1] > s.predictedDy - s.tolerance) {
            int t = airTicks.merge(uuid, 1, Integer::sum);
            if (t >= getConfig().getInt("min-air-ticks", 8)) {
                double confidence = Math.min(1.0, 0.3 + t * 0.05);
                flag(player, confidence,
                        Map.of("airTicks", t, "dy", String.format("%.3f", packet.getY() - player.getLastPosition()[1])));
            }
        } else {
            airTicks.put(uuid, 0);
        }
    }
}
