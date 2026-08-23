package com.neonac.checks.movement;

import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckInfo;
import com.neonac.api.packet.PlayerMovePacket;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.check.AbstractCheck;
import com.neonac.core.check.CheckEngine;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@CheckInfo(id = "phase", name = "Phase", category = CheckCategory.MOVEMENT,
        description = "Clipping through solid blocks", since = 7)
public final class Phase extends AbstractCheck {

    private final Map<UUID, Location> lastPos = new ConcurrentHashMap<>();

    public Phase(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        lastPos.remove(UUID.fromString(playerUuid));
    }

    @Override
    public void onMove(NeonACPlayer player, PlayerMovePacket packet) {
        if (isExempt(player)) return;
        if (!packet.hasPosition()) return;

        org.bukkit.entity.Player bp = (org.bukkit.entity.Player) player.getPlatformPlayer();
        if (bp == null) return;

        UUID uuid = player.getUniqueId();
        Location prev = lastPos.put(uuid, bp.getLocation());
        if (prev == null) return;

        Location curr = bp.getLocation();
        Block head = curr.getBlock().getRelative(0, 1, 0);
        Block feet = curr.getBlock();

        if (isSolid(head.getType()) && isSolid(feet.getType())) {
            flag(player, 0.8, "blocks", feet.getType().name());
        }
    }

    private boolean isSolid(Material m) {
        return m.isSolid() && !m.name().contains("FENCE") && !m.name().contains("WALL")
                && !m.name().contains("GLASS") && !m.name().contains("STAIRS");
    }
}
