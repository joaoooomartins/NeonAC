package com.neonac.checks.combat;

import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckInfo;
import com.neonac.api.packet.PlayerAttackPacket;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.check.AbstractCheck;
import com.neonac.core.check.CheckEngine;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@CheckInfo(id = "hitbox", name = "HitBox", category = CheckCategory.COMBAT,
        description = "Attack angle outside legitimate hitbox", since = 7)
public final class HitBox extends AbstractCheck {

    private final Map<UUID, Double> buffer = new ConcurrentHashMap<>();

    public HitBox(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        buffer.remove(UUID.fromString(playerUuid));
    }

    @Override
    public void onAttack(NeonACPlayer player, PlayerAttackPacket packet) {
        if (isExempt(player)) return;
        if (!(packet.getTargetEntity() instanceof org.bukkit.entity.Entity)) return;
        org.bukkit.entity.Entity target = (org.bukkit.entity.Entity) packet.getTargetEntity();

        double angle = com.neonac.core.combat.CombatEngine.angleTo(player, target);
        double maxAngle = 55.0;

        if (angle < maxAngle) {
            buffer.merge(player.getUniqueId(), -getConfig().getVlDecay(), Double::max);
            return;
        }

        double confidence = Math.min(1.0, (angle - maxAngle) / 45.0);
        UUID uuid = player.getUniqueId();
        double b = buffer.merge(uuid, confidence * getConfig().getVlAdd(), Double::sum);
        if (b < getConfig().getAlertThreshold()) return;

        flag(player, Math.min(1.0, b / 5.0), "angle", String.format("%.1f", angle));
    }
}
