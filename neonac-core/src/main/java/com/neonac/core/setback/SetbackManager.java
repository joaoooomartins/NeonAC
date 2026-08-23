package com.neonac.core.setback;

import com.neonac.api.check.Check;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.NeonACPlugin;
import com.neonac.core.check.CheckEngine;
import com.neonac.core.exemption.ExemptionManager;
import com.neonac.core.violation.ViolationManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SetbackManager {

    private static final long SETBACK_COOLDOWN_MS = 1000;
    private static final double MIN_CONFIDENCE = 0.7;

    private final NeonACPlugin plugin;
    private final CheckEngine engine;
    private final ViolationManager violationManager;
    private final ExemptionManager exemptionManager;
    private final SafePositionManager safePositionManager;

    private final Map<UUID, Long> lastSetback = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> setbackCount = new ConcurrentHashMap<>();

    private boolean enabled = true;
    private int maxSetbacksPerMinute = 3;
    private double minConfidence = MIN_CONFIDENCE;

    public SetbackManager(NeonACPlugin plugin, CheckEngine engine,
                          ViolationManager violationManager, ExemptionManager exemptionManager) {
        this.plugin = plugin;
        this.engine = engine;
        this.violationManager = violationManager;
        this.exemptionManager = exemptionManager;
        this.safePositionManager = new SafePositionManager();
    }

    public void loadConfig() {
        this.enabled = plugin.getConfig().getBoolean("setback.enabled", true);
        this.maxSetbacksPerMinute = plugin.getConfig().getInt("setback.max-per-minute", 3);
        this.minConfidence = plugin.getConfig().getDouble("setback.min-confidence", MIN_CONFIDENCE);
    }

    public void onMove(Player player) {
        if (!enabled) return;
        safePositionManager.update(player);
    }

    public boolean trySetback(NeonACPlayer player, Check check, double confidence, Map<String, ?> info) {
        if (!enabled) return false;
        if (check.getConfig().getPunishThreshold() <= 0) return false;

        UUID uuid = player.getUniqueId();
        Player bukkit = (Player) player.getPlatformPlayer();
        if (bukkit == null || !bukkit.isOnline()) return false;

        if (player.getLastTeleportAge() < 500_000_000L) return false;
        if (player.getLastVelocityAge() < 1_000_000_000L) return false;
        if (confidence < minConfidence) return false;

        Long last = lastSetback.get(uuid);
        if (last != null && System.currentTimeMillis() - last < SETBACK_COOLDOWN_MS) return false;

        Integer count = setbackCount.getOrDefault(uuid, 0);
        if (count >= maxSetbacksPerMinute) return false;

        Location safe = safePositionManager.getLastSafe(uuid);
        if (safe == null) return false;

        Location current = bukkit.getLocation();
        if (safe.getWorld() == null || current.getWorld() == null) return false;
        if (!safe.getWorld().equals(current.getWorld())) return false;

        double distSq = safe.distanceSquared(current);
        if (distSq < 0.5) return false;

        if (exemptionManager.isExempt(player, check)) return false;

        if (bukkit.hasPermission("neonac.nosetback") || bukkit.hasPermission("neonac.nosetback." + check.getId())) return false;

        double vl = violationManager.getVL(uuid, check.getId());
        double punishThreshold = check.getConfig().getPunishThreshold();
        if (vl < punishThreshold * 0.5) return false;

        bukkit.teleport(safe);

        lastSetback.put(uuid, System.currentTimeMillis());
        setbackCount.merge(uuid, 1, Integer::sum);

        return true;
    }

    public void decayCounts() {
        long now = System.currentTimeMillis();
        setbackCount.entrySet().removeIf(e -> {
            Long last = lastSetback.get(e.getKey());
            return last == null || now - last > 60_000;
        });
    }

    public void removePlayer(UUID uuid) {
        safePositionManager.remove(uuid);
        lastSetback.remove(uuid);
        setbackCount.remove(uuid);
    }

    public SafePositionManager getSafePositionManager() {
        return safePositionManager;
    }

    public boolean isEnabled() { return enabled; }
}
