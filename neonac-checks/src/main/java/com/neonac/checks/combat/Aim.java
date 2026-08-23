package com.neonac.checks.combat;

import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckInfo;
import com.neonac.api.packet.PlayerAttackPacket;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.check.AbstractCheck;
import com.neonac.core.check.CheckEngine;
import com.neonac.core.combat.CombatEngine;
import com.neonac.core.util.MathUtils;
import org.bukkit.entity.Entity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
@CheckInfo(id = "aim", name = "Aim", category = CheckCategory.COMBAT,
        description = "Impossibly fast rotation between hits", since = 8)
public final class Aim extends AbstractCheck {

    private final Map<UUID, Float> lastYaw = new ConcurrentHashMap<>();
    private final Map<UUID, Float> lastPitch = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastTime = new ConcurrentHashMap<>();
    private final Map<UUID, Double> buffer = new ConcurrentHashMap<>();

    public Aim(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        java.util.UUID uuid = java.util.UUID.fromString(playerUuid);
        lastYaw.remove(uuid);
        lastPitch.remove(uuid);
        lastTime.remove(uuid);
        buffer.remove(uuid);
    }

    @Override
    public void onAttack(NeonACPlayer player, PlayerAttackPacket packet) {
        if (isExempt(player)) return;
        Object t = packet.getTargetEntity();
        if (!(t instanceof Entity)) return;
        Entity target = (Entity) t;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        Float py = lastYaw.get(uuid);
        Float pp = lastPitch.get(uuid);
        Long pt = lastTime.get(uuid);

        lastYaw.put(uuid, player.getYaw());
        lastPitch.put(uuid, player.getPitch());
        lastTime.put(uuid, now);

        if (py == null || pt == null) return;

        float yawDelta = MathUtils.angleDelta(py, player.getYaw());
        float pitchDelta = MathUtils.angleDelta(pp, player.getPitch());
        double rotation = Math.hypot(yawDelta, pitchDelta);
        long dt = Math.max(1, now - pt);
        double speed = rotation / dt;

        double ceiling = getConfig().getDouble("max-speed", 1.2);
        if (speed <= ceiling) {
            buffer.merge(uuid, -getConfig().getVlDecay(), Double::max);
            return;
        }

        double confidence = Math.min(1.0, (speed - ceiling) / ceiling);
        double b = buffer.merge(uuid, confidence * getConfig().getVlAdd(), Double::sum);
        if (b < getConfig().getAlertThreshold()) return;

        flag(player, Math.min(1.0, b / 6.0),
                Map.of("speed", String.format("%.2f", speed), "rotation", String.format("%.1f", rotation)));
    }
}
