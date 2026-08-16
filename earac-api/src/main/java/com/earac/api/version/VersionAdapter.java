package com.earac.api.version;

/**
 * Isolates all version-specific physics, constants and capability queries.
 * The core and engines only ever talk to this interface — never to NMS or
 * scattered {@code if (version == ...)} checks.
 */
public interface VersionAdapter {

    MinecraftVersion getVersion();

    /**
     * Base walking speed (blocks/tick) applied on the ground without modifiers.
     */
    double getBaseGroundSpeed();

    /**
     * Base walking speed (blocks/tick) applied in the air.
     */
    double getBaseAirSpeed();

    /**
     * Gravity applied per tick (negative = downwards).
     */
    double getGravity();

    /**
     * Maximum fall speed (terminal velocity, blocks/tick).
     */
    double getMaxFallSpeed();

    /**
     * Horizontal friction factor applied per tick on the ground.
     */
    double getGroundFriction();

    /**
     * Horizontal friction factor applied per tick in the air.
     */
    double getAirFriction();

    /**
     * Maximum step height (blocks) the client can climb automatically.
     */
    double getStepHeight();

    /**
     * Whether the client supports the modern sprint/jump movement model (1.9+).
     */
    boolean hasModernMovement();

    /**
     * Whether elytra flight exists in this version (1.9+).
     */
    boolean hasElytra();

    /**
     * Whether the client reports itself as "flying" via the abilities packet.
     */
    boolean supportsAbilitiesFlying();

    /**
     * Whether this version uses the newer block break/place timing model.
     */
    boolean hasModernDigTiming();

    /**
     * Default tolerance applied to movement prediction for this version.
     */
    double getDefaultTolerance();
}
