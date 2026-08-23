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

@CheckInfo(id = "duplicaterotplace", name = "DuplicateRotPlace", category = CheckCategory.PLAYER,
        description = "Duplicate rotation during block placement", since = 7)
public final class DuplicateRotPlace extends AbstractCheck {

    private final Map<UUID, Float> lastYaw = new ConcurrentHashMap<>();
    private final Map<UUID, Float> lastPitch = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> dupCount = new ConcurrentHashMap<>();

    public DuplicateRotPlace(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        UUID uuid = UUID.fromString(playerUuid);
        lastYaw.remove(uuid);
        lastPitch.remove(uuid);
        dupCount.remove(uuid);
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
            boolean sameYaw = Math.abs(yaw - prevYaw) < 0.01;
            boolean samePitch = Math.abs(pitch - prevPitch) < 0.01;

            if (sameYaw && samePitch) {
                int c = dupCount.merge(uuid, 1, Integer::sum);
                if (c >= getConfig().getInt("min-duplicates", 3)) {
                    double confidence = Math.min(1.0, 0.3 + c * 0.1);
                    flag(player, confidence, Map.of("duplicates", c));
                }
            } else {
                dupCount.put(uuid, 0);
            }
        }

        lastYaw.put(uuid, yaw);
        lastPitch.put(uuid, pitch);
    }
}
