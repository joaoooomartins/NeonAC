package com.earac.api.exemption;

/**
 * Known exemption reasons that checks consult before flagging.
 * Plugins may register custom reasons via the API.
 */
public enum ExemptionType {

    /** Player teleported recently. */
    TELEPORT,
    /** Server applied velocity/knockback recently. */
    VELOCITY,
    /** Player is in water/lava. */
    LIQUID,
    /** Player is on a ladder/vine. */
    CLIMBABLE,
    /** Player is in cobweb. */
    WEB,
    /** Player is on slime. */
    SLIME,
    /** Player is on ice. */
    ICE,
    /** Player is gliding (elytra). */
    GLIDING,
    /** Player is in a vehicle. */
    VEHICLE,
    /** Player is dead/respawning. */
    DEAD,
    /** Creative/spectator gamemode. */
    GAMEMODE,
    /** Server TPS too low to trust movement. */
    LOW_TPS,
    /** Player ping too high to trust timing. */
    HIGH_PING,
    /** A 3rd party plugin requested exemption (e.g. minigame). */
    PLUGIN;

    public String getKey() {
        return name().toLowerCase();
    }
}
