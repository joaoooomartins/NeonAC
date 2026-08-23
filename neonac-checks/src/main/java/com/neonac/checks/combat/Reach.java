package com.neonac.checks.combat;

import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckInfo;
import com.neonac.api.packet.PlayerAttackPacket;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.check.AbstractCheck;
import com.neonac.core.check.CheckEngine;
import com.neonac.core.combat.CombatEngine;
import org.bukkit.entity.Entity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
@CheckInfo(id = "reach", name = "Reach", category = CheckCategory.COMBAT,
        description = "Attacks beyond legitimate interaction distance", since = 7)
public final class Reach extends AbstractCheck {

    private final Map<UUID, Double> buffer = new ConcurrentHashMap<>();

    public Reach(CheckEngine engine) {
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
