package com.neonac.checks.movement;

import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckInfo;
import com.neonac.api.packet.PlayerMovePacket;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.check.AbstractCheck;
import com.neonac.core.check.CheckEngine;
import com.neonac.core.movement.MovementEngine;
import com.neonac.core.version.VersionAdapterRegistry;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
@CheckInfo(id = "fly", name = "Fly", category = CheckCategory.MOVEMENT,
        description = "Invalid vertical suspension / flight", since = 7)
public final class Fly extends AbstractCheck {

    private final Map<UUID, Integer> airTicks = new ConcurrentHashMap<>();

    public Fly(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        java.util.UUID uuid = java.util.UUID.fromString(playerUuid);
        airTicks.remove(uuid);
    }

    @Override
    public void onMove(NeonACPlayer player, PlayerMovePacket packet) {
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
