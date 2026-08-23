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
@CheckInfo(id = "scaffold", name = "Scaffold", category = CheckCategory.PLAYER,
        description = "Repetitive downward placement pattern", since = 7)
public final class Scaffold extends AbstractCheck {

    private final Map<UUID, Integer> streak = new ConcurrentHashMap<>();

    public Scaffold(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        java.util.UUID uuid = java.util.UUID.fromString(playerUuid);
        streak.remove(uuid);
    }

    @Override
    public void onPlace(NeonACPlayer player, PlayerPlacePacket packet) {
        if (isExempt(player)) return;

        // Decode block position (x,y,z) from packed long.
        long pos = packet.getBlockPosition();
        int y = (int) ((pos >> 26) & 0xFFF);

        boolean lookingDown = player.getPitch() < -70.0f;
        boolean below = y < (int) player.getPosition()[1];

        if (lookingDown && below) {
            int s = streak.merge(player.getUniqueId(), 1, Integer::sum);
            if (s >= getConfig().getInt("min-streak", 6)) {
                double confidence = Math.min(1.0, 0.25 + s * 0.04);
                flag(player, confidence, Map.of("streak", s, "pitch", String.format("%.1f", player.getPitch())));
            }
        } else {
            streak.put(player.getUniqueId(), 0);
        }
    }
}
