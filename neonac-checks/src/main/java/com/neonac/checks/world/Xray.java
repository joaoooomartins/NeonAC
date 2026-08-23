package com.neonac.checks.world;

import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckInfo;
import com.neonac.api.packet.PlayerMovePacket;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.check.AbstractCheck;
import com.neonac.core.check.CheckEngine;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@CheckInfo(id = "xray", name = "Xray", category = CheckCategory.WORLD,
        description = "Looking directly at valuable ores through walls", since = 7)
public final class Xray extends AbstractCheck {

    private static final Set<Material> VALUABLE = Set.of(
            Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
            Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.ANCIENT_DEBRIS, Material.NETHER_GOLD_ORE);

    private final Map<UUID, Integer> xrayHits = new ConcurrentHashMap<>();

    public Xray(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        xrayHits.remove(UUID.fromString(playerUuid));
    }

    @Override
    public void onMove(NeonACPlayer player, PlayerMovePacket packet) {
        if (isExempt(player)) return;
        if (!packet.hasPosition()) return;

        org.bukkit.entity.Player bp = (org.bukkit.entity.Player) player.getPlatformPlayer();
        if (bp == null) return;

        Location eye = bp.getEyeLocation();
        Location look = eye.clone().add(eye.getDirection().multiply(5));
        Material lookingAt = look.getBlock().getType();

        if (VALUABLE.contains(lookingAt)) {
            UUID uuid = player.getUniqueId();
            int hits = xrayHits.merge(uuid, 1, Integer::sum);
            if (hits > 3) {
                flag(player, Math.min(1.0, (hits - 3) * 0.2), "ore", lookingAt.name());
            }
        }
    }
}
