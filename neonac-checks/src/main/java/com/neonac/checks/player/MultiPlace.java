package com.neonac.checks.player;

import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckInfo;
import com.neonac.api.packet.PlayerPlacePacket;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.check.AbstractCheck;
import com.neonac.core.check.CheckEngine;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@CheckInfo(id = "multiplace", name = "MultiPlace", category = CheckCategory.PLAYER,
        description = "Multiple blocks placed in one tick", since = 7)
public final class MultiPlace extends AbstractCheck {

    private final Map<UUID, Long> lastPlaceTick = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> placeCount = new ConcurrentHashMap<>();

    public MultiPlace(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        UUID uuid = UUID.fromString(playerUuid);
        lastPlaceTick.remove(uuid);
        placeCount.remove(uuid);
    }

    @Override
    public void onPlace(NeonACPlayer player, PlayerPlacePacket packet) {
        if (isExempt(player)) return;

        long now = System.nanoTime();
        UUID uuid = player.getUniqueId();
        Long last = lastPlaceTick.get(uuid);

        if (last != null && now - last < 50_000_000L) {
            int c = placeCount.merge(uuid, 1, Integer::sum);
            if (c >= getConfig().getInt("max-per-tick", 3)) {
                double confidence = Math.min(1.0, 0.4 + c * 0.15);
                flag(player, confidence, Map.of("count", c));
            }
        } else {
            placeCount.put(uuid, 1);
        }
        lastPlaceTick.put(uuid, now);
    }
}
