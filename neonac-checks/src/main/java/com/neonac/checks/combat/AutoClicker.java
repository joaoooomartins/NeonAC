package com.neonac.checks.combat;

import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckInfo;
import com.neonac.api.packet.PlayerAttackPacket;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.check.AbstractCheck;
import com.neonac.core.check.CheckEngine;
import com.neonac.core.util.MathUtils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
@CheckInfo(id = "autoclicker", name = "AutoClicker", category = CheckCategory.COMBAT,
        description = "Robotic click timing / abnormal CPS", since = 7)
public final class AutoClicker extends AbstractCheck {

    private final Map<UUID, Deque<Long>> clicks = new ConcurrentHashMap<>();
    private final Map<UUID, Double> buffer = new ConcurrentHashMap<>();

    public AutoClicker(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        java.util.UUID uuid = java.util.UUID.fromString(playerUuid);
        clicks.remove(uuid);
        buffer.remove(uuid);
    }

    @Override
    public void onAttack(NeonACPlayer player, PlayerAttackPacket packet) {
        if (isExempt(player)) return;
        UUID uuid = player.getUniqueId();
        Deque<Long> q = clicks.computeIfAbsent(uuid, k -> new ArrayDeque<>());
        long now = System.currentTimeMillis();
        q.addLast(now);
        while (!q.isEmpty() && now - q.peekFirst() > 1500) q.pollFirst();

        if (q.size() < 10) return;

        double cps = q.size() / 1.5;
        double maxCps = getConfig().getDouble("max-cps", 16.0);
        if (cps <= maxCps) {
            buffer.merge(uuid, -getConfig().getVlDecay(), Double::max);
            return;
        }

        // Variance of intervals: low variance + high rate => bot.
        double[] intervals = new double[q.size() - 1];
        Long[] arr = q.toArray(new Long[0]);
        for (int i = 1; i < arr.length; i++) intervals[i - 1] = arr[i] - arr[i - 1];
        double std = MathUtils.stdDev(intervals);
        double consistency = Math.max(0.0, 1.0 - std / 60.0); // tighter than 60ms apart => suspicious

        double confidence = Math.min(1.0, 0.3 + (cps - maxCps) / maxCps) * (0.5 + 0.5 * consistency);
        double b = buffer.merge(uuid, confidence * getConfig().getVlAdd(), Double::sum);
        if (b < getConfig().getAlertThreshold()) return;

        flag(player, Math.min(1.0, b / 8.0),
                Map.of("cps", String.format("%.1f", cps), "std", String.format("%.1f", std)));
    }
}
