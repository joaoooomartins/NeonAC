package com.earac.core.version;

import com.earac.api.version.MinecraftVersion;
import com.earac.api.version.VersionAdapter;
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
        // Requesting 1.21 (not registered) should return the 1.20 adapter (closest lower).
        assertSame(modern, VersionAdapterRegistry.get(MinecraftVersion.V1_21));
    }
}
