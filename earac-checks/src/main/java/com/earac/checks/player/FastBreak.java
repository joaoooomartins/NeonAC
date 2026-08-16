package com.earac.checks.player;

import com.earac.api.check.CheckCategory;
import com.earac.api.check.CheckInfo;
import com.earac.api.packet.PlayerDigPacket;
import com.earac.api.player.EarACPlayer;
import com.earac.core.check.AbstractCheck;
import com.earac.core.check.CheckEngine;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FastBreak: limits block-break completion rate. Uses the dig "finish" action only,
 * so the rate reflects actual completed breaks rather than click spam.
 */
@CheckInfo(id = "fastbreak", name = "FastBreak", category = CheckCategory.PLAYER,
        description = "Abnormally fast block breaking", since = 7)
public final class FastBreak extends AbstractCheck {

    private final Map<UUID, Deque<Long>> times = new ConcurrentHashMap<>();
    private final Map<UUID, Double> buffer = new ConcurrentHashMap<>();

    public FastBreak(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void onDig(EarACPlayer player, PlayerDigPacket packet) {
        if (isExempt(player)) return;
        if (packet.getAction() != 2) return; // finish only
        UUID uuid = player.getUniqueId();
        Deque<Long> q = times.computeIfAbsent(uuid, k -> new ArrayDeque<>());
        long now = System.currentTimeMillis();
        q.addLast(now);
        while (!q.isEmpty() && now - q.peekFirst() > 2000) q.pollFirst();

        if (q.size() < 5) return;
        double rate = q.size() / 2.0;
        double max = getConfig().getDouble("max-rate", 8.0);
        if (rate <= max) {
            buffer.merge(uuid, -getConfig().getVlDecay(), Double::max);
            return;
        }
        double confidence = Math.min(1.0, 0.3 + (rate - max) / max);
        double b = buffer.merge(uuid, confidence * getConfig().getVlAdd(), Double::sum);
        if (b < getConfig().getAlertThreshold()) return;
        flag(player, Math.min(1.0, b / 6.0), Map.of("rate", String.format("%.1f", rate)));
    }
}
