package com.earac.checks.movement;

import com.earac.api.check.CheckCategory;
import com.earac.api.check.CheckInfo;
import com.earac.api.packet.PlayerMovePacket;
import com.earac.api.player.EarACPlayer;
import com.earac.core.check.AbstractCheck;
import com.earac.core.check.CheckEngine;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NoFall: tracks vertical drop since the player left the ground. When the player lands
 * after a significant fall but the platform reports little/no fall distance (i.e. fall
 * damage was avoided), this indicates the client suppressed fall handling.
 */
@CheckInfo(id = "nofall", name = "NoFall", category = CheckCategory.MOVEMENT,
        description = "Suppressed fall distance / fall damage", since = 7)
public final class NoFall extends AbstractCheck {

    private final Map<UUID, Double> fallStart = new ConcurrentHashMap<>();

    public NoFall(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void onMove(EarACPlayer player, PlayerMovePacket packet) {
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
