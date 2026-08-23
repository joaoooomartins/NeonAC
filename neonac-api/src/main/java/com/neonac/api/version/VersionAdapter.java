package com.neonac.api.version;
public interface VersionAdapter {

    MinecraftVersion getVersion();
    double getBaseGroundSpeed();
    double getBaseAirSpeed();
    double getGravity();
    double getMaxFallSpeed();
    double getGroundFriction();
    double getAirFriction();
    double getStepHeight();
    boolean hasModernMovement();
    boolean hasElytra();
    boolean supportsAbilitiesFlying();
    boolean hasModernDigTiming();
    double getDefaultTolerance();
}
