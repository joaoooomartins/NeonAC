package com.earac.versions;

import com.earac.api.version.MinecraftVersion;
import com.earac.api.version.VersionAdapter;

/**
 * Physics constants for modern clients (1.9+). Elytra, modern dig timing and the
 * updated movement model are enabled.
 */
public final class ModernVersionAdapter implements VersionAdapter {

    private final MinecraftVersion version;

    public ModernVersionAdapter(MinecraftVersion version) {
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
        return -0.085;
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
        return 0.98;
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
