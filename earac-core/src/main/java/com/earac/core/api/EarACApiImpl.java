package com.earac.core.api;

import com.earac.api.EarACAPI;
import com.earac.api.check.Check;
import com.earac.api.exemption.ExemptionType;
import com.earac.api.player.EarACPlayer;
import com.earac.api.version.MinecraftVersion;
import com.earac.api.version.VersionAdapter;
import com.earac.core.EarACPlugin;
import com.earac.core.exemption.ExemptionManager;
import com.earac.core.player.PlayerData;
import com.earac.core.version.VersionAdapterRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * Public API implementation backed by the live plugin. Registered on enable.
 */
public final class EarACApiImpl implements EarACAPI {

    private final EarACPlugin plugin;

    public EarACApiImpl(EarACPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public EarACPlayer getPlayer(UUID uuid) {
        return plugin.getPlayerManager().get(uuid);
    }

    @Override
    public EarACPlayer getPlayer(String name) {
        return plugin.getPlayerManager().get(name);
    }

    @Override
    public double getViolationLevel(UUID playerUuid, String checkId) {
        return plugin.getViolationManager().getVL(playerUuid, checkId);
    }

    @Override
    public Collection<Check> getRegisteredChecks() {
        return new ArrayList<>(plugin.getCheckEngine().getAll());
    }

    @Override
    public Check getCheck(String id) {
        return plugin.getCheckEngine().get(id);
    }

    @Override
    public void registerCheck(Check check) {
        plugin.getCheckEngine().registerCheck(check);
    }

    @Override
public void registerExemption(UUID playerUuid, String checkId, ExemptionType type, long durationMillis) {
        plugin.getExemptionManager().addTimedExemption(playerUuid, type, durationMillis);
    }

    @Override
    public VersionAdapter getVersionAdapter(MinecraftVersion version) {
        return VersionAdapterRegistry.get(version);
    }

    @Override
    public boolean isReady() {
        return plugin.getCheckEngine() != null && plugin.getServerVersion() != null;
    }
}
