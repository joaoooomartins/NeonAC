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

@CheckInfo(id = "longjump", name = "LongJump", category = CheckCategory.MOVEMENT,
        description = "Excessive jump boost or horizontal air speed", since = 7)
public final class LongJump extends AbstractCheck {

    private final Map<UUID, Double> buffer = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> airTicks = new ConcurrentHashMap<>();

    public LongJump(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        UUID uuid = UUID.fromString(playerUuid);
        buffer.remove(uuid);
        airTicks.remove(uuid);
    }

    @Override
    public void onMove(NeonACPlayer player, PlayerMovePacket packet) {
        if (isExempt(player)) return;
        if (!packet.hasPosition()) return;

        UUID uuid = player.getUniqueId();
        if (player.isOnGround()) {
            airTicks.put(uuid, 0);
            buffer.merge(uuid, -getConfig().getVlDecay(), Double::max);
            return;
        }

        int air = airTicks.merge(uuid, 1, Integer::sum);
        if (air < 2) return;

        double dx = Math.abs(player.getDelta()[0]);
        double dz = Math.abs(player.getDelta()[2]);
        double horizontal = Math.sqrt(dx * dx + dz * dz);

        double maxH = 0.65 + air * 0.03;
        if (horizontal > maxH) {
            double confidence = Math.min(1.0, (horizontal - maxH) / 0.3);
            double b = buffer.merge(uuid, confidence * getConfig().getVlAdd(), Double::sum);
            if (b >= getConfig().getAlertThreshold()) {
                flag(player, Math.min(1.0, b / 5.0), Map.of(
                        "h", String.format("%.3f", horizontal), "air", air));
            }
        }
    }
}
