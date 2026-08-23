package com.neonac.checks.combat;

import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckInfo;
import com.neonac.api.packet.PlayerAttackPacket;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.check.AbstractCheck;
import com.neonac.core.check.CheckEngine;
import com.neonac.core.combat.CombatEngine;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
@CheckInfo(id = "killaura", name = "KillAura", category = CheckCategory.COMBAT,
        description = "Attacks on geometrically unreachable targets", since = 7)
public final class KillAura extends AbstractCheck {

    private final Map<UUID, Double> buffer = new ConcurrentHashMap<>();

    public KillAura(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        java.util.UUID uuid = java.util.UUID.fromString(playerUuid);
        buffer.remove(uuid);
    }

    @Override
    public void onAttack(NeonACPlayer player, PlayerAttackPacket packet) {
        if (isExempt(player)) return;
        Object t = packet.getTargetEntity();
        if (!(t instanceof Entity)) return;
        Entity target = (Entity) t;

        double distance = CombatEngine.distanceTo(player, target);
        double angle = CombatEngine.angleTo(player, target);

        double maxReach = 3.1; // vanilla ~3.0; small slack
        double maxAngle = 45.0;

        double confidence = 0.0;
        if (distance > maxReach) {
            confidence += Math.min(0.8, (distance - maxReach) * 0.5);
        }
        // Allow some angle slack for legitimate flicks; only flag large misses.
        if (angle > maxAngle) {
            confidence += Math.min(0.6, (angle - maxAngle) / 90.0);
        }

        if (confidence <= 0.0) {
            buffer.merge(player.getUniqueId(), -getConfig().getVlDecay(), Double::max);
            return;
        }

        UUID uuid = player.getUniqueId();
        double b = buffer.merge(uuid, confidence * getConfig().getVlAdd(), Double::sum);
        if (b < getConfig().getAlertThreshold()) return;

        flag(player, Math.min(1.0, b / 5.0),
                Map.of("distance", String.format("%.2f", distance), "angle", String.format("%.1f", angle)));
    }
}
