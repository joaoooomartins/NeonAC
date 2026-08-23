package com.neonac.checks.packet;

import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckInfo;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.check.AbstractCheck;
import com.neonac.core.check.CheckEngine;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@CheckInfo(id = "timer", name = "Timer", category = CheckCategory.PACKET,
        description = "Server tick rate manipulation detected", since = 7)
public final class Timer extends AbstractCheck {

    private final Map<UUID, Long> lastTick = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> fastTicks = new ConcurrentHashMap<>();

    public Timer(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        UUID uuid = UUID.fromString(playerUuid);
        lastTick.remove(uuid);
        fastTicks.remove(uuid);
    }

    @Override
    public void onTick(NeonACPlayer player, long tick) {
        if (isExempt(player)) return;

        UUID uuid = player.getUniqueId();
        long now = System.nanoTime();
        Long last = lastTick.put(uuid, now);
        if (last == null) return;

        long deltaMs = (now - last) / 1_000_000;
        if (deltaMs < 30) {
            int fast = fastTicks.merge(uuid, 1, Integer::sum);
            if (fast > 5) {
                flag(player, Math.min(1.0, (fast - 5) * 0.2), "delta", deltaMs);
            }
        } else {
            fastTicks.put(uuid, 0);
        }
    }
}
