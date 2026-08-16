package com.earac.core.debug;

import com.earac.api.player.EarACPlayer;
import com.earac.core.movement.MovementEngine;
import com.earac.core.violation.ViolationManager;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player debug toggles. When enabled for a player, detailed diagnostic snapshots
 * are produced (not broadcast) for use by {@code /earac debug}.
 */
public final class DebugManager {

    private final Set<UUID> debugTargets = ConcurrentHashMap.newKeySet();

    public void toggle(UUID uuid) {
        if (debugTargets.contains(uuid)) debugTargets.remove(uuid);
        else debugTargets.add(uuid);
    }

    public boolean isDebugging(UUID uuid) {
        return debugTargets.contains(uuid);
    }

    public void remove(UUID uuid) {
        debugTargets.remove(uuid);
    }

    /**
     * @return a human-readable snapshot for the debug command.
     */
    public String snapshot(EarACPlayer p, MovementEngine movement, ViolationManager vl) {
        double[] pos = p.getPosition();
        double[] delta = p.getDelta();
        StringBuilder sb = new StringBuilder();
        sb.append("&b== EarAC Debug: ").append(p.getName()).append(" ==\n");
        sb.append("&7Version: &f").append(p.getVersion()).append(" (").append(p.getProtocolVersion()).append(")\n");
        sb.append("&7Ping: &f").append(p.getPing()).append(" &7| TPS: &f").append(String.format("%.1f", p.getTPS())).append("\n");
        sb.append(String.format("&7Pos: &f%.2f, %.2f, %.2f\n", pos[0], pos[1], pos[2]));
        sb.append(String.format("&7Delta: &f%.4f, %.4f, %.4f\n", delta[0], delta[1], delta[2]));
        sb.append(String.format("&7Yaw: &f%.2f &7Pitch: &f%.2f\n", p.getYaw(), p.getPitch()));
        sb.append("&7Ground: &f").append(p.isOnGround())
                .append(" &7| Flying: &f").append(p.isFlying())
                .append(" &7| Water: &f").append(p.isInWater()).append("\n");
        sb.append("&7Slime: &f").append(p.isOnSlime())
                .append(" &7| Ice: &f").append(p.isOnIce())
                .append(" &7| Web: &f").append(p.isInWeb()).append("\n");
        sb.append("&7VL:\n");
        for (Map.Entry<String, Double> e : vl.getAll(p.getUniqueId()).entrySet()) {
            sb.append("  &7").append(e.getKey()).append(": &c").append(String.format("%.1f", e.getValue())).append("\n");
        }
        return sb.toString();
    }
}
