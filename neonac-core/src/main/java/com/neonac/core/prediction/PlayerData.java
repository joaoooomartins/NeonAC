package com.neonac.core.prediction;

import com.neonac.core.prediction.math.CollisionMath;
import com.neonac.core.prediction.math.Vector3d;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class PlayerData {

    public final Player player;
    public final long tick;

    public double x, y, z;
    public double lastX, lastY, lastZ;
    public float yaw, pitch;
    public float lastYaw, lastPitch;

    public Vector3d clientVelocity = new Vector3d();
    public Vector3d actualMovement = new Vector3d();

    public boolean onGround, lastOnGround;
    public boolean isSneaking, isSprinting, isFlying, isSwimming, isGliding, isClimbing;

    public boolean wasTouchingWater, wasTouchingLava, wasEyeInWater;

    public double gravity = 0.08;
    public float friction = 0.91f;

    public Material blockBelow = Material.AIR;
    public Material blockAt = Material.AIR;

    public int airTicks;
    public int waterTicks;

    public double fallDistance;

    public PlayerData(Player player, long tick) {
        this.player = player;
        this.tick = tick;
        updateFromBukkit();
    }

    public void updateFromBukkit() {
        this.lastX = x; this.lastY = y; this.lastZ = z;
        this.lastYaw = yaw; this.lastPitch = pitch;
        this.lastOnGround = onGround;

        org.bukkit.Location loc = player.getLocation();
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.yaw = loc.getYaw();
        this.pitch = loc.getPitch();

        this.onGround = player.isOnGround();
        this.isSneaking = player.isSneaking();
        this.isSprinting = player.isSprinting();
        this.isFlying = player.isFlying();
        this.isSwimming = player.isSwimming();
        this.isGliding = player.isGliding();

        this.actualMovement = new Vector3d(x - lastX, y - lastY, z - lastZ);

        if (lastX != 0 || lastY != 0 || lastZ != 0) {
            this.onGround = player.isOnGround();
        }
    }

    public void doBaseTick() {
        org.bukkit.World world = player.getWorld();
        blockBelow = world.getBlockAt((int) Math.floor(x), (int) Math.floor(y) - 1, (int) Math.floor(z)).getType();
        blockAt = world.getBlockAt((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z)).getType();

        wasTouchingWater = blockAt == Material.WATER
                || world.getBlockAt((int) Math.floor(x), (int) Math.floor(y + 0.1), (int) Math.floor(z)).getType() == Material.WATER;
        wasTouchingLava = blockAt == Material.LAVA;

        boolean eyeInWater = world.getBlockAt((int) Math.floor(x), (int) Math.floor(y + 1.62), (int) Math.floor(z)).getType() == Material.WATER;
        wasEyeInWater = eyeInWater;

        isClimbing = blockAt == Material.VINE || blockAt == Material.OAK_FENCE_GATE
                || blockAt.name().contains("LADDER") || blockAt.name().contains("VINE");

        friction = 0.91f;
        if (onGround) {
            friction = (float) CollisionMath.getBlockFriction(blockBelow) * 0.91f;
        }

        gravity = 0.08;
        if (isSwimming && wasTouchingWater) {
            gravity = 0.04;
        }
    }

    public double getMovementThreshold() {
        return 0.003;
    }

    public double getSpeed() {
        if (isFlying) return 0.05F * (isSprinting ? 1.5 : 1.0);
        if (isSwimming && wasTouchingWater) return 0.02F * (isSprinting ? 1.5 : 1.0);
        return isSprinting ? 0.26 : (isSneaking ? 0.065 : 0.1);
    }
}
