package com.neonac.checks.combat;

import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckInfo;
import com.neonac.api.packet.PlayerMovePacket;
import com.neonac.api.packet.PlayerVelocityPacket;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.check.AbstractCheck;
import com.neonac.core.check.CheckEngine;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
@CheckInfo(id = "velocity", name = "Velocity", category = CheckCategory.COMBAT,
        description = "Suppressed server knockback", since = 7)
public final class Velocity extends AbstractCheck {

    private static final class Pending {
        double expectedX, expectedZ;
        double observedX, observedZ;
        int ticksLeft;
    }

    private final Map<UUID, Pending> pending = new ConcurrentHashMap<>();

    public Velocity(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        java.util.UUID uuid = java.util.UUID.fromString(playerUuid);
        pending.remove(uuid);
    }

    @Override
    public void onVelocity(NeonACPlayer player, PlayerVelocityPacket packet) {
        if (isExempt(player)) return;
        Pending p = new Pending();
        p.expectedX = packet.getVelocityX();
        p.expectedZ = packet.getVelocityZ();
        p.observedX = 0;
        p.observedZ = 0;
        p.ticksLeft = 5;
        pending.put(player.getUniqueId(), p);
    }

    @Override
    public void onMove(NeonACPlayer player, PlayerMovePacket packet) {
        Pending p = pending.get(player.getUniqueId());
        if (p == null) return;
        double[] delta = player.getDelta();
        p.observedX += delta[0];
        p.observedZ += delta[2];
        p.ticksLeft--;
        if (p.ticksLeft > 0) return;

        double expected = Math.hypot(p.expectedX, p.expectedZ);
        double observed = Math.hypot(p.observedX, p.observedZ);
        pending.remove(player.getUniqueId());

        if (expected < 0.2) return; // negligible knockback
        double ratio = expected > 0 ? observed / expected : 1.0;
        if (ratio >= 0.35) return; // close enough to expected

        double confidence = Math.min(1.0, (0.35 - ratio) / 0.35);
        flag(player, confidence,
                Map.of("expected", String.format("%.2f", expected), "observed", String.format("%.2f", observed)));
    }
}
