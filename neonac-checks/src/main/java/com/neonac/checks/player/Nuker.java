package com.neonac.checks.player;

import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckInfo;
import com.neonac.api.packet.PlayerDigPacket;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.check.AbstractCheck;
import com.neonac.core.check.CheckEngine;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@CheckInfo(id = "nuker", name = "Nuker", category = CheckCategory.PLAYER,
        description = "Breaking blocks faster than legitimate speed", since = 7)
public final class Nuker extends AbstractCheck {

    private final Map<UUID, Integer> blocksPerTick = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastBreak = new ConcurrentHashMap<>();

    public Nuker(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        UUID uuid = UUID.fromString(playerUuid);
        blocksPerTick.remove(uuid);
        lastBreak.remove(uuid);
    }

    @Override
    public void onDig(NeonACPlayer player, PlayerDigPacket packet) {
        if (isExempt(player)) return;

        UUID uuid = player.getUniqueId();
        long now = System.nanoTime();
        Long last = lastBreak.getOrDefault(uuid, 0L);
        lastBreak.put(uuid, now);

        if (now - last < 10_000_000L) {
            int count = blocksPerTick.merge(uuid, 1, Integer::sum);
            if (count > 2) {
                flag(player, Math.min(1.0, (count - 2) * 0.3), "count", count);
            }
        } else {
            blocksPerTick.put(uuid, 1);
        }
    }
}
