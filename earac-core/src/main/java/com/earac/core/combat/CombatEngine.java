package com.earac.core.combat;

import com.earac.api.player.EarACPlayer;
import com.earac.core.util.MathUtils;

import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

/**
 * Combat analysis helpers shared by combat checks: geometry, angles and
 * line-of-sight. These are pure math and do not depend on a specific client version.
 */
public final class CombatEngine {

    private CombatEngine() {
    }

    /**
     * @return Euclidean distance from the player's eyes to the target's centre.
     */
    public static double distanceTo(EarACPlayer player, Entity target) {
        Vector eye = eyePosition(player);
        Vector centre = target.getLocation().add(0, target.getHeight() / 2.0, 0).toVector();
        return eye.distance(centre);
    }

    /**
     * @return the angle (degrees) between where the player is looking and the
     * direction to the target's centre.
     */
    public static double angleTo(EarACPlayer player, Entity target) {
        Vector dir = directionVector(player.getYaw(), player.getPitch());
        Vector toTarget = target.getLocation().add(0, target.getHeight() / 2.0, 0)
                .toVector().subtract(eyePosition(player));
        if (toTarget.lengthSquared() == 0) return 0;
        toTarget.normalize();
        double dot = MathUtils.clamp(dir.dot(toTarget), -1.0, 1.0);
        return Math.toDegrees(Math.acos(dot));
    }

    /**
     * @return true if the target is within a reasonable hitbox reach (vanilla ~3.0 blocks).
     */
    public static boolean withinReach(EarACPlayer player, Entity target, double maxReach) {
        return distanceTo(player, target) <= maxReach;
    }

    public static Vector eyePosition(EarACPlayer player) {
        double[] pos = player.getPosition();
        double eyeHeight = player.isSneaking() ? 1.54 : 1.62;
        return new Vector(pos[0], pos[1] + eyeHeight, pos[2]);
    }

    public static Vector directionVector(float yaw, float pitch) {
        double radYaw = Math.toRadians(yaw + 90.0);
        double radPitch = Math.toRadians(-pitch);
        double cosPitch = Math.cos(radPitch);
        return new Vector(cosPitch * Math.cos(radYaw), Math.sin(radPitch), cosPitch * Math.sin(radYaw));
    }
}
