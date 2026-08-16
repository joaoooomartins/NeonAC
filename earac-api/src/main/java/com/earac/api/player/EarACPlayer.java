package com.earac.api.player;

import com.earac.api.version.MinecraftVersion;
import com.earac.api.version.ProtocolVersion;

import java.util.UUID;

/**
 * Version-agnostic view of a monitored player. Wraps the server player without
 * exposing NMS. Provides the data engines and checks need (ping, position,
 * velocity, ground state, version, TPS, latest interactions).
 */
public interface EarACPlayer {

    UUID getUniqueId();

    String getName();

    MinecraftVersion getVersion();

    ProtocolVersion getProtocolVersion();

    int getPing();

    /**
     * Server TPS sampled at the last tick.
     */
    double getTPS();

    boolean isOnline();

    boolean isSprinting();

    boolean isSneaking();

    boolean isOnGround();

    boolean isFlying();

    boolean isInWater();

    boolean isInLava();

    boolean isOnLadder();

    boolean isInWeb();

    boolean isOnSlime();

    boolean isOnIce();

    boolean isGliding();

    boolean isOnVehicle();

    boolean isDead();

    boolean isCreative();

    boolean isSpectator();

    /**
     * @return the last known X, Y, Z.
     */
    double[] getPosition();

    /**
     * @return the previous X, Y, Z.
     */
    double[] getLastPosition();

    /**
     * @return delta between last and current position.
     */
    double[] getDelta();

    float getYaw();

    float getPitch();

    float getLastYaw();

    float getLastPitch();

    /**
     * @return the underlying server player handle (cast by adapters).
     */
    Object getPlatformPlayer();

    /**
     * @return true if the player is currently exempt from the given check id.
     */
    boolean isExempt(String checkId);

    /**
     * @return nanoseconds since the last server teleport applied to this player.
     */
    long getLastTeleportAge();

    /**
     * @return nanoseconds since the last server velocity (knockback) applied.
     */
    long getLastVelocityAge();
}
