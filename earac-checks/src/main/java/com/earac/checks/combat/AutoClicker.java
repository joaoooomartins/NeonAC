package com.earac.checks.combat;

import com.earac.api.check.CheckCategory;
import com.earac.api.check.CheckInfo;
import com.earac.api.packet.PlayerAttackPacket;
import com.earac.api.player.EarACPlayer;
import com.earac.core.check.AbstractCheck;
import com.earac.core.check.CheckEngine;
import com.earac.core.util.MathUtils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AutoClicker / CPS: tracks attack timing. A human click pattern has natural variance;
 * a bot produces a near-constant, very high click rate. Flags when CPS exceeds the
 * legitimate ceiling AND the inter-click interval variance is abnormally low.
 */
@CheckInfo(id = "autoclicker", name = "AutoClicker", category = CheckCategory.COMBAT,
        description = "Robotic click timing / abnormal CPS", since = 7)
public final class AutoClicker extends AbstractCheck {

    private final Map<UUID, Deque<Long>> clicks = new ConcurrentHashMap<>();
    private final Map<UUID, Double> buffer = new ConcurrentHashMap<>();

    public AutoClicker(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void onAttack(EarACPlayer player, PlayerAttackPacket packet) {
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
