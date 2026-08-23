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

@CheckInfo(id = "waterwalk", name = "WaterWalk", category = CheckCategory.MOVEMENT,
        description = "Walking on water surface", since = 7)
public final class WaterWalk extends AbstractCheck {

    private final Map<UUID, Double> buffer = new ConcurrentHashMap<>();

    public WaterWalk(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        buffer.remove(UUID.fromString(playerUuid));
    }

    @Override
    public void onMove(NeonACPlayer player, PlayerMovePacket packet) {
        if (isExempt(player)) return;
        if (!packet.hasPosition()) return;

        boolean onWater = player.isInWater();
        boolean moving = Math.abs(player.getDelta()[0]) > 0.05 || Math.abs(player.getDelta()[2]) > 0.05;

        if (onWater && moving && !player.isGliding()) {
            UUID uuid = player.getUniqueId();
            double b = buffer.merge(uuid, getConfig().getVlAdd(), Double::sum);
            if (b >= getConfig().getAlertThreshold()) {
                flag(player, Math.min(1.0, b / 5.0), "moving", true);
            }
        } else {
            buffer.merge(player.getUniqueId(), -getConfig().getVlDecay(), Double::max);
        }
    }
}
