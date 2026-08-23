package com.neonac.core.version;

import com.neonac.api.version.MinecraftVersion;
import com.neonac.api.version.VersionAdapter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class VersionAdapterRegistryTest {

    @Test
    void fallbackUsedWhenNothingRegistered() {
        VersionAdapterRegistry.setFallback(new VersionAdapterRegistry.FallbackVersionAdapter());
        VersionAdapter a = VersionAdapterRegistry.get(MinecraftVersion.V1_20);
        assertNotNull(a);
        assertEquals(MinecraftVersion.UNKNOWN, a.getVersion());
    }

    @Test
    void closestLowerAdapterSelected() {
        VersionAdapterRegistry.setFallback(new VersionAdapterRegistry.FallbackVersionAdapter());
        VersionAdapter modern = new VersionAdapterRegistry.FallbackVersionAdapter() {
            @Override public MinecraftVersion getVersion() { return MinecraftVersion.V1_20; }
        };
        VersionAdapterRegistry.register(modern);
        assertSame(modern, VersionAdapterRegistry.get(MinecraftVersion.V1_21));
    }
}
