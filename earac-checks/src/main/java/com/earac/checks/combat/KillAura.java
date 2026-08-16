package com.earac.checks.combat;

import com.earac.api.check.CheckCategory;
import com.earac.api.check.CheckInfo;
import com.earac.api.packet.PlayerAttackPacket;
import com.earac.api.player.EarACPlayer;
import com.earac.core.check.AbstractCheck;
import com.earac.core.check.CheckEngine;
import com.earac.core.combat.CombatEngine;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * KillAura (A): detects attacks that connect despite the target being geometrically
 * unreachable — either beyond the vanilla reach or outside the player's field of view.
 * Combines reach + angle evidence rather than a single metric, and relies on the
 * exemption system for legitimate edge cases (teleport, velocity, high ping).
 */
@CheckInfo(id = "killaura", name = "KillAura", category = CheckCategory.COMBAT,
        description = "Attacks on geometrically unreachable targets", since = 7)
public final class KillAura extends AbstractCheck {

    private final Map<UUID, Double> buffer = new ConcurrentHashMap<>();

    public KillAura(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void onAttack(EarACPlayer player, PlayerAttackPacket packet) {
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
