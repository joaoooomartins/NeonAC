package com.neonac.core.violation;

import com.neonac.api.check.Check;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.api.violation.Violation;
import com.neonac.core.NeonACPlugin;
import com.neonac.core.alert.AlertManager;
import com.neonac.core.mode.ModeLogger;
import com.neonac.core.mode.ModeManager;
import com.neonac.core.punishment.PunishmentManager;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ViolationManager {

    private final NeonACPlugin plugin;
    private final AlertManager alertManager;
    private final PunishmentManager punishmentManager;
    private ModeManager modeManager;
    private ModeLogger modeLogger;
    private final Map<UUID, Map<String, Double>> levels = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Long>> lastDetection = new ConcurrentHashMap<>();
    private final Map<UUID, ViolationHistory> history = new ConcurrentHashMap<>();
    private final Set<UUID> dirty = ConcurrentHashMap.newKeySet();

    private static final long DEFAULT_WINDOW_MS = 5 * 60 * 1000L;

    public ViolationManager(NeonACPlugin plugin, AlertManager alertManager, PunishmentManager punishmentManager) {
        this.plugin = plugin;
        this.alertManager = alertManager;
        this.punishmentManager = punishmentManager;
    }

    public void setModeManager(ModeManager modeManager) {
        this.modeManager = modeManager;
    }

    public void setModeLogger(ModeLogger modeLogger) {
        this.modeLogger = modeLogger;
    }

    public void flag(Check check, NeonACPlayer player, double vlAdded, double confidence, Map<String, ?> info) {
        UUID uuid = player.getUniqueId();
        Map<String, Double> pl = levels.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
        Map<String, Long> ld = lastDetection.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());

        double multiplier = modeManager != null ? modeManager.getVlMultiplier() : 1.0;
        double adjustedVl = vlAdded * multiplier;

        double current = pl.getOrDefault(check.getId(), 0.0);
        double max = check.getConfig().getVlMax();
        double next = Math.min(max, current + adjustedVl);
        pl.put(check.getId(), next);
        ld.put(check.getId(), System.nanoTime());
        dirty.add(uuid);

        ViolationHistory vh = history.computeIfAbsent(uuid, k -> new ViolationHistory(DEFAULT_WINDOW_MS));
        vh.record(check.getId(), adjustedVl);

        Violation v = new ViolationImpl(uuid.toString(), player.getName(), check, adjustedVl, next, confidence, info);
        com.neonac.core.api.ApiEventDispatcher.fireViolation(v);

        if (modeManager != null && modeLogger != null) {
            String mode = modeManager.getMode().name().toLowerCase();
            if (modeManager.isSilent() || modeManager.isLogging()) {
                modeLogger.log(v, mode);
            }
        }

        if (modeManager != null && !modeManager.shouldAlert()) return;

        if (next >= check.getConfig().getAlertThreshold()) {
            alertManager.sendAlert(v);
        }

        if (modeManager == null || !modeManager.shouldPunish()) return;

        double punishThreshold = check.getConfig().getPunishThreshold();
        if (punishThreshold > 0 && next >= punishThreshold) {
            punishmentManager.punish(v);
            pl.put(check.getId(), Math.max(0, next - punishThreshold));
        }
    }

    public void decayTick() {
        if (dirty.isEmpty()) return;
        long now = System.nanoTime();
        for (UUID uuid : dirty) {
            Map<String, Double> pl = levels.get(uuid);
            Map<String, Long> ld = lastDetection.get(uuid);
            if (pl == null) {
                dirty.remove(uuid);
                continue;
            }
            boolean allZero = true;
            for (Map.Entry<String, Double> cv : pl.entrySet()) {
                long last = ld != null ? ld.getOrDefault(cv.getKey(), 0L) : 0L;
                if (now - last < 1_000_000_000L) {
                    allZero = false;
                    continue;
                }
                double nv = cv.getValue() - 0.05;
                if (nv <= 0) {
                    pl.put(cv.getKey(), 0.0);
                } else {
                    pl.put(cv.getKey(), nv);
                    allZero = false;
                }
            }
            if (allZero) dirty.remove(uuid);
        }
    }

    public double getVL(UUID uuid, String checkId) {
        Map<String, Double> pl = levels.get(uuid);
        return pl != null ? pl.getOrDefault(checkId, 0.0) : 0.0;
    }

    public Map<String, Double> getAll(UUID uuid) {
        return levels.getOrDefault(uuid, new ConcurrentHashMap<>());
    }

    public ViolationHistory getHistory(UUID uuid) {
        return history.get(uuid);
    }

    public void reset(UUID uuid) {
        levels.remove(uuid);
        lastDetection.remove(uuid);
        history.remove(uuid);
        dirty.remove(uuid);
    }

    public void setVL(UUID uuid, String checkId, double vl) {
        levels.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(checkId, vl);
        if (vl > 0) dirty.add(uuid);
    }

    public Map<UUID, Map<String, Double>> getAllLevels() {
        return levels;
    }

    public void cleanExpiredTimedExemptions() {
        plugin.getExemptionManager().cleanExpired();
    }
}
