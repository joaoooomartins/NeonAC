package com.neonac.checks.movement;

import com.neonac.api.check.CheckCategory;
import com.neonac.api.check.CheckInfo;
import com.neonac.api.packet.PlayerMovePacket;
import com.neonac.api.player.NeonACPlayer;
import com.neonac.core.check.AbstractCheck;
import com.neonac.core.check.CheckEngine;
import com.neonac.core.prediction.PredictionEngine;
import com.neonac.core.prediction.PlayerData;
import com.neonac.core.prediction.UncertaintyHandler;
import com.neonac.core.prediction.math.Vector3d;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@CheckInfo(id = "simulation", name = "Simulation", category = CheckCategory.MOVEMENT,
        description = "Moved differently than predicted movement simulation", since = 7)
public final class Simulation extends AbstractCheck {

    private final Map<UUID, PlayerData> playerData = new ConcurrentHashMap<>();
    private final Map<UUID, UncertaintyHandler> uncertainty = new ConcurrentHashMap<>();
    private final Map<UUID, Double> buffer = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> tickCount = new ConcurrentHashMap<>();

    public Simulation(CheckEngine engine) {
        super(engine);
    }

    @Override
    public void resetPlayer(String playerUuid) {
        UUID uuid = UUID.fromString(playerUuid);
        playerData.remove(uuid);
        uncertainty.remove(uuid);
        buffer.remove(uuid);
        tickCount.remove(uuid);
    }

    @Override
    public void onMove(NeonACPlayer player, PlayerMovePacket packet) {
        if (isExempt(player)) return;
        if (player.isFlying()) return;

        UUID uuid = player.getUniqueId();
        PlayerData data = playerData.computeIfAbsent(uuid,
                k -> new PlayerData((org.bukkit.entity.Player) player.getPlatformPlayer(), engine.getTick()));

        data.updateFromBukkit();
        data.doBaseTick();

        UncertaintyHandler unc = uncertainty.computeIfAbsent(uuid, k -> new UncertaintyHandler());
        unc.update(data);

        int ticks = tickCount.merge(uuid, 1, Integer::sum);
        if (ticks < 5) return;

        Vector3d predicted = PredictionEngine.predict(data);
        Vector3d resolved = PredictionEngine.resolveCollision(data, predicted);
        double offset = PredictionEngine.calculateOffset(data, resolved);
        offset = unc.reduceOffset(offset);

        double hUnc = unc.getHorizontalUncertainty(data);
        double vUnc = unc.getVerticalUncertainty(data);
        double totalUnc = Math.sqrt(hUnc * hUnc + vUnc * vUnc);

        double threshold = getConfig().getDouble("threshold", 0.001);
        double effectiveThreshold = threshold + totalUnc;

        double current = buffer.getOrDefault(uuid, 0.0);
        if (offset > effectiveThreshold) {
            current += offset;
            buffer.put(uuid, current);

            double alertAt = getConfig().getAlertThreshold();
            if (current >= alertAt) {
                flag(player, Math.min(1.0, current / 2.0),
                        Map.of("offset", String.format("%.4f", offset),
                                "threshold", String.format("%.4f", effectiveThreshold),
                                "predicted", String.format("%.4f,%.4f,%.4f", resolved.x, resolved.y, resolved.z),
                                "actual", String.format("%.4f,%.4f,%.4f", data.actualMovement.x, data.actualMovement.y, data.actualMovement.z)));
                buffer.put(uuid, 0.0);
            }
        } else {
            double decay = getConfig().getVlDecay();
            double next = Math.max(0, current - decay);
            buffer.put(uuid, next);
        }
    }
}
