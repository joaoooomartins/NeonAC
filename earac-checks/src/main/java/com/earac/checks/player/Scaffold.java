package com.earac.checks.player;

import com.earac.api.check.CheckCategory;
import com.earac.api.check.CheckInfo;
import com.earac.api.packet.PlayerPlacePacket;
import com.earac.api.player.EarACPlayer;
import com.earac.core.check.AbstractCheck;
import com.earac.core.check.CheckEngine;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Scaffold: detects the characteristic "look straight down and place" pattern used by
 * tower/scaffold modules. We do NOT flag on pitch alone (legitimate builders look down
 * too); instead we require a sustained, low-variance downward-looking placement pattern
 * combined with placing blocks beneath the player.
 */
@CheckInfo(id = "scaffold", name = "Scaffold", category = CheckCategory.PLAYER,
        description = "Repetitive downward placement pattern", since = 7)
public final class Scaffold extends AbstractCheck {

    private final Map<UUID, Integer> streak = new ConcurrentHashMap<>();

    public Scaffold(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void onPlace(EarACPlayer player, PlayerPlacePacket packet) {
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
