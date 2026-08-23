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

@CheckInfo(id = "fasteat", name = "FastEat", category = CheckCategory.PLAYER,
        description = "Consuming items faster than normal", since = 7)
public final class FastEat extends AbstractCheck {

    private final Map<UUID, Long> lastConsume = new ConcurrentHashMap<>();

    public FastEat(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        lastConsume.remove(UUID.fromString(playerUuid));
    }

    @Override
    public void onAttack(NeonACPlayer player, PlayerAttackPacket packet) {
        if (isExempt(player)) return;
        if (!packet.isConsuming()) return;

        UUID uuid = player.getUniqueId();
        long now = System.nanoTime();
        Long last = lastConsume.put(uuid, now);
        if (last == null) return;

        long deltaMs = (now - last) / 1_000_000;
        if (deltaMs < 300) {
            flag(player, 0.6, "delay", deltaMs);
        }
    }
}
