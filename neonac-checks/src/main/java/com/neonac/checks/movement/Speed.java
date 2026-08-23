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
@CheckInfo(id = "speed", name = "Speed", category = CheckCategory.MOVEMENT,
        description = "Excessive horizontal movement", since = 7)
public final class Speed extends AbstractCheck {

    private final Map<UUID, Double> buffer = new ConcurrentHashMap<>();

    public Speed(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        java.util.UUID uuid = java.util.UUID.fromString(playerUuid);
        buffer.remove(uuid);
    }

    @Override
    public void onMove(NeonACPlayer player, PlayerMovePacket packet) {
        if (isExempt(player)) return;
        if (!packet.hasPosition()) return;

        MovementEngine.MovementSample s = engine.getMovementEngine()
                .analyse(player, packet.getX(), packet.getY(), packet.getZ(),
                        packet.isOnGround(), VersionAdapterRegistry.get(player.getVersion()));

        if (s.horizontalError <= 0.02) {
            buffer.merge(player.getUniqueId(), -getConfig().getVlDecay(), Double::max);
            return;
        }

        double confidence = Math.min(1.0, 0.25 + s.horizontalError / 0.5);
        UUID uuid = player.getUniqueId();
        double b = buffer.merge(uuid, confidence * getConfig().getVlAdd(), Double::sum);
        if (b < getConfig().getAlertThreshold()) return;

        flag(player, Math.min(1.0, b / 8.0),
                Map.of("horizontal", String.format("%.3f", s.horizontal), "max", String.format("%.3f", s.maxHorizontal)));
    }
}
