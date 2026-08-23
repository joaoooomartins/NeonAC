package com.neonac.core.prediction;

import com.neonac.core.prediction.math.CollisionBox;
import com.neonac.core.prediction.math.CollisionMath;
import com.neonac.core.prediction.math.Vector3d;

import java.util.List;

public final class PredictionEngine {

    public static Vector3d predict(PlayerData data) {
        Vector3d velocity = estimateClientVelocity(data);

        applyGravity(data, velocity);
        applyMovementInput(data, velocity);
        applyFriction(data, velocity);

        return velocity;
    }

    private static Vector3d estimateClientVelocity(PlayerData data) {
        Vector3d lastVel = new Vector3d(data.x - data.lastX, data.y - data.lastY, data.z - data.lastZ);
        return lastVel;
    }

    private static void applyGravity(PlayerData data, Vector3d vel) {
        if (data.isFlying) {
            double flySpeed = 0.05 * (data.isSprinting ? 1.5 : 1.0);
            if (data.isSneaking) {
                vel.y -= flySpeed * 3;
            }
            return;
        }

        if (data.wasTouchingWater && !data.isFlying) {
            vel.y -= 0.04;
            if (vel.y < -0.15) vel.y = -0.15;
            return;
        }

        vel.y -= data.gravity;
    }

    private static void applyMovementInput(PlayerData data, Vector3d vel) {
        float yaw = (float) Math.toRadians(data.yaw);

        int forward = data.isSprinting ? 1 : (data.isSneaking ? 0 : 0);
        int strafe = 0;

        double inputX = strafe * Math.cos(yaw) - forward * Math.sin(yaw);
        double inputZ = forward * Math.cos(yaw) + strafe * Math.sin(yaw);

        double inputLen = Math.sqrt(inputX * inputX + inputZ * inputZ);
        if (inputLen > 1) {
            inputX /= inputLen;
            inputZ /= inputLen;
        }

        double speed = data.getSpeed();
        vel.x += inputX * speed;
        vel.z += inputZ * speed;

        if (data.onGround && data.isSprinting) {
            vel.x += -Math.sin(yaw) * 0.2;
            vel.z += Math.cos(yaw) * 0.2;
        }
    }

    private static void applyFriction(PlayerData data, Vector3d vel) {
        double friction = data.friction;
        vel.x *= friction;
        vel.z *= friction;
        vel.y *= 0.98;
    }

    public static Vector3d resolveCollision(PlayerData data, Vector3d desired) {
        if (desired.x == 0 && desired.y == 0 && desired.z == 0) return new Vector3d();

        double stepHeight = data.onGround ? 0.6 : 0.0;

        CollisionBox playerBox = getPlayerBoundingBox(data.x, data.y, data.z);

        CollisionBox searchBox = playerBox.copy().expand(
                Math.max(Math.abs(desired.x), Math.abs(data.x - data.lastX)),
                Math.max(Math.abs(desired.y), Math.abs(data.y - data.lastY)),
                Math.max(Math.abs(desired.z), Math.abs(data.z - data.lastZ)));

        List<CollisionBox> blocks = CollisionMath.getCollisionBoxes(data.player.getWorld(), searchBox);

        Vector3d result = CollisionMath.collide(playerBox, desired.x, desired.y, desired.z, blocks);

        if (stepHeight > 0 && result.x != desired.x || result.z != desired.z) {
            if (data.lastOnGround || desired.y < 0) {
                CollisionBox stepBox = playerBox.copy();
                stepBox.offset(0, stepHeight, 0);
                Vector3d stepResult = CollisionMath.collide(stepBox, desired.x, 0, desired.z, blocks);

                if (stepResult.x * stepResult.x + stepResult.z * stepResult.z
                        > result.x * result.x + result.z * result.z) {
                    Vector3d fallResult = CollisionMath.collide(
                            stepBox.copy().offset(stepResult.x, 0, stepResult.z),
                            0, desired.y - stepHeight, 0, blocks);
                    result = new Vector3d(stepResult.x, stepHeight + fallResult.y, stepResult.z);
                }
            }
        }

        return result;
    }

    public static CollisionBox getPlayerBoundingBox(double x, double y, double z) {
        return new CollisionBox(x - 0.3, y, z - 0.3, x + 0.3, y + 1.8, z + 0.3);
    }

    public static double calculateOffset(PlayerData data, Vector3d predicted) {
        Vector3d actual = data.actualMovement;
        double dx = actual.x - predicted.x;
        double dy = actual.y - predicted.y;
        double dz = actual.z - predicted.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
