package com.neonac.checks.world;

import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckInfo;
import com.neonac.api.packet.PlayerMovePacket;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.check.AbstractCheck;
import com.neonac.core.check.CheckEngine;
import org.bukkit.Location;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@CheckInfo(id = "freecam", name = "Freecam", category = CheckCategory.WORLD,
        description = "Camera position diverges from body position", since = 7)
public final class Freecam extends AbstractCheck {

    private final Map<UUID, Location> lastBody = new ConcurrentHashMap<>();

    public Freecam(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        lastBody.remove(UUID.fromString(playerUuid));
    }

    @Override
    public void onMove(NeonACPlayer player, PlayerMovePacket packet) {
        if (isExempt(player)) return;
        if (!packet.hasPosition()) return;

        org.bukkit.entity.Player bp = (org.bukkit.entity.Player) player.getPlatformPlayer();
        if (bp == null) return;

        UUID uuid = player.getUniqueId();
        Location body = bp.getLocation();
        lastBody.put(uuid, body);

        Location eye = bp.getEyeLocation();
        double dist = eye.distance(body);

        if (dist > 5.0) {
            flag(player, 0.9, "distance", String.format("%.1f", dist));
        }
    }
}
