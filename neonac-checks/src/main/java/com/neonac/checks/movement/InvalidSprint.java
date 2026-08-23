package com.neonac.checks.movement;

import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckInfo;
import com.neonac.api.packet.PlayerMovePacket;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.check.AbstractCheck;
import com.neonac.core.check.CheckEngine;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@CheckInfo(id = "invalidsprint", name = "InvalidSprint", category = CheckCategory.MOVEMENT,
        description = "Sprinting in invalid conditions", since = 7)
public final class InvalidSprint extends AbstractCheck {

    private final Map<UUID, Boolean> lastSprint = new ConcurrentHashMap<>();

    public InvalidSprint(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        lastSprint.remove(UUID.fromString(playerUuid));
    }

    @Override
    public void onMove(NeonACPlayer player, PlayerMovePacket packet) {
        if (isExempt(player)) return;

        UUID uuid = player.getUniqueId();
        boolean sprinting = player.isSprinting();
        Boolean wasSprinting = lastSprint.put(uuid, sprinting);

        if (wasSprinting == null || !sprinting) return;

        Player bp = (Player) player.getPlatformPlayer();
        if (bp == null) return;

        if (bp.getFoodLevel() <= 6) {
            flag(player, 0.7, Map.of("reason", "low_food", "food", bp.getFoodLevel()));
            return;
        }

        Material block = bp.getLocation().getBlock().getRelative(0, -1, 0).getType();
        if (block == Material.SOUL_SAND || block == Material.SOUL_SOIL) {
            flag(player, 0.6, Map.of("reason", "soul_sand"));
            return;
        }

        if (bp.isBlocking()) {
            flag(player, 0.5, Map.of("reason", "blocking"));
            return;
        }
    }
}
