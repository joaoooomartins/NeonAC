package com.neonac.versions;

import com.neonac.api.version.MinecraftVersion;
import com.neonac.core.version.VersionAdapterRegistry;
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
