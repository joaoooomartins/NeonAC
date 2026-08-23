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

@CheckInfo(id = "fastbow", name = "FastBow", category = CheckCategory.COMBAT,
        description = "Bow shot faster than possible", since = 7)
public final class FastBow extends AbstractCheck {

    private final Map<UUID, Long> lastShoot = new ConcurrentHashMap<>();

    public FastBow(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        lastShoot.remove(UUID.fromString(playerUuid));
    }

    @Override
    public void onAttack(NeonACPlayer player, PlayerAttackPacket packet) {
        if (isExempt(player)) return;
        if (!packet.isBow()) return;

        UUID uuid = player.getUniqueId();
        long now = System.nanoTime();
        Long last = lastShoot.get(uuid);
        lastShoot.put(uuid, now);

        if (last == null) return;
        long deltaMs = (now - last) / 1_000_000;
        if (deltaMs < 250) {
            flag(player, 0.8, "delay", deltaMs);
        }
    }
}
