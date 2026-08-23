package com.neonac.checks.player;

import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckInfo;
import com.neonac.api.packet.PlayerPlacePacket;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.check.AbstractCheck;
import com.neonac.core.check.CheckEngine;
import org.bukkit.Location;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@CheckInfo(id = "farplace", name = "FarPlace", category = CheckCategory.PLAYER,
        description = "Block placed from too far away", since = 7)
public final class FarPlace extends AbstractCheck {

    private final Map<UUID, Long> lastPlaceTime = new ConcurrentHashMap<>();

    public FarPlace(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        UUID uuid = UUID.fromString(playerUuid);
        lastPlaceTime.remove(uuid);
    }

    @Override
    public void onPlace(NeonACPlayer player, PlayerPlacePacket packet) {
        if (isExempt(player)) return;

        long pos = packet.getBlockPosition();
        int bx = (int) (pos >> 26);
        int by = (int) (pos >> 12) & 0xFFF;
        int bz = (int) pos;

        double[] pp = player.getPosition();
        double dx = bx + 0.5 - pp[0];
        double dy = by + 0.5 - (pp[1] + 1.62);
        double dz = bz + 0.5 - pp[2];
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        double maxReach = 4.5;
        double threshold = getConfig().getDouble("max-reach", maxReach);

        if (distance > threshold) {
            double confidence = Math.min(1.0, (distance - threshold) / 2.0);
            flag(player, confidence, Map.of(
                    "distance", String.format("%.2f", distance),
                    "max", String.format("%.1f", threshold)));
        }
    }
}
