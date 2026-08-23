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

@CheckInfo(id = "rotationplace", name = "RotationPlace", category = CheckCategory.PLAYER,
        description = "Block placed with impossible rotation", since = 7)
public final class RotationPlace extends AbstractCheck {

    private final Map<UUID, Float> lastYaw = new ConcurrentHashMap<>();
    private final Map<UUID, Float> lastPitch = new ConcurrentHashMap<>();

    public RotationPlace(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        UUID uuid = UUID.fromString(playerUuid);
        lastYaw.remove(uuid);
        lastPitch.remove(uuid);
    }

    @Override
    public void onPlace(NeonACPlayer player, PlayerPlacePacket packet) {
        if (isExempt(player)) return;

        UUID uuid = player.getUniqueId();
        float yaw = player.getYaw();
        float pitch = player.getPitch();

        Float prevYaw = lastYaw.get(uuid);
        Float prevPitch = lastPitch.get(uuid);

        if (prevYaw != null && prevPitch != null) {
            long pos = packet.getBlockPosition();
            int bx = (int) (pos >> 26);
            int by = (int) (pos >> 12) & 0xFFF;
            int bz = (int) pos;

            double[] pp = player.getPosition();
            double dx = bx + 0.5 - pp[0];
            double dy = by + 0.5 - (pp[1] + 1.62);
            double dz = bz + 0.5 - pp[2];

            double expectedYaw = Math.toDegrees(Math.atan2(-dx, dz));
            double expectedPitch = Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));

            double yawDiff = Math.abs(normalizeAngle(yaw) - normalizeAngle(expectedYaw));
            if (yawDiff > 180) yawDiff = 360 - yawDiff;
            double pitchDiff = Math.abs(pitch - expectedPitch);

            if (yawDiff > 45 && pitchDiff > 30) {
                flag(player, Math.min(1.0, (yawDiff + pitchDiff) / 120.0),
                        Map.of("yawDiff", String.format("%.1f", yawDiff),
                               "pitchDiff", String.format("%.1f", pitchDiff)));
            }
        }

        lastYaw.put(uuid, yaw);
        lastPitch.put(uuid, pitch);
    }

    private double normalizeAngle(double angle) {
        angle = angle % 360;
        if (angle < 0) angle += 360;
        return angle;
    }
}
