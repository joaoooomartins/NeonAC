package com.neonac.api.exemption;
public enum ExemptionType {
    TELEPORT,
    VELOCITY,
    LIQUID,
    CLIMBABLE,
    WEB,
    SLIME,
    ICE,
    GLIDING,
    VEHICLE,
    DEAD,
    GAMEMODE,
    LOW_TPS,
    HIGH_PING,
    PLUGIN;

    public String getKey() {
        return name().toLowerCase();
    }
}
