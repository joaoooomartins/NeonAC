package com.earac.versions;

import com.earac.api.version.MinecraftVersion;
import com.earac.api.version.VersionAdapter;

/**
 * Physics constants for legacy clients (1.7.10 – 1.8). Values reflect the pre-1.9
 * movement model (no elytra, different dig timing).
 */
public final class LegacyVersionAdapter implements VersionAdapter {

    private final MinecraftVersion version;

    public LegacyVersionAdapter(MinecraftVersion version) {
        this.version = version;
    }

    @Override
    public MinecraftVersion getVersion() {
        return version;
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
        return false;
    }

    @Override
    public boolean hasElytra() {
        return false;
    }

    @Override
    public boolean supportsAbilitiesFlying() {
        return true;
    }

    @Override
    public boolean hasModernDigTiming() {
        return false;
    }

    @Override
    public double getDefaultTolerance() {
        return 0.1;
    }
}
