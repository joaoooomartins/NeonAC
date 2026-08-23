package com.neonac.api;

import com.neonac.api.check.Check;
import com.neonac.api.check.CheckCategory;
import com.neonac.api.exemption.ExemptionType;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.api.version.VersionAdapter;

import java.util.Collection;
import java.util.UUID;

public interface NeonACAPI {
    static NeonACAPI get() {
        return Holder.INSTANCE;
    }

    static void set(NeonACAPI instance) {
        Holder.INSTANCE = instance;
    }

    NeonACPlayer getPlayer(UUID uuid);
    NeonACPlayer getPlayer(String name);
    double getViolationLevel(UUID playerUuid, String checkId);
    Collection<Check> getRegisteredChecks();
    Check getCheck(String id);
    void registerCheck(Check check);
    VersionAdapter getVersionAdapter(com.neonac.api.version.MinecraftVersion version);
    boolean isReady();

    void addGlobalExemption(UUID playerUuid, ExemptionType type, long durationMillis);
    void addCheckExemption(UUID playerUuid, String checkId, ExemptionType type, long durationMillis);
    void addCategoryExemption(UUID playerUuid, CheckCategory category, ExemptionType type, long durationMillis);
    void removeExemption(UUID playerUuid, ExemptionType type);

    class Holder {
        static NeonACAPI INSTANCE;
    }
}
