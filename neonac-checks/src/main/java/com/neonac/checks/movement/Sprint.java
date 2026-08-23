package com.neonac.checks.movement;

import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckInfo;
import com.neonac.api.packet.PlayerMovePacket;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.check.AbstractCheck;
import com.neonac.core.check.CheckEngine;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@CheckInfo(id = "sprint", name = "Sprint", category = CheckCategory.MOVEMENT,
        description = "Impossible sprint state changes", since = 7)
public final class Sprint extends AbstractCheck {

    private final Map<UUID, Boolean> lastSprint = new ConcurrentHashMap<>();

    public Sprint(CheckEngine engine) {
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

        if (wasSprinting == null) return;

        if (sprinting && player.isSneaking()) {
            flag(player, 0.6, "sprint sneak", true);
        }

        if (sprinting && !wasSprinting && player.isSneaking()) {
            flag(player, 0.5, "instant sprint", true);
        }
    }
}
