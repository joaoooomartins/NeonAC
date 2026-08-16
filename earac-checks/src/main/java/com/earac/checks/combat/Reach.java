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
 * Reach: flags attacks where the target centre is beyond the legitimate interact range.
 * Distance is the strongest single signal for reach, so it is isolated for tuning.
 */
@CheckInfo(id = "reach", name = "Reach", category = CheckCategory.COMBAT,
        description = "Attacks beyond legitimate interaction distance", since = 7)
public final class Reach extends AbstractCheck {

    private final Map<UUID, Double> buffer = new ConcurrentHashMap<>();

    public Reach(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void onAttack(EarACPlayer player, PlayerAttackPacket packet) {
        if (isExempt(player)) return;
        Object t = packet.getTargetEntity();
        if (!(t instanceof Entity)) return;
        Entity target = (Entity) t;

        double distance = CombatEngine.distanceTo(player, target);
        double maxReach = getConfig().getDouble("max-reach", 3.2);

        if (distance <= maxReach) {
            buffer.merge(player.getUniqueId(), -getConfig().getVlDecay(), Double::max);
            return;
        }

        double exceed = distance - maxReach;
        double confidence = Math.min(1.0, 0.3 + exceed * 0.6);
        UUID uuid = player.getUniqueId();
        double b = buffer.merge(uuid, confidence * getConfig().getVlAdd(), Double::sum);
        if (b < getConfig().getAlertThreshold()) return;

        flag(player, Math.min(1.0, b / 6.0),
                Map.of("distance", String.format("%.2f", distance), "max", maxReach));
    }
}
