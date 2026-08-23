package com.neonac.checks.player;

import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckInfo;
import com.neonac.api.packet.PlayerPlacePacket;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.check.AbstractCheck;
import com.neonac.core.check.CheckEngine;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@CheckInfo(id = "fabricatedplace", name = "FabricatedPlace", category = CheckCategory.PLAYER,
        description = "Block placed without looking at it", since = 7)
public final class FabricatedPlace extends AbstractCheck {

    private final Map<UUID, Integer> violations = new ConcurrentHashMap<>();

    public FabricatedPlace(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        UUID uuid = UUID.fromString(playerUuid);
        violations.remove(uuid);
    }

    @Override
    public void onPlace(NeonACPlayer player, PlayerPlacePacket packet) {
        if (isExempt(player)) return;

        long pos = packet.getBlockPosition();
        int bx = (int) (pos >> 26);
        int by = (int) (pos >> 12) & 0xFFF;
        int bz = (int) pos;

        double[] pp = player.getPosition();
        double dx = bx + 0.5 - pp[0];
        double dy = by + 0.5 - (pp[1] + 1.62);
        double dz = bz + 0.5 - pp[2];
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        double yaw = Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));

        float playerYaw = player.getYaw();
        float playerPitch = player.getPitch();

        double yawDiff = Math.abs(normalizeAngle(yaw) - normalizeAngle(playerYaw));
        if (yawDiff > 180) yawDiff = 360 - yawDiff;

        double pitchDiff = Math.abs(pitch - playerPitch);

        if (dist > 2.0 && yawDiff > 60) {
            int v = violations.merge(player.getUniqueId(), 1, Integer::sum);
            if (v >= getConfig().getInt("min-violations", 3)) {
                double confidence = Math.min(1.0, 0.4 + v * 0.1);
                flag(player, confidence, Map.of(
                        "yawDiff", String.format("%.1f", yawDiff),
                        "pitchDiff", String.format("%.1f", pitchDiff),
                        "distance", String.format("%.2f", dist)));
            }
        } else {
            violations.put(player.getUniqueId(), 0);
        }
    }

    private double normalizeAngle(double angle) {
        angle = angle % 360;
        if (angle < 0) angle += 360;
        return angle;
    }
}
