package com.neonac.checks.movement;

import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckInfo;
import com.neonac.api.packet.PlayerMovePacket;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.check.AbstractCheck;
import com.neonac.core.check.CheckEngine;
import com.neonac.core.version.VersionAdapterRegistry;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
@CheckInfo(id = "step", name = "Step", category = CheckCategory.MOVEMENT,
        description = "Abrupt vertical step beyond auto-step height", since = 7)
public final class Step extends AbstractCheck {

    private final Map<UUID, Double> buffer = new ConcurrentHashMap<>();

    public Step(CheckEngine engine) {
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

        double dy = packet.getY() - player.getLastPosition()[1];
        double step = VersionAdapterRegistry.get(player.getVersion()).getStepHeight();
        double[] delta = player.getDelta();

        if (dy > step + 0.15 && dy < 1.6 && Math.hypot(delta[0], delta[2]) < 0.4) {
            double confidence = Math.min(1.0, 0.4 + (dy - step) * 0.5);
            UUID uuid = player.getUniqueId();
            double b = buffer.merge(uuid, confidence * getConfig().getVlAdd(), Double::sum);
            if (b < getConfig().getAlertThreshold()) return;
            flag(player, Math.min(1.0, b / 6.0), Map.of("dy", String.format("%.2f", dy)));
        } else {
            buffer.merge(player.getUniqueId(), -getConfig().getVlDecay(), Double::max);
        }
    }
}
