package com.earac.checks.player;

import com.earac.api.check.CheckCategory;
import com.earac.api.check.CheckInfo;
import com.earac.api.packet.PlayerPlacePacket;
import com.earac.api.player.EarACPlayer;
import com.earac.core.check.AbstractCheck;
import com.earac.core.check.CheckEngine;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FastPlace: limits block placement rate. Legitimate building has a natural ceiling;
 * placing significantly faster than a human can click is anomalous.
 */
@CheckInfo(id = "fastplace", name = "FastPlace", category = CheckCategory.PLAYER,
        description = "Abnormally fast block placement", since = 7)
public final class FastPlace extends AbstractCheck {

    private final Map<UUID, Deque<Long>> times = new ConcurrentHashMap<>();
    private final Map<UUID, Double> buffer = new ConcurrentHashMap<>();

    public FastPlace(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void onPlace(EarACPlayer player, PlayerPlacePacket packet) {
        if (isExempt(player)) return;
        UUID uuid = player.getUniqueId();
        Deque<Long> q = times.computeIfAbsent(uuid, k -> new ArrayDeque<>());
        long now = System.currentTimeMillis();
        q.addLast(now);
        while (!q.isEmpty() && now - q.peekFirst() > 1500) q.pollFirst();

        if (q.size() < 6) return;
        double rate = q.size() / 1.5; // places per second
        double max = getConfig().getDouble("max-rate", 14.0);
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
