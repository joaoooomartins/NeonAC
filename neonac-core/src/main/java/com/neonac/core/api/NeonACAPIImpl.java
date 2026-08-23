package com.neonac.core.api;

import com.neonac.api.NeonACAPI;
import com.neonac.api.check.Check;
import com.neonac.api.check.CheckCategory;
import com.neonac.api.exemption.ExemptionType;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.api.version.MinecraftVersion;
import com.neonac.api.version.VersionAdapter;
import com.neonac.core.NeonACPlugin;
import com.neonac.core.version.VersionAdapterRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public final class NeonACAPIImpl implements NeonACAPI {

    private final NeonACPlugin plugin;

    public NeonACAPIImpl(NeonACPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public NeonACPlayer getPlayer(UUID uuid) {
        return plugin.getPlayerManager().get(uuid);
    }

    @Override
    public NeonACPlayer getPlayer(String name) {
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
    public VersionAdapter getVersionAdapter(MinecraftVersion version) {
        return VersionAdapterRegistry.get(version);
    }

    @Override
    public boolean isReady() {
        return plugin.getCheckEngine() != null && plugin.getServerVersion() != null;
    }

    @Override
    public void addGlobalExemption(UUID playerUuid, ExemptionType type, long durationMillis) {
        plugin.getExemptionManager().addTimedGlobal(playerUuid, type, durationMillis);
    }

    @Override
    public void addCheckExemption(UUID playerUuid, String checkId, ExemptionType type, long durationMillis) {
        plugin.getExemptionManager().addTimedCheck(playerUuid, checkId, type, durationMillis);
    }

    @Override
    public void addCategoryExemption(UUID playerUuid, CheckCategory category, ExemptionType type, long durationMillis) {
        plugin.getExemptionManager().addTimedCategory(playerUuid, category, type, durationMillis);
    }

    @Override
    public void removeExemption(UUID playerUuid, ExemptionType type) {
        plugin.getExemptionManager().removeGlobalExemption(playerUuid, type);
    }
}
