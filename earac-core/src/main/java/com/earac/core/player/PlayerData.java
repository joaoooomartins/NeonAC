package com.earac.core.player;

import com.earac.api.player.EarACPlayer;
import com.earac.api.version.MinecraftVersion;
import com.earac.api.version.ProtocolVersion;
import com.earac.core.util.TimeUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Per-player state wrapper. Reads from the Bukkit API only (no NMS). Tracks the
 * last two positions for delta computation and the age of server-induced
 * teleports / velocity, which feeds exemptions.
 */
public final class PlayerData implements EarACPlayer {

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
    private long joinNanos = TimeUtils.nanos();

    private double velocityX, velocityY, velocityZ;

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
        this.onGround = l.getY() % 1 == 0; // rough; refined by move packets
    }

    /**
     * Updates position/rotation from a movement sample (called by the packet layer).
     */
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

    public double getVelocityX() {
        return velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public double getVelocityZ() {
        return velocityZ;
    }

    public void setVelocityY(double vy) {
        this.velocityY = vy;
    }

    public void setOnline(boolean online) {
        // handled by manager; kept for symmetry
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public MinecraftVersion getVersion() {
        return version;
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return protocol;
    }

    @Override
    public int getPing() {
        try {
            return player.getPing();
        } catch (Throwable t) {
            return 0;
        }
    }

    @Override
    public double getTPS() {
        return TpsTracker.getTPS();
    }

    @Override
    public boolean isOnline() {
        return player.isOnline();
    }

    @Override
    public boolean isSprinting() {
        return player.isSprinting();
    }

    @Override
    public boolean isSneaking() {
        return player.isSneaking();
    }

    @Override
    public boolean isOnGround() {
        return onGround;
    }

    @Override
    public boolean isFlying() {
        return player.isFlying();
    }

    @Override
    public boolean isInWater() {
        return isMaterialNearby(Material.WATER);
    }

    @Override
    public boolean isInLava() {
        return isMaterialNearby(Material.LAVA);
    }

    @Override
    public boolean isOnLadder() {
        return isMaterialNearby(Material.LADDER) || isMaterialNearby(Material.VINE);
    }

    @Override
    public boolean isInWeb() {
        return isMaterialNearby(Material.COBWEB);
    }

    @Override
    public boolean isOnSlime() {
        return isMaterialBelow(Material.SLIME_BLOCK);
    }

    @Override
    public boolean isOnIce() {
        return isMaterialBelow(Material.ICE) || isMaterialBelow(Material.PACKED_ICE)
                || isMaterialBelow(Material.FROSTED_ICE) || isMaterialBelow(Material.BLUE_ICE);
    }

    @Override
    public boolean isGliding() {
        try {
            return player.isGliding();
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public boolean isOnVehicle() {
        return player.isInsideVehicle();
    }

    @Override
    public boolean isDead() {
        return player.isDead();
    }

    @Override
    public boolean isCreative() {
        return player.getGameMode().name().equalsIgnoreCase("CREATIVE");
    }

    @Override
    public boolean isSpectator() {
        return player.getGameMode().name().equalsIgnoreCase("SPECTATOR");
    }

    @Override
    public double[] getPosition() {
        return new double[]{x, y, z};
    }

    @Override
    public double[] getLastPosition() {
        return new double[]{lastX, lastY, lastZ};
    }

    @Override
    public double[] getDelta() {
        return new double[]{x - lastX, y - lastY, z - lastZ};
    }

    @Override
    public float getYaw() {
        return yaw;
    }

    @Override
    public float getPitch() {
        return pitch;
    }

    @Override
    public float getLastYaw() {
        return lastYaw;
    }

    @Override
    public float getLastPitch() {
        return lastPitch;
    }

    @Override
    public Object getPlatformPlayer() {
        return player;
    }

    @Override
    public boolean isExempt(String checkId) {
        return false; // resolved by ExemptionManager via the player instance
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

    private boolean isMaterialNearby(Material m) {
        Location l = player.getLocation();
        return l.getBlock().getType() == m
                || l.clone().add(0, 1, 0).getBlock().getType() == m
                || l.clone().add(0, -1, 0).getBlock().getType() == m;
    }

    private boolean isMaterialBelow(Material m) {
        return player.getLocation().clone().add(0, -1, 0).getBlock().getType() == m;
    }

    @SuppressWarnings("unused")
    private ItemStack getHand() {
        return player.getInventory().getItemInMainHand();
    }
}
