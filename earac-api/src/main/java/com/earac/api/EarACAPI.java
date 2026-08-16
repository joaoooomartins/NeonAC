package com.earac.api;

import com.earac.api.check.Check;
import com.earac.api.exemption.ExemptionType;
import com.earac.api.player.EarACPlayer;
import com.earac.api.version.VersionAdapter;

import java.util.Collection;
import java.util.UUID;

/**
 * Public API surface for other plugins. The core registers a concrete
 * implementation at startup; plugins obtain it via {@link #get()}.
 */
public interface EarACAPI {

    /**
     * @return the active API instance, or null if EarAC is not loaded.
     */
    static EarACAPI get() {
        return Holder.INSTANCE;
    }

    static void set(EarACAPI instance) {
        Holder.INSTANCE = instance;
    }

    EarACPlayer getPlayer(UUID uuid);

    EarACPlayer getPlayer(String name);

    /**
     * @return the current VL for a player/check.
     */
    double getViolationLevel(UUID playerUuid, String checkId);

    Collection<Check> getRegisteredChecks();

    Check getCheck(String id);

    /**
     * Register a 3rd-party check at runtime.
     */
    void registerCheck(Check check);

    /**
     * Register a temporary exemption for a player/check.
     */
    void registerExemption(UUID playerUuid, String checkId, ExemptionType type, long durationMillis);

    /**
     * @return the version adapter for the supplied version (fallback to latest if none).
     */
    VersionAdapter getVersionAdapter(com.earac.api.version.MinecraftVersion version);

    /**
     * @return true if the plugin is fully initialised.
     */
    boolean isReady();

    class Holder {
        static EarACAPI INSTANCE;
    }
}
