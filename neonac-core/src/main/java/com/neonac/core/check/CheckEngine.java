package com.neonac.core.check;

import com.neonac.api.check.Check;
import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckRegistry;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.api.packet.PlayerAttackPacket;
import com.neonac.api.packet.PlayerDigPacket;
import com.neonac.api.packet.PlayerMovePacket;
import com.neonac.api.packet.PlayerPlacePacket;
import com.neonac.api.packet.PlayerVelocityPacket;
import com.neonac.core.NeonACPlugin;
import com.neonac.core.api.ApiEventDispatcher;
import com.neonac.core.exemption.ExemptionManager;
import com.neonac.core.metrics.Metrics;
import com.neonac.core.movement.MovementEngine;
import com.neonac.core.player.PlayerData;
import com.neonac.core.player.PlayerManager;
import com.neonac.core.violation.ViolationManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public final class CheckEngine implements CheckRegistry {

    private final NeonACPlugin plugin;
    private final ExemptionManager exemptionManager;
    private final ViolationManager violationManager;
    private final PlayerManager playerManager;
    private final MovementEngine movementEngine;
    private final Metrics metrics;

    private final Map<String, AbstractCheck> checks = new ConcurrentHashMap<>();
    private long tick = 0;

    public CheckEngine(NeonACPlugin plugin, ExemptionManager exemptionManager,
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
    public void flag(AbstractCheck check, NeonACPlayer player, double confidence, Map<String, ?> info) {
        if (!check.isEnabled()) return;
        if (exemptionManager.isExempt(player, check)) return;
        double vlAdd = check.getConfig().getVlAdd() * Math.max(0.0, Math.min(1.0, confidence));
        violationManager.flag(check, player, vlAdd, confidence, info);
        metrics.recordViolation();
    }
    public void dispatchMove(NeonACPlayer player, PlayerMovePacket packet) {
        for (AbstractCheck c : checks.values()) {
            if (!c.isEnabled()) continue;
            if (!c.supports(player.getVersion())) continue;
            if (player.isExempt(c.getId())) continue;
            metrics.recordCheck();
            c.onMove(player, packet);
        }
    }

    public void dispatchAttack(NeonACPlayer player, PlayerAttackPacket packet) {
        for (AbstractCheck c : checks.values()) {
            if (!c.isEnabled()) continue;
            if (!c.supports(player.getVersion())) continue;
            if (player.isExempt(c.getId())) continue;
            metrics.recordCheck();
            c.onAttack(player, packet);
        }
    }

    public void dispatchDig(NeonACPlayer player, PlayerDigPacket packet) {
        for (AbstractCheck c : checks.values()) {
            if (!c.isEnabled()) continue;
            if (!c.supports(player.getVersion())) continue;
            if (player.isExempt(c.getId())) continue;
            metrics.recordCheck();
            c.onDig(player, packet);
        }
    }

    public void dispatchPlace(NeonACPlayer player, PlayerPlacePacket packet) {
        for (AbstractCheck c : checks.values()) {
            if (!c.isEnabled()) continue;
            if (!c.supports(player.getVersion())) continue;
            if (player.isExempt(c.getId())) continue;
            metrics.recordCheck();
            c.onPlace(player, packet);
        }
    }

    public void dispatchVelocity(NeonACPlayer player, PlayerVelocityPacket packet) {
        for (AbstractCheck c : checks.values()) {
            if (!c.isEnabled()) continue;
            if (!c.supports(player.getVersion())) continue;
            c.onVelocity(player, packet);
        }
    }
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

    public NeonACPlugin getPlugin() {
        return plugin;
    }
}
