package com.neonac.core.version;

import com.neonac.api.version.MinecraftVersion;
import com.neonac.api.version.VersionAdapter;

import java.util.EnumMap;
import java.util.Map;
public final class VersionAdapterRegistry {

    private static final Map<MinecraftVersion, VersionAdapter> ADAPTERS = new EnumMap<>(MinecraftVersion.class);
    private static VersionAdapter fallback;

    private VersionAdapterRegistry() {
    }

    public static void register(VersionAdapter adapter) {
        ADAPTERS.put(adapter.getVersion(), adapter);
    }

    public static void setFallback(VersionAdapter adapter) {
        fallback = adapter;
    }

    public static VersionAdapter get(MinecraftVersion version) {
        VersionAdapter a = ADAPTERS.get(version);
        if (a != null) return a;
        // Pick the closest registered adapter at or below the requested version.
        VersionAdapter best = null;
        for (Map.Entry<MinecraftVersion, VersionAdapter> e : ADAPTERS.entrySet()) {
            if (e.getKey().getMinor() <= version.getMinor()) {
                if (best == null || e.getKey().getMinor() > best.getVersion().getMinor()) {
                    best = e.getValue();
                }
            }
        }
        return best != null ? best : (fallback != null ? fallback : new FallbackVersionAdapter());
    }
    public static class FallbackVersionAdapter implements VersionAdapter {
        @Override
        public MinecraftVersion getVersion() {
            return MinecraftVersion.UNKNOWN;
        }

        @Override
        public double getBaseGroundSpeed() {
            return 0.2158;
        }

        @Override
        public double getBaseAirSpeed() {
            return 0.02;
        }

        @Override
        public double getGravity() {
            return -0.08;
        }

        @Override
        public double getMaxFallSpeed() {
            return 3.92;
        }

        @Override
        public double getGroundFriction() {
            return 0.546;
        }

        @Override
        public double getAirFriction() {
            return 0.91;
        }

        @Override
        public double getStepHeight() {
            return 0.6;
        }

        @Override
        public boolean hasModernMovement() {
            return true;
        }

        @Override
        public boolean hasElytra() {
            return true;
        }

        @Override
        public boolean supportsAbilitiesFlying() {
            return true;
        }

        @Override
        public boolean hasModernDigTiming() {
            return true;
        }

        @Override
        public double getDefaultTolerance() {
            return 0.06;
        }
    }
}
