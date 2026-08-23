package com.neonac.checks.player;

import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckInfo;
import com.neonac.api.packet.PlayerPlacePacket;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.check.AbstractCheck;
import com.neonac.core.check.CheckEngine;
import org.bukkit.Material;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@CheckInfo(id = "airliquidplace", name = "AirLiquidPlace", category = CheckCategory.PLAYER,
        description = "Block placed against air or liquid (scaffold indicator)", since = 7)
public final class AirLiquidPlace extends AbstractCheck {

    private final Map<UUID, Integer> count = new ConcurrentHashMap<>();

    public AirLiquidPlace(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        UUID uuid = UUID.fromString(playerUuid);
        count.remove(uuid);
    }

    @Override
    public void onPlace(NeonACPlayer player, PlayerPlacePacket packet) {
        if (isExempt(player)) return;

        long pos = packet.getBlockPosition();
        int bx = (int) (pos >> 26);
        int by = (int) (pos >> 12) & 0xFFF;
        int bz = (int) pos;

        org.bukkit.World world = (org.bukkit.World) player.getPlatformPlayer();
        if (world == null) return;

        Material placed = world.getBlockAt(bx, by, bz).getType();
        if (placed == Material.AIR || placed == Material.CAVE_AIR || placed == Material.VOID_AIR) return;

        boolean againstAir = false;
        int face = packet.getBlockFace();
        int ax = bx, ay = by, az = bz;
        switch (face) {
            case 0: ay--; break;
            case 1: ay++; break;
            case 2: az--; break;
            case 3: az++; break;
            case 4: ax--; break;
            case 5: ax++; break;
        }

        Material against = world.getBlockAt(ax, ay, az).getType();
        if (against == Material.AIR || against == Material.CAVE_AIR || against == Material.VOID_AIR) {
            againstAir = true;
        } else if (against.name().contains("WATER") || against.name().contains("LAVA")) {
            againstAir = true;
        }

        if (againstAir) {
            int c = count.merge(player.getUniqueId(), 1, Integer::sum);
            if (c >= getConfig().getInt("min-count", 5)) {
                double confidence = Math.min(1.0, 0.3 + c * 0.05);
                flag(player, confidence, Map.of("count", c, "against", against.name()));
            }
        } else {
            count.put(player.getUniqueId(), 0);
        }
    }
}
