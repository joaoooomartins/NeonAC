package com.neonac.core.player;

import com.neonac.api.player.NeonACPlayer;
import com.neonac.api.version.MinecraftVersion;
import com.neonac.api.version.ProtocolVersion;
import com.neonac.core.util.TimeUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class PlayerData implements NeonACPlayer {

    private final Player player;
    private final MinecraftVersion version;
    private final ProtocolVersion protocol;

    private double lastX, lastY, lastZ;
    private double x, y, z;
    private float lastYaw, lastPitch;
    private float yaw, pitch;
    private boolean onGround;

    private long lastTeleportNanos = -1;
    private long lastVelocityNanos = -1;

    private double velocityX, velocityY, velocityZ;

    private boolean cachedInWater, cachedInLava, cachedOnLadder, cachedInWeb;
    private boolean cachedOnSlime, cachedOnIce;
    private double lastCacheX = Double.NaN, lastCacheY = Double.NaN, lastCacheZ = Double.NaN;

    public PlayerData(Player player, MinecraftVersion version, ProtocolVersion protocol) {
        this.player = player;
        this.version = version;
        this.protocol = protocol;
        Location l = player.getLocation();
        this.x = l.getX();
        this.y = l.getY();
        this.z = l.getZ();
        this.lastX = x;
        this.lastY = y;
        this.lastZ = z;
        this.yaw = l.getYaw();
        this.pitch = l.getPitch();
        this.lastYaw = yaw;
        this.lastPitch = pitch;
        this.onGround = l.getY() % 1 == 0;
    }

    public void update(double nx, double ny, double nz, float nyaw, float npitch, boolean ground) {
        this.lastX = this.x;
        this.lastY = this.y;
        this.lastZ = this.z;
        this.lastYaw = this.yaw;
        this.lastPitch = this.pitch;
        this.x = nx;
        this.y = ny;
        this.z = nz;
        this.yaw = nyaw;
        this.pitch = npitch;
        this.onGround = ground;
        invalidateCache();
    }

    private void invalidateCache() {
        if (Math.abs(x - lastCacheX) > 0.5 || Math.abs(y - lastCacheY) > 0.5 || Math.abs(z - lastCacheZ) > 0.5) {
            lastCacheX = Double.NaN;
        }
    }

    private void refreshCache() {
        if (!Double.isNaN(lastCacheX)) return;
        lastCacheX = x;
        lastCacheY = y;
        lastCacheZ = z;
        Location l = player.getLocation();
        cachedInWater = checkMaterial(l, Material.WATER);
        cachedInLava = checkMaterial(l, Material.LAVA);
        cachedOnLadder = checkMaterial(l, Material.LADDER) || checkMaterial(l, Material.VINE);
        cachedInWeb = checkMaterial(l, Material.COBWEB);
        cachedOnSlime = checkMaterialBelow(l, Material.SLIME_BLOCK);
        cachedOnIce = checkMaterialBelow(l, Material.ICE) || checkMaterialBelow(l, Material.PACKED_ICE)
                || checkMaterialBelow(l, Material.FROSTED_ICE) || checkMaterialBelow(l, Material.BLUE_ICE);
    }

    private boolean checkMaterial(Location l, Material m) {
        return l.getBlock().getType() == m
                || l.clone().add(0, 1, 0).getBlock().getType() == m
                || l.clone().add(0, -1, 0).getBlock().getType() == m;
    }

    private boolean checkMaterialBelow(Location l, Material m) {
        return l.clone().add(0, -1, 0).getBlock().getType() == m;
    }

    public void markTeleport() {
        this.lastTeleportNanos = TimeUtils.nanos();
    }

    public void markVelocity() {
        this.lastVelocityNanos = TimeUtils.nanos();
    }

    public void applyVelocity(double vx, double vy, double vz) {
        this.velocityX = vx;
        this.velocityY = vy;
        this.velocityZ = vz;
        this.lastVelocityNanos = TimeUtils.nanos();
    }

    public double getVelocityX() { return velocityX; }
    public double getVelocityY() { return velocityY; }
    public double getVelocityZ() { return velocityZ; }
    public void setVelocityY(double vy) { this.velocityY = vy; }

    @Override public UUID getUniqueId() { return player.getUniqueId(); }
    @Override public String getName() { return player.getName(); }
    @Override public MinecraftVersion getVersion() { return version; }
    @Override public ProtocolVersion getProtocolVersion() { return protocol; }

    @Override
    public int getPing() {
        try { return player.getPing(); } catch (Throwable t) { return 0; }
    }

    @Override public double getTPS() { return TpsTracker.getTPS(); }
    @Override public boolean isOnline() { return player.isOnline(); }
    @Override public boolean isSprinting() { return player.isSprinting(); }
    @Override public boolean isSneaking() { return player.isSneaking(); }
    @Override public boolean isOnGround() { return onGround; }

    @Override
    public boolean isFlying() {
        try { return player.isFlying(); } catch (Throwable t) { return false; }
    }

    @Override
    public boolean isInWater() {
        refreshCache();
        return cachedInWater;
    }

    @Override
    public boolean isInLava() {
        refreshCache();
        return cachedInLava;
    }

    @Override
    public boolean isOnLadder() {
        refreshCache();
        return cachedOnLadder;
    }

    @Override
    public boolean isInWeb() {
        refreshCache();
        return cachedInWeb;
    }

    @Override
    public boolean isOnSlime() {
        refreshCache();
        return cachedOnSlime;
    }

    @Override
    public boolean isOnIce() {
        refreshCache();
        return cachedOnIce;
    }

    @Override
    public boolean isGliding() {
        try { return player.isGliding(); } catch (Throwable t) { return false; }
    }

    @Override public boolean isOnVehicle() { return player.isInsideVehicle(); }
    @Override public boolean isDead() { return player.isDead(); }

    @Override
    public boolean isCreative() {
        return player.getGameMode().name().equalsIgnoreCase("CREATIVE");
    }

    @Override
    public boolean isSpectator() {
        return player.getGameMode().name().equalsIgnoreCase("SPECTATOR");
    }

    @Override public double[] getPosition() { return new double[]{x, y, z}; }
    @Override public double[] getLastPosition() { return new double[]{lastX, lastY, lastZ}; }

    @Override
    public double[] getDelta() {
        return new double[]{x - lastX, y - lastY, z - lastZ};
    }

    @Override public float getYaw() { return yaw; }
    @Override public float getPitch() { return pitch; }
    @Override public float getLastYaw() { return lastYaw; }
    @Override public float getLastPitch() { return lastPitch; }
    @Override public Object getPlatformPlayer() { return player; }

    @Override
    public boolean isExempt(String checkId) {
        return false;
    }

    @Override
    public long getLastTeleportAge() {
        return lastTeleportNanos < 0 ? Long.MAX_VALUE : TimeUtils.nanos() - lastTeleportNanos;
    }

    @Override
    public long getLastVelocityAge() {
        return lastVelocityNanos < 0 ? Long.MAX_VALUE : TimeUtils.nanos() - lastVelocityNanos;
    }

    public Player getBukkitPlayer() {
        return player;
    }
}
