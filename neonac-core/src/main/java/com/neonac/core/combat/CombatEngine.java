package com.neonac.core.combat;

import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.util.MathUtils;

import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
public final class CombatEngine {

    private CombatEngine() {
    }
    public static double distanceTo(NeonACPlayer player, Entity target) {
        Vector eye = eyePosition(player);
        Vector centre = target.getLocation().add(0, target.getHeight() / 2.0, 0).toVector();
        return eye.distance(centre);
    }
    public static double angleTo(NeonACPlayer player, Entity target) {
        Vector dir = directionVector(player.getYaw(), player.getPitch());
        Vector toTarget = target.getLocation().add(0, target.getHeight() / 2.0, 0)
                .toVector().subtract(eyePosition(player));
        if (toTarget.lengthSquared() == 0) return 0;
        toTarget.normalize();
        double dot = MathUtils.clamp(dir.dot(toTarget), -1.0, 1.0);
        return Math.toDegrees(Math.acos(dot));
    }
    public static boolean withinReach(NeonACPlayer player, Entity target, double maxReach) {
        return distanceTo(player, target) <= maxReach;
    }

    public static Vector eyePosition(NeonACPlayer player) {
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
