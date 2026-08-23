package com.neonac.checks.player;

import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckInfo;
import com.neonac.api.packet.PlayerTransactionPacket;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.check.AbstractCheck;
import com.neonac.core.check.CheckEngine;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@CheckInfo(id = "inventoryclick", name = "InventoryClick", category = CheckCategory.PLAYER,
        description = "Inventory click patterns consistent with automation", since = 7)
public final class InventoryClick extends AbstractCheck {

    private final Map<UUID, Integer> clicks = new ConcurrentHashMap<>();
    private final Map<UUID, Long> windowStart = new ConcurrentHashMap<>();

    public InventoryClick(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        UUID uuid = UUID.fromString(playerUuid);
        clicks.remove(uuid);
        windowStart.remove(uuid);
    }

    @Override
    public void onTransaction(NeonACPlayer player, PlayerTransactionPacket packet) {
        if (isExempt(player)) return;

        UUID uuid = player.getUniqueId();
        long now = System.nanoTime();
        Long start = windowStart.getOrDefault(uuid, now);
        long elapsed = now - start;

        if (elapsed > 2_000_000_000L) {
            windowStart.put(uuid, now);
            clicks.put(uuid, 0);
            return;
        }

        int count = clicks.merge(uuid, 1, Integer::sum);
        if (count > 15) {
            flag(player, Math.min(1.0, (count - 15) * 0.15), "count", count);
        }
    }
}
