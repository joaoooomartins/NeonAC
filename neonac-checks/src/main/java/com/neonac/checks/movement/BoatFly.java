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

@CheckInfo(id = "boatfly", name = "BoatFly", category = CheckCategory.MOVEMENT,
        description = "Flying while in a boat", since = 7)
public final class BoatFly extends AbstractCheck {

    private final Map<UUID, Double> buffer = new ConcurrentHashMap<>();

    public BoatFly(CheckEngine engine) {
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
        if (!player.isOnVehicle()) return;

        double dy = player.getDelta()[1];
        if (dy > 0.15) {
            UUID uuid = player.getUniqueId();
            double b = buffer.merge(uuid, getConfig().getVlAdd(), Double::sum);
            if (b >= getConfig().getAlertThreshold()) {
                flag(player, Math.min(1.0, b / 5.0), "dy", String.format("%.3f", dy));
            }
        } else {
            buffer.merge(player.getUniqueId(), -getConfig().getVlDecay(), Double::max);
        }
    }
}
