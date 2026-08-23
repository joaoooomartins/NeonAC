package com.neonac.checks.combat;

import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckInfo;
import com.neonac.api.packet.PlayerAttackPacket;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.check.AbstractCheck;
import com.neonac.core.check.CheckEngine;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@CheckInfo(id = "velocity", name = "VelocityCheck", category = CheckCategory.COMBAT,
        description = "Invalid velocity cancel patterns", since = 7)
public final class VelocityCheck extends AbstractCheck {

    private final Map<UUID, Boolean> pendingVelocity = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastVelocityTime = new ConcurrentHashMap<>();

    public VelocityCheck(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        UUID uuid = UUID.fromString(playerUuid);
        pendingVelocity.remove(uuid);
        lastVelocityTime.remove(uuid);
    }

    @Override
    public void onVelocity(NeonACPlayer player, com.neonac.api.packet.PlayerVelocityPacket packet) {
        pendingVelocity.put(player.getUniqueId(), true);
        lastVelocityTime.put(player.getUniqueId(), System.nanoTime());
    }

    @Override
    public void onMove(NeonACPlayer player, com.neonac.api.packet.PlayerMovePacket packet) {
        if (isExempt(player)) return;
        UUID uuid = player.getUniqueId();

        Boolean pending = pendingVelocity.get(uuid);
        if (pending == null || !pending) return;

        long velTime = lastVelocityTime.getOrDefault(uuid, 0L);
        long elapsed = System.nanoTime() - velTime;
        if (elapsed > 500_000_000L) {
            pendingVelocity.remove(uuid);
            return;
        }

        double[] delta = player.getDelta();
        double actualMotionY = delta[1];
        double expectedMotionY = packet.getVelocityY();

        if (expectedMotionY > 0 && actualMotionY == 0) {
            flag(player, 0.9, "cancelled", true);
        }

        pendingVelocity.remove(uuid);
    }
}
