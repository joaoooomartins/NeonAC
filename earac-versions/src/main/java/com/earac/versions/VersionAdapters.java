package com.earac.versions;

import com.earac.api.version.MinecraftVersion;
import com.earac.core.version.VersionAdapterRegistry;

/**
 * Registers precise per-version adapters. The core calls {@link #registerAll()} via
 * reflection on startup; if this module is absent the core's fallback adapter is used.
 *
 * <p>Mapping strategy: legacy clients (1.7.10 – 1.8) use {@link LegacyVersionAdapter};
 * 1.9+ use {@link ModernVersionAdapter}. Additional per-minor tweaks can be added here
 * without touching any check or engine code.</p>
 */
public final class VersionAdapters {

    private VersionAdapters() {
    }

    public static void registerAll() {
        VersionAdapterRegistry.register(new LegacyVersionAdapter(MinecraftVersion.V1_7_10));
        VersionAdapterRegistry.register(new LegacyVersionAdapter(MinecraftVersion.V1_8));
        for (MinecraftVersion v : new MinecraftVersion[]{
                MinecraftVersion.V1_9, MinecraftVersion.V1_12, MinecraftVersion.V1_13,
                MinecraftVersion.V1_14, MinecraftVersion.V1_15, MinecraftVersion.V1_16,
                MinecraftVersion.V1_17, MinecraftVersion.V1_18, MinecraftVersion.V1_19,
                MinecraftVersion.V1_20, MinecraftVersion.V1_21, MinecraftVersion.V1_22,
                MinecraftVersion.V1_23, MinecraftVersion.V1_24, MinecraftVersion.V1_25,
                MinecraftVersion.V1_26
        }) {
            VersionAdapterRegistry.register(new ModernVersionAdapter(v));
        }
    }
}
