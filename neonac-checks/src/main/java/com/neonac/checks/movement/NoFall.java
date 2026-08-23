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
@CheckInfo(id = "nofall", name = "NoFall", category = CheckCategory.MOVEMENT,
        description = "Suppressed fall distance / fall damage", since = 7)
public final class NoFall extends AbstractCheck {

    private final Map<UUID, Double> fallStart = new ConcurrentHashMap<>();

    public NoFall(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        java.util.UUID uuid = java.util.UUID.fromString(playerUuid);
        fallStart.remove(uuid);
    }

    @Override
    public void onMove(NeonACPlayer player, PlayerMovePacket packet) {
        if (isExempt(player)) return;
        UUID uuid = player.getUniqueId();

        if (!packet.isOnGround()) {
            fallStart.putIfAbsent(uuid, packet.getY());
            return;
        }

        Double start = fallStart.remove(uuid);
        if (start == null) return;

        double fall = start - packet.getY();
        double minFall = getConfig().getDouble("min-fall", 3.0);
        if (fall < minFall) return;

        double reported = 0.0;
        try {
            org.bukkit.entity.Player p = (org.bukkit.entity.Player) player.getPlatformPlayer();
            reported = p.getFallDistance();
        } catch (Throwable ignored) {
        }

        if (reported >= fall * 0.5) return; // consistent with a real fall

        double confidence = Math.min(1.0, 0.3 + (fall - minFall) / 5.0);
        flag(player, confidence, Map.of("fall", String.format("%.2f", fall), "reported", String.format("%.2f", reported)));
    }
}
