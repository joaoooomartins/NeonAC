package com.neonac.checks.player;

import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckInfo;
import com.neonac.api.packet.PlayerAttackPacket;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.check.AbstractCheck;
import com.neonac.core.check.CheckEngine;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@CheckInfo(id = "autoeat", name = "AutoEat", category = CheckCategory.PLAYER,
        description = "Eating food faster than possible", since = 7)
public final class AutoEat extends AbstractCheck {

    private final Map<UUID, Long> lastEat = new ConcurrentHashMap<>();

    public AutoEat(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        lastEat.remove(UUID.fromString(playerUuid));
    }

    @Override
    public void onAttack(NeonACPlayer player, PlayerAttackPacket packet) {
        if (isExempt(player)) return;
        if (!packet.isEating()) return;

        UUID uuid = player.getUniqueId();
        long now = System.nanoTime();
        Long last = lastEat.put(uuid, now);
        if (last == null) return;

        long deltaMs = (now - last) / 1_000_000;
        if (deltaMs < 500) {
            flag(player, 0.7, "delay", deltaMs);
        }
    }
}
