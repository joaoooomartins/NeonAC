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

@CheckInfo(id = "criticals", name = "Criticals", category = CheckCategory.COMBAT,
        description = "Critical hit without proper fall conditions", since = 7)
public final class Criticals extends AbstractCheck {

    private final Map<UUID, Integer> ticksSinceGround = new ConcurrentHashMap<>();

    public Criticals(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        ticksSinceGround.remove(UUID.fromString(playerUuid));
    }

    @Override
    public void onAttack(NeonACPlayer player, PlayerAttackPacket packet) {
        if (isExempt(player)) return;
        if (!packet.isCritical()) return;

        UUID uuid = player.getUniqueId();
        int ticks = ticksSinceGround.getOrDefault(uuid, 0);

        if (player.isOnGround()) {
            ticksSinceGround.put(uuid, 0);
            return;
        }

        if (ticks < 3) {
            flag(player, 0.7, "ticks", ticks);
        }

        ticksSinceGround.merge(uuid, 1, Integer::sum);
    }
}
