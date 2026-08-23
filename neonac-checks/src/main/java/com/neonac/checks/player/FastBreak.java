package com.neonac.checks.player;

import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckInfo;
import com.neonac.api.packet.PlayerDigPacket;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.check.AbstractCheck;
import com.neonac.core.check.CheckEngine;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
@CheckInfo(id = "fastbreak", name = "FastBreak", category = CheckCategory.PLAYER,
        description = "Abnormally fast block breaking", since = 7)
public final class FastBreak extends AbstractCheck {

    private final Map<UUID, Deque<Long>> times = new ConcurrentHashMap<>();
    private final Map<UUID, Double> buffer = new ConcurrentHashMap<>();

    public FastBreak(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        java.util.UUID uuid = java.util.UUID.fromString(playerUuid);
        times.remove(uuid);
        buffer.remove(uuid);
    }

    @Override
    public void onDig(NeonACPlayer player, PlayerDigPacket packet) {
        if (isExempt(player)) return;
        if (packet.getAction() != 2) return;
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
