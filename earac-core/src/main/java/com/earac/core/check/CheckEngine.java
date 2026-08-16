package com.earac.core.check;

import com.earac.api.check.Check;
import com.earac.api.check.CheckCategory;
import com.earac.api.check.CheckRegistry;
import com.earac.api.player.EarACPlayer;
import com.earac.api.packet.PlayerAttackPacket;
import com.earac.api.packet.PlayerDigPacket;
import com.earac.api.packet.PlayerMovePacket;
import com.earac.api.packet.PlayerPlacePacket;
import com.earac.api.packet.PlayerVelocityPacket;
import com.earac.core.EarACPlugin;
import com.earac.core.api.ApiEventDispatcher;
import com.earac.core.exemption.ExemptionManager;
import com.earac.core.metrics.Metrics;
import com.earac.core.movement.MovementEngine;
import com.earac.core.player.PlayerData;
import com.earac.core.player.PlayerManager;
import com.earac.core.violation.ViolationManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry and dispatcher. Holds all checks, routes abstract packets to the
 * checks that consume them, and provides the {@link #flag} funnel that all detections
 * pass through (VL accumulation, exemption check, confidence scaling, alerts, punishments).
 */
public final class CheckEngine implements CheckRegistry {

    private final EarACPlugin plugin;
    private final ExemptionManager exemptionManager;
    private final ViolationManager violationManager;
    private final PlayerManager playerManager;
    private final MovementEngine movementEngine;
    private final Metrics metrics;

    private final Map<String, AbstractCheck> checks = new ConcurrentHashMap<>();
    private long tick = 0;

    public CheckEngine(EarACPlugin plugin, ExemptionManager exemptionManager,
                       ViolationManager violationManager, PlayerManager playerManager,
                       MovementEngine movementEngine, Metrics metrics) {
        this.plugin = plugin;
        this.exemptionManager = exemptionManager;
        this.violationManager = violationManager;
        this.playerManager = playerManager;
        this.movementEngine = movementEngine;
        this.metrics = metrics;
    }

    public void register(AbstractCheck check) {
        check.initialize(plugin.getServerVersion());
        checks.put(check.getId(), check);
    }

    @Override
    public void register(Check check) {
        if (check instanceof AbstractCheck) {
            register((AbstractCheck) check);
        }
    }

    public void registerCheck(Check check) {
        if (check instanceof AbstractCheck) {
            register((AbstractCheck) check);
        }
    }

    public Collection<AbstractCheck> getAll() {
        return checks.values();
    }

    public AbstractCheck get(String id) {
        return checks.get(id);
    }

    public List<AbstractCheck> byCategory(CheckCategory category) {
        List<AbstractCheck> out = new ArrayList<>();
        for (AbstractCheck c : checks.values()) {
            if (c.getCategory() == category) out.add(c);
        }
        return out;
    }

    public void setEnabled(String id, boolean enabled) {
        AbstractCheck c = checks.get(id);
        if (c == null) return;
        c.setEnabled(enabled);
        ApiEventDispatcher.fireCheckState(c);
    }

    public void resetPlayer(String uuid) {
        for (AbstractCheck c : checks.values()) {
            c.resetPlayer(uuid);
        }
        violationManager.reset(java.util.UUID.fromString(uuid));
        movementEngine.reset(java.util.UUID.fromString(uuid));
    }

    /**
     * Funnel for all detections. Applies exemption + confidence + VL accumulation.
     */
    public void flag(AbstractCheck check, EarACPlayer player, double confidence, Map<String, ?> info) {
        if (!check.isEnabled()) return;
        if (exemptionManager.isExempt(player, check)) return;
        double vlAdd = check.getConfig().getVlAdd() * Math.max(0.0, Math.min(1.0, confidence));
        violationManager.flag(check, player, vlAdd, confidence, info);
        metrics.recordViolation();
    }

    /** Dispatch move packets to subscribed checks. */
    public void dispatchMove(EarACPlayer player, PlayerMovePacket packet) {
        for (AbstractCheck c : checks.values()) {
            if (!c.isEnabled()) continue;
            if (!c.supports(player.getVersion())) continue;
            if (player.isExempt(c.getId())) continue;
            metrics.recordCheck();
            c.onMove(player, packet);
        }
    }

    public void dispatchAttack(EarACPlayer player, PlayerAttackPacket packet) {
        for (AbstractCheck c : checks.values()) {
            if (!c.isEnabled()) continue;
            if (!c.supports(player.getVersion())) continue;
            if (player.isExempt(c.getId())) continue;
            metrics.recordCheck();
            c.onAttack(player, packet);
        }
    }

    public void dispatchDig(EarACPlayer player, PlayerDigPacket packet) {
        for (AbstractCheck c : checks.values()) {
            if (!c.isEnabled()) continue;
            if (!c.supports(player.getVersion())) continue;
            if (player.isExempt(c.getId())) continue;
            metrics.recordCheck();
            c.onDig(player, packet);
        }
    }

    public void dispatchPlace(EarACPlayer player, PlayerPlacePacket packet) {
        for (AbstractCheck c : checks.values()) {
            if (!c.isEnabled()) continue;
            if (!c.supports(player.getVersion())) continue;
            if (player.isExempt(c.getId())) continue;
            metrics.recordCheck();
            c.onPlace(player, packet);
        }
    }

    public void dispatchVelocity(EarACPlayer player, PlayerVelocityPacket packet) {
        for (AbstractCheck c : checks.values()) {
            if (!c.isEnabled()) continue;
            if (!c.supports(player.getVersion())) continue;
            c.onVelocity(player, packet);
        }
    }

    /** Per-tick hook for checks that need periodic evaluation. */
    public void tick() {
        tick++;
        for (PlayerData p : playerManager.getAll()) {
            for (AbstractCheck c : checks.values()) {
                if (!c.isEnabled()) continue;
                if (!c.supports(p.getVersion())) continue;
                if (p.isExempt(c.getId())) continue;
                metrics.recordCheck();
                c.onTick(p, tick);
            }
        }
    }

    public long getTick() {
        return tick;
    }

    public MovementEngine getMovementEngine() {
        return movementEngine;
    }

    public ExemptionManager getExemptionManager() {
        return exemptionManager;
    }

    public EarACPlugin getPlugin() {
        return plugin;
    }
}
