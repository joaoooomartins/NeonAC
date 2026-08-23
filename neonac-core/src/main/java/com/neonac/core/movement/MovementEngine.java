package com.neonac.core.movement;

import com.neonac.api.player.NeonACPlayer;
import com.neonac.api.version.VersionAdapter;
import com.neonac.core.util.MathUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
public final class MovementEngine {

    private final Map<UUID, MovementState> states = new ConcurrentHashMap<>();

    public MovementSample analyse(NeonACPlayer p, double newX, double newY, double newZ,
                                  boolean onGround, VersionAdapter adapter) {
        MovementState s = states.computeIfAbsent(p.getUniqueId(), k -> new MovementState());

        double[] last = p.getLastPosition();
        double dx = newX - last[0];
        double dy = newY - last[1];
        double dz = newZ - last[2];
        double horiz = MathUtils.distanceHorizontal(last[0], last[2], newX, newZ);

        double tolerance = adapter.getDefaultTolerance();

        double speedMul = 1.0;
        if (p.isSprinting()) speedMul *= 1.3;
        if (p.isOnIce()) speedMul *= 1.6;
        if (p.isOnSlime()) speedMul *= 1.4;
        if (p.isInWater()) speedMul *= 0.7;
        if (p.isOnLadder()) speedMul *= 0.3;

        double base = onGround ? adapter.getBaseGroundSpeed() : adapter.getBaseAirSpeed();
        double maxHorizontal = base * speedMul + tolerance;

        if (onGround) {
            s.velocityY = 0.0;
        } else {
            s.velocityY = s.velocityY + adapter.getGravity();
            if (s.velocityY < -adapter.getMaxFallSpeed()) {
                s.velocityY = -adapter.getMaxFallSpeed();
            }
        }

        double predictedDy = s.velocityY;
        double verticalError = Math.abs(dy - predictedDy);
        double horizontalError = horiz - maxHorizontal;

        int amp = p.getPlatformPlayer() != null ? getSpeedAmplifier(p) : 0;
        if (amp > 0) {
            maxHorizontal += base * 0.2 * amp;
        }

        MovementSample sample = new MovementSample(
                dx, dy, dz, horiz,
                maxHorizontal, predictedDy,
                horizontalError, verticalError,
                tolerance);

        if (p.getLastTeleportAge() > 1_000_000_000L && p.getLastVelocityAge() > 500_000_000L) {
            s.velocityY = onGround ? 0.0 : s.velocityY;
        }
        return sample;
    }
    public void reset(UUID uuid) {
        states.remove(uuid);
    }

    private int getSpeedAmplifier(NeonACPlayer p) {
        try {
            org.bukkit.entity.Player player = (org.bukkit.entity.Player) p.getPlatformPlayer();
            org.bukkit.potion.PotionEffect effect = player.getPotionEffect(org.bukkit.potion.PotionEffectType.SPEED);
            return effect != null ? effect.getAmplifier() + 1 : 0;
        } catch (Throwable t) {
            return 0;
        }
    }

    public static final class MovementState {
        double velocityY = 0.0;
    }

    public static final class MovementSample {
        public final double dx, dy, dz, horizontal;
        public final double maxHorizontal, predictedDy;
        public final double horizontalError, verticalError;
        public final double tolerance;

        MovementSample(double dx, double dy, double dz, double horizontal,
                       double maxHorizontal, double predictedDy,
                       double horizontalError, double verticalError, double tolerance) {
            this.dx = dx;
            this.dy = dy;
            this.dz = dz;
            this.horizontal = horizontal;
            this.maxHorizontal = maxHorizontal;
            this.predictedDy = predictedDy;
            this.horizontalError = horizontalError;
            this.verticalError = verticalError;
            this.tolerance = tolerance;
        }

        public boolean exceededHorizontal(double extraTolerance) {
            return horizontalError > extraTolerance;
        }

        public boolean exceededVertical(double extraTolerance) {
            return verticalError > extraTolerance;
        }
    }
}
