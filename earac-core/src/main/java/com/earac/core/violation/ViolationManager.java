package com.earac.core.violation;

import com.earac.api.check.Check;
import com.earac.api.player.EarACPlayer;
import com.earac.api.violation.Violation;
import com.earac.core.EarACPlugin;
import com.earac.core.alert.AlertManager;
import com.earac.core.punishment.PunishmentManager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Accumulates Violation Level (VL) per player/check. Detections raise VL scaled by
 * confidence; absence of detections decays it. When VL crosses the alert/punish
 * thresholds the respective managers are invoked. This decoupling means a single
 * weak detection never directly punishes.
 */
public final class ViolationManager {

    private final EarACPlugin plugin;
    private final AlertManager alertManager;
    private final PunishmentManager punishmentManager;

    /** playerUuid -> (checkId -> vl) */
    private final Map<UUID, Map<String, Double>> levels = new ConcurrentHashMap<>();
    /** playerUuid -> (checkId -> last detection nanos) */
    private final Map<UUID, Map<String, Long>> lastDetection = new ConcurrentHashMap<>();

    public ViolationManager(EarACPlugin plugin, AlertManager alertManager, PunishmentManager punishmentManager) {
        this.plugin = plugin;
        this.alertManager = alertManager;
        this.punishmentManager = punishmentManager;
    }

    /**
     * Apply a detection: accumulate VL and route to alerts/punishments.
     */
    public void flag(Check check, EarACPlayer player, double vlAdded, double confidence, Map<String, ?> info) {
        UUID uuid = player.getUniqueId();
        Map<String, Double> pl = levels.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
        Map<String, Long> ld = lastDetection.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());

        double current = pl.getOrDefault(check.getId(), 0.0);
        double max = check.getConfig().getVlMax();
        double next = Math.min(max, current + vlAdded);
        pl.put(check.getId(), next);
        ld.put(check.getId(), System.nanoTime());

        Violation v = new ViolationImpl(uuid.toString(), player.getName(), check, vlAdded, next, confidence, info);
        com.earac.core.api.ApiEventDispatcher.fireViolation(v);

        if (next >= check.getConfig().getAlertThreshold()) {
            alertManager.sendAlert(v);
        }
        double punishThreshold = check.getConfig().getPunishThreshold();
        if (punishThreshold > 0 && next >= punishThreshold) {
            punishmentManager.punish(v);
            // reset to avoid repeated punishment spam for the same threshold
            pl.put(check.getId(), Math.max(0, next - punishThreshold));
        }
    }

    /**
     * Exponential-style decay for every (player,check) that has not detected recently.
     */
    public void decayTick() {
        long now = System.nanoTime();
        for (Map.Entry<UUID, Map<String, Double>> e : levels.entrySet()) {
            Map<String, Long> ld = lastDetection.get(e.getKey());
            Map<String, Double> pl = e.getValue();
            for (Map.Entry<String, Double> cv : pl.entrySet()) {
                long last = ld != null ? ld.getOrDefault(cv.getKey(), 0L) : 0L;
                if (now - last < 1_000_000_000L) continue; // recent detection -> no decay yet
                double decay = 0.05; // global default; per-check decay applied by check context
                double nv = cv.getValue() - decay;
                if (nv <= 0) pl.put(cv.getKey(), 0.0);
                else pl.put(cv.getKey(), nv);
            }
        }
    }

    public double getVL(UUID uuid, String checkId) {
        Map<String, Double> pl = levels.get(uuid);
        return pl != null ? pl.getOrDefault(checkId, 0.0) : 0.0;
    }

    public Map<String, Double> getAll(UUID uuid) {
        return levels.getOrDefault(uuid, new ConcurrentHashMap<>());
    }

    public void reset(UUID uuid) {
        levels.remove(uuid);
        lastDetection.remove(uuid);
    }

    public void setVL(UUID uuid, String checkId, double vl) {
        levels.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(checkId, vl);
    }
}
