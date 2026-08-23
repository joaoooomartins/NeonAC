package com.neonac.api.player;

import com.neonac.api.version.MinecraftVersion;
import com.neonac.api.version.ProtocolVersion;

import java.util.UUID;
public interface NeonACPlayer {

    UUID getUniqueId();

    String getName();

    MinecraftVersion getVersion();

    ProtocolVersion getProtocolVersion();

    int getPing();
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
    double[] getPosition();
    double[] getLastPosition();
    double[] getDelta();

    float getYaw();

    float getPitch();

    float getLastYaw();

    float getLastPitch();
    Object getPlatformPlayer();
    boolean isExempt(String checkId);
    long getLastTeleportAge();
    long getLastVelocityAge();
}
